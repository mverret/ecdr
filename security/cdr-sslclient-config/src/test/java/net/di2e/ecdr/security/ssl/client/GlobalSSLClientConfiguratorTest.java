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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codice.ddf.configuration.ConfigurationManager;
import org.junit.Test;

/**
 * Runs tests to ensure the Java properties are defaulted to what they should be as well as the Jav aproperties are
 * properly set when the methods are called
 */
public class GlobalSSLClientConfiguratorTest {

    private static String[] ciphers = new String[] { "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_anon_WITH_AES_128_CBC_SHA" };

    private static String[] protocols = new String[] { "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2" };

    /**
     * Test thet the System.properties get set with the Keystore and truststore
     * location and password
     */
    @Test
    public void testPopulatedConfigurationUpdateCallback() {
        Map<String, String> configProperties = new HashMap<String, String>();
        GlobalSSLClientConfigurator configurator = new GlobalSSLClientConfigurator();

        configProperties.put( ConfigurationManager.KEY_STORE, "keystore-property" );
        configProperties.put( ConfigurationManager.KEY_STORE_PASSWORD, "keystore-password-property" );
        configProperties.put( ConfigurationManager.TRUST_STORE, "truststore-property" );
        configProperties.put( ConfigurationManager.TRUST_STORE_PASSWORD, "truststore-password-property" );
        configurator.configurationUpdateCallback( configProperties );
        assertEquals( "keystore-property", System.getProperty( GlobalSSLClientConfigurator.SSL_KEYSTORE_JAVA_PROPERTY ) );
        assertEquals( "keystore-password-property", System.getProperty( GlobalSSLClientConfigurator.SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY ) );
        assertEquals( "truststore-property", System.getProperty( GlobalSSLClientConfigurator.SSL_TRUSTSTORE_JAVA_PROPERTY ) );
        assertEquals( "truststore-password-property", System.getProperty( GlobalSSLClientConfigurator.SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY ) );
    }
    
    /**
     * Test the the System.properties get set with the Keystore and truststore
     * location and password for the values that are present and empty string for values that are not present
     */
    @Test
    public void testMixedConfigurationUpdateCallback() {
        Map<String, String> configProperties = new HashMap<String, String>();
        GlobalSSLClientConfigurator configurator = new GlobalSSLClientConfigurator();

        configProperties.put( ConfigurationManager.KEY_STORE, "keystore-property" );
        configProperties.put( ConfigurationManager.TRUST_STORE_PASSWORD, "truststore-password-property" );
        configurator.configurationUpdateCallback( configProperties );
        assertEquals( "keystore-property", System.getProperty( GlobalSSLClientConfigurator.SSL_KEYSTORE_JAVA_PROPERTY ) );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY ) );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_TRUSTSTORE_JAVA_PROPERTY ) );
        assertEquals( "truststore-password-property", System.getProperty( GlobalSSLClientConfigurator.SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY ) );
    }

    /**
     * Test that the GlobalSSLClientConfigurator handles a null configuration
     * properly
     */
    @Test
    public void testNullConfigurationUpdateCallback() {
        Map<String, String> configProperties = null;
        GlobalSSLClientConfigurator configurator = new GlobalSSLClientConfigurator();
        configurator.configurationUpdateCallback( configProperties );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_KEYSTORE_JAVA_PROPERTY ) );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY ) );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_TRUSTSTORE_JAVA_PROPERTY ) );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY ) );
    }

    /**
     * Test that the GlobalSSLClientConfigurator handles and empty configuration
     * properly
     */
    @Test
    public void testEmptyConfigurationUpdateCallback() {
        Map<String, String> configProperties = new HashMap<String, String>();
        GlobalSSLClientConfigurator configurator = new GlobalSSLClientConfigurator();

        configurator.configurationUpdateCallback( configProperties );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_KEYSTORE_JAVA_PROPERTY ) );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY ) );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_TRUSTSTORE_JAVA_PROPERTY ) );
        assertEquals( "", System.getProperty( GlobalSSLClientConfigurator.SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY ) );
    }

    /**
     * Test that the default ciphers are set properly (when the setIncludeCipher method is not called)
     */
    @Test
    public void testDefaultCiphers() {
        new GlobalSSLClientConfigurator();
        assertEquals( StringUtils.join( ciphers, ',' ) + ",", System.getProperty( GlobalSSLClientConfigurator.HTTPS_CIPHER_SUITES_PROPERTY ) );
    }

    /**
     * Test that the default HTTPS protocols are set properly (when the setHttpsProtocols method is not called)
     */
    @Test
    public void testDefaultProtocols() {
        new GlobalSSLClientConfigurator();
        assertEquals( StringUtils.join( protocols, ',' ) + ",", System.getProperty( GlobalSSLClientConfigurator.HTTPS_PROTOCOLS_PROPERTY ) );
    }

    /**
     * Test that the Inclede Ciphers java property are set properly (when the setIncludeCipher method is called)
     */
    @Test
    public void testSetCiphers() {
        GlobalSSLClientConfigurator configurator = new GlobalSSLClientConfigurator();
        String[] myCiphers = new String[] { "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA" };
        configurator.setIncludeCiphers( Arrays.asList( myCiphers ) );
        assertEquals( StringUtils.join( myCiphers, ',' ) + ",", System.getProperty( GlobalSSLClientConfigurator.HTTPS_CIPHER_SUITES_PROPERTY ) );
    }

    /**
     * Test that the HTTPS protocols java property are set properly (when the setHttpsProtocols method is called)
     */
    @Test
    public void testSetProtocols() {
        GlobalSSLClientConfigurator configurator = new GlobalSSLClientConfigurator();
        String[] myProtocols = new String[] { "TLSv1.1", "TLSv1.2" };
        configurator.setHttpsProtocols( Arrays.asList( myProtocols ) );
        assertEquals( StringUtils.join( myProtocols, ',' ) + ",", System.getProperty( GlobalSSLClientConfigurator.HTTPS_PROTOCOLS_PROPERTY ) );
    }

}
