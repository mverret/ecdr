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
package net.di2e.ecdr.commons.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchUtilsTest {

    private static final String MAP_STRING = "1=2,2=3,3=4";
    private static final List<String> MAP_LIST = Arrays.asList(MAP_STRING.split( "," ));

    @Test
    public void testConvertStringToMap() {
        Map<String, String> map = SearchUtils.convertToMap( MAP_STRING );
        assertEquals(3, map.size());
        assertTrue(map.containsKey( "1" ));
        assertTrue(map.containsKey( "2" ));
        assertTrue(map.containsKey( "3" ));
        assertEquals("2", map.get( "1" ));
        assertEquals("3", map.get( "2" ));
        assertEquals("4", map.get( "3" ));
    }

    @Test
    public void testConvertListToMap() {
        Map<String, String> map = SearchUtils.convertToMap( MAP_LIST );
        assertEquals(3, map.size());
        assertTrue(map.containsKey( "1" ));
        assertTrue(map.containsKey( "2" ));
        assertTrue(map.containsKey( "3" ));
        assertEquals("2", map.get( "1" ));
        assertEquals("3", map.get( "2" ));
        assertEquals("4", map.get( "3" ));
    }

}
