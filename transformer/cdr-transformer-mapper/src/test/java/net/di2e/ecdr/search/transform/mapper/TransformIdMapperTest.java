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
package net.di2e.ecdr.search.transform.mapper;

import junit.framework.Assert;

import org.junit.Test;

public class TransformIdMapperTest {

    @Test
    public void getQueryTranformValueTest() {
        TransformIdMapperImpl mapper = new TransformIdMapperImpl();
        Assert.assertEquals( "cdr-atom", mapper.getQueryResponseTransformValue( "atom" ) );
        Assert.assertEquals( "atom", mapper.getQueryResponseTransformValue( "ddf-atom" ) );
        Assert.assertEquals( "cdr-atom", mapper.getQueryResponseTransformValue( "cdr-atom" ) );
        Assert.assertEquals( "atom-with-payload", mapper.getQueryResponseTransformValue( "atom-ddms" ) );
        Assert.assertEquals( "atom-with-payload", mapper.getQueryResponseTransformValue( "atom-ddms-2.0" ) );
        Assert.assertEquals( "atom-with-payload", mapper.getQueryResponseTransformValue( "atom-ddms-4.1" ) );
        Assert.assertEquals( "atom-with-payload", mapper.getQueryResponseTransformValue( "atom-ddms-5.0" ) );

        Assert.assertEquals( null, mapper.getMetacardTransformValue( "atom" ) );
        Assert.assertEquals( null, mapper.getMetacardTransformValue( "ddf-atom" ) );
        Assert.assertEquals( null, mapper.getMetacardTransformValue( "cdr-atom" ) );
        Assert.assertEquals( null, mapper.getMetacardTransformValue( "atom-ddms" ) );
        Assert.assertEquals( "ddms20", mapper.getMetacardTransformValue( "atom-ddms-2.0" ) );
        Assert.assertEquals( "ddms41", mapper.getMetacardTransformValue( "atom-ddms-4.1" ) );
        Assert.assertEquals( "ddms50", mapper.getMetacardTransformValue( "atom-ddms-5.0" ) );
    }

}
