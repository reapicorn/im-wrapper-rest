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
 * Service wrapper for the IBM Verify Identity Manager <strong>Adoption Policy Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /adoptionpolicies}          – search adoption policies</li>
 *   <li>{@code POST   /adoptionpolicies}          – create an adoption policy</li>
 *   <li>{@code GET    /adoptionpolicies/{uuid}}   – look up an adoption policy</li>
 *   <li>{@code PUT    /adoptionpolicies/{uuid}}   – update an adoption policy</li>
 *   <li>{@code DELETE /adoptionpolicies/{uuid}}   – delete an adoption policy</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class AdoptionPoliciesService {

    private final HttpExecutor http;

    public AdoptionPoliciesService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for adoption policies with optional filtering.
     *
     * @param name           filter by name, or {@code null}
     * @param description    filter by description, or {@code null}
     * @param isGlobal       filter by isGlobal flag ({@code "true"} / {@code "false"}), or {@code null}
     * @param serviceProfile filter by service profile name, or {@code null}
     * @param serviceUUID    filter by service UUID, or {@code null}
     * @param params         additional query parameters (attributes, limit, sort)
     * @param range          pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchAdoptionPolicies(String name, String description,
                                                             String isGlobal, String serviceProfile,
                                                             String serviceUUID,
                                                             SearchParams params, PageRange range) {
        StringBuilder path = new StringBuilder("/adoptionpolicies");
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
        if (description != null && !description.isBlank()) {
            path.append(first ? "?" : "&").append("description=").append(description);
            first = false;
        }
        if (isGlobal != null && !isGlobal.isBlank()) {
            path.append(first ? "?" : "&").append("isGlobal=").append(isGlobal);
            first = false;
        }
        if (serviceProfile != null && !serviceProfile.isBlank()) {
            path.append(first ? "?" : "&").append("serviceProfile=").append(serviceProfile);
            first = false;
        }
        if (serviceUUID != null && !serviceUUID.isBlank()) {
            path.append(first ? "?" : "&").append("serviceUUID=").append(serviceUUID);
        }
        return http.get(path.toString(), SearchParams.empty(), range)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates a new adoption policy.
     *
     * @param body adoption policy definition
     * @return {@code Result<HalResource>} (201 Created on success)
     */
    public Result<HalResource> createAdoptionPolicy(Map<String, Object> body) {
        return http.post("/adoptionpolicies", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Returns information about the specified adoption policy.
     *
     * @param uuid       unique identifier of the adoption policy
     * @param attributes comma-separated attributes to return, or {@code null}
     * @param embedded   embedded reference attributes, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getAdoptionPolicy(String uuid, String attributes, String embedded) {
        SearchParams params = SearchParams.builder()
                .attributesRaw(attributes)
                .embedded(embedded != null ? embedded : "")
                .build();
        return http.get("/adoptionpolicies/" + uuid, params, null)
                .map(HalParser::toResource);
    }

    /**
     * Updates the specified adoption policy.
     *
     * @param uuid unique identifier of the adoption policy
     * @param body updated adoption policy definition
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> updateAdoptionPolicy(String uuid, Map<String, Object> body) {
        return http.put("/adoptionpolicies/" + uuid, JsonParser.toJson(body), null);
    }

    /**
     * Deletes the specified adoption policy.
     *
     * @param uuid unique identifier of the adoption policy
     * @return {@code Result<String>} (204 No Content on success)
     */
    public Result<String> deleteAdoptionPolicy(String uuid) {
        return http.delete("/adoptionpolicies/" + uuid);
    }
}
