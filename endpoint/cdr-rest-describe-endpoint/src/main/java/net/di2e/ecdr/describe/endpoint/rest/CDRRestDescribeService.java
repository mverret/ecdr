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
package net.di2e.ecdr.describe.endpoint.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.registry.api.RegistrableService;

/**
 * JAX-RS Web Service which implements the CDR REST Describe Specification
 */
@Path( "/" )
public class CDRRestDescribeService implements RegistrableService {

    private static final Logger LOGGER = LoggerFactory.getLogger( CDRRestDescribeService.class );

    private static final String RELATIVE_URL = "/services/cdr/describe/rest";
    private static final String SERVICE_TYPE = "CDR REST Describe Service";

    private ConfigurationWatcherImpl configWatcher = null;

    private String pathToDescribeFile = null;

    public CDRRestDescribeService( ConfigurationWatcherImpl config ) {
        configWatcher = config;
    }

    @HEAD
    public Response ping() {
        LOGGER.debug( "Ping (HTTP HEAD) was called to check if the CDR Describe Endpoint" );
        boolean isAvailable = false;
        if ( StringUtils.isNotBlank( pathToDescribeFile ) ) {
            if ( new File( pathToDescribeFile ).exists() ) {
                isAvailable = true;
            }
        }
        return isAvailable ? Response.ok().build() : Response.status( Response.Status.BAD_REQUEST ).build();
    }

    @GET
    @Produces( { "application/json", "application/xml", "text/xml" } )
    public Response describe() {
        LOGGER.debug( "Describe request sent, returning the description that is located at {}", pathToDescribeFile );
        if ( StringUtils.isNotBlank( pathToDescribeFile ) ) {
            try {
                FileInputStream fis = new FileInputStream( pathToDescribeFile );
                return Response.ok( fis, new MediaType( "application", "xml" ) ).build();
            } catch ( IOException e ) {
                LOGGER.warn( "The describe service could not read the file located at {} becuase encountered error {}", pathToDescribeFile, e.getMessage(), e );
            }
        }
        LOGGER.warn( "The describe service could not read the file located at {}, returning Internal Server Error Status {}", pathToDescribeFile, Response.Status.INTERNAL_SERVER_ERROR );
        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public String getServiceRelativeUrl() {
        return RELATIVE_URL;
    }

    @Override
    public String getServiceDescription() {
        return "Describes the Content Collections available through the CDR Search and Brokered interfaces";
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.EMPTY_MAP;
    }
    
    public void setPathToDescribeFile( String fileLocation ) {
        pathToDescribeFile = fileLocation;
    }

}
