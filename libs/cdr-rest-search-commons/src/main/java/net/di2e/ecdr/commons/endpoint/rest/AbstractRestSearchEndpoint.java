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
package net.di2e.ecdr.commons.endpoint.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.query.rest.CDRQueryImpl;
import net.di2e.ecdr.commons.query.rest.parsers.QueryParser;
import net.di2e.ecdr.commons.query.util.QueryHelper;
import net.di2e.ecdr.search.transform.mapper.TransformIdMapper;

import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.registry.api.RegistrableService;

public abstract class AbstractRestSearchEndpoint implements RegistrableService {

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractRestSearchEndpoint.class );

    private CatalogFramework catalogFramework = null;
    private ConfigurationWatcherImpl platformConfig = null;
    private FilterBuilder filterBuilder = null;
    private QueryParser queryParser = null;

    private TransformIdMapper transformMapper = null;

    /**
     * Constructor for JAX RS CDR Search Service. Values should ideally be
     * passed into the constructor using a dependency injection framework like
     * blueprint
     *
     * @param framework
     *            Catalog Framework which will be used for search
     * @param config
     *            ConfigurationWatcherImpl used to get the platform
     *            configuration values
     * @param builder
     *            FilterBuilder implementation
     * @param parser
     *            The instance of the QueryParser to use which will determine
     *            how to parse the parameters from the queyr String. Query
     *            parsers are tied to different versions of a query profile
     */
    public AbstractRestSearchEndpoint( CatalogFramework framework, ConfigurationWatcherImpl config, FilterBuilder builder, QueryParser parser,
            TransformIdMapper mapper ) {
        this.catalogFramework = framework;
        this.platformConfig = config;
        this.filterBuilder = builder;
        this.queryParser = parser;
        this.transformMapper = mapper;
    }

    public Response executePing( UriInfo uriInfo, String encodingHeader, String authHeader ) {
        boolean isValid = queryParser.isValidQuery( uriInfo.getQueryParameters(), platformConfig.getSiteName() );
        return isValid ? Response.ok().build() : Response.status( Response.Status.BAD_REQUEST ).build();
    }

    /**
     * Search method that gets called when issuing an HTTP GET to the
     * corresponding URL. HTTP GET URL query parameters contain the query
     * criteria values
     *
     * @param uriInfo
     *            Query parameters obtained by e
     * @param encoding
     *            accept-encoding from the client
     * @param auth
     *            Authorization header
     * @return Response to send back to the calling client
     */
    public Response executeSearch( UriInfo uriInfo, String encoding, String auth ) {
        Response response;
        try {
            String localSourceId = platformConfig.getSiteName();
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            CDRQueryImpl query = new CDRQueryImpl( filterBuilder, queryParameters, queryParser, useDefaultSortIfNotSpecified(), localSourceId );

            QueryResponse queryResponse = executeQuery( localSourceId, queryParameters, query );

            // Move the specific links into Atom Transformer if possible
            Map<String, Serializable> transformProperties = QueryHelper.getTransformLinkProperties( uriInfo, query, queryResponse,
                    platformConfig.getSchemeFromProtocol(), platformConfig.getHostname(), platformConfig.getPort() );
            transformProperties.put( SearchConstants.FEED_TITLE, "Atom Search Results from '" + localSourceId + "' for Query: "
                    + query.getHumanReadableQuery().trim() );
            transformProperties.put( SearchConstants.FORMAT_PARAMETER, query.getResponseFormat() );
            transformProperties.put( SearchConstants.STATUS_PARAMETER, queryParser.isIncludeStatus( queryParameters ) );
            transformProperties.put( SearchConstants.LOCAL_SOURCE_ID, catalogFramework.getId() );
            transformProperties.put( SearchConstants.GEORSS_RESULT_FORMAT_PARAMETER, queryParser.getGeoRSSFormat( queryParameters ) );

            String format = query.getResponseFormat();

            String internalTransformerFormat = transformMapper.getQueryResponseTransformValue( format );
            transformProperties.put( SearchConstants.METACARD_TRANSFORMER_NAME, transformMapper.getMetacardTransformValue( format ) );
            BinaryContent content = catalogFramework.transform( queryResponse, internalTransformerFormat, transformProperties );

            try ( InputStream is = content.getInputStream() ) {
                response = Response.ok( is, content.getMimeTypeValue() ).build();
            } catch ( IOException e ) {
                LOGGER.error( "Error reading resposne [" + e.getMessage() + "]", e );
                response = Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
            }

        } catch ( UnsupportedQueryException e ) {
            LOGGER.error( e.getMessage(), e );
            response = Response.status( Response.Status.BAD_REQUEST ).build();
        } catch ( SourceUnavailableException e ) {
            LOGGER.error( e.getMessage(), e );
            response = Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        } catch ( FederationException e ) {
            LOGGER.error( e.getMessage(), e );
            response = Response.status( Response.Status.BAD_REQUEST ).build();
            // These exceptions happen when the transform is not available via
            // the framework or an exception occurs in translation
        } catch ( CatalogTransformerException | IllegalArgumentException e ) {
            LOGGER.error( e.getMessage(), e );
            response = Response.status( Response.Status.BAD_REQUEST ).build();
        } catch ( RuntimeException e ) {
            LOGGER.error( "Unexpected exception received [" + e.getMessage() + "]", e );
            response = Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }

        return response;
    }

    public abstract boolean useDefaultSortIfNotSpecified();

    public abstract QueryResponse executeQuery( String localSourceId, MultivaluedMap<String, String> queryParameters, CDRQueryImpl query )
            throws SourceUnavailableException, UnsupportedQueryException, FederationException;

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    protected CatalogFramework getCatalogFramework() {
        return catalogFramework;
    }

    protected QueryParser getQueryParser() {
        return queryParser;
    }

}
