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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import net.di2e.ecdr.commons.query.GeospatialCriteria;
import net.di2e.ecdr.commons.query.GeospatialCriteria.SpatialOperator;
import net.di2e.ecdr.commons.query.PropertyCriteria;
import net.di2e.ecdr.commons.query.PropertyCriteria.Operator;
import net.di2e.ecdr.commons.util.SearchConstants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;

import ddf.catalog.data.Metacard;
import ddf.catalog.source.UnsupportedQueryException;

public class LegacyQueryParser extends BasicQueryParser {

    private static final XLogger LOGGER = new XLogger( LoggerFactory.getLogger( LegacyQueryParser.class ) );
    public static final String DAD_SCHEME = "dad";
    public static final String NOT_APPLICABLE = "N/A";

    public LegacyQueryParser() {
        super();
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
        if ( queryParameters.containsKey( Metacard.RESOURCE_URI ) ) {
            String uriString = queryParameters.getFirst( Metacard.RESOURCE_URI );

            if ( StringUtils.isNotEmpty( uriString ) ) {
                URI uri = URI.create( uriString );
                if ( DAD_SCHEME.equals( uri.getScheme() ) ) {
                    String path = uri.getPath();
                    try {
                        path = URLDecoder.decode( path.startsWith( "/" ) ? path.substring( 1 ) : path, "UTF-8" );

                        uri = new URI( DAD_SCHEME + ":///" + URLEncoder.encode( path, "UTF-8" ) + "?"
                                + URLEncoder.encode( URLDecoder.decode( uri.getQuery(), "UTF-8" ), "UTF-8" ).replaceAll( "\\+", "%20" ) + "#"
                                + URLEncoder.encode( URLDecoder.decode( uri.getFragment(), "UTF-8" ), "UTF-8" ) );
                    } catch ( UnsupportedEncodingException e ) {
                        LOGGER.error( "Unsupported URL enciding [UTF-8]: " + e.getMessage(), e );
                    } catch ( URISyntaxException e ) {
                        LOGGER.warn( "Error converting dad scheme specific URI: " + e.getMessage(), e );
                    }

                }
                criteriaList.add( new PropertyCriteria( Metacard.RESOURCE_URI, uri.toString(), Operator.EQUALS ) );
            }
        }
        if ( queryParameters.containsKey( Metacard.CONTENT_TYPE ) ) {
            String contentTypesString = queryParameters.getFirst( Metacard.CONTENT_TYPE );
            if ( StringUtils.isNotEmpty( contentTypesString ) ) {
                criteriaList.add( new PropertyCriteria( Metacard.CONTENT_TYPE, contentTypesString, Operator.EQUALS ) );
            }
        }

        return criteriaList;
    }

}
