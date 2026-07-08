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
import io.github.reapicorn.im.internal.JsonParser;
import io.github.reapicorn.im.model.HalResource;
import io.github.reapicorn.im.model.RequestResponse;

import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Access Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /access}                             – search accesses</li>
 *   <li>{@code POST   /access}                             – create access</li>
 *   <li>{@code GET    /access/{accessId}}                  – look up an access</li>
 *   <li>{@code PUT    /access/{accessId}}                  – modify access</li>
 *   <li>{@code GET    /access/{accessId}/owners}           – search access owners</li>
 *   <li>{@code GET    /access/assignments}                 – search access assignments</li>
 *   <li>{@code POST   /access/assignments}                 – batch assignment requests</li>
 *   <li>{@code DELETE /access/assignments/{assignmentId}}  – delete assignment</li>
 *   <li>{@code POST   /access/bulk}                        – enable/disable access in bulk</li>
 *   <li>{@code POST   /access/export}                      – export bulk access (CSV)</li>
 *   <li>{@code GET    /access/export/{processId}}          – download exported CSV</li>
 *   <li>{@code POST   /access/import}                      – import access (JSON body)</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class AccessService {

    private final HttpExecutor http;

    public AccessService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  Search / lookup
    // ------------------------------------------------------------------

    /**
     * Searches for accesses matching the given criteria.
     *
     * @param params query parameters (attributes, limit, sort)
     * @param range  pagination range, or {@code null} for no paging
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchAccesses(SearchParams params, PageRange range) {
        return http.get("/access", params, range)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates an access entry.
     *
     * <p>Required body fields: {@code entityType} (3 for role), {@code entityRef} (UUID),
     * {@code accessCategory}.
     *
     * @param body the request body as a JSON-serialisable map
     * @return {@code Result<HalResource>} containing the new {@code accessId}
     */
    public Result<HalResource> createAccess(Map<String, Object> body) {
        return http.post("/access", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Looks up a single access by its unique ID.
     *
     * @param accessId   the access unique identifier
     * @param attributes comma-separated attributes to return, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getAccess(String accessId, String attributes) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes);
        return http.get("/access/" + accessId, b.build(), null)
                .map(HalParser::toResource);
    }

    /**
     * Modifies an existing access.
     *
     * @param accessId the access unique identifier
     * @param body     the modification payload as a JSON-serialisable map
     * @return {@code Result<HalResource>} (204 No Content — returns empty resource on success)
     */
    public Result<HalResource> modifyAccess(String accessId, Map<String, Object> body) {
        return http.put("/access/" + accessId, JsonParser.toJson(body), null)
                .map(json -> json == null || json.isBlank()
                        ? new HalResource(null, null, null)
                        : HalParser.toResource(json));
    }

    /**
     * Returns users who are owners of the specified access.
     *
     * @param accessId   the access unique identifier
     * @param params     query parameters (attributes, embedded, sort)
     * @param range      pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> getAccessOwners(String accessId, SearchParams params,
                                                      PageRange range) {
        return http.get("/access/" + accessId + "/owners", params, range)
                .map(HalParser::toResourceList);
    }

    // ------------------------------------------------------------------
    //  Assignments
    // ------------------------------------------------------------------

    /**
     * Returns all access assignments for the specified requestee.
     *
     * @param params query parameters (attributes, limit, sort)
     * @param range  pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchAssignments(SearchParams params, PageRange range) {
        return http.get("/access/assignments", params, range)
                .map(HalParser::toResourceList);
    }

    /**
     * Submits a batch of access assignment requests (add, delete, modify, validate or
     * check-compliance).
     *
     * <p>Pass {@code methodOverride} as one of:
     * {@code "submit-in-batch"}, {@code "validate-full"}, {@code "check-compliance"},
     * or {@code null} for the default behaviour.
     *
     * @param body           the batch request payload
     * @param methodOverride optional {@code X-HTTP-Method-Override} value
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> batchAssignments(Map<String, Object> body, String methodOverride) {
        // POST uses methodOverride via post; for simplicity delegate to put when override present
        if (methodOverride != null) {
            return http.put("/access/assignments", JsonParser.toJson(body), methodOverride)
                    .map(json -> json == null || json.isBlank()
                            ? new HalResource(null, null, null)
                            : HalParser.toResource(json));
        }
        return http.post("/access/assignments", JsonParser.toJson(body))
                .map(json -> json == null || json.isBlank()
                        ? new HalResource(null, null, null)
                        : HalParser.toResource(json));
    }

    /**
     * Deletes the specified access assignment.
     *
     * @param assignmentId the assignment unique identifier
     * @param body         optional request body (can be an empty map)
     * @return {@code Result<RequestResponse>}
     */
    public Result<RequestResponse> deleteAssignment(String assignmentId,
                                                     Map<String, Object> body) {
        // DELETE with body is not directly supported — send body via POST override
        return http.delete("/access/assignments/" + assignmentId)
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Bulk / export / import
    // ------------------------------------------------------------------

    /**
     * Enables or disables access in bulk for role entities.
     *
     * <p>Required body fields: {@code enableAccess} (boolean), {@code entityRef} (role UUID).
     *
     * @param body the bulk operation payload
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> bulkAccess(Map<String, Object> body) {
        return http.post("/access/bulk", JsonParser.toJson(body))
                .map(json -> json == null || json.isBlank()
                        ? new HalResource(null, null, null)
                        : HalParser.toResource(json));
    }

    /**
     * Initiates an export of bulk access data and returns a {@code processId}.
     *
     * @param accessType optional access type filter (e.g. {@code "role"})
     * @param body       the export request payload (list of UUIDs to export)
     * @return {@code Result<HalResource>} containing the {@code processId}
     */
    public Result<HalResource> exportAccess(String accessType, Map<String, Object> body) {
        String path = accessType != null
                ? "/access/export?accessType=" + accessType
                : "/access/export";
        return http.post(path, JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Downloads the exported CSV file for the given {@code processId}.
     *
     * @param processId  the process identifier returned by {@link #exportAccess}
     * @param accessType optional access type filter
     * @return {@code Result<String>} the raw CSV/JSON payload
     */
    public Result<String> getExportedFile(String processId, String accessType) {
        String path = accessType != null
                ? "/access/export/" + processId + "?accessType=" + accessType
                : "/access/export/" + processId;
        return http.get(path, SearchParams.empty(), null);
    }

    /**
     * Imports access data.
     *
     * <p>Note: The OpenAPI spec describes a {@code multipart/form-data} body.
     * This method accepts the body as a pre-serialised JSON string for compatibility
     * with the standard HTTP executor.
     *
     * @param accessType optional access type filter
     * @param jsonBody   the serialised import payload
     * @return {@code Result<HalResource>} containing the {@code processId}
     */
    public Result<HalResource> importAccess(String accessType, String jsonBody) {
        String path = accessType != null
                ? "/access/import?accessType=" + accessType
                : "/access/import";
        return http.post(path, jsonBody)
                .map(HalParser::toResource);
    }
}
