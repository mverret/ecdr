/**
 * Copyright (C) 2014 Cohesive Integrations, LLC (info@cohesiveintegrations.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
