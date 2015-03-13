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
        for ( Entry<String, String> marking : securityConfiguration.getAttributes().entrySet() ) {
            List<String> values = new ArrayList<>();
            values.add( marking.getValue() );
            securityDataMarkings.put( marking.getKey(), values );
        }
        SecurityData securityData = new SecurityData( securityDataMarkings, securityConfiguration.getNamespace() );
        return securityData;
    }

}
