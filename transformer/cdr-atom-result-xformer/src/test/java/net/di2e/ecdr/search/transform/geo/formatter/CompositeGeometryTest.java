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
package net.di2e.ecdr.search.transform.geo.formatter;

import com.vividsolutions.jts.io.WKTReader;
import net.di2e.ecdr.search.transform.atom.response.AtomTest;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CompositeGeometryTest {

    @Test
    public void testGetCompositeGeometry() throws Exception {

        WKTReader wktReader = new WKTReader();
        // point
        CompositeGeometry point = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.POINT_WKT ) );
        assertNotNull( point );
        assertNotNull( CompositeGeometry.getCompositeGeometry( Point.TYPE, point.toJsonMap() ) );
        // line
        CompositeGeometry line = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.LINE_WKT ) );
        assertNotNull( line );
        assertNotNull( CompositeGeometry.getCompositeGeometry( LineString.TYPE, line.toJsonMap() ) );
        // multipoint
        CompositeGeometry multiPoint = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.MULTIPOINT_WKT ) );
        assertNotNull( multiPoint );
        assertNotNull( CompositeGeometry.getCompositeGeometry( MultiPoint.TYPE, multiPoint.toJsonMap() ) );
        // multilinestring
        CompositeGeometry multiLineString = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.MULTILINESTRING_WKT ) );
        assertNotNull( multiLineString );
        assertNotNull( CompositeGeometry.getCompositeGeometry( MultiLineString.TYPE, multiLineString.toJsonMap() ) );
        // polygon
        CompositeGeometry polygon = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.POLYGON_WKT ) );
        assertNotNull( polygon );
        assertNotNull( CompositeGeometry.getCompositeGeometry( Polygon.TYPE, polygon.toJsonMap() ) );
        // multipolygon
        CompositeGeometry multiPolygon = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.MULTIPOLYGON_WKT ) );
        assertNotNull( multiPolygon );
        assertNotNull( CompositeGeometry.getCompositeGeometry( MultiPolygon.TYPE, multiPolygon.toJsonMap() ) );
        // geometrycollection
        CompositeGeometry geometryCollection = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.GEOMETRYCOLLECTION_WKT ) );
        assertNotNull( geometryCollection );
        assertNotNull( CompositeGeometry.getCompositeGeometry( GeometryCollection.TYPE, geometryCollection.toJsonMap() ) );
        // null
        assertNull( CompositeGeometry.getCompositeGeometry( null ) );
        assertNull( CompositeGeometry.getCompositeGeometry( null, Collections.emptyMap() ) );

    }

}
