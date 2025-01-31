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
package net.di2e.ecdr.commons.query.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import net.di2e.ecdr.commons.constants.SearchConstants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryRequestCache {

    private static final Logger LOGGER = LoggerFactory.getLogger( QueryRequestCache.class );

    private LRUCache<String, Boolean> cache = null;

    public QueryRequestCache( int cacheSize ) {
        cache = new LRUCache<String, Boolean>( cacheSize );
    }

    public boolean isQueryIdUnique( String id ) {
        boolean unique = true;
        if ( StringUtils.isNotBlank( id ) ) {
            if ( cache.containsKey( id ) ) {
                unique = false;
            } else {
                cache.put( id, Boolean.TRUE );
            }
        }
        LOGGER.debug( "Checking uniqueness of query with {}={} and isUnique={}", SearchConstants.OID_PARAMETER, id, unique );
        return unique;
    }

    public void updateCacheSize( int capacity ) {
        cache.updateCacheSize( capacity );
    }

    public void add( String id ) {
        cache.put( id, Boolean.TRUE );
    }

    private static final class LRUCache<K, V> extends LinkedHashMap<K, V> {

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
