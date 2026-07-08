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

import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>System Roles (ITIM Group) Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /systemroles}              – search system roles</li>
 *   <li>{@code POST   /systemroles}              – create a system role</li>
 *   <li>{@code GET    /systemroles/{id}}         – look up a system role by legacy ID</li>
 *   <li>{@code GET    /systemroles/{uuid}}        – look up a system role by UUID</li>
 *   <li>{@code PUT    /systemroles/{uuid}}        – update a system role</li>
 *   <li>{@code DELETE /systemroles/{uuid}}        – delete a system role</li>
 * </ul>
 *
 * <p>Note: the server uses the same path pattern {@code /systemroles/{id}} for both legacy
 * base64-encoded IDs and UUIDs. The two {@code get} methods are provided for clarity but
 * ultimately call the same path structure.
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class SystemRolesService {

    private final HttpExecutor http;

    public SystemRolesService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for system roles with optional filtering.
     *
     * @param filter     LDAP filter, e.g. {@code "(errolename=AdminRole)"}, or {@code null}
     * @param orgId      filter by org ID, or {@code null}
     * @param params     additional query parameters (attributes, limit, sort)
     * @param range      pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchSystemRoles(String filter, String orgId,
                                                         SearchParams params, PageRange range) {
        StringBuilder path = new StringBuilder("/systemroles");
        String qs = params.toQueryString();
        boolean first = true;
        if (!qs.isBlank()) {
            path.append("?").append(qs);
            first = false;
        }
        if (filter != null && !filter.isBlank()) {
            path.append(first ? "?" : "&").append("filter=").append(filter);
            first = false;
        }
        if (orgId != null && !orgId.isBlank()) {
            path.append(first ? "?" : "&").append("orgId=").append(orgId);
        }
        return http.get(path.toString(), SearchParams.empty(), range)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates a new system role.
     *
     * @param body system role definition (errolename, description, ersystemrolecategory, parentUUID, etc.)
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> createSystemRole(Map<String, Object> body) {
        return http.post("/systemroles", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Returns information about the system role identified by its legacy base64-encoded ID.
     *
     * @param systemRoleId the legacy unique identifier of the system role
     * @param attributes   comma-separated attributes to return, or {@code null}
     * @param embedded     embedded reference attributes, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getSystemRole(String systemRoleId, String attributes, String embedded) {
        SearchParams params = SearchParams.builder()
                .attributesRaw(attributes)
                .embedded(embedded != null ? embedded : "")
                .build();
        return http.get("/systemroles/" + systemRoleId, params, null)
                .map(HalParser::toResource);
    }

    /**
     * Returns information about the system role identified by its UUID.
     *
     * @param uuid       the UUID of the system role
     * @param attributes comma-separated attributes to return, or {@code null}
     * @param embedded   embedded reference attributes, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getSystemRoleByUuid(String uuid, String attributes, String embedded) {
        return getSystemRole(uuid, attributes, embedded);
    }

    /**
     * Updates the specified system role.
     *
     * @param uuid the UUID of the system role
     * @param body updated system role definition
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> updateSystemRole(String uuid, Map<String, Object> body) {
        return http.put("/systemroles/" + uuid, JsonParser.toJson(body), null);
    }

    /**
     * Deletes the specified system role.
     *
     * @param uuid the UUID of the system role
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> deleteSystemRole(String uuid) {
        return http.delete("/systemroles/" + uuid);
    }
}
