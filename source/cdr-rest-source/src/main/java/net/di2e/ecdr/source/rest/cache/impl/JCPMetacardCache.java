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

public class JCPMetacardCache {

    private static final Logger LOGGER = LoggerFactory.getLogger( JCPMetacardCache.class );

    private Cache<String, Metacard> metacardCache = null;
    private CachingProvider cachingProvider = null;
    private CacheManager cacheManager = null;

    public JCPMetacardCache( String cacheName, long minutes ) {

        Caching.setDefaultClassLoader( JCPMetacardCache.class.getClassLoader() );
        cachingProvider = Caching.getCachingProvider( JCPMetacardCache.class.getClassLoader() );

        cacheManager = cachingProvider.getCacheManager();

        // configure the cache
        MutableConfiguration<String, Metacard> config = new MutableConfiguration<String, Metacard>();
        config.setStoreByValue( true ).setTypes( String.class, Metacard.class ).setExpiryPolicyFactory( AccessedExpiryPolicy.factoryOf( new Duration( TimeUnit.MINUTES, minutes ) ) )
                .setStatisticsEnabled( true );

        // create the cache
        cacheName = "source-result-cache-" + cacheName;
        cacheManager.createCache( cacheName, config );

        // get the cache
        metacardCache = cacheManager.getCache( cacheName, String.class, Metacard.class );
    }

    public void putMetacard( Metacard metacard ) {
        LOGGER.debug( "Adding metacard to cache with id [{}]", metacard.getId() );
        metacardCache.put( metacard.getId(), metacard );
    }

    public Metacard getMetacard( String id ) {
        LOGGER.debug( "Searching cache for Metacard with id [{}]", id );
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( JCPMetacardCache.class.getClassLoader() );
            return metacardCache.get( id );
        } finally {
            Thread.currentThread().setContextClassLoader( currentClassLoader );
        }

    }

    public void closeCache() {
        metacardCache.clear();
        metacardCache.close();
        cacheManager.close();
        cachingProvider.close();
    }

}
