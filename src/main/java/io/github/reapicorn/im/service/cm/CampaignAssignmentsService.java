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
 * Service wrapper for the IBM Verify Identity Manager Certification Campaign <strong>Assignments</strong>
 * REST API (v2.0).
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET  /assignments/stats}             – assignment-level statistics</li>
 *   <li>{@code GET  /assignments/{id}}              – get a specific assignment</li>
 *   <li>{@code PUT  /assignments/{id}}              – update (approve/reject/redirect/escalate)</li>
 *   <li>{@code GET  /instances/{id}/assignments}    – list assignments for an instance (GET)</li>
 *   <li>{@code POST /instances/{id}/assignments}    – list assignments for an instance (POST)</li>
 *   <li>{@code GET  /instances/{instanceId}/assignments/resources} – assignment resource stats</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <String>} carrying the raw JSON response.
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class CampaignAssignmentsService {

    private final HttpExecutor http;
    private final String       cmBase;

    /**
     * @param http    the shared {@link HttpExecutor}
     * @param cmBase  the CM origin, e.g. {@code "https://your-im-host:30943"}
     */
    CampaignAssignmentsService(HttpExecutor http, String cmBase) {
        this.http   = http;
        this.cmBase = cmBase;
    }

    // ------------------------------------------------------------------
    //  Assignment-level stats
    // ------------------------------------------------------------------

    /**
     * Retrieves aggregated assignment statistics for a campaign instance.
     *
     * @param params optional query filters (instanceId, asSupervisor, assignmentSource,
     *               filterType, …)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getAssignmentStats(SearchParams params) {
        return http.get(cm("/assignments/stats"), params, null);
    }

    // ------------------------------------------------------------------
    //  Single assignment operations
    // ------------------------------------------------------------------

    /**
     * Retrieves the details of a specific campaign assignment.
     *
     * @param id     the campaign assignment ID
     * @param params optional query filters (asSupervisor, assignmentSource)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getAssignment(String id, SearchParams params) {
        return http.get(cm("/assignments/" + id), params, null);
    }

    /**
     * Updates (approve, reject, redirect, or escalate) a specific assignment.
     *
     * @param id       the campaign assignment ID
     * @param params   optional query parameters (asSupervisor, assignmentSource)
     * @param jsonBody JSON array of assignment action objects
     *                 {@code [{ "operation": "approved|rejected|redirected|escalated",
     *                           "justification": "...", "reviewer": {...} }]}
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> updateAssignment(String id, SearchParams params, String jsonBody) {
        String path = cm("/assignments/" + id);
        String qs   = (params != null) ? params.toQueryString() : "";
        if (!qs.isBlank()) path = path + "?" + qs;
        return http.put(path, jsonBody, null);
    }

    // ------------------------------------------------------------------
    //  Assignments by instance
    // ------------------------------------------------------------------

    /**
     * Retrieves the list of assignments for a specific campaign instance (GET variant).
     *
     * @param instanceId the campaign instance ID
     * @param params     optional query filters (actions, appsId, asSupervisor,
     *                   assigneesId, assignmentName, assignmentSource, assignmentStatus,
     *                   assignmentType, filter, groupsId, limit, page, reviewersId,
     *                   riskLevel, riskMitigation, riskType, sort, sortDesc)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getInstanceAssignments(String instanceId, SearchParams params) {
        return http.get(cm("/instances/" + instanceId + "/assignments"), params, null);
    }

    /**
     * Retrieves the list of assignments for a specific campaign instance (POST variant —
     * allows complex filter payloads when query-string limits would be exceeded).
     *
     * @param instanceId the campaign instance ID
     * @param params     optional query parameters appended to the URL
     * @param jsonBody   optional JSON body with extended filter criteria, or {@code "{}"}
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> searchInstanceAssignments(String instanceId,
                                                    SearchParams params,
                                                    String jsonBody) {
        String path = cm("/instances/" + instanceId + "/assignments");
        String qs   = (params != null) ? params.toQueryString() : "";
        if (!qs.isBlank()) path = path + "?" + qs;
        return http.post(path, jsonBody != null ? jsonBody : "{}");
    }

    /**
     * Retrieves aggregated statistics of specific assignments within a campaign instance
     * (assignee, reviewer, application, etc.).
     *
     * @param instanceId the campaign instance ID
     * @param params     optional query filters (resourceType, actions, asSupervisor,
     *                   assignmentSource, filter, limit, page, reviewerId, sort, sortDesc)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getInstanceAssignmentResourceStats(String instanceId,
                                                             SearchParams params) {
        return http.get(cm("/instances/" + instanceId + "/assignments/resources"),
                params, null);
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    private String cm(String path) {
        return cmBase + "/itim/cm/v2.0" + path;
    }
}
