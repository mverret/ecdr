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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.query.GeospatialCriteria;
import net.di2e.ecdr.commons.query.GeospatialCriteria.SpatialOperator;
import net.di2e.ecdr.commons.query.PropertyCriteria;
import net.di2e.ecdr.commons.query.PropertyCriteria.Operator;
import net.di2e.ecdr.commons.sort.SortTypeConfiguration;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;

import ddf.catalog.data.Metacard;
import ddf.catalog.source.UnsupportedQueryException;

public class LegacyQueryParser extends BasicQueryParser {

    private static final XLogger LOGGER = new XLogger( LoggerFactory.getLogger( LegacyQueryParser.class ) );
    public static final String DAD_SCHEME = "dad:///";
    public static final String NOT_APPLICABLE = "N/A";

    public LegacyQueryParser(List<SortTypeConfiguration> sortTypeConfigurations) {
        super(sortTypeConfigurations);
    }

    @Override
    public GeospatialCriteria getGeospatialCriteria( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException {
        GeospatialCriteria geoCrit = super.getGeospatialCriteria( queryParameters );
        if ( geoCrit != null ) {
            String operator = queryParameters.getFirst( SearchConstants.GEOSPATIALOPERATOR_PARAMETER );
            if ( StringUtils.isNotBlank( operator ) ) {
                if ( operator.equalsIgnoreCase( SpatialOperator.Contains.name() ) ) {
                    geoCrit.setSpatialOperator( SpatialOperator.Contains );
                }
            }
        }
        return geoCrit;
    }

    public List<PropertyCriteria> getPropertyCriteria( MultivaluedMap<String, String> queryParameters ) {
        List<PropertyCriteria> criteriaList = super.getPropertyCriteria( queryParameters );
        if ( queryParameters.containsKey( SearchConstants.RESOURCE_URI_PARAMETER ) ) {
            String uriString = queryParameters.getFirst( SearchConstants.RESOURCE_URI_PARAMETER );

            if ( StringUtils.isNotEmpty( uriString ) ) {
                if ( uriString.startsWith( DAD_SCHEME ) ) {
                    try {
                        String uriSubstring = uriString;
                        StringBuilder sb = new StringBuilder( DAD_SCHEME );
                        uriSubstring = uriSubstring.substring( DAD_SCHEME.length() );
                        int index = uriSubstring.indexOf( '?' );
                        sb.append( URLEncoder.encode( uriSubstring.substring( 0, index ), "UTF-8" ) );
                        sb.append( "?" );
                        uriSubstring = uriSubstring.substring( index + 1 );
                        index = uriSubstring.indexOf( '#' );
                        sb.append( URLEncoder.encode( uriSubstring.substring( 0, index ), "UTF-8" ) );
                        sb.append( "#" );
                        uriSubstring = uriSubstring.substring( index + 1 );
                        sb.append( URLEncoder.encode( uriSubstring.substring( 0 ), "UTF-8" ) );

                        uriString = sb.toString();
                    } catch ( UnsupportedEncodingException | RuntimeException e ) {
                        LOGGER.warn( "Could parse the 'resource-uri' due to exception so falling back to not parsing: " + e.getMessage() );
                    }
                }
                criteriaList.add( new PropertyCriteria( Metacard.RESOURCE_URI, uriString, Operator.EQUALS ) );
            }
        }
        if ( queryParameters.containsKey( SearchConstants.CONTENT_TYPE_PARAMETER ) ) {
            String contentTypesString = queryParameters.getFirst( SearchConstants.CONTENT_TYPE_PARAMETER );
            if ( StringUtils.isNotEmpty( contentTypesString ) ) {
                criteriaList.add( new PropertyCriteria( Metacard.CONTENT_TYPE, contentTypesString, Operator.EQUALS ) );
            }
        }

        return criteriaList;
    }

}
