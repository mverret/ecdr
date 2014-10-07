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
package net.di2e.ecdr.commons.query.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import net.di2e.ecdr.commons.query.GeospatialCriteria;
import net.di2e.ecdr.commons.query.PropertyCriteria;
import net.di2e.ecdr.commons.query.TemporalCriteria;
import net.di2e.ecdr.commons.query.TextualCriteria;
import net.di2e.ecdr.commons.query.GeospatialCriteria.SpatialOperator;
import net.di2e.ecdr.commons.query.PropertyCriteria.Operator;
import net.di2e.ecdr.commons.query.rest.parsers.QueryParser;
import net.di2e.ecdr.commons.query.util.keywordparser.ASTNode;
import net.di2e.ecdr.commons.query.util.keywordparser.KeywordTextParser;
import net.di2e.ecdr.commons.util.BrokerConstants;
import net.di2e.ecdr.commons.util.SearchConstants;

import org.apache.commons.lang.StringUtils;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.operation.Query;
import ddf.catalog.source.UnsupportedQueryException;

public class CDRQueryImpl implements Query {

    // public static final String INCLUDE_THUMBNAIL = "include-thumbnail";

    private static final Logger LOGGER = LoggerFactory.getLogger( CDRQueryImpl.class );

    private Filter queryFilter;
    private QueryParser queryParser;
    private StringBuilder humanReadableQueryBuilder = new StringBuilder();

    private Collection<String> sources = null;

    private boolean useDefaultSortIfNotSpecified = false;

    private boolean isStrictMode = false;
    private String responseFormat;
    private int startIndex;
    private int count;
    private long timeoutMilliseconds;
    private SortBy sortBy;

    private GeospatialCriteria geoCriteria;
    private TemporalCriteria temporalCriteria;
    private TextualCriteria textualCriteria;
    private List<PropertyCriteria> propertyCriteriaList;

    private String localSourceId = null;

    public CDRQueryImpl( FilterBuilder filterBuilder, MultivaluedMap<String, String> queryParameters, QueryParser parser, boolean useDefaultSort, String localSourceId )
            throws UnsupportedQueryException {
        queryParser = parser;
        this.localSourceId = localSourceId;
        this.useDefaultSortIfNotSpecified = useDefaultSort;

        createQuery( filterBuilder, queryParameters );
        humanReadableQueryBuilder.append( " " + SearchConstants.STRICTMODE_PARAMETER + "=[" + isStrictMode + "] " + SearchConstants.STARTINDEX_PARAMETER + "=[" + startIndex + "] "
                + SearchConstants.COUNT_PARAMETER + "=[" + count + "]" );
        humanReadableQueryBuilder.append( " " + SearchConstants.FORMAT_PARAMETER + "=[" + responseFormat + "]" );

        sources = queryParser.getSiteNames( queryParameters );
        if ( !sources.isEmpty() ) {
            humanReadableQueryBuilder.append( " " + BrokerConstants.SOURCE_PARAMETER + "=[" );
            StringUtils.join( sources, ", " );
            humanReadableQueryBuilder.append( "]" );
        }
    }

    @Override
    public boolean evaluate( Object object ) {
        return queryFilter.evaluate( object );
    }

    @Override
    public Object accept( FilterVisitor visitor, Object extraData ) {
        return queryFilter.accept( visitor, extraData );
    }

    @Override
    public int getPageSize() {
        return count;
    }

    @Override
    public SortBy getSortBy() {
        return sortBy;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public long getTimeoutMillis() {
        return timeoutMilliseconds;
    }

    @Override
    public boolean requestsTotalResultsCount() {
        return true;
    }

    protected boolean getStrictMode() {
        return isStrictMode;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public String getHumanReadableQuery() {
        return humanReadableQueryBuilder.toString();
    }

    public Collection<String> getSiteNames() {
        return sources;
    }

    protected void createQuery( FilterBuilder filterBuilder, MultivaluedMap<String, String> queryParameters ) throws UnsupportedQueryException {
        List<Filter> filters = new ArrayList<Filter>();
        if ( !queryParser.isValidQuery( queryParameters, localSourceId ) ) {
            throw new UnsupportedQueryException( "Invalid query parameters passed in" );
        }

        isStrictMode = queryParser.isStrictMode( queryParameters );
        responseFormat = queryParser.getResponseFormat( queryParameters );
        startIndex = queryParser.getStartIndex( queryParameters );
        count = queryParser.getCount( queryParameters );
        timeoutMilliseconds = queryParser.getTimeoutMilliseconds( queryParameters );
        sortBy = queryParser.getSortBy( queryParameters );

        // keyword parameters
        textualCriteria = queryParser.getTextualCriteria( queryParameters );
        if ( textualCriteria != null ) {
            LOGGER.debug( "Attempting to create a Contextual filter with params keywords=[{}], isCaseSensitive=[{}], strictMode=[{}]", textualCriteria.getKeywords(),
                    textualCriteria.isCaseSensitive(), isStrictMode );
            Filter filter = getContextualFilter( filterBuilder, textualCriteria.getKeywords(), textualCriteria.isCaseSensitive(), isStrictMode );
            addFilter( filters, filter );
            if ( useDefaultSortIfNotSpecified && sortBy == null ) {
                sortBy = new SortByImpl( Result.RELEVANCE, SortOrder.DESCENDING );
            }
        }

        // Geospatial query parameters
        geoCriteria = queryParser.getGeospatialCriteria( queryParameters );
        if ( geoCriteria != null ) {
            LOGGER.debug( "Attempting to create a Geospatial filter with params radius=[{}], latitude=[{}], longitude=[{}], bbox=[{}] and geometry=[{}]", geoCriteria.getRadius(),
                    geoCriteria.getLatitude(), geoCriteria.getLongitude(), geoCriteria.getBBoxWKT(), geoCriteria.getGeometryWKT() );
            Filter filter = getGeoFilter( filterBuilder, geoCriteria.getRadius(), geoCriteria.getLatitude(), geoCriteria.getLongitude(), geoCriteria.getBBoxWKT(), geoCriteria.getGeometryWKT(),
                    geoCriteria.getSpatialOperator() );
            addFilter( filters, filter );
            if ( useDefaultSortIfNotSpecified && sortBy == null ) {
                sortBy = new SortByImpl( Result.DISTANCE, SortOrder.ASCENDING );
            }
        }

        // Temporal Criteria
        temporalCriteria = queryParser.getTemporalCriteria( queryParameters );
        if ( temporalCriteria != null ) {
            LOGGER.debug( "Attempting to create a Temporal filter with params startDate=[{}], endDate=[{}], dateType=[{}]", temporalCriteria.getStartDate(), temporalCriteria.getEndDate(),
                    temporalCriteria.getDateType() );
            Filter filter = getTemporalFilter( filterBuilder, temporalCriteria.getStartDate(), temporalCriteria.getEndDate(), temporalCriteria.getDateType() );
            addFilter( filters, filter );
        }

        // Property Criteria
        propertyCriteriaList = queryParser.getPropertyCriteria( queryParameters );
        if ( propertyCriteriaList != null && !propertyCriteriaList.isEmpty() ) {
            for ( PropertyCriteria propCriteria : propertyCriteriaList ) {
                LOGGER.debug( "Attempting to create a Property filter with params property=[{}], value=[{}], operator=[{}]", propCriteria.getProperty(), propCriteria.getValue(),
                        propCriteria.getOperator() );
                Filter filter = getPropertyFilter( filterBuilder, propCriteria.getProperty(), propCriteria.getValue(), propCriteria.getOperator() );
                addFilter( filters, filter );
            }
        }

        if ( filters.isEmpty() ) {
            throw new UnsupportedQueryException( "Could not create any valid filters from the provided query parameters" );
        }

        // Default to Effective time based sorting if desired to default and no sort order specified
        if ( useDefaultSortIfNotSpecified && sortBy == null ) {
            sortBy = new SortByImpl( Result.TEMPORAL, SortOrder.DESCENDING );
        }

        if ( sortBy != null ) {
            humanReadableQueryBuilder.append( " " + SearchConstants.SORTKEYS_PARAMETER + "=[" + sortBy.getPropertyName().getPropertyName() + " " + sortBy.getSortOrder().name() + "]" );
        }

        queryFilter = filterBuilder.allOf( filters );
    }

    protected void addFilter( List<Filter> filters, Filter filter ) {
        if ( filter != null ) {
            LOGGER.debug( "Filter was not null, and will be added to the list of valid filters" );
            filters.add( filter );
        } else {
            LOGGER.debug( "Filter was null, not adding to the Filter list" );
        }
    }

    protected Filter getGeoFilter( FilterBuilder filterBuilder, Double radius, Double latitude, Double longitude, String boxWKT, String geometry, SpatialOperator operator ) {
        Filter filter = null;

        if ( latitude != null && longitude != null && radius != null ) {
            String wkt = WKTWriter.toPoint( new Coordinate( longitude, latitude ) );
            filter = filterBuilder.attribute( Metacard.ANY_GEO ).withinBuffer().wkt( wkt, radius );
            humanReadableQueryBuilder.append( " lat=[" + latitude + "] lon=[" + longitude + "] radius=[" + radius + "]" );
        } else if ( boxWKT != null ) {
            if ( SpatialOperator.Contains.equals( operator ) ) {
                filter = filterBuilder.attribute( Metacard.ANY_GEO ).within().wkt( boxWKT );
                humanReadableQueryBuilder.append( " box=[" + boxWKT + "] spatialOp=[contains]" );
            } else {
                filter = filterBuilder.attribute( Metacard.ANY_GEO ).intersecting().wkt( boxWKT );
                humanReadableQueryBuilder.append( " box=[" + boxWKT + "]" );
            }
        } else if ( geometry != null ) {
            if ( SpatialOperator.Contains.equals( operator ) ) {
                filter = filterBuilder.attribute( Metacard.ANY_GEO ).within().wkt( geometry );
                humanReadableQueryBuilder.append( " " + SearchConstants.GEOMETRY_PARAMETER + "=[" + geometry + "] spatialOp=[contains]" );
            } else {
                filter = filterBuilder.attribute( Metacard.ANY_GEO ).intersecting().wkt( geometry );
                humanReadableQueryBuilder.append( " " + SearchConstants.GEOMETRY_PARAMETER + "=[" + geometry + "]" );
            }
        }
        return filter;
    }

    protected Filter getTemporalFilter( FilterBuilder filterBuilder, Date startDate, Date endDate, String type ) throws UnsupportedQueryException {
        Filter filter = null;
        if ( startDate != null || endDate != null ) {
            if ( startDate != null && endDate != null ) {
                if ( startDate.after( endDate ) ) {
                    throw new UnsupportedQueryException( "Start date value [" + startDate + "] cannot be after endDate [" + endDate + "]" );
                }
                filter = filterBuilder.attribute( type ).during().dates( startDate, endDate );
                humanReadableQueryBuilder.append( " " + SearchConstants.STARTDATE_PARAMETER + "=[" + startDate + "] " + SearchConstants.ENDDATE_PARAMETER + "=[" + endDate + "] "
                        + SearchConstants.DATETYPE_PARAMETER + "=[" + type + "]" );
            } else if ( startDate != null ) {
                filter = filterBuilder.attribute( type ).after().date( startDate );
                humanReadableQueryBuilder.append( " " + SearchConstants.STARTDATE_PARAMETER + "=[" + startDate + "] " + SearchConstants.DATETYPE_PARAMETER + "=[" + type + "]" );
            } else if ( endDate != null ) {
                filter = filterBuilder.attribute( type ).before().date( endDate );
                humanReadableQueryBuilder.append( " " + SearchConstants.ENDDATE_PARAMETER + "=[" + endDate + "] " + SearchConstants.DATETYPE_PARAMETER + "=[" + type + "]" );
            }
        }

        return filter;
    }

    protected Filter getPropertyFilter( FilterBuilder filterBuilder, String property, String value, Operator operator ) {
        Filter filter = null;
        if ( property != null || value != null && operator != null ) {

            if ( property.equals( Metacard.CONTENT_TYPE ) ) {
                filter = getContentTypeFilter( filterBuilder, value );
                humanReadableQueryBuilder.append( " " + property + "-like[" + value + "] " );
            } else {
                if ( Operator.EQUALS.equals( operator ) ) {
                    filter = filterBuilder.attribute( property ).equalTo().text( value );
                    humanReadableQueryBuilder.append( " " + property + "=[" + value + "] " );
                } else if ( Operator.LIKE.equals( operator ) ) {
                    filter = filterBuilder.attribute( property ).like().text( value );
                    humanReadableQueryBuilder.append( " " + property + "-like[" + value + "] " );
                }
            }
        }
        return filter;
    }

    private Filter getContentTypeFilter( FilterBuilder filterBuilder, String value ) {
        List<Filter> filterList = new ArrayList<Filter>();
        String[] contentTypes = value.split( "," );
        for ( String contentType : contentTypes ) {
            String[] typeAndVersion = contentType.split( ":" );
            String type = typeAndVersion[0];
            if ( typeAndVersion.length == 1 ) {
                filterList.add( filterBuilder.attribute( Metacard.CONTENT_TYPE ).like().text( type ) );
            } else {
                List<Filter> typeVersionPairs = new ArrayList<Filter>();
                String[] versions = typeAndVersion[1].split( "\\|" );
                for ( String version : versions ) {
                    Filter typeFilter = filterBuilder.attribute( Metacard.CONTENT_TYPE ).like().text( type );
                    Filter versionFilter = filterBuilder.attribute( Metacard.CONTENT_TYPE_VERSION ).like().text( version );
                    typeVersionPairs.add( filterBuilder.allOf( typeFilter, versionFilter ) );
                }

                // Check if we had any type/version pairs and 'OR' them together.
                if ( !typeVersionPairs.isEmpty() ) {
                    filterList.add( filterBuilder.anyOf( typeVersionPairs ) );
                }
            }
        }

        return filterBuilder.anyOf( filterList );
    }

    protected Filter getContextualFilter( FilterBuilder filterBuilder, String keywords, boolean caseSensitive, boolean strictMode ) throws UnsupportedQueryException {
        Filter filter = null;
        if ( keywords != null ) {
            KeywordTextParser keywordParser = Parboiled.createParser( KeywordTextParser.class );
            ParseRunner<ASTNode> runner = strictMode ? new ReportingParseRunner<ASTNode>( keywordParser.inputPhrase() ) : new RecoveringParseRunner<ASTNode>( keywordParser.inputPhrase() );
            ParsingResult<ASTNode> parsingResult = runner.run( keywords );

            if ( !parsingResult.hasErrors() ) {
                try {
                    filter = getFilterFromASTNode( filterBuilder, parsingResult.resultValue, caseSensitive, strictMode );
                } catch ( IllegalStateException e ) {
                    throw new UnsupportedQueryException( "searchTerms parameter [" + keywords + "] was invalid and resulted in the error: " + e.getMessage() );
                }
            } else {
                throw new UnsupportedQueryException( "searchTerms parameter [" + keywords + "] was invalid and resulted in the error: " + parsingResult.parseErrors.get( 0 ).getErrorMessage() );
            }
            humanReadableQueryBuilder.append( " " + SearchConstants.KEYWORD_PARAMETER + "=[" + keywords + "] " + SearchConstants.CASESENSITIVE_PARAMETER + "=[" + caseSensitive + "]" );
        }
        return filter;
    }

    protected Filter getFilterFromASTNode( FilterBuilder filterBuilder, ASTNode astNode, boolean caseSensitive, boolean fuzzy ) {

        if ( astNode.isKeyword() ) {
            String keyword = astNode.getKeyword();
            // this means it is an Text Path
            if ( keyword.startsWith( "{" ) && keyword.contains( "}:" ) ) {
                int endXpath = keyword.lastIndexOf( "}:" );
                String xpath = keyword.substring( 1, endXpath );
                String literal = keyword.substring( endXpath + 2 );
                if ( literal.trim().isEmpty() ) {
                    return filterBuilder.xpath( xpath ).exists();
                } else {
                    if ( fuzzy ) {
                        return filterBuilder.xpath( xpath ).like().fuzzyText( literal );
                    } else if ( caseSensitive ) {
                        return filterBuilder.xpath( xpath ).like().caseSensitiveText( literal );
                    } else {
                        return filterBuilder.xpath( xpath ).like().text( literal );
                    }
                }
            } else {
                // If case sensitive then don't set Fuzzy
                // If not case sensitive then set fuzzy based on fuzzy boolean
                return caseSensitive ? filterBuilder.attribute( Metacard.ANY_TEXT ).like().caseSensitiveText( astNode.getKeyword() ) : (fuzzy ? filterBuilder.attribute( Metacard.ANY_TEXT ).like()
                        .fuzzyText( astNode.getKeyword() ) : filterBuilder.attribute( Metacard.ANY_TEXT ).like().fuzzyText( astNode.getKeyword() ));
            }
        } else if ( astNode.isOperator() ) {
            switch ( astNode.getOperator() ) {
            case AND:
                return filterBuilder
                        .allOf( getFilterFromASTNode( filterBuilder, astNode.left(), caseSensitive, fuzzy ), getFilterFromASTNode( filterBuilder, astNode.right(), caseSensitive, fuzzy ) );
            case OR:

                return filterBuilder
                        .anyOf( getFilterFromASTNode( filterBuilder, astNode.left(), caseSensitive, fuzzy ), getFilterFromASTNode( filterBuilder, astNode.right(), caseSensitive, fuzzy ) );
            case NOT: // since NOT really means AND NOT
                return filterBuilder.allOf( getFilterFromASTNode( filterBuilder, astNode.left(), caseSensitive, fuzzy ),
                        filterBuilder.not( getFilterFromASTNode( filterBuilder, astNode.right(), caseSensitive, fuzzy ) ) );
            default:
                throw new IllegalStateException( "Unable to generate Filter from invalid OperatorASTNode." );
            }
        }

        throw new IllegalStateException( "Unable to generate Filter from ASTNode. Found invalid ASTNode in the tree" );
    }

}
