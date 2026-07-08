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
 * Service wrapper for the IBM Verify Identity Manager ARC <strong>Business Activity</strong> and
 * <strong>Activity Folder</strong> REST APIs (v1.0).
 *
 * <p>Covered endpoints under {@code /itim/arc/v1.0/}:
 * <ul>
 *   <li>{@code POST   /activities}                    – create activity or bulk-delete</li>
 *   <li>{@code POST   /activities/search}             – search activities</li>
 *   <li>{@code GET    /activities/{id}}               – get activity details</li>
 *   <li>{@code DELETE /activities/{id}}               – delete activity</li>
 *   <li>{@code PATCH  /activities/{id}}               – update activity</li>
 *   <li>{@code GET    /activities/{id}/permissions}   – list permissions linked to activity</li>
 *   <li>{@code POST   /activities/{id}/permissions}   – link permission to activity</li>
 *   <li>{@code GET    /activities/{id}/risks}         – list risks linked to activity</li>
 *   <li>{@code POST   /activityfolders}               – create folder or bulk-delete</li>
 *   <li>{@code POST   /activityfolders/search}        – search folders</li>
 *   <li>{@code GET    /activityfolders/{id}}          – get folder details</li>
 *   <li>{@code DELETE /activityfolders/{id}}          – delete folder</li>
 *   <li>{@code PATCH  /activityfolders/{id}}          – update folder</li>
 *   <li>{@code GET    /activityfolders/{id}/children} – list children of folder</li>
 *   <li>{@code POST   /activityfolders/{id}/children} – update folder-children relation</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <String>} carrying the raw JSON response.
 */
public class ArcActivitiesService {

    private final HttpExecutor http;
    private final String       arcBase;

    ArcActivitiesService(HttpExecutor http, String arcBase) {
        this.http    = http;
        this.arcBase = arcBase;
    }

    // ------------------------------------------------------------------
    //  Business Activities
    // ------------------------------------------------------------------

    /**
     * Creates a new business activity (or bulk-deletes when
     * {@code X-HTTP-Method-Override: DELETE} is needed — pass the body accordingly).
     *
     * @param jsonBody JSON payload
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> createActivity(String jsonBody) {
        return http.post(arc("/activities"), jsonBody);
    }

    /**
     * Searches for business activities matching the provided filters.
     *
     * @param params optional query filters (filter, folderId, limit, page, sort, sortDesc)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> searchActivities(SearchParams params) {
        return http.post(arc("/activities/search"), "{}");
    }

    /**
     * Searches for business activities — POST body variant (for richer filter payloads).
     *
     * @param params   optional query parameters appended to the URL
     * @param jsonBody optional JSON filter body
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> searchActivities(SearchParams params, String jsonBody) {
        String path = arc("/activities/search");
        String qs   = (params != null) ? params.toQueryString() : "";
        String url  = qs.isBlank() ? path : path + "?" + qs;
        return http.post(url, jsonBody == null ? "{}" : jsonBody);
    }

    /**
     * Retrieves the details of a specific business activity.
     *
     * @param id the activity ID
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getActivity(String id) {
        return http.get(arc("/activities/" + id), SearchParams.empty(), null);
    }

    /**
     * Deletes a specific business activity.
     *
     * @param id the activity ID
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> deleteActivity(String id) {
        return http.delete(arc("/activities/" + id));
    }

    /**
     * Updates the details of a business activity (PATCH).
     *
     * @param id       the activity ID
     * @param jsonBody JSON patch payload
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> updateActivity(String id, String jsonBody) {
        return http.patch(arc("/activities/" + id), jsonBody);
    }

    /**
     * Retrieves the permissions linked to a business activity.
     *
     * @param id     the activity ID
     * @param params optional query filters (filter, limit, page, sort, sortDesc)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getActivityPermissions(String id, SearchParams params) {
        return http.get(arc("/activities/" + id + "/permissions"), params, null);
    }

    /**
     * Links a permission to a business activity.
     *
     * @param id       the activity ID
     * @param jsonBody JSON payload
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> addActivityPermission(String id, String jsonBody) {
        return http.post(arc("/activities/" + id + "/permissions"), jsonBody);
    }

    /**
     * Retrieves the risks linked to a business activity.
     *
     * @param id     the activity ID
     * @param params optional query filters (filter, limit, page, sort, sortDesc)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getActivityRisks(String id, SearchParams params) {
        return http.get(arc("/activities/" + id + "/risks"), params, null);
    }

    // ------------------------------------------------------------------
    //  Activity Folders
    // ------------------------------------------------------------------

    /**
     * Creates a new activity folder (or bulk-deletes with override header).
     *
     * @param jsonBody JSON payload
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> createActivityFolder(String jsonBody) {
        return http.post(arc("/activityfolders"), jsonBody);
    }

    /**
     * Searches for activity folders matching the provided filters.
     *
     * @param params   optional query parameters
     * @param jsonBody optional JSON filter body
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> searchActivityFolders(SearchParams params, String jsonBody) {
        String path = arc("/activityfolders/search");
        String qs   = (params != null) ? params.toQueryString() : "";
        String url  = qs.isBlank() ? path : path + "?" + qs;
        return http.post(url, jsonBody == null ? "{}" : jsonBody);
    }

    /**
     * Retrieves the details of a specific activity folder.
     *
     * @param id the folder ID
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getActivityFolder(String id) {
        return http.get(arc("/activityfolders/" + id), SearchParams.empty(), null);
    }

    /**
     * Deletes a specific activity folder.
     *
     * @param id             the folder ID
     * @param removeChildren whether to also remove child folders/activities
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> deleteActivityFolder(String id, boolean removeChildren) {
        String path = arc("/activityfolders/" + id
                + (removeChildren ? "?removeChildren=true" : ""));
        return http.delete(path);
    }

    /**
     * Updates a specific activity folder (PATCH).
     *
     * @param id       the folder ID
     * @param jsonBody JSON patch payload
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> updateActivityFolder(String id, String jsonBody) {
        return http.patch(arc("/activityfolders/" + id), jsonBody);
    }

    /**
     * Retrieves the children (activities or sub-folders) of an activity folder.
     *
     * @param id     the folder ID
     * @param params optional query filters (filter, limit, page, sort, sortDesc)
     * @return {@code Result<String>} raw JSON
     */
    public Result<String> getActivityFolderChildren(String id, SearchParams params) {
        return http.get(arc("/activityfolders/" + id + "/children"), params, null);
    }

    /**
     * Updates the relation between an activity folder and its children.
     *
     * @param id       the folder ID
     * @param jsonBody JSON payload describing children relations
     * @return {@code Result<String>} raw body (empty on 204)
     */
    public Result<String> updateActivityFolderChildren(String id, String jsonBody) {
        return http.post(arc("/activityfolders/" + id + "/children"), jsonBody);
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    private String arc(String path) {
        return arcBase + "/itim/arc/v1.0" + path;
    }
}
