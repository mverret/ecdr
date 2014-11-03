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
import net.di2e.ecdr.search.api.RegistrableService;

import java.util.Map;

public class RegistrableServiceInfo implements ServiceInfo {

    private RegistrableService service;

    public RegistrableServiceInfo(RegistrableService service) {
        this.service = service;
    }

    @Override
    public String getServiceType() {
        return service.getServiceType();
    }

    @Override
    public String getServiceRelativeUrl() {
        return service.getServiceRelativeUrl();
    }

    @Override
    public String getServiceDescription() {
        return service.getServiceDescription();
    }

    @Override
    public Map<String, String> getProperties() {
        return service.getProperties();
    }
}
