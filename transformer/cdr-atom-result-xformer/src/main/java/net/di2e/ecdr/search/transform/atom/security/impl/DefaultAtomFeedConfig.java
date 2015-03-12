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
package net.di2e.ecdr.search.transform.atom.security.impl;

    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.Dictionary;
    import java.util.Hashtable;
    import java.util.List;

    import org.osgi.service.cm.Configuration;
    import org.osgi.service.cm.ConfigurationAdmin;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

public class DefaultAtomFeedConfig {

    private static final String CONFIG_PID = "cdr-atom-feed-configuration";

    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultAtomFeedConfig.class );

    private ConfigurationAdmin configAdmin;

    private List<Configuration> configurationList = new ArrayList<>();

    public enum DefaultConfig {

        ATOM_DDMS_20( "atom-ddms-2.0", "urn:us:gov:ic:ism:v2", "classification=U,ownerProducer=USA" ),
        ATOM_DDMS_41( "atom-ddms-4.1", "urn:us:gov:ic:ism", "DESVersion=9,classification=U,ownerProducer=USA" );

        private final String format;
        private final String namespace;
        private final String attributes;

        DefaultConfig( String entryFormat, String entryNamespace, String entryAttributes ) {
            format = entryFormat;
            namespace = entryNamespace;
            attributes = entryAttributes;
        }

        public String getFormat() {
            return format;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getAttributes() {
            return attributes;
        }

    }

    public DefaultAtomFeedConfig( ConfigurationAdmin configurationAdmin ) {
        configAdmin = configurationAdmin;
    }

    public void init() throws IOException {
        for ( DefaultConfig config : DefaultConfig.values() ) {
            LOGGER.debug( "Adding configuration with format {}", config.getFormat() );
            Configuration configuration = configAdmin.createFactoryConfiguration( CONFIG_PID );
            Dictionary<String, String> properties = new Hashtable<>();
            properties.put( "format", config.getFormat() );
            properties.put( "namespace", config.getNamespace() );
            properties.put( "attributeList", config.getAttributes() );
            configuration.update( properties );
            configurationList.add( configuration );
        }
    }

    public void destroy() throws IOException {
        for ( Configuration curConfig : configurationList ) {
            curConfig.delete();
        }
    }

}
