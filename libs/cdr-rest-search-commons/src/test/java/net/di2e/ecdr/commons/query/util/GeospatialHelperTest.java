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
package net.di2e.ecdr.commons.query.util;

import org.junit.Assert;
import org.junit.Test;

public class GeospatialHelperTest {

    @Test
    public void testPolygonToWKT() {
        String wkt = GeospatialHelper.polygonToWKT( "45.256,-110.45,46.46,-109.48,43.84,-109.86,45.256,-110.45" );
        Assert.assertEquals( "WKT not expected value from polygon", "POLYGON((-110.45 45.256,-109.86 43.84,-109.48 46.46,-110.45 45.256))", wkt );
    }

}
