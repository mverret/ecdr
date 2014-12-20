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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        assertFalse( point.toGeoRssPositions().isEmpty() );
        assertEquals(AtomTest.POINT_WKT, point.toWkt());
        // line
        CompositeGeometry line = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.LINE_WKT ) );
        assertNotNull( line );
        assertNotNull( CompositeGeometry.getCompositeGeometry( LineString.TYPE, line.toJsonMap() ) );
        assertFalse( line.toGeoRssPositions().isEmpty() );
        assertEquals(AtomTest.LINE_WKT, line.toWkt());
        // multipoint
        CompositeGeometry multiPoint = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.MULTIPOINT_WKT ) );
        assertNotNull( multiPoint );
        assertNotNull( CompositeGeometry.getCompositeGeometry( MultiPoint.TYPE, multiPoint.toJsonMap() ) );
        assertFalse( multiPoint.toGeoRssPositions().isEmpty() );
        assertEquals(AtomTest.MULTIPOINT_WKT, multiPoint.toWkt());
        // multilinestring
        CompositeGeometry multiLineString = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.MULTILINESTRING_WKT ) );
        assertNotNull( multiLineString );
        assertNotNull( CompositeGeometry.getCompositeGeometry( MultiLineString.TYPE, multiLineString.toJsonMap() ) );
        assertFalse( multiLineString.toGeoRssPositions().isEmpty() );
        assertEquals(AtomTest.MULTILINESTRING_WKT, multiLineString.toWkt());
        // polygon
        CompositeGeometry polygon = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.POLYGON_WKT ) );
        assertNotNull( polygon );
        assertNotNull( CompositeGeometry.getCompositeGeometry( Polygon.TYPE, polygon.toJsonMap() ) );
        assertFalse( polygon.toGeoRssPositions().isEmpty() );
        assertEquals(AtomTest.POLYGON_WKT, polygon.toWkt());
        // multipolygon
        CompositeGeometry multiPolygon = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.MULTIPOLYGON_WKT ) );
        assertNotNull( multiPolygon );
        assertNotNull( CompositeGeometry.getCompositeGeometry( MultiPolygon.TYPE, multiPolygon.toJsonMap() ) );
        assertFalse( multiPolygon.toGeoRssPositions().isEmpty() );
        assertEquals(AtomTest.MULTIPOLYGON_WKT, multiPolygon.toWkt());
        // geometrycollection
        CompositeGeometry geometryCollection = CompositeGeometry.getCompositeGeometry( wktReader.read( AtomTest.GEOMETRYCOLLECTION_WKT ) );
        assertNotNull( geometryCollection );
        assertNotNull( CompositeGeometry.getCompositeGeometry( GeometryCollection.TYPE, geometryCollection.toJsonMap() ) );
        assertFalse( geometryCollection.toGeoRssPositions().isEmpty() );
        assertEquals(AtomTest.GEOMETRYCOLLECTION_WKT, geometryCollection.toWkt());
        // null
        assertNull( CompositeGeometry.getCompositeGeometry( null ) );
        assertNull( CompositeGeometry.getCompositeGeometry( null, Collections.emptyMap() ) );

    }

}
