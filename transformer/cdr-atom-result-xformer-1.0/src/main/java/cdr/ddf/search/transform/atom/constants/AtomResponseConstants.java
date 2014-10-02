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
package cdr.ddf.search.transform.atom.constants;

public final class AtomResponseConstants {

    private AtomResponseConstants() {
    }

    public static final String CDRB_NAMESPACE = "urn:cdr:broker:2.0";
    public static final String CDRB_NAMESPACE_PREFIX = "cdrb";
    public static final String CDRS_EXT_NAMESPACE = "urn:cdr-ex:search:1.1";
    public static final String CDRS_EXT_NAMESPACE_PREFIX = "cdrsx";

    public static final String GEORSS_NAMESPACE = "http://www.georss.org/georss";
    public static final String GEORSS_NAMESPACE_PREFIX = "georss";
    public static final String RELEVANCE_NAMESPACE = "http://a9.com/-/opensearch/extensions/relevance/1.0/";
    public static final String RELEVANCE_NAMESPACE_PREFIX = "relevance";
    public static final String RELEVANCE_ELEMENT = "score";
    public static final String RESULT_SOURCE_ELEMENT = "resultSource";
    public static final String RESULT_SOURCE_ATTRIBUTE = "sourceId";
    public static final String DISTANCE_ELEMENT = "distance";

    public static final String METACARD_ATOM_NAMESPACE = "urn:catalog:metacard:atom";
    public static final String METACARD_ATOM_NAMESPACE_PREFIX = "metacard";
    public static final String METACARD_CREATED_DATE_ELEMENT = "createdDate";
    public static final String METADATA_EXPIRATION_DATE_ELEMENT = "expirationDate";

    public static final String ATOM_MIME_TYPE = "application/atom+xml";

}
