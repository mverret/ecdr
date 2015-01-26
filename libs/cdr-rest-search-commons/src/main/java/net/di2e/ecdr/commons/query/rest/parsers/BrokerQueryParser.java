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
package net.di2e.ecdr.commons.query.rest.parsers;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedMap;

import net.di2e.ecdr.commons.constants.BrokerConstants;
import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.query.cache.QueryRequestCache;
import net.di2e.ecdr.commons.sort.SortTypeConfiguration;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;

public class BrokerQueryParser extends LegacyQueryParser {

    private static final XLogger LOGGER = new XLogger( LoggerFactory.getLogger( BrokerQueryParser.class ) );
    private static final Charset ASCII_CHARSET = StandardCharsets.US_ASCII;

    private boolean defaultDedup = true;

    public BrokerQueryParser(List<SortTypeConfiguration> sortTypeConfigurations) {
        super(sortTypeConfigurations);
    }

    public void setDefaultDeduplication( boolean defaultDeduplication ) {
        this.defaultDedup = defaultDeduplication;
        LOGGER.debug( "Updating the default deduplication to [{}]", this.defaultDedup );
    }

    @Override
    public Collection<String> getSiteNames( MultivaluedMap<String, String> queryParameters ) {
        List<String> sources = new ArrayList<String>();
        String sourceString = queryParameters.getFirst( BrokerConstants.SOURCE_PARAMETER );
        if ( sourceString != null && !sourceString.isEmpty() ) {
            String[] sourceArray = sourceString.split( "," );
            sources = Arrays.asList( sourceArray );
        }
        return sources;
    }

    @Override
    public Map<String, Serializable> getQueryProperties( MultivaluedMap<String, String> queryParameters, String sourceId ) {
        Map<String, Serializable> queryProperties = super.getQueryProperties( queryParameters, sourceId );

        String pathString = queryParameters.getFirst( BrokerConstants.PATH_PARAMETER );
        if ( StringUtils.isNotBlank( pathString ) ) {
            pathString += "," + sourceId;
        } else {
            pathString = sourceId;
        }
        queryProperties.put( BrokerConstants.PATH_PARAMETER, pathString );

        String dedupString = queryParameters.getFirst( BrokerConstants.DEDUP_PARAMETER );
        if ( StringUtils.isNotBlank( dedupString ) && isBoolean( dedupString ) ) {
            queryProperties.put( BrokerConstants.DEDUP_PARAMETER, dedupString );
        } else {
            queryProperties.put( BrokerConstants.DEDUP_PARAMETER, defaultDedup );
        }

        String statusString = queryParameters.getFirst( SearchConstants.STATUS_PARAMETER );
        if ( StringUtils.isNotBlank( statusString ) ) {
            queryProperties.put( SearchConstants.STATUS_PARAMETER, statusString );
        }

        String oidString = queryParameters.getFirst( SearchConstants.OID_PARAMETER );
        if ( StringUtils.isNotBlank( oidString ) ) {
            queryProperties.put( SearchConstants.OID_PARAMETER, oidString );
        }

        return queryProperties;
    }

    @Override
    public boolean isValidQuery( MultivaluedMap<String, String> queryParameters, String sourceId ) {
        boolean isValidQuery = super.isValidQuery( queryParameters, sourceId );
        if ( isValidQuery ) {
            if ( !isBooleanNullOrBlank( queryParameters.getFirst( BrokerConstants.DEDUP_PARAMETER ) ) ) {
                isValidQuery = false;
            }
        }
        return isValidQuery;
    }

    protected boolean isUniqueQuery( MultivaluedMap<String, String> queryParameters, String sourceId ) {
        boolean uniqueQuery = true;
        QueryRequestCache queryRequestCache = getQueryRequestCache();
        String oid = queryParameters.getFirst( SearchConstants.OID_PARAMETER );
        if ( StringUtils.isNotBlank( oid ) ) {
            uniqueQuery = queryRequestCache.isQueryIdUnique( oid );
        } else {
            String uuid = UUID.randomUUID().toString();
            queryParameters.putSingle( SearchConstants.OID_PARAMETER, uuid );
            queryRequestCache.add( uuid );
        }

        String path = queryParameters.getFirst( BrokerConstants.PATH_PARAMETER );
        if ( StringUtils.isNotBlank( path ) ) {
            String[] pathValues = path.split( "," );
            if ( ArrayUtils.contains( pathValues, sourceId ) ) {
                uniqueQuery = false;
            }
        }
        return uniqueQuery;
    }

}
