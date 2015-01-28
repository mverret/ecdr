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
package net.di2e.ecdr.broker.endpoint.rest;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.di2e.ecdr.commons.endpoint.rest.AbstractRestSearchEndpoint;
import net.di2e.ecdr.commons.query.rest.CDRQueryImpl;
import net.di2e.ecdr.commons.query.rest.parsers.QueryParser;
import net.di2e.ecdr.federation.api.NormalizingFederationStrategy;
import net.di2e.ecdr.search.transform.mapper.TransformIdMapper;

import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;
import org.opengis.filter.sort.SortBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;
import ddf.catalog.federation.FederationException;
import ddf.catalog.federation.FederationStrategy;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;

/**
 * JAX-RS Web Service which implements the CDR REST Brokered Search Specification which is based on the CDR Search Spec
 * which is in turn based on Open Search
 * 
 * The key difference between CDR Brokered Search and CDR Search is that Brokered Search can route the search to
 * multiple sources (FederatedSource).
 *
 * @author Jeff Vettraino
 */
@Path( "/" )
public class CDRRestBrokerServiceImpl extends AbstractRestSearchEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger( CDRRestBrokerServiceImpl.class );

    private static final String RELATIVE_URL = "/services/cdr/broker/rest";

    private static final String SERVICE_TYPE = "CDR REST Brokered Search Service";

    private NormalizingFederationStrategy sortedFedStrategy = null;

    private FederationStrategy defaultFederationStrategy = null;

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
    public CDRRestBrokerServiceImpl( CatalogFramework framework, ConfigurationWatcherImpl config, FilterBuilder builder, QueryParser parser, TransformIdMapper mapper,
            NormalizingFederationStrategy sortedFedStrategy, FederationStrategy defaultFedStrategy ) {
        super( framework, config, builder, parser, mapper );
        this.sortedFedStrategy = sortedFedStrategy;
        this.defaultFederationStrategy = defaultFedStrategy;
    }

    @HEAD
    public Response ping( @Context UriInfo uriInfo, @HeaderParam( "Accept-Encoding" ) String encoding, @HeaderParam( "Authorization" ) String auth ) {
        Response response = executePing( uriInfo, encoding, auth );
        LOGGER.debug( "Ping (HTTP HEAD) was called to check if the CDR Broker Endpoint is available, result is [{}]", response.getStatus() );
        return response;
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
        LOGGER.debug( "Query received on CDR Broker Endpoint: {}", uriInfo.getRequestUri() );
        return executeSearch( uriInfo, encoding, auth );
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public String getServiceRelativeUrl() {
        return RELATIVE_URL;
    }

    @Override
    public String getServiceDescription() {
        return "Provides a RESTful search service using the CDR Brokered Search specification.";
    }

    @Override
    public String getParameterTemplate() {
        return "?q={os:searchTerms}&src={fs:routeTo?}&caseSensitive={caseSensitive?}&fuzzy={fuzzy?}&timeout={fs:maxTimeout?}&start={os:startIndex?}&uid={uid?}&strictMode={strictMode?}"
            + "&dtstart={time:start?}&dtend={time:end?}&dtType={time:type?}"
            + "&filter={fs:sourceFilter?}&sort={fs:sort?}&box={geo:box?}&lat={geo:lat?}&lon={geo:lon?}&radius={geo:radius?}&geometry={geo:geometry?}&polygon={polygon?}"
            + "&count={os:count?}&sortKeys={sru:sortKeys?}&status={cdrb:includeStatus?}&format={cdrs:responseFormat?}&timeout={cdrb:timeout?}&queryLanguage={queryLanguage?}&oid={oid?}";
    }

    @Override
    public boolean useDefaultSortIfNotSpecified() {
        return false;
    }

    @Override
    public QueryResponse executeQuery( String localSourceId, MultivaluedMap<String, String> queryParameters, CDRQueryImpl query ) throws SourceUnavailableException, UnsupportedQueryException,
            FederationException {
        Collection<String> siteNames = query.getSiteNames();

        QueryRequest queryRequest = new QueryRequestImpl( query, siteNames.isEmpty(), siteNames, getQueryParser().getQueryProperties( queryParameters, localSourceId ) );
        SortBy originalSortBy = getQueryParser().getSortBy( queryParameters );
        QueryResponse queryResponse = originalSortBy == null ? getCatalogFramework().query( queryRequest, defaultFederationStrategy ) : getCatalogFramework().query( queryRequest, sortedFedStrategy );
        return queryResponse;
    }

}
