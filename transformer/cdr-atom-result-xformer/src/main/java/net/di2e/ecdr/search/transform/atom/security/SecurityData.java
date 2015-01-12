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

import java.util.Map;

public class SecurityData {

    private Map<String, String> securityMarkings = null;
    private String securityNamespace = null;
    
    public SecurityData( Map<String, String> markings, String namespace ) {
        this.securityMarkings = markings;
        this.securityNamespace = namespace;
    }
    
    public Map<String, String> getSecurityMarkings() {
        return securityMarkings;
    }
    
    public String getSecurityNamespace() {
        return securityNamespace;
    }

}
