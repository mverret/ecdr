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
package net.di2e.ecdr.security.cors.config;

import java.util.List;

import net.di2e.ecdr.security.cors.CORSFilter;

public interface CORSFilterConfiguration {

    void addCORSFilterConfiguration( CORSFilter filter );

    /**
     * @param filter
     */
    void removeCORSFilterConfiguration( CORSFilter filter );

    /**
     * The origin strings to allow. An empty list allows all origins.
     * 
     * @param allowedOrigins
     *            a list of case-sensitive origin strings.
     */
    void setAllowOrigins( List<String> allowedOrigins );

    /**
     * The value for the Access-Control-Allow-Credentials header. If false, no
     * header is added. If true, the header is added with the value 'true'.
     * 
     * @param allowCredentials
     */
    void setAllowCredentials( boolean allowCredentials );

    /**
     * A list of non-simple headers to be exposed via
     * Access-Control-Expose-Headers.
     * 
     * @param exposeHeaders
     *            the list of (case-sensitive) header names.
     */
    void setExposeHeaders( List<String> exposeHeaders );

}
