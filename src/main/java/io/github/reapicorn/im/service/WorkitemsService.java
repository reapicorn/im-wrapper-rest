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

import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.internal.HalParser;
import io.github.reapicorn.im.internal.HttpExecutor;
import io.github.reapicorn.im.internal.JsonParser;
import io.github.reapicorn.im.model.HalResource;

import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Work Item Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code PUT  /workitems}                         – batch modify work items</li>
 *   <li>{@code PUT  /workitems/{workitemId}}             – modify a single work item</li>
 *   <li>{@code POST /workitems/{workitemId}}             – check recertification impact</li>
 *   <li>{@code PUT  /workitems/userrecert/{workitemId}}  – modify user re-certification work item</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class WorkitemsService {

    private final HttpExecutor http;

    public WorkitemsService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Modifies a single work item by executing an action for the activity.
     *
     * <p>Common action codes:
     * <ul>
     *   <li>{@code AA} – approve</li>
     *   <li>{@code AR} – reject</li>
     *   <li>{@code SS} – work order success / correct noncompliance</li>
     *   <li>{@code SW} – work order partial success</li>
     *   <li>{@code SF} – work order failure</li>
     *   <li>{@code RS} – RFI submission</li>
     * </ul>
     *
     * @param workitemId the work item unique identifier
     * @param body       the action payload (must include the action code and any required attributes)
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> modifyWorkitem(String workitemId, Map<String, Object> body) {
        return http.put("/workitems/" + workitemId, JsonParser.toJson(body), null)
                .map(json -> json == null || json.isBlank()
                        ? new HalResource(null, null, null)
                        : HalParser.toResource(json));
    }

    /**
     * Modifies a batch of work items (approve or reject).
     *
     * <p>Pass {@code methodOverride} as {@code "submit-in-batch"} to submit.
     *
     * @param items          list of work item update beans (each as a map)
     * @param methodOverride optional method override value (e.g. {@code "submit-in-batch"})
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> batchModifyWorkitems(List<?> items, String methodOverride) {
        return http.put("/workitems", JsonParser.toJson(items), methodOverride)
                .map(json -> json == null || json.isBlank()
                        ? new HalResource(null, null, null)
                        : HalParser.toResource(json));
    }

    /**
     * Modifies a user re-certification work item.
     *
     * @param workitemId the work item unique identifier
     * @param body       the recertification payload
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> modifyUserRecertWorkitem(String workitemId,
                                                         Map<String, Object> body) {
        return http.put("/workitems/userrecert/" + workitemId, JsonParser.toJson(body), null)
                .map(json -> json == null || json.isBlank()
                        ? new HalResource(null, null, null)
                        : HalParser.toResource(json));
    }

    /**
     * Evaluates the impact of a user re-certification action without committing it.
     *
     * <p>Pass {@code methodOverride} as {@code "check-recertification"}.
     *
     * @param workitemId     the work item unique identifier
     * @param body           the re-certification details payload
     * @param methodOverride the method override value ({@code "check-recertification"})
     * @return {@code Result<HalResource>} containing impact analysis results
     */
    public Result<HalResource> checkRecertificationImpact(String workitemId,
                                                           Map<String, Object> body,
                                                           String methodOverride) {
        return http.put("/workitems/" + workitemId, JsonParser.toJson(body), methodOverride)
                .map(json -> json == null || json.isBlank()
                        ? new HalResource(null, null, null)
                        : HalParser.toResource(json));
    }
}
