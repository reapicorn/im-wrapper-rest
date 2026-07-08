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
import io.github.reapicorn.im.SearchParams;
import io.github.reapicorn.im.internal.HalParser;
import io.github.reapicorn.im.internal.HttpExecutor;
import io.github.reapicorn.im.internal.JsonParser;
import io.github.reapicorn.im.model.HalResource;

import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>View Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /views}         – search views</li>
 *   <li>{@code POST   /views}         – create a view</li>
 *   <li>{@code GET    /views/{id}}    – get a view</li>
 *   <li>{@code PUT    /views/{id}}    – update a view</li>
 *   <li>{@code DELETE /views/{id}}    – delete a view</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class ViewsService {

    private final HttpExecutor http;

    public ViewsService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for views, optionally filtering by name.
     *
     * @param filter     search pattern for view name, or {@code null} for all views
     * @param attributes comma-separated attributes to return, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchViews(String filter, String attributes) {
        StringBuilder path = new StringBuilder("/views");
        boolean first = true;
        if (attributes != null && !attributes.isBlank()) {
            path.append("?attributes=").append(attributes);
            first = false;
        }
        if (filter != null && !filter.isBlank()) {
            path.append(first ? "?" : "&").append("filter=").append(filter);
        }
        return http.get(path.toString(), SearchParams.empty(), null)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates a new view.
     *
     * @param body view definition — required: {@code name}; optional: {@code description}, {@code tasks}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> createView(Map<String, Object> body) {
        return http.post("/views", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Returns details of the specified view.
     *
     * @param id unique identifier for the view
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getView(String id) {
        return http.get("/views/" + id, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Updates the specified view.
     *
     * @param id   unique identifier for the view
     * @param body updated view definition
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> updateView(String id, Map<String, Object> body) {
        return http.put("/views/" + id, JsonParser.toJson(body), null);
    }

    /**
     * Deletes the specified view.
     *
     * @param id unique identifier for the view
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> deleteView(String id) {
        return http.delete("/views/" + id);
    }
}
