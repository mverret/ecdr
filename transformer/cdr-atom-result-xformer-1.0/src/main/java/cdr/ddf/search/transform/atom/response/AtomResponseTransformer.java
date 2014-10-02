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
package cdr.ddf.search.transform.atom.response;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.MimeType;
import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.ext.geo.Coordinate;
import org.apache.abdera.ext.geo.GeoHelper;
import org.apache.abdera.ext.geo.Point;
import org.apache.abdera.ext.geo.Position;
import org.apache.abdera.ext.opensearch.OpenSearchConstants;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.Parser;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cdr.ddf.commons.CDRMetacard;
import cdr.ddf.commons.response.SearchResponseTransformer;
import cdr.ddf.search.transform.atom.constants.AtomResponseConstants;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.impl.SourceResponseImpl;

public class AtomResponseTransformer implements SearchResponseTransformer {

    private static final transient Logger LOGGER = LoggerFactory.getLogger( AtomResponseTransformer.class );
    private static final String METADATA_ELEMENT_NAME = "Resource";
    private static final DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat.dateTimeParser();

    private static final Abdera ABDERA = Abdera.getInstance();

    @Override
    public SourceResponse processSearchResponse( InputStream inputStream, String format, QueryRequest request, String siteName ) {
        List<Result> resultList = new ArrayList<Result>();

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Parser parser = null;
        Document<Feed> atomDoc;
        try {

            Thread.currentThread().setContextClassLoader( AtomResponseTransformer.class.getClassLoader() );
            parser = ABDERA.getParser();
            atomDoc = parser.parse( new InputStreamReader( inputStream ) );

        } finally {
            Thread.currentThread().setContextClassLoader( tccl );
        }

        Feed feed = atomDoc.getRoot();

        List<Entry> entries = feed.getEntries();
        for ( Entry entry : entries ) {
            resultList.add( createResponseFromEntry( entry, siteName ) );
        }

        long totalResults = entries.size();
        Element totalResultsElement = atomDoc.getRoot().getExtension( OpenSearchConstants.TOTAL_RESULTS );

        if ( totalResultsElement != null ) {
            try {
                totalResults = Long.parseLong( totalResultsElement.getText() );
            } catch ( NumberFormatException e ) {
                LOGGER.warn( "Received invalid number of results from Atom response [" + totalResultsElement.getText() + "]", e );
            }
        }

        return new SourceResponseImpl( request, resultList, totalResults );
    }

    private Result createResponseFromEntry( Entry entry, String siteName ) {

        MetacardImpl metacard = new MetacardImpl();

        String id = entry.getIdElement().getText();
        // id may be formatted catalog:id:<id>, so we parse out the <id>
        if ( id != null && id.contains( ":" ) ) {
            id = id.substring( id.lastIndexOf( ':' ) + 1 );
        }
        metacard.setId( id );

        // Set the source to the original source name
        String resultSource = entry.getSimpleExtension( AtomResponseConstants.CDRB_NAMESPACE, AtomResponseConstants.RESULT_SOURCE_ELEMENT, AtomResponseConstants.CDRB_NAMESPACE_PREFIX );
        metacard.setSourceId( resultSource == null ? siteName : resultSource );

        List<Category> categories = entry.getCategories();
        if ( categories != null && !categories.isEmpty() ) {
            Category category = categories.get( 0 );
            metacard.setContentTypeName( category.getTerm() );
            metacard.setContentTypeVersion( category.getScheme().toString() );
        }

        metacard.setModifiedDate( entry.getUpdated() );
        metacard.setEffectiveDate( entry.getPublished() );

        String createdDate = entry.getSimpleExtension( new QName( AtomResponseConstants.METACARD_ATOM_NAMESPACE, AtomResponseConstants.METACARD_CREATED_DATE_ELEMENT ) );
        if ( createdDate != null ) {
            metacard.setCreatedDate( new Date( DATE_FORMATTER.parseMillis( createdDate ) ) );
        }

        String expirationDate = entry.getSimpleExtension( new QName( AtomResponseConstants.METACARD_ATOM_NAMESPACE, AtomResponseConstants.METADATA_EXPIRATION_DATE_ELEMENT ) );
        if ( expirationDate != null ) {
            metacard.setExpirationDate( new Date( DATE_FORMATTER.parseMillis( expirationDate ) ) );
        }

        String metadata = entry.getContent();
        metacard.setMetadata( metadata );

        metacard.setLocation( getWKT( entry ) );

        // if ( position != null ){
        // metacard.setLocation( toWKT( position ) );
        // }
        Link productLink = entry.getAlternateLink();
        if ( productLink != null ) {
            metacard.setResourceURI( URI.create( productLink.getHref().toASCIIString() ) );
            long resourceSize = productLink.getLength();
            if ( resourceSize > 0 ) {
                metacard.setResourceSize( String.valueOf( resourceSize ) );
            }
            String productTitle = productLink.getTitle();
            if ( productTitle != null ) {
                metacard.setAttribute( CDRMetacard.RESOURCE_TITLE, productTitle );
            }
            MimeType productType = productLink.getMimeType();
            if ( productType != null ) {
                metacard.setAttribute( CDRMetacard.RESOURCE_MIME_TYPE, productType.toString() );
            }
        }

        List<Link> links = entry.getLinks( CDRMetacard.LINK_REL_PREVIEW );
        if ( links != null && !links.isEmpty() ) {
            for ( Link link : links ) {
                MimeType mimeType = link.getMimeType();
                if ( mimeType != null && "image".equals( mimeType.getPrimaryType() ) ) {

                    metacard.setAttribute( CDRMetacard.THUMBNAIL_LINK, URI.create( link.getHref().toASCIIString() ) );
                    long thumbnailSize = link.getLength();
                    if ( thumbnailSize > 0 ) {
                        metacard.setAttribute( CDRMetacard.THUMBNAIL_LENGTH, Long.valueOf( thumbnailSize ) );
                    }
                    metacard.setAttribute( CDRMetacard.THUMBNAIL_MIMETYPE, link.getMimeType() );
                    metacard.setAttribute( CDRMetacard.THUMBNAIL_LINK_TITLE, link.getTitle() );
                }
            }
        }

        metacard.setTitle( entry.getTitle() );

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {

            Thread.currentThread().setContextClassLoader( AtomResponseTransformer.class.getClassLoader() );
            List<Element> extensions = entry.getExtensions();
            for ( Element element : extensions ) {
                if ( METADATA_ELEMENT_NAME.equalsIgnoreCase( element.getQName().getLocalPart() ) ) {
                    StringWriter writer = new StringWriter();
                    try {
                        element.writeTo( writer );
                        metacard.setMetadata( writer.toString() );
                    } catch ( IOException e ) {
                        LOGGER.error( "Could not convert Metadata String value from Atom to Metacard.METADATA attribute", e );
                    }

                }
            }

        } finally {
            Thread.currentThread().setContextClassLoader( tccl );
        }

        // populate the relevance
        ResultImpl result = new ResultImpl( metacard );
        String relevance = entry.getSimpleExtension( AtomResponseConstants.RELEVANCE_NAMESPACE, AtomResponseConstants.RELEVANCE_ELEMENT, AtomResponseConstants.RELEVANCE_NAMESPACE_PREFIX );
        if ( relevance != null ) {
            try {
                result.setRelevanceScore( Double.parseDouble( relevance ) );
            } catch ( NumberFormatException e ) {
                LOGGER.warn( "Received invalid number for relevance from Atom response [" + relevance + "]", e );
            }
        }

        String distance = entry.getSimpleExtension( AtomResponseConstants.CDRS_EXT_NAMESPACE, AtomResponseConstants.DISTANCE_ELEMENT, AtomResponseConstants.CDRS_EXT_NAMESPACE_PREFIX );
        if ( distance != null ) {
            try {
                result.setDistanceInMeters( Double.parseDouble( relevance ) );
            } catch ( NumberFormatException e ) {
                LOGGER.warn( "Received invalid number for distance from Atom response [" + distance + "]", e );
            }
        }

        return result;
    }

    private String getWKT( Entry entry ) {
        String wkt = null;
        ExtensibleElement geoElement = entry.getExtension( GeoHelper.QNAME_SIMPLE_POINT );
        if ( geoElement != null ) {
            Position position = GeoHelper.getAsPosition( geoElement );
            if ( position instanceof Point ) {
                Coordinate coord = ((Point) position).getCoordinate();
                wkt = "POINT(" + coord.getLongitude() + " " + coord.getLatitude() + ")";
            }
        } else {
            geoElement = entry.getExtension( GeoHelper.QNAME_WHERE );

        }
        return wkt;
    }

}
