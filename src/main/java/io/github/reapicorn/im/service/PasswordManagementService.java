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
 * Service wrapper for the IBM Verify Identity Manager <strong>Password Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code PUT /accounts/password}                             – get password rules / submit change / validate</li>
 *   <li>{@code GET /password/challengeresponse}                    – get challenge questions for current user</li>
 *   <li>{@code PUT /password/challengeresponse}                    – update challenge responses for current user</li>
 *   <li>{@code POST /password/challengeresponse}                   – submit challenge responses for current user</li>
 *   <li>{@code POST /password/challengeresponse/user}              – lookup challenge questions by user payload</li>
 *   <li>{@code GET /password/challengeresponse/{userid}}           – lookup challenge questions by userid (deprecated)</li>
 *   <li>{@code PUT /password/challengeresponse/{userid}}           – submit responses for forgotten password (deprecated)</li>
 *   <li>{@code GET /password/configuration}                        – password configuration</li>
 *   <li>{@code PUT /password/retrieve}                             – retrieve password by transaction ID</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class PasswordManagementService {

    private final HttpExecutor http;

    public PasswordManagementService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  /accounts/password
    // ------------------------------------------------------------------

    /**
     * Multi-purpose account password endpoint.
     * Use {@code methodOverride} to control behaviour:
     * <ul>
     *   <li>{@code cumulate} – gather password rules</li>
     *   <li>{@code submit-in-batch} – set new password</li>
     *   <li>{@code validate-error-check-only} – validate password against rules</li>
     * </ul>
     *
     * @param body           request body (account IDs, passwords, etc.)
     * @param methodOverride X-HTTP-Method-Override value
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> accountsPassword(Map<String, Object> body, String methodOverride) {
        return http.put("/accounts/password", JsonParser.toJson(body), methodOverride)
                .map(HalParser::toResource);
    }

    // ------------------------------------------------------------------
    //  /password/challengeresponse
    // ------------------------------------------------------------------

    /** Returns the challenge questions configured for the currently logged-in user. */
    public Result<HalResource> getChallengeResponse() {
        return http.get("/password/challengeresponse", SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /** Updates the challenge responses for the currently logged-in user. */
    public Result<HalResource> updateChallengeResponse(Map<String, Object> body) {
        return http.put("/password/challengeresponse", JsonParser.toJson(body), null)
                .map(HalParser::toResource);
    }

    /** Creates (submits) the challenge responses for the currently logged-in user. */
    public Result<HalResource> createChallengeResponse(Map<String, Object> body) {
        return http.post("/password/challengeresponse", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /** Looks up challenge questions for a user specified in the request body. */
    public Result<HalResource> getChallengeResponseForUser(Map<String, Object> body) {
        return http.post("/password/challengeresponse/user", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Looks up challenge questions for the given user ID.
     *
     * @deprecated This endpoint has been deprecated by IBM. Use {@link #getChallengeResponseForUser} instead.
     */
    @Deprecated
    public Result<HalResource> getChallengeResponseByUserId(String userId) {
        return http.get("/password/challengeresponse/" + userId, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Submits responses for forgotten password questions to recover password.
     *
     * @deprecated This endpoint has been deprecated by IBM. Use {@link #getChallengeResponseForUser} instead.
     */
    @Deprecated
    public Result<HalResource> submitChallengeResponseByUserId(String userId, Map<String, Object> body) {
        return http.put("/password/challengeresponse/" + userId, JsonParser.toJson(body), null)
                .map(HalParser::toResource);
    }

    // ------------------------------------------------------------------
    //  /password/configuration
    // ------------------------------------------------------------------

    /** Returns the password configuration for Identity Governance. */
    public Result<List<HalResource>> getPasswordConfiguration(String attributes) {
        SearchParams params = SearchParams.builder().attributesRaw(attributes).build();
        return http.get("/password/configuration", params, null)
                .map(HalParser::toResourceList);
    }

    // ------------------------------------------------------------------
    //  /password/retrieve
    // ------------------------------------------------------------------

    /**
     * Retrieves an account password using a transaction ID.
     *
     * @param transactionId the transaction ID from the original request
     * @param body          request body containing shared secret validation
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> retrievePassword(String transactionId, Map<String, Object> body) {
        String path = "/password/retrieve" + (transactionId != null ? "?transactionid=" + transactionId : "");
        return http.put(path, JsonParser.toJson(body), null)
                .map(HalParser::toResource);
    }
}
