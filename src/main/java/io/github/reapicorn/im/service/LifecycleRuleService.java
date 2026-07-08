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
 * Service wrapper for the IBM Verify Identity Manager <strong>Lifecycle Rule Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /lifecyclerule}                                  – search lifecycle rules</li>
 *   <li>{@code POST   /lifecyclerule}                                  – create a lifecycle rule</li>
 *   <li>{@code GET    /lifecyclerule/{lifecycleRuleIdentifier}}        – look up a lifecycle rule</li>
 *   <li>{@code PUT    /lifecyclerule/{lifecycleRuleIdentifier}}        – modify a lifecycle rule</li>
 *   <li>{@code DELETE /lifecyclerule/{lifecycleRuleIdentifier}}        – remove a lifecycle rule</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class LifecycleRuleService {

    private final HttpExecutor http;

    public LifecycleRuleService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for lifecycle rules by type.
     *
     * <p>{@code ruleType} and {@code typeInfo} are required by the API.
     *
     * @param ruleType   rule type to search ({@code PROFILE_TYPE}, {@code CATEGORY_TYPE},
     *                   or {@code RECERT_POLICY_TYPE})
     * @param typeInfo   type info string (e.g. {@code "Global"}, {@code "Account"}, profile name)
     * @param attributes comma-separated attributes to return, or {@code null}
     * @param range      pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchLifecycleRules(String ruleType, String typeInfo,
                                                           String attributes, PageRange range) {
        StringBuilder path = new StringBuilder("/lifecyclerule");
        boolean first = true;
        if (ruleType != null && !ruleType.isBlank()) {
            path.append("?ruleType=").append(ruleType);
            first = false;
        }
        if (typeInfo != null && !typeInfo.isBlank()) {
            path.append(first ? "?" : "&").append("typeInfo=").append(typeInfo);
            first = false;
        }
        if (attributes != null && !attributes.isBlank()) {
            path.append(first ? "?" : "&").append("attributes=").append(attributes);
        }
        return http.get(path.toString(), SearchParams.empty(), range)
                .map(HalParser::toResourceList);
    }

    /**
     * Creates a new lifecycle rule.
     *
     * @param body lifecycle rule definition
     * @return {@code Result<HalResource>} (201 Created on success)
     */
    public Result<HalResource> createLifecycleRule(Map<String, Object> body) {
        return http.post("/lifecyclerule", JsonParser.toJson(body))
                .map(HalParser::toResource);
    }

    /**
     * Returns information about the specified lifecycle rule.
     *
     * @param lifecycleRuleIdentifier unique identifier of the lifecycle rule
     * @param attributes              comma-separated attributes to return, or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getLifecycleRule(String lifecycleRuleIdentifier, String attributes) {
        SearchParams params = SearchParams.builder()
                .attributesRaw(attributes)
                .build();
        return http.get("/lifecyclerule/" + lifecycleRuleIdentifier, params, null)
                .map(HalParser::toResource);
    }

    /**
     * Modifies the specified lifecycle rule.
     *
     * @param lifecycleRuleIdentifier unique identifier of the lifecycle rule
     * @param body                    updated lifecycle rule definition
     * @return {@code Result<String>} (202 Accepted on success)
     */
    public Result<String> updateLifecycleRule(String lifecycleRuleIdentifier, Map<String, Object> body) {
        return http.put("/lifecyclerule/" + lifecycleRuleIdentifier, JsonParser.toJson(body), null);
    }

    /**
     * Removes the specified lifecycle rule.
     *
     * @param lifecycleRuleIdentifier unique identifier of the lifecycle rule
     * @return {@code Result<String>} (200 OK on success)
     */
    public Result<String> deleteLifecycleRule(String lifecycleRuleIdentifier) {
        return http.delete("/lifecyclerule/" + lifecycleRuleIdentifier);
    }
}
