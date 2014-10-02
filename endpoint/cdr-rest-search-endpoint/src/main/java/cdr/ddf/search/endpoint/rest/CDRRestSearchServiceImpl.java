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
package cdr.ddf.search.endpoint.rest;

import java.io.Serializable;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;

import cdr.ddf.commons.query.rest.CDRQueryImpl;
import cdr.ddf.commons.query.rest.parsers.QueryParser;
import cdr.ddf.commons.query.util.QueryHelper;
import cdr.ddf.commons.util.SearchConstants;
import ddf.catalog.CatalogFramework;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.federation.FederationException;
import ddf.catalog.federation.FederationStrategy;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.transform.CatalogTransformerException;

/**
 * JAX-RS Web Service which implements the CDR REST Search Specification which is based on Open Search
 * 
 * @author Jeff Vettraino
 */
// @OutInterceptors( interceptors = "com.cohesive.ddf.util.exi.EXIStaxOutInterceptor" } )
@Path( "/" )
public class CDRRestSearchServiceImpl {

    private static final XLogger LOGGER = new XLogger( LoggerFactory.getLogger( CDRRestSearchServiceImpl.class ) );

    private CatalogFramework catalogFramework = null;
    private ConfigurationWatcherImpl platformConfig = null;
    private FilterBuilder filterBuilder = null;
    private QueryParser queryParser = null;

    private FederationStrategy fifoFedStrategy = null;

    /**
     * Constructor for JAX RS CDR Search Service. Values should ideally be passed into the constructor using a
     * dependency injection framework like blueprint
     * 
     * @param framework
     *            Catalog Framework which will be used for search
     * @param config
     *            ConfigurationWatcherImpl used to get the platform configuration values
     * @param builder
     *            FilterBuilder implementation
     * @param parser
     *            The instance of the QueryParser to use which will determine how to parse the parameters from the queyr
     *            String. Query parsers are tied to different versions of a query profile
     */
    public CDRRestSearchServiceImpl( CatalogFramework framework, ConfigurationWatcherImpl config, FilterBuilder builder, QueryParser parser, FederationStrategy fifoFedStrategy ) {
        this.catalogFramework = framework;
        this.platformConfig = config;
        this.filterBuilder = builder;
        this.queryParser = parser;
        this.fifoFedStrategy = fifoFedStrategy;

    }

    @HEAD
    public Response ping( @Context UriInfo uriInfo, @HeaderParam( "Accept-Encoding" ) String encoding, @HeaderParam( "Authorization" ) String auth ) {
        boolean isValid = queryParser.isValidQuery( uriInfo.getQueryParameters(), platformConfig.getSiteName() );
        return isValid ? Response.ok().build() : Response.status( Response.Status.BAD_REQUEST ).build();
    }

    /**
     * Search method that gets called when issuing an HTTP GET to the corresponding URL. HTTP GET URL query parameters
     * contain the query criteria values
     * 
     * @param uriInfo
     *            Query parameters obtained by e
     * @param encoding
     * @param auth
     * @return
     */
    @GET
    public Response search( @Context UriInfo uriInfo, @HeaderParam( "Accept-Encoding" ) String encoding, @HeaderParam( "Authorization" ) String auth ) {
        LOGGER.debug( "Query received: " + uriInfo.getRequestUri() );

        Response response = null;
        try {
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            CDRQueryImpl query = new CDRQueryImpl( filterBuilder, queryParameters, queryParser, true, platformConfig.getSiteName() );

            QueryResponse queryResponse = catalogFramework.query(
                    new QueryRequestImpl( query, false, query.getSiteNames(), queryParser.getQueryProperties( queryParameters, platformConfig.getSiteName() ) ), fifoFedStrategy );

            Map<String, Serializable> transformProperties = QueryHelper.getTransformLinkProperties( uriInfo, query, queryResponse, platformConfig.getProtocol(), platformConfig.getHostname(),
                    platformConfig.getPort() );
            transformProperties.put( SearchConstants.FEED_TITLE, "Atom Search Results from '" + platformConfig.getSiteName() + "' for Query: " + query.getHumanReadableQuery().trim() );
            transformProperties.put( SearchConstants.FORMAT_PARAMETER, query.getResponseFormat() );
            transformProperties.put( SearchConstants.STATUS_PARAMETER, Boolean.valueOf( queryParser.isIncludeStatus( queryParameters ) ) );
            transformProperties.put( SearchConstants.LOCAL_SOURCE_ID, catalogFramework.getId() );
            String format = query.getResponseFormat();
            BinaryContent content = catalogFramework.transform( queryResponse, format == null ? "atom" : format, transformProperties );

            response = Response.ok( content.getInputStream(), content.getMimeTypeValue() ).build();

        } catch ( UnsupportedQueryException e ) {
            LOGGER.error( e.getMessage(), e );
            response = Response.status( Response.Status.BAD_REQUEST ).build();
        } catch ( SourceUnavailableException e ) {
            LOGGER.error( e.getMessage(), e );
            response = Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        } catch ( FederationException e ) {
            LOGGER.error( e.getMessage(), e );
            response = Response.status( Response.Status.BAD_REQUEST ).build();
        } catch ( CatalogTransformerException e ) {
            LOGGER.error( e.getMessage(), e );
            response = Response.status( Response.Status.BAD_REQUEST ).build();
        } catch ( Exception e ) {
            LOGGER.error( e.getMessage(), e );
            response = Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }

        return response;
    }
}
