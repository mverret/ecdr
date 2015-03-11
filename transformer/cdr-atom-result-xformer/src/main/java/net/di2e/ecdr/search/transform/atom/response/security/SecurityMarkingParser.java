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
package net.di2e.ecdr.search.transform.atom.response.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import net.di2e.ecdr.commons.constants.SecurityConstants;
import net.di2e.ecdr.search.transform.atom.security.SecurityData;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.lang.StringUtils;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;

public final class SecurityMarkingParser {

    private static final Set<String> NAMESPACES = new HashSet<String>();

    static {
        NAMESPACES.add( "urn:us:gov:ic:ism:v2" );
        NAMESPACES.add( "urn:us:gov:ic:ism" );
    }

    private SecurityMarkingParser() {
    }

    public static Metacard addSecurityToMetacard( Metacard metacard, Entry entry ) {
        HashMap<String, List<String>> securityProps = new HashMap<String, List<String>>();
        MetacardImpl metacardImpl = new MetacardImpl( metacard );
        List<QName> attributes = entry.getAttributes();
        if ( attributes != null ) {
            String metacardSecurityNamespace = null;
            for ( QName qName : attributes ) {
                String namespace = qName.getNamespaceURI();
                if ( NAMESPACES.contains( namespace ) ) {
                    String value = entry.getAttributeValue( qName );
                    if ( StringUtils.isNotBlank( value ) ) {
                        securityProps.put( qName.getLocalPart(), getValues( value ) );
                        if ( metacardSecurityNamespace == null ) {
                            metacardSecurityNamespace = namespace;
                        }
                    }
                }
            }
            if ( !securityProps.isEmpty() ) {
                metacardImpl.setSecurity( securityProps );
                metacardImpl.setAttribute( SecurityConstants.SECURITY_NAMESPACE, metacardSecurityNamespace );
            }
        }
        return metacardImpl;
    }

    public static SecurityData getFeedSecurityMarkings( Feed feed ) {

        List<QName> attributes = feed.getAttributes();
        if ( attributes != null ) {
            HashMap<String, List<String>> securityProps = new HashMap<String, List<String>>();
            String feedNamespace = null;
            for ( QName qName : attributes ) {
                String namespace = qName.getNamespaceURI();
                if ( NAMESPACES.contains( namespace ) ) {
                    String value = feed.getAttributeValue( qName );
                    if ( StringUtils.isNotBlank( value ) ) {
                        securityProps.put( qName.getLocalPart(), getValues( value ) );
                        if ( feedNamespace == null ) {
                            feedNamespace = namespace;
                        }
                    }
                }
            }
            if ( !securityProps.isEmpty() ) {
                return new SecurityData( securityProps, feedNamespace );
            }
        }
        return null;
    }

    public static List<String> getValues( String attributeValue ) {

        String[] values = attributeValue.split( " " );
        return Arrays.asList( values );
    }

}
