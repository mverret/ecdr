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
package net.di2e.ecdr.libs.cache.impl;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;
import net.di2e.ecdr.libs.cache.Cache;
import net.di2e.ecdr.libs.cache.CacheManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class MetacardCacheManagerTest {

    private static final String CACHE_ID = "example";
    private static final String CACHE_ID_2 = "example2";
    private static final String CACHE_ID_3 = "example3";

    private static final int CACHE_SIZE = 10;

    private static final long CACHE_DURATION = 10;

    private CacheManager<Metacard> cacheManager;

    abstract CacheManager<Metacard> createCacheManager();

    @Test
    public void testCacheManager() {
        // create the cache manager
        cacheManager = createCacheManager();

        // test cache creation
        Cache<Metacard> cache1 = cacheManager.createCacheInstance( CACHE_ID, createProps( CACHE_SIZE, CACHE_DURATION ) );
        Cache<Metacard> cache2 = cacheManager.createCacheInstance( CACHE_ID_2, null );
        Cache<Metacard> cache3 = cacheManager.createCacheInstance( CACHE_ID_3, createProps( null, null ) );
        assertNotNull(cache1);
        assertNotNull(cache2);
        assertNotNull(cache3);

        // verify cache works
        MetacardImpl metacard = new MetacardImpl();
        metacard.setId( CACHE_ID );
        cache1.put( CACHE_ID, metacard );
        assertEquals(CACHE_ID, cache1.get( CACHE_ID ).getId());

        // test duplicate check
        try {
            cacheManager.createCacheInstance( CACHE_ID, createProps( CACHE_SIZE, CACHE_DURATION ) );
            fail("Cache Manager did not throw an exception when a duplicate cache was created.");
        } catch (IllegalArgumentException iae) {
            // good exception
        }

        // test removal
        cacheManager.removeCacheInstance( CACHE_ID );
        cacheManager.createCacheInstance( CACHE_ID, createProps( CACHE_SIZE, CACHE_DURATION ) );

        // test bad name
        try {
            cacheManager.createCacheInstance( null, createProps( CACHE_SIZE, CACHE_DURATION ) );
            fail("Cache Manager did not throw an exception when a bad cache id was used.");
        } catch (IllegalArgumentException iae) {
            //good exception
        }

        // destroy the cache manager
        cacheManager.destroy();
    }


    private Map<String, Object> createProps(Integer size, Long duration) {
        Map<String, Object> cacheProperties = new HashMap<String, Object>();
        cacheProperties.put( CacheManager.CACHE_SIZE, size );
        cacheProperties.put( CacheManager.CACHE_EXPIRE_AFTER_MINUTES, duration);
        return cacheProperties;
    }

}
