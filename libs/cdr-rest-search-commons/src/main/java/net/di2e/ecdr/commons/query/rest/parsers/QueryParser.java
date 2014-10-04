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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import net.di2e.ecdr.commons.query.GeospatialCriteria;
import net.di2e.ecdr.commons.query.PropertyCriteria;
import net.di2e.ecdr.commons.query.TemporalCriteria;
import net.di2e.ecdr.commons.query.TextualCriteria;

import org.opengis.filter.sort.SortBy;

import ddf.catalog.source.UnsupportedQueryException;

public interface QueryParser {
    
    boolean isValidQuery( MultivaluedMap<String, String> queryParameters, String sourceId );

    Collection<String> getSiteNames( MultivaluedMap<String, String> queryParameters );

    String getQueryLanguage( MultivaluedMap<String, String> queryParameters );

    boolean isStrictMode( MultivaluedMap<String, String> queryParameters );
    
    boolean isIncludeStatus( MultivaluedMap<String, String> queryParameters );

    int getStartIndex( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException;

    int getCount( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException;

    long getTimeoutMilliseconds( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException;

    SortBy getSortBy( MultivaluedMap<String, String> queryParameters );

    String getResponseFormat( MultivaluedMap<String, String> queryParameters );

    GeospatialCriteria getGeospatialCriteria( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException;

    TemporalCriteria getTemporalCriteria( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException;

    TextualCriteria getTextualCriteria( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException;
    
    Map<String, Serializable> getQueryProperties( MultivaluedMap<String, String> queryParameters, String sourceId );

    List<PropertyCriteria> getPropertyCriteria( MultivaluedMap<String, String> queryParameters );

}
