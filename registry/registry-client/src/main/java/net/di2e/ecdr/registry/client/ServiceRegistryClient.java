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

import net.di2e.ecdr.registry.ServiceInfo;
import net.di2e.ecdr.search.api.DynamicServiceResolver;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Client code that connects to a service registry and retrieves data back.
 */
public class ServiceRegistryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger( ServiceRegistryClient.class );

    private ConfigurationAdmin configAdmin;
    private DynamicServiceResolver serviceResolver;

    private List<Configuration> configurationList = new ArrayList<Configuration>();

    public ServiceRegistryClient( ConfigurationAdmin configAdmin, DynamicServiceResolver serviceResolver ) {
        LOGGER.info( "Starting new service registry client." );
        this.configAdmin = configAdmin;
        this.serviceResolver = serviceResolver;
    }

    public void setRegistryUrl( String url ) {
        if ( StringUtils.isNotBlank( url ) ) {
            if ( !configurationList.isEmpty() ) {
                LOGGER.debug( "Updating registry URL and removing old sources." );
                clearConfigurations();
            }
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet( url );
            try {
                CloseableHttpResponse response = httpClient.execute( httpGet );
                String endpoints = IOUtils.toString( response.getEntity().getContent() );
                JSONArray endpointArray = new JSONArray( endpoints );
                for ( int i = 0; i < endpointArray.length(); i++ ) {
                    JSONObject curEndpoint = endpointArray.getJSONObject( i );

                    // get endpoint props
                    String type = curEndpoint.getString( ServiceInfo.TYPE_KEY );
                    String endpointUrl = curEndpoint.getString( ServiceInfo.URL_KEY );

                    // get factory pid using service type
                    String pid = serviceResolver.getFactoryPid( type );
                    LOGGER.debug( "Got factory pid of [{}] for service of type [{}].", pid, type );

                    // create new source pointing to this endpoint
                    Configuration siteConfig = configAdmin.createFactoryConfiguration( pid, null );
                    Dictionary<String, Object> properties = new Hashtable<>();
                    properties.put( "endpointUrl", endpointUrl );
                    properties.put( "id", pid );
                    LOGGER.debug( "Creating new source that points to {}.", endpointUrl );
                    siteConfig.update( properties );
                    configurationList.add( siteConfig );
                }
            } catch ( Exception e ) {
                LOGGER.warn( "Encountered an error while trying to connect to the remote registry.", e );
            } finally {
                IOUtils.closeQuietly( httpClient );
            }
        } else {
            LOGGER.debug( "Registry location reset due to url being empty or null. Deleting any remote sources created from previous registry." );
            clearConfigurations();
        }
    }

    /**
     * Clears out the configurations which will remove any generated sources that were created from the external registry.
     */
    private void clearConfigurations() {
        for ( Configuration config : configurationList ) {
            try {
                config.delete();
            } catch ( IOException ioe ) {
                LOGGER.warn( "Could not delete configuration " + config.toString(), ioe );
            } catch ( IllegalStateException ise ) {
                LOGGER.debug( "Configuration was already deleted, ignore error and continuing deletions.", ise );
            }
        }
        configurationList.clear();
    }

}
