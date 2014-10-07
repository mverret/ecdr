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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.filter.FilterDelegate;

public abstract class AbstractFilterDelegate<T> extends FilterDelegate<T> {

    public enum StringFilterOptions {
        CASE_SENSITIVE, FUZZY
    }

    public enum GeospatialFilterOptions {
        CONTAINS, CROSSES, DISJOINT, INTERSECTS, WITHIN, TOUCHES, OVERLAPS
    }

    public enum GeospatialDistanceFilterOptions {
        WITHIN, BEYOND
    }

    private static final Logger LOGGER = LoggerFactory.getLogger( StrictFilterDelegate.class );
    private double defaultRadiusforNN = 0;

    public AbstractFilterDelegate( double defaultRadiusforNN ) {
        this.defaultRadiusforNN = defaultRadiusforNN;
    }

    public abstract T handlePropertyLike( String propertyName, String pattern, StringFilterOptions options );

    public abstract T handlePropertyEqualToString( String propertyName, String literal, StringFilterOptions options );

    public abstract T handlePropertyIsEqualToNumber( String propertyName, double literal );

    public abstract T handlePropertyIsNotEqualToString( String propertyName, String literal, boolean isCaseSensitive );

    public abstract T handlePropertyIsNotEqualToNumber( String propertyName, double literal );

    public abstract T handlePropertyIsGreaterThanString( String propertyName, String literal, boolean inclusive );

    public abstract T handlePropertyIsGreaterThanNumber( String propertyName, double literal, boolean inclusive );

    public abstract T handlePropertyIsLessThanString( String propertyName, String literal, boolean inclusive );

    public abstract T handlePropertyIsLessThanNumber( String propertyName, double literal, boolean inclusive );

    public abstract T handlePropertyBetweenString( String propertyName, String lowerBoundary, String upperBoundary );

    public abstract T handleNumericRange( String propertyName, double lowerBoundary, double upperBoundary );

    public abstract T handleTimeDuring( String propertyName, Date start, Date end );

    public abstract T handleTimeAfter( String propertyName, Date start, boolean inclusive );

    public abstract T handleTimeBefore( String propertyName, Date end, boolean inclusive );

    public abstract T handleTimeNotDuring( String propertyName, Date start, Date end );

    public abstract T handleGeospatial( String propertyName, String wkt, GeospatialFilterOptions options );

    public abstract T handleGeospatialDistance( String propertyName, String wkt, double distance, GeospatialDistanceFilterOptions options );

    public abstract T handleXpath( String xpath, String literal, StringFilterOptions options );

    public abstract T handleAnd( List<T> operands );

    public abstract T handleOr( List<T> operands );

    public abstract T handleNot( T operand );

    @Override
    public T and( List<T> operands ) {
        logEntry( "and", operands.toString(), null );
        return handleAnd( operands );
    }

    @Override
    public T or( List<T> operands ) {
        logEntry( "or", operands.toString(), null );
        return handleOr( operands );
    }

    @Override
    public T not( T operand ) {
        logEntry( "not", operand.toString(), null );
        return handleNot( operand );
    }

    @Override
    public T include() {
        logEntry( "include", null, null );
        return super.include();
    }

    @Override
    public T exclude() {
        logEntry( "exclude", null, null );
        return super.exclude();
    }

    @Override
    public T propertyIsEqualTo( String propertyName, String literal, boolean isCaseSensitive ) {
        logEntry( "propertyIsEqualTo", propertyName, literal, isCaseSensitive );
        return handlePropertyEqualToString( propertyName, literal, isCaseSensitive ? StringFilterOptions.CASE_SENSITIVE : null );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, Date literal ) {
        logEntry( "propertyIsEqualTo_Date", propertyName, literal );
        return handleTimeDuring( propertyName, literal, literal );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, Date startDate, Date endDate ) {
        logEntry( "propertyIsEqualTo_Dates", propertyName, startDate, endDate );
        return handleTimeDuring( propertyName, startDate, endDate );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, int literal ) {
        logEntry( "propertyIsEqualTo_int", propertyName, literal );
        return handlePropertyIsEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, short literal ) {
        logEntry( "propertyIsEqualTo_short", propertyName, literal );
        return handlePropertyIsEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, long literal ) {
        logEntry( "propertyIsEqualTo_long", propertyName, literal );
        return handlePropertyIsEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, float literal ) {
        logEntry( "propertyIsEqualTo_float", propertyName, literal );
        return handlePropertyIsEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, double literal ) {
        logEntry( "propertyIsEqualTo_double", propertyName, literal );
        return handlePropertyIsEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, boolean literal ) {
        logEntry( "propertyIsEqualTo_boolean", propertyName, literal );
        return handlePropertyEqualToString( propertyName, String.valueOf( literal ), null );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, byte[] literal ) {
        logEntry( "propertyIsEqualTo_Bytes", propertyName, literal );
        return super.propertyIsEqualTo( propertyName, literal );
    }

    @Override
    public T propertyIsEqualTo( String propertyName, Object literal ) {
        logEntry( "propertyIsEqualTo_Object", propertyName, literal );
        return super.propertyIsEqualTo( propertyName, literal );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, String literal, boolean isCaseSensitive ) {
        logEntry( "propertyIsNotEqualTo", propertyName, literal, isCaseSensitive );
        return handlePropertyIsNotEqualToString( propertyName, literal, isCaseSensitive );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, Date literal ) {
        logEntry( "propertyIsEqualTo_Date", propertyName, literal );
        return super.propertyIsNotEqualTo( propertyName, literal );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, Date startDate, Date endDate ) {
        logEntry( "propertyIsNotEqualTo_Dates", propertyName, startDate, endDate );
        return super.propertyIsNotEqualTo( propertyName, startDate, endDate );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, int literal ) {
        logEntry( "propertyIsNotEqualTo_int", propertyName, literal );
        return handlePropertyIsNotEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, short literal ) {
        logEntry( "propertyIsNotEqualTo_short", propertyName, literal );
        return handlePropertyIsNotEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, long literal ) {
        logEntry( "propertyIsNotEqualTo_long", propertyName, literal );
        return handlePropertyIsNotEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, float literal ) {
        logEntry( "propertyIsNotEqualTo_float", propertyName, literal );
        return handlePropertyIsNotEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, double literal ) {
        logEntry( "propertyIsNotEqualTo_double", propertyName, literal );
        return handlePropertyIsNotEqualToNumber( propertyName, literal );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, boolean literal ) {
        logEntry( "propertyIsNotEqualTo_boolean", propertyName, literal );
        return handlePropertyIsNotEqualToString( propertyName, String.valueOf( literal ), false );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, byte[] literal ) {
        logEntry( "propertyIsNotEqualTo_Bytes", propertyName, literal );
        return super.propertyIsNotEqualTo( propertyName, literal );
    }

    @Override
    public T propertyIsNotEqualTo( String propertyName, Object literal ) {
        logEntry( "propertyIsNotEqualTo", propertyName, literal );
        return super.propertyIsNotEqualTo( propertyName, literal );
    }

    @Override
    public T propertyIsGreaterThan( String propertyName, String literal ) {
        logEntry( "propertyIsGreaterThan", propertyName, literal );
        return handlePropertyIsGreaterThanString( propertyName, literal, false );
    }

    @Override
    public T propertyIsGreaterThan( String propertyName, Date literal ) {
        logEntry( "propertyIsGreaterThan_Date", propertyName, literal );
        return handleTimeAfter( propertyName, literal, false );
    }

    @Override
    public T propertyIsGreaterThan( String propertyName, int literal ) {
        logEntry( "propertyIsGreaterThan_int", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, false );
    }

    @Override
    public T propertyIsGreaterThan( String propertyName, short literal ) {
        logEntry( "propertyIsGreaterThan_short", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, false );
    }

    @Override
    public T propertyIsGreaterThan( String propertyName, long literal ) {
        logEntry( "propertyIsGreaterThan_long", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, false );
    }

    @Override
    public T propertyIsGreaterThan( String propertyName, float literal ) {
        logEntry( "propertyIsGreaterThan_float", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, false );
    }

    @Override
    public T propertyIsGreaterThan( String propertyName, double literal ) {
        logEntry( "propertyIsGreaterThan_double", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, false );
    }

    @Override
    public T propertyIsGreaterThan( String propertyName, Object literal ) {
        logEntry( "propertyIsGreaterThan_Object", propertyName, literal );
        return super.propertyIsGreaterThan( propertyName, literal );
    }

    @Override
    public T propertyIsGreaterThanOrEqualTo( String propertyName, String literal ) {
        logEntry( "propertyIsGreaterThanOrEqualTo", propertyName, literal );
        return handlePropertyIsGreaterThanString( propertyName, literal, true );
    }

    @Override
    public T propertyIsGreaterThanOrEqualTo( String propertyName, Date literal ) {
        logEntry( "propertyIsGreaterThanOrEqualTo_Date", propertyName, literal );
        return handleTimeAfter( propertyName, literal, true );
    }

    @Override
    public T propertyIsGreaterThanOrEqualTo( String propertyName, int literal ) {
        logEntry( "propertyIsGreaterThanOrEqualTo_int", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsGreaterThanOrEqualTo( String propertyName, short literal ) {
        logEntry( "propertyIsGreaterThanOrEqualTo_short", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsGreaterThanOrEqualTo( String propertyName, long literal ) {
        logEntry( "propertyIsGreaterThanOrEqualTo_long", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsGreaterThanOrEqualTo( String propertyName, float literal ) {
        logEntry( "propertyIsGreaterThanOrEqualTo_float", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsGreaterThanOrEqualTo( String propertyName, double literal ) {
        logEntry( "propertyIsGreaterThanOrEqualTo_double", propertyName, literal );
        return handlePropertyIsGreaterThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsGreaterThanOrEqualTo( String propertyName, Object literal ) {
        logEntry( "propertyIsGreaterThanOrEqualTo_Object", propertyName, literal );
        return super.propertyIsGreaterThanOrEqualTo( propertyName, literal );
    }

    @Override
    public T propertyIsLessThan( String propertyName, String literal ) {
        logEntry( "propertyIsLessThan", propertyName, literal );
        return handlePropertyIsLessThanString( propertyName, literal, false );
    }

    @Override
    public T propertyIsLessThan( String propertyName, Date literal ) {
        logEntry( "propertyIsLessThan_Date", propertyName, literal );
        return handleTimeBefore( propertyName, literal, false );
    }

    @Override
    public T propertyIsLessThan( String propertyName, int literal ) {
        logEntry( "propertyIsLessThan_int", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, (float) literal, false );
    }

    @Override
    public T propertyIsLessThan( String propertyName, short literal ) {
        logEntry( "propertyIsLessThan_short", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, literal, false );
    }

    @Override
    public T propertyIsLessThan( String propertyName, long literal ) {
        logEntry( "propertyIsLessThan_long", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, literal, false );
    }

    @Override
    public T propertyIsLessThan( String propertyName, float literal ) {
        logEntry( "propertyIsLessThan_float", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, literal, false );
    }

    @Override
    public T propertyIsLessThan( String propertyName, double literal ) {
        logEntry( "propertyIsLessThan_double", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, literal, false );
    }

    @Override
    public T propertyIsLessThan( String propertyName, Object literal ) {
        logEntry( "propertyIsLessThan_Object", propertyName, literal );
        return super.propertyIsLessThan( propertyName, literal );
    }

    @Override
    public T propertyIsLessThanOrEqualTo( String propertyName, String literal ) {
        logEntry( "propertyIsLessThanOrEqualTo", propertyName, literal );
        return handlePropertyIsLessThanString( propertyName, literal, true );
    }

    @Override
    public T propertyIsLessThanOrEqualTo( String propertyName, Date literal ) {
        logEntry( "propertyIsLessThanOrEqualTo_Date", propertyName, literal );
        return handleTimeBefore( propertyName, literal, true );
    }

    @Override
    public T propertyIsLessThanOrEqualTo( String propertyName, int literal ) {
        logEntry( "propertyIsLessThanOrEqualTo_int", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsLessThanOrEqualTo( String propertyName, short literal ) {
        logEntry( "propertyIsLessThanOrEqualTo_short", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsLessThanOrEqualTo( String propertyName, long literal ) {
        logEntry( "propertyIsLessThanOrEqualTo_long", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsLessThanOrEqualTo( String propertyName, float literal ) {
        logEntry( "propertyIsLessThanOrEqualTo_float", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsLessThanOrEqualTo( String propertyName, double literal ) {
        logEntry( "propertyIsLessThanOrEqualTo_double", propertyName, literal );
        return handlePropertyIsLessThanNumber( propertyName, literal, true );
    }

    @Override
    public T propertyIsLessThanOrEqualTo( String propertyName, Object literal ) {
        logEntry( "propertyIsLessThanOrEqualTo_Object", propertyName, literal );
        return super.propertyIsLessThanOrEqualTo( propertyName, literal );
    }

    @Override
    public T propertyIsBetween( String propertyName, String lowerBoundary, String upperBoundary ) {
        logEntry( "propertyIsBetween", propertyName, lowerBoundary, upperBoundary );
        return handlePropertyBetweenString( propertyName, lowerBoundary, upperBoundary );
    }

    @Override
    public T propertyIsBetween( String propertyName, Date lowerBoundary, Date upperBoundary ) {
        logEntry( "propertyIsBetween_Dates", propertyName, lowerBoundary, upperBoundary );
        return handleTimeDuring( propertyName, lowerBoundary, upperBoundary );
    }

    @Override
    public T propertyIsBetween( String propertyName, int lowerBoundary, int upperBoundary ) {
        logEntry( "propertyIsBetween_int", propertyName, lowerBoundary, upperBoundary );
        return handleNumericRange( propertyName, lowerBoundary, upperBoundary );
    }

    @Override
    public T propertyIsBetween( String propertyName, short lowerBoundary, short upperBoundary ) {
        logEntry( "propertyIsBetween_short", propertyName, lowerBoundary, upperBoundary );
        return handleNumericRange( propertyName, lowerBoundary, upperBoundary );
    }

    @Override
    public T propertyIsBetween( String propertyName, long lowerBoundary, long upperBoundary ) {
        logEntry( "propertyIsBetween_long", propertyName, lowerBoundary, upperBoundary );
        return handleNumericRange( propertyName, lowerBoundary, upperBoundary );
    }

    @Override
    public T propertyIsBetween( String propertyName, float lowerBoundary, float upperBoundary ) {
        logEntry( "propertyIsBetween_float", propertyName, lowerBoundary, upperBoundary );
        return handleNumericRange( propertyName, lowerBoundary, upperBoundary );
    }

    @Override
    public T propertyIsBetween( String propertyName, double lowerBoundary, double upperBoundary ) {
        logEntry( "propertyIsBetween_double", propertyName, lowerBoundary, upperBoundary );
        return handleNumericRange( propertyName, lowerBoundary, upperBoundary );
    }

    @Override
    public T propertyIsBetween( String propertyName, Object lowerBoundary, Object upperBoundary ) {
        logEntry( "propertyIsBetween_Object", propertyName, lowerBoundary, upperBoundary );
        return super.propertyIsBetween( propertyName, lowerBoundary, upperBoundary );
    }

    @Override
    public T propertyIsNull( String propertyName ) {
        logEntry( "propertyIsNull", propertyName, null );
        return handlePropertyEqualToString( propertyName, null, null );
    }

    @Override
    public T propertyIsLike( String propertyName, String pattern, boolean isCaseSensitive ) {
        logEntry( "propertyIsLike", propertyName, pattern, isCaseSensitive );
        return handlePropertyLike( propertyName, pattern, isCaseSensitive ? StringFilterOptions.CASE_SENSITIVE : null );
    }

    @Override
    public T propertyIsFuzzy( String propertyName, String literal ) {
        logEntry( "propertyIsFuzzy", propertyName, literal );
        return handlePropertyEqualToString( propertyName, literal, StringFilterOptions.FUZZY );
    }

    @Override
    public T xpathExists( String xpath ) {
        logEntry( "xpathExists", null, xpath );
        return handleXpath( xpath, null, null );
    }

    @Override
    public T xpathIsLike( String xpath, String pattern, boolean isCaseSensitive ) {
        logEntry( "xpathIsLike", xpath, pattern, isCaseSensitive );
        return handleXpath( xpath, pattern, isCaseSensitive ? StringFilterOptions.CASE_SENSITIVE : null );
    }

    @Override
    public T xpathIsFuzzy( String xpath, String literal ) {
        logEntry( "xpathIsFuzzy", xpath, literal );
        return handleXpath( xpath, literal, null );
    }

    @Override
    public T beyond( String propertyName, String wkt, double distance ) {
        logEntry( "beyond", propertyName, wkt, distance );
        return handleGeospatialDistance( propertyName, wkt, distance, GeospatialDistanceFilterOptions.BEYOND );
    }

    @Override
    public T contains( String propertyName, String wkt ) {
        logEntry( "contains", propertyName, wkt );
        return handleGeospatial( propertyName, wkt, GeospatialFilterOptions.CONTAINS );
    }

    @Override
    public T crosses( String propertyName, String wkt ) {
        logEntry( "crosses", propertyName, wkt );
        return handleGeospatial( propertyName, wkt, GeospatialFilterOptions.CROSSES );
    }

    @Override
    public T disjoint( String propertyName, String wkt ) {
        logEntry( "disjoint", propertyName, wkt );
        return handleGeospatial( propertyName, wkt, GeospatialFilterOptions.DISJOINT );
    }

    @Override
    public T dwithin( String propertyName, String wkt, double distance ) {
        logEntry( "dwithin", propertyName, wkt, distance );
        return handleGeospatialDistance( propertyName, wkt, distance, GeospatialDistanceFilterOptions.WITHIN );
    }

    @Override
    public T nearestNeighbor( String propertyName, String wkt ) {
        logEntry( "nearestNeighbor", propertyName, wkt );
        return handleGeospatialDistance( propertyName, wkt, defaultRadiusforNN, GeospatialDistanceFilterOptions.WITHIN );
    }

    @Override
    public T intersects( String propertyName, String wkt ) {
        logEntry( "intersects", propertyName, wkt );
        return handleGeospatial( propertyName, wkt, GeospatialFilterOptions.INTERSECTS );
    }

    @Override
    public T overlaps( String propertyName, String wkt ) {
        logEntry( "overlaps", propertyName, wkt );
        return handleGeospatial( propertyName, wkt, GeospatialFilterOptions.OVERLAPS );
    }

    @Override
    public T touches( String propertyName, String wkt ) {
        logEntry( "touches", propertyName, wkt );
        return handleGeospatial( propertyName, wkt, GeospatialFilterOptions.TOUCHES );
    }

    @Override
    public T within( String propertyName, String wkt ) {
        logEntry( "within", propertyName, wkt );
        return handleGeospatial( propertyName, wkt, GeospatialFilterOptions.WITHIN );
    }

    @Override
    public T after( String propertyName, Date date ) {
        logEntry( "after", propertyName, date );
        return handleTimeAfter( propertyName, date, false );
    }

    @Override
    public T before( String propertyName, Date date ) {
        logEntry( "before", propertyName, date );
        return handleTimeBefore( propertyName, date, false );
    }

    @Override
    public T during( String propertyName, Date startDate, Date endDate ) {
        logEntry( "during", propertyName, startDate, endDate );
        return handleTimeDuring( propertyName, startDate, endDate );
    }

    @Override
    public T relative( String propertyName, long duration ) {
        logEntry( "relative", propertyName, duration );
        return handleTimeAfter( propertyName, new Date( System.currentTimeMillis() - duration ), true );
    }

    protected void logEntry( String method, String name, Object value ) {
        logEntry( method, name, value, false );
    }

    protected void logEntry( String method, String name, Object value, boolean isCaseSensitive ) {
        LOGGER.debug( "ENTERING {}( propertyName=[{}" + (isCaseSensitive ? "_CaseSensitive" : "") + "] , value=[{}] )", method, name, value );
    }

    protected void logEntry( String method, String name, Object lower, Object upper ) {
        LOGGER.debug( "ENTERING {}( propertyName=[{}] , lowerValue=[{}], upperValue=[{}] )", method, name, lower, upper );
    }

}
