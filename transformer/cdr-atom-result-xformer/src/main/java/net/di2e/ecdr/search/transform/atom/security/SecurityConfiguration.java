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
