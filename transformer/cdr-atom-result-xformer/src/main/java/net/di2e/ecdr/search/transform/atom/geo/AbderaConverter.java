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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Converts abdera geometry shapes into other formats
 */
public final class AbderaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger( AbderaConverter.class );

    private AbderaConverter() {

    }

    public static String convertToWKT( Point point ) {
        GeometryFactory geometryFactory = new GeometryFactory();
        return geometryFactory.createPoint( convertCoordinateToJTS( point.getCoordinate() ) ).toText();
    }

    public static String convertToWKT( Line line ) {
        GeometryFactory geometryFactory = new GeometryFactory();
        return geometryFactory.createLineString( convertCoordinatesToJTS( line.getCoordinates() ) ).toText();
    }

    public static String convertToWKT( Polygon polygon ) {
        GeometryFactory geometryFactory = new GeometryFactory();
        LinearRing ring = geometryFactory.createLinearRing( convertCoordinatesToJTS( polygon.getCoordinates() ) );

        return geometryFactory.createPolygon( ring, new LinearRing[0] ).toText();
    }

    public static String convertToWKT( Box box ) {
        com.vividsolutions.jts.geom.Coordinate coord1 = convertCoordinateToJTS( box.getLowerCorner() );
        com.vividsolutions.jts.geom.Coordinate coord2 = convertCoordinateToJTS( box.getUpperCorner() );
        return JTS.toGeometry( new Envelope( coord1, coord2 ) ).toText();
    }

    public static String convertToMultiPointWKT( Collection<Position> points ) {
        GeometryFactory geometryFactory = new GeometryFactory();
        com.vividsolutions.jts.geom.Point[] pointArray = new com.vividsolutions.jts.geom.Point[points.size()];
        Iterator<Position> pointIterator = points.iterator();
        for ( int i = 0; i < points.size(); i++ ) {
            pointArray[i] = geometryFactory.createPoint( convertCoordinateToJTS( ((Point) pointIterator.next()).getCoordinate() ) );
        }
        return geometryFactory.createMultiPoint( pointArray ).toText();
    }

    public static String convertToMultiLineStringWKT( Collection<Position> lines ) {
        GeometryFactory geometryFactory = new GeometryFactory();
        com.vividsolutions.jts.geom.LineString[] lineArray = new com.vividsolutions.jts.geom.LineString[lines.size()];
        Iterator<Position> lineIterator = lines.iterator();
        for ( int i = 0; i < lines.size(); i++ ) {
            lineArray[i] = geometryFactory.createLineString( convertCoordinatesToJTS( ((Line) lineIterator.next()).getCoordinates() ) );
        }
        return geometryFactory.createMultiLineString( lineArray ).toText();
    }

    public static String convertToMultiPolygonWKT( Collection<Position> polygons ) {
        GeometryFactory geometryFactory = new GeometryFactory();
        com.vividsolutions.jts.geom.Polygon[] polygonArray = new com.vividsolutions.jts.geom.Polygon[polygons.size()];
        Iterator<Position> polygonIterator = polygons.iterator();
        for ( int i = 0; i < polygons.size(); i++ ) {
            Position position = polygonIterator.next();
            if ( position instanceof Box ) {
                Box box = (Box) position;
                com.vividsolutions.jts.geom.Coordinate coord1 = convertCoordinateToJTS( box.getLowerCorner() );
                com.vividsolutions.jts.geom.Coordinate coord2 = convertCoordinateToJTS( box.getUpperCorner() );
                polygonArray[i] = JTS.toGeometry( new Envelope( coord1, coord2 ) );
                //LinearRing ring = geometryFactory.createLinearRing( convertCoordinatesToJTS( box.getCoordinates() ) );
                //polygonArray[i] = geometryFactory.createPolygon( ring, new LinearRing[0] );
            } else {
                Polygon polygon = (Polygon) position;
                LinearRing ring = geometryFactory.createLinearRing( convertCoordinatesToJTS( polygon.getCoordinates() ) );
                polygonArray[i] = geometryFactory.createPolygon( ring, new LinearRing[0] );
            }

        }
        return geometryFactory.createMultiPolygon( polygonArray ).toText();
    }

    public static String convertToGeometryCollection( Collection<Position> positions ) {
        GeometryFactory geometryFactory = new GeometryFactory();
        com.vividsolutions.jts.geom.Geometry[] geometryArray = new com.vividsolutions.jts.geom.Geometry[positions.size()];
        Iterator<Position> positionIterator = positions.iterator();
        for ( int i = 0; i < positions.size(); i++ ) {
            Position position = (Position) positionIterator.next();
            if ( position instanceof Point ) {
                Point point = (Point) position;
                geometryArray[i] = geometryFactory.createPoint( convertCoordinateToJTS( point.getCoordinate() ) );
            } else if ( position instanceof Line ) {
                Line line = (Line) position;
                geometryArray[i] = geometryFactory.createLineString( convertCoordinatesToJTS( line.getCoordinates() ) );
            } else if ( position instanceof Box ) {
                Box box = (Box) position;
                com.vividsolutions.jts.geom.Coordinate coord1 = convertCoordinateToJTS( box.getLowerCorner() );
                com.vividsolutions.jts.geom.Coordinate coord2 = convertCoordinateToJTS( box.getUpperCorner() );
                geometryArray[i] = JTS.toGeometry( new Envelope( coord1, coord2 ) );
            } else {
                Polygon polygon = (Polygon) position;
                LinearRing ring = geometryFactory.createLinearRing( convertCoordinatesToJTS( polygon.getCoordinates() ) );
                geometryArray[i] = geometryFactory.createPolygon( ring, new LinearRing[0] );
            }

        }
        return geometryFactory.createGeometryCollection( geometryArray ).toText();
    }

    public static String convertToWKT( Position position ) {
        if ( position instanceof Point ) {
            return convertToWKT( (Point) position );
        } else if ( position instanceof Line ) {
            return convertToWKT( (Line) position );
        } else if ( position instanceof Polygon ) {
            return convertToWKT( (Polygon) position );
        } else if ( position instanceof Box ) {
            return convertToWKT( (Box) position );
        } else {
            LOGGER.warn( "No conversion available for the abdera geo position of type {}.", position.getClass().getName() );
            return null;
        }
    }

    public static String convertToWKT( Position[] positions ) {
        boolean isPoint = false;
        boolean isLine = false;
        boolean isPolygon = false;
        int uniqueGeos = 0;
        List<Position> geos = new ArrayList<Position>();
        for ( Position position : positions ) {
            if ( position instanceof Point ) {
                geos.add( position );
                if ( !isPoint ) {
                    uniqueGeos++;
                    isPoint = true;
                }
            } else if ( position instanceof Line ) {
                geos.add( position );
                if ( !isLine ) {
                    uniqueGeos++;
                    isLine = true;
                }
            } else if ( position instanceof Polygon ) {
                geos.add( position );
                if ( !isPolygon ) {
                    uniqueGeos++;
                    isPolygon = true;
                }
            } else if ( position instanceof Box ) {
                geos.add( position );
                if ( !isPolygon ) {
                    uniqueGeos++;
                    isPolygon = true;
                }
            } else {
                LOGGER.warn( "No conversion available for abdera geo position of type {}.", position.getClass().getName() );
            }
        }
        if ( geos.isEmpty() ) {
            return null;
        } else if ( geos.size() == 1 ) {
            return convertToWKT( geos.get( 0 ) );
        } else {
            if ( uniqueGeos > 1 ) {
                return convertToGeometryCollection( geos );
            } else if ( isPoint ) {
                return convertToMultiPointWKT( geos );
            } else if ( isLine ) {
                return convertToMultiLineStringWKT( geos );
            } else {
                return convertToMultiPolygonWKT( geos );
            }
        }
    }

    public static com.vividsolutions.jts.geom.Coordinate convertCoordinateToJTS( Coordinate coordinate ) {
        return new com.vividsolutions.jts.geom.Coordinate( coordinate.getLongitude(), coordinate.getLatitude() );
    }

    public static com.vividsolutions.jts.geom.Coordinate[] convertCoordinatesToJTS( Coordinates coordinates ) {
        List<com.vividsolutions.jts.geom.Coordinate> jtsCoords = new ArrayList<com.vividsolutions.jts.geom.Coordinate>();
        for ( Iterator<Coordinate> iterator = coordinates.iterator(); iterator.hasNext();) {
            jtsCoords.add( convertCoordinateToJTS( iterator.next() ) );
        }
        return jtsCoords.toArray( new com.vividsolutions.jts.geom.Coordinate[jtsCoords.size()] );
    }

}
