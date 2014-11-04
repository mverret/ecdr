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
package net.di2e.ecdr.registry.internal.rest;

import net.di2e.ecdr.registry.ServiceInfo;
import net.di2e.ecdr.registry.ServiceRegistry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/")
public class ServiceRegistryEndpoint {

    private static final String JSON_MIME = "application/json";

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryEndpoint.class);

    private ServiceRegistry serviceRegistry;

    public ServiceRegistryEndpoint(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @HEAD
    public Response ping() {
        LOGGER.debug("Ping (HTTP HEAD) was called to check if the Registry endpoint is available.");
        return Response.ok().build();
    }

    @GET
    public Response getServices(@Context UriInfo uriInfo) {
        List<ServiceInfo> services = serviceRegistry.getServices();
        JSONArray servicesJSON = new JSONArray();
        for (ServiceInfo curService : services) {
            servicesJSON.put(convertServiceInfo(curService, uriInfo));
        }
        return Response.ok(servicesJSON.toString(), JSON_MIME).build();
    }

    /**
     * Converts service information into a JSON object.
     *
     * @param service Information on the service.
     * @param uriInfo URI from the calling context.
     * @return JSON object describing the service.
     */
    private JSONObject convertServiceInfo(ServiceInfo service, UriInfo uriInfo) {
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ServiceInfo.TYPE_KEY, service.getServiceType());
        propertyMap.put(ServiceInfo.DESCRIPTION_KEY, service.getServiceDescription());
        propertyMap.put(ServiceInfo.URL_KEY, combineUrl(uriInfo.getBaseUri(), service.getServiceRelativeUrl()));
        propertyMap.put(ServiceInfo.PROPERTIES_KEY, service.getProperties());
        return new JSONObject(propertyMap);
    }

    /**
     * Combines the URI and relative URL to form a full URL for external clients to use.
     *
     * @param registryUri URI from the calling context
     * @param relativeUrl URL Relative URL for a service
     * @return Absolute URL for the service
     */
    private String combineUrl(URI registryUri, String relativeUrl) {
        StringBuilder serviceUrl = new StringBuilder();
        serviceUrl.append(registryUri.getScheme());
        serviceUrl.append("://");
        serviceUrl.append(registryUri.getHost());
        serviceUrl.append(":");
        serviceUrl.append(registryUri.getPort());
        serviceUrl.append(relativeUrl);
        return serviceUrl.toString();
    }
}
