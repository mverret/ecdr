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
package cdr.ddf.commons.query.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class QueryRequestCache {

    private LRUCache<String, Boolean> cache = null;

    public QueryRequestCache( int cacheSize ) {
        cache = new LRUCache<String, Boolean>( cacheSize );
    }

    public boolean isQueryIdUnique( String id, boolean addIfMissing ) {
        boolean unique = true;
        if ( StringUtils.isNotBlank( id ) ) {
            if ( cache.containsKey( id ) ) {
                unique = false;
            } else if ( addIfMissing ) {
                cache.put( id, Boolean.TRUE );
            }
        }
        return unique;
    }

    public void updateCacheSize( int capacity ) {
        cache.updateCacheSize( capacity );
    }

    public void add( String id ) {
        cache.put( id, Boolean.TRUE );
    }

    private class LRUCache<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 1L;
        private int cacheSize = 0;

        public LRUCache( int capacity ) {
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
