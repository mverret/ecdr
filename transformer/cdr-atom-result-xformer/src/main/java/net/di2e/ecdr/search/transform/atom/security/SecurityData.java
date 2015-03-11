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
package net.di2e.ecdr.search.transform.atom.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityData {

    private HashMap<String, List<String>> securityMarkings = null;
    private String securityNamespace = null;
    
    public SecurityData( Map<String, List<String>> markings, String namespace ) {
        if ( markings != null ) {
            this.securityMarkings = new HashMap<String, List<String>>( markings );
        }
        this.securityNamespace = namespace;
    }
    
    public HashMap<String, List<String>> getSecurityMarkings() {
        return securityMarkings;
    }
    
    public String getSecurityNamespace() {
        return securityNamespace;
    }

}
