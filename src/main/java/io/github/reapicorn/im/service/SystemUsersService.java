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
import io.github.reapicorn.im.model.SystemUser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>System User Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET  /systemusers}                                        – search system users</li>
 *   <li>{@code GET  /systemusers/me}                                     – current system user</li>
 *   <li>{@code GET  /systemusers/{id}}                                   – look up a system user</li>
 *   <li>{@code GET  /systemusers/{id}/constraints}                       – constraints of a system user</li>
 *   <li>{@code GET  /systemusers/{id}/constraints/password}              – password constraints</li>
 *   <li>{@code PUT  /systemusers/{id}/password}                          – change password</li>
 *   <li>{@code GET  /systemusers/{id}/delegations}                       – list delegations</li>
 *   <li>{@code POST /systemusers/{id}/delegations}                       – add delegation</li>
 *   <li>{@code PUT  /systemusers/{id}/delegations/{delegateId}}          – modify delegation</li>
 *   <li>{@code DELETE /systemusers/{id}/delegations/{delegateId}}        – delete delegation</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class SystemUsersService {

    private final HttpExecutor http;

    public SystemUsersService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  Search / lookup
    // ------------------------------------------------------------------

    /**
     * Searches for system users matching the given criteria.
     *
     * @param params  query parameters (attributes, limit, sort)
     * @param range   pagination range, or {@code null} for no paging
     * @return {@code Result<List<SystemUser>>}
     */
    public Result<List<SystemUser>> searchSystemUsers(SearchParams params, PageRange range) {
        return http.get("/systemusers", params, range)
                .map(json -> toSystemUserList(HalParser.toResourceList(json)));
    }

    /**
     * Searches for system users — convenience overload.
     *
     * @param attributes comma-separated attributes to return, or {@code "*"} for all
     * @param limit      maximum number of results (null = server default)
     * @param sort       sort expression, e.g. {@code "+eruid"} (null = server default)
     * @return {@code Result<List<SystemUser>>}
     */
    public Result<List<SystemUser>> searchSystemUsers(String attributes, String limit, String sort) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes).sort(sort);
        if (limit != null) b.limit(Integer.parseInt(limit));
        return http.get("/systemusers", b.build(), null)
                .map(json -> toSystemUserList(HalParser.toResourceList(json)));
    }

    /**
     * Returns information about the currently authenticated system user.
     *
     * <p>This is the same endpoint used internally by {@code HttpExecutor.initSession()}
     * to obtain the CSRF token. Calling this method also provides the consumer
     * with the current user's attributes.
     *
     * @param attributes comma-separated attributes to return, or {@code null} for defaults
     * @param embedded   comma-separated reference attributes to embed, or {@code null}
     * @return {@code Result<SystemUser>}
     */
    public Result<SystemUser> getCurrentSystemUser(String attributes, String embedded) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes);
        if (embedded != null) b.embedded(embedded);
        return http.get("/systemusers/me", b.build(), null)
                .map(json -> {
                    // /systemusers/me returns an array with one element
                    List<HalResource> list = HalParser.toResourceList(json);
                    return list.isEmpty() ? new SystemUser() : toSystemUser(list.get(0));
                });
    }

    /**
     * Looks up a single system user by their unique ID.
     *
     * @param systemUserId the system user's unique identifier
     * @param attributes   comma-separated attributes to return, or {@code null} for defaults
     * @param embedded     comma-separated reference attributes to embed, or {@code null}
     * @return {@code Result<SystemUser>}
     */
    public Result<SystemUser> getSystemUser(String systemUserId, String attributes, String embedded) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes);
        if (embedded != null) b.embedded(embedded);
        return http.get("/systemusers/" + systemUserId, b.build(), null)
                .map(json -> toSystemUser(HalParser.toResource(json)));
    }

    // ------------------------------------------------------------------
    //  Constraints
    // ------------------------------------------------------------------

    /**
     * Returns the constraints for the specified system user.
     *
     * @param systemUserId the system user's unique identifier
     * @param embedded     comma-separated constraint links to embed, or {@code null}
     * @return {@code Result<HalResource>} containing constraint data
     */
    public Result<HalResource> getConstraints(String systemUserId, String embedded) {
        SearchParams.Builder b = SearchParams.builder();
        if (embedded != null) b.embedded(embedded);
        return http.get("/systemusers/" + systemUserId + "/constraints", b.build(), null)
                .map(json -> {
                    List<HalResource> list = HalParser.toResourceList(json);
                    return list.isEmpty() ? new HalResource(null, null, null) : list.get(0);
                });
    }

    /**
     * Returns the password constraints for the specified system user.
     *
     * @param systemUserId the system user's unique identifier
     * @param embedded     comma-separated constraint links to embed, or {@code null}
     * @return {@code Result<HalResource>} containing password constraint data
     */
    public Result<HalResource> getPasswordConstraints(String systemUserId, String embedded) {
        SearchParams.Builder b = SearchParams.builder();
        if (embedded != null) b.embedded(embedded);
        return http.get("/systemusers/" + systemUserId + "/constraints/password", b.build(), null)
                .map(json -> {
                    List<HalResource> list = HalParser.toResourceList(json);
                    return list.isEmpty() ? new HalResource(null, null, null) : list.get(0);
                });
    }

    // ------------------------------------------------------------------
    //  Password
    // ------------------------------------------------------------------

    /**
     * Changes the password for the specified system user.
     *
     * <p>The {@code methodOverride} can be {@code "validate"} to only validate
     * the password without changing it, or {@code null} to perform the actual change.
     *
     * @param systemUserId   the system user's unique identifier
     * @param newPassword    the new password to set
     * @param methodOverride optional method override (e.g. {@code "validate"}), or {@code null}
     * @return {@code Result<RequestResponse>} (202 response with async request info)
     */
    public Result<RequestResponse> changePassword(String systemUserId, String newPassword,
                                                   String methodOverride) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("password", newPassword);
        return http.put("/systemusers/" + systemUserId + "/password",
                        JsonParser.toJson(body), methodOverride)
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Delegations
    // ------------------------------------------------------------------

    /**
     * Returns information about the delegations for the specified system user.
     *
     * @param systemUserId the system user's unique identifier
     * @return {@code Result<HalResource>} containing delegation data
     */
    public Result<HalResource> getDelegations(String systemUserId) {
        return http.get("/systemusers/" + systemUserId + "/delegations",
                        SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Adds a delegation for the specified system user.
     *
     * <p>The {@code delegateId} is the system user ID of the person being delegated to.
     * {@code startDate} and {@code endDate} are epoch milliseconds (as longs).
     *
     * @param systemUserId the system user's unique identifier
     * @param delegateId   system user ID of the delegate
     * @param startDate    delegation start date in epoch milliseconds
     * @param endDate      delegation end date in epoch milliseconds
     * @return {@code Result<RequestResponse>}
     */
    public Result<RequestResponse> addDelegation(String systemUserId, String delegateId,
                                                  long startDate, long endDate) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("delegateID", delegateId);
        body.put("startDate", startDate);
        body.put("endDate", endDate);
        return http.post("/systemusers/" + systemUserId + "/delegations",
                         JsonParser.toJson(body))
                .map(HalParser::toRequestResponse);
    }

    /**
     * Modifies the delegation identified by {@code delegateId} for the specified user.
     *
     * @param systemUserId the system user's unique identifier
     * @param delegateId   unique identifier of the delegation to modify
     * @param startDate    new delegation start date in epoch milliseconds
     * @param endDate      new delegation end date in epoch milliseconds
     * @return {@code Result<RequestResponse>}
     */
    public Result<RequestResponse> modifyDelegation(String systemUserId, String delegateId,
                                                     long startDate, long endDate) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("delegateID", delegateId);
        body.put("startDate", startDate);
        body.put("endDate", endDate);
        return http.put("/systemusers/" + systemUserId + "/delegations/" + delegateId,
                        JsonParser.toJson(body), null)
                .map(HalParser::toRequestResponse);
    }

    /**
     * Deletes the delegation identified by {@code delegateId} for the specified user.
     *
     * @param systemUserId the system user's unique identifier
     * @param delegateId   unique identifier of the delegation to delete
     * @return {@code Result<RequestResponse>}
     */
    public Result<RequestResponse> deleteDelegation(String systemUserId, String delegateId) {
        return http.delete("/systemusers/" + systemUserId + "/delegations/" + delegateId)
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Domain mapping helpers
    // ------------------------------------------------------------------

    private static List<SystemUser> toSystemUserList(List<HalResource> resources) {
        List<SystemUser> list = new ArrayList<>(resources.size());
        for (HalResource r : resources) {
            list.add(toSystemUser(r));
        }
        return list;
    }

    private static SystemUser toSystemUser(HalResource r) {
        SystemUser u = new SystemUser();
        u.setId(r.getId());
        u.setHref(r.getHref());
        u.setUid(r.getTitle());
        Map<String, Object> attrs = r.getAttributes();
        if (!attrs.isEmpty()) {
            u.setAttributes(attrs);
            Object eruid = attrs.get("eruid");
            if (eruid != null) u.setUid(eruid.toString());
        }
        return u;
    }
}
