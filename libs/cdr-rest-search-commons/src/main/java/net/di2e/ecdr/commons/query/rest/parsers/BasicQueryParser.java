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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import net.di2e.ecdr.commons.CDRMetacard;
import net.di2e.ecdr.commons.constants.BrokerConstants;
import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.query.GeospatialCriteria;
import net.di2e.ecdr.commons.query.PropertyCriteria;
import net.di2e.ecdr.commons.query.PropertyCriteria.Operator;
import net.di2e.ecdr.commons.query.TemporalCriteria;
import net.di2e.ecdr.commons.query.TextualCriteria;
import net.di2e.ecdr.commons.query.cache.QueryRequestCache;
import net.di2e.ecdr.commons.query.util.GeospatialHelper;
import net.di2e.ecdr.commons.sort.SortTypeConfiguration;
import net.di2e.ecdr.commons.util.SearchUtils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.catalog.data.Metacard;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.source.UnsupportedQueryException;

public class BasicQueryParser implements QueryParser {

    private static final Logger LOGGER = LoggerFactory.getLogger( BasicQueryParser.class );

    private static final int DEFAULT_QUERYID_CACHE_SIZE = 1000;

    private static final Map<String, String> DATETYPE_MAP = new HashMap<String, String>();

    private List<SortTypeConfiguration> sortTypeConfigurationList;

    static {
        DATETYPE_MAP.put( "created", Metacard.CREATED );
        DATETYPE_MAP.put( "updated", Metacard.MODIFIED );
        DATETYPE_MAP.put( "posted", SearchConstants.POSTED );
        DATETYPE_MAP.put( "infoCutOff", SearchConstants.INFO_CUT_OFF );
        DATETYPE_MAP.put( "validTil", SearchConstants.VALID_TIL );
        DATETYPE_MAP.put( "temporalCoverage", SearchConstants.TEMPORAL_COVERAGE );
        DATETYPE_MAP.put( "effective", Metacard.EFFECTIVE );

    }

    private static final List<String> LANGUAGE_LIST = new ArrayList<String>();

    static {
        LANGUAGE_LIST.add( SearchConstants.CDR_KEYWORD_QUERY_LANGUAGE );
        LANGUAGE_LIST.add( SearchConstants.CDR_CQL_QUERY_LANGUAGE );
    }

    private static DateTimeFormatter formatter;

    /*
     * The OpenSearch specification uses RFC 3339, which is a specific profile of the ISO 8601 standard and corresponds
     * to the second and (as a "rarely used option") the first parser below. We additionally support the corresponding
     * ISO 8601 Basic profiles.
     */
    static {
        DateTimeParser[] parsers = { ISODateTimeFormat.dateTime().getParser(), ISODateTimeFormat.dateTimeNoMillis().getParser(), ISODateTimeFormat.basicDateTime().getParser(),
                ISODateTimeFormat.basicDateTimeNoMillis().getParser() };
        formatter = new DateTimeFormatterBuilder().append( null, parsers ).toFormatter();
    }

    private int defaultCount = 100;
    private long defaultTimeoutMillis = 300000;
    private String defaultDateType = "effective";
    private double defaultRadius = 10000;
    private String defaultResponseFormat = SearchConstants.ATOM_RESPONSE_FORMAT;;
    private boolean defaultFuzzySearch = true;
    private Map<String, String> parameterExtensionMap = SearchUtils.convertToMap( "uid=id" );
    private List<String> parameterPropertyList = new ArrayList<>();

    private QueryRequestCache queryRequestCache = null;

    public BasicQueryParser( List<SortTypeConfiguration> sortTypeConfigurations ) {
        sortTypeConfigurationList = sortTypeConfigurations;
        queryRequestCache = new QueryRequestCache( DEFAULT_QUERYID_CACHE_SIZE );
        parameterPropertyList.add( "oid" );
        parameterPropertyList.add( "path" );
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
            LOGGER.warn( "Could not update the default count due to invalid value [{}], the default count will stay at [{}]", count, defaultCount );
        }

    }

    public void setDefaultTimeoutSeconds( long timeout ) {
        if ( timeout > 0 ) {
            defaultTimeoutMillis = timeout * 1000L;
            LOGGER.debug( "Updating the default timeout to [{}] seconds", timeout );
        } else {
            LOGGER.warn( "Could not update the default timeout due to invalid integer [{}], the default timeout will stay at [{}] seconds", timeout, defaultTimeoutMillis / 1000 );
        }

    }

    public void setDefaultRadiusMeters( double meters ) {
        if ( meters > 0 ) {
            defaultRadius = meters;
            LOGGER.debug( "Updating the default radius to [{}]", defaultRadius );
        } else {
            LOGGER.warn( "Could not update the default radius due to invalid value [{}], the default radius will stay at [{}]", meters, defaultRadius );
        }

    }

    public void setDefaultDateType( String type ) {
        defaultDateType = type;
        LOGGER.debug( "Updating the default response date type to [{}]", defaultDateType );
    }

    public void setDefaultFuzzySearch( boolean fuzzy ) {
        LOGGER.debug( "ConfigUpdate: Updating the default fuzzy search from [{}] to [{}]", defaultFuzzySearch, fuzzy );
        defaultFuzzySearch = fuzzy;
    }

    public void setQueryRequestCacheSize( int size ) {
        if ( size > -1 ) {
            queryRequestCache.updateCacheSize( size );
            LOGGER.debug( "Updating the default query request cache size to [{}]", size );
        } else {
            LOGGER.warn( "Could not update the default query request cache size due to invalid integer value [{}]", size );
        }
    }

    public void setExtensionMap( List<String> extensionMap ) {
        this.parameterExtensionMap = SearchUtils.convertToMap( extensionMap );
    }

    public void setPropertyList( List<String> propertyList ) {
        this.parameterPropertyList = propertyList;
    }

    @Override
    public boolean isValidQuery( MultivaluedMap<String, String> queryParameters, String sourceId ) {
        boolean isValidQuery = true;
        String queryLang = queryParameters.getFirst( SearchConstants.QUERYLANGUAGE_PARAMETER );
        if ( StringUtils.isNotBlank( queryLang ) && !LANGUAGE_LIST.contains( queryLang ) ) {
            isValidQuery = false;
            LOGGER.debug( "The query is not valid because the {} parameter with value {} is not in the allowed values {}", SearchConstants.QUERYLANGUAGE_PARAMETER, queryLang, LANGUAGE_LIST );
        } else if ( !isBooleanNullOrBlank( queryParameters.getFirst( SearchConstants.CASESENSITIVE_PARAMETER ) ) ) {
            isValidQuery = false;
            LOGGER.debug( "The query is not valid because the {} parameter with value {} is not valid", SearchConstants.CASESENSITIVE_PARAMETER,
                    queryParameters.getFirst( SearchConstants.CASESENSITIVE_PARAMETER ) );
        } else if ( !isBooleanNullOrBlank( queryParameters.getFirst( SearchConstants.STRICTMODE_PARAMETER ) ) ) {
            isValidQuery = false;
            LOGGER.debug( "The query is not valid because the {} parameter with value {} is not valid", SearchConstants.STRICTMODE_PARAMETER,
                    queryParameters.getFirst( SearchConstants.STRICTMODE_PARAMETER ) );
        } else if ( !isBooleanNullOrBlank( queryParameters.getFirst( SearchConstants.STATUS_PARAMETER ) ) ) {
            isValidQuery = false;
            LOGGER.debug( "The query is not valid because the {} parameter with value {} is not valid", SearchConstants.STATUS_PARAMETER,
                    queryParameters.getFirst( SearchConstants.STATUS_PARAMETER ) );
        } else if ( !isBooleanNullOrBlank( queryParameters.getFirst( SearchConstants.FUZZY_PARAMETER ) ) ) {
            isValidQuery = false;
            LOGGER.debug( "The query is not valid because the {} parameter with value {} is not valid", SearchConstants.FUZZY_PARAMETER, queryParameters.getFirst( SearchConstants.FUZZY_PARAMETER ) );
        } else {
            isValidQuery = isUniqueQuery( queryParameters, sourceId );
            LOGGER.debug( "Checking if the query is valid: {}", isValidQuery );
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
        // Include status is true unless explicitly set to false
        Boolean boolStatus = getBoolean( queryParameters.getFirst( SearchConstants.STATUS_PARAMETER ) );
        return Boolean.FALSE.equals( boolStatus ) ? false : true;
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
        LOGGER.debug( "Attempting to set 'startIndex' value from request [{}] to int", startIndex );
        if ( StringUtils.isNotBlank( startIndex ) ) {
            try {
                index = Integer.parseInt( startIndex );
            } catch ( NumberFormatException e ) {
                String message = "Invalid Number found for 'startIndex' [" + startIndex + "].  Resulted in exception: " + e.getMessage();
                LOGGER.warn( message );
                throw new UnsupportedQueryException( message );
            }
        } else {
            LOGGER.debug( "'startIndex' parameter was not specified, defaulting value to [{}]", index );
        }
        return index < 1 ? 1 : index;
    }

    @Override
    public int getCount( MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException {
        String stringCount = queryParameters.getFirst( SearchConstants.COUNT_PARAMETER );
        int count = this.defaultCount;
        LOGGER.debug( "Attempting to set 'count' value from request [{}] to int", stringCount );
        if ( StringUtils.isNotBlank( stringCount ) ) {
            try {
                count = Integer.parseInt( stringCount );
            } catch ( NumberFormatException e ) {
                String message = "Invalid Number found for 'count' [" + stringCount + "].  Resulted in exception: " + e.getMessage();
                LOGGER.warn( message );
                throw new UnsupportedQueryException( message );
            }
        } else {
            LOGGER.debug( "'count' parameter was not specified, defaulting value to [{}]", count );
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
                if ( timeoutMilliseconds <= 0 ) {
                    throw new UnsupportedQueryException( "The [" + SearchConstants.TIMEOUT_PARAMETER + "] parameter cannot nbe less than 0 and was [" + timeout + "]" );
                }
            } catch ( NumberFormatException e ) {
                String message = "Invalid Number found for 'timeout' [" + timeout + "].  Resulted in exception: " + e.getMessage();
                LOGGER.warn( message );
                if ( isStrictMode( queryParameters ) ) {
                    throw new UnsupportedQueryException( message );
                }
            }
        } else {
            LOGGER.debug( "'timeout' parameter was not specified, defaulting value to [{}]", timeout );
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
            SortTypeConfiguration sortType = getSortConfiguration( sortKey );
            if ( sortType != null ) {
                String sortAttribute = sortType.getSortAttribute();
                SortOrder sortOrder = null;
                if ( sortValues.length >= 3 ) {
                    if ( Boolean.FALSE.toString().equalsIgnoreCase( sortValues[2] ) ) {
                        sortOrder = SortOrder.DESCENDING;
                    } else {
                        sortOrder = SortOrder.ASCENDING;
                    }
                } else {
                    sortOrder = SortOrder.valueOf( sortType.getSortOrder() );
                }
                sortBy = new SortByImpl( sortAttribute, sortOrder );
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

        TextualCriteria textualCriteria = null;
        if ( StringUtils.isNotBlank( words ) ) {
            String stringFuzzy = queryParameters.getFirst( SearchConstants.FUZZY_PARAMETER );
            LOGGER.debug( "Attempting to set 'fuzzy' value from request [{}]", stringFuzzy );
            Boolean fuzzy = getBoolean( stringFuzzy );
            if ( fuzzy == null ) {
                LOGGER.debug( "The 'fuzzy' parameter was not specified, defaulting value to [{}]", defaultFuzzySearch );
                fuzzy = defaultFuzzySearch;
            }

            String caseSensitiveString = queryParameters.getFirst( SearchConstants.CASESENSITIVE_PARAMETER );
            LOGGER.debug( "Attempting to set '{}' value from request [{}], will default to false if not boolean", SearchConstants.CASESENSITIVE_PARAMETER, caseSensitiveString );
            Boolean caseSensitive = getBoolean( caseSensitiveString );

            textualCriteria = new TextualCriteria( words, caseSensitive == null ? false : caseSensitive, fuzzy );
        }
        return textualCriteria;
    }

    @Override
    public Map<String, Serializable> getQueryProperties( MultivaluedMap<String, String> queryParameters, String sourceId ) {
        Map<String, Serializable> queryProps = new HashMap<String, Serializable>();
        String format = queryParameters.getFirst( SearchConstants.FORMAT_PARAMETER );
        queryProps.put( SearchConstants.FORMAT_PARAMETER, StringUtils.isNotBlank( format ) ? format : defaultResponseFormat );
        for ( String key : queryParameters.keySet() ) {
            String value = queryParameters.getFirst( key );
            if ( StringUtils.isNotBlank( value ) && parameterPropertyList.contains( key ) ) {
                queryProps.put( key, value );
            }
        }
        return queryProps;
    }

    @Override
    public List<PropertyCriteria> getPropertyCriteria( MultivaluedMap<String, String> queryParameters ) {
        List<PropertyCriteria> criteriaList = new ArrayList<PropertyCriteria>();
        Set<String> keySet = queryParameters.keySet();
        for ( String key : keySet ) {
            String value = queryParameters.getFirst( key );
            if ( StringUtils.isNotBlank( value ) && parameterExtensionMap.containsKey( key ) ) {
                criteriaList.add( new PropertyCriteria( parameterExtensionMap.get( key ), value, Operator.LIKE ) );
            }
        }
        if ( queryParameters.containsKey( SearchConstants.CONTENT_COLLECTIONS_PARAMETER ) ) {
            String contentCollections = queryParameters.getFirst( SearchConstants.CONTENT_COLLECTIONS_PARAMETER );
            if ( StringUtils.isNotEmpty( contentCollections ) ) {
                criteriaList.add( new PropertyCriteria( CDRMetacard.METACARD_CONTENT_COLLECTION_ATTRIBUTE, contentCollections, Operator.LIKE ) );
            }
        }
        return criteriaList;
    }

    @Override
    public String getGeoRSSFormat( MultivaluedMap<String, String> queryParameters ) {
        return StringUtils.defaultIfBlank( queryParameters.getFirst( SearchConstants.GEORSS_RESULT_FORMAT_PARAMETER ), null );
    }

    protected QueryRequestCache getQueryRequestCache() {
        return queryRequestCache;
    }

    protected boolean isUniqueQuery( MultivaluedMap<String, String> queryParameters, String sourceId ) {
        boolean isUniqueQuery = true;
        isUniqueQuery = queryRequestCache.isQueryIdUnique( queryParameters.getFirst( SearchConstants.OID_PARAMETER ) );

        String path = queryParameters.getFirst( BrokerConstants.PATH_PARAMETER );
        if ( StringUtils.isNotBlank( path ) ) {
            String[] pathValues = path.split( "," );
            if ( ArrayUtils.contains( pathValues, sourceId ) ) {
                isUniqueQuery = false;
                LOGGER.debug( "The '{}' with value '{}' contains the local source id {}", BrokerConstants.PATH_PARAMETER, path, sourceId );
            }
        }
        return isUniqueQuery;
    }

    protected GeospatialCriteria createGeospatialCriteria( String rad, String lat, String lon, String box, String geom, String polygon, boolean strictMode ) throws UnsupportedQueryException {
        GeospatialCriteria geoCriteria = null;
        if ( StringUtils.isNotBlank( box ) ) {
            try {
                String[] bboxArray = box.split( " |,\\p{Space}?" );

                if ( bboxArray.length != 3 ) {
                    double minX = Double.parseDouble( bboxArray[0] );
                    double minY = Double.parseDouble( bboxArray[1] );
                    double maxX = Double.parseDouble( bboxArray[2] );
                    double maxY = Double.parseDouble( bboxArray[3] );
                    geoCriteria = new GeospatialCriteria( minX, minY, maxX, maxY );
                } else {
                    throw new UnsupportedQueryException( "Invalid values found for bbox [" + box + "]" );
                }

            } catch ( NumberFormatException e ) {
                LOGGER.warn( "Invalid values found for bbox [{}].  Resulted in exception: {}", box, e.getMessage() );
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
                LOGGER.warn( "Invalid Number found for lat [{}], lon [{}], and/or radius [{}].  Resulted in exception: {}", lat, lon, rad, e.getMessage() );
                if ( strictMode ) {
                    throw new UnsupportedQueryException( "Invalid Number found for lat [" + lat + "], lon [" + lon + "], and/or radius [" + rad + "]." );
                }
            }

        } else if ( StringUtils.isNotBlank( geom ) ) {
            try {
                WKTReader reader = new WKTReader();
                reader.read( geom );
            } catch ( ParseException e ) {
                LOGGER.warn( "The following is not a valid WKT String: {}", geom );
                throw new UnsupportedQueryException( "Invalid WKT, cannot create geospatial query." );
            }
            geoCriteria = new GeospatialCriteria( geom );
        } else if ( StringUtils.isNotBlank( polygon ) ) {
            String wkt = GeospatialHelper.polygonToWKT( polygon );
            try {
                WKTReader reader = new WKTReader();
                reader.read( wkt );
            } catch ( ParseException e ) {
                LOGGER.warn( "The following is not a valid WKT String: {}", wkt );
                throw new UnsupportedQueryException( "Invalid WKT, cannot create geospatial query." );
            }
            geoCriteria = new GeospatialCriteria( wkt );
        }
        return geoCriteria;
    }

    protected TemporalCriteria createTemporalCriteria( String start, String end, String type ) throws UnsupportedQueryException {
        TemporalCriteria temporalCriteria = null;

        if ( StringUtils.isNotBlank( start ) || StringUtils.isNotBlank( end ) ) {
            Date startDate = parseDate( start );
            Date endDate = parseDate( end );
            if ( startDate != null && endDate != null ) {
                if ( startDate.after( endDate ) ) {
                    throw new UnsupportedQueryException( "Start date value [" + startDate + "] cannot be after endDate [" + endDate + "]" );
                }
            }
            String dateType = null;
            LOGGER.debug( "Getting date type name for type [{}]", type );
            if ( StringUtils.isNotBlank( type ) ) {
                if ( DATETYPE_MAP.containsKey( type ) ) {
                    dateType = DATETYPE_MAP.get( type );

                    LOGGER.debug( "Date type value received in map for request value [{}], setting internal query value to [{}]", type, dateType );
                } else {
                    String message = "Date type value not found in map for type [" + type + "], defaulting internal query value to [" + dateType + "]";
                    LOGGER.warn( message );
                    throw new UnsupportedQueryException( message );
                }
            } else {
                dateType = DATETYPE_MAP.get( this.defaultDateType );
                LOGGER.debug( "Date type value was not specified in request, defaulting internal query value to [{}]", dateType );
            }
            temporalCriteria = new TemporalCriteria( startDate, endDate, dateType );
        }
        return temporalCriteria;

    }

    protected Date parseDate( String date ) throws UnsupportedQueryException {
        Date returnDate = null;
        if ( StringUtils.isNotBlank( date ) ) {
            try {
                returnDate = formatter.parseDateTime( date ).toDate();
            } catch ( IllegalArgumentException e ) {
                LOGGER.warn( "Could not process date because of invalid format [{}]", date );
                throw new UnsupportedQueryException( "Invalid date format [" + date + "]" );
            }
        }
        return returnDate;
    }

    protected boolean isBoolean( String value ) {
        boolean isBoolean = false;
        if ( StringUtils.isNotBlank( value ) ) {
            value = value.toLowerCase();
            isBoolean = value.equals( "false" ) || value.equals( "true" ) || value.equals( "0" ) || value.equals( "1" );
        }

        return isBoolean;
    }

    protected boolean isBooleanNullOrBlank( String value ) {
        boolean isBoolOrNull = true;
        if ( StringUtils.isNotBlank( value ) ) {
            isBoolOrNull = isBoolean( value );
        }
        return isBoolOrNull;
    }

    protected Boolean getBoolean( String booleanString ) {
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

    private SortTypeConfiguration getSortConfiguration( String sortKey ) {
        for ( SortTypeConfiguration sortType : sortTypeConfigurationList ) {
            LOGGER.debug( "Comparing incoming sort key of {} with configuration of key {}", sortKey, sortType.getSortKey() );
            if ( sortType.getSortKey().equals( sortKey ) ) {
                return sortType;
            }
        }
        return null;
    }

}
