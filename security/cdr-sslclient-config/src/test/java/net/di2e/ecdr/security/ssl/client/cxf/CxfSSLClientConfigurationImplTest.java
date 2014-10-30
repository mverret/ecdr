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
package net.di2e.ecdr.security.ssl.client.cxf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.codice.ddf.configuration.ConfigurationManager;
import org.junit.Test;

/**
 * Runs some simple unit tests to test out the CXF specific configuration
 */
public class CxfSSLClientConfigurationImplTest {

    /**
     * Tests the constructor to ensure there are no NPEs and objects get sent back as null
     */
    @Test
    public void testEmptyParameters() {
        CxfSSLClientConfiguration config = new CxfSSLClientConfigurationImpl();
        assertNull( config.getKeyManager() );
        assertNull( config.getTrustManager() );
        assertEquals( new TLSClientParameters(), config.getTLSClientParameters() );
    }

    /**
     * Test a valid configuration
     */
    @Test
    public void testConfigurationUpdateCallback() {
        CxfSSLClientConfigurationImpl config = new CxfSSLClientConfigurationImpl();
        Map<String, String> configProperties = new HashMap<String, String>();

        configProperties.put( ConfigurationManager.KEY_STORE, getClass().getResource( "/serverKeystore.jks" ).getPath() );
        configProperties.put( ConfigurationManager.KEY_STORE_PASSWORD, "changeit" );
        configProperties.put( ConfigurationManager.TRUST_STORE, getClass().getResource( "/serverTruststore.jks" ).getPath() );
        configProperties.put( ConfigurationManager.TRUST_STORE_PASSWORD, "changeit" );

        config.configurationUpdateCallback( configProperties );

        assertNotNull( config.getKeyManager() );
        assertNotNull( config.getTrustManager() );
        assertEquals( config.getKeyManager(), config.getTLSClientParameters().getKeyManagers()[0] );
        assertEquals( config.getTrustManager(), config.getTLSClientParameters().getTrustManagers()[0] );
    }

    /**
     * Test if an empty map is passed into the configurationUpdateCallback method
     */
    @Test
    public void testEmptyConfigurationUpdateCallback() {
        CxfSSLClientConfigurationImpl config = new CxfSSLClientConfigurationImpl();
        config.configurationUpdateCallback( new HashMap<String, String>() );
        assertNull( config.getKeyManager() );
        assertNull( config.getTrustManager() );
        assertEquals( new TLSClientParameters(), config.getTLSClientParameters() );
    }

    /**
     * Test if an null map is passed into the configurationUpdateCallback method
     */
    @Test
    public void testNullConfigurationUpdateCallback() {
        CxfSSLClientConfigurationImpl config = new CxfSSLClientConfigurationImpl();
        config.configurationUpdateCallback( null );
        assertNull( config.getKeyManager() );
        assertNull( config.getTrustManager() );
        assertEquals( new TLSClientParameters(), config.getTLSClientParameters() );
    }

}
