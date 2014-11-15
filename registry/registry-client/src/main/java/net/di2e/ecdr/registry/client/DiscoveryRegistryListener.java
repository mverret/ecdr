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
package net.di2e.ecdr.registry.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;

/**
 * Listens for registries on the local network using the mDNS protocol, implemented by JmDNS, and automatically registers them into the service registry client.
 */
public class DiscoveryRegistryListener implements ServiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger( DiscoveryRegistryListener.class );

    private JmDNS jmDNS;
    private ServiceRegistryClient registryClient;
    private long lastUpdated = 0;

    private static final String TYPE = "_http._tcp.local.";

    public DiscoveryRegistryListener( ServiceRegistryClient registryClient ) throws IOException {
        this.registryClient = registryClient;
        jmDNS = JmDNS.create();
        jmDNS.addServiceListener( TYPE, this );
        LOGGER.info( "Registered listener for local network registries." );
    }

    @Override
    public void serviceAdded( ServiceEvent event ) {
        LOGGER.trace( "New service added, waiting until it is resolved until adding registry." );
    }

    @Override
    public void serviceRemoved( ServiceEvent event ) {
        LOGGER.debug( "Service removed, updating registry client." );
        registryClient.setRegistryUrl( null );
    }

    @Override
    public void serviceResolved( ServiceEvent event ) {
        LOGGER.debug( "Service Resolved!" );
        ServiceInfo serviceInfo = event.getInfo();
        long updatedTime = Long.parseLong( serviceInfo.getPropertyString( "lastUpdated" ) );
        if ( updatedTime > lastUpdated ) {
            String registryUrl = serviceInfo.getApplication() + ":/" + serviceInfo.getInetAddresses()[0] + ":" + serviceInfo.getPort() + serviceInfo.getPropertyString( "context" );
            LOGGER.info( "Updating registry client with a new registry url: {}", registryUrl );
            registryClient.setRegistryUrl( registryUrl );
            lastUpdated = updatedTime;
        } else {
            LOGGER.debug( "Received a service resolved message, but registry was not newer. Not updating registry client." );
        }
    }
}
