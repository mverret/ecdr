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
package net.di2e.ecdr.commons.query.rest.parsers;

import java.util.Arrays;

import javax.ws.rs.core.MultivaluedMap;

import net.di2e.ecdr.commons.util.SearchConstants;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for BasicQueryParser class
 */
public class BasicQueryParserTest {

    @Test
    public void testGetGeoRSSFormat() {
        MultivaluedMap<String, String> props = new MetadataMap<String, String>();
        BasicQueryParser parser = new BasicQueryParser();
        Assert.assertNull( parser.getGeoRSSFormat( props ) );
        
        props.putSingle( SearchConstants.GEORSS_RESULT_FORMAT_PARAMETER, null );
        Assert.assertNull( parser.getGeoRSSFormat( props ) );

        props.clear();
        props.putSingle( SearchConstants.GEORSS_RESULT_FORMAT_PARAMETER, "" );

        props.clear();
        props.putSingle( SearchConstants.GEORSS_RESULT_FORMAT_PARAMETER, "value" );
        Assert.assertEquals( "value", parser.getGeoRSSFormat( props ) );
        
        props.clear();
        props.put( SearchConstants.GEORSS_RESULT_FORMAT_PARAMETER, Arrays.asList( "value1", "value2" ) );
        Assert.assertEquals( "value1", parser.getGeoRSSFormat( props ) );
    }

}
