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
package net.di2e.ecdr.commons.filter.config;


public class FilterConfig {

    public enum SingleRecordQueryMethod {
        ID_ELEMENT_URL, UID_PARAMETER
    }

    private SingleRecordQueryMethod singleRecordQueryMethod = null;
    private String metadataLinkRelation = null;
    private String productLinkRelation = null;
    
    private boolean zeroBasedStartIndex = false;
    private boolean localUrls = false;

    public FilterConfig() {
    }

    public SingleRecordQueryMethod getSingleRecordQueryMethod() {
        return singleRecordQueryMethod;
    }

    public void setSingleRecordQueryMethod( SingleRecordQueryMethod queryMethod ) {
        this.singleRecordQueryMethod = queryMethod;
    }

    public void setMetadataLinkRelation( String rel ) {
        metadataLinkRelation = rel;
    }

    public String getMetadataLinkRelation() {
        return metadataLinkRelation;
    }

    public void setProductLinkRelation( String rel ) {
        productLinkRelation = rel;
    }

    public String getProductLinkRelation() {
        return productLinkRelation;
    }
    
    public void setProvideLocalUrls( boolean proxy ){
        localUrls = proxy;
    }
    
    public boolean isProvideLocalUrls(){
        return localUrls;
    }
    
    public void setZeroBasedStartIndex( int index ){
        zeroBasedStartIndex = index == 0;
    }
    
    public boolean isZeroBasedStartIndex(){
        return zeroBasedStartIndex;
    }

}
