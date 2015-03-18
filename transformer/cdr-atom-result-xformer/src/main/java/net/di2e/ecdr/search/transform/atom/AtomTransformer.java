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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.activation.MimeType;

import net.di2e.ecdr.commons.CDRMetacard;
import net.di2e.ecdr.search.transform.atom.security.SecurityConfiguration;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.action.ActionProvider;
import ddf.catalog.operation.SourceResponse;

public class AtomTransformer extends AbstractAtomTransformer {
    
    private static final String FORMAT_KEY = "format";
    
    private static final Logger LOGGER = LoggerFactory.getLogger( AtomTransformer.class );

    public AtomTransformer( ConfigurationWatcherImpl configWatcher, ActionProvider viewMetacardProvider, ActionProvider metadataProvider, ActionProvider resourceProvider,
            ActionProvider thumbnailProvider, MimeType thumbnailMime, MimeType viewMime, List<SecurityConfiguration> securityConfig ) {
        super( configWatcher, viewMetacardProvider, metadataProvider, resourceProvider, thumbnailProvider, thumbnailMime, viewMime, securityConfig );
    }

    @Override
    public void addFeedElements( Feed feed, SourceResponse response, Map<String, Serializable> properties ) {
        if ( properties.get( FORMAT_KEY ) != null ) {
            setFeedSecurity( feed, properties.get( FORMAT_KEY ).toString() );
        } else {
            LOGGER.debug( "No format was found for response, using default security markings." );
            setFeedSecurity( feed, null );
        }
    }

    @Override
    public void addEntryElements( Entry entry, CDRMetacard metacard, Map<String, Serializable> properties ) {
        setEntrySecurity( entry, metacard );
    }

}
