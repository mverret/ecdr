/**
 * Copyright (c) Cohesive Integrations, LLC
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
package net.di2e.ecdr.source.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.net.SocketException;

import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.client.ClientException;

import net.di2e.ecdr.source.rest.AbstractCDRSource.PingMethod;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.filter.FilterAdapter;

/**
 * Tests that the certificates are properly added to outgoing requests and allow for mutual authentication on a server
 * that requires client auth.
 */
public class CDRRestSourceSecureTest {

    private static final Logger LOGGER = LoggerFactory.getLogger( CDRRestSourceSecureTest.class );
    
    private static Server server;
    private static int serverPort = 0;
    
    static final String SSL_KEYSTORE_JAVA_PROPERTY = "javax.net.ssl.keyStore";
    static final String SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY = "javax.net.ssl.keyStorePassword";
    static final String SSL_TRUSTSTORE_JAVA_PROPERTY = "javax.net.ssl.trustStore";
    static final String SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY = "javax.net.ssl.trustStorePassword";

    @BeforeClass
    public static void startServer() {

        // create jetty server
        server = new Server();
        server.setStopAtShutdown( true );
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath( "/" );

        // add dummy servlet that will return static response
        context.addServlet( TrustedServlet.class, "/" );

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers( new Handler[] { context, new DefaultHandler() } );
        server.setHandler( handlers );
        SslContextFactory sslContextFactory = new SslContextFactory();
        // server uses the server cert
        sslContextFactory.setKeyStorePath( CDRRestSourceSecureTest.class.getResource( "/serverKeystore.jks" ).getPath() );
        sslContextFactory.setKeyStorePassword( "changeit" );

        // only accept connection with proper client certificate
        sslContextFactory.setNeedClientAuth( true );

        SslSocketConnector sslSocketConnector = new SslSocketConnector( sslContextFactory );
        sslSocketConnector.setPort( serverPort );
        server.addConnector( sslSocketConnector );

        try {
            server.start();
            if ( server.getConnectors().length == 1 ) {
                serverPort = server.getConnectors()[0].getLocalPort();
                LOGGER.info( "Server started on Port: {} ", serverPort );
            } else {
                LOGGER.warn( "Got more than one connector back, could not determine correct port for SSL communication." );
            }
        } catch ( Exception e ) {
            LOGGER.warn( "Could not start jetty server, expecting test failures.", e );
        }
    }

    /**
     * Tests that server properly accepts trusted certificates.
     */
    @Test
    public void testGoodCertificates() {

        CDRRestSource restSource = createSecuredSource( "/serverKeystore.jks", "changeit", "/serverTruststore.jks", "changeit" );
        // quick check that getters do not cause exceptions
        restSource.getContentTypes();
        restSource.getOptions( null );
        restSource.getSupportedSchemes();
        // hit server
        if ( restSource.isAvailable() == false ) {
            fail( "Could not get capabilities from the test server. This means no connection was established." );
        }

    }

    /**
     * Tests that server fails on non-trusted client certificates.
     */
    @SuppressWarnings( "unchecked" )
    @Test
    public void testBadClientCertificate() {

        CDRRestSource restSource = createSecuredSource( "/client-bad.jks", "", "/serverTruststore.jks", "changeit" );
        // hit server
        try {
            if ( restSource.isAvailable() ) {
                fail( "Server should have errored out with bad certificate but request passed instead." );
            }
        } catch ( ClientException e ) {
            assertThat( e.getCause(), anyOf( is( SSLHandshakeException.class ), is( SocketException.class ) ) );
        }

    }

    /**
     * Tests that client fails on non-trusted server certificates.
     */
    // @Test
    public void testBadServerCertificate() {

        CDRRestSource restSource = createSecuredSource( "/serverKeystore.jks", "changeit", "/client-bad.jks", "" );
        // hit server
        try {
            if ( restSource.isAvailable() ) {
                fail( "Client should have errored out with no valid certification path found, but request passed instead." );
            }
        } catch ( ClientException e ) {
            assertThat( e.getCause(), is( SSLHandshakeException.class ) );
        }

    }

    /**
     * Creates the Rest Source and sets the ping method and no ping caching so it the tests will return the proper value
     */
    private CDRRestSource createSecuredSource( String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword ) {
        System.setProperty( SSL_KEYSTORE_JAVA_PROPERTY, CDRRestSourceSecureTest.class.getResource( keyStorePath ).getPath() );
        System.setProperty( SSL_KEYSTORE_PASSWORD_JAVA_PROPERTY, keyStorePassword );
        System.setProperty( SSL_TRUSTSTORE_JAVA_PROPERTY, CDRRestSourceSecureTest.class.getResource( trustStorePath ).getPath() );
        System.setProperty( SSL_TRUSTSTORE_PASSWORD_JAVA_PROPERTY, trustStorePassword );
        
        FilterAdapter filterAdapter = mock( FilterAdapter.class );
        CDRRestSource source = new CDRRestSource( filterAdapter );

        source.setPingUrl( "https://localhost:" + serverPort + "/" );
        source.setPingMethodString( PingMethod.HEAD.toString() );
        source.setPingMethod( PingMethod.GET );
        source.setAvailableCheckCacheTime( 0 );
        source.setMaxResultCount( 10 );
        source.setDefaultResponseFormat( "atom-cdr" );
        source.setEndpointUrl( "https://localhost:" + serverPort + "/" );
        source.setReceiveTimeoutSeconds( 10 );
        source.setConnectionTimeoutSeconds( 1 );

        return source;
    }


}