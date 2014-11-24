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
package net.di2e.ecdr.source.rest;

import ddf.registry.api.DynamicServiceIdentifier;

public class SimpleDynamicServiceIdentifier implements DynamicServiceIdentifier {

    private String factoryPid;
    private String serviceType;

    public SimpleDynamicServiceIdentifier(String factoryPid, String serviceType) {
        this.factoryPid = factoryPid;
        this.serviceType = serviceType;
    }

    @Override
    public String getFactoryIdentifier() {
        return factoryPid;
    }

    @Override
    public String getServiceType() {
        return serviceType;
    }
}
