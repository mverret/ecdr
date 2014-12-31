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
package net.di2e.ecdr.search.transform.atom;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimeType;

import net.di2e.ecdr.commons.CDRMetacard;
import net.di2e.ecdr.commons.constants.SearchConstants;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.action.ActionProvider;
import ddf.catalog.Constants;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.Metacard;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.MetacardTransformer;

public class AtomTransformerWithPayload extends AbstractAtomTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger( AtomTransformerWithPayload.class );

    private Map<String, MetacardTransformer> metacardTransformerMap = null;

    public AtomTransformerWithPayload( ConfigurationWatcherImpl configWatcher, ActionProvider viewMetacardProvider, ActionProvider metadataProvider, ActionProvider resourceProvider,
            ActionProvider thumbnailProvider, MimeType thumbnailMime, MimeType viewMime ) {
        super( configWatcher, viewMetacardProvider, metadataProvider, resourceProvider, thumbnailProvider, thumbnailMime, viewMime );
        metacardTransformerMap = new HashMap<String, MetacardTransformer>();
    }

    @Override
    public void addFeedElements( Feed feed, SourceResponse response, Map<String, Serializable> properties ) {
    }

    @Override
    public void addEntryElements( Entry entry, CDRMetacard metacard, Map<String, Serializable> properties ) {
        // Abdera.getParser() spins up a new thread so must do this
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( AtomTransformerWithPayload.class.getClassLoader() );
            entry.addExtension( Abdera.getNewParser().parse( new StringReader( getMetadataXML( metacard, (String) properties.get( SearchConstants.METACARD_TRANSFORMER_NAME ) ) ) ).getRoot() );
        } finally {
            Thread.currentThread().setContextClassLoader( currentClassLoader );
        }
    }

    /**
     * Method responsible for getting the metadata XML String that is associated with the Metacard. The metadata is
     * retrieved by calling the MetadataTransformer that is registered with the id that matches the format String that
     * is passed into the method. If a MetacardTransformer doesn't exist, or the result of the Transform is not XML, or
     * if there is an error while transforming, the Metacard.METADATA will be returned
     * 
     * @param metacard
     *            the Metacard to get the Metacard from
     * @param format
     *            the format of the MetacardTransformer to use (which is looked up by MetacardTransformer id)
     * @return the XML String
     */
    protected String getMetadataXML( Metacard metacard, String format ) {
        String metadata = null;
        LOGGER.debug( "Getting metadata to include in atom response in the format [{}]", format );
        if ( StringUtils.isNotBlank( format ) ) {
            MetacardTransformer metacardTransformer = metacardTransformerMap.get( format );
            if ( metacardTransformer != null ) {
                try {
                    LOGGER.debug( "Calling the MetacardTransformer with id [{}] to transform the Metacard into XML Metadata", format );
                    BinaryContent binaryContent = metacardTransformer.transform( metacard, null );
                    String mimeValue = binaryContent.getMimeTypeValue();
                    if ( StringUtils.isNotBlank( mimeValue ) && mimeValue.contains( "xml" ) ) {
                        try ( InputStream inputStream = binaryContent.getInputStream() ) {
                            metadata = IOUtils.toString( inputStream );
                        } catch ( IOException e ) {
                            LOGGER.warn( "Error while writing transformed Metacard into a String: " + e.getMessage(), e );
                        }
                    }
                } catch ( CatalogTransformerException e ) {
                    LOGGER.warn( "Error while transforming metacard using the [{}] MetacardTransformer", format );
                    LOGGER.warn( e.getMessage(), e );
                }
            }
        }
        if ( metadata == null ) {
            LOGGER.debug( "A MetacardTransform didn't exist for format [{}] or ran into problems when transforming Metacard, so falling back to using the Metadata in the Metacard", format );
            metadata = metacard.getMetadata();
        }
        return metadata;
    }

    /**
     * Method called by the OSGi container, managed by blueprint whenever a new MetacardTransformer service is exposed
     * to the OSGi Registry
     * 
     * @param transformer
     *            the MetacardTransformer that was added
     * @param map
     *            the service properties for the corresponding MetacardTransformer
     */
    public void metacardTransformerAdded( MetacardTransformer transformer, Map<String, Object> map ) {
        String id = (String) map.get( Constants.SERVICE_ID );
        metacardTransformerMap.put( id, transformer );
        LOGGER.debug( "Adding MetacardTransformer with id [{}] to transformer map.", id );
    }

    /**
     * Method is called when a MetacardTransformer is removed from the OSGi registry (called by OSGi container, managed
     * by blueprint)
     * 
     * @param transformer
     *            the MetacardTransformer service that was removed
     * @param map
     *            the service properties for the corresponding MetacardTransformer
     */
    public void metacardTransformerRemoved( MetacardTransformer transformer, Map<String, Object> map ) {
        String id = (String) map.get( Constants.SERVICE_ID );
        metacardTransformerMap.remove( id );
        LOGGER.debug( "Removing MetacardTransformer with id [{}] from transformer map.", id );
    }
}
