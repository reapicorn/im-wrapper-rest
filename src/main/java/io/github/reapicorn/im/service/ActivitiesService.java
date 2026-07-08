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
 * Service wrapper for the IBM Verify Identity Manager <strong>Activity Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET /activities}                                      – search activities</li>
 *   <li>{@code GET /activities/{activityId}}                         – look up an activity</li>
 *   <li>{@code GET /activities/count}                                – pending count for current user</li>
 *   <li>{@code GET /activities/quicksearches}                        – quick search categories</li>
 *   <li>{@code GET /activities/complianceDetails/{activityId}}       – compliance issues for activity</li>
 *   <li>{@code GET /activities/rfiformdetails/{workitemId}}          – RFI form details</li>
 *   <li>{@code GET /activities/userrecertdetails/{workitemId}}       – user re-certification details</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class ActivitiesService {

    private final HttpExecutor http;

    public ActivitiesService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  Search / lookup
    // ------------------------------------------------------------------

    /**
     * Searches for activities matching the given criteria.
     *
     * @param params query parameters (limit, sort)
     * @param range  pagination range, or {@code null} for no paging
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchActivities(SearchParams params, PageRange range) {
        return http.get("/activities", params, range)
                .map(HalParser::toResourceList);
    }

    /**
     * Returns information about the specified activity.
     *
     * @param activityId     the activity unique identifier
     * @param participantLimit optional limit on the number of participants returned, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getActivity(String activityId, Integer participantLimit) {
        String path = participantLimit != null
                ? "/activities/" + activityId + "?participantLimit=" + participantLimit
                : "/activities/" + activityId;
        return http.get(path, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Returns the count of pending activities for the current authenticated user.
     *
     * @param status optional status filter (currently only {@code "PENDING"} is supported),
     *               or {@code null} for the default
     * @return {@code Result<HalResource>} — access the count via {@code _attributes.count}
     */
    public Result<HalResource> getActivityCount(String status) {
        String path = status != null
                ? "/activities/count?status=" + status
                : "/activities/count";
        return http.get(path, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Returns quick-search categories for activities, sorted by due date.
     *
     * @param limit optional maximum number of items to return, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getQuickSearches(Integer limit) {
        SearchParams.Builder b = SearchParams.builder();
        if (limit != null) b.limit(limit);
        return http.get("/activities/quicksearches", b.build(), null)
                .map(HalParser::toResource);
    }

    // ------------------------------------------------------------------
    //  Detail endpoints
    // ------------------------------------------------------------------

    /**
     * Returns compliance issues to be resolved for the specified todo activity.
     *
     * @param activityId the activity unique identifier
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> getComplianceDetails(String activityId) {
        return http.get("/activities/complianceDetails/" + activityId,
                        SearchParams.empty(), null)
                .map(HalParser::toResourceList);
    }

    /**
     * Returns RFI (Request For Information) form details for the specified work item.
     *
     * @param workitemId the work item unique identifier
     * @return {@code Result<HalResource>} containing template, default attribute values and operation info
     */
    public Result<HalResource> getRfiFormDetails(String workitemId) {
        return http.get("/activities/rfiformdetails/" + workitemId,
                        SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Returns user re-certification details for the specified work item.
     *
     * @param workitemId the work item unique identifier
     * @return {@code Result<HalResource>} containing roles and accounts to certify
     */
    public Result<HalResource> getUserRecertDetails(String workitemId) {
        return http.get("/activities/userrecertdetails/" + workitemId,
                        SearchParams.empty(), null)
                .map(HalParser::toResource);
    }
}
