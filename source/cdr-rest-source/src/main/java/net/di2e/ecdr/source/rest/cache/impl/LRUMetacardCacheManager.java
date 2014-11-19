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
package net.di2e.ecdr.source.rest.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.di2e.ecdr.source.rest.cache.Cache;
import net.di2e.ecdr.source.rest.cache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;

public class LRUMetacardCacheManager implements CacheManager<Metacard> {

    private static final Logger LOGGER = LoggerFactory.getLogger( LRUMetacardCacheManager.class );
    private Map<String, Cache<Metacard>> cacheList = new HashMap<String, Cache<Metacard>>();;
    private int cacheSize = 5000;

    public LRUMetacardCacheManager( int size ) {
        LOGGER.debug( "Creating a new LRUMetacardCacheManager with cache instances that will hold a maximum of [{}] entries", size );
        cacheSize = size;
    }

    @Override
    public Cache<Metacard> createCacheInstance( String cacheId ) {
        if ( cacheId == null ) {
            throw new IllegalArgumentException( "CacheId cannot be null when calling the LRUCache.createCache method" );
        } else if ( cacheList.containsKey( cacheId ) ) {
            throw new IllegalArgumentException( "CacheId with the name [" + cacheId + "] already exists, each cache instance must have a unique name" );
        }
        Cache<Metacard> cache = new LRUCache<Metacard>( cacheSize );
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

}
