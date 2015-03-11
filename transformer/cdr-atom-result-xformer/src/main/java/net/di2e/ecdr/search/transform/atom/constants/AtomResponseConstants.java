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
package net.di2e.ecdr.search.transform.atom.constants;

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
