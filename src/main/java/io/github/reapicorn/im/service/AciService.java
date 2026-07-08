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

import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>ACI (Access Control Item) Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /acis}        – search ACIs</li>
 *   <li>{@code POST   /acis}        – create an ACI</li>
 *   <li>{@code GET    /acis/{id}}   – look up an ACI</li>
 *   <li>{@code PUT    /acis/{id}}   – update an ACI</li>
 *   <li>{@code DELETE /acis/{id}}   – delete an ACI</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class AciService {

    private final HttpExecutor http;

    public AciService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for ACIs with optional filtering.
     *
     * @param name   filter ACIs by name, or {@code null}
     * @param orgID  filter ACIs by organisation UUID, or {@code null}
     * @param target filter ACIs by type, or {@code null}
     * @param params additional query parameters (limit, sort)
     * @param range  pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchAcis(String name, String orgID, String target,
                                                 SearchParams params, PageRange range) {
        StringBuilder path = new StringBuilder("/acis");
        String qs = params.toQueryString();
        boolean first = true;
        if (!qs.isBlank()) {
            path.append("?").append(qs);
            first = false;
        }
        if (name != null && !name.isBlank()) {
            path.append(first ? "?" : "&").append("name=").append(name);
            first = false;
        }
        if (orgID != null && !orgID.isBlank()) {
            path.append(first ? "?" : "&").append("orgID=").append(orgID);
            first = false;
        }
        if (target != null && !target.isBlank()) {
            path.append(first ? "?" : "&").append("target=").append(target);
        }
        return http.get(path.toString(), SearchParams.empty(), range)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates a new ACI.
     *
     * @param body ACI definition (ACIRequestBean structure)
     * @return {@code Result<HalResource>} (201 Created on success)
     */
    public Result<HalResource> createAci(Map<String, Object> body) {
        return http.post("/acis", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Returns information about the specified ACI.
     *
     * @param id unique identifier of the ACI
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getAci(String id) {
        return http.get("/acis/" + id, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Updates the specified ACI.
     *
     * @param id   unique identifier of the ACI
     * @param body updated ACI definition (ACIRequestBean structure)
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> updateAci(String id, Map<String, Object> body) {
        return http.put("/acis/" + id, JsonParser.toJson(body), null);
    }

    /**
     * Deletes the specified ACI.
     *
     * @param id unique identifier of the ACI
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> deleteAci(String id) {
        return http.delete("/acis/" + id);
    }
}
