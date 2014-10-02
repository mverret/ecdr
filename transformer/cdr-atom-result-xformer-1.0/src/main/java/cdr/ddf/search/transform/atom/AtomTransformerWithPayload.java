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
package cdr.ddf.search.transform.atom;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Map;

import javax.activation.MimeType;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;

import cdr.ddf.commons.CDRMetacard;
import ddf.action.ActionProvider;
import ddf.catalog.operation.SourceResponse;

public class AtomTransformerWithPayload extends AbstractAtomTransformer {

    public AtomTransformerWithPayload( ConfigurationWatcherImpl configWatcher, ActionProvider viewMetacardProvider, ActionProvider resourceProvider, ActionProvider thumbnailProvider,
            MimeType thumbnailMime, MimeType viewMime ) {
        super( configWatcher, viewMetacardProvider, resourceProvider, thumbnailProvider, thumbnailMime, viewMime );
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
            entry.addExtension( Abdera.getNewParser().parse( new StringReader( metacard.getMetadata() ) ).getRoot() );
        } finally {
            Thread.currentThread().setContextClassLoader( currentClassLoader );
        }
    }

}
