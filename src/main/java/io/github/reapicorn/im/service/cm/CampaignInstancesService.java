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
 * Service wrapper for the IBM Verify Identity Manager Certification Campaign <strong>Instances</strong>
 * REST API (v2.0).
 *
 * <p>Covered endpoints under {@code /itim/cm/v2.0/instances*}:
 * <ul>
 *   <li>{@code GET   /instances}               – list campaign instances</li>
 *   <li>{@code PATCH /instances}               – bulk action on campaign instances</li>
 *   <li>{@code GET   /instances/stats}         – campaign instance statistics</li>
 *   <li>{@code GET   /instances/{id}}          – get a specific campaign instance</li>
 *   <li>{@code PUT   /instances/{id}}          – perform action on a campaign instance</li>
 *   <li>{@code POST  /instances/{id}/preview}  – generate a preview for an instance</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <String>} carrying the raw JSON response.
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class CampaignInstancesService {

    private final HttpExecutor http;
    private final String       cmBase;

    /**
     * @param http    the shared {@link HttpExecutor}
     * @param cmBase  the CM origin, e.g. {@code "https://your-im-host:30943"}
     */
    CampaignInstancesService(HttpExecutor http, String cmBase) {
        this.http   = http;
        this.cmBase = cmBase;
    }

    // ------------------------------------------------------------------
    //  Instances
    // ------------------------------------------------------------------

    /**
     * Retrieves the list of campaign instances for the tenant.
     *
     * @param params optional query filters (state, types, continuous, priority,
     *               assignmentSource, overallState, sort, sortDesc, limit, page, …)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getInstances(SearchParams params) {
        return http.get(cm("/instances"), params, null);
    }

    /**
     * Performs bulk actions on a collection of campaign instances (cancel, pause,
     * restart, runnow, end, delete).
     *
     * @param jsonBody JSON array of {@code { "id": "...", "eventType": "..." }} objects
     * @return {@code Result<String>} raw JSON (202 or bulk results)
     */
    public Result<String> bulkActionInstances(String jsonBody) {
        return http.patch(cm("/instances"), jsonBody);
    }

    /**
     * Retrieves campaign instance statistics for the tenant.
     *
     * @param params optional query filters
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getInstanceStats(SearchParams params) {
        return http.get(cm("/instances/stats"), params, null);
    }

    /**
     * Retrieves the details of a specific campaign instance.
     *
     * @param id     the campaign instance ID
     * @param params optional query filters (asSupervisor, assignmentSource)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getInstance(String id, SearchParams params) {
        return http.get(cm("/instances/" + id), params, null);
    }

    /**
     * Performs an action on a specific campaign instance (cancel, pause, restart, runnow).
     *
     * @param id       the campaign instance ID
     * @param jsonBody JSON payload with {@code { "eventType": "cancel|pause|restart|runnow" }}
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> actionInstance(String id, String jsonBody) {
        return http.put(cm("/instances/" + id), jsonBody, null);
    }

    /**
     * Generates a preview campaign configuration for a specific campaign instance.
     *
     * @param id the campaign instance ID
     * @return {@code Result<String>} raw body (202 on success)
     */
    public Result<String> generatePreview(String id) {
        return http.post(cm("/instances/" + id + "/preview"), "{}");
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    private String cm(String path) {
        return cmBase + "/itim/cm/v2.0" + path;
    }
}
