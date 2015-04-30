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

import java.io.IOException;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigAdminUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger( ConfigAdminUtils.class );

    private ConfigAdminUtils() {
    }

    public static boolean configurationPidExists( ConfigurationAdmin configAdmin, String pid ) throws IOException, InvalidSyntaxException {
        boolean configExists = false;
        String filter = "(" + ConfigurationAdmin.SERVICE_FACTORYPID + "=" + pid + ")";
        Configuration[] config = configAdmin.listConfigurations( filter );
        if ( config != null && config.length > 0 ) {
            configExists = true;
        }
        LOGGER.debug( "The {} configuration returned {} services so return {} for configExists method", pid, config == null ? 0 : config.length, configExists );
        return configExists;
    }

}
