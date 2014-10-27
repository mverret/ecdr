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
package net.di2e.ecdr.security.ssl.client;

import java.util.Map;

import org.codice.ddf.configuration.ConfigurationManager;
import org.codice.ddf.configuration.ConfigurationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens for the ConfigurationWatcher to be called (when the Admin Console "Platform Global Configuration" is
 * updated, and then it will take the values for the Java Keystore and Truststore and set them to the Java System properties.
 * The System.properties get used by default when making outgoing SSL calls (for example in ResourceReader, FederatedSource, etc.)
 * 
 * NOTE - CXF 2.7.x does not properly configure the keystore (it does the truststore) so if you are using CXF and want to 
 * do 2-way SSL (aka client auth), then you need to set the keystore properties  explicitly).
 */
public class GlobalSSLClientConfigurator implements ConfigurationWatcher {

    private static final transient Logger LOGGER = LoggerFactory.getLogger( GlobalSSLClientConfigurator.class );

    static final String SSL_KEYSTORE_JAVA_PROPERTY = "javax.net.ssl.keyStore";
    static final String SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY = "javax.net.ssl.keyStorePassword";
    static final String SSL_TRUSTSTORE_JAVA_PROPERTY = "javax.net.ssl.trustStore";
    static final String SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY = "javax.net.ssl.trustStorePassword";

    private static final String EMPTY_STRING = "";

    /**
     * Gets the values that were set in the configuration for the Java keystore and truststore
     * and sets them to the corresponding System.properties.
     * 
     * If the configiration values are null, it sets the ptoperties to Empty String instead of 
     * falling back to use the unsecure out of the box cacerts.
     */
    @Override
    public void configurationUpdateCallback( Map<String, String> properties ) {
        LOGGER.debug( "Attempting to update the SSL Java Properties." );
        try {
            if ( properties != null && !properties.isEmpty() ) {
                LOGGER.debug( "Configuration values: {}", properties );

                String keystore = properties.get( ConfigurationManager.KEY_STORE );
                LOGGER.info( "Updating container client SSL keystore proprty to [{}]", keystore );
                System.setProperty( SSL_KEYSTORE_JAVA_PROPERTY, keystore == null ? EMPTY_STRING : keystore );

                String keystorePass = properties.get( ConfigurationManager.KEY_STORE_PASSWORD );
                System.setProperty( SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY, keystorePass == null ? EMPTY_STRING : keystorePass );

                String truststore = properties.get( ConfigurationManager.TRUST_STORE );
                LOGGER.info( "Updating container client SSL truststore property to [{}]", truststore );
                System.setProperty( SSL_TRUSTSTORE_JAVA_PROPERTY, truststore == null ? EMPTY_STRING : truststore );

                String truststorePass = properties.get( ConfigurationManager.TRUST_STORE_PASSWORD );
                System.setProperty( SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY, truststorePass == null ? EMPTY_STRING : truststorePass );

            } else {
                System.setProperty( SSL_KEYSTORE_JAVA_PROPERTY, EMPTY_STRING );
                System.setProperty( SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY, EMPTY_STRING );
                System.setProperty( SSL_TRUSTSTORE_JAVA_PROPERTY, EMPTY_STRING );
                System.setProperty( SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY, EMPTY_STRING );
                LOGGER.info( "Platform Configuration Properties are NULL or empty, setting all client SSL Java property values to null" );
            }
        } catch ( SecurityException e ) {
            LOGGER.warn( "Could not update Java global SSL client properties due to SecurityManager permissions:" + e.getMessage() );
        }
    }
}
