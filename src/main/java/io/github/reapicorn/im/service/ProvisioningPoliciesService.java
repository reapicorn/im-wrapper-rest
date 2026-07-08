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
 * Service wrapper for the IBM Verify Identity Manager <strong>Provisioning Policy Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /provisioningpolicies}       – search provisioning policies</li>
 *   <li>{@code POST   /provisioningpolicies}       – create a provisioning policy or draft</li>
 *   <li>{@code GET    /provisioningpolicies/{id}}  – look up a provisioning policy</li>
 *   <li>{@code PUT    /provisioningpolicies/{id}}  – modify / publish a provisioning policy</li>
 *   <li>{@code DELETE /provisioningpolicies/{id}}  – delete a provisioning policy</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class ProvisioningPoliciesService {

    private final HttpExecutor http;

    public ProvisioningPoliciesService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for provisioning policies with optional filtering.
     *
     * @param name       filter by name, or {@code null}
     * @param parentUUID filter by organisation UUID, or {@code null}
     * @param enabled    filter by status ({@code "true"} / {@code "false"}), or {@code null}
     * @param params     additional query parameters (limit, sort)
     * @param range      pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchProvisioningPolicies(String name, String parentUUID,
                                                                 String enabled,
                                                                 SearchParams params,
                                                                 PageRange range) {
        StringBuilder path = new StringBuilder("/provisioningpolicies");
        String qs = params.toQueryString();
        boolean first = true;
        if (!qs.isBlank()) {
            path.append("?").append(qs);
            first = false;
        }
        if (name != null && !name.isBlank()) {
            path.append(first ? "?" : "&").append("name=").append(name);
            first = false;
        }
        if (parentUUID != null && !parentUUID.isBlank()) {
            path.append(first ? "?" : "&").append("parentUUID=").append(parentUUID);
            first = false;
        }
        if (enabled != null && !enabled.isBlank()) {
            path.append(first ? "?" : "&").append("enabled=").append(enabled);
        }
        return http.get(path.toString(), SearchParams.empty(), range)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates a new provisioning policy or a draft.
     *
     * @param body policy definition
     * @return {@code Result<String>} (201 draft created, or 202 async request accepted)
     */
    public Result<String> createProvisioningPolicy(Map<String, Object> body) {
        return http.post("/provisioningpolicies", JsonParser.toJson(body))
                .map(r -> r);
    }

    /**
     * Returns information about the specified provisioning policy.
     *
     * @param id unique identifier of the provisioning policy
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getProvisioningPolicy(String id) {
        return http.get("/provisioningpolicies/" + id, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Modifies or publishes the specified provisioning policy.
     *
     * @param id   unique identifier of the provisioning policy
     * @param body updated policy definition
     * @return {@code Result<String>} (202 Accepted on success)
     */
    public Result<String> updateProvisioningPolicy(String id, Map<String, Object> body) {
        return http.put("/provisioningpolicies/" + id, JsonParser.toJson(body), null);
    }

    /**
     * Deletes the specified provisioning policy (and its drafts if any).
     *
     * @param id   unique identifier of the provisioning policy
     * @param body optional delete request body (can be {@code null} or empty map)
     * @return {@code Result<String>} (202 Accepted on success)
     */
    public Result<String> deleteProvisioningPolicy(String id, Map<String, Object> body) {
        return http.delete("/provisioningpolicies/" + id);
    }
}
