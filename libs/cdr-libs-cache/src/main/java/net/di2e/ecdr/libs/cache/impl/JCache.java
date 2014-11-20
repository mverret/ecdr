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

import javax.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCache<T> implements net.di2e.ecdr.libs.cache.Cache<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger( JCache.class );

    private Cache<String, T> cache = null;

    @Override
    public void put( String id, T entry ) {
        LOGGER.debug( "Adding entry to cache with id [{}]", id );
        cache.put( id, entry );
    }

    @Override
    public T get( String id ) {
        LOGGER.debug( "Searching cache for Entry with id [{}]", id );
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( JCache.class.getClassLoader() );
            return cache.get( id );
        } finally {
            Thread.currentThread().setContextClassLoader( currentClassLoader );
        }
    }

    @Override
    public void destroy() {
        cache.clear();
        cache.close();
    }

    public JCache( Cache<String, T> jcache ) {
        this.cache = jcache;
    }



}
