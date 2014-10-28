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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codice.ddf.configuration.ConfigurationManager;
import org.codice.ddf.configuration.ConfigurationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens for the ConfigurationWatcher to be called (when the Admin Console "Platform Global Configuration"
 * is updated, and then it will take the values for the Java Keystore and Truststore and set them to the Java System
 * properties. The System.properties get used by default when making outgoing SSL calls (for example in ResourceReader,
 * FederatedSource, etc.)
 * 
 * NOTE - CXF 2.7.x does not properly configure the keystore (it does the truststore) so if you are using CXF and want
 * to do 2-way SSL (aka client auth), then you should check out the CxfSSLClientConfiguration).
 */
public class GlobalSSLClientConfigurator implements ConfigurationWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger( GlobalSSLClientConfigurator.class );

    static final String SSL_KEYSTORE_JAVA_PROPERTY = "javax.net.ssl.keyStore";
    static final String SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY = "javax.net.ssl.keyStorePassword";
    static final String SSL_TRUSTSTORE_JAVA_PROPERTY = "javax.net.ssl.trustStore";
    static final String SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY = "javax.net.ssl.trustStorePassword";

    static final String HTTPS_CIPHER_SUITES_PROPERTY = "https.cipherSuites";
    static final String HTTPS_PROTOCOLS_PROPERTY = "https.protocols";

    private static final String EMPTY_STRING = "";

    /**
     * Default constructor which sets the default properties for the ciphers and protocols. This needs to be done in
     * case the Metatype is not called (on first deployment) or is not used.
     */
    public GlobalSSLClientConfigurator() {
        // If the ciphers or protocols are updated be sure to update the associated metatype.xml file with the
        // added/removed ciphers/protocols
        String[] ciphers = new String[] { "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_anon_WITH_AES_128_CBC_SHA" };
        setIncludeCiphers( Arrays.asList( ciphers ) );
        
        String[] protocols = new String[] { "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2" };
        setHttpsProtocols( Arrays.asList( protocols ) );

        // NOTE - Don't need to set the properties that are in the configurationUpdateCallback method since that will
        // always be covered by the container
    }

    /**
     * Gets the values that were set in the configuration for the Java keystore and truststore and sets them to the
     * corresponding System.properties.
     * 
     * If the configiration values are null, it sets the ptoperties to Empty String instead of falling back to use the
     * unsecure out of the box cacerts.
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

    /**
     * Updates the included SSL Cipher Suites, and set the Java System Property 'https.cipherSuites' accordingly
     * 
     * @param includedCiphers
     *            List of ciphers that should be included
     */
    public void setIncludeCiphers( List<String> includedCiphers ) {
        StringBuilder ciphersBuidler = new StringBuilder();
        if ( includedCiphers != null ) {
            for ( String includedCipher : includedCiphers ) {
                ciphersBuidler.append( includedCipher + "," );
            }
            LOGGER.debug( "Setting the SSL cipher suite filter [{}] to the included (Allowed) list", ciphersBuidler );
        }
        System.setProperty( HTTPS_CIPHER_SUITES_PROPERTY, ciphersBuidler.toString() );
    }

    /**
     * Updates the included HTTPS Protocols, and set the Java System Property 'https.protocols' accordingly
     * 
     * @param includedCiphers
     *            List of HTTPS protocols that can be used
     */
    public void setHttpsProtocols( List<String> protocols ) {
        StringBuilder protocolsBuidler = new StringBuilder();
        if ( protocols != null ) {
            for ( String protocol : protocols ) {
                protocolsBuidler.append( protocol + "," );
            }
            LOGGER.debug( "Setting the allowed HTTPS Protocols [{}]", protocolsBuidler );
        }
        System.setProperty( HTTPS_PROTOCOLS_PROPERTY, protocolsBuidler.toString() );
    }
}
