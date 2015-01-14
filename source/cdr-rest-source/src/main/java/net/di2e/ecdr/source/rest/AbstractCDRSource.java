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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.net.ssl.KeyManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.filter.StrictFilterDelegate;
import net.di2e.ecdr.commons.filter.config.FilterConfig;
import net.di2e.ecdr.search.transform.atom.response.AtomResponseTransformer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.net.util.KeyManagerUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
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

public abstract class AbstractCDRSource extends MaskableImpl implements FederatedSource, ConnectedSource {

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractCDRSource.class );

    private static final String SSL_KEYSTORE_JAVA_PROPERTY = "javax.net.ssl.keyStore";
    private static final String SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY = "javax.net.ssl.keyStorePassword";

    private static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HEADER_RANGE = "Range";
    private static final String BYTES_TO_SKIP = "BytesToSkip";
    private static final String BYTES_SKIPPED_RESPONSE = "BytesSkipped";
    private static final String BYTES = "bytes";
    private static final String BYTES_EQUAL = "bytes=";

    private static final String MAP_ENTRY_DELIMITER = "=";

    public enum PingMethod {
        GET, HEAD, NONE
    }

    // matches 'user-friendly' OS terms with parameter
    private static Map<String, String> parameterMatchMap;

    static {
        parameterMatchMap = new HashMap<>();
        parameterMatchMap.put( "os:searchTerms", SearchConstants.KEYWORD_PARAMETER );
        parameterMatchMap.put( "os:count", SearchConstants.COUNT_PARAMETER );
        parameterMatchMap.put( "os:startIndex", SearchConstants.STARTINDEX_PARAMETER );
        parameterMatchMap.put( "time:start", SearchConstants.STARTDATE_PARAMETER );
        parameterMatchMap.put( "time:end", SearchConstants.ENDDATE_PARAMETER );
        parameterMatchMap.put( "geo:uid", SearchConstants.UID_PARAMETER );
        parameterMatchMap.put( "geo:box", SearchConstants.BOX_PARAMETER );
        parameterMatchMap.put( "geo:lat", SearchConstants.LATITUDE_PARAMETER );
        parameterMatchMap.put( "geo:lon", SearchConstants.LONGITUDE_PARAMETER );
        parameterMatchMap.put( "geo:radius", SearchConstants.RADIUS_PARAMETER );
        parameterMatchMap.put( "geo:geometry", SearchConstants.GEOMETRY_PARAMETER );
        parameterMatchMap.put( "sru:sortKeys", SearchConstants.SORTKEYS_PARAMETER );
    }

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

    private Map<String, String> parameterMap = new HashMap<>();

    private Map<String, String> sortMap = Collections.emptyMap();

    public AbstractCDRSource( FilterAdapter adapter ) {
        this.filterAdapter = adapter;
    }

    public abstract Map<String, String> getStaticUrlQueryValues();

    public abstract FilterConfig getFilterConfig();

    public abstract SourceResponse enhanceResults( SourceResponse response );

    public abstract SourceResponse lookupById( QueryRequest queryRequest, String id ) throws UnsupportedQueryException;

    @Override
    public SourceResponse query( QueryRequest queryRequest ) throws UnsupportedQueryException {
        try {
            Query query = queryRequest.getQuery();
            SourceResponse sourceResponse;
            // ECDR-72 Add in default radius
            Map<String, String> filterParameters = filterAdapter.adapt( query, new StrictFilterDelegate( false, 50000.00, getFilterConfig() ) );

            String id = filterParameters.get( SearchConstants.UID_PARAMETER );
            // check if this is an id-only query
            if ( StringUtils.isBlank( id ) ) {
                // non-id query, perform normal search
                sourceResponse = doQuery( filterParameters, queryRequest );
            } else {
                // id-only query, check if remote source supports it
                if ( parameterMap.containsKey( SearchConstants.UID_PARAMETER) || useDefaultParameters() ) {
                    sourceResponse = doQuery( filterParameters, queryRequest );
                } else {
                    sourceResponse = lookupById( queryRequest, id );
                }
            }
            return sourceResponse;

        } catch ( Exception e ) {
            LOGGER.error( e.getMessage(), e );
            throw new UnsupportedQueryException( "Could not complete query to site [" + getId() + "] due to: " + e.getMessage(), e );
        }
    }

    protected SourceResponse doQuery( Map<String, String> filterParameters, QueryRequest queryRequest ) throws UnsupportedQueryException {
        SourceResponse sourceResponse;
        filterParameters.putAll( getInitialFilterParameters( queryRequest ) );
        setURLQueryString( filterParameters );

        LOGGER.debug( "Executing http GET query to source [{}] with url [{}]", getId(), cdrRestClient.getCurrentURI().toString() );
        Response response = cdrRestClient.get();
        LOGGER.debug( "Query to source [{}] returned http status code [{}] and media type [{}]", getId(), response.getStatus(), response.getMediaType() );

        if ( response.getStatus() == Status.OK.getStatusCode() ) {
            AtomResponseTransformer transformer = new AtomResponseTransformer( getFilterConfig() );

            sourceResponse = transformer.processSearchResponse( (InputStream) response.getEntity(), queryRequest, getId() );
            sourceResponse = enhanceResults( sourceResponse );
        } else {
            Object entity = response.getEntity();
            if ( entity != null ) {
                try {
                    LOGGER.warn( "Error status code received [{}] when querying site [{}]:{}[{}]", response.getStatus(), getId(), System.lineSeparator(), IOUtils.toString( (InputStream) entity ) );
                } catch ( IOException e ) {
                    LOGGER.warn( "Error status code received [{}] when querying site [{}]", response.getStatus(), getId() );
                }
            } else {
                LOGGER.warn( "Error status code received [{}] when querying site [{}]", response.getStatus(), getId() );
            }
            throw new UnsupportedQueryException( "Query to remote source returned http status code " + response.getStatus() );
        }
        return sourceResponse;
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
                } catch ( RuntimeException e ) {
                    LOGGER.warn( "CDR Rest Source named [" + getId() + "] encountered an unexpected error while executing HTTP Head at URL [" + cdrAvailabilityCheckClient.getBaseURI() + "]:"
                            + e.getMessage() );
                }

            } else {
                LOGGER.debug( "Pulling availability of CDR Rest Federated Source named [{}] from cache, isAvailable=[{}]", getId(), isCurrentlyAvailable );
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

        ResourceResponse resourceResponse = null;
        if ( uri != null ) {
            LOGGER.debug( "Retrieving the remote resource using the uri [{}]", uri );
            WebClient retrieveWebClient = WebClient.create( uri );
            resourceResponse = doRetrieval( retrieveWebClient, requestProperties );
        }

        if ( resourceResponse == null ) {
            LOGGER.warn( "Could not retrieve resource from CDR Source named [{}] using uri [{}]", getId(), uri );
            throw new ResourceNotFoundException( "Could not retrieve resource from source [" + getId() + "] and uri [" + uri + "]" );
        }
        return resourceResponse;
    }

    protected ResourceResponse doRetrieval( WebClient retrieveWebClient, Map<String, Serializable> requestProperties ) throws ResourceNotFoundException, IOException {
        ResourceResponse resourceResponse = null;
        URI uri = retrieveWebClient.getCurrentURI();
        try {

            Long bytesToSkip = null;
            // If a bytesToSkip property is present add range header
            if ( requestProperties != null && requestProperties.containsKey( BYTES_TO_SKIP ) ) {
                bytesToSkip = (Long) requestProperties.get( BYTES_TO_SKIP );
                if ( bytesToSkip != null ) {
                    LOGGER.debug( "Setting Range header on retrieve request from remote CDR Source [{}] with bytes to skip [{}]", getId(), bytesToSkip );
                    // This creates a Range header in the following manner if
                    // 100 bytes were to be skipped. The end - means its open ended
                    // Range: bytes=100-
                    retrieveWebClient.header( HEADER_RANGE, BYTES_EQUAL + bytesToSkip + "-" );
                }
            }

            Response clientResponse = retrieveWebClient.get();

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
                    // ECDR-74 use MIMEType parser to get the file extension in
                    fileName = getId() + "-" + System.currentTimeMillis();
                }
            } else {
                // ECDR-74 use MIMEType parser to get the file extension in this case
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
                        try {
                            // the Java inputStream.skip() method is not guaranteed to skip all the bytes so we use a
                            // utility method that is
                            IOUtils.skipFully( binaryStream, bytesToSkip );
                        } catch ( EOFException e ) {
                            LOGGER.warn( "Skipping the requested number of bytes [{}] for URI [{}] resulted in an End of File, so re-retrieving the complete file without skipping bytes: {}",
                                    bytesToSkip, uri, e.getMessage() );
                            try {
                                binaryStream.close();
                            } catch ( IOException e1 ) {
                                LOGGER.debug( "Error encountered while closing inputstream" );
                            }
                            return doRetrieval( retrieveWebClient, null );
                        }
                    } else if ( rangeHeader != null && rangeHeader.equals( BYTES ) ) {
                        LOGGER.debug( "CDR Remote source supports Range Headers, only retrieving part of file that has not been downloaded yet." );
                        responseProperties.put( BYTES_SKIPPED_RESPONSE, Boolean.TRUE );
                    }
                }
                resourceResponse = new ResourceResponseImpl( new ResourceRequestByProductUri( uri, requestProperties ), responseProperties, new ResourceImpl( binaryStream, mimeType, fileName ) );
            }
        } catch ( RuntimeException e ) {
            LOGGER.warn( "Expected exception encountered when trying to retrieve resource with URI [{}] from source [{}}", uri, getId() );
        }
        return resourceResponse;
    }

    protected void setURLQueryString( Map<String, String> filterParameters ) {
        cdrRestClient.resetQuery();
        for ( Entry<String, String> entry : filterParameters.entrySet() ) {
            String parameterName = parameterMap.get( entry.getKey() );
            if ( StringUtils.isNotBlank( parameterName ) ) {
                cdrRestClient.replaceQueryParam( parameterName, entry.getValue() );
            } else if ( useDefaultParameters() ) {
                cdrRestClient.replaceQueryParam( entry.getKey(), entry.getValue() );
            }
        }

        Map<String, String> hardcodedQueryParams = getStaticUrlQueryValues();
        for ( Entry<String, String> entry : hardcodedQueryParams.entrySet() ) {
            cdrRestClient.replaceQueryParam( entry.getKey(), entry.getValue() );
        }
    }

    protected Map<String, String> getInitialFilterParameters( QueryRequest request ) {
        Map<String, String> filterParameters = new HashMap<String, String>();
        Map<String, Serializable> queryRequestProps = request.getProperties();

        if ( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "CDR REST Source received Query: " + ToStringBuilder.reflectionToString( request.getQuery() ) );
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

        for ( Entry<String, Serializable> entry : queryRequestProps.entrySet() ) {
            if ( parameterMap.containsKey( entry.getKey() ) ) {
                filterParameters.put( entry.getKey(), String.valueOf( entry.getValue() ) );
            }
        }

        return filterParameters;
    }

    private String getSortOrderString( SortBy sortBy ) {
        String sortOrderString = null;
        if ( sortBy != null ) {
            SortOrder sortOrder = sortBy.getSortOrder();
            String sortField = sortMap.get( sortBy.getPropertyName().getPropertyName() );
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
        SourceResponse sourceResponse = transformer.processSearchResponse( (InputStream) response.getEntity(), null, getId() );
        List<Result> results = sourceResponse.getResults();
        if ( !results.isEmpty() ) {
            returnUri = results.get( 0 ).getMetacard().getResourceURI();
        }
        return returnUri;
    }

    public synchronized void setUrl(String endpointUrl) {
        String existingUrl = cdrRestClient == null ? null : cdrRestClient.getCurrentURI().toString();
        if ( StringUtils.isNotBlank( endpointUrl ) && !endpointUrl.equals( existingUrl ) ) {
            LOGGER.debug( "ConfigUpdate: Updating the source endpoint url value from [{}] to [{}] for sourceId [{}]", existingUrl, endpointUrl, getId() );
            cdrRestClient = WebClient.create( endpointUrl, true );

            HTTPConduit conduit = WebClient.getConfig( cdrRestClient ).getHttpConduit();
            conduit.getClient().setReceiveTimeout( receiveTimeout );
            conduit.getClient().setConnectionTimeout( connectionTimeout );
            conduit.setTlsClientParameters( getTlsClientParameters() );
        } else {
            LOGGER.warn( "OpenSearch Source Endpoint URL is not a valid value (either blank or same as previous value), so cannot update [{}]", endpointUrl );
        }
    }

    public synchronized void setPingUrl( String url ) {
        if ( StringUtils.isNotBlank( url ) ) {
            LOGGER.debug( "ConfigUpdate: Updating the ping (site availability check) endpoint url value from [{}] to [{}]", cdrAvailabilityCheckClient == null ? null : cdrAvailabilityCheckClient
                    .getCurrentURI().toString(), url );

            cdrAvailabilityCheckClient = WebClient.create( url, true );

            HTTPConduit conduit = WebClient.getConfig( cdrAvailabilityCheckClient ).getHttpConduit();
            conduit.getClient().setReceiveTimeout( receiveTimeout );
            conduit.getClient().setConnectionTimeout( connectionTimeout );
            conduit.setTlsClientParameters( getTlsClientParameters() );
        } else {
            LOGGER.debug( "ConfigUpdate: Updating the ping (site availability check) endpoint url to [null], will not be performing ping checks" );
        }
    }

    /*
     * This method is needed because of a CXF deficiency of not using the keystore values from hte java system
     * properties. So this specifically pulls the values from the system properties then sets them to a KeyManager being
     * used
     */
    protected TLSClientParameters getTlsClientParameters() {
        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        String keystore = System.getProperty( SSL_KEYSTORE_JAVA_PROPERTY );
        String keystorePassword = System.getProperty( SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY );

        KeyManager[] keyManagers = null;
        if ( StringUtils.isNotBlank( keystore ) && keystorePassword != null ) {
            try {
                KeyManager manager = KeyManagerUtils.createClientKeyManager( new File( keystore ), keystorePassword );
                keyManagers = new KeyManager[1];
                keyManagers[0] = manager;

            } catch ( IOException | GeneralSecurityException ex ) {
                LOGGER.debug( "Could not access keystore {}, using default java keystore.", keystore );
            }
        }

        LOGGER.debug( "Setting the CXF KeyManager and TrustManager based on the Platform Global Configuration values" );
        tlsClientParameters.setKeyManagers( keyManagers );
        return tlsClientParameters;

    }

    public void setPingMethodString( String method ) {

        try {
            LOGGER.debug( "ConfigUpdate: Updating the httpPing method value from [{}] to [{}]", pingMethod, method );
            if ( method != null ) {
                pingMethod = PingMethod.valueOf( method );
            }
        } catch ( IllegalArgumentException e ) {
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
     * <p/>
     * This settings allow admins to ensure that a site is not overloaded with availability checks
     *
     * @param newCacheTime
     *            New time period, in seconds, to check the availability of the federated source.
     */
    public void setAvailableCheckCacheTime( long newCacheTime ) {
        if ( newCacheTime < 1 ) {
            newCacheTime = 1;
        }
        LOGGER.debug( "ConfigUpdate: Updating the Available Check Cache Time value from [{}] to [{}] seconds", availableCheckCacheTime / 1000, newCacheTime );
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

    public void setSortMap ( String sortMapStr ) {
        Map<String, String> convertedMap = convertToMap( sortMapStr );
        LOGGER.debug( "Updating sortMap with new entries: {}", convertedMap.toString() );
        sortMap = convertedMap;
    }

    protected void setCdrRestClient( WebClient restClient ) {
        this.cdrRestClient = restClient;
    }

    protected void setCdrAvailabilityCheckClient( WebClient availabilityCheckClient ) {
        this.cdrAvailabilityCheckClient = availabilityCheckClient;
    }

    abstract boolean useDefaultParameters();

    public void setParameterMap( String parameterMapStr ) {
        Map<String, String> convertedMap = convertToMap( parameterMapStr );
        Map<String, String> translateMap = new HashMap<>( convertedMap.size() );
        for ( Entry<String, String> entry : convertedMap.entrySet() ) {
            if ( parameterMatchMap.containsKey( entry.getKey() ) ) {
                translateMap.put( parameterMatchMap.get( entry.getKey() ), entry.getValue() );
            } else {
                translateMap.put( entry.getKey(), entry.getValue() );
            }
        }
        LOGGER.debug( "Updating parameterMap with new entries: {}", convertedMap.toString() );
        parameterMap = translateMap;
    }

    private Map<String, String> convertToMap( String mapStr) {
        Map<String, String> inputMap = new HashMap<String, String>();
        if ( StringUtils.isNotBlank( mapStr ) ) {
            for ( String sortPair : mapStr.split( "," ) ) {
                String[] pairAry = sortPair.split( MAP_ENTRY_DELIMITER );
                if ( pairAry.length == 2 ) {
                    inputMap.put( pairAry[0], pairAry[1] );
                } else {
                    LOGGER.warn( "Could not parse out map entry from {}, skipping this item.", sortPair );
                }
            }
        }
        return inputMap;
    }

}
