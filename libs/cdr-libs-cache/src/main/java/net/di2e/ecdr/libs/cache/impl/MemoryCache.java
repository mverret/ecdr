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
