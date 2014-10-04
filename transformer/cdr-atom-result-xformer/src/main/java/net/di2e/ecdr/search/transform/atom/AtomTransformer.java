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

import java.io.Serializable;
import java.util.Map;

import javax.activation.MimeType;

import net.di2e.ecdr.commons.CDRMetacard;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;

import ddf.action.ActionProvider;
import ddf.catalog.operation.SourceResponse;

public class AtomTransformer extends AbstractAtomTransformer {

    public AtomTransformer( ConfigurationWatcherImpl configWatcher, ActionProvider viewMetacardProvider, ActionProvider resourceProvider, ActionProvider thumbnailProvider, MimeType thumbnailMime,
            MimeType viewMime ) {
        super( configWatcher, viewMetacardProvider, resourceProvider, thumbnailProvider, thumbnailMime, viewMime );
    }

    @Override
    public void addFeedElements( Feed feed, SourceResponse response, Map<String, Serializable> properties ) {
    }

    @Override
    public void addEntryElements( Entry entry, CDRMetacard metacard, Map<String, Serializable> properties ) {
    }

}
