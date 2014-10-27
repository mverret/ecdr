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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Bean that handles setting the SSL cipher suites to the Java
 * System.property from the values set in the Admin Console. These suites will
 * be used for outgoing SSL communications (e.g. in the FederatedSource
 * instances or ResourceReader)
 */
public class SSLCipherConfiguration {

    private static final transient Logger LOGGER = LoggerFactory.getLogger( SSLCipherConfiguration.class );

    public void setCiphers( List<String> ciphers ) {
        if ( ciphers != null ) {
            for ( String cipher : ciphers ) {
                LOGGER.info( "Adding the following SSL cipher suite to the allowed values [{}]", cipher );
            }
        }
    }

}
