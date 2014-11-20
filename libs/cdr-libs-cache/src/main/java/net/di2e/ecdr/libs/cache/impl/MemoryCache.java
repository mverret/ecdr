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

import java.util.LinkedHashMap;
import java.util.Map;

import net.di2e.ecdr.libs.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryCache<T> implements Cache<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger( MemoryCache.class );

    private LRUCacheMap<String, T> metacardCache = null;

    public MemoryCache( int size ) {
        metacardCache = new LRUCacheMap<String, T>( size );
    }

    @Override
    public void put( String id, T entry ) {
        LOGGER.debug( "Adding entry to cache with id [{}]", id );
        synchronized ( metacardCache ) {
            metacardCache.put( id, entry );
        }
    }

    @Override
    public void destroy() {
        synchronized ( metacardCache ) {
            metacardCache.clear();
        }
    }

    @Override
    public T get( String id ) {
        LOGGER.debug( "Searching cache for entry with id [{}]", id );
        synchronized ( metacardCache ) {
            return metacardCache.get( id );
        }
    }

    public class LRUCacheMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 1L;
        private int cacheSize = 0;

        public LRUCacheMap( int capacity ) {
            super( capacity + 1, 1.1f, true );
            this.cacheSize = capacity;
        }

        @Override
        protected boolean removeEldestEntry( Map.Entry<K, V> eldest ) {
            return size() > cacheSize;
        }

        @Override
        public V put( K k, V v ) {
            if ( cacheSize > 0 ) {
                return super.put( k, v );
            }
            return null;
        }

        public void updateCacheSize( int size ) {
            this.clear();
            cacheSize = size;
        }
    }
}
