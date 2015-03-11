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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple POJO designed to handle the differences between the CDR endpoint format Strings that are used and
 * the internal format Strings that are used to lookup the correct transform.
 * 
 * This class came about because out of the box DDF uses the transform with ID 'atom' and the CDR spec uses that same
 * string ('atom') for the atom format in the spec. The DDF atom format does not align with the spec. So as not to
 * impact the existing DDF atom transform and the components that may be using it, this class was created and should be
 * used by CDR components before they do a transform.
 */
public class TransformIdMapperImpl implements TransformIdMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger( TransformIdMapperImpl.class );

    private Map<String, String> queryTransformFormats = new HashMap<String, String>();
    private Map<String, String> metacardTransformFormats = new HashMap<String, String>();

    /**
     * Creates the initial default mappings which consist of ddf-atom -> atom atom -> cdr-atom
     */
    public TransformIdMapperImpl() {
        queryTransformFormats.put( DEFAULT_DDF_ATOM_TRANSFORM_ID, CDR_ATOM_TRANSFORM_ID );
        queryTransformFormats.put( UPDATED_DDF_ATOM_TRANSFORM_ID, DEFAULT_DDF_ATOM_TRANSFORM_ID );
        queryTransformFormats.put( CDR_ATOM_DDMS_TRANSFORM_ID, CDR_ATOM_WITH_PAYLOAD_TRANSFORM_ID );
        queryTransformFormats.put( CDR_ATOM_DDMS20_TRANSFORM_ID, CDR_ATOM_WITH_PAYLOAD_TRANSFORM_ID );
        queryTransformFormats.put( CDR_ATOM_DDMS41_TRANSFORM_ID, CDR_ATOM_WITH_PAYLOAD_TRANSFORM_ID );
        queryTransformFormats.put( CDR_ATOM_DDMS50_TRANSFORM_ID, CDR_ATOM_WITH_PAYLOAD_TRANSFORM_ID );

        metacardTransformFormats.put( CDR_ATOM_DDMS20_TRANSFORM_ID, DDMS20_METACARD_TRANSFORM );
        metacardTransformFormats.put( CDR_ATOM_DDMS41_TRANSFORM_ID, DDMS41_METACARD_TRANSFORM );
        metacardTransformFormats.put( CDR_ATOM_DDMS50_TRANSFORM_ID, DDMS50_METACARD_TRANSFORM );
    }

    /**
     * Returns the mapped value of the QueryResponseTransformer, otherwise it just returns the value that was passed
     * into the method
     * 
     * @param value
     *            - the value to lookup to see if an updated mapped value exists
     * @return the mapped value (or the value that was passed in, if there is no mapping for the value)
     */
    @Override
    public String getQueryResponseTransformValue( String value ) {
        String xformValue = getValue( queryTransformFormats, value, false );
        LOGGER.debug( "Returning the QueryResponseTransformer name [{}] for original value [{}]", xformValue, value );
        return xformValue;
    }

    /**
     * Set the QueryResponseTransformer mapped values to be used by the CDR endpoints. The list of values must be in the
     * format name=value (where name = the external service format/transform value, and value is the internal DDF value
     * 
     * @param values
     *            the list of mapped values
     */
    public void setQueryResponseTransformValues( List<String> values ) {
        queryTransformFormats.clear();
        LOGGER.debug( "Updating the CDR QueryResponseTransformer value mapper to [{}]", values );
        setValues( values, queryTransformFormats );
    }

    /**
     * Returns the mapped value of the MetacardTransformer, otherwise it just returns the value that was passed into the
     * method
     * 
     * @param value
     *            - the value to lookup to see if an updated mapped value exists
     * @return the mapped value (or the value that was passed in, if there is no mapping for the value)
     */
    @Override
    public String getMetacardTransformValue( String value ) {
        String xformValue = getValue( metacardTransformFormats, value, true );
        LOGGER.debug( "Returning the MetacardTransformer name [{}] for original value [{}]", xformValue, value );
        return xformValue;
    }

    /**
     * Set the MetacardTransformer mapped values to be used by the CDR endpoints. The list of values must be in the
     * format name=value (where name = the external service format/transform value, and value is the internal DDF value
     * for the MetacardTransformer. For example atom-ddms-2.0=ddms20
     * 
     * @param values
     *            the list of mapped values
     */
    public void setMetacardTransformValues( List<String> values ) {
        metacardTransformFormats.clear();
        LOGGER.debug( "Updating the CDR MetacardTransformer value mapper to [{}]", values );
        setValues( values, metacardTransformFormats );
    }

    protected String getValue( Map<String, String> valueMap, String value, boolean allowNull ) {
        String mappedValue = null;
        if ( value != null && valueMap.containsKey( value ) ) {
            mappedValue = valueMap.get( value );
        } else if ( !allowNull ) {
            mappedValue = value;
        }
        return mappedValue;
    }

    protected void setValues( List<String> values, Map<String, String> valueMap ) {
        if ( values != null ) {
            for ( String value : values ) {
                String[] valueArray = value.split( "=" );
                if ( valueArray.length == 2 ) {
                    valueMap.put( valueArray[0], valueArray[1] );
                    LOGGER.debug( "Adding Transformer map name[{}] value[{}]", valueArray[0], valueArray[1] );
                } else {
                    LOGGER.warn( "Could not set the CDR Transformer Mapped Values because it was an invalid format, must be in the format [name=value] and was [{}]", value );
                }
            }
        }
    }

}
