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
package io.github.reapicorn.im.service.arc;

import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.SearchParams;
import io.github.reapicorn.im.internal.HttpExecutor;

/**
 * Service wrapper for the IBM Verify Identity Manager ARC <strong>User</strong> REST API (v1.0).
 *
 * <p>Covered endpoints under {@code /itim/arc/v1.0/}:
 * <ul>
 *   <li>{@code GET  /users}                                  – list users for an entity</li>
 *   <li>{@code POST /users/risks/preview/bulk}               – bulk risk-preview for users</li>
 *   <li>{@code POST /users/{id}/risks/analysis}              – run risk analysis for a user</li>
 *   <li>{@code POST /users/{id}/risks/preview}               – preview risk impact of adding permissions</li>
 *   <li>{@code POST /users/{userId}/risks/{riskId}/mitigations} – apply mitigation to user risk</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <String>} carrying the raw JSON response.
 */
public class ArcUsersService {

    private final HttpExecutor http;
    private final String       arcBase;

    ArcUsersService(HttpExecutor http, String arcBase) {
        this.http    = http;
        this.arcBase = arcBase;
    }

    // ------------------------------------------------------------------
    //  ARC Users
    // ------------------------------------------------------------------

    /**
     * Retrieves users for a provided entity.
     *
     * @param params query parameters — {@code entityType} is required
     *               (one of {@code Business Activity}, {@code Mitigation}, {@code Risk});
     *               optional: {@code entityId}, {@code filter}, {@code limit},
     *               {@code page}, {@code sort}, {@code sortDesc}
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getUsers(SearchParams params) {
        return http.get(arc("/users"), params, null);
    }

    /**
     * Returns bulk risk-preview analysis for multiple users and accesses.
     *
     * @param jsonBody JSON array of user-risk-preview items
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> bulkRisksPreview(String jsonBody) {
        return http.post(arc("/users/risks/preview/bulk"), jsonBody);
    }

    /**
     * Executes a risk analysis for a specific user.
     *
     * @param userId   the user ID
     * @param jsonBody JSON payload (analysis parameters)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> runUserRiskAnalysis(String userId, String jsonBody) {
        return http.post(arc("/users/" + userId + "/risks/analysis"), jsonBody);
    }

    /**
     * Previews the risk impact of adding permissions to a specific user.
     *
     * @param userId   the user ID
     * @param groupBy  optional grouping ({@code Permission} or {@code Risk}); may be {@code null}
     * @param jsonBody JSON payload with permissions to preview
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> previewUserRisks(String userId, String groupBy, String jsonBody) {
        String path = arc("/users/" + userId + "/risks/preview");
        if (groupBy != null && !groupBy.isBlank()) {
            path += "?groupBy=" + groupBy;
        }
        return http.post(path, jsonBody);
    }

    /**
     * Applies a mitigation to a specific risk of a user.
     *
     * @param userId   the user ID
     * @param riskId   the risk ID
     * @param jsonBody JSON payload with mitigation details
     * @return {@code Result<String>} raw body (empty on 204 / multi-status on 207)
     */
    public Result<String> applyUserRiskMitigation(String userId, String riskId, String jsonBody) {
        return http.post(arc("/users/" + userId + "/risks/" + riskId + "/mitigations"), jsonBody);
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    private String arc(String path) {
        return arcBase + "/itim/arc/v1.0" + path;
    }
}
