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
 * Service wrapper for the IBM Verify Identity Manager ARC <strong>Risk</strong> REST API (v1.0).
 *
 * <p>Covered endpoints under {@code /itim/arc/v1.0/}:
 * <ul>
 *   <li>{@code POST   /risks}                  – create risk or bulk-delete</li>
 *   <li>{@code POST   /risks/search}            – search risks</li>
 *   <li>{@code GET    /risks/{id}}              – get risk details</li>
 *   <li>{@code DELETE /risks/{id}}              – delete risk</li>
 *   <li>{@code PATCH  /risks/{id}}              – update risk</li>
 *   <li>{@code GET    /risks/{id}/activities}   – list activities linked to risk</li>
 *   <li>{@code POST   /risks/{id}/activities}   – link activity to risk</li>
 *   <li>{@code GET    /risks/{id}/mitigations}  – list mitigations linked to risk</li>
 *   <li>{@code POST   /risks/{id}/mitigations}  – link mitigation to risk</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <String>} carrying the raw JSON response.
 */
public class ArcRisksService {

    private final HttpExecutor http;
    private final String       arcBase;

    ArcRisksService(HttpExecutor http, String arcBase) {
        this.http    = http;
        this.arcBase = arcBase;
    }

    // ------------------------------------------------------------------
    //  Risks
    // ------------------------------------------------------------------

    /**
     * Creates a new risk (or bulk-deletes with {@code X-HTTP-Method-Override: DELETE}).
     *
     * @param jsonBody JSON payload
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> createRisk(String jsonBody) {
        return http.post(arc("/risks"), jsonBody);
    }

    /**
     * Searches for risks matching the provided filters.
     *
     * @param params   optional query parameters (filter, level, limit, page, sort,
     *                 sortDesc, status, type)
     * @param jsonBody optional JSON filter body
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> searchRisks(SearchParams params, String jsonBody) {
        String path = arc("/risks/search");
        String qs   = (params != null) ? params.toQueryString() : "";
        String url  = qs.isBlank() ? path : path + "?" + qs;
        return http.post(url, jsonBody == null ? "{}" : jsonBody);
    }

    /**
     * Retrieves the details of a specific risk.
     *
     * @param id the risk ID
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getRisk(String id) {
        return http.get(arc("/risks/" + id), SearchParams.empty(), null);
    }

    /**
     * Deletes a specific risk.
     *
     * @param id the risk ID
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> deleteRisk(String id) {
        return http.delete(arc("/risks/" + id));
    }

    /**
     * Updates a specific risk (PATCH).
     *
     * @param id       the risk ID
     * @param jsonBody JSON patch payload
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> updateRisk(String id, String jsonBody) {
        return http.patch(arc("/risks/" + id), jsonBody);
    }

    /**
     * Retrieves the activities linked to a risk.
     *
     * @param id     the risk ID
     * @param params optional query filters (filter, limit, page, sort, sortDesc)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getRiskActivities(String id, SearchParams params) {
        return http.get(arc("/risks/" + id + "/activities"), params, null);
    }

    /**
     * Links a business activity to a risk.
     *
     * @param id       the risk ID
     * @param jsonBody JSON payload with activity references
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> addRiskActivity(String id, String jsonBody) {
        return http.post(arc("/risks/" + id + "/activities"), jsonBody);
    }

    /**
     * Retrieves the mitigations linked to a risk.
     *
     * @param id     the risk ID
     * @param params optional query filters (filter, limit, page, sort, sortDesc)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getRiskMitigations(String id, SearchParams params) {
        return http.get(arc("/risks/" + id + "/mitigations"), params, null);
    }

    /**
     * Links a mitigation to a risk.
     *
     * @param id       the risk ID
     * @param jsonBody JSON payload with mitigation references
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> addRiskMitigation(String id, String jsonBody) {
        return http.post(arc("/risks/" + id + "/mitigations"), jsonBody);
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    private String arc(String path) {
        return arcBase + "/itim/arc/v1.0" + path;
    }
}
