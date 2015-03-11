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
