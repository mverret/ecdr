/**
 * Copyright (C) 2014 Cohesive Integrations, LLC (info@cohesiveintegrations.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.di2e.ecdr.source.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.di2e.ecdr.commons.filter.config.FilterConfig;
import net.di2e.ecdr.commons.filter.config.FilterConfig.AtomContentXmlWrapOption;
import net.di2e.ecdr.libs.cache.Cache;
import net.di2e.ecdr.libs.cache.CacheManager;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.impl.SourceResponseImpl;
import ddf.catalog.source.UnsupportedQueryException;

public class OpenSearchSource extends AbstractCDRSource {

    public static final String TIME_START_NAME = "startTimeParameter";
    public static final String TIME_END_NAME = "endTimeParameter";
    public static final String GEO_BOX_NAME = "boxParameter";
    public static final String GEO_LAT_NAME = "latParameter";
    public static final String GEO_LON_NAME = "lonParameter";
    public static final String GEO_RADIUS_NAME = "radiusParameter";
    public static final String GEO_GEOMETRY_NAME = "geometryParameter";
    public static final String SRU_SORTKEY_NAME = "sortKeysParameter";

    private static final Logger LOGGER = LoggerFactory.getLogger( OpenSearchSource.class );

    private Map<String, String> harcodedParamMap = new HashMap<String, String>();

    private FilterConfig filterConfig = null;
    private CacheManager<Metacard> cacheManager = null;
    private Cache<Metacard> metacardCache = null;
    private String cacheId = null;

    public OpenSearchSource( FilterAdapter adapter, CacheManager<Metacard> manager ) {
        super( adapter );
        filterConfig = new FilterConfig();
        cacheManager = manager;
        setSendSecurityCookie( false );
    }

    @Override
    public SourceResponse enhanceResults( SourceResponse sourceResponse ) {
        for ( Result result : sourceResponse.getResults() ) {
            Metacard metacard = result.getMetacard();
            metacardCache.put( metacard.getId(), metacard );
        }
        return sourceResponse;
    }

    @Override
    public SourceResponse lookupById( QueryRequest queryRequest, String id ) throws UnsupportedQueryException {
        SourceResponse sourceResponse = null;
        LOGGER.debug( "Checking cache for Result with id [{}].", id );
        Metacard metacard = metacardCache.get( id );
        if ( metacard != null ) {
            LOGGER.debug( "Cache hit found for id [{}], returning response", id );
            sourceResponse = new SourceResponseImpl( queryRequest, Arrays.asList( (Result) new ResultImpl( metacard ) ), 1L );
        } else {
            LOGGER.debug( "Could not find result id [{}] in cache", id );
            throw new UnsupportedQueryException( "Queries for parameter uid are not supported by source [" + getId() + "]" );
        }
        return sourceResponse;
    }

    @Override
    public Map<String, String> getStaticUrlQueryValues() {
        return harcodedParamMap;
    }

    @Override
    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    public void cleanUp() {
        LOGGER.debug( "Shutting down CDR Federated Source with id [{}]", getId() );
        if ( metacardCache != null ) {
            metacardCache.destroy();
        }
    }

    public void setThumbnailLinkRelation( String rel ) {
        LOGGER.debug( "ConfigUpdate: Updating the Thumbnail Link Relation value from [{}] to [{}]", filterConfig.getThumbnailLinkRelation(), rel );
        filterConfig.setThumbnailLinkRelation( rel );
    }

    public void setMetadataLinkRelation( String rel ) {
        LOGGER.debug( "ConfigUpdate: Updating the Metadata Link Relation value from [{}] to [{}]", filterConfig.getMetadataLinkRelation(), rel );
        filterConfig.setMetadataLinkRelation( rel );
    }

    public void setProductLinkRelation( String rel ) {
        LOGGER.debug( "ConfigUpdate: Updating the Product Link Relation value from [{}] to [{}]", filterConfig.getProductLinkRelation(), rel );
        filterConfig.setProductLinkRelation( rel );
    }

    public void setProxyProductUrls( boolean proxy ) {
        LOGGER.debug( "ConfigUpdate: Updating the Proxy URLs through Local Instance value from [{}] to [{}]", filterConfig.isProxyProductUrl(), proxy );
        filterConfig.setProxyProductUrl( proxy );
    }

    public void setWrapContentWithXmlOption( String option ) {
        LOGGER.debug( "ConfigUpdate: Updating the WrapContentWithXmlOption value from [{}] to [{}]", filterConfig.getAtomContentXmlWrapOption(), option );
        filterConfig.setAtomContentXmlWrapOption( AtomContentXmlWrapOption.valueOf( option ) );
    }

    public void setStartIndexStartNumber( String startNumber ) {
        try {
            // get the existing start index (0 or 1) to use in the log statement
            // after setting the index
            String oldIndex = filterConfig.isZeroBasedStartIndex() ? "0" : "1";
            filterConfig.setZeroBasedStartIndex( Integer.parseInt( startNumber ) == 0 );
            LOGGER.debug( "ConfigUpdate: Updating the Start Index Numbering value from [{}] to [{}]", oldIndex, startNumber );
        } catch ( NumberFormatException e ) {
            LOGGER.warn( "ConfigUpdate Failed: Attempted to update the 'start index number method' due to non valid (must be 1 or 0) start index numbering passed in["
                    + startNumber + "]" );
        }
    }

    public void setHardCodedParameters( String hardcodedString ) {
        harcodedParamMap.clear();
        LOGGER.debug( "ConfigUpdate: Updating the hard coded parameters to [{}]", hardcodedString );

        if ( StringUtils.isNotBlank( hardcodedString ) ) {
            String[] params = hardcodedString.split( "," );
            for ( String param : params ) {
                String[] singleParam = param.split( "=" );
                if ( singleParam.length == 2 ) {
                    harcodedParamMap.put( singleParam[0], singleParam[1] );
                }
            }
        }
    }

    public void setCacheExpirationMinutes( Long minutes ) {
        if ( minutes == null || minutes == 0 ) {
            LOGGER.debug( "ConfigUpdate: Clearing any existing cached Metacards, and no longer using the Cache for id lookups for source [{}]", getId() );
            if ( metacardCache != null ) {
                metacardCache.destroy();
            }
        } else {
            if ( metacardCache != null ) {
                metacardCache.destroy();
                cacheManager.removeCacheInstance( cacheId );
            }
            cacheId = getId() + "-" + UUID.randomUUID();
            LOGGER.debug( "ConfigUpdate: Creating a cache with id [{}] for Metacard id lookups for source [{}] with an cache expiration time of [{}] minutes",
                    cacheId, getId(), minutes );

            Map<String, Object> cacheProps = new HashMap<String, Object>();
            cacheProps.put( CacheManager.CACHE_EXPIRE_AFTER_MINUTES, minutes );

            metacardCache = cacheManager.createCacheInstance( cacheId, cacheProps );
        }
    }

    @Override
    public boolean useDefaultParameters() {
        return false;
    }

}
