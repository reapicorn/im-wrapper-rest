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
import io.github.reapicorn.im.model.OrgCategory;
import io.github.reapicorn.im.model.OrgContainer;
import io.github.reapicorn.im.model.RequestResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Organizational Container</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /organizationcontainers}                          – search all containers</li>
 *   <li>{@code GET    /organizationcontainers/{category}}               – search by category</li>
 *   <li>{@code POST   /organizationcontainers/{category}}               – create a container</li>
 *   <li>{@code GET    /organizationcontainers/{category}/{id}}          – look up a container</li>
 *   <li>{@code PUT    /organizationcontainers/{category}/{id}}          – modify a container</li>
 *   <li>{@code DELETE /organizationcontainers/{category}/{id}}          – delete a container</li>
 *   <li>{@code PUT    /organizationcontainers/{id}}                     – transfer (re-parent) a container</li>
 * </ul>
 *
 * <p>The {@code {category}} path parameter is represented by the {@link OrgCategory} enum.
 * Valid values: {@code organizations}, {@code organizationunits}, {@code locations},
 * {@code bporganizations}, {@code admindomains}.
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class OrganizationsService {

    private final HttpExecutor http;

    public OrganizationsService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  Search / lookup
    // ------------------------------------------------------------------

    /**
     * Returns a list of all organisational containers across all categories.
     *
     * <p>Optional filter parameters ({@code name}, {@code description}, {@code type}) can be
     * passed via the {@link SearchParams} attributes field or as additional query params.
     * For the simplest use case, pass {@link SearchParams#empty()}.
     *
     * @param params  query parameters (attributes); use {@link SearchParams#empty()} for none
     * @return {@code Result<List<OrgContainer>>}
     */
    public Result<List<OrgContainer>> searchAll(SearchParams params) {
        return http.get("/organizationcontainers", params, null)
                .map(json -> toOrgContainerList(HalParser.toResourceList(json)));
    }

    /**
     * Searches for organisational containers of the specified category.
     *
     * @param category   the container category (e.g. {@link OrgCategory#LOCATIONS})
     * @param params     query parameters (attributes, limit, sort)
     * @param range      pagination range, or {@code null} for no paging
     * @return {@code Result<List<OrgContainer>>}
     */
    public Result<List<OrgContainer>> searchByCategory(OrgCategory category,
                                                        SearchParams params,
                                                        PageRange range) {
        return http.get("/organizationcontainers/" + category.value(), params, range)
                .map(json -> toOrgContainerList(HalParser.toResourceList(json)));
    }

    /**
     * Looks up a single organisational container by category and unique ID.
     *
     * @param category       the container category
     * @param orgContainerId the container's unique identifier
     * @param attributes     comma-separated attributes to return, or {@code null} for defaults
     * @param embedded       comma-separated reference attributes to embed, or {@code null}
     * @return {@code Result<OrgContainer>}
     */
    public Result<OrgContainer> getContainer(OrgCategory category, String orgContainerId,
                                              String attributes, String embedded) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes);
        if (embedded != null) b.embedded(embedded);
        return http.get("/organizationcontainers/" + category.value() + "/" + orgContainerId,
                        b.build(), null)
                .map(json -> toOrgContainer(HalParser.toResource(json)));
    }

    // ------------------------------------------------------------------
    //  Create
    // ------------------------------------------------------------------

    /**
     * Creates a new organisational container under the specified category.
     *
     * <p>Example body:
     * <pre>{@code
     * {
     *   "parentID": "<parentContainerId>",
     *   "attributes": [
     *     { "name": "ou", "values": ["Finance"] }
     *   ]
     * }
     * }</pre>
     *
     * @param category  the type of container to create (typically {@link OrgCategory#ORGANIZATIONAL_UNITS})
     * @param parentId  the ID of the parent container
     * @param attrs     attribute name → value(s) pairs
     * @return {@code Result<RequestResponse>} with the async request info
     */
    public Result<RequestResponse> createContainer(OrgCategory category, String parentId,
                                                    Map<String, List<String>> attrs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("parentID", parentId);
        body.put("attributes", buildAttributeList(attrs));
        return http.post("/organizationcontainers/" + category.value(), JsonParser.toJson(body))
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Modify
    // ------------------------------------------------------------------

    /**
     * Modifies attributes of an existing organisational container.
     *
     * @param category       the container category
     * @param orgContainerId the container's unique identifier
     * @param attrs          attribute name → value(s) pairs to update
     * @return {@code Result<RequestResponse>} with the async request info
     */
    public Result<RequestResponse> modifyContainer(OrgCategory category, String orgContainerId,
                                                    Map<String, List<String>> attrs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("attributes", buildAttributeList(attrs));
        return http.put("/organizationcontainers/" + category.value() + "/" + orgContainerId,
                        JsonParser.toJson(body), null)
                .map(HalParser::toRequestResponse);
    }

    /**
     * Transfers (re-parents) the specified organisational container to a new parent.
     *
     * <p>The container is identified by its ID without a category prefix — the IM API
     * uses {@code PUT /organizationcontainers/{orgContainerId}} for this operation
     * with {@code parentOrg} as a required query parameter.
     *
     * @param orgContainerId the container's unique identifier
     * @param newParentOrgId the unique ID of the new parent container
     * @return {@code Result<RequestResponse>} with the async request info (202)
     */
    public Result<RequestResponse> transferContainer(String orgContainerId, String newParentOrgId) {
        // parentOrg is passed as a query param; we build the path with it manually
        // because SearchParams doesn't have a generic "extra params" mechanism.
        String path = "/organizationcontainers/" + orgContainerId + "?parentOrg="
                + java.net.URLEncoder.encode(newParentOrgId, java.nio.charset.StandardCharsets.UTF_8);
        return http.put(path, "{}", null)
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Delete
    // ------------------------------------------------------------------

    /**
     * Deletes the specified organisational container.
     *
     * @param category       the container category
     * @param orgContainerId the container's unique identifier
     * @return {@code Result<RequestResponse>} with the async request info
     */
    public Result<RequestResponse> deleteContainer(OrgCategory category, String orgContainerId) {
        return http.delete("/organizationcontainers/" + category.value() + "/" + orgContainerId)
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Domain mapping helpers
    // ------------------------------------------------------------------

    private static List<OrgContainer> toOrgContainerList(List<HalResource> resources) {
        List<OrgContainer> list = new ArrayList<>(resources.size());
        for (HalResource r : resources) {
            list.add(toOrgContainer(r));
        }
        return list;
    }

    private static OrgContainer toOrgContainer(HalResource r) {
        OrgContainer c = new OrgContainer();
        c.setId(r.getId());
        c.setHref(r.getHref());
        c.setName(r.getTitle());
        Map<String, Object> attrs = r.getAttributes();
        if (!attrs.isEmpty()) {
            c.setAttributes(attrs);
        }
        return c;
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
