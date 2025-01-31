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
package net.di2e.ecdr.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Date;

import javax.activation.MimeType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;

public class CDRMetacard extends MetacardImpl {

    public static final String METACARD_CONTENT_COLLECTION_ATTRIBUTE = "content-collections";

    public static final String LINK_REL_PREVIEW = "preview";

    public static final String RESOURCE_MIME_TYPE = "resource-mime-type";
    public static final String RESOURCE_TITLE = "resource-title";

    public static final String THUMBNAIL_LINK = "thumbnail-link";
    public static final String THUMBNAIL_LENGTH = "thumbnail-length";
    public static final String THUMBNAIL_MIMETYPE = "thumbnail-mimetype";
    public static final String THUMBNAIL_LINK_TITLE = "thumbnail-link-title";
    
    public static final String METADATA_LINK = "metadata-link";

    private static final String THUMBNAIL_N_A = "N/A";

    private static final XLogger LOGGER = new XLogger( LoggerFactory.getLogger( CDRMetacard.class ) );

    private static final long serialVersionUID = 1L;
    private Metacard originalMetacard;

    public CDRMetacard( Metacard metacard ) {
        if ( metacard == null ) {
            throw new IllegalArgumentException( "Metacard cannot be null" );
        }
        originalMetacard = metacard;
    }

    public boolean hasLocation() {
        return originalMetacard.getLocation() != null;
    }

    public boolean hasResource() {
        return originalMetacard.getResourceURI() != null;
    }

    public String getResourceMIMETypeString() {
        Attribute attribute = originalMetacard.getAttribute( RESOURCE_MIME_TYPE );
        if ( attribute != null ) {
            Serializable mimeType = attribute.getValue();
            if ( mimeType != null && mimeType instanceof String ) {
                return (String) mimeType;
            }
        }
        return "application/unknown";

    }

    public long getResourceSizeLong() {
        String size = originalMetacard.getResourceSize();
        if ( StringUtils.isNotBlank( size ) ) {
            try {
                return Long.parseLong( originalMetacard.getResourceSize() );
            } catch ( NumberFormatException e ) {
                LOGGER.debug( "Could not parse resource size into integer from Metacard: " + size );
            }
        }
        return -1;
    }

    public boolean hasThumbnail() {
        // Order is import here especially if the original Metacard is using
        // Thumbnail links and doesn't pull the thumbnail until the getThumbnail
        // method is called
        return originalMetacard.getAttribute( THUMBNAIL_LINK ) != null || originalMetacard.getThumbnail() != null;
    }

    public long getThumbnailLength() {
        long thumbnailSize = -1;
        Attribute attribute = originalMetacard.getAttribute( THUMBNAIL_LENGTH );
        if ( attribute != null ) {
            thumbnailSize = (Long) attribute.getValue();
        }
        if ( thumbnailSize < 0 ) {
            byte[] thumbnail = originalMetacard.getThumbnail();
            thumbnailSize = thumbnail == null ? -1 : thumbnail.length;
        }
        return thumbnailSize;
    }

    public String getAtomId() {
        return "urn:catalog:id:" + originalMetacard.getId();
    }

    public URI getMetadataURL() {
        Attribute attribute = originalMetacard.getAttribute( METADATA_LINK );
        if ( attribute != null ) {
            return (URI) attribute.getValue();
        }
        return null;
    }

    public URI getThumbnailURL() {
        Attribute attribute = originalMetacard.getAttribute( THUMBNAIL_LINK );
        if ( attribute != null ) {
            return (URI) attribute.getValue();
        }
        return null;
    }

    public MimeType getThumbnailMIMEType() {
        Attribute attribute = originalMetacard.getAttribute( THUMBNAIL_MIMETYPE );
        if ( attribute != null ) {
            return (MimeType) attribute.getValue();
        }
        return null;
    }

    public String getThumbnailLinkTitle() {
        Attribute attribute = originalMetacard.getAttribute( THUMBNAIL_LINK_TITLE );
        if ( attribute != null ) {
            return (String) attribute.getValue();
        }
        return null;
    }

    @Override
    public Attribute getAttribute( String attributeName ) {
        if ( Metacard.THUMBNAIL.equals( attributeName ) ) {
            return new AttributeImpl( attributeName, this.getThumbnail() );
        } else if ( Metacard.METADATA.equals( attributeName ) ) {
            return new AttributeImpl( attributeName, this.getMetadata() );
        } else {
            return originalMetacard.getAttribute( attributeName );
        }
    }

    @Override
    public String getContentTypeName() {
        String type = originalMetacard.getContentTypeName();
        return StringUtils.isNotBlank( type ) ? type : "Unknown";
    }

    @Override
    public URI getContentTypeNamespace() {
        return originalMetacard.getContentTypeNamespace();
    }

    @Override
    public String getContentTypeVersion() {
        String version = originalMetacard.getContentTypeVersion();
        return StringUtils.isNotBlank( version ) ? version : "Unknown";
    }

    @Override
    public Date getCreatedDate() {
        return originalMetacard.getCreatedDate();
    }

    @Override
    public Date getEffectiveDate() {
        return originalMetacard.getEffectiveDate();
    }

    @Override
    public Date getExpirationDate() {
        return originalMetacard.getExpirationDate();
    }

    @Override
    public String getId() {
        return originalMetacard.getId();
    }

    @Override
    public String getLocation() {
        return originalMetacard.getLocation();
    }

    @Override
    public MetacardType getMetacardType() {
        return originalMetacard.getMetacardType();
    }

    @Override
    public String getMetadata() {
        String metadata = originalMetacard.getMetadata();
        if ( StringUtils.isBlank( metadata ) ) {
            URI metadataURI = getMetadataURL();
            if ( metadataURI != null ) {
                try ( InputStream in = metadataURI.toURL().openStream() ) {
                    metadata = IOUtils.toString( in );
                    originalMetacard.setAttribute( new AttributeImpl( Metacard.METADATA, metadata ) );
                } catch ( MalformedURLException e ) {
                    LOGGER.warn( "Cannot read metadata due to Invalid metadata URL[" + metadataURI + "]: " + e.getMessage(), e );
                } catch ( IOException e ) {
                    LOGGER.warn( "Could not read metadata from remote URL[" + metadataURI + "] due to: " + e.getMessage(), e );
                }
            }
        }
        return metadata;
    }

    @Override
    public Date getModifiedDate() {
        Date modified = originalMetacard.getModifiedDate();
        return modified == null ? new Date() : modified;
    }

    @Override
    public String getResourceSize() {
        String size = originalMetacard.getResourceSize();
        return StringUtils.isNotBlank( size ) && !THUMBNAIL_N_A.equalsIgnoreCase( size ) ? size : null;
    }

    @Override
    public URI getResourceURI() {
        return originalMetacard.getResourceURI();
    }

    @Override
    public String getSourceId() {
        return originalMetacard.getSourceId();
    }

    @Override
    public byte[] getThumbnail() {
        byte[] thumbnail = originalMetacard.getThumbnail();
        if ( thumbnail == null ) {
            URI thumbnailURI = getThumbnailURL();
            if ( thumbnailURI != null ) {
                 try ( InputStream in = thumbnailURI.toURL().openStream() ) {
                    thumbnail = IOUtils.toByteArray( in );
                    originalMetacard.setAttribute( new AttributeImpl( Metacard.THUMBNAIL, thumbnail ) );
                } catch ( MalformedURLException e ) {
                    LOGGER.warn( "Cannot read thumbnail due to invalid thumbnail URL[" + thumbnailURI + "]: " + e.getMessage(), e );
                } catch ( IOException e ) {
                    LOGGER.warn( "Could not read thumbnail from remote URL[" + thumbnailURI + "] due to: " + e.getMessage(), e );
                }
            }
        }
        return thumbnail;
    }

    @Override
    public String getTitle() {
        String title = originalMetacard.getTitle();
        return title == null ? "No Title" : title;
    }

    @Override
    public void setAttribute( Attribute attribute ) {
        originalMetacard.setAttribute( attribute );
    }

    @Override
    public void setSourceId( String sourceId ) {
        originalMetacard.setSourceId( sourceId );
    }

}
