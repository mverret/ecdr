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

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.apache.cxf.configuration.jsse.TLSClientParameters;

/**
 * Interface that has CXF Specific CXF SSL Client Configuration Objects because CXF does not honor the All the Java
 * System Properties for SSL settings (specifically the keystore settings). So this interface is can be used to set
 * those CXF specfic settings.
 */
public interface CxfSSLClientConfiguration {
    
    /**
     * Returns the SSL KeyManager. The implementation classes should populate this through a generic configuration way,
     * like when the Global Platform Configuration is set
     * 
     * @return The SSL KeyManager
     */
    KeyManager getKeyManager();
    
    /**
     * Returns the SSL TrustManager. The implementation classes should populate this through a generic configuration
     * way, like when the Global Platform Configuration is set
     * 
     * @return The SSL TrustManager
     */
    TrustManager getTrustManager();
    
    /**
     * Returns the CXF TLSClientParameters which can be passed to a CXF HTTPConduit class. The TLS Parameters include
     * the TrustManager and KeyManager.
     * 
     * @return the CXF TLSClientParameters
     */
    TLSClientParameters getTLSClientParameters();
    
}
