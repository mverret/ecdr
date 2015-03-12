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
package net.di2e.ecdr.search.transform.atom.security.impl;

import net.di2e.ecdr.search.transform.atom.security.FeedSecurityConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedSecurityConfigurationImpl implements FeedSecurityConfiguration {

    private String format = "atom-ddms-2.0";
    private String namespace = "urn:us:gov:ic:ism:v2";
    private Map<String, String> attributes = null;

    public void setFormat( String newFormat ) {
        format = newFormat;
    }

    public void setNamespace( String newNamespace) {
        namespace = newNamespace;
    }

    public void setAttributeList( String newAttributes ) {
        if ( newAttributes != null ) {
            List<String> attrList = Arrays.asList(newAttributes.split( "," ));
            setAttributesList( attrList );
        }
    }

    public void setAttributesList( List<String> attrList ) {
        attributes = new HashMap<String, String>();
        if ( attrList != null ) {
            for ( String attribute : attrList ) {
                String[] values = attribute.split( "=" );
                if ( values.length == 2 ) {
                    attributes.put( values[0], values[1] );
                }
            }
        }
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
