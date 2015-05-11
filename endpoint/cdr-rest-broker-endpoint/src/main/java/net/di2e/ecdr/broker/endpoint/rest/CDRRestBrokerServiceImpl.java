/**
 * Copyright (C) 2014 Cohesive Integrations, LLC (info@cohesiveintegrations.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.di2e.ecdr.broker.endpoint.rest;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.di2e.ecdr.api.auditor.SearchAuditor;
import net.di2e.ecdr.commons.endpoint.rest.AbstractRestSearchEndpoint;
import net.di2e.ecdr.commons.query.rest.CDRQueryImpl;
import net.di2e.ecdr.commons.query.rest.parsers.QueryParser;
import net.di2e.ecdr.federation.api.NormalizingFederationStrategy;
import net.di2e.ecdr.search.transform.mapper.TransformIdMapper;

import org.apache.cxf.jaxrs.ext.MessageContext;
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
 * JAX-RS Web Service which implements the CDR REST Brokered Search
 * Specification which is based on the CDR Search Spec which is in turn based on
 * Open Search
 * 
 * The key difference between CDR Brokered Search and CDR Search is that
 * Brokered Search can route the search to multiple sources (FederatedSource).
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
    public CDRRestBrokerServiceImpl( CatalogFramework framework, ConfigurationWatcherImpl config, FilterBuilder builder, QueryParser parser,
            TransformIdMapper mapper, NormalizingFederationStrategy sortedFedStrategy, FederationStrategy defaultFedStrategy, List<SearchAuditor> auditors ) {
        super( framework, config, builder, parser, mapper, auditors );
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
     * Search method that gets called when issuing an HTTP GET to the
     * corresponding URL. HTTP GET URL query parameters contain the query
     * criteria values
     *
     * @param uriInfo
     *            Query parameters obtained by e
     * @param encoding
     * @param auth
     * @return
     */
    @GET
    public Response search( @Context MessageContext context, @HeaderParam( "Accept-Encoding" ) String encoding, @HeaderParam( "Authorization" ) String auth ) {
        UriInfo uriInfo = context.getUriInfo();
        LOGGER.debug( "Query received on CDR Broker Endpoint: {}", uriInfo.getRequestUri() );
        return executeSearch( context.getHttpServletRequest(), uriInfo, encoding, auth );
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
        return "?q={os:searchTerms}&src={fs:routeTo?}&caseSensitive={caseSensitive?}&fuzzy={fuzzy?}&timeout={fs:maxTimeout?}&start={os:startIndex?}&strictMode={strictMode?}"
                + "&dtstart={time:start?}&dtend={time:end?}&dtType={time:type?}"
                + "&collections={ecdr:collections?}&sort={fs:sort?}"
                + "&box={geo:box?}&lat={geo:lat?}&lon={geo:lon?}&radius={geo:radius?}&geometry={geo:geometry?}&polygon={polygon?}&uid={geo:uid?}"
                + "&count={os:count?}&sortKeys={sru:sortKeys?}&status={cdrb:includeStatus?}&format={cdrs:responseFormat?}&timeout={cdrb:timeout?}&queryLanguage={queryLanguage?}&oid={oid?}";
    }

    @Override
    public boolean useDefaultSortIfNotSpecified() {
        return false;
    }

    @Override
    public QueryResponse executeQuery( String localSourceId, MultivaluedMap<String, String> queryParameters, CDRQueryImpl query )
            throws SourceUnavailableException, UnsupportedQueryException, FederationException {
        Collection<String> siteNames = query.getSiteNames();

        QueryRequest queryRequest = new QueryRequestImpl( query, siteNames.isEmpty(), siteNames, getQueryParser().getQueryProperties( queryParameters,
                localSourceId ) );
        SortBy originalSortBy = getQueryParser().getSortBy( queryParameters );
        QueryResponse queryResponse = originalSortBy == null ? getCatalogFramework().query( queryRequest, defaultFederationStrategy ) : getCatalogFramework()
                .query( queryRequest, sortedFedStrategy );
        return queryResponse;
    }

}
