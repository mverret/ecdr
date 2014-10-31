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

/**
 * Interface for an OSGi service that maps factory PIDs to their corresponding service name. Generally used to map
 * RegistrableService implementations to their corresponding configuration factory in a DynamicExternalSource.
 */
public interface DynamicServiceResolver {

    /**
     * Returns the corresponding factory pid for the given service name.
     *
     * @param serviceType Service type to lookup the pid for
     * @return Factory pid that can be used to create new instances using the configuration admin.
     */
    String getFactoryPid(String serviceType);

    /**
     * Returns the corresponding service type for the given factory pid.
     *
     * @param factoryPid Factory pid used to create new instances of the service
     * @return Service type that defines the service
     */
    String getServiceType(String factoryPid);

}
