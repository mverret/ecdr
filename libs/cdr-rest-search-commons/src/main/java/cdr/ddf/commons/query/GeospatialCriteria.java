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
package cdr.ddf.commons.query;

public class GeospatialCriteria {
    
    public enum SpatialOperator {
        Contains, Overlaps
    }

    private SpatialOperator spatialOp = null;

    private Double radius;
    private Double longitude;
    private Double latitude;
    private String bboxWKT;
    private String geometryWKT;

    private boolean isBBox = false;
    private boolean isPointRadius = false;

    public GeospatialCriteria( double lat, double lon, double rad ) {
        // if ( rad == null || lon == null || lat == null ) {
        // throw new IllegalArgumentException( "Null is not valid for lat [" + lat + "], lon [" + lon +
        // "], and/or radius [" + rad + "]" );
        // }
        this.radius = rad;
        this.longitude = lon;
        this.latitude = lat;
        spatialOp = SpatialOperator.Overlaps;
    }

    public GeospatialCriteria( double minX, double minY, double maxX, double maxY, SpatialOperator operator ) {
        if ( operator == null ) {
            throw new IllegalArgumentException( "SpatialOperator cannot be null when creating GeospatialCriteria" );
        }
        spatialOp = operator;
        StringBuilder wktBuilder = new StringBuilder( "POLYGON((" );
        wktBuilder.append( minX + " " + minY );
        wktBuilder.append( "," + minX + " " + maxY );
        wktBuilder.append( "," + maxX + " " + maxY );
        wktBuilder.append( "," + maxX + " " + minY );
        wktBuilder.append( "," + minX + " " + minY );
        wktBuilder.append( "))" );
        bboxWKT = wktBuilder.toString();
        isBBox = true;
        
    }

    public GeospatialCriteria( double minX, double minY, double maxX, double maxY ) {
        this( minX, minY, maxX, maxY, SpatialOperator.Overlaps );
    }

    public GeospatialCriteria( String wkt, SpatialOperator operator ) {
        if ( operator == null ) {
            throw new IllegalArgumentException( "SpatialOperator cannot be null when creating GeospatialCriteria" );
        }
        spatialOp = operator;
        this.geometryWKT = wkt;
    }

    public GeospatialCriteria( String wkt ) {
        this( wkt, SpatialOperator.Overlaps );
    }

    public Double getRadius() {
        return radius;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getBBoxWKT() {
        return bboxWKT;
    }

    public String getGeometryWKT() {
        return geometryWKT;
    }

    public boolean isBBox() {
        return isBBox;
    }

    public boolean isPointRadius() {
        return isPointRadius;
    }

    public SpatialOperator getSpatialOperator() {
        return spatialOp;
    }

    public void setSpatialOperator( SpatialOperator operator ) {
        spatialOp = operator;
    }

}
