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

import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.SearchParams;
import io.github.reapicorn.im.internal.HalParser;
import io.github.reapicorn.im.internal.HttpExecutor;
import io.github.reapicorn.im.internal.JsonParser;
import io.github.reapicorn.im.model.HalResource;

import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Entitlement Assignments</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code POST /entitlements/assignments/search}      – search role assignment attributes</li>
 *   <li>{@code GET  /entitlements/assignments/{assignmentId}} – look up a single assignment</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class EntitlementsService {

    private final HttpExecutor http;

    public EntitlementsService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  /entitlements/assignments/search
    // ------------------------------------------------------------------

    /**
     * Searches for role assignment attributes matching the given criteria.
     *
     * <p>Optional query parameters:
     * <ul>
     *   <li>{@code limit}  – maximum items to return (default server value)</li>
     *   <li>{@code page}   – page to retrieve, 1-based</li>
     *   <li>{@code user}   – encoded person ID returned by the people-search API</li>
     * </ul>
     *
     * <p>The request body (can be {@code null} or an empty JSON object) may contain
     * additional filter criteria matching the {@code SearchAssignmentRequestBean} schema.
     *
     * @param limit  maximum items, or {@code null} to use server default
     * @param page   1-based page number, or {@code null} for the first page
     * @param user   encoded person ID filter, or {@code null}
     * @param body   optional JSON body with additional search criteria; pass an empty
     *               map or {@code null} for no extra filters
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchAssignments(Integer limit, Integer page,
                                                        String user,
                                                        Map<String, Object> body) {
        StringBuilder path = new StringBuilder("/entitlements/assignments/search");
        boolean first = true;
        if (limit != null) {
            path.append(first ? "?" : "&").append("limit=").append(limit);
            first = false;
        }
        if (page != null) {
            path.append(first ? "?" : "&").append("page=").append(page);
            first = false;
        }
        if (user != null && !user.isBlank()) {
            path.append(first ? "?" : "&").append("user=").append(user);
        }
        String json = (body == null) ? "{}" : JsonParser.toJson(body);
        return http.post(path.toString(), json)
                .map(raw -> {
                    // Response is a wrapper: { "count": N, "resources": [...] }
                    // Parse the "resources" array as a list of HalResources
                    List<HalResource> resources = HalParser.toResourceList(raw);
                    return resources;
                });
    }

    // ------------------------------------------------------------------
    //  /entitlements/assignments/{assignmentId}
    // ------------------------------------------------------------------

    /**
     * Returns assignment details for the specified assignment ID.
     *
     * @param assignmentId the encoded assignment identifier returned by
     *                     {@link #searchAssignments}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getAssignment(String assignmentId) {
        return http.get("/entitlements/assignments/" + assignmentId, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }
}
