/**
 * Copyright (c) Cohesive Integrations, LLC
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or any later version. 
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 * 
 **/
package net.di2e.ecdr.source.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.di2e.ecdr.commons.filter.StrictFilterDelegate;
import net.di2e.ecdr.commons.filter.config.FilterConfig;
import net.di2e.ecdr.commons.filter.config.FilterConfig.SingleRecordQueryMethod;
import net.di2e.ecdr.commons.util.SearchConstants;
import net.di2e.ecdr.search.api.DynamicExternalSource;
import net.di2e.ecdr.search.transform.atom.response.AtomResponseTransformer;
import net.di2e.ecdr.security.ssl.client.cxf.CxfSSLClientConfiguration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.transport.http.HTTPConduit;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.ContentType;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.operation.Query;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.impl.ResourceRequestByProductUri;
import ddf.catalog.operation.impl.ResourceResponseImpl;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.catalog.resource.impl.ResourceImpl;
import ddf.catalog.source.ConnectedSource;
import ddf.catalog.source.FederatedSource;
import ddf.catalog.source.SourceMonitor;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.util.impl.MaskableImpl;

public abstract class AbstractCDRSource extends MaskableImpl implements FederatedSource, ConnectedSource, DynamicExternalSource {

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractCDRSource.class );

    // TODO check the retrieve resuming from previous place capability
    // compare with existing DDF code as it may have been updated recently
    private static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HEADER_RANGE = "Range";
    private static final String BYTES_TO_SKIP = "BytesToSkip";
    private static final String BYTES_SKIPPED_RESPONSE = "BytesSkipped";
    private static final String BYTES = "bytes";
    private static final String BYTES_EQUAL = "bytes=";

    public enum PingMethod {
        GET, HEAD, NONE
    };

    private SourceMonitor siteAvailabilityCallback = null;
    private FilterAdapter filterAdapter = null;

    private long availableCheckCacheTime = 60000;
    private Date lastAvailableCheckDate = null;
    private boolean isCurrentlyAvailable = false;

    private WebClient cdrRestClient = null;
    private WebClient cdrAvailabilityCheckClient = null;
    private PingMethod pingMethod = PingMethod.NONE;

    private long receiveTimeout = 0;
    private long connectionTimeout = 30000;
    private int maxResultsCount = 0;
    private String defaultResponseFormat = null;

    private CxfSSLClientConfiguration sslClientConfig = null;

    public AbstractCDRSource( FilterAdapter adapter, CxfSSLClientConfiguration sslClient ) {
        this.filterAdapter = adapter;
        this.sslClientConfig = sslClient;
    }

    public abstract Map<String, String> getDynamicUrlParameterMap();

    public abstract Map<String, String> getStaticUrlQueryValues();

    public abstract FilterConfig getFilterConfig();

    @Override
    public SourceResponse query( QueryRequest queryRequest ) throws UnsupportedQueryException {
        try {
            Query query = queryRequest.getQuery();
            // TODO Add in default radius
            Map<String, String> filterParameters = filterAdapter.adapt( query, new StrictFilterDelegate( false, 50000.00, getFilterConfig() ) );

            // Check to see if this is a remote Metacard Lookup
            Response response = getResponseIfRemoteMetacard( filterParameters );

            // If the custom metacard lookup didn't produce anything, then down
            // the normal query path
            if ( response == null ) {
                filterParameters.putAll( getIntialFilterParameters( queryRequest ) );
                setURLQueryString( filterParameters );

                LOGGER.debug( "Executing http GET query to source [{}] with url [{}]", getId(), cdrRestClient.getCurrentURI().toString() );
                response = cdrRestClient.get();
                LOGGER.debug( "Query to source [{}] returned http status code [{}] and media type [{}]", getId(), response.getStatus(), response.getMediaType() );
            }

            if ( response.getStatus() == Status.OK.getStatusCode() ) {
                AtomResponseTransformer transformer = new AtomResponseTransformer( getFilterConfig() );
                // TODO check why "atom" is passed in here
                SourceResponse sourceResponse = transformer.processSearchResponse( (InputStream) response.getEntity(), "atom", queryRequest, getId() );
                return sourceResponse;
            } else {
                Object entity = response.getEntity();
                if ( entity != null ) {
                    LOGGER.warn( "Error recieved when querying site [{}] \n[{}]", getId(), IOUtils.toString( (InputStream) entity ) );
                }
                throw new UnsupportedQueryException( "Query to remote source returned http status code " + response.getStatus() );
            }

        } catch ( Exception e ) {
            LOGGER.error( e.getMessage(), e );
            throw new UnsupportedQueryException( "Could not complete query to site [" + getId() + "] due to: " + e.getMessage(), e );
        }

    }

    private Response getResponseIfRemoteMetacard( Map<String, String> filterParameters ) {
        Response response = null;
        String metacardUrl = filterParameters.get( SingleRecordQueryMethod.ID_ELEMENT_URL.toString() );
        if ( metacardUrl != null ) {
            metacardUrl = URLDecoder.decode( metacardUrl );
            LOGGER.debug( "Retreiving the metadata from the following url [{}]", metacardUrl );
            response = WebClient.create( metacardUrl ).get();
        }
        // TODO support self link releation URL
        return response;
    }

    @Override
    public boolean isAvailable() {
        LOGGER.debug( "isAvailable method called on CDR Rest Source named [{}], determining whether to check availability or pull from cache", getId() );
        if ( pingMethod != null && !PingMethod.NONE.equals( pingMethod ) && cdrAvailabilityCheckClient != null ) {
            if ( !isCurrentlyAvailable || (lastAvailableCheckDate.getTime() < System.currentTimeMillis() - availableCheckCacheTime) ) {
                LOGGER.debug( "Checking availability on CDR Rest Source named [{}] in real time by calling endpoint [{}]", getId(), cdrAvailabilityCheckClient.getBaseURI() );
                try {
                    Response response = PingMethod.HEAD.equals( pingMethod ) ? cdrAvailabilityCheckClient.head() : cdrAvailabilityCheckClient.get();
                    if ( response.getStatus() == Status.OK.getStatusCode() || response.getStatus() == Status.ACCEPTED.getStatusCode() ) {
                        isCurrentlyAvailable = true;
                        lastAvailableCheckDate = new Date();
                    } else {
                        isCurrentlyAvailable = false;
                    }
                } catch ( Exception e ) {
                    LOGGER.warn( "CDR Rest Source named [" + getId() + "] encountered error while executing HTTP Head at URL [" + cdrAvailabilityCheckClient.getBaseURI() + "]:" + e.getMessage() );

                }

            } else {
                LOGGER.debug( "Pulling availability of CDR Rest Federated Source named [{}] from cache, isAvaialble=[{}]", getId(), isCurrentlyAvailable );
            }
            if ( siteAvailabilityCallback != null ) {
                if ( isCurrentlyAvailable ) {
                    siteAvailabilityCallback.setAvailable();
                } else {
                    siteAvailabilityCallback.setUnavailable();
                }
            }
        } else {
            LOGGER.debug( "HTTP Ping is set to false so not checking the sites availability, just setting to available" );
            isCurrentlyAvailable = true;
            if ( siteAvailabilityCallback != null ) {
                siteAvailabilityCallback.setAvailable();
            }
        }
        return isCurrentlyAvailable;
    }

    @Override
    public boolean isAvailable( SourceMonitor callback ) {
        this.siteAvailabilityCallback = callback;
        return isAvailable();
    }

    @Override
    public Set<ContentType> getContentTypes() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getOptions( Metacard paramMetacard ) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedSchemes() {
        return Collections.emptySet();
    }

    @Override
    public ResourceResponse retrieveResource( URI uri, Map<String, Serializable> requestProperties ) throws ResourceNotFoundException, ResourceNotSupportedException, IOException {
        LOGGER.debug( "Retrieving Resource from remote CDR Source named [{}] using URI [{}]", getId(), uri );

        // Check to see if the resource-uri value was passed through which is
        // the original metacard uri which
        // can be different from what was returned or used by the client
        Serializable resourceUriProperty = requestProperties.get( Metacard.RESOURCE_URI );
        if ( resourceUriProperty != null && resourceUriProperty instanceof URI ) {
            URI resourceUri = (URI) resourceUriProperty;
            if ( !resourceUri.equals( uri ) ) {
                LOGGER.debug( "Overriding the passed in resourceUri [{}] with the value found in the request properties [{}]", uri, resourceUri );
                uri = resourceUri;
            }

        } else if ( uri != null ) {
            String scheme = uri.getScheme();
            if ( !"http".equalsIgnoreCase( scheme ) && !"https".equalsIgnoreCase( scheme ) ) {
                uri = getURIFromMetacard( uri );
            }
        }

        if ( uri != null ) {
            WebClient retreiveWebClient = WebClient.create( uri );
            Long bytesToSkip = null;
            // If a bytesToSkip property is present add range header
            if ( requestProperties.containsKey( BYTES_TO_SKIP ) ) {
                bytesToSkip = (Long) requestProperties.get( BYTES_TO_SKIP );
                if ( bytesToSkip != null ) {
                    LOGGER.debug( "Setting Range header on retrieve request from remote CDR Source [{}] with bytes to skip [{}]", getId(), bytesToSkip );
                    // This creates a Range header in the following manner if
                    // 100 bytes were to be skipped. The end -
                    // means its open ended
                    // Range: bytes=100-
                    retreiveWebClient.header( HEADER_RANGE, BYTES_EQUAL + bytesToSkip + "-" );
                }
            }

            Response clientResponse = retreiveWebClient.get();

            MediaType mediaType = clientResponse.getMediaType();
            MimeType mimeType = null;
            try {
                mimeType = (mediaType == null) ? new MimeType( "application/octet-stream" ) : new MimeType( mediaType.toString() );
                LOGGER.debug( "Creating mime type from CDR Source named [{}] using uri [{}] with value [{}] defaulting to [{}]", getId(), uri, mediaType );
            } catch ( MimeTypeParseException e ) {
                try {
                    mimeType = new MimeType( "application/octet-stream" );
                    LOGGER.warn( "Creating mime type from CDR Source named [{}] using uri [{}] with value [{}] defaulting to [{}]", getId(), uri, "application/octet-stream" );
                } catch ( MimeTypeParseException e1 ) {
                    LOGGER.error( "Could not create MIMEType for resource being retrieved", e1 );
                }

            }

            String dispositionString = clientResponse.getHeaderString( HEADER_CONTENT_DISPOSITION );

            String fileName = null;
            if ( dispositionString != null ) {
                ContentDisposition contentDisposition = new ContentDisposition( dispositionString );
                fileName = contentDisposition.getParameter( "filename" );
                if ( fileName == null ) {
                    fileName = contentDisposition.getParameter( "\"filename\"" );
                }
                if ( fileName == null ) {
                    // TODO use MIMEType parser to get the file extension in
                    // this case
                    fileName = getId() + "-" + System.currentTimeMillis();
                }
            } else {
                // TODO use MIMEType parser to get the file extension in this
                // case
                fileName = getId() + "-" + System.currentTimeMillis();
            }

            InputStream binaryStream = (InputStream) clientResponse.getEntity();
            if ( binaryStream != null ) {
                Map<String, Serializable> responseProperties = new HashMap<String, Serializable>();
                if ( bytesToSkip != null ) {
                    // Since we sent a range header an accept-ranges header
                    // should be returned if the
                    // remote endpoint support it. If is not present, the
                    // inputStream hasn't skipped ahead
                    // by the given number of bytes, so we need to take care of
                    // it here.
                    String rangeHeader = clientResponse.getHeaderString( HEADER_ACCEPT_RANGES );
                    if ( rangeHeader == null || !rangeHeader.equals( BYTES ) ) {
                        LOGGER.debug( "Skipping {} bytes in CDR Remote Source because endpoint didn't support Range Headers", bytesToSkip );
                        binaryStream.skip( bytesToSkip );
                        responseProperties.put( BYTES_SKIPPED_RESPONSE, Boolean.TRUE );
                    }
                }
                return new ResourceResponseImpl( new ResourceRequestByProductUri( uri, requestProperties ), responseProperties, new ResourceImpl( binaryStream, mimeType, fileName ) );
            }
        }
        LOGGER.warn( "Could not retrieve resource from CDR Source named [{}] using uri [{}]", getId(), uri );
        throw new ResourceNotFoundException( "Could not retrieve resource from source [" + getId() + "] and uri [" + uri + "]" );
    }

    protected void setURLQueryString( Map<String, String> filterParameters ) {
        cdrRestClient.resetQuery();
        Map<String, String> urlParameterMap = getDynamicUrlParameterMap();
        // If there is no dynamic mapping then just use the defaults
        if ( urlParameterMap == null || urlParameterMap.isEmpty() ) {
            for ( Entry<String, String> entry : filterParameters.entrySet() ) {
                cdrRestClient.replaceQueryParam( entry.getKey(), entry.getValue() );
            }
            // Dynamic paraameter map exists so use that and do the mapping
        } else {

            for ( Entry<String, String> entry : filterParameters.entrySet() ) {
                String parameterName = urlParameterMap.get( entry.getKey() );
                if ( StringUtils.isNotBlank( parameterName ) ) {
                    // TODO fix this
                    cdrRestClient.replaceQueryParam( parameterName, entry.getValue() );
                }
                // else if ( Metacard.ID.equals( entry.getKey() ) ) {
                // System.out.println( "Retreiveing value: " + entry.getValue()
                // );
                // cdrRestClient = WebClient.create( entry.getValue(), true );
                // addHardocded = false;
                // }
            }
        }

        Map<String, String> hardcodedQueryParams = getStaticUrlQueryValues();
        for ( Entry<String, String> entry : hardcodedQueryParams.entrySet() ) {
            cdrRestClient.replaceQueryParam( entry.getKey(), entry.getValue() );
        }
    }

    protected Map<String, String> getIntialFilterParameters( QueryRequest request ) {
        Map<String, String> filterParameters = new HashMap<String, String>();
        Map<String, Serializable> queryRequestProps = request.getProperties();

        if ( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "CDR REST Source recieved Query: " + ToStringBuilder.reflectionToString( request.getQuery() ) );
        }

        // include format parameter
        String format = (String) queryRequestProps.get( SearchConstants.FORMAT_PARAMETER );
        if ( format != null ) {
            filterParameters.put( SearchConstants.FORMAT_PARAMETER, format );
        } else {
            if ( defaultResponseFormat != null ) {
                filterParameters.put( SearchConstants.FORMAT_PARAMETER, defaultResponseFormat );
            }
        }

        // Strict Mode
        Boolean strictMode = (Boolean) queryRequestProps.get( SearchConstants.STRICTMODE_PARAMETER );
        if ( strictMode != null ) {
            filterParameters.put( SearchConstants.STRICTMODE_PARAMETER, String.valueOf( strictMode ) );
        }

        Query query = request.getQuery();

        // Include timeout
        long timeout = query.getTimeoutMillis();
        if ( timeout > 1000 ) {
            filterParameters.put( SearchConstants.TIMEOUT_PARAMETER, String.valueOf( timeout ) );
        }

        int pageSize = query.getPageSize();
        filterParameters.put( SearchConstants.COUNT_PARAMETER, maxResultsCount > 0 && pageSize > maxResultsCount ? String.valueOf( maxResultsCount ) : String.valueOf( pageSize ) );

        int startIndex = query.getStartIndex();
        filterParameters.put( SearchConstants.STARTINDEX_PARAMETER, String.valueOf( getFilterConfig().isZeroBasedStartIndex() ? startIndex - 1 : startIndex ) );

        String sortOrderString = getSortOrderString( query.getSortBy() );
        if ( sortOrderString != null ) {
            filterParameters.put( SearchConstants.SORTKEYS_PARAMETER, sortOrderString );
        }
        return filterParameters;
    }

    private String getSortOrderString( SortBy sortBy ) {
        String sortOrderString = null;
        if ( sortBy != null ) {
            SortOrder sortOrder = sortBy.getSortOrder();
            String sortField = StrictFilterDelegate.SORTKEYS_MAP.get( sortBy.getPropertyName().getPropertyName() );
            if ( sortField != null ) {
                sortOrderString = sortField + (SortOrder.DESCENDING.equals( sortOrder ) ? ",,false" : "");
            }
        }
        return sortOrderString;
    }

    protected URI getURIFromMetacard( URI uri ) {
        URI returnUri = null;
        Map<String, String> uriMap = new HashMap<String, String>( 3 );
        uriMap.put( Metacard.RESOURCE_URI, uri.toString() );
        setURLQueryString( uriMap );
        Response response = cdrRestClient.get();
        AtomResponseTransformer transformer = new AtomResponseTransformer( getFilterConfig() );
        SourceResponse sourceResponse = transformer.processSearchResponse( (InputStream) response.getEntity(), "atom", null, getId() );
        List<Result> results = sourceResponse.getResults();
        if ( !results.isEmpty() ) {
            returnUri = results.get( 0 ).getMetacard().getResourceURI();
        }
        return returnUri;
    }

    public void setEndpointUrl( String endpointUrl ) {
        String existingUrl = cdrRestClient == null ? null : cdrRestClient.getCurrentURI().toString();
        if ( StringUtils.isNotBlank( endpointUrl ) && !endpointUrl.equals( existingUrl ) ) {
            LOGGER.debug( "ConfigUpdate: Updating the source endpoint url value from [{}] to [{}] for sourceId [{}]", existingUrl, endpointUrl, getId() );
            cdrRestClient = WebClient.create( endpointUrl, true );
            synchronized ( cdrRestClient ) {
                HTTPConduit conduit = WebClient.getConfig( cdrRestClient ).getHttpConduit();
                conduit.getClient().setReceiveTimeout( receiveTimeout );
                conduit.getClient().setConnectionTimeout( connectionTimeout );
                conduit.setTlsClientParameters( sslClientConfig.getTLSClientParameters() );
            }


        } else {
            LOGGER.warn( "OpenSearch Source Endpoint URL is not a valid value, so cannot update [{}]", endpointUrl );
        }
    }

    public void setPingUrl( String url ) {
        if ( StringUtils.isNotBlank( url ) ) {
            LOGGER.debug( "ConfigUpdate: Updating the ping (site availability check) endpoint url value from [{}] to [{}]", cdrAvailabilityCheckClient == null ? null : cdrAvailabilityCheckClient
                    .getCurrentURI().toString(), url );

            cdrAvailabilityCheckClient = WebClient.create( url, true );
            synchronized ( cdrAvailabilityCheckClient ) {
                HTTPConduit conduit = WebClient.getConfig( cdrAvailabilityCheckClient ).getHttpConduit();
                conduit.getClient().setReceiveTimeout( receiveTimeout );
                conduit.getClient().setConnectionTimeout( connectionTimeout );
                conduit.setTlsClientParameters( sslClientConfig.getTLSClientParameters() );
            }
        } else {
            LOGGER.debug( "ConfigUpdate: Updating the ping (site availability check) endpoint url to [null], will not be performing ping checks" );
        }
    }

    public void setPingMethodString( String method ) {

        try {
            LOGGER.debug( "ConfigUpdate: Updating the httpPing method value from [{}] to [{}]", pingMethod, method );
            pingMethod = PingMethod.valueOf( method );
        } catch ( IllegalArgumentException | NullPointerException e ) {
            LOGGER.warn( "Could not update the http ping method due to invalid valus [{}], so leaving at [{}]", method, pingMethod );
        }
    }

    public void setPingMethod( PingMethod method ) {
        LOGGER.debug( "ConfigUpdate: Updating the httpPing method value from [{}] to [{}]", pingMethod, method );
        pingMethod = method;
    }

    /**
     * Sets the time (in seconds) that availability should be cached (that is, the minimum amount of time between 2
     * perform availability checks). For example if set to 60 seconds, then if an availability check is called 30
     * seconds after a previous availability check was called, the second call will just return a cache value and not do
     * another check.
     * 
     * This settings allow admins to ensure that a site is not overloaded with availability checks
     * 
     * @param newCacheTime
     *            New time period, in seconds, to check the availability of the federated source.
     */
    public void setAvailableCheckCacheTime( long newCacheTime ) {
        if ( newCacheTime < 1 ) {
            newCacheTime = 1;
        }
        LOGGER.debug( "ConfigUpdate: Updating the Availanle Check Cache Time value from [{}] to [{}] seconds", availableCheckCacheTime / 1000, newCacheTime );
        this.availableCheckCacheTime = newCacheTime * 1000;
    }

    public void setReceiveTimeoutSeconds( Integer seconds ) {
        seconds = seconds == null ? 0 : seconds;
        long millis = seconds * 1000L;
        if ( millis != receiveTimeout ) {
            LOGGER.debug( "ConfigUpdate: Updating the source endpoint receive timeout value from [{}] to [{}] milliseconds", receiveTimeout, millis );
            receiveTimeout = millis;
            WebClient.getConfig( cdrRestClient ).getHttpConduit().getClient().setReceiveTimeout( receiveTimeout );
        }
    }

    public void setConnectionTimeoutSeconds( Integer seconds ) {
        seconds = seconds == null ? 0 : seconds;
        long millis = seconds * 1000L;
        if ( millis != connectionTimeout ) {
            LOGGER.debug( "ConfigUpdate: Updating the source endpoint connection timeout value from [{}] to [{}] milliseconds", connectionTimeout, millis );
            connectionTimeout = millis;
            WebClient.getConfig( cdrRestClient ).getHttpConduit().getClient().setConnectionTimeout( connectionTimeout );
        }
    }

    public void setMaxResultCount( Integer count ) {
        count = count == null ? 0 : count;
        if ( count != maxResultsCount ) {
            LOGGER.debug( "ConfigUpdate: Updating the max results count value from [{}] to [{}]", maxResultsCount, count );
            maxResultsCount = count;
        }
    }

    public void setDefaultResponseFormat( String defaultFormat ) {
        LOGGER.debug( "ConfigUpdate: Updating the default response format value from [{}] to [{}]", defaultResponseFormat, defaultFormat );
        defaultResponseFormat = defaultFormat;
    }
}
