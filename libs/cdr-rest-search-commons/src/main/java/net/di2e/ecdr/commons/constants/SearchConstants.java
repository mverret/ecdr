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
package net.di2e.ecdr.commons.constants;

public final class SearchConstants {
    
    private SearchConstants() {
    }

    public static final String NO_QUERY_PARAMETERS_MESSAGE = "The query did not contain any of the required critera, one of the following is required [searchTerms, geospatial, or temporal]";

    public static final String POSTED = "posted";
    public static final String INFO_CUT_OFF = "infoCutOff";
    public static final String VALID_TIL = "validTil";
    public static final String TEMPORAL_COVERAGE = "temporalCoverage";
    
    public static final String NEXT_LINK_REL = "next";
    public static final String PREV_LINK_REL = "previous";
    public static final String SELF_LINK_REL = "self";
    public static final String FIRST_LINK_REL = "first";
    public static final String LAST_LINK_REL = "last";
    public static final String SEARCH_LINK_REL = "search";
    
    public static final String FEED_TITLE = "feed-title";
    
    //Parameters
    public static final String KEYWORD_PARAMETER = "q";
    public static final String CASESENSITIVE_PARAMETER = "caseSensitive";
    public static final String FUZZY_PARAMETER = "fuzzy";
    
    public static final String RADIUS_PARAMETER = "radius";
    public static final String LATITUDE_PARAMETER = "lat";
    public static final String LONGITUDE_PARAMETER = "lon";
    public static final String BOX_PARAMETER = "box";
    public static final String GEOMETRY_PARAMETER = "geometry";
    public static final String POLYGON_PARAMETER = "polygon";
    public static final String UID_PARAMETER = "uid";
    
    public static final String STARTDATE_PARAMETER = "dtStart";
    public static final String ENDDATE_PARAMETER = "dtEnd";
    public static final String DATETYPE_PARAMETER = "dtType";
    
    public static final String STARTINDEX_PARAMETER = "startIndex";
    public static final String COUNT_PARAMETER = "count";
    
    public static final String SORTKEYS_PARAMETER = "sortKeys";

    public static final String FORMAT_PARAMETER = "format";
    public static final String STRICTMODE_PARAMETER = "strict";
    public static final String QUERYLANGUAGE_PARAMETER = "queryLanguage";
    
    public static final String TIMEOUT_PARAMETER = "timeout";
    public static final String STATUS_PARAMETER = "status";
    public static final String OID_PARAMETER = "oid";
    
    public static final String GEORSS_RESULT_FORMAT_PARAMETER = "georssFormat";
    public static final String GEORSS_SIMPLE_FORMAT = "simple";
    public static final String GEORSS_GML_FORMAT = "gml";

    // Parameter that are part of the Basic Plus query profile
    public static final String GEOSPATIALOPERATOR_PARAMETER = "spatialOp";
    public static final String TEXTPATH_PARAMETER = "textPath";

    public static final String CDR_KEYWORD_QUERY_LANGUAGE = "cdr-kw-basic-2.0";
    public static final String CDR_CQL_QUERY_LANGUAGE = "cql-1.2";

    public static final String LOCAL_SOURCE_ID = "local-source-id";

    public static final String TRUE_STRING = "1";
    public static final String FALSE_STRING = "0";

    // Constants for Strategies
    public static final String TOTAL_HITS = "total-hits";
    public static final String SITE_LIST = "site-list";

    public static final String TOTAL_RESULTS_RETURNED = "total-results-returned";
    public static final String ELAPSED_TIME = "elapsed-time";
    
    // public static final String LOCAL_RETRIEVE_URL_PREFIX = "local-retrieve-url-prefix";
    public static final String METACARD_TRANSFORMER_NAME = "metacard-transformer-name";

}
