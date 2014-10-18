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
package net.di2e.ecdr.search.transform.atom.geo;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import org.apache.abdera.ext.geo.Box;
import org.apache.abdera.ext.geo.Coordinate;
import org.apache.abdera.ext.geo.Coordinates;
import org.apache.abdera.ext.geo.Line;
import org.apache.abdera.ext.geo.Point;
import org.apache.abdera.ext.geo.Polygon;
import org.apache.abdera.ext.geo.Position;
import org.geotools.geometry.jts.JTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Converts abdera geometry shapes into other formats
 */
public final class AbderaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbderaConverter.class);

    private AbderaConverter() {

    }

    public static String convertToWKT(Point point) {
        GeometryFactory geometryFactory = new GeometryFactory();
        return geometryFactory.createPoint(convertCoordinateToJTS(point.getCoordinate())).toText();
    }

    public static String convertToWKT(Line line) {
        GeometryFactory geometryFactory = new GeometryFactory();
        return geometryFactory.createLineString(convertCoordinatesToJTS(line.getCoordinates())).toText();
    }

    public static String convertToWKT(Polygon polygon) {
        GeometryFactory geometryFactory = new GeometryFactory();
        LinearRing ring = geometryFactory.createLinearRing(convertCoordinatesToJTS(polygon.getCoordinates()));
        return geometryFactory.createPolygon(ring, new LinearRing[0]).toText();
    }

    public static String convertToWKT(Box box) {
        com.vividsolutions.jts.geom.Coordinate coord1 = convertCoordinateToJTS(box.getLowerCorner());
        com.vividsolutions.jts.geom.Coordinate coord2 = convertCoordinateToJTS(box.getUpperCorner());
        return JTS.toGeometry(new Envelope(coord1, coord2)).toText();
    }

    public static String convertToWKT(Position position) {
        if (position instanceof Point) {
            return convertToWKT((Point) position);
        } else if (position instanceof Line) {
            return convertToWKT((Line) position);
        } else if (position instanceof Polygon) {
            return convertToWKT((Polygon) position);
        } else if (position instanceof Box) {
            return convertToWKT((Box) position);
        } else {
            LOGGER.warn("No conversion available for position of type {}.", position.getClass().getName());
            return null;
        }
    }

    public static com.vividsolutions.jts.geom.Coordinate convertCoordinateToJTS(Coordinate coordinate) {
        return new com.vividsolutions.jts.geom.Coordinate(coordinate.getLongitude(), coordinate.getLatitude());
    }

    public static com.vividsolutions.jts.geom.Coordinate[] convertCoordinatesToJTS(Coordinates coordinates) {
        List<com.vividsolutions.jts.geom.Coordinate> jtsCoords = new ArrayList<com.vividsolutions.jts.geom.Coordinate>();
        for (Iterator<Coordinate> iterator = coordinates.iterator(); iterator.hasNext();) {
            jtsCoords.add(convertCoordinateToJTS(iterator.next()));
        }
        return jtsCoords.toArray(new com.vividsolutions.jts.geom.Coordinate[jtsCoords.size()]);
    }

}
