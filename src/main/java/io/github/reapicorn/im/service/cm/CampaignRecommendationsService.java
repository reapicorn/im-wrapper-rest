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
package io.github.reapicorn.im.service.cm;

import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.SearchParams;
import io.github.reapicorn.im.internal.HttpExecutor;

/**
 * Service wrapper for the IBM Verify Identity Manager Certification Campaign <strong>Recommendations</strong>
 * REST API (v2.0).
 *
 * <p>Covered endpoints under {@code /itim/cm/v2.0/recommendation*}:
 * <ul>
 *   <li>{@code POST /recommendation/role}           – fetch role recommendations for an instance</li>
 *   <li>{@code GET  /recommendation/role/{id}}      – get details of a recommended role</li>
 *   <li>{@code POST /recommendation/roles/export}   – export recommended roles as ZIP</li>
 *   <li>{@code POST /instances/{instanceId}/insight/reload} – reload smart-campaign insights</li>
 *   <li>{@code GET  /instances/{instanceId}/recommendations/resources} – recommendation resources</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <String>} carrying the raw JSON (or raw ZIP bytes
 * as a string for the export endpoint).
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class CampaignRecommendationsService {

    private final HttpExecutor http;
    private final String       cmBase;

    /**
     * @param http    the shared {@link HttpExecutor}
     * @param cmBase  the CM origin, e.g. {@code "https://your-im-host:30943"}
     */
    CampaignRecommendationsService(HttpExecutor http, String cmBase) {
        this.http   = http;
        this.cmBase = cmBase;
    }

    // ------------------------------------------------------------------
    //  Role recommendations
    // ------------------------------------------------------------------

    /**
     * Retrieves role recommendations for a specific campaign instance.
     *
     * @param params   query parameters (instanceId [required], limit, page,
     *                 recommendationScope, sort, sortDesc)
     * @param jsonBody JSON filter criteria
     *                 {@code { "application": "...", "user": "...", "reviewer": "..." }}
     * @return {@code Result<String>} raw JSON ({@code RoleRecommendationResult})
     */
    public Result<String> fetchRoleRecommendations(SearchParams params, String jsonBody) {
        String path = cm("/recommendation/role");
        String qs   = (params != null) ? params.toQueryString() : "";
        if (!qs.isBlank()) path = path + "?" + qs;
        return http.post(path, jsonBody != null ? jsonBody : "{}");
    }

    /**
     * Retrieves the details of a recommended role by ID.
     *
     * @param id     the recommended role ID
     * @param params optional query parameters (filter, impactedEntity, limit, page,
     *               sort, sortDesc)
     * @return {@code Result<String>} raw JSON ({@code RecommendedRoleDetails})
     */
    public Result<String> getRecommendedRole(String id, SearchParams params) {
        return http.get(cm("/recommendation/role/" + id), params, null);
    }

    /**
     * Exports one or more recommended roles into a ZIP file.
     *
     * <p>The response body is a ZIP archive. The caller is responsible for saving it.
     *
     * @param jsonBody JSON array of role IDs: {@code ["cf8d…", "dd78…"]}
     * @return {@code Result<String>} raw ZIP content (binary as string)
     */
    public Result<String> exportRoleRecommendations(String jsonBody) {
        return http.post(cm("/recommendation/roles/export"), jsonBody);
    }

    // ------------------------------------------------------------------
    //  Instance-level insight / recommendation resources
    // ------------------------------------------------------------------

    /**
     * Reloads insights for the given assignments within a campaign instance.
     *
     * @param instanceId the campaign instance ID
     * @param jsonBody   JSON payload with the assignment filter criteria
     * @return {@code Result<String>} raw body (202 on acceptance)
     */
    public Result<String> reloadInstanceInsights(String instanceId, String jsonBody) {
        return http.post(cm("/instances/" + instanceId + "/insight/reload"),
                jsonBody != null ? jsonBody : "{}");
    }

    /**
     * Retrieves recommendation resources for a specific campaign instance.
     *
     * @param instanceId the campaign instance ID
     * @param params     optional query filters
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getInstanceRecommendationResources(String instanceId,
                                                              SearchParams params) {
        return http.get(cm("/instances/" + instanceId + "/recommendations/resources"),
                params, null);
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    private String cm(String path) {
        return cmBase + "/itim/cm/v2.0" + path;
    }
}
