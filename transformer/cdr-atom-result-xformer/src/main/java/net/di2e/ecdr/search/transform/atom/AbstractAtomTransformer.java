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
package net.di2e.ecdr.search.transform.atom;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import ddf.action.Action;
import ddf.action.ActionProvider;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.BinaryContentImpl;
import ddf.catalog.operation.ProcessingDetails;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.MetacardTransformer;
import ddf.catalog.transform.QueryResponseTransformer;
import net.di2e.ecdr.commons.CDRMetacard;
import net.di2e.ecdr.commons.constants.BrokerConstants;
import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.constants.SecurityConstants;
import net.di2e.ecdr.search.transform.atom.constants.AtomResponseConstants;
import net.di2e.ecdr.search.transform.atom.geo.GeoHelper;
import net.di2e.ecdr.search.transform.atom.security.SecurityConfiguration;
import net.di2e.ecdr.search.transform.atom.security.SecurityData;
import net.di2e.ecdr.search.transform.atom.security.SecurityMarkingHandler;
import net.di2e.ecdr.search.transform.atom.security.impl.ConfigurationSecurityMarkingHandler;
import net.di2e.ecdr.search.transform.atom.security.impl.MetacardSecurityMarkingHandler;
import net.di2e.ecdr.search.transform.atom.security.impl.XmlMetadataSecurityMarkingHandler;
import net.di2e.ecdr.search.transform.geo.formatter.CompositeGeometry;
import org.apache.abdera.Abdera;
import org.apache.abdera.ext.geo.Position;
import org.apache.abdera.ext.opensearch.OpenSearchConstants;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;
import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractAtomTransformer implements MetacardTransformer, QueryResponseTransformer {

    private static final String CDR_ATOM_TRANSFORMER_ID = "cdr-atom";

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractAtomTransformer.class );
    private static final DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat.dateTime();

    private static final String FORMAT_KEY = "format";

    private ActionProvider viewMetacardActionProvider = null;
    private ActionProvider resourceActionProvider = null;
    private ActionProvider thumbnailActionProvider = null;
    private ActionProvider metadataActionProvider = null;
    private ConfigurationWatcherImpl configWatcher = null;
    private List<SecurityConfiguration> securityConfigurations = null;
    private MimeType thumbnailMimeType = null;
    private MimeType viewMimeType = null;

    private boolean isTransform41 = false;
    private boolean isTransform50 = false;

    private boolean defaultToUseGMLEncoding = true;

    List<SecurityMarkingHandler> securityHandlers = null;

    public AbstractAtomTransformer( ConfigurationWatcherImpl config, ActionProvider viewMetacard, ActionProvider metadataProvider, ActionProvider resourceProvider, ActionProvider thumbnailProvider,
        MimeType thumbnailMime, MimeType viewMime, List<SecurityConfiguration> securityConfigs ) {
        if ( viewMime == null || thumbnailMime == null ) {
            throw new IllegalArgumentException( "MimeType parameters to constructor cannot be null" );
        }
        this.configWatcher = config;
        this.viewMetacardActionProvider = viewMetacard;
        this.metadataActionProvider = metadataProvider;
        this.resourceActionProvider = resourceProvider;
        this.thumbnailActionProvider = thumbnailProvider;
        this.thumbnailMimeType = thumbnailMime;
        this.viewMimeType = viewMime;
        this.securityConfigurations = securityConfigs;

        securityHandlers = new ArrayList<SecurityMarkingHandler>();
        securityHandlers.add( new MetacardSecurityMarkingHandler() );
        securityHandlers.add( new XmlMetadataSecurityMarkingHandler() );
        SecurityConfiguration metacardDefault = getConfigurationFromFormat( "metacard-default" );
        if ( metacardDefault != null ) {
            securityHandlers.add( new ConfigurationSecurityMarkingHandler( metacardDefault ) );
        }
    }

    public abstract void addFeedElements( Feed feed, SourceResponse response, Map<String, Serializable> properties );

    public abstract void addEntryElements( Entry entry, CDRMetacard metacard, Map<String, Serializable> properties );

    /**
     * Specifies if GML encoding should be used for location data.
     *
     * @param shouldUseGMLEncoding true (default) will return locations as GeoRSS-GML; false will return locations as GeoRSS-Simple
     */
    public void setUseGMLEncoding( boolean shouldUseGMLEncoding ) {
        defaultToUseGMLEncoding = shouldUseGMLEncoding;
    }

    @Override
    public BinaryContent transform( SourceResponse response, Map<String, Serializable> properties ) throws CatalogTransformerException {
        if ( properties == null ) {
            properties = new HashMap<String, Serializable>();
        }
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        Feed feed = null;
        // The Adbera.getInstance.newFeed() spins up a new thread so must do
        // this
        try {
            Thread.currentThread().setContextClassLoader( AbstractAtomTransformer.class.getClassLoader() );
            feed = Abdera.getInstance().newFeed();
        } finally {
            Thread.currentThread().setContextClassLoader( currentClassLoader );
        }

        feed.declareNS( AtomResponseConstants.CDRB_NAMESPACE, AtomResponseConstants.CDRB_NAMESPACE_PREFIX );
        feed.declareNS( AtomResponseConstants.CDRS_EXT_NAMESPACE, AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX );

        if ( properties.get( FORMAT_KEY ) != null ) {
            setFeedSecurity( feed, properties.get( FORMAT_KEY ).toString() );
        }

        feed.newId();
        setFeedTitle( feed, properties );
        feed.setUpdated( new Date() );

        List<Result> results = response.getResults();
        QueryRequest queryRequest = response.getRequest();

        feed.addExtension( OpenSearchConstants.ITEMS_PER_PAGE ).setText( String.valueOf( results.size() ) );
        feed.addExtension( OpenSearchConstants.START_INDEX ).setText( String.valueOf( queryRequest.getQuery().getStartIndex() ) );
        feed.addExtension( OpenSearchConstants.TOTAL_RESULTS ).setText( String.valueOf( response.getHits() ) );

        feed.setGenerator( null, configWatcher.getVersion(), configWatcher.getSiteName() );
        feed.addAuthor( configWatcher.getOrganization(), configWatcher.getContactEmailAddress(), null );

        addLinksToFeed( feed, properties );

        if ( !isFalse( (Boolean) properties.get( SearchConstants.STATUS_PARAMETER ) ) ) {
            addStatus( response, feed, results, properties );
        }

        addFeedElements( feed, response, properties );

        Entry entry = null;
        for ( Result result : results ) {
            entry = getMetacardEntry( new CDRMetacard( result.getMetacard() ), properties );
            Double relevance = result.getRelevanceScore();

            if ( relevance != null ) {
                Element relevanceElement = entry.addExtension( new QName( AtomResponseConstants.RELEVANCE_NAMESPACE, AtomResponseConstants.RELEVANCE_ELEMENT,
                    AtomResponseConstants.RELEVANCE_NAMESPACE_PREFIX ) );
                relevanceElement.setText( String.valueOf( relevance ) );
            }
            Double distance = result.getDistanceInMeters();
            if ( distance != null ) {
                Element distanceElement = entry.addExtension( new QName( AtomResponseConstants.CDRS_EXT_NAMESPACE, AtomResponseConstants.DISTANCE_ELEMENT,
                    AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX ) );
                distanceElement.setText( String.valueOf( distance ) );
            }
            feed.addEntry( entry );
        }

        BinaryContent binaryContent = null;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // Feed writeTo spins up a new thread so must do this
            currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader( AbstractAtomTransformer.class.getClassLoader() );
                feed.writeTo( outputStream );
            } finally {
                Thread.currentThread().setContextClassLoader( currentClassLoader );
            }

            binaryContent = new BinaryContentImpl( new ByteArrayInputStream( outputStream.toByteArray() ), new MimeType( AtomResponseConstants.ATOM_MIME_TYPE ) );
        } catch ( IOException e ) {
            LOGGER.error( e.getMessage(), e );
        } catch ( MimeTypeParseException e ) {
            LOGGER.error( e.getMessage(), e );
        }
        return binaryContent;
    }

    protected void addStatus( SourceResponse response, Feed feed, List<Result> results, Map<String, Serializable> properties ) {
        if ( response instanceof QueryResponse ) {
            QueryResponse queryResponse = (QueryResponse) response;
            Set<ProcessingDetails> details = queryResponse.getProcessingDetails();

            List<String> siteList = (List<String>) queryResponse.getPropertyValue( "site-list" );
            if ( siteList == null ) {
                siteList = new ArrayList<String>();
            }
            for ( ProcessingDetails detail : details ) {

                String sourceId = detail.getSourceId();
                siteList.remove( sourceId );

                ExtensibleElement sourceStatus = feed.addExtension( AtomResponseConstants.CDRB_NAMESPACE, "sourceStatus", AtomResponseConstants.CDRB_NAMESPACE_PREFIX );
                sourceStatus.setAttributeValue( new QName( AtomResponseConstants.CDRB_NAMESPACE, "sourceId", AtomResponseConstants.CDRB_NAMESPACE_PREFIX ), sourceId );
                sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "shortName", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, sourceId );

                String feedPath = (String) properties.get( BrokerConstants.PATH_PARAMETER );
                if ( detail.hasException() ) {
                    sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "status", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, "error" );
                    sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "resultsRetrieved", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, "0" );
                    if ( StringUtils.isNotBlank( feedPath ) ) {
                        sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "path", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, feedPath );
                    }
                    sourceStatus.addSimpleExtension( AtomResponseConstants.CDRS_EXT_NAMESPACE, "warning", AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX, detail.getException().getMessage() );
                    sourceStatus.addSimpleExtension( AtomResponseConstants.CDRS_EXT_NAMESPACE, "statusMessage", AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX, "Search complete with errors" );
                } else {
                    Serializable object = queryResponse.getPropertyValue( sourceId );

                    if ( object != null && object instanceof Map ) {
                        Map<String, Serializable> sourceProperties = (Map<String, Serializable>) object;
                        Long elapsedTime = (Long) sourceProperties.get( "elapsed-time" );
                        if ( elapsedTime != null ) {
                            sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "elapsedTime", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, String.valueOf( elapsedTime ) );
                        }

                        Long totalHits = (Long) sourceProperties.get( "total-hits" );
                        if ( totalHits != null ) {
                            sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "totalResults", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, String.valueOf( totalHits ) );
                        }

                        Integer totalResultsReturned = (Integer) sourceProperties.get( "total-results-returned" );
                        if ( totalResultsReturned != null ) {
                            sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "resultsRetrieved", AtomResponseConstants.CDRB_NAMESPACE_PREFIX,
                                String.valueOf( totalResultsReturned ) );
                        }
                    } else {
                        sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "resultsRetrieved", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, String.valueOf( results.size() ) );
                    }
                    sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "status", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, "complete" );

                    if ( StringUtils.isNotBlank( feedPath ) ) {
                        sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "path", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, feedPath );
                    }
                    sourceStatus.addSimpleExtension( AtomResponseConstants.CDRS_EXT_NAMESPACE, "statusMessage", AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX, "Search complete with no errors" );
                    List<String> warnings = detail.getWarnings();
                    if ( warnings != null && !warnings.isEmpty() ) {
                        sourceStatus.addSimpleExtension( AtomResponseConstants.CDRS_EXT_NAMESPACE, "warning", AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX, warnings.get( 0 ) );
                    }
                }
            }

            if ( details.isEmpty() && siteList.isEmpty() ) {
                String sourceId = (String) properties.get( SearchConstants.LOCAL_SOURCE_ID );
                if ( sourceId == null ) {
                    sourceId = "SELF";
                }

                ExtensibleElement sourceStatus = feed.addExtension( AtomResponseConstants.CDRB_NAMESPACE, "sourceStatus", AtomResponseConstants.CDRB_NAMESPACE_PREFIX );
                sourceStatus.setAttributeValue( new QName( AtomResponseConstants.CDRB_NAMESPACE, "sourceId", AtomResponseConstants.CDRB_NAMESPACE_PREFIX ), sourceId );
                sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "shortName", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, sourceId );
                sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "status", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, "complete" );
                sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "resultsRetrieved", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, String.valueOf( results.size() ) );
                sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "totalResults", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, String.valueOf( response.getHits() ) );
                sourceStatus.addSimpleExtension( AtomResponseConstants.CDRS_EXT_NAMESPACE, "statusMessage", AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX, "Search complete with no errors" );
            } else if ( !siteList.isEmpty() ) {
                for ( String site : siteList ) {
                    ExtensibleElement sourceStatus = feed.addExtension( AtomResponseConstants.CDRB_NAMESPACE, "sourceStatus", AtomResponseConstants.CDRB_NAMESPACE_PREFIX );
                    sourceStatus.setAttributeValue( new QName( AtomResponseConstants.CDRB_NAMESPACE, "sourceId", AtomResponseConstants.CDRB_NAMESPACE_PREFIX ), site );
                    sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "shortName", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, site );

                    String feedPath = (String) properties.get( BrokerConstants.PATH_PARAMETER );
                    Serializable object = queryResponse.getPropertyValue( site );
                    if ( object != null && object instanceof Map ) {
                        Map<String, Serializable> sourceProperties = (Map<String, Serializable>) object;
                        Long elapsedTime = (Long) sourceProperties.get( "elapsed-time" );
                        if ( elapsedTime != null ) {
                            sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "elapsedTime", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, String.valueOf( elapsedTime ) );
                        }

                        Long totalHits = (Long) sourceProperties.get( "total-hits" );
                        if ( totalHits != null ) {
                            sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "totalResults", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, String.valueOf( totalHits ) );
                        }

                        Integer totalResultsReturned = (Integer) sourceProperties.get( "total-results-returned" );
                        if ( totalResultsReturned != null ) {
                            sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "resultsRetrieved", AtomResponseConstants.CDRB_NAMESPACE_PREFIX,
                                String.valueOf( totalResultsReturned ) );
                        }
                        sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "status", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, "complete" );
                        sourceStatus
                            .addSimpleExtension( AtomResponseConstants.CDRS_EXT_NAMESPACE, "statusMessage", AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX, "Search complete with no errors" );
                    } else {
                        sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "resultsRetrieved", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, "0" );
                        sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "status", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, "waiting" );
                        sourceStatus.addSimpleExtension( AtomResponseConstants.CDRS_EXT_NAMESPACE, "statusMessage", AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX,
                            "Source is still being searched, and has not returned results yet" );
                    }

                    if ( StringUtils.isNotBlank( feedPath ) ) {
                        sourceStatus.addSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, "path", AtomResponseConstants.CDRB_NAMESPACE_PREFIX, feedPath );
                    }

                }
            }

        }
    }

    protected boolean isFalse( Boolean includeStatus ) {
        return Boolean.FALSE.equals( includeStatus );
    }

    @Override
    public BinaryContent transform( Metacard metacard, Map<String, Serializable> properties ) throws CatalogTransformerException {
        if ( properties == null ) {
            properties = new HashMap<String, Serializable>();
        }
        Entry entry = getMetacardEntry( new CDRMetacard( metacard ), properties );

        BinaryContent binaryContent = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            entry.writeTo( outputStream );
            binaryContent = new BinaryContentImpl( new ByteArrayInputStream( outputStream.toByteArray() ), new MimeType( AtomResponseConstants.ATOM_MIME_TYPE ) );
        } catch ( IOException e ) {
            LOGGER.error( e.getMessage(), e );
        } catch ( MimeTypeParseException e ) {
            LOGGER.error( e.getMessage(), e );
        }
        return binaryContent;
    }

    protected ActionProvider getViewMetacardActionProvider() {
        return viewMetacardActionProvider;
    }

    protected ActionProvider getThumbnailActionProvider() {
        return thumbnailActionProvider;
    }

    protected ActionProvider getResourceActionProvider() {
        return resourceActionProvider;
    }

    protected ConfigurationWatcherImpl getConfigurationWatcherImpl() {
        return configWatcher;
    }

    protected MimeType getThumbnailMimeType() {
        return thumbnailMimeType;
    }

    protected MimeType getViewMimeType() {
        return viewMimeType;
    }

    protected boolean isTransform41() {
        return isTransform41;
    }

    protected boolean isTransform50() {
        return isTransform50;
    }

    protected Entry getMetacardEntry( CDRMetacard metacard, Map<String, Serializable> properties ) {

        String format = (String) properties.get( SearchConstants.FORMAT_PARAMETER );

        Entry entry = Abdera.getInstance().newEntry();

        entry.declareNS( AtomResponseConstants.GEORSS_NAMESPACE, AtomResponseConstants.GEORSS_NAMESPACE_PREFIX );
        entry.declareNS( AtomResponseConstants.RELEVANCE_NAMESPACE, AtomResponseConstants.RELEVANCE_NAMESPACE_PREFIX );

        setEntrySecurity( entry, metacard );

        entry.setId( metacard.getAtomId() );
        entry.setTitle( metacard.getTitle() );
        entry.setUpdated( metacard.getModifiedDate() );
        Date effective = metacard.getEffectiveDate();
        if ( effective != null ) {
            entry.setPublished( effective );
        }

        entry.addCategory( metacard.getContentTypeVersion(), metacard.getContentTypeName(), "Content Type" );

        String sourceId = metacard.getSourceId();
        if ( sourceId != null ) {
            ExtensibleElement element = entry.addExtension( AtomResponseConstants.CDRB_NAMESPACE, AtomResponseConstants.RESULT_SOURCE_ELEMENT, AtomResponseConstants.CDRB_NAMESPACE_PREFIX );
            element.setAttributeValue( new QName( AtomResponseConstants.CDRB_NAMESPACE, "sourceId", AtomResponseConstants.CDRB_NAMESPACE_PREFIX ), sourceId );
            element.setText( sourceId );
        }

        addLinksToEntry( entry, metacard, format );

        if ( metacard.hasLocation() ) {
            addLocation( entry, metacard, useGmlEncoding( properties ) );
        }

        Date createdDate = metacard.getCreatedDate();
        if ( createdDate != null ) {
            entry.addSimpleExtension( AtomResponseConstants.METACARD_ATOM_NAMESPACE, AtomResponseConstants.METACARD_CREATED_DATE_ELEMENT, AtomResponseConstants.METACARD_ATOM_NAMESPACE_PREFIX,
                DATE_FORMATTER.print( createdDate.getTime() ) );
        }

        Date expirationDate = metacard.getExpirationDate();
        if ( expirationDate != null ) {
            entry.addSimpleExtension( AtomResponseConstants.METACARD_ATOM_NAMESPACE, AtomResponseConstants.METADATA_EXPIRATION_DATE_ELEMENT, AtomResponseConstants.METACARD_ATOM_NAMESPACE_PREFIX,
                DATE_FORMATTER.print( expirationDate.getTime() ) );
        }

        addEntryElements( entry, metacard, properties );

        return entry;
    }

    /**
     * This method inspects the properties to determine if there is a property that specifies the GeoRSS format. If that
     * property exists then it will use the value of that property to determine whether to use simple or gml format.
     * Otherwise it will return the default global property for using GML or Simple
     *
     * @param properties that were passed into the transformer
     * @return true if GML encoding for GeoRSS should be used, false would mean to use simple encoding
     */
    protected boolean useGmlEncoding( Map<String, Serializable> properties ) {
        String format = (String) properties.get( SearchConstants.GEORSS_RESULT_FORMAT_PARAMETER );
        boolean useGml = defaultToUseGMLEncoding;
        if ( StringUtils.isNotBlank( format ) ) {
            if ( SearchConstants.GEORSS_SIMPLE_FORMAT.equalsIgnoreCase( format ) ) {
                useGml = false;
            } else if ( SearchConstants.GEORSS_GML_FORMAT.equalsIgnoreCase( format ) ) {
                useGml = true;
            }
        }
        return useGml;
    }

    protected void addLinksToEntry( Entry entry, CDRMetacard metacard, String format ) {
        if ( metacard.hasThumbnail() ) {
            URI thumbnailURI = metacard.getThumbnailURL();

            if ( thumbnailActionProvider != null ) {
                Action action = thumbnailActionProvider.getAction( metacard );
                if ( action != null && action.getUrl() != null ) {
                    entry.addLink( action.getUrl().toString(), CDRMetacard.LINK_REL_PREVIEW, thumbnailMimeType.getBaseType(), action.getTitle(), null, metacard.getThumbnailLength() );
                }
            }
        }

        if ( resourceActionProvider != null && metacard.hasResource() ) {
            Action action = resourceActionProvider.getAction( metacard );
            if ( action != null && action.getUrl() != null ) {
                entry.addLink( action.getUrl().toString(), Link.REL_ALTERNATE, metacard.getResourceMIMETypeString(), action.getTitle(), null, metacard.getResourceSizeLong() );
            }
            // If there is no explicit resource then the metadata serves as
            // the product/resource so include a link to it here
        } else if ( metadataActionProvider != null ) {
            Action action = metadataActionProvider.getAction( metacard );
            if ( action != null && action.getUrl() != null ) {
                entry.addLink( action.getUrl().toString(), Link.REL_ALTERNATE, "text/xml", "View Product", null, -1 );
            }
        }
        if ( viewMetacardActionProvider != null ) {
            Action action = viewMetacardActionProvider.getAction( metacard );
            if ( action != null && action.getUrl() != null ) {
                entry.addLink( action.getUrl().toString() + "?transform=" + (format == null ? CDR_ATOM_TRANSFORMER_ID : format), Link.REL_SELF, AtomResponseConstants.ATOM_MIME_TYPE,
                    "View Atom Entry", null, -1 );
                entry.addLink( action.getUrl().toString(), Link.REL_RELATED, "text/xml", action.getTitle(), null, -1 );
            }
        }
    }

    protected void addLocation( Entry entry, Metacard metacard, boolean useGmlEncoding ) {
        WKTReader reader = new WKTReader();
        try {
            Geometry geo = reader.read( metacard.getLocation() );

            List<Position> positions = CompositeGeometry.getCompositeGeometry( geo ).toGeoRssPositions();
            for ( Position position : positions ) {
                if ( useGmlEncoding ) {
                    GeoHelper.addPosition( entry, position, GeoHelper.Encoding.GML );
                } else {
                    GeoHelper.addPosition( entry, position, GeoHelper.Encoding.SIMPLE );
                }
            }
        } catch ( ParseException e ) {
            LOGGER.error( e.getMessage(), e );
        }
    }

    protected void addLinksToFeed( Feed feed, Map<String, Serializable> properties ) {
        Serializable property = properties.get( SearchConstants.SELF_LINK_REL );
        if ( property != null && property instanceof String ) {
            feed.addLink( String.valueOf( property ), Link.REL_SELF, AtomResponseConstants.ATOM_MIME_TYPE, "Current Page", null, -1 );
        }

        property = properties.get( SearchConstants.FIRST_LINK_REL );
        if ( property != null && property instanceof String ) {
            feed.addLink( String.valueOf( property ), Link.REL_FIRST, AtomResponseConstants.ATOM_MIME_TYPE, "First Page", null, -1 );
        }

        property = properties.get( SearchConstants.LAST_LINK_REL );
        if ( property != null && property instanceof String ) {
            feed.addLink( String.valueOf( property ), Link.REL_LAST, AtomResponseConstants.ATOM_MIME_TYPE, "Last Page", null, -1 );
        }

        property = properties.get( SearchConstants.NEXT_LINK_REL );
        if ( property != null && property instanceof String ) {
            feed.addLink( String.valueOf( property ), Link.REL_NEXT, AtomResponseConstants.ATOM_MIME_TYPE, "Next Page", null, -1 );
        }

        property = properties.get( SearchConstants.PREV_LINK_REL );
        if ( property != null && property instanceof String ) {
            feed.addLink( String.valueOf( property ), Link.REL_PREVIOUS, AtomResponseConstants.ATOM_MIME_TYPE, "Previous Page", null, -1 );
        }

    }

    protected void setFeedTitle( Feed feed, Map<String, Serializable> properties ) {
        Serializable property = properties.get( SearchConstants.FEED_TITLE );
        if ( property != null && property instanceof String ) {
            feed.setTitle( String.valueOf( property ) );
        } else {
            feed.setTitle( "Atom Search Results Feed from source:" + configWatcher.getSiteName() );
        }

    }

    protected void setFeedSecurity( Feed feed, String format ) {
        SecurityConfiguration securityConfiguration = getConfigurationFromFormat( format );
        if ( securityConfiguration == null ) {
            LOGGER.debug( "No valid security configuration found for format {}, using default configurations.", format );
            securityConfiguration = getConfigurationFromFormat( SecurityConfiguration.DEFAULT_FORMAT_CONFIGURATION );
        }
        if ( securityConfiguration != null ) {
            String namespace = securityConfiguration.getNamespace();
            if ( StringUtils.isNotBlank( namespace ) ) {
                Map<String, String> securityAttributes = securityConfiguration.getAttributes();
                if ( securityAttributes != null && !securityAttributes.isEmpty() ) {
                    feed.declareNS( namespace, SecurityConstants.ISM_NAMESPACE_PREFIX );
                    for ( java.util.Map.Entry<String, String> securityEntry : securityAttributes.entrySet() ) {
                        String securityValue = securityEntry.getValue();
                        if ( StringUtils.isNotBlank( securityValue ) ) {
                            feed.setAttributeValue( new QName( namespace, securityEntry.getKey() ), securityEntry.getValue() );
                        }
                    }
                }
            }
        }
    }

    protected void setEntrySecurity( Entry entry, Metacard metacard ) {
        for ( SecurityMarkingHandler securityHandler : securityHandlers ) {
            SecurityData securityData = securityHandler.getSecurityData( metacard );
            if ( securityData != null ) {
                String securityNamespace = securityData.getSecurityNamespace();
                if ( StringUtils.isNotBlank( securityNamespace ) ) {
                    boolean hasAttribute = false;
                    for ( java.util.Map.Entry<String, List<String>> securityEntry : securityData.getSecurityMarkings().entrySet() ) {
                        List<String> securityValues = securityEntry.getValue();
                        if ( securityValues != null && !securityValues.isEmpty() ) {
                            if ( !hasAttribute ) {
                                entry.declareNS( securityNamespace, SecurityConstants.ISM_NAMESPACE_PREFIX );
                            }
                            entry.setAttributeValue( new QName( securityNamespace, securityEntry.getKey() ), StringUtils.join( securityValues, " " ) );
                            hasAttribute = true;
                        }
                    }
                    if ( hasAttribute ) {
                        break;
                    }
                }
            }
        }
    }

    private SecurityConfiguration getConfigurationFromFormat( String format ) {
        SecurityConfiguration securityConfiguration = null;
        for ( SecurityConfiguration curConfig : securityConfigurations ) {
            if ( curConfig.getFormats().contains( format ) ) {
                securityConfiguration = curConfig;
                LOGGER.debug( "Setting feed security to use {} format.", format );
                break;
            }
        }
        return securityConfiguration;
    }

}
