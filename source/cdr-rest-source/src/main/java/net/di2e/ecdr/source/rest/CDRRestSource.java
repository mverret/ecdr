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
package net.di2e.ecdr.source.rest;

import java.util.HashMap;
import java.util.Map;

import net.di2e.ecdr.commons.filter.config.FilterConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.SourceResponse;

public class CDRRestSource extends AbstractCDRSource {

    private static final Logger LOGGER = LoggerFactory.getLogger( CDRRestSource.class );

    private Map<String, String> harcodedParamMap = new HashMap<String, String>();

    private FilterConfig filterConfig = null;

    public CDRRestSource( FilterAdapter filterAdapter ) {
        super( filterAdapter );
        filterConfig = new FilterConfig();
        LOGGER.info( "Creating a new CDRRestSource." );
        setPingMethod(PingMethod.HEAD);
        setSendSecurityCookie( true );
    }

    @Override
    public Map<String, String> getStaticUrlQueryValues() {
        return harcodedParamMap;
    }

    @Override
    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    @Override
    public SourceResponse enhanceResults( SourceResponse response ) {
        return response;
    }

    @Override
    public SourceResponse lookupById( QueryRequest queryRequest, String id ) {
        return null;
    }

    public void setDoSourcePing( boolean doPing ) {
        LOGGER.debug( "ConfigUpdate: Updating the doSourcePing value from to [{}]", doPing );
        setPingMethod( doPing ? PingMethod.HEAD : PingMethod.NONE );
    }

    public synchronized void setUrl(String endpointUrl) {
        super.setPingUrl( endpointUrl );
        super.setUrl(endpointUrl);
    }

    @Override
    public boolean useDefaultParameters() {
        return true;
    }

}
