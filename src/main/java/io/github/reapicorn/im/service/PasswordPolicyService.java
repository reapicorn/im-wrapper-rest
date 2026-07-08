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
 * Service wrapper for the IBM Verify Identity Manager <strong>Password Policy Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /passwordpolicy}                 – search password policies</li>
 *   <li>{@code POST   /passwordpolicy}                 – create a password policy</li>
 *   <li>{@code GET    /passwordpolicy/{id}}             – look up a password policy</li>
 *   <li>{@code PUT    /passwordpolicy/{id}}             – modify a password policy</li>
 *   <li>{@code DELETE /passwordpolicy/{id}}             – delete a password policy</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class PasswordPolicyService {

    private final HttpExecutor http;

    public PasswordPolicyService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for password policies matching the given criteria.
     *
     * @param params query parameters (attributes, limit, sort)
     * @param range  pagination range, or {@code null} for no paging
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchPasswordPolicies(SearchParams params, PageRange range) {
        return http.get("/passwordpolicy", params, range)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates a new password policy.
     *
     * @param body the policy definition (orgID, rules, policyTargets, _attributes)
     * @return {@code Result<HalResource>} containing the new policy's self link
     */
    public Result<HalResource> createPasswordPolicy(Map<String, Object> body) {
        return http.post("/passwordpolicy", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Returns information about the specified password policy.
     *
     * @param policyId   the unique ID of the password policy
     * @param attributes comma-separated attributes to return, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getPasswordPolicy(String policyId, String attributes) {
        SearchParams params = SearchParams.builder().attributesRaw(attributes).build();
        return http.get("/passwordpolicy/" + policyId, params, null)
                .map(HalParser::toResource);
    }

    /**
     * Modifies the specified password policy.
     *
     * @param policyId the unique ID of the password policy
     * @param body     the updated policy definition
     * @return {@code Result<String>} (raw response body)
     */
    public Result<String> modifyPasswordPolicy(String policyId, Map<String, Object> body) {
        return http.put("/passwordpolicy/" + policyId, JsonParser.toJson(body), null);
    }

    /**
     * Deletes the specified password policy.
     *
     * @param policyId the unique ID of the password policy
     * @return {@code Result<String>} (empty on success)
     */
    public Result<String> deletePasswordPolicy(String policyId) {
        return http.delete("/passwordpolicy/" + policyId);
    }
}
