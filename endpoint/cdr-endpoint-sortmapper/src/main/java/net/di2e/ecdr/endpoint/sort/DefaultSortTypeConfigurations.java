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
package net.di2e.ecdr.endpoint.sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.opengis.filter.sort.SortOrder;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;

public class DefaultSortTypeConfigurations {

    private static final String MAPPING_PID = "ecdr-sort-mapping";

    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultSortTypeConfigurations.class );

    private ConfigurationAdmin configAdmin;

    private List<Configuration> configurationList = new ArrayList<>();

    public enum SortMap {

        TITLE( "entry/title", Metacard.TITLE, SortOrder.ASCENDING.name() ), MODIFIED_DATE( "entry/date", Metacard.MODIFIED, SortOrder.DESCENDING.name() ), EFFECTIVE_DATE( "effective",
                Metacard.EFFECTIVE, SortOrder.DESCENDING.name() ), SCORE( "score", Result.RELEVANCE, SortOrder.DESCENDING.name() ), DISTANCE( "distance", Result.DISTANCE, SortOrder.ASCENDING.name() );

        private final String key;
        private final String attribute;
        private final String order;

        SortMap( String sortKey, String sortAttribute, String sortOrder ) {
            key = sortKey;
            attribute = sortAttribute;
            order = sortOrder;
        }

        public String getKey() {
            return key;
        }

        public String getAttribute() {
            return attribute;
        }

        public String getOrder() {
            return order;
        }

    }

    public DefaultSortTypeConfigurations( ConfigurationAdmin configurationAdmin ) {
        configAdmin = configurationAdmin;
    }

    public void init() throws IOException {
        for ( SortMap map : SortMap.values() ) {
            LOGGER.debug( "Adding configuration with key {}", map.getKey() );
            Configuration configuration = configAdmin.createFactoryConfiguration( MAPPING_PID );
            Dictionary<String, String> properties = new Hashtable<>();
            properties.put( "sortKey", map.getKey() );
            properties.put( "sortAttribute", map.getAttribute() );
            properties.put( "sortOrder", map.getOrder() );
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
