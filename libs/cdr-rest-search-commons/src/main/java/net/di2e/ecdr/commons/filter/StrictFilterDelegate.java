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
package net.di2e.ecdr.commons.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.filter.config.FilterConfig;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;

public class StrictFilterDelegate extends AbstractFilterDelegate<Map<String, String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger( StrictFilterDelegate.class );

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat.dateTime();;

    private static final Map<String, String> DATETYPE_MAP = new HashMap<String, String>();
    public static final Map<String, String> SORTKEYS_MAP = new HashMap<String, String>();

    private boolean strictMode = false;

    static {
        DATETYPE_MAP.put( Metacard.CREATED, "created" );
        DATETYPE_MAP.put( Metacard.MODIFIED, "updated" );
        DATETYPE_MAP.put( SearchConstants.POSTED, "posted" );
        DATETYPE_MAP.put( SearchConstants.INFO_CUT_OFF, "infoCutOff" );
        DATETYPE_MAP.put( SearchConstants.VALID_TIL, "validTil" );
        DATETYPE_MAP.put( SearchConstants.TEMPORAL_COVERAGE, "temporalCoverage" );
        DATETYPE_MAP.put( Metacard.EFFECTIVE, "effective" );

        SORTKEYS_MAP.put( Metacard.TITLE, "entry/title" );
        SORTKEYS_MAP.put( Metacard.MODIFIED, "entry/date" );
        SORTKEYS_MAP.put( Result.RELEVANCE, "score" );

    }

    public StrictFilterDelegate( boolean strictMode, double defaultRadiusforNN, FilterConfig config ) {
        super( defaultRadiusforNN, config );
        this.strictMode = strictMode;
    }

    @Override
    public Map<String, String> handleAnd( List<Map<String, String>> operands ) {
        Map<String, String> masterFilter = operands.get( 0 );
        if ( operands.size() >= 2 ) {
            for ( int i = 1; i < operands.size(); i++ ) {
                String masterKeywords = masterFilter.get( SearchConstants.KEYWORD_PARAMETER );

                Map<String, String> andedFilter = operands.get( i );
                String keyword = andedFilter.get( SearchConstants.KEYWORD_PARAMETER );
                if ( keyword != null ) {
                    if ( keyword.startsWith( "NOT " ) ) {
                        masterFilter.put( SearchConstants.KEYWORD_PARAMETER, masterKeywords == null ? keyword : "(" + masterKeywords + " " + keyword + ")" );
                    } else {
                        masterFilter.put( SearchConstants.KEYWORD_PARAMETER, masterKeywords == null ? keyword : "(" + masterKeywords + " AND " + keyword + ")" );
                    }

                }

                String caseSensitive = andedFilter.get( SearchConstants.CASESENSITIVE_PARAMETER );
                if ( caseSensitive != null && (caseSensitive.equalsIgnoreCase( "true" ) || caseSensitive.equals( SearchConstants.TRUE_STRING )) ) {
                    masterFilter.put( SearchConstants.CASESENSITIVE_PARAMETER, SearchConstants.TRUE_STRING );
                }
                String fuzzyString = andedFilter.get( SearchConstants.FUZZY_PARAMETER );
                if ( fuzzyString != null && (fuzzyString.equalsIgnoreCase( "true" ) || fuzzyString.equals( SearchConstants.TRUE_STRING )) ) {
                    masterFilter.put( SearchConstants.FUZZY_PARAMETER, SearchConstants.TRUE_STRING );
                }

                // Now add in the Content Types if they exist
                String masterContentType = masterFilter.get( Metacard.CONTENT_TYPE );
                String andedContentType = andedFilter.get( Metacard.CONTENT_TYPE );
                String andedVersion = andedFilter.get( Metacard.CONTENT_TYPE_VERSION );
                if ( andedContentType != null ) {
                    andedFilter.remove( Metacard.CONTENT_TYPE );
                    masterContentType = masterContentType == null ? andedContentType + ":" : masterContentType + "," + andedContentType + ":";
                    masterFilter.put( Metacard.CONTENT_TYPE, masterContentType );
                }
                if ( andedVersion != null ) {
                    andedFilter.remove( Metacard.CONTENT_TYPE_VERSION );
                    if ( masterContentType == null || masterContentType.endsWith( "," ) ) {
                        masterContentType = "*:" + andedVersion;
                    } else if ( masterContentType.endsWith( ":" ) ) {
                        masterContentType += andedVersion + ",";
                    } else {
                        masterContentType += ":" + andedVersion + ",";
                    }
                    masterFilter.put( Metacard.CONTENT_TYPE, masterContentType );
                }
                masterFilter = combineFilters( masterFilter, andedFilter );
            }
        }
        return masterFilter;
    }

    @Override
    public Map<String, String> handleOr( List<Map<String, String>> operands ) {
        Map<String, String> masterFilter = operands.get( 0 );
        if ( operands.size() >= 2 ) {
            for ( int i = 1; i < operands.size(); i++ ) {

                String masterKeywords = masterFilter.get( SearchConstants.KEYWORD_PARAMETER );

                Map<String, String> andedFilter = operands.get( i );
                String keyword = andedFilter.get( SearchConstants.KEYWORD_PARAMETER );
                if ( keyword != null ) {
                    masterFilter.put( SearchConstants.KEYWORD_PARAMETER, masterKeywords == null ? keyword : "(" + masterKeywords + " OR " + keyword + ")" );
                }

                String caseSensitive = andedFilter.get( SearchConstants.CASESENSITIVE_PARAMETER );
                if ( caseSensitive != null && (caseSensitive.equalsIgnoreCase( "true" ) || caseSensitive.equals( SearchConstants.TRUE_STRING )) ) {
                    masterFilter.put( SearchConstants.CASESENSITIVE_PARAMETER, SearchConstants.TRUE_STRING );
                }
                String fuzzy = andedFilter.get( SearchConstants.FUZZY_PARAMETER );
                if ( fuzzy != null && (fuzzy.equalsIgnoreCase( "true" ) || fuzzy.equals( SearchConstants.TRUE_STRING )) ) {
                    masterFilter.put( SearchConstants.FUZZY_PARAMETER, SearchConstants.TRUE_STRING );
                }

                // Now add in the Content Types if they exist
                String masterContentType = masterFilter.get( Metacard.CONTENT_TYPE );
                String andedContentType = andedFilter.get( Metacard.CONTENT_TYPE );
                String andedVersion = andedFilter.get( Metacard.CONTENT_TYPE_VERSION );
                if ( andedContentType != null ) {
                    andedFilter.remove( Metacard.CONTENT_TYPE );
                    masterContentType = masterContentType == null ? andedContentType : masterContentType + andedContentType;
                    masterFilter.put( Metacard.CONTENT_TYPE, masterContentType );
                }
                masterFilter = combineFilters( masterFilter, andedFilter );
            }
        }
        return masterFilter;
    }

    @Override
    public Map<String, String> handleNot( Map<String, String> operand ) {
        Map<String, String> filterContainer = new HashMap<String, String>();
        String keyword = operand.get( SearchConstants.KEYWORD_PARAMETER );
        if ( keyword != null ) {
            filterContainer.put( SearchConstants.KEYWORD_PARAMETER, "NOT " + keyword );
        }

        return filterContainer;
    }

    @Override
    public Map<String, String> handlePropertyLike( String propertyName, String pattern, StringFilterOptions options ) {
        return handlePropertyEqualToString( propertyName, pattern, options );
    }

    @Override
    public Map<String, String> handlePropertyEqualToString( String propertyName, String literal, StringFilterOptions options ) {
        Map<String, String> filterContainer = new HashMap<String, String>();
        if ( handleKeyword( propertyName, literal, filterContainer ) ) {
            if ( StringFilterOptions.CASE_SENSITIVE.equals( options ) ) {
                filterContainer.put( SearchConstants.CASESENSITIVE_PARAMETER, SearchConstants.TRUE_STRING );
            } else if ( StringFilterOptions.FUZZY.equals( options ) ) {
                filterContainer.put( SearchConstants.FUZZY_PARAMETER, SearchConstants.TRUE_STRING );
            }
        } else {
            // The geo:uid parameter is used to uniquely find an entry so we must map the Metacard.ID to geo:uid
            if ( Metacard.ID.equals( propertyName ) ) {
                filterContainer.put( SearchConstants.UID_PARAMETER, literal );
            } else {
                filterContainer.put( propertyName, literal );
            }
        }

        return filterContainer;
    }

    @Override
    public Map<String, String> handlePropertyIsEqualToNumber( String propertyName, double literal ) {
        Map<String, String> filterContainer = new HashMap<String, String>();
        filterContainer.put( propertyName, String.valueOf( literal ) );
        return filterContainer;
    }

    @Override
    public Map<String, String> handlePropertyIsNotEqualToString( String propertyName, String literal, boolean isCaseSensitive ) {
        failIfStrictMode( "handlePropertyIsNotEqualToString" );
        Map<String, String> filterContainer = new HashMap<String, String>();
        return filterContainer;
    }

    @Override
    public Map<String, String> handlePropertyIsNotEqualToNumber( String propertyName, double literal ) {
        failIfStrictMode( "handlePropertyIsNotEqualToNumber" );
        Map<String, String> filterContainer = new HashMap<String, String>();
        return filterContainer;
    }

    @Override
    public Map<String, String> handlePropertyIsGreaterThanString( String propertyName, String literal, boolean inclusive ) {
        failIfStrictMode( "handlePropertyIsGreaterThanString" );
        Map<String, String> filterContainer = new HashMap<String, String>();
        return filterContainer;
    }

    @Override
    public Map<String, String> handlePropertyIsGreaterThanNumber( String propertyName, double literal, boolean inclusive ) {
        failIfStrictMode( "handlePropertyIsGreaterThanNumber" );
        Map<String, String> filterContainer = new HashMap<String, String>();
        return filterContainer;
    }

    @Override
    public Map<String, String> handlePropertyIsLessThanString( String propertyName, String literal, boolean inclusive ) {
        failIfStrictMode( "handlePropertyIsLessThanString" );
        Map<String, String> filterContainer = new HashMap<String, String>();
        return filterContainer;
    }

    @Override
    public Map<String, String> handlePropertyIsLessThanNumber( String propertyName, double literal, boolean inclusive ) {
        failIfStrictMode( "handlePropertyIsLessThanNumber" );
        Map<String, String> filterContainer = new HashMap<String, String>();
        return filterContainer;
    }

    @Override
    public Map<String, String> handlePropertyBetweenString( String propertyName, String lowerBoundary, String upperBoundary ) {
        failIfStrictMode( "handlePropertyBetweenString" );
        Map<String, String> filterContainer = new HashMap<String, String>();
        return filterContainer;
    }

    @Override
    public Map<String, String> handleNumericRange( String propertyName, double lowerBoundary, double upperBoundary ) {
        failIfStrictMode( "handleNumericRange" );
        Map<String, String> filterContainer = new HashMap<String, String>();
        return filterContainer;
    }

    @Override
    public Map<String, String> handleTimeDuring( String propertyName, Date start, Date end ) {
        String datetype = DATETYPE_MAP.get( propertyName );
        Map<String, String> filterContainer = new HashMap<String, String>();
        filterContainer.put( SearchConstants.STARTDATE_PARAMETER, DATE_FORMATTER.print( start.getTime() ) );
        filterContainer.put( SearchConstants.ENDDATE_PARAMETER, DATE_FORMATTER.print( end.getTime() ) );
        filterContainer.put( SearchConstants.DATETYPE_PARAMETER, datetype == null ? propertyName : datetype );
        return filterContainer;
    }

    @Override
    public Map<String, String> handleTimeAfter( String propertyName, Date start, boolean inclusive ) {
        String datetype = DATETYPE_MAP.get( propertyName );
        Map<String, String> filterContainer = new HashMap<String, String>();
        filterContainer.put( SearchConstants.STARTDATE_PARAMETER, DATE_FORMATTER.print( start.getTime() ) );
        filterContainer.put( SearchConstants.DATETYPE_PARAMETER, datetype == null ? propertyName : datetype );
        return filterContainer;
    }

    @Override
    public Map<String, String> handleTimeBefore( String propertyName, Date end, boolean inclusive ) {
        String datetype = DATETYPE_MAP.get( propertyName );
        Map<String, String> filterContainer = new HashMap<String, String>();
        filterContainer.put( SearchConstants.ENDDATE_PARAMETER, DATE_FORMATTER.print( end.getTime() ) );
        filterContainer.put( SearchConstants.DATETYPE_PARAMETER, datetype == null ? propertyName : datetype );
        return filterContainer;
    }

    @Override
    public Map<String, String> handleTimeNotDuring( String propertyName, Date start, Date end ) {
        failIfStrictMode( "handleTimeNotDuring" );
        Map<String, String> filterContainer = new HashMap<String, String>();
        return filterContainer;
    }

    @Override
    public Map<String, String> handleGeospatial( String propertyName, String wkt, GeospatialFilterOptions options ) {
        Map<String, String> filterContainer = new HashMap<String, String>();
        if ( GeospatialFilterOptions.BBOX.equals( options ) ) {
            try {
                WKTReader reader = new WKTReader( GEOMETRY_FACTORY );
                Envelope envelope = reader.read( wkt ).getEnvelopeInternal();
                filterContainer.put( SearchConstants.BOX_PARAMETER, envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX() + "," + envelope.getMaxY() );
            } catch ( ParseException e ) {
                throw new UnsupportedOperationException( "Bounding box parameter was not formated correctly" );
            }
        } else {
            if ( !GeospatialFilterOptions.INTERSECTS.equals( options ) && !GeospatialFilterOptions.WITHIN.equals( options ) ) {
                failIfStrictMode( "handleGeospatial" );
            }
            if ( isKnownGeometryProperty( propertyName ) ) {
                filterContainer.put( SearchConstants.GEOMETRY_PARAMETER, wkt );
                return filterContainer;
            }
            LOGGER.info( "Unsupported geospatial query sent in with wkt[{}], propertyName=[{}] and type=[{}]", wkt, propertyName, options );
            failIfStrictMode( "handleGeospatial" );
        }
        return filterContainer;
    }

    @Override
    public Map<String, String> handleGeospatialDistance( String propertyName, String wkt, double distance, GeospatialDistanceFilterOptions options ) {
        Map<String, String> filterContainer = new HashMap<String, String>();
        if ( options == null || options.equals( GeospatialDistanceFilterOptions.WITHIN ) ) {
            if ( isKnownGeometryProperty( propertyName ) ) {
                Point point = getPointFromWkt( wkt );
                if ( point != null ) {
                    filterContainer.put( SearchConstants.LATITUDE_PARAMETER, String.valueOf( point.getY() ) );
                    filterContainer.put( SearchConstants.LONGITUDE_PARAMETER, String.valueOf( point.getX() ) );
                    filterContainer.put( SearchConstants.RADIUS_PARAMETER, String.valueOf( distance ) );
                    return filterContainer;
                } else {
                    LOGGER.debug( "Could not translate WKT into point to use for point raduis query wkt=[{}]", wkt );
                }
            }
        }
        LOGGER.info( "Unsupported geospatial distance query sent in with wkt[{}], propertyName=[{}] and type=[{}]", wkt, propertyName, options );
        failIfStrictMode( "handleGeospatialDistance" );
        return filterContainer;
    }

    @Override
    public Map<String, String> handleXpath( String xpath, String literal, StringFilterOptions options ) {
        failIfStrictMode( "handleXpath" );

        if ( literal == null ) {
            literal = "";
        }
        Map<String, String> filterContainer = new HashMap<String, String>();
        filterContainer.put( SearchConstants.KEYWORD_PARAMETER, "{" + xpath + "}:" + literal );
        if ( StringFilterOptions.CASE_SENSITIVE.equals( options ) ) {
            filterContainer.put( SearchConstants.CASESENSITIVE_PARAMETER, SearchConstants.TRUE_STRING );
        }
        if ( StringFilterOptions.FUZZY.equals( options ) ) {
            filterContainer.put( SearchConstants.FUZZY_PARAMETER, SearchConstants.TRUE_STRING );
        }
        return filterContainer;
    }

    public void failIfStrictMode( String method ) {
        if ( strictMode ) {
            throw new UnsupportedOperationException( method + " not supported directly, so failing because in strictMode" );
        }
    }

    protected Point getPointFromWkt( String wkt ) {
        Point point = null;
        try {
            WKTReader wktReader = new WKTReader( GEOMETRY_FACTORY );
            Geometry geometry = wktReader.read( wkt );
            if ( geometry instanceof Point ) {
                point = (Point) geometry;
            } else {
                point = geometry.getCentroid();
                LOGGER.debug( "Using the centroid for the single point for the point raduis query for oriingal wkt=[{}]", wkt );
            }
        } catch ( ParseException e ) {
            LOGGER.error( "Could not properly parse the string into a WKT object=[" + wkt + "]", e );
        }
        return point;
    }

    protected boolean isKnownGeometryProperty( String propertyName ) {
        return Metacard.ANY_GEO.equals( propertyName ) || Metacard.GEOGRAPHY.equals( propertyName );
    }

    protected boolean handleKeyword( String propertyName, String keyword, Map<String, String> filterContainer ) {
        if ( Metacard.ANY_TEXT.equals( propertyName ) && keyword != null ) {
            keyword = keyword.trim();
            if ( keyword.contains( " " ) ) {
                keyword = "\"" + keyword + "\"";
            }
            filterContainer.put( SearchConstants.KEYWORD_PARAMETER, keyword );
            return true;
        }
        return false;
    }

    protected Map<String, String> combineFilters( Map<String, String> masterFilter, Map<String, String> andedFilter ) {
        Set<String> keys = andedFilter.keySet();
        for ( String key : keys ) {
            if ( !masterFilter.containsKey( key ) ) {
                masterFilter.put( key, andedFilter.get( key ) );
            }
        }
        return masterFilter;
    }

}
