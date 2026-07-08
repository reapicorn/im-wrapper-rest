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
 * Service wrapper for the IBM Verify Identity Manager <strong>Identity Policy Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /identitypolicy}                        – search all identity policies</li>
 *   <li>{@code POST   /identitypolicy}                        – create an identity policy</li>
 *   <li>{@code GET    /identitypolicy/{identityPolicyId}}     – look up an identity policy</li>
 *   <li>{@code PUT    /identitypolicy/{identityPolicyId}}     – modify an identity policy</li>
 *   <li>{@code DELETE /identitypolicy/{identityPolicyId}}     – delete an identity policy</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class IdentityPolicyService {

    private final HttpExecutor http;

    public IdentityPolicyService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Returns a list of all identity policies.
     *
     * @param params additional query parameters (attributes, limit, sort)
     * @param range  pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchIdentityPolicies(SearchParams params, PageRange range) {
        return http.get("/identitypolicy", params, range)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates a new identity policy.
     *
     * @param body identity policy definition (must include {@code orgID} and {@code _attributes})
     * @return {@code Result<String>} (201 Created on success)
     */
    public Result<String> createIdentityPolicy(Map<String, Object> body) {
        return http.post("/identitypolicy", JsonParser.toJson(body))
                .map(r -> r);
    }

    /**
     * Returns information about the specified identity policy.
     *
     * @param identityPolicyId unique identifier of the identity policy
     * @param attributes       comma-separated attributes to return, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getIdentityPolicy(String identityPolicyId, String attributes) {
        SearchParams params = SearchParams.builder()
                .attributesRaw(attributes)
                .build();
        return http.get("/identitypolicy/" + identityPolicyId, params, null)
                .map(HalParser::toResource);
    }

    /**
     * Modifies information for the specified identity policy.
     *
     * @param identityPolicyId unique identifier of the identity policy
     * @param body             updated identity policy definition
     * @return {@code Result<String>} (202 Accepted on success)
     */
    public Result<String> updateIdentityPolicy(String identityPolicyId, Map<String, Object> body) {
        return http.put("/identitypolicy/" + identityPolicyId, JsonParser.toJson(body), null);
    }

    /**
     * Deletes the specified identity policy.
     *
     * @param identityPolicyId unique identifier of the identity policy
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> deleteIdentityPolicy(String identityPolicyId) {
        return http.delete("/identitypolicy/" + identityPolicyId);
    }
}
