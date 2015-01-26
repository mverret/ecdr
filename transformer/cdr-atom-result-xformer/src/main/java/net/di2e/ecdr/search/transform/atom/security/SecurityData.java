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
