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
 * Service wrapper for the IBM Verify Identity Manager ARC <strong>Statistics</strong>,
 * <strong>Entity Search</strong> and <strong>Bulk Operations</strong> REST APIs (v1.0).
 *
 * <p>Covered endpoints under {@code /itim/arc/v1.0/}:
 * <ul>
 *   <li>{@code GET  /stats}         – get statistics for an entity type</li>
 *   <li>{@code POST /entity/search} – cross-entity search</li>
 *   <li>{@code POST /bulk}          – perform bulk operations</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <String>} carrying the raw JSON response.
 */
public class ArcStatsService {

    private final HttpExecutor http;
    private final String       arcBase;

    ArcStatsService(HttpExecutor http, String arcBase) {
        this.http    = http;
        this.arcBase = arcBase;
    }

    // ------------------------------------------------------------------
    //  Statistics
    // ------------------------------------------------------------------

    /**
     * Retrieves statistics for a given entity type.
     *
     * @param params query parameters — {@code resourceType} is required
     *               (one of {@code Business Activity}, {@code Mitigation}, {@code Risk},
     *               {@code All}); optional: {@code limit}, {@code page}
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getStats(SearchParams params) {
        return http.get(arc("/stats"), params, null);
    }

    // ------------------------------------------------------------------
    //  Entity Search
    // ------------------------------------------------------------------

    /**
     * Searches for ARC entities (cross-type) based on the provided filters.
     *
     * @param params   optional query parameters (filter, folderId, limit, page,
     *                 sort, sortDesc, type)
     * @param jsonBody optional JSON filter body
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> searchEntities(SearchParams params, String jsonBody) {
        String path = arc("/entity/search");
        String qs   = (params != null) ? params.toQueryString() : "";
        String url  = qs.isBlank() ? path : path + "?" + qs;
        return http.post(url, jsonBody == null ? "{}" : jsonBody);
    }

    // ------------------------------------------------------------------
    //  Bulk Operations
    // ------------------------------------------------------------------

    /**
     * Performs bulk operations across ARC entities.
     *
     * <p>The request body must conform to the {@code V1SwaggerBulkApi} schema —
     * an array of individual operation descriptors.
     *
     * @param jsonBody JSON payload describing the bulk operations to perform
     * @return {@code Result<String>} raw JSON (200 success / 201 created / 207 partial)
     */
    public Result<String> bulk(String jsonBody) {
        return http.post(arc("/bulk"), jsonBody);
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    private String arc(String path) {
        return arcBase + "/itim/arc/v1.0" + path;
    }
}
