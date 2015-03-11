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
