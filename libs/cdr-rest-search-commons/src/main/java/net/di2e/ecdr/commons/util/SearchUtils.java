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
package net.di2e.ecdr.commons.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class that contains helper methods.
 */
public final class SearchUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger( SearchUtils.class );

    private static final String MAP_ENTRY_DELIMITER = "=";

    private SearchUtils() {
    }

    public static Map<String, String> convertToMap( String mapStr ) {
        Map<String, String> inputMap = new HashMap<String, String>();
        if ( StringUtils.isNotBlank( mapStr ) ) {
            inputMap = convertToMap( Arrays.asList(mapStr.split( "," )));
        }
        return inputMap;
    }

    public static Map<String, String> convertToMap( List<String> mapList ) {
        Map<String, String> inputMap = new HashMap<String, String>();
        for ( String sortPair : mapList ) {
            String[] pairAry = sortPair.split( MAP_ENTRY_DELIMITER );
            if ( pairAry.length == 2 ) {
                inputMap.put( pairAry[0], pairAry[1] );
            } else {
                LOGGER.warn( "Could not parse out map entry from {}, skipping this item.", sortPair );
            }
        }
        return inputMap;
    }
}
