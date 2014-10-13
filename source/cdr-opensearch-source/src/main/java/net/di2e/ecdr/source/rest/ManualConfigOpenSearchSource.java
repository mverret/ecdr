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
package net.di2e.ecdr.source.rest;

import java.util.HashMap;
import java.util.Map;

import net.di2e.ecdr.commons.filter.config.FilterConfig;
import net.di2e.ecdr.commons.filter.config.FilterConfig.SingleRecordQueryMethod;
import net.di2e.ecdr.commons.util.SearchConstants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.filter.FilterAdapter;

public class ManualConfigOpenSearchSource extends AbstractOpenSearchSource {

    public static final String TIME_START_NAME = "startTimeParameter";
    public static final String TIME_END_NAME = "endTimeParameter";
    public static final String GEO_BOX_NAME = "boxParameter";
    public static final String GEO_LAT_NAME = "latParameter";
    public static final String GEO_LON_NAME = "lonParameter";
    public static final String GEO_RADIUS_NAME = "radiusParameter";
    public static final String GEO_GEOMETRY_NAME = "geometryParameter";
    public static final String SRU_SORTKEY_NAME = "sortKeysParameter";

    private static final transient Logger LOGGER = LoggerFactory.getLogger( ManualConfigOpenSearchSource.class );

    private String queryEndpointUrl = null;

    private String pingUrl = null;
    private PingMethod pingMethod = PingMethod.NONE;
    private long availableCheckCacheTime = 60000;
    private String defaultResponseFormat = null;

    private Map<String, String> parameterMap = new HashMap<String, String>();
    private Map<String, String> harcodedParamMap = new HashMap<String, String>();

    private FilterConfig filterConfig = null;

    public ManualConfigOpenSearchSource( FilterAdapter adapter ) {
        super( adapter );
        filterConfig = new FilterConfig();

    }

    @Override
    public String getEndpointURL() {
        return queryEndpointUrl;
    }

    @Override
    public String getPingUrl() {
        return pingUrl;
    }

    @Override
    public PingMethod getPingMethod() {
        return pingMethod;
    }

    @Override
    public long getSourceAvailCheckMillis() {
        return availableCheckCacheTime;
    }

    @Override
    public Map<String, String> getDynamicUrlParameterMap() {
        return parameterMap;
    }

    @Override
    public String getDefaultResponseFormat() {
        return defaultResponseFormat;
    }

    @Override
    public Map<String, String> getStaticUrlQueryValues() {
        return harcodedParamMap;
    }

    @Override
    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    public void setEndpointUrl( String endpoint ) {
        if ( endpoint != null && !endpoint.equals( queryEndpointUrl ) ) {
            LOGGER.debug( "ConfigUpdate: Updating the source endpoint url value from [{}] to [{}] for sourceId [{}]", queryEndpointUrl, endpoint, getId() );
            queryEndpointUrl = endpoint;
            endpointUrlUpdated();
        }
    }

    public void setPingUrl( String url ) {
        if ( (StringUtils.isBlank( url ) && StringUtils.isNotBlank( pingUrl )) || (url != null && !url.equals( pingUrl )) ) {
            LOGGER.debug( "ConfigUpdate: Updating the ping (site availability check) endpoint url value from [{}] to [{}]", pingUrl, url );
            pingUrl = url;
            pingUrlUpdated();
        }
    }

    public void setSitePingMethod( String method ) {

        try {
            LOGGER.debug( "ConfigUpdate: Updating the httpPing method value from [{}] to [{}]", pingMethod, method );
            pingMethod = PingMethod.valueOf( method );
        } catch ( IllegalArgumentException | NullPointerException e ) {
            LOGGER.warn( "Could not update the http ping method due to invalid valus [{}], so leaving at [{}]", method, pingMethod );
        }
    }

    public void setSingleRecordQueryMethod( String method ) {
        LOGGER.debug( "ConfigUpdate: Updating the Single Record Lookup Method value from [{}] to [{}]", filterConfig.getSingleRecordQueryMethod(), method );
        filterConfig.setSingleRecordQueryMethod( SingleRecordQueryMethod.valueOf( method ) );
    }

    public void setMetadataLinkRelation( String rel ) {
        LOGGER.debug( "ConfigUpdate: Updating the Metadata Link Relation value from [{}] to [{}]", filterConfig.getMetadataLinkRelation(), rel );
        filterConfig.setMetadataLinkRelation( rel );
    }

    public void setProductLinkRelation( String rel ) {
        LOGGER.debug( "ConfigUpdate: Updating the Product Link Relation value from [{}] to [{}]", filterConfig.getProductLinkRelation(), rel );
        filterConfig.setProductLinkRelation( rel );
    }

    public void setDefaultResponseFormat( String format ) {
        LOGGER.debug( "ConfigUpdate: Updating the default response format value from [{}] to [{}]", defaultResponseFormat, format );
        defaultResponseFormat = format;
    }

    public void setSearchTermsParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating os:searchTerms parameter from [{}] to [{}]", parameterMap.get( SearchConstants.KEYWORD_PARAMETER ), param );
        parameterMap.put( SearchConstants.KEYWORD_PARAMETER, param );
    }

    public void setStartIndexParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating os:startIndex parameter from [{}] to [{}]", parameterMap.get( SearchConstants.STARTINDEX_PARAMETER ), param );
        parameterMap.put( SearchConstants.STARTINDEX_PARAMETER, param );
    }

    public void setCountParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating os:count parameter from [{}] to [{}]", parameterMap.get( SearchConstants.COUNT_PARAMETER ), param );
        parameterMap.put( SearchConstants.COUNT_PARAMETER, param );
    }

    public void setStartTimeParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating time:start parameter from [{}] to [{}]", parameterMap.get( SearchConstants.STARTDATE_PARAMETER ), param );
        parameterMap.put( SearchConstants.STARTDATE_PARAMETER, param );
    }

    public void setEndTimeParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating time:end parameter from [{}] to [{}]", parameterMap.get( SearchConstants.ENDDATE_PARAMETER ), param );
        parameterMap.put( SearchConstants.ENDDATE_PARAMETER, param );
    }

    public void setUidParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating geo:uid parameter from [{}] to [{}]", parameterMap.get( SearchConstants.UID_PARAMETER ), param );
        parameterMap.put( SearchConstants.UID_PARAMETER, param );
    }

    public void setBoxParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating geo:box parameter from [{}] to [{}]", parameterMap.get( SearchConstants.BOX_PARAMETER ), param );
        parameterMap.put( SearchConstants.BOX_PARAMETER, param );
    }

    public void setLatParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating geo:lat parameter from [{}] to [{}]", parameterMap.get( SearchConstants.LATITUDE_PARAMETER ), param );
        parameterMap.put( SearchConstants.LATITUDE_PARAMETER, param );
    }

    public void setLonParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating geo:lon parameter from [{}] to [{}]", parameterMap.get( SearchConstants.LONGITUDE_PARAMETER ), param );
        parameterMap.put( SearchConstants.LONGITUDE_PARAMETER, param );
    }

    public void setRadiusParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating geo:radius parameter from [{}] to [{}]", parameterMap.get( SearchConstants.RADIUS_PARAMETER ), param );
        parameterMap.put( SearchConstants.RADIUS_PARAMETER, param );
    }

    public void setGeometryParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating geo:box parameter from [{}] to [{}]", parameterMap.get( SearchConstants.GEOMETRY_PARAMETER ), param );
        parameterMap.put( SearchConstants.GEOMETRY_PARAMETER, param );
    }

    public void setSortKeysParameter( String param ) {
        LOGGER.debug( "ConfigUpdate: Updating sru:sortKeys parameter from [{}] to [{}]", parameterMap.get( SearchConstants.SORTKEYS_PARAMETER ), param );
        parameterMap.put( SearchConstants.SORTKEYS_PARAMETER, param );
    }

    public void setHardCodedParameters( String hardcodedString ) {
        harcodedParamMap.clear();
        LOGGER.debug( "ConfigUpdate: Updating the hard coded parameters to [{}]", hardcodedString );
        try {
            if ( StringUtils.isNotBlank( hardcodedString ) ) {
                String[] params = hardcodedString.split( "," );
                for ( String param : params ) {
                    String[] singleParam = param.split( "=" );
                    harcodedParamMap.put( singleParam[0], singleParam[1] );
                }
            }
        } catch ( Exception e ) {
            LOGGER.warn( "Could not update hard coded parameters because the String was not in the correct foramt [{}]", hardcodedString );
        }
    }

}
