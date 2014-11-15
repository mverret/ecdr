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
package net.di2e.ecdr.commons.transform;

import java.util.HashMap;
import java.util.Map;

import net.di2e.ecdr.commons.util.TransformConstants;

/**
 * This is a simple POJO designed to handle the differences between the CDR endpoint format Strings that are used and
 * the internal format Strings that are used to lookup the correct transform.
 * 
 * This class came about because out of the box DDF uses the transform with ID 'atom' and the CDR spec uses that same
 * string ('atom') for the atom format in the spec. The DDF atom format does not align with the spec. So as not to
 * impact the existing DDF atom transform and the components that may be using it, this class was created and should be
 * used by CDR components before they do a transform.
 */
public class TransformIdMapper {

    private Map<String, String> transformFormats = new HashMap<String, String>();

    /**
     * Creates the initial default mappings which consist of ddf-atom -> atom atom -> cdr-atom
     */
    public TransformIdMapper() {
        transformFormats.put( TransformConstants.DEFAULT_DDF_ATOM_TRANSFORM_ID, TransformConstants.CDR_ATOM_TRANSFORM_ID );
        transformFormats.put( TransformConstants.UPDATED_DDF_ATOM_TRANSFORM_ID, TransformConstants.DEFAULT_DDF_ATOM_TRANSFORM_ID );
    }

    /**
     * Returns the mapped value if it exists, otherwise it just returns the value that was passed into the method
     * 
     * @param value
     *            - the value to lookup to see if an updated mapped value exists
     * @return the mapped value (or the value that was passed in, if there is no mapping for the value)
     */
    public String getMappedValue( String value ) {
        String mappedValue = value;
        if ( value != null && transformFormats.containsKey( value ) ) {
            mappedValue = transformFormats.get( value );
        } else {
            mappedValue = value;
        }
        return mappedValue;
    }

}
