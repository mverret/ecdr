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
