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
package net.di2e.ecdr.endpoint.sort;

import net.di2e.ecdr.commons.sort.SortTypeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SortTypeConfigurationImpl implements SortTypeConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger( SortTypeConfigurationImpl.class );

    private String sortKey;

    private String sortAttribute;

    private String customAttribute;

    private String sortOrder;

    public SortTypeConfigurationImpl() {
        LOGGER.info( "Creating a new sort type configuration." );
    }

    public void setSortKey (String key) {
        this.sortKey = key;
    }

    public void setSortAttribute (String attribute) {
        this.sortAttribute = attribute;
    }

    public void setCustomSortAttribute ( String attribute ) {
        this.customAttribute = attribute;
    }

    public void setSortOrder (String order) {
        this.sortOrder = order;
    }

    @Override
    public String getSortKey() {
        return sortKey;
    }

    @Override
    public String getSortAttribute() {
        if (StringUtils.isNotBlank( customAttribute )) {
            return customAttribute;
        } else {
            return sortAttribute;
        }
    }

    @Override
    public String getSortOrder() {
        return sortOrder;
    }

}
