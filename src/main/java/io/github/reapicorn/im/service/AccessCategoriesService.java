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
import io.github.reapicorn.im.model.HalResource;

import java.util.List;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Access Categories</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET /accesscategories}                                            – list all access categories</li>
 *   <li>{@code GET /accesscategories/{accessCategoryID}}                         – look up a category</li>
 *   <li>{@code GET /accesscategories/{accessCategoryID}/childaccesscategories}   – sub-categories</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class AccessCategoriesService {

    private final HttpExecutor http;

    public AccessCategoriesService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Returns all access categories defined in the system.
     *
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchAccessCategories() {
        return http.get("/accesscategories", SearchParams.empty(), null)
                .map(HalParser::toResourceList);
    }

    /**
     * Returns information about the specified access category.
     *
     * @param accessCategoryId the unique identifier of the access category
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getAccessCategory(String accessCategoryId) {
        return http.get("/accesscategories/" + accessCategoryId, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Returns all sub-categories (children) of the specified access category.
     *
     * @param accessCategoryId the unique identifier of the parent access category
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> getChildCategories(String accessCategoryId) {
        return http.get("/accesscategories/" + accessCategoryId + "/childaccesscategories",
                        SearchParams.empty(), null)
                .map(HalParser::toResourceList);
    }
}
