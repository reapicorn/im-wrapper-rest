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
 * Service wrapper for the IBM Verify Identity Manager <strong>Workflow Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET /workflows}              – search workflows (type is required)</li>
 *   <li>{@code GET /workflows/{workflowId}} – look up a workflow by UUID</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class WorkflowsService {

    private final HttpExecutor http;

    public WorkflowsService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for workflows of the specified type.
     *
     * <p>The {@code type} parameter is required by the server. Valid values are:
     * {@code OPERATIONAL}, {@code ACCOUNT_REQUEST}, {@code ACCESS_REQUEST}.
     *
     * <p>Additional optional parameters are passed through {@code params}:
     * {@code attributes}, {@code limit}, {@code sort}. The parameters
     * {@code name}, {@code entityName}, {@code entityType}, {@code operationLevel},
     * {@code serviceName}, {@code accessName} can be appended to the query string
     * manually or by extending this method.
     *
     * @param type   workflow type (required) — {@code OPERATIONAL}, {@code ACCOUNT_REQUEST}
     *               or {@code ACCESS_REQUEST}
     * @param params additional query parameters (attributes, limit, sort)
     * @param range  pagination range, or {@code null} for no paging
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchWorkflows(String type, SearchParams params,
                                                      PageRange range) {
        String qs = params.toQueryString();
        String path = "/workflows?type=" + type + (qs.isBlank() ? "" : "&" + qs);
        return http.get(path, SearchParams.empty(), range)
                .map(HalParser::toResourceList);
    }

    /**
     * Returns information about the workflow identified by the given UUID.
     *
     * @param workflowId the UUID of the workflow
     * @param attributes comma-separated attributes to return, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getWorkflow(String workflowId, String attributes) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes);
        return http.get("/workflows/" + workflowId, b.build(), null)
                .map(HalParser::toResource);
    }
}
