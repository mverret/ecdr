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
package net.di2e.ecdr.security.cors.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.di2e.ecdr.security.cors.CORSFilter;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple CORS Filter Configuration that can handle all simple CORS requests
 * which HTTP GET's fall under.
 */
public class CORSFilterConfigurationImpl implements CORSFilterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger( CORSFilterConfigurationImpl.class );

    private List<CORSFilter> filters = new ArrayList<CORSFilter>();

    public void addCORSFilterConfiguration( CORSFilter filter ) {
        LOGGER.debug( "Adding new CORSFilter and managing the configuration." );
        filters.add( filter );
    }

    /**
     * @param filter
     */
    public void removeCORSFilterConfiguration( CORSFilter filter ) {
        filters.remove( filter );
    }

    /**
     * The origin strings to allow. An empty list allows all origins.
     * 
     * @param allowedOrigins
     *            a list of case-sensitive origin strings.
     */
    public void setAllowOrigins( List<String> allowedOrigins ) {
        for ( CrossOriginResourceSharingFilter filter : filters ) {
            if ( filter != null ) {
                LOGGER.debug( "Setting [allowedOrigins] for CORS filter: " + allowedOrigins );
                filter.setAllowOrigins( removeEmptyAndNulls( allowedOrigins ) );
            }
        }
    }

    /**
     * The value for the Access-Control-Allow-Credentials header. If false, no
     * header is added. If true, the header is added with the value 'true'.
     * 
     * @param allowCredentials
     */
    public void setAllowCredentials( boolean allowCredentials ) {
        for ( CrossOriginResourceSharingFilter filter : filters ) {
            if ( filter != null ) {
                LOGGER.debug( "Setting [allowCredentials] for CORS filter: " + allowCredentials );
                filter.setAllowCredentials( allowCredentials );
            }
        }
    }

    /**
     * A list of non-simple headers to be exposed via
     * Access-Control-Expose-Headers.
     * 
     * @param exposeHeaders
     *            the list of (case-sensitive) header names.
     */
    public void setExposeHeaders( List<String> exposeHeaders ) {
        for ( CrossOriginResourceSharingFilter filter : filters ) {
            if ( filter != null ) {
                LOGGER.debug( "Setting [exposeHeaders] for CORS filter: " + exposeHeaders );
                filter.setExposeHeaders( removeEmptyAndNulls( exposeHeaders ) );
            }
        }
    }

    private List<String> removeEmptyAndNulls( List<String> list ) {
        for ( Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
            String string = iterator.next();
            if ( string.isEmpty() ) {
                // Remove the current element from the iterator and the list.
                iterator.remove();
            }
        }
        return list;
    }
}
