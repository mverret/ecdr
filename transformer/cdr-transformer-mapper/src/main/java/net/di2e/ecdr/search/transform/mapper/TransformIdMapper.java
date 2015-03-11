/**
 * Copyright (C) 2014 Cohesive Integrations, LLC (info@cohesiveintegrations.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
