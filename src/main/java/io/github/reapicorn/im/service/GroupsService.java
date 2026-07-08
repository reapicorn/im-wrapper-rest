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
 * Service wrapper for the IBM Verify Identity Manager <strong>Groups Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /groups}         – search groups</li>
 *   <li>{@code POST   /groups}         – create a group</li>
 *   <li>{@code GET    /groups/{uuid}}  – look up a group</li>
 *   <li>{@code PUT    /groups/{uuid}}  – update a group</li>
 *   <li>{@code DELETE /groups/{uuid}}  – delete a group</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class GroupsService {

    private final HttpExecutor http;

    public GroupsService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for groups with optional filtering.
     *
     * @param filter    LDAP filter string, e.g. {@code "(erntlocalname=Remote)"}, or {@code null}
     * @param ownerID   filter by owner UUID, or {@code null}
     * @param serviceID filter by service UUID, or {@code null}
     * @param params    additional query parameters (attributes, limit, sort)
     * @param range     pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchGroups(String filter, String ownerID, String serviceID,
                                                    SearchParams params, PageRange range) {
        // /groups is only available under /itim/rest/v1.2
        StringBuilder path = new StringBuilder(http.getBaseUrlV12() + "/groups");
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
        if (ownerID != null && !ownerID.isBlank()) {
            path.append(first ? "?" : "&").append("ownerID=").append(ownerID);
            first = false;
        }
        if (serviceID != null && !serviceID.isBlank()) {
            path.append(first ? "?" : "&").append("serviceID=").append(serviceID);
        }
        return http.get(path.toString(), SearchParams.empty(), range)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates a new group.
     *
     * @param body group definition
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> createGroup(Map<String, Object> body) {
        return http.post("/groups", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Returns information about the specified group.
     *
     * @param uuid       the unique identifier of the group
     * @param attributes comma-separated attributes to return, or {@code null}
     * @param embedded   embedded reference attributes, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getGroup(String uuid, String attributes, String embedded) {
        SearchParams params = SearchParams.builder()
                .attributesRaw(attributes)
                .embedded(embedded != null ? embedded : "")
                .build();
        return http.get("/groups/" + uuid, params, null)
                .map(HalParser::toResource);
    }

    /**
     * Updates the specified group.
     *
     * @param uuid the unique identifier of the group
     * @param body updated group definition
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> updateGroup(String uuid, Map<String, Object> body) {
        return http.put("/groups/" + uuid, JsonParser.toJson(body), null);
    }

    /**
     * Deletes the specified group.
     *
     * @param uuid the unique identifier of the group
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> deleteGroup(String uuid) {
        return http.delete("/groups/" + uuid);
    }
}
