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
import io.github.reapicorn.im.model.HalResource;

import java.util.List;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Requests</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET  /requests}                    – search requests</li>
 *   <li>{@code GET  /requests/{requestId}}         – look up a request (use for polling 202)</li>
 *   <li>{@code GET  /requests/{requestId}/log}     – get request log file</li>
 *   <li>{@code POST /requests/cancel/{requestId}}  – cancel a pending request</li>
 *   <li>{@code GET  /requests/quicksearches}       – quick search categories</li>
 * </ul>
 *
 * <p>{@link #getRequest(String)} is the primary method for polling the outcome of
 * asynchronous operations that returned HTTP 202 — pass the {@code requestId} from the
 * {@code RequestResponse} and inspect the status in {@code _attributes.status.key}.
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class RequestsService {

    private final HttpExecutor http;

    public RequestsService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  Search / lookup
    // ------------------------------------------------------------------

    /**
     * Searches for requests matching the given criteria.
     *
     * @param params query parameters (limit, sort)
     * @param range  pagination range, or {@code null} for no paging
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchRequests(SearchParams params, PageRange range) {
        return http.get("/requests", params, range)
                .map(HalParser::toResourceList);
    }

    /**
     * Returns information about the specified request.
     *
     * <p>This is the method to call when <strong>polling</strong> the outcome of an
     * asynchronous operation (HTTP 202). Inspect {@code _attributes.status.key} in the
     * returned resource — common values are {@code RequestAuditData.Status.PENDING},
     * {@code FULFILLED}, {@code FAILED}, etc.
     *
     * @param requestId  the unique identifier of the request (from {@code RequestResponse})
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getRequest(String requestId) {
        return http.get("/requests/" + requestId, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Returns information about the specified request, optionally scoped to an activity.
     *
     * @param requestId  the unique identifier of the request
     * @param activityId optional activity ID, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getRequest(String requestId, String activityId) {
        String path = activityId != null
                ? "/requests/" + requestId + "?activityID=" + activityId
                : "/requests/" + requestId;
        return http.get(path, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Returns the log file for the specified request.
     *
     * <p>Useful for import/export operations where error detail is stored in a log.
     *
     * @param requestId  the unique identifier of the request
     * @param accessType optional access type filter (e.g. {@code "role"})
     * @return {@code Result<String>} the raw log content
     */
    public Result<String> getRequestLog(String requestId, String accessType) {
        String path = accessType != null
                ? "/requests/" + requestId + "/log?accessType=" + accessType
                : "/requests/" + requestId + "/log";
        return http.get(path, SearchParams.empty(), null);
    }

    // ------------------------------------------------------------------
    //  Mutations
    // ------------------------------------------------------------------

    /**
     * Cancels the pending request identified by {@code requestId}.
     *
     * @param requestId the unique identifier of the request to cancel
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> cancelRequest(String requestId) {
        return http.post("/requests/cancel/" + requestId, "")
                .map(json -> json == null || json.isBlank()
                        ? new HalResource(null, null, null)
                        : HalParser.toResource(json));
    }

    // ------------------------------------------------------------------
    //  Quick searches
    // ------------------------------------------------------------------

    /**
     * Returns the quick-search categories for requests.
     *
     * <p>The returned resource contains pre-defined filter options (statuses, accesses,
     * requestees, actions) to narrow down request searches.
     *
     * @param limit optional maximum number of items per category, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getQuickSearches(Integer limit) {
        SearchParams.Builder b = SearchParams.builder();
        if (limit != null) b.limit(limit);
        return http.get("/requests/quicksearches", b.build(), null)
                .map(HalParser::toResource);
    }
}
