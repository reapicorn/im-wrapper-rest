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
 * Service wrapper for the IBM Verify Identity Manager ARC <strong>Mitigation</strong> REST API (v1.0).
 *
 * <p>Covered endpoints under {@code /itim/arc/v1.0/}:
 * <ul>
 *   <li>{@code POST   /mitigations}              – create mitigation or bulk-delete</li>
 *   <li>{@code POST   /mitigations/search}        – search mitigations</li>
 *   <li>{@code GET    /mitigations/{id}}          – get mitigation details</li>
 *   <li>{@code DELETE /mitigations/{id}}          – delete mitigation</li>
 *   <li>{@code PATCH  /mitigations/{id}}          – update mitigation</li>
 *   <li>{@code GET    /mitigations/{id}/risks}    – list risks linked to mitigation</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <String>} carrying the raw JSON response.
 */
public class ArcMitigationsService {

    private final HttpExecutor http;
    private final String       arcBase;

    ArcMitigationsService(HttpExecutor http, String arcBase) {
        this.http    = http;
        this.arcBase = arcBase;
    }

    // ------------------------------------------------------------------
    //  Mitigations
    // ------------------------------------------------------------------

    /**
     * Creates a new mitigation (or bulk-deletes with {@code X-HTTP-Method-Override: DELETE}).
     *
     * @param jsonBody JSON payload
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> createMitigation(String jsonBody) {
        return http.post(arc("/mitigations"), jsonBody);
    }

    /**
     * Searches for mitigations matching the provided filters.
     *
     * @param params   optional query parameters (filter, limit, page, sort, sortDesc)
     * @param jsonBody optional JSON filter body
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> searchMitigations(SearchParams params, String jsonBody) {
        String path = arc("/mitigations/search");
        String qs   = (params != null) ? params.toQueryString() : "";
        String url  = qs.isBlank() ? path : path + "?" + qs;
        return http.post(url, jsonBody == null ? "{}" : jsonBody);
    }

    /**
     * Retrieves the details of a specific mitigation.
     *
     * @param id the mitigation ID
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getMitigation(String id) {
        return http.get(arc("/mitigations/" + id), SearchParams.empty(), null);
    }

    /**
     * Deletes a specific mitigation.
     *
     * @param id the mitigation ID
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> deleteMitigation(String id) {
        return http.delete(arc("/mitigations/" + id));
    }

    /**
     * Updates a specific mitigation (PATCH).
     *
     * @param id       the mitigation ID
     * @param jsonBody JSON patch payload
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> updateMitigation(String id, String jsonBody) {
        return http.patch(arc("/mitigations/" + id), jsonBody);
    }

    /**
     * Retrieves the risks linked to a mitigation.
     *
     * @param id     the mitigation ID
     * @param params optional query filters (filter, limit, page, sort, sortDesc)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getMitigationRisks(String id, SearchParams params) {
        return http.get(arc("/mitigations/" + id + "/risks"), params, null);
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    private String arc(String path) {
        return arcBase + "/itim/arc/v1.0" + path;
    }
}
