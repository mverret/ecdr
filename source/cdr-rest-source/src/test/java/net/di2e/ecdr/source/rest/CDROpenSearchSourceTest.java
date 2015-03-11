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

import net.di2e.ecdr.commons.filter.config.FilterConfig;
import net.di2e.ecdr.libs.cache.CacheManager;
import net.di2e.ecdr.libs.cache.impl.MetacardMemoryCacheManager;

import org.junit.Test;

import ddf.catalog.data.Metacard;

public class CDROpenSearchSourceTest extends CDRAbstractSourceTest {

    private CacheManager<Metacard> cacheManager = new MetacardMemoryCacheManager();

    @Override
    AbstractCDRSource createSource() {
        OpenSearchSource source = new OpenSearchSource(FILTER_ADAPTER, cacheManager);
        source.setCacheExpirationMinutes( new Long(1) );
        source.setParameterMap( "os:searchTerms=q,os:count=count,os:startIndex=startIndex,time:start=dtStart,time:end=dtEnd,geo:uid=uid,geo:box=box,geo:lat=lat,geo:lon=lon,geo:radius=radius,geo:geometry=geometry,sru:sortKeys=sortKeys" );
        source.setStartIndexStartNumber( "1" );
        source.setMetadataLinkRelation( "alternate" );
        source.setProductLinkRelation( "enclosure" );
        source.setThumbnailLinkRelation( "preview" );
        source.setProxyProductUrls( true );
        source.setWrapContentWithXmlOption( FilterConfig.AtomContentXmlWrapOption.NEVER_WRAP.toString() );
        source.setHardCodedParameters( "test=example" );
        return source;
    }

    @Test
    public void testCleanUp() {
        CacheManager<Metacard> caches = new MetacardMemoryCacheManager();
        OpenSearchSource source = new OpenSearchSource(FILTER_ADAPTER, caches);
        source.cleanUp();
    }

}
