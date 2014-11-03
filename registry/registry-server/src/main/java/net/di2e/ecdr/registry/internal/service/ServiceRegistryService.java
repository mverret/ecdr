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
package net.di2e.ecdr.registry.internal.service;

import net.di2e.ecdr.registry.ServiceInfo;
import net.di2e.ecdr.registry.ServiceRegistry;
import net.di2e.ecdr.search.api.RegistrableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ServiceRegistryService implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger( ServiceRegistryService.class );

    private List<RegistrableService> endpoints;

    public ServiceRegistryService( List<RegistrableService> endpoints ) {
        this.endpoints = endpoints;
    }

    @Override
    public List<ServiceInfo> getServices() {
        LOGGER.debug( "Returning up to {} services", endpoints.size() );
        ArrayList<ServiceInfo> infoList = new ArrayList<>( endpoints.size() );
        for ( RegistrableService endpoint : endpoints ) {
            infoList.add( new RegistrableServiceInfo( endpoint ) );
        }
        return infoList;
    }

}
