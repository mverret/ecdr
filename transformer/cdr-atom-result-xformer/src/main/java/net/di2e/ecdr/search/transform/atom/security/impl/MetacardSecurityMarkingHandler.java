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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import net.di2e.ecdr.commons.constants.SecurityConstants;
import net.di2e.ecdr.search.transform.atom.security.SecurityData;
import net.di2e.ecdr.search.transform.atom.security.SecurityMarkingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;

public class MetacardSecurityMarkingHandler implements SecurityMarkingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger( MetacardSecurityMarkingHandler.class );

    @Override
    public SecurityData getSecurityData( Metacard metacard ) {
        Attribute attribute = metacard.getAttribute( SecurityConstants.SECURITY_NAMESPACE );
        String namespace = null;
        if ( attribute != null ) {
            Serializable value = attribute.getValue();
            if ( value instanceof String ) {
                namespace = (String) value;
            } else {
                LOGGER.debug( "The Metacard Attribute named [{}], was not a String, instead it was [{}]", SecurityConstants.SECURITY_NAMESPACE, value.getClass() );
            }
        }

        Map<String, List<String>> security = new MetacardImpl( metacard ).getSecurity();
        if ( security != null && !security.isEmpty() ) {
            return new SecurityData( security, namespace );
        }
        return null;
    }

}
