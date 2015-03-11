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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.di2e.ecdr.libs.cache.Cache;
import net.di2e.ecdr.libs.cache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;

public class MetacardMemoryCacheManager implements CacheManager<Metacard> {

    private static final Logger LOGGER = LoggerFactory.getLogger( MetacardMemoryCacheManager.class );
    private Map<String, Cache<Metacard>> cacheList = new HashMap<String, Cache<Metacard>>();;

    public MetacardMemoryCacheManager() {
        LOGGER.debug( "Creating a new LRUMetacardCacheManager for Metacard cache" );
    }

    @Override
    public Cache<Metacard> createCacheInstance( String cacheId, Map<String, Object> cacheProperties ) {
        if ( cacheId == null ) {
            throw new IllegalArgumentException( "CacheId cannot be null when calling the LRUCache.createCache method" );
        } else if ( cacheList.containsKey( cacheId ) ) {
            throw new IllegalArgumentException( "CacheId with the name [" + cacheId + "] already exists, each cache instance must have a unique name" );
        }
        Cache<Metacard> cache = new MemoryCache<Metacard>( getSize( cacheProperties ) );
        cacheList.put( cacheId, cache );
        return cache;
    }

    @Override
    public void destroy() {
        LOGGER.debug( "Destroying all active caches and destroying the Cache Manager" );
        for ( Entry<String, Cache<Metacard>> entry : cacheList.entrySet() ) {
            entry.getValue().destroy();
        }
        cacheList.clear();
    }

    @Override
    public void removeCacheInstance( String cacheId ) {
        LOGGER.debug( "Removing the cache instance [{}]", cacheId );
        cacheList.remove( cacheId );
    }

    protected int getSize( Map<String, Object> cacheProperties ) {
        int size = -1;
        if ( cacheProperties != null ) {
            Object cacheSize = cacheProperties.get( net.di2e.ecdr.libs.cache.CacheManager.CACHE_SIZE );
            if ( cacheSize != null ) {
                if ( cacheSize instanceof Integer ) {
                    LOGGER.debug( "Setting cache maximum size of newly created cache to [{}] entries", cacheSize );
                    size = (Integer) cacheSize;
                } else {
                    LOGGER.warn( "Cache property [{}] was not the expected type of Integer, instead it was [{}]", net.di2e.ecdr.libs.cache.CacheManager.CACHE_SIZE, cacheSize.getClass()
                            .getName() );
                }
            }
        }
        if ( size < 0 ) {
            size = 5000;
            LOGGER.debug( "Duration was not passed into cache creation, so defaulting to cache of [{}] minutes", size );

        }
        return size;
    }

}
