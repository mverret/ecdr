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
package cdr.ddf.commons.query.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;

import cdr.ddf.commons.util.SearchConstants;
import ddf.catalog.operation.Query;
import ddf.catalog.operation.QueryResponse;

public final class QueryHelper {

    private QueryHelper() {
    }

    private static final XLogger LOGGER = new XLogger( LoggerFactory.getLogger( QueryHelper.class ) );

    public static Map<String, Serializable> getTransformLinkProperties( UriInfo uriInfo, Query query, QueryResponse response, String scheme, String host, Integer port ) {
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();

        uriBuilder = updateURLWithPlatformValues( uriBuilder, scheme, host, port );

        String selfLink = uriBuilder.toTemplate();
        properties.put( SearchConstants.SELF_LINK_REL, selfLink );
        LOGGER.debug( "Adding self link parameter[{}] with value [{}] to transform properties to be sent to result transformer", SearchConstants.SELF_LINK_REL, selfLink );
        int startIndex = query.getStartIndex();
        int pageSize = query.getPageSize();
        long totalCount = response.getHits();

        if ( startIndex + pageSize <= totalCount ) {
            String template = uriBuilder.replaceQueryParam( SearchConstants.STARTINDEX_PARAMETER, String.valueOf( startIndex + pageSize ) ).toTemplate();
            properties.put( SearchConstants.NEXT_LINK_REL, template );
            LOGGER.debug( "Adding next link parameter[{}] with value [{}] to transform properties to be sent to result transformer", SearchConstants.NEXT_LINK_REL, template );
        }

        if ( startIndex > 1 ) {
            String template = uriBuilder.replaceQueryParam( SearchConstants.STARTINDEX_PARAMETER, String.valueOf( Math.max( 1, startIndex - pageSize ) ) ).toTemplate();
            properties.put( SearchConstants.PREV_LINK_REL, template );
            LOGGER.debug( "Adding previous link parameter[{}] with value [{}] to transform properties to be sent to result transformer", SearchConstants.PREV_LINK_REL, template );
        }
        return properties;
    }

    public static UriBuilder updateURLWithPlatformValues( UriBuilder builder, String scheme, String host, Integer port ) {
        if ( StringUtils.isNotBlank( scheme ) && StringUtils.isNotBlank( host ) ) {
            LOGGER.debug( "Using values from Platform Configuration for Atom Links host[" + host + "], scheme[" + scheme + "], and port[" + port + "]" );
            builder.scheme( scheme );
            builder.host( host );
            if ( port != null ) {
                builder.port( port );
            }
        }
        return builder;
    }

}
