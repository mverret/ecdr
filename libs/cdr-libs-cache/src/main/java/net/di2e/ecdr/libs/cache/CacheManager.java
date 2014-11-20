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
package net.di2e.ecdr.libs.cache;

import java.util.Map;

public interface CacheManager<T> {

    String CACHE_EXPIRE_AFTER_MINUTES = "cache-expire-after-minutes";
    String CACHE_SIZE = "cache-size";

    Cache<T> createCacheInstance( String cacheId, Map<String, Object> cacheProperties );

    void removeCacheInstance( String cacheId );

    void destroy();

}
