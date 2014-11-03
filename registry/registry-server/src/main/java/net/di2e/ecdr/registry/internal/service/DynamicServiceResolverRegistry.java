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

import net.di2e.ecdr.search.api.DynamicServiceIdentifier;
import net.di2e.ecdr.search.api.DynamicServiceResolver;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DynamicServiceResolverRegistry implements DynamicServiceResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicServiceResolverRegistry.class);

    private List<DynamicServiceIdentifier> identifiers;

    public DynamicServiceResolverRegistry(List<DynamicServiceIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public String getFactoryPid(String serviceType) {
        for (DynamicServiceIdentifier identifier : identifiers) {
            if (StringUtils.equals(serviceType, identifier.getServiceType())) {
                return identifier.getFactoryIdentifier();
            }
        }
        LOGGER.info("No Factory PID found for {}, returning a 'null' PID.", serviceType);
        return null;
    }

    @Override
    public String getServiceType(String factoryPid) {
        for (DynamicServiceIdentifier identifier : identifiers) {
            if (StringUtils.equals(factoryPid, identifier.getFactoryIdentifier())) {
                return identifier.getServiceType();
            }
        }
        LOGGER.info("No Service Type found for {}, returning a 'null' type.", factoryPid);
        return null;
    }
}
