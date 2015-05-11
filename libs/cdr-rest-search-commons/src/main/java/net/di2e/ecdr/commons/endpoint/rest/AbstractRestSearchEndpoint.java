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
package net.di2e.ecdr.commons.endpoint.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.di2e.ecdr.api.auditor.SearchAuditor;
import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.query.rest.CDRQueryImpl;
import net.di2e.ecdr.commons.query.rest.parsers.QueryParser;
import net.di2e.ecdr.commons.query.util.QueryHelper;
import net.di2e.ecdr.commons.xml.fs.SourceDescription;
import net.di2e.ecdr.commons.xml.osd.OpenSearchDescription;
import net.di2e.ecdr.commons.xml.osd.Query;
import net.di2e.ecdr.commons.xml.osd.SyndicationRight;
import net.di2e.ecdr.commons.xml.osd.Url;
import net.di2e.ecdr.search.transform.mapper.TransformIdMapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
    private List<SearchAuditor> auditors = null;

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
     *            how to parse the parameters from the query String. Query
     *            parsers are tied to different versions of a query profile
     */
    public AbstractRestSearchEndpoint( CatalogFramework framework, ConfigurationWatcherImpl config, FilterBuilder builder, QueryParser parser,
            TransformIdMapper mapper, List<SearchAuditor> auditorList ) {
        this.catalogFramework = framework;
        this.platformConfig = config;
        this.filterBuilder = builder;
        this.queryParser = parser;
        this.transformMapper = mapper;
        this.auditors = auditorList;
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
    public Response executeSearch( HttpServletRequest servletRequest, UriInfo uriInfo, String encoding, String auth ) {
        Response response;
        QueryResponse queryResponse = null;
        boolean success = false;
        try {
            String localSourceId = platformConfig.getSiteName();
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            CDRQueryImpl query = new CDRQueryImpl( filterBuilder, queryParameters, queryParser, useDefaultSortIfNotSpecified(), localSourceId );

            queryResponse = executeQuery( localSourceId, queryParameters, query );

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
                success = true;
            } catch ( IOException e ) {
                LOGGER.error( "Error reading response [" + e.getMessage() + "]", e );
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

        for ( SearchAuditor auditor : auditors ) {
            auditor.auditRESTQuery( servletRequest, queryResponse, response );
        }
        return response;
    }

    @GET
    @Path( "/osd.xml" )
    @Produces( "application/opensearchdescription+xml" )
    public Response getOSD() {
        OpenSearchDescription osd = new OpenSearchDescription();
        osd.setShortName( platformConfig.getSiteName() );
        osd.setDescription( getServiceDescription() );
        osd.setTags( "ecdr opensearch cdr ddf" );
        if ( StringUtils.isNotBlank( platformConfig.getOrganization() ) ) {
            osd.setDeveloper( platformConfig.getOrganization() );
        }
        if ( StringUtils.isNotBlank( platformConfig.getContactEmailAddress() ) ) {
            osd.setContact( platformConfig.getContactEmailAddress() );
        }
        Query query = new Query();
        query.setRole( "example" );
        query.setSearchTerms( "test" );
        osd.getQuery().add( query );
        osd.setSyndicationRight( SyndicationRight.OPEN );
        osd.getLanguage().add( MediaType.MEDIA_TYPE_WILDCARD );
        osd.getInputEncoding().add( StandardCharsets.UTF_8.name() );
        osd.getOutputEncoding().add( StandardCharsets.UTF_8.name() );

        // url example
        Url url = new Url();
        url.setType( MediaType.APPLICATION_ATOM_XML );
        url.setTemplate( generateTemplateUrl() );
        osd.getUrl().add( url );

        // federated sites
        for ( String curSource : catalogFramework.getSourceIds() ) {
            SourceDescription description = new SourceDescription();
            description.setSourceId( curSource );
            description.setShortName( curSource );
            osd.getAny().add( description );
        }

        StringWriter writer = new StringWriter();
        InputStream is = null;
        try {
            JAXBContext context = JAXBContext.newInstance( OpenSearchDescription.class, SourceDescription.class );
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            marshaller.setProperty( Marshaller.JAXB_FRAGMENT, true );
            marshaller.marshal( osd, writer );
            is = getClass().getResourceAsStream( "/templates/osd_info.template" );
            if ( is != null ) {
                String osdTemplate = IOUtils.toString( is );

                String responseStr = osdTemplate + writer.toString();
                return Response.ok( responseStr, MediaType.APPLICATION_XML_TYPE ).build();
            } else {
                return Response.serverError().entity( "COULD NOT LOAD OSD TEMPLATE." ).build();
            }
        } catch ( JAXBException | IOException e ) {
            LOGGER.warn( "Could not create OSD for client due to exception.", e );
            return Response.serverError().build();
        } finally {
            IOUtils.closeQuietly( is );
        }
    }

    private String generateTemplateUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append( platformConfig.getProtocol() );
        urlBuilder.append( platformConfig.getHostname() );
        urlBuilder.append( ":" );
        urlBuilder.append( platformConfig.getPort() );
        urlBuilder.append( getServiceRelativeUrl() );
        urlBuilder.append( getParameterTemplate() );
        return urlBuilder.toString();
    }

    public abstract String getParameterTemplate();

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
