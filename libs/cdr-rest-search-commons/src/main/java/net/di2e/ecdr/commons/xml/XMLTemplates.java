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
package net.di2e.ecdr.commons.xml;

public final class XMLTemplates {

    private XMLTemplates () {

    }

    public static final String OSD_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!--\n"
        + "\n"
        + "Request Properties \n"
        + "******************n"
        + "os:count - number of items to return per request (default: count=20)\n"
        + "os:startIndex - number of result to start response with. (Example: startIndex of 5 would return the 5th ranked result. default: startIndex=1)\n"
        + "\n"
        + "fs:routeTo - comma delimited list of data sources to query (default: all sources)\n"
        + "fs:sort - specifies sort by field as sort=<sbfield>:<sborder>, where <sbfield> may be 'date', 'title' or 'relevance' (default is 'relevance'). The conditional param <sborder> is "
        + "optional but has a value of 'asc' or 'desc' (default is 'desc'). When <sbfield> is 'relevance', <sborder> must be 'desc'.\n"
        + "fs:format - format to return the response in (default: atom-ddms).\n"
        + "\n"
        + "****Criteria Types**** \n"
        + "\n"
        + "\n"
        + "Contextual\n"
        + "******************n"
        + "os:searchTerms - search terms\n"
        + "\n"
        + "Spatial\n"
        + "******************n"
        + "geo:box - comma delimited list of lat/lon (deg) bounding box coordinates (geo format: geo:bbox ~ west,south,east,north). This is also commonly referred to by minX, minY, maxX, "
        + "maxY (where longitude is the X-axis, and latitude is the Y-axis).\n"
        + "geo:lat/lon - latititude & longitude, respectively, in deceimal degrees (typical GPS receiver WGS84 coordinates). Should include a 'radius' parameter that specifies the search "
        + "radius in meters.\n"
        + "geo:radius - the radius (m) parameter, used with the lat and lon paramters, specifies the search distance from this point (default: radius=5000).\n"
        + "\n"
        + "Temporal\n"
        + "******************n"
        + "time:start - replaced with a string of the beginning of the time slice of the search (RFC-3339 - Date and Time format, i.e. YYYY-MM-DDTHH:mm:ssZ). Default value of "
        + "\"1970-01-01T00:00:00Z\" is used when dtend is indicated but dtstart is not specified.\n"
        + "time:end - replaced with a string of the ending of the time slice of the search (RFC-3339 - Date and Time format, i.e. YYYY-MM-DDTHH:mm:ssZ). Current GMT date/time is used when "
        + "dtstart is specified but not dtend.\n"
        + "\n"
        + "\n"
        + "Sample Response:\n"
        + "******************\n"
        + "\n"
        + "\n"
        + "-->";
}
