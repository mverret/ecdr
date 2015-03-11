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
