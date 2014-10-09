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
import net.di2e.ecdr.commons.util.SearchConstants;
import net.di2e.ecdr.search.transform.atom.response.AtomResponseTransformer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
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

public class CDROpenSearchSource extends MaskableImpl implements FederatedSource, ConnectedSource {

    private static final transient Logger LOGGER = LoggerFactory.getLogger( CDROpenSearchSource.class );
    
    public enum PingMethod { GET, HEAD, NONE };

    private static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HEADER_RANGE = "Range";
    private static final String BYTES_TO_SKIP = "BytesToSkip";
    private static final String BYTES_SKIPPED_RESPONSE = "BytesSkipped";
    private static final String BYTES = "bytes";
    private static final String BYTES_EQUAL = "bytes=";

    private SourceMonitor siteAvailabilityCallback = null;
    private FilterAdapter filterAdapter = null;

    private long availableCheckCacheTime = 60000;
    private Date lastAvailableCheckDate = null;
    private boolean isCurrentlyAvailable = false;
    private PingMethod pingMethod = PingMethod.NONE;
    private String defaultResponseFormat = null;

    private WebClient cdrRestClient = null;
    private WebClient cdrAvailabilityCheckClient = null;
    private String queryEndpointUrl = null;
    private String availabilityEndpointUrl = null;
    
    private Map<String,String> parameterMap = new HashMap<String,String>();
    private Map<String,String> hardCodedParameterMap = new HashMap<String,String>();

    public CDROpenSearchSource( FilterAdapter filterAdapter ) {

        this.filterAdapter = filterAdapter;
    }

    public void init() {

        updateQueryUrl();
    }

    public void destroy() {

    }

    @Override
    public SourceResponse query( QueryRequest queryRequest ) throws UnsupportedQueryException {
        try {
            Query query = queryRequest.getQuery();
            Map<String, String> filterParameters = filterAdapter.adapt( query, new StrictFilterDelegate( false, 50000.00 ) );
            filterParameters.putAll( getIntialFilterParameters( queryRequest ) );
            setURLQueryString( filterParameters );
           
            LOGGER.debug(  "Executing http GET query to source [{}] with url [{}]", getId(), cdrRestClient.getCurrentURI().toString() );
            Response response = cdrRestClient.get();
            LOGGER.debug( "Query to source [{}] returned http status code [{}] and media type [{}]", getId(), response.getStatus(), response.getMediaType() );
            if ( response.getStatus() == Status.OK.getStatusCode() ){
                AtomResponseTransformer transformer = new AtomResponseTransformer();
                SourceResponse sourceResponse = transformer.processSearchResponse( (InputStream) response.getEntity(), "atom", queryRequest, getId() );
                return sourceResponse;
            }else{
                throw new UnsupportedQueryException( "Query to remote source returned http status code " + response.getStatus() );
            }
            
        } catch ( Exception e ) {
            LOGGER.error( e.getMessage(), e );
            throw new UnsupportedQueryException( "Could not complete query to site [" + getId() + "] due to: " + e.getMessage(), e );
        }
        
    }

    @Override
    public boolean isAvailable() {
        LOGGER.debug( "isAvailable method called on CDR Rest Source named [{}], determining whether to check availability or pull from cache", getId() );
        if ( !PingMethod.NONE.equals( pingMethod ) && cdrAvailabilityCheckClient != null ) {
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
                LOGGER.debug( "Pulling availability f CDR Rest Federated Source named [{}] from cache, isAvaialble=[{}]", getId(), isCurrentlyAvailable );
            }
            if ( siteAvailabilityCallback != null ) {
                if ( isCurrentlyAvailable ) {
                    siteAvailabilityCallback.setAvailable();
                } else {
                    siteAvailabilityCallback.setUnavailable();
                }
            }
        } else {
            LOGGER.debug( "HTTP Ping is set to false so not checking the sites availablility, just setting ot available" );
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

        // Check to see if the resource-uri value was passed through which is the original metacard uri which
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

    public void setFilterAdapter( FilterAdapter adapter ) {
        this.filterAdapter = adapter;
    }

    // Not used right not since using container managed bean
    public void refresh( Map<String, Object> properties ) {

    }

    public void setEndpointUrl( String url ) {
        LOGGER.debug( "Updating the endpoint url value from [{}] to [{}]", queryEndpointUrl, url );
        this.queryEndpointUrl = url;
        updateQueryUrl();
    }
    
    public void setHardCodedParameters( String parameterString ){
        hardCodedParameterMap.clear();
        try{
            if ( StringUtils.isNotBlank( parameterString ) ){
                String[] params = parameterString.split( "," );
                for ( String param : params ) {
                    String[] singleParam = param.split( "=" );
                    hardCodedParameterMap.put( singleParam[0], singleParam[1] );
                }
            }
        }catch( Exception e){
            LOGGER.warn( "Could not update hard coded parameters", e.getMessage(), e );
        }
    }
    
    public void setAvailabilityUrl( String url ) {
        LOGGER.debug( "Updating the availability check (ping) endpoint url value from [{}] to [{}]", availabilityEndpointUrl, url );
        this.availabilityEndpointUrl = url;
        updateAvailabilityUrl();
    }

    public void setDefaultResponseFormat( String defaultFormat ) {
        this.defaultResponseFormat = defaultFormat;
    }

    public void setAvailabilityMethod( String method ) {
        LOGGER.debug( "Updating the httpPing value from [{}] to [{}]", pingMethod, method );
        pingMethod = PingMethod.valueOf( method );
    }
    
    public void setSearchTermsParameter( String searchTermsParameter ) {
        LOGGER.debug( "Updating os:searchTerms parameter from [{}] to [{}]", parameterMap.get( SearchConstants.KEYWORD_PARAMETER ), searchTermsParameter );
        parameterMap.put( SearchConstants.KEYWORD_PARAMETER, searchTermsParameter );
    }
    
    public void setCountParameter( String parameter ) {
        LOGGER.debug( "Updating os:count parameter from [{}] to [{}]", parameterMap.get( SearchConstants.COUNT_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.COUNT_PARAMETER, parameter );
    }
    
    public void setStartIndexParameter( String parameter ) {
        LOGGER.debug( "Updating os:startIndex parameter from [{}] to [{}]", parameterMap.get( SearchConstants.STARTINDEX_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.STARTINDEX_PARAMETER, parameter );
    }
    
    public void setStartTimeParameter( String parameter ) {
        LOGGER.debug( "Updating time:start parameter from [{}] to [{}]", parameterMap.get( SearchConstants.STARTDATE_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.STARTDATE_PARAMETER, parameter );
    }
    
    public void setEndTimeParameter( String parameter ) {
        LOGGER.debug( "Updating time:end parameter from [{}] to [{}]", parameterMap.get( SearchConstants.ENDDATE_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.ENDDATE_PARAMETER, parameter );
    }
    
    public void setBoxParameter( String parameter ) {
        LOGGER.debug( "Updating geo:box parameter from [{}] to [{}]", parameterMap.get( SearchConstants.BOX_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.BOX_PARAMETER, parameter );
    }
    
    public void setLatParameter( String parameter ) {
        LOGGER.debug( "Updating geo:lat parameter from [{}] to [{}]", parameterMap.get( SearchConstants.LATITUDE_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.LATITUDE_PARAMETER, parameter );
    }
    
    public void setLonParameter( String parameter ) {
        LOGGER.debug( "Updating geo:lon parameter from [{}] to [{}]", parameterMap.get( SearchConstants.LONGITUDE_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.LONGITUDE_PARAMETER, parameter );
    }
    
    public void setRadiusParameter( String parameter ) {
        LOGGER.debug( "Updating geo:radius parameter from [{}] to [{}]", parameterMap.get( SearchConstants.RADIUS_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.RADIUS_PARAMETER, parameter );
    }
    
    public void setGeometryParameter( String parameter ) {
        LOGGER.debug( "Updating geo:geometry parameter from [{}] to [{}]", parameterMap.get( SearchConstants.GEOMETRY_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.GEOMETRY_PARAMETER, parameter );
    }
    
    public void setSortKeysParameter( String parameter ) {
        LOGGER.debug( "Updating sru:sortKeys parameter from [{}] to [{}]", parameterMap.get( SearchConstants.SORTKEYS_PARAMETER ), parameter );
        parameterMap.put( SearchConstants.SORTKEYS_PARAMETER, parameter );
    }

    protected void updateQueryUrl() {
        if ( StringUtils.isNotBlank( queryEndpointUrl ) ) {
            cdrRestClient = WebClient.create( queryEndpointUrl, true );
        }
    }
    
    protected void updateAvailabilityUrl() {
        if ( StringUtils.isNotBlank( availabilityEndpointUrl ) ) {
            cdrAvailabilityCheckClient = WebClient.create( availabilityEndpointUrl, true );
        }else{
            cdrAvailabilityCheckClient = null;
        }
    }

    protected void setURLQueryString( Map<String, String> filterParameters ) {
        cdrRestClient.resetQuery();
        boolean addHardocded = true;
        for ( Entry<String, String> entry : filterParameters.entrySet() ) {
            String parameterName = parameterMap.get( entry.getKey() );
            System.out.println( "**** Parameter NAme: " + entry.getKey() );
            if ( StringUtils.isNotBlank( parameterName ) ){
                //TODO fix this
                cdrRestClient.replaceQueryParam( parameterName, entry.getKey().equals( SearchConstants.COUNT_PARAMETER ) ?  20 : entry.getValue() );
            }else if ( Metacard.ID.equals( entry.getKey() ) ){
                System.out.println( "Retreiveing value: " + entry.getValue() );
                cdrRestClient = WebClient.create( entry.getValue(), true );
                addHardocded = false;
            }
        }
        if ( addHardocded ){
            for ( Entry<String, String> entry : hardCodedParameterMap.entrySet() ){
                cdrRestClient.replaceQueryParam( entry.getKey(), entry.getValue() );
            }
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
        } else if ( defaultResponseFormat != null ) {
            filterParameters.put( SearchConstants.FORMAT_PARAMETER, defaultResponseFormat );
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

        filterParameters.put( SearchConstants.COUNT_PARAMETER, String.valueOf( query.getPageSize() ) );

        filterParameters.put( SearchConstants.STARTINDEX_PARAMETER, String.valueOf( query.getStartIndex() ) );

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
        AtomResponseTransformer transformer = new AtomResponseTransformer();
        SourceResponse sourceResponse = transformer.processSearchResponse( (InputStream) response.getEntity(), "atom", null, getId() );
        List<Result> results = sourceResponse.getResults();
        if ( !results.isEmpty() ) {
            returnUri = results.get( 0 ).getMetacard().getResourceURI();
        }
        return returnUri;
    }

}
