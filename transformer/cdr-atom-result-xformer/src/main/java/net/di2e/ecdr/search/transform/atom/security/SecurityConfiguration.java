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

public class SecurityConfiguration {

    private String resultSecurityNamespace = null;
    private Map<String, String> resultSecurityMarkings = null;

    public SecurityConfiguration() {
        resultSecurityNamespace = "urn:us:gov:ic:ism:v2";
        resultSecurityMarkings = new HashMap<String, String>();
        resultSecurityMarkings.put( "classification", "U" );
        resultSecurityMarkings.put( "ownerProducer", "USA" );
    }

    public void setResultSecurityNamespace( String namespace ) {
        resultSecurityNamespace = namespace;
    }

    public String getResultSecurityNamespace() {
        return resultSecurityNamespace;
    }

    public void setResultSecurityMarkings( List<String> markings ) {
        resultSecurityMarkings.clear();
        if ( markings != null ) {
            for ( String marking : markings ) {
                String[] values = marking.split( "=" );
                if ( values.length == 2 ) {
                    resultSecurityMarkings.put( values[0], values[1] );
                }
            }
        }
    }

    public Map<String, String> getResultSecurityMarkings() {
        return resultSecurityMarkings;
    }

}
