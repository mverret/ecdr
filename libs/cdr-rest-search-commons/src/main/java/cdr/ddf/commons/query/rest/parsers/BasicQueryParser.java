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
package cdr.ddf.commons.query.rest.parsers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;

import cdr.ddf.commons.query.GeospatialCriteria;
import cdr.ddf.commons.query.PropertyCriteria;
import cdr.ddf.commons.query.TemporalCriteria;
import cdr.ddf.commons.query.TextualCriteria;
import cdr.ddf.commons.query.cache.QueryRequestCache;
import cdr.ddf.commons.query.util.GeospatialHelper;
import cdr.ddf.commons.util.SearchConstants;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.impl.filter.TemporalFilter;
import ddf.catalog.source.UnsupportedQueryException;

public class BasicQueryParser implements QueryParser {

    private static final XLogger LOGGER = new XLogger( LoggerFactory.getLogger( BasicQueryParser.class ) );
    private static final int BBOX_COORDINATE_LENGTH = 3;

    private static final Map<String, String> DATETYPE_MAP = new HashMap<String, String>();
    private static final Map<String, String> SORTKEYS_MAP = new HashMap<String, String>();

    static {
        DATETYPE_MAP.put( "created", Metacard.CREATED );
        DATETYPE_MAP.put( "updated", Metacard.MODIFIED );
        DATETYPE_MAP.put( "posted", SearchConstants.POSTED );
        DATETYPE_MAP.put( "infoCutOff", SearchConstants.INFO_CUT_OFF );
        DATETYPE_MAP.put( "validTil", SearchConstants.VALID_TIL );
        DATETYPE_MAP.put( "temporalCoverage", SearchConstants.TEMPORAL_COVERAGE );
        DATETYPE_MAP.put( "effective", Metacard.EFFECTIVE );

        SORTKEYS_MAP.put( "entry/title", Metacard.TITLE );
        SORTKEYS_MAP.put( "entry/date", Metacard.MODIFIED );
        SORTKEYS_MAP.put( "score", Result.RELEVANCE );
        SORTKEYS_MAP.put( "distance", Result.DISTANCE );

    }

    private static final List<String> LANGUAGE_LIST = new ArrayList<String>();
    static {
        LANGUAGE_LIST.add( SearchConstants.CDR_KEYWORD_QUERY_LANGUAGE );
    }

    private int defaultCount;
    private long defaultTimeoutMillis;
    private String defaultDateType;
    private double defaultRadius;
    private String defaultResponseFormat;

    private QueryRequestCache queryRequestCache = null;

    public BasicQueryParser() {
        queryRequestCache = new QueryRequestCache( 1000 );
    }

    public void setDefaultResponseFormat( String defaultFormat ) {
        defaultResponseFormat = defaultFormat;
        LOGGER.debug( "Updating the default response format to [{}]", defaultResponseFormat );
    }

    public void setDefaultCount( int count ) {
        if ( count > 0 ) {
            defaultCount = count;
            LOGGER.debug( "Updating the default count to [{}]", defaultCount );
        } else {
            LOGGER.warn( "Cound not update the default count due to invalid value [{}], the default count will stay at [{}]", count, defaultCount );
        }

    }

    public void setDefaultTimeoutSeconds( long timeout ) {
        if ( timeout > 0 ) {
            defaultTimeoutMillis = timeout * 1000L;
            LOGGER.debug( "Updating the default timeout to [{}] seconds", timeout );
        } else {
            LOGGER.warn( "Cound not update the default timeout due to invalid integer [{}], the default timeout will stay at [{}] seconds", timeout, defaultTimeoutMillis / 1000 );
        }

    }

    public void setDefaultRadiusMeters( double meters ) {
        if ( meters > 0 ) {
            defaultRadius = meters;
            LOGGER.debug( "Updating the default radius to [{}]", defaultRadius );
        } else {
            LOGGER.warn( "Cound not update the default radius due to invalid value [{}], the default radius will stay at [{}]", meters, defaultRadius );
        }

    }

    public void setDefaultDateType( String type ) {
        defaultDateType = type;
        LOGGER.debug( "Updating the default response date type to [{}]", defaultDateType );
    }

    public void setQueryRequestCacheSize( int size ) {
        if ( size > -1 ) {
            queryRequestCache.updateCacheSize( size );
            LOGGER.debug( "Updating the default query request cache size to [{}]", size );
        } else {
            LOGGER.warn( "Cound not update the default query request cache size due to invalid integer value [{}]", size );
        }
    }

    @Override
    public boolean isValidQuery( MultivaluedMap<String, String> queryParameters, String sourceId ) {
        boolean isValidQuery = true;
        String languageList = queryParameters.getFirst( SearchConstants.QUERYLANGUAGE_PARAMETER );
        if ( StringUtils.isNotBlank( languageList ) && !LANGUAGE_LIST.contains( languageList ) ) {
            isValidQuery = false;
        } else if ( !isBooleanOrNull( queryParameters.getFirst( SearchConstants.CASESENSITIVE_PARAMETER ) ) ) {
            isValidQuery = false;
        } else if ( !isBooleanOrNull( queryParameters.getFirst( SearchConstants.STRICTMODE_PARAMETER ) ) ) {
            isValidQuery = false;
        } else if ( !isBooleanOrNull( queryParameters.getFirst( SearchConstants.STATUS_PARAMETER ) ) ) {
            isValidQuery = false;
        } else {
            isValidQuery = isUniqueQuery( queryParameters, sourceId );
        }

        return isValidQuery;
    }

    @Override
    public Collection<String> getSiteNames( MultivaluedMap<String, String> queryParameters ) {
        List<String> sources = new ArrayList<String>();
        return sources;
    }

    @Override
    public boolean isStrictMode( MultivaluedMap<String, String> queryParameters ) {
        String strict = queryParameters.getFirst( SearchConstants.STRICTMODE_PARAMETER );
        return Boolean.TRUE.equals( getBoolean( strict ) );
    }

    @Override
    public boolean isIncludeStatus( MultivaluedMap<String, String> queryParameters ) {
        String status = queryParameters.getFirst( SearchConstants.STATUS_PARAMETER );
        return Boolean.TRUE.equals( getBoolean( status ) );
    }

    private Boolean getBoolean( String booleanString ) {
        Boolean bool = null;
        if ( booleanString != null ) {
            booleanString = booleanString.trim();
            if ( "1".equals( booleanString ) || Boolean.TRUE.toString().equalsIgnoreCase( booleanString ) ) {
                bool = Boolean.TRUE;
            } else if ( "0".equals( booleanString ) || Boolean.FALSE.toString().equalsIgnoreCase( booleanString ) ) {
                bool = Boolean.FALSE;
            }
        }
        return bool;
    }

    @Override
    public String getQueryLanguage( MultivaluedMap<String, String> queryParameters ) {
        String queryLanguage = queryParameters.getFirst( SearchConstants.QUERYLANGUAGE_PARAMETER );
        return queryLanguage == null || queryLanguage.isEmpty() ? null : queryLanguage;
    }

    @Override
    public int getStartIndex( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException {
        String startIndex = queryParameters.getFirst( SearchConstants.STARTINDEX_PARAMETER );
        int index = 1;
        LOGGER.debug( "Attempting to set 'startIndex' value from request [" + startIndex + "] to int" );
        if ( StringUtils.isNotBlank( startIndex ) ) {
            try {
                index = Integer.parseInt( startIndex );
            } catch ( NumberFormatException e ) {
                String message = "Invalid Number found for 'startIndex' [" + startIndex + "].  Resulted in exception: " + e.getMessage();
                LOGGER.warn( message );
                throw new UnsupportedQueryException( message );
            }
        } else {
            LOGGER.debug( "'startIndex' parameter was not specified, defaulting value to [" + index + "]" );
        }
        return index < 1 ? 1 : index;
    }

    @Override
    public int getCount( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException {
        String stringCount = queryParameters.getFirst( SearchConstants.COUNT_PARAMETER );
        int count = this.defaultCount;
        LOGGER.debug( "Attempting to set 'count' value from request [" + stringCount + "] to int" );
        if ( StringUtils.isNotBlank( stringCount ) ) {
            try {
                count = Integer.parseInt( stringCount );
            } catch ( NumberFormatException e ) {
                String message = "Invalid Number found for 'count' [" + stringCount + "].  Resulted in exception: " + e.getMessage();
                LOGGER.warn( message );
                throw new UnsupportedQueryException( message );
            }
        } else {
            LOGGER.debug( "'count' parameter was not specified, defaulting value to [" + count + "]" );
        }
        return count;
    }

    @Override
    public long getTimeoutMilliseconds( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException {
        String timeout = queryParameters.getFirst( SearchConstants.TIMEOUT_PARAMETER );
        long timeoutMilliseconds = this.defaultTimeoutMillis;
        LOGGER.debug( "Attempting to set 'timeout' value from request [" + timeout + "] to long" );
        if ( StringUtils.isNotBlank( timeout ) ) {
            try {
                timeoutMilliseconds = Long.parseLong( timeout );
            } catch ( NumberFormatException e ) {
                String message = "Invalid Number found for 'timeout' [" + timeout + "].  Resulted in exception: " + e.getMessage();
                LOGGER.warn( message );
                if ( isStrictMode( queryParameters ) ) {
                    throw new UnsupportedQueryException( message );
                }
            }
        } else {
            LOGGER.debug( "'timeout' parameter was not specified, defaulting value to [" + timeout + "]" );
        }
        return timeoutMilliseconds;
    }

    @Override
    public SortBy getSortBy( MultivaluedMap<String, String> queryParameters ) {
        String sortByString = queryParameters.getFirst( SearchConstants.SORTKEYS_PARAMETER );
        SortBy sortBy = null;
        if ( StringUtils.isNotBlank( sortByString ) ) {
            String[] sortValues = sortByString.split( "," );
            String sortKey = sortValues[0];
            String sortValue = SORTKEYS_MAP.get( sortKey );
            if ( sortValue != null ) {
                SortOrder sortOrder = SortOrder.ASCENDING;
                if ( sortValues.length >= 3 ) {
                    if ( "false".equalsIgnoreCase( sortValues[2] ) ) {
                        sortOrder = SortOrder.DESCENDING;
                    }
                }
                sortBy = new SortByImpl( sortValue, sortOrder );
            }
        }
        return sortBy;
    }

    @Override
    public String getResponseFormat( MultivaluedMap<String, String> queryParameters ) {
        String format = queryParameters.getFirst( SearchConstants.FORMAT_PARAMETER );
        return StringUtils.isNotBlank( format ) ? format : this.defaultResponseFormat;
    }

    @Override
    public GeospatialCriteria getGeospatialCriteria( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException {
        return createGeospatialCriteria( queryParameters.getFirst( SearchConstants.RADIUS_PARAMETER ), queryParameters.getFirst( SearchConstants.LATITUDE_PARAMETER ),
                queryParameters.getFirst( SearchConstants.LONGITUDE_PARAMETER ), queryParameters.getFirst( SearchConstants.BOX_PARAMETER ),
                queryParameters.getFirst( SearchConstants.GEOMETRY_PARAMETER ), queryParameters.getFirst( SearchConstants.POLYGON_PARAMETER ), isStrictMode( queryParameters ) );

    }

    @Override
    public TemporalCriteria getTemporalCriteria( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException {
        return createTemporalCriteria( queryParameters.getFirst( SearchConstants.STARTDATE_PARAMETER ), queryParameters.getFirst( SearchConstants.ENDDATE_PARAMETER ),
                queryParameters.getFirst( SearchConstants.DATETYPE_PARAMETER ) );
    }

    @Override
    public TextualCriteria getTextualCriteria( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException {
        String words = queryParameters.getFirst( SearchConstants.KEYWORD_PARAMETER );
        String caseSensitive = queryParameters.getFirst( SearchConstants.CASESENSITIVE_PARAMETER );
        TextualCriteria textualCriteria = null;
        if ( StringUtils.isNotBlank( words ) ) {
            Boolean bool = getBoolean( caseSensitive );
            textualCriteria = new TextualCriteria( words, bool == null ? false : bool );
        }
        return textualCriteria;
    }

    @Override
    public Map<String, Serializable> getQueryProperties( MultivaluedMap<String, String> queryParameters, String sourceId ) {
        Map<String, Serializable> queryProps = new HashMap<String, Serializable>();
        String format = queryParameters.getFirst( SearchConstants.FORMAT_PARAMETER );
        queryProps.put( SearchConstants.FORMAT_PARAMETER, StringUtils.isNotBlank( format ) ? format : defaultResponseFormat );
        return queryProps;
    }

    @Override
    public List<PropertyCriteria> getPropertyCriteria( MultivaluedMap<String, String> queryParameters ) {
        return null;
    }

    protected QueryRequestCache getQueryRequestCache() {
        return queryRequestCache;
    }

    protected boolean isUniqueQuery( MultivaluedMap<String, String> queryParameters, String sourceId ) {
        return queryRequestCache.isQueryIdUnique( queryParameters.getFirst( SearchConstants.OID_PARAMETER ), true );
    }

    protected GeospatialCriteria createGeospatialCriteria( String rad, String lat, String lon, String box, String geom, String polygon, boolean strictMode ) throws UnsupportedQueryException {
        GeospatialCriteria geoCriteria = null;
        if ( StringUtils.isNotBlank( box ) ) {
            try {
                String[] bboxArray = box.split( " |,\\p{Space}?" );

                if ( bboxArray.length != BBOX_COORDINATE_LENGTH ) {
                    double minX = Double.parseDouble( bboxArray[0] );
                    double minY = Double.parseDouble( bboxArray[1] );
                    double maxX = Double.parseDouble( bboxArray[2] );
                    double maxY = Double.parseDouble( bboxArray[3] );
                    geoCriteria = new GeospatialCriteria( minX, minY, maxX, maxY );
                } else {
                    throw new UnsupportedQueryException( "Invalid values found for bbox [" + box + "]" );
                }

            } catch ( NumberFormatException e ) {
                LOGGER.warn( "Invalid values found for bbox [" + box + "].  Resulted in exception: " + e.getMessage() );
                if ( strictMode ) {
                    throw new UnsupportedQueryException( "Invalid values found for bbox [" + box + "], values must be numeric." );
                }
            }
            // Only check lat and lon. If Radius is blank is should be defaulted
        } else if ( StringUtils.isNotBlank( lat ) && StringUtils.isNotBlank( lon ) ) {
            try {
                double longitude = Double.parseDouble( lon );
                double latitude = Double.parseDouble( lat );
                double radius = StringUtils.isNotBlank( rad ) ? Double.parseDouble( rad ) : this.defaultRadius;
                geoCriteria = new GeospatialCriteria( latitude, longitude, radius );

            } catch ( NumberFormatException e ) {
                LOGGER.warn( "Invalid Number found for lat [" + lat + "], lon [" + lon + "], and/or radius [" + rad + "].  Resulted in exception: " + e.getMessage() );
                if ( strictMode ) {
                    throw new UnsupportedQueryException( "Invalid Number found for lat [" + lat + "], lon [" + lon + "], and/or radius [" + rad + "]." );
                }
            }

        } else if ( StringUtils.isNotBlank( geom ) ) {
            try {
                WKTReader reader = new WKTReader();
                reader.read( geom );
            } catch ( ParseException e ) {
                LOGGER.warn( "The following is not a valid WKT String: " + geom );
                throw new UnsupportedQueryException( "Invalid WKT, cannot create geospatial query." );
            }
            geoCriteria = new GeospatialCriteria( geom );
        } else if ( StringUtils.isNotBlank( polygon ) ) {
            String wkt = GeospatialHelper.polygonToWKT( polygon );
            try {
                WKTReader reader = new WKTReader();
                reader.read( wkt );
            } catch ( ParseException e ) {
                LOGGER.warn( "The following is not a valid WKT String: " + wkt );
                throw new UnsupportedQueryException( "Invalid WKT, cannot create geospatial query." );
            }
            geoCriteria = new GeospatialCriteria( wkt );
        }
        return geoCriteria;
    }

    protected TemporalCriteria createTemporalCriteria( String start, String end, String type ) throws UnsupportedQueryException {
        TemporalCriteria temporalCriteria = null;

        if ( StringUtils.isNotBlank( start ) || StringUtils.isNotBlank( end ) ) {
            Date startDate = TemporalFilter.parseDate( start );
            Date endDate = TemporalFilter.parseDate( end );
            if ( startDate != null && endDate != null ) {
                if ( startDate.after( endDate ) ) {
                    throw new UnsupportedQueryException( "Start date value [" + startDate + "] cannot be after endDate [" + endDate + "]" );
                }
            }
            String dateType = null;
            LOGGER.debug( "Getting date type name for type [" + type + "]" );
            if ( StringUtils.isNotBlank( type ) ) {
                if ( DATETYPE_MAP.containsKey( type ) ) {
                    dateType = DATETYPE_MAP.get( type );

                    LOGGER.debug( "Date type value retreived in map for request value [" + type + "], setting internal query value to [" + dateType + "]" );
                } else {
                    String message = "Date type value not found in map for type [" + type + "], defaulting internal query value to [" + dateType + "]";
                    LOGGER.warn( message );
                    throw new UnsupportedQueryException( message );
                }
            } else {
                dateType = DATETYPE_MAP.get( this.defaultDateType );
                LOGGER.debug( "Date type value was not specified in request, defaulting internal query value to [" + dateType + "]" );
            }
            temporalCriteria = new TemporalCriteria( startDate, endDate, dateType );
        }
        return temporalCriteria;

    }

    protected boolean isBoolean( String value ) {
        boolean isBoolean = false;
        if ( StringUtils.isNotBlank( value ) ) {
            value = value.toLowerCase();
            isBoolean = value.equals( "false" ) || value.equals( "true" ) || value.equals( "0" ) || value.equals( "1" );
        }

        return isBoolean;
    }

    protected boolean isBooleanOrNull( String value ) {
        boolean isBoolOrNull = true;
        if ( StringUtils.isNotBlank( value ) ) {
            isBoolOrNull = isBoolean( value );
        }
        return isBoolOrNull;
    }

}
