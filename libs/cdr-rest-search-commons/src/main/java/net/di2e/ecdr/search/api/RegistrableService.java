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

/**
 * This is the interface that any endpoint that wants to be able to be dynamically published to a registry would
 * implement.
 */
public interface RegistrableService {

    /**
     * The Type of the service. This field is how the Registry subscribe matches up with the existing returned registry
     * entries to know what "type" of source to create. Obviously this needs to matchup with the values that are in the
     * Dynamic Sources that are to be created from the registry entry. An example of this value for an existing endpoint
     * would be "CDR REST Service"
     *
     * @return A string value that defines the type for this service.
     */
    String getServiceType();

    /**
     * This is the relative URL to the Service for example for cdr rest it is /services/cdr/search/rest
     *
     * @return A string value of the relative context to this service
     */
    String getServiceRelativeUrl();

    /**
     * Human readable description of the service. This description will be used by the registry to inform users what this
     * service offers.
     *
     * @return Human-readable description.
     */
    String getServiceDescription();

    /**
     * This is the extensibility point, and where all the additional properties a source has can be put in. So this
     * would handle things like registerForEvents, and subscribeCriteria
     *
     * @return A map with additionally properties or an empty map if no additional properties are used.
     */
    Map<String, String> getProperties();

}
