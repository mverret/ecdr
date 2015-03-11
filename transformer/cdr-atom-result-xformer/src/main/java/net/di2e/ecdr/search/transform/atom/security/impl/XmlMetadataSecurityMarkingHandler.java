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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.di2e.ecdr.search.transform.atom.response.security.SecurityMarkingParser;
import net.di2e.ecdr.search.transform.atom.security.SecurityData;
import net.di2e.ecdr.search.transform.atom.security.SecurityMarkingHandler;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import ddf.catalog.data.Metacard;
import ddf.util.XPathHelper;

public class XmlMetadataSecurityMarkingHandler implements SecurityMarkingHandler {

    private static final String XMLNS_PREFIX = "xmlns";

    @Override
    public SecurityData getSecurityData(Metacard metacard) {
        String metadata = metacard.getMetadata();
        if ( StringUtils.isNotBlank( metadata ) ) {
            XPathHelper helper = new XPathHelper( metacard.getMetadata() );
            Document document = helper.getDocument();
            NodeList nodeList = document.getElementsByTagNameNS( "*", "security" );
            if ( nodeList != null && nodeList.getLength() > 0 ) {
                Element element = (Element) nodeList.item( 0 );
                NamedNodeMap nodeNameMap = element.getAttributes();
                int length = nodeNameMap.getLength();

                Map<String, List<String>> securityProps = new HashMap<String, List<String>>();
                String securityNamespace = null;
                for ( int i = 0; i < length; i++ ) {
                    Attr attr = (Attr) nodeNameMap.item( i );
                    String value = attr.getValue();
                    if ( !attr.getName().startsWith( XMLNS_PREFIX ) && StringUtils.isNotBlank( value ) ) {
                        securityProps.put( attr.getLocalName(), SecurityMarkingParser.getValues( value ) );
                        if ( securityNamespace == null ) {
                            securityNamespace = attr.getNamespaceURI();
                        }
                    }
                }
                if ( !securityProps.isEmpty() ) {
                    return new SecurityData( securityProps, securityNamespace );
                }
            }
        }
        return null;
    }


}
