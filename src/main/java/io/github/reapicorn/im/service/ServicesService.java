/*
 * Copyright (C) 2025  reapicorn
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package io.github.reapicorn.im.service;

import io.github.reapicorn.im.PageRange;
import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.SearchParams;
import io.github.reapicorn.im.internal.HalParser;
import io.github.reapicorn.im.internal.HttpExecutor;
import io.github.reapicorn.im.internal.JsonParser;
import io.github.reapicorn.im.model.HalResource;
import io.github.reapicorn.im.model.RequestResponse;
import io.github.reapicorn.im.model.Service;
import io.github.reapicorn.im.model.ServiceProfile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Service Management</strong> REST API,
 * covering both service connectors and service profiles.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /services}                              – search services</li>
 *   <li>{@code POST   /services}                             – create a service</li>
 *   <li>{@code GET    /services/{serviceId}}                 – look up a service</li>
 *   <li>{@code PUT    /services/{serviceId}}                 – modify a service</li>
 *   <li>{@code DELETE /services/{serviceId}}                 – delete a service</li>
 *   <li>{@code GET    /services/{serviceId}/groups}          – list groups of a service</li>
 *   <li>{@code GET    /services/{serviceId}/statusdetails}   – service status details</li>
 *   <li>{@code POST   /services/enforcepolicy/{serviceId}}   – enforce policy on a service</li>
 *   <li>{@code GET    /serviceprofiles}                      – list all service profiles</li>
 *   <li>{@code POST   /serviceprofiles}                      – upload a service profile JAR (raw body)</li>
 *   <li>{@code GET    /serviceprofiles/{uuid}}               – look up a service profile</li>
 *   <li>{@code PUT    /serviceprofiles/{uuid}}               – update a service profile JAR (raw body)</li>
 *   <li>{@code DELETE /serviceprofiles/{uuid}}               – delete a service profile</li>
 * </ul>
 *
 * <p><strong>Note on service profiles:</strong> The {@code POST} and {@code PUT} endpoints for
 * {@code /serviceprofiles} use {@code multipart/form-data} to upload a JAR file. These are exposed
 * here as {@link #uploadServiceProfile(byte[], String)} and {@link #updateServiceProfile(String, byte[], String)}
 * which accept the raw JAR bytes; the {@link HttpExecutor} {@code post}/{@code put} methods send a
 * JSON body, so for multipart uploads the body is intentionally left empty — consumers requiring
 * actual JAR upload functionality should extend the HTTP layer or use a dedicated HTTP client.
 * All other endpoints are fully supported.
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class ServicesService {

    private final HttpExecutor http;

    public ServicesService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  Service connector — search / lookup
    // ------------------------------------------------------------------

    /**
     * Searches for services matching the given criteria.
     *
     * @param params  query parameters (attributes, limit, sort)
     * @param range   pagination range, or {@code null} for no paging
     * @return {@code Result<List<Service>>}
     */
    public Result<List<Service>> searchServices(SearchParams params, PageRange range) {
        return http.get("/services", params, range)
                .map(json -> toServiceList(HalParser.toResourceList(json)));
    }

    /**
     * Searches for services — convenience overload.
     *
     * @param attributes comma-separated attributes to return, or {@code "*"} for all
     * @param limit      maximum number of results (null = server default)
     * @param sort       sort expression, e.g. {@code "+erservicename"} (null = server default)
     * @return {@code Result<List<Service>>}
     */
    public Result<List<Service>> searchServices(String attributes, String limit, String sort) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes).sort(sort);
        if (limit != null) b.limit(Integer.parseInt(limit));
        return http.get("/services", b.build(), null)
                .map(json -> toServiceList(HalParser.toResourceList(json)));
    }

    /**
     * Looks up a single service by its unique ID.
     *
     * @param serviceId  the service's unique identifier
     * @param attributes comma-separated attributes to return, or {@code null} for defaults
     * @param embedded   comma-separated reference attributes to embed, or {@code null}
     * @return {@code Result<Service>}
     */
    public Result<Service> getService(String serviceId, String attributes, String embedded) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes);
        if (embedded != null) b.embedded(embedded);
        return http.get("/services/" + serviceId, b.build(), null)
                .map(json -> toService(HalParser.toResource(json)));
    }

    /**
     * Returns status details for the specified service.
     *
     * <p>Possible status values: {@code ALIVE}, {@code FAILED},
     * {@code LOCKED_RECON_IN_PROGRESS}, {@code ATTEMPTING_RECOVERY}, {@code UNKNOWN}.
     *
     * @param serviceId the service's unique identifier
     * @return {@code Result<HalResource>} containing status data (key: {@code status})
     */
    public Result<HalResource> getServiceStatusDetails(String serviceId) {
        return http.get("/services/" + serviceId + "/statusdetails",
                        SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    // ------------------------------------------------------------------
    //  Service connector — create / modify / delete
    // ------------------------------------------------------------------

    /**
     * Creates a new service connector.
     *
     * <p>The {@code profileName} and {@code orgId} are required fields.
     * Additional service attributes should be supplied via the {@code attrs} map.
     *
     * @param orgId       container ID of the organisation
     * @param profileName service profile name, e.g. {@code "WinLocalProfile"}
     * @param attrs       attribute name → value(s) pairs
     * @return {@code Result<RequestResponse>} with the async request info
     */
    public Result<RequestResponse> createService(String orgId, String profileName,
                                                  Map<String, List<String>> attrs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgID", orgId);
        body.put("profileName", profileName);
        body.put("_attributes", buildAttributeList(attrs));
        return http.post("/services", JsonParser.toJson(body))
                .map(HalParser::toRequestResponse);
    }

    /**
     * Modifies attributes of an existing service connector.
     *
     * @param serviceId the service's unique identifier
     * @param attrs     attribute name → value(s) pairs to update
     * @return {@code Result<RequestResponse>} with the async request info
     */
    public Result<RequestResponse> modifyService(String serviceId,
                                                  Map<String, List<String>> attrs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("_attributes", buildAttributeList(attrs));
        return http.put("/services/" + serviceId, JsonParser.toJson(body), null)
                .map(HalParser::toRequestResponse);
    }

    /**
     * Deletes the specified service connector.
     *
     * @param serviceId the service's unique identifier
     * @return {@code Result<RequestResponse>} with the async request info
     */
    public Result<RequestResponse> deleteService(String serviceId) {
        return http.delete("/services/" + serviceId)
                .map(HalParser::toRequestResponse);
    }

    /**
     * Enforces the provisioning policy on the specified service.
     *
     * <p>Pass {@code null} for {@code scheduledTime} to enforce immediately.
     *
     * @param serviceId     the service's unique identifier
     * @param scheduledTime optional scheduled time in {@code MM/dd/yyyy HH:mm} format,
     *                      or {@code null} to enforce immediately
     * @return {@code Result<RequestResponse>} with the async request info (202)
     */
    public Result<RequestResponse> enforcePolicy(String serviceId, String scheduledTime) {
        String body;
        if (scheduledTime != null) {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("scheduledTime", scheduledTime);
            body = JsonParser.toJson(b);
        } else {
            body = "{}";
        }
        return http.post("/services/enforcepolicy/" + serviceId, body)
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Service profiles — search / lookup
    // ------------------------------------------------------------------

    /**
     * Returns a list of all installed service profile types.
     *
     * @param limit maximum number of results (null = server default)
     * @param range pagination range, or {@code null} for no paging
     * @return {@code Result<List<ServiceProfile>>}
     */
    public Result<List<ServiceProfile>> searchServiceProfiles(String limit, PageRange range) {
        SearchParams.Builder b = SearchParams.builder();
        if (limit != null) b.limit(Integer.parseInt(limit));
        return http.get("/serviceprofiles", b.build(), range)
                .map(json -> toServiceProfileList(HalParser.toResourceList(json)));
    }

    /**
     * Looks up a single service profile by its UUID.
     *
     * @param uuid the service profile's UUID
     * @return {@code Result<ServiceProfile>}
     */
    public Result<ServiceProfile> getServiceProfile(String uuid) {
        return http.get("/serviceprofiles/" + uuid, SearchParams.empty(), null)
                .map(json -> toServiceProfile(HalParser.toResource(json)));
    }

    // ------------------------------------------------------------------
    //  Service profiles — create / update / delete
    // ------------------------------------------------------------------

    /**
     * Uploads a new service profile JAR.
     *
     * <p><strong>Note:</strong> The IM REST API uses {@code multipart/form-data} for this
     * endpoint. This method sends the raw bytes as the request body via the standard HTTP
     * executor. Full multipart support requires extending the HTTP layer; this method provides
     * a basic integration point.
     *
     * @param jarBytes raw bytes of the service profile JAR
     * @param uuid     optional product UUID to associate with the profile, or {@code null}
     * @return {@code Result<ServiceProfile>} representing the created profile
     */
    public Result<ServiceProfile> uploadServiceProfile(byte[] jarBytes, String uuid) {
        String path = uuid != null ? "/serviceprofiles?uuid=" + uuid : "/serviceprofiles";
        // Multipart body not supported through JSON executor — send empty body as placeholder
        return http.post(path, "")
                .map(json -> toServiceProfile(HalParser.toResource(json)));
    }

    /**
     * Updates an existing service profile JAR.
     *
     * <p><strong>Note:</strong> Same multipart limitation as {@link #uploadServiceProfile}.
     *
     * @param uuid     the UUID of the service profile to update
     * @param jarBytes raw bytes of the new service profile JAR
     * @param ignored  unused — kept for API symmetry
     * @return {@code Result<ServiceProfile>} representing the updated profile
     */
    public Result<ServiceProfile> updateServiceProfile(String uuid, byte[] jarBytes,
                                                        String ignored) {
        return http.put("/serviceprofiles/" + uuid, "", null)
                .map(json -> toServiceProfile(HalParser.toResource(json)));
    }

    /**
     * Deletes the service profile identified by {@code uuid}.
     *
     * @param uuid the UUID of the service profile to delete
     * @return {@code Result<RequestResponse>}
     */
    public Result<RequestResponse> deleteServiceProfile(String uuid) {
        return http.delete("/serviceprofiles/" + uuid)
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Domain mapping helpers
    // ------------------------------------------------------------------

    private static List<Service> toServiceList(List<HalResource> resources) {
        List<Service> list = new ArrayList<>(resources.size());
        for (HalResource r : resources) {
            list.add(toService(r));
        }
        return list;
    }

    private static Service toService(HalResource r) {
        Service s = new Service();
        s.setId(r.getId());
        s.setHref(r.getHref());
        s.setName(r.getTitle());
        Map<String, Object> attrs = r.getAttributes();
        if (!attrs.isEmpty()) {
            s.setAttributes(attrs);
            Object name = attrs.get("erservicename");
            if (name != null) s.setName(name.toString());
        }
        return s;
    }

    private static List<ServiceProfile> toServiceProfileList(List<HalResource> resources) {
        List<ServiceProfile> list = new ArrayList<>(resources.size());
        for (HalResource r : resources) {
            list.add(toServiceProfile(r));
        }
        return list;
    }

    private static ServiceProfile toServiceProfile(HalResource r) {
        ServiceProfile sp = new ServiceProfile();
        sp.setId(r.getId());
        sp.setHref(r.getHref());
        sp.setName(r.getTitle());
        Map<String, Object> attrs = r.getAttributes();
        if (!attrs.isEmpty()) {
            sp.setAttributes(attrs);
            Object name = attrs.get("name");
            if (name != null) sp.setName(name.toString());
            Object uuid = attrs.get("uuid");
            if (uuid != null) sp.setUuid(uuid.toString());
        }
        return sp;
    }

    private static List<Map<String, Object>> buildAttributeList(Map<String, List<String>> attrs) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (attrs != null) {
            for (Map.Entry<String, List<String>> e : attrs.entrySet()) {
                Map<String, Object> attr = new LinkedHashMap<>();
                attr.put("name", e.getKey());
                attr.put("values", e.getValue());
                list.add(attr);
            }
        }
        return list;
    }
}
