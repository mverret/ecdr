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
package net.di2e.ecdr.search.api;


import java.util.Map;

public interface DynamicExternalSource {

    /**
     * The ID/Name of the Site
     */
    void setId( String id );

    /**
     * The endpoint URL for the site.
     */
    void setEndpointUrl( String url );

    /**
     * Basically contains any additional info outside of the URL and the ID that the source would need. This is the
     * extensibility point for things like registerForEvents, subscriptionCriteria, etc
     */
    void setSourceProperties( Map<String, String> props );

}
