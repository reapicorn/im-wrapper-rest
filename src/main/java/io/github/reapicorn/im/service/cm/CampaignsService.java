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
 * Service wrapper for the IBM Verify Identity Manager Certification Campaign <strong>Configurations</strong>
 * REST API (v2.0).
 *
 * <p>Covered endpoints under {@code /itim/cm/v2.0/campaigns*}:
 * <ul>
 *   <li>{@code GET    /campaigns}            – list campaign configurations</li>
 *   <li>{@code POST   /campaigns}            – create a campaign configuration</li>
 *   <li>{@code GET    /campaigns/stats}      – campaign configuration statistics</li>
 *   <li>{@code GET    /campaigns/{id}}       – get a specific campaign configuration</li>
 *   <li>{@code PUT    /campaigns/{id}}       – update a campaign configuration</li>
 *   <li>{@code DELETE /campaigns/{id}}       – delete a campaign configuration</li>
 *   <li>{@code GET    /campaigns/{id}/instances} – list instances for a configuration</li>
 *   <li>{@code PUT    /campaigns/{id}/launch} – launch a specific campaign</li>
 *   <li>{@code POST   /campaigns/clone}      – clone campaign configurations</li>
 *   <li>{@code PATCH  /campaigns/launch}     – bulk-launch draft configurations</li>
 *   <li>{@code GET    /campaign/stats}       – resource-level statistics</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <String>} carrying the raw JSON response.
 * Use {@link io.github.reapicorn.im.internal.JsonParser} to parse the payload as needed.
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class CampaignsService {

    private final HttpExecutor http;
    private final String       cmBase;

    /**
     * @param http    the shared {@link HttpExecutor}
     * @param cmBase  the CM origin, e.g. {@code "https://your-im-host:30943"}
     */
    CampaignsService(HttpExecutor http, String cmBase) {
        this.http   = http;
        this.cmBase = cmBase;
    }

    // ------------------------------------------------------------------
    //  Campaigns (configurations)
    // ------------------------------------------------------------------

    /**
     * Retrieves the list of campaign configurations for the tenant.
     *
     * @param params optional query filters (filter, limit, page, sort, sortDesc, type,
     *               types, continuous, draft, preview, priority)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getCampaigns(SearchParams params) {
        return http.get(cm("/campaigns"), params, null);
    }

    /**
     * Creates a new campaign configuration.
     *
     * @param jsonBody JSON payload (owner, name, type, priority, …)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> createCampaign(String jsonBody) {
        return http.post(cm("/campaigns"), jsonBody);
    }

    /**
     * Retrieves campaign configuration statistics for the tenant.
     *
     * @param params optional query filters (filterType, …)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getCampaignStats(SearchParams params) {
        return http.get(cm("/campaigns/stats"), params, null);
    }

    /**
     * Retrieves statistics for a specific resource type (e.g. campaign/stats endpoint).
     *
     * @param params optional query filters
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getCampaignResourceStats(SearchParams params) {
        return http.get(cm("/campaign/stats"), params, null);
    }

    /**
     * Retrieves the details of a specific campaign configuration.
     *
     * @param id the campaign configuration ID
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getCampaign(String id) {
        return http.get(cm("/campaigns/" + id), SearchParams.empty(), null);
    }

    /**
     * Updates (replaces) a campaign configuration.
     *
     * @param id       the campaign configuration ID
     * @param jsonBody JSON payload with updated fields
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> updateCampaign(String id, String jsonBody) {
        return http.put(cm("/campaigns/" + id), jsonBody, null);
    }

    /**
     * Deletes a campaign configuration.
     *
     * @param id the campaign configuration ID
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> deleteCampaign(String id) {
        return http.delete(cm("/campaigns/" + id));
    }

    /**
     * Retrieves the list of campaign instances associated with a configuration.
     *
     * @param id     the campaign configuration ID
     * @param params optional query filters (state, sort, sortDesc, …)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getCampaignInstances(String id, SearchParams params) {
        return http.get(cm("/campaigns/" + id + "/instances"), params, null);
    }

    /**
     * Launches a specific campaign configuration (single launch).
     *
     * @param id       the campaign configuration ID (must be in draft mode)
     * @param jsonBody optional JSON body for launch configuration, or {@code "{}"}
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> launchCampaign(String id, String jsonBody) {
        return http.put(cm("/campaigns/" + id + "/launch"), jsonBody, null);
    }

    /**
     * Clones one or more campaign configurations.
     *
     * @param jsonBody JSON array of {@code { "sourceCampaignId": "..." }} objects
     * @return {@code Result<String>} raw JSON (bulk operation results)
     */
    public Result<String> cloneCampaigns(String jsonBody) {
        return http.post(cm("/campaigns/clone"), jsonBody);
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    private String cm(String path) {
        return cmBase + "/itim/cm/v2.0" + path;
    }
}
