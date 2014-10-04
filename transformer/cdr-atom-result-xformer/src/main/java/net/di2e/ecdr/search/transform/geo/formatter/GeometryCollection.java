/**
 * Copyright (c) Cohesive Integrations, LLC
 * Copyright (c) Codice Foundation
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.abdera.ext.geo.Position;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryCollection extends MultiPolygon {

    public static final String TYPE = "GeometryCollection";

    public GeometryCollection( Geometry geometry ) {
        super( geometry );
    }

    public static CompositeGeometry toCompositeGeometry( List geometries ) {
        Geometry[] allGeometries = new Geometry[geometries.size()];

        for ( int i = 0; i < allGeometries.length; i++ ) {
            Map jsonGeometry = (Map) geometries.get( i );
            allGeometries[i] = getCompositeGeometry( jsonGeometry.get( TYPE_KEY ).toString(), jsonGeometry ).getGeometry();
        }
        return new GeometryCollection( GEOMETRY_FACTORY.createGeometryCollection( allGeometries ) );
    }

    @Override
    public Map toJsonMap() {
        Map map = new HashMap();

        if ( TYPE.equals( getGeometry().getGeometryType() ) ) {

            map.put( TYPE_KEY, TYPE );

            List<Map> listOfGeometries = new ArrayList<Map>();

            for ( int i = 0; i < getGeometry().getNumGeometries(); i++ ) {
                listOfGeometries.add( getCompositeGeometry( getGeometry().getGeometryN( i ) ).toJsonMap() );
            }

            map.put( GEOMETRIES_KEY, listOfGeometries );

        } else {
            throw new UnsupportedOperationException( "Geometry is not a " + TYPE );
        }

        return map;

    }

    @Override
    public List<Position> toGeoRssPositions() {

        List<Position> positions = new ArrayList<Position>();

        for ( int i = 0; i < getGeometry().getNumGeometries(); i++ ) {

            CompositeGeometry compositeGeo = CompositeGeometry.getCompositeGeometry( getGeometry().getGeometryN( i ) );

            positions.addAll( compositeGeo.toGeoRssPositions() );
        }

        return positions;

    }

}
