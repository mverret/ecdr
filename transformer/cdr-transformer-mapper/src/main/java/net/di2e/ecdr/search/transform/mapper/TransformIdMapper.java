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
package net.di2e.ecdr.search.transform.mapper;

/**
 * Interface to be used to help map the external 'format' values that are used on external interfaces like web services
 * to the transform/format values that are used internally by the DDF CatalogFramework to transform Metacard and Query
 * Responses
 */
public interface TransformIdMapper {

    String DEFAULT_DDF_ATOM_TRANSFORM_ID = "atom";
    String UPDATED_DDF_ATOM_TRANSFORM_ID = "ddf-atom";
    String CDR_ATOM_TRANSFORM_ID = "cdr-atom";
    String CDR_ATOM_WITH_PAYLOAD_TRANSFORM_ID = "atom-with-payload";

    String CDR_ATOM_DDMS_TRANSFORM_ID = "atom-ddms";
    String CDR_ATOM_DDMS20_TRANSFORM_ID = "atom-ddms-2.0";
    String CDR_ATOM_DDMS41_TRANSFORM_ID = "atom-ddms-4.1";
    String CDR_ATOM_DDMS50_TRANSFORM_ID = "atom-ddms-5.0";

    String DDMS20_METACARD_TRANSFORM = "ddms20";
    String DDMS41_METACARD_TRANSFORM = "ddms41";
    String DDMS50_METACARD_TRANSFORM = "ddms50";

    /**
     * Provides the internal QueryResponseTrnasformer identifier value for the corresponding String.
     * 
     * @param format
     *            external transform format
     * @return internal transform format String that can be used within DDF CatalogFramework
     */
    String getQueryResponseTransformValue( String format );

    /**
     * Provides the internal MetacardTransformer identifier value for the corresponding String.
     * 
     * @param format
     *            external transform format
     * @return internal transform format String that can be used within DDF CatalogFramework
     */
    String getMetacardTransformValue( String format );

}
