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
package net.di2e.ecdr.search.transform.atom.security.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.di2e.ecdr.search.transform.atom.security.SecurityConfiguration;
import net.di2e.ecdr.search.transform.atom.security.SecurityData;
import net.di2e.ecdr.search.transform.atom.security.SecurityMarkingHandler;
import ddf.catalog.data.Metacard;

public class ConfigurationSecurityMarkingHandler implements SecurityMarkingHandler {

    private SecurityConfiguration securityConfiguration = null;

    public ConfigurationSecurityMarkingHandler( SecurityConfiguration securityConfig ) {
        securityConfiguration = securityConfig;
    }

    @Override
    public SecurityData getSecurityData( Metacard metacard ) {
        Map<String, List<String>> securityDataMarkings = new HashMap<>();
        for ( Entry<String, String> marking : securityConfiguration.getResultSecurityMarkings().entrySet() ) {
            List<String> values = new ArrayList<>();
            values.add( marking.getValue() );
            securityDataMarkings.put( marking.getKey(), values );
        }
        SecurityData securityData = new SecurityData( securityDataMarkings, securityConfiguration.getResultSecurityNamespace() );
        return securityData;
    }

}
