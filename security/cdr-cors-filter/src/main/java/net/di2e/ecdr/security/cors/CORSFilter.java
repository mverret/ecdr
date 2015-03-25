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
package net.di2e.ecdr.security.cors;

import net.di2e.ecdr.security.cors.config.CORSFilterConfiguration;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CORSFilter extends CrossOriginResourceSharingFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger( CORSFilter.class );

    private CORSFilterConfiguration corsConfig = null;

    public CORSFilter( CORSFilterConfiguration config ) {
        corsConfig = config;
        corsConfig.addCORSFilterConfiguration( this );
    }

    public void destroy() {
        LOGGER.debug( "Removing the CORSFilterConfiguration from CORSFilter" );
        corsConfig.removeCORSFilterConfiguration( this );
    }

}
