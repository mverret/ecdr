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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;

public class MetacardJCacheManager implements net.di2e.ecdr.libs.cache.CacheManager<Metacard> {

    private static final String CACHE_ID_PREFIX = "jcache-metacard-";
    private static final Logger LOGGER = LoggerFactory.getLogger( MetacardJCacheManager.class );

    private CachingProvider cachingProvider = null;
    private CacheManager cacheManager = null;

    public MetacardJCacheManager() {
        LOGGER.debug( "Creating a new JCacheManager for caching Metacards" );
        Caching.setDefaultClassLoader( JCache.class.getClassLoader() );
        cachingProvider = Caching.getCachingProvider();
        cacheManager = cachingProvider.getCacheManager();
    }

    @Override
    public net.di2e.ecdr.libs.cache.Cache<Metacard> createCacheInstance( String cacheId, Map<String, Object> cacheProperties ) {
        if ( cacheId == null ) {
            throw new IllegalArgumentException( "CacheId cannot be null when calling the LRUCache.createCache method" );
        } else if ( cacheManager.getCache( CACHE_ID_PREFIX + cacheId ) != null ) {
            throw new IllegalArgumentException( "CacheId with the name [" + cacheId + "] already exists, each cache instance must have a unique name" );
        }

        // configure the cache
        MutableConfiguration<String, Metacard> config = new MutableConfiguration<String, Metacard>();
        config.setStoreByValue( true ).setTypes( String.class, Metacard.class ).setExpiryPolicyFactory( AccessedExpiryPolicy.factoryOf( getDuration( cacheProperties ) ) )
                .setStatisticsEnabled( true );

        // create the cache
        Cache<String, Metacard> cache = cacheManager.createCache( CACHE_ID_PREFIX + cacheId, config );

        JCache<Metacard> jcache = new JCache<Metacard>( cache );
        return jcache;
    }

    @Override
    public void destroy() {
        LOGGER.debug( "Destroying all active caches and destroying the Cache Manager" );
        for ( String cacheId : cacheManager.getCacheNames() ) {
            cacheManager.destroyCache( CACHE_ID_PREFIX + cacheId );
        }
        cacheManager.close();
        cachingProvider.close();
    }

    @Override
    public void removeCacheInstance( String cacheId ) {
        LOGGER.debug( "Removing the cache instance [{}]", cacheId );
        cacheManager.destroyCache( CACHE_ID_PREFIX + cacheId );
    }

    protected Duration getDuration( Map<String, Object> cacheProperties ) {
        Duration duration = null;
        if ( cacheProperties != null ) {
            Object minutes = cacheProperties.get( net.di2e.ecdr.libs.cache.CacheManager.CACHE_EXPIRE_AFTER_MINUTES );
            if ( minutes != null ) {
                if ( minutes instanceof Long ) {
                    LOGGER.debug( "Setting cache duration of newly created cache to [{}] minutes", minutes );
                    duration = new Duration( TimeUnit.MINUTES, (Long) minutes );
                } else {
                    LOGGER.warn( "Cache property [{}] was not the expected type of Long, instead it was [{}]", net.di2e.ecdr.libs.cache.CacheManager.CACHE_EXPIRE_AFTER_MINUTES, minutes.getClass()
                            .getName() );
                }
            }
        }
        if ( duration == null ) {
            LOGGER.debug( "Duration was not passed into cache creation, so defaulting to cache of 60 minutes" );
            duration = new Duration( TimeUnit.MINUTES, 60 );
        }
        return duration;
    }

}
