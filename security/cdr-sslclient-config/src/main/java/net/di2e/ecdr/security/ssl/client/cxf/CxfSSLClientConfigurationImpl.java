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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.KeyManagerUtils;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.codice.ddf.configuration.ConfigurationManager;
import org.codice.ddf.configuration.ConfigurationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of CxfSSLClientConfiguration that should be passed into CXF clients to set the SSL configuration based
 * on the Global Platform Settings values.
 * 
 * This needs to be done because of a CXF Limitation where it doesn't pull the KeyManager values from the Java System
 * Properties
 */
public class CxfSSLClientConfigurationImpl implements CxfSSLClientConfiguration, ConfigurationWatcher {

    private static final transient Logger LOGGER = LoggerFactory.getLogger( CxfSSLClientConfigurationImpl.class );

    private TLSClientParameters tlsClientParameters = null;

    /**
     * Default constructor
     */
    public CxfSSLClientConfigurationImpl() {
        tlsClientParameters = new TLSClientParameters();
    }

    @Override
    public void configurationUpdateCallback( Map<String, String> updatedConfiguration ) {
        String keystore = updatedConfiguration.get( ConfigurationManager.KEY_STORE );
        String keystorePassword = updatedConfiguration.get( ConfigurationManager.KEY_STORE_PASSWORD );

        KeyManager[] keyManagers = null;
        if ( StringUtils.isNotBlank( keystore ) && keystorePassword != null ) {
            try {
                KeyManager manager = KeyManagerUtils.createClientKeyManager( new File( keystore ), keystorePassword );
                keyManagers = new KeyManager[1];
                keyManagers[0] = manager;

            } catch ( IOException | GeneralSecurityException ex ) {
                LOGGER.debug( "Could not access keystore {}, using default java keystore.", keystore );
            }
        }

        String trustStoreLocation = updatedConfiguration.get( ConfigurationManager.TRUST_STORE );
        String trustStorePassword = updatedConfiguration.get( ConfigurationManager.TRUST_STORE_PASSWORD );
        TrustManager[] trustManagers = null;
        if ( StringUtils.isNotBlank( trustStoreLocation ) && trustStorePassword != null ) {
            FileInputStream fis = null;
            try {
                KeyStore trustStore = KeyStore.getInstance( KeyStore.getDefaultType() );
                fis = new FileInputStream( trustStoreLocation );
                try {
                    trustStore.load( fis, StringUtils.isNotEmpty( trustStorePassword ) ? trustStorePassword.toCharArray() : null );
                    trustManagers = new TrustManager[1];
                    trustManagers[0] = TrustManagerUtils.getDefaultTrustManager( trustStore );
                } catch ( IOException ioe ) {
                    LOGGER.debug( "Could not load truststore {}, using default java truststore" );
                }
            } catch ( IOException | GeneralSecurityException ex ) {
                LOGGER.debug( "Could not access truststore {}, using default java truststore.", trustStoreLocation );
            } finally {
                IOUtils.closeQuietly( fis );
            }

        }
        synchronized ( tlsClientParameters ) {
            LOGGER.debug( "Setting the CXF KeyManager and TrustManager based on the Platform Global Configuration values" );
            tlsClientParameters.setKeyManagers( keyManagers );
            tlsClientParameters.setTrustManagers( trustManagers );
        }

    }

    @Override
    public KeyManager getKeyManager() {
        KeyManager[] managers = tlsClientParameters.getKeyManagers();
        if ( managers.length > 0 ) {
            return tlsClientParameters.getKeyManagers()[0];
        }
        return null;
    }

    @Override
    public TrustManager getTrustManager() {
        TrustManager[] managers = tlsClientParameters.getTrustManagers();
        if ( managers.length > 0 ) {
            return tlsClientParameters.getTrustManagers()[0];
        }
        return null;
    }

    @Override
    public TLSClientParameters getTLSClientParameters() {
        return tlsClientParameters;
    }

}
