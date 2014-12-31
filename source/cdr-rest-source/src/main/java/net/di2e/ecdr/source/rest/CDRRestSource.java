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

    private Map<String, String> parameterMap = new HashMap<String, String>();
    private Map<String, String> harcodedParamMap = new HashMap<String, String>();

    private FilterConfig filterConfig = null;

    public CDRRestSource( FilterAdapter filterAdapter ) {
        super( filterAdapter );
        filterConfig = new FilterConfig();
        LOGGER.info( "Creating a new CDRRestSource." );
        setPingMethod(PingMethod.HEAD);
    }

    @Override
    public Map<String, String> getDynamicUrlParameterMap() {
        return parameterMap;
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

    @Override
    public boolean canHandleUidQuery() {
        return true;
    }

    public void setDoSourcePing( boolean doPing ) {
        LOGGER.debug( "ConfigUpdate: Updating the doSourcePing value from to [{}]", doPing );
        setPingMethod( doPing ? PingMethod.HEAD : PingMethod.NONE );
    }

    public synchronized void setUrl(String endpointUrl) {
        super.setPingUrl( endpointUrl );
        super.setUrl(endpointUrl);
    }

}
