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
