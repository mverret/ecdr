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
package net.di2e.ecdr.registry.internal.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegistryDiscoveryAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger( RegistryDiscoveryAgent.class );

    private JmDNS jmDNS = null;
    private static final String CONTEXT_KEY = "context";
    private static final String UPDATE_KEY = "lastUpdated";

    public void init() throws IOException {
        jmDNS = JmDNS.create();
        Map<String, String> propMap = new HashMap<String, String>();
        propMap.put( CONTEXT_KEY, "/services/registry" );
        propMap.put( UPDATE_KEY, Long.toString( System.currentTimeMillis() ) );
        ServiceInfo serviceInfo = ServiceInfo.create( "_http._tcp.local.", "Service Registry", 8181, 0, 0, propMap );
        jmDNS.registerService( serviceInfo );
        LOGGER.info( "Registered registry as service on local DNS." );
    }

    public void destroy() {
        if ( jmDNS != null ) {
            LOGGER.info( "Unregistering all services from DNS multicast." );
            jmDNS.unregisterAllServices();
        }
    }
}
