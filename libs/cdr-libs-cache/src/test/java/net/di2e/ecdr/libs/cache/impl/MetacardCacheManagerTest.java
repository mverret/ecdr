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
package net.di2e.ecdr.libs.cache.impl;

import ddf.catalog.data.Metacard;
import net.di2e.ecdr.libs.cache.Cache;
import net.di2e.ecdr.libs.cache.CacheManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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
