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

    private String thumbnailLinkRelation = null;
    private String metadataLinkRelation = null;
    private String productLinkRelation = null;

    private boolean zeroBasedStartIndex = false;
    private boolean proxyProductUrl = false;
    private boolean wrapXml = false;

    public FilterConfig() {
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

    public String getThumbnailLinkRelation() {
        return thumbnailLinkRelation;
    }

    public void setThumbnailLinkRelation( String rel ) {
        thumbnailLinkRelation = rel;
    }

    public void setProxyProductUrl( boolean proxy ) {
        proxyProductUrl = proxy;
    }

    public boolean isProxyProductUrl() {
        return proxyProductUrl;
    }

    public void setWrapMetadata( boolean wrap ) {
        wrapXml = wrap;
    }

    public boolean isWrapMetadata() {
        return wrapXml;
    }

    public void setZeroBasedStartIndex( boolean zeroBased ) {
        zeroBasedStartIndex = zeroBased;
    }

    public boolean isZeroBasedStartIndex() {
        return zeroBasedStartIndex;
    }

}
