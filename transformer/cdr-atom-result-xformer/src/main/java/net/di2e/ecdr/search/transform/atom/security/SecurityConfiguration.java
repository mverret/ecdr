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
package net.di2e.ecdr.search.transform.atom.security;

import java.util.Map;
import java.util.Set;

/**
 * Interface that details a simple security configurations. Can be used to populate entries with pre-set information.
 */
public interface SecurityConfiguration {

    /**
     * Used for fallback configurations, only one configuration should use this format
     */
    String DEFAULT_FORMAT_CONFIGURATION = "default";

    Set<String> getFormats();

    String getNamespace();

    Map<String, String> getAttributes();

}
