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
import io.github.reapicorn.im.model.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Role</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET /roles/orgroles}  – search organisational roles (static/dynamic)</li>
 *   <li>{@code GET /roles/{roleId}}  – look up a role</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class RolesService {

    private final HttpExecutor http;

    public RolesService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  Search / lookup
    // ------------------------------------------------------------------

    /**
     * Searches for organisational roles.
     *
     * @param attributes comma-separated attributes to return, or {@code "*"} for all
     *                   (e.g. {@code "errolename"})
     * @param type       role type filter: {@code "static"}, {@code "dynamic"}, or {@code "all"}
     *                   (null uses server default = static)
     * @param limit      maximum number of results (null = server default)
     * @param sort       sort expression (e.g. {@code "+errolename"})
     * @return {@code Result<List<Role>>}
     */
    public Result<List<Role>> searchRoles(String attributes, String type,
                                          String limit, String sort) {
        SearchParams.Builder b = SearchParams.builder()
                .attributesRaw(attributes)
                .sort(sort);
        if (limit != null) b.limit(Integer.parseInt(limit));

        // 'type' is a custom param not in SearchParams — append manually
        String qs = b.build().toQueryString();
        if (type != null && !type.isBlank()) {
            qs = qs.isBlank() ? "type=" + type : qs + "&type=" + type;
        }
        String path = "/roles/orgroles" + (qs.isBlank() ? "" : "?" + qs);
        return http.get(path, SearchParams.empty(), null)
                .map(json -> toRoleList(HalParser.toResourceList(json)));
    }

    /**
     * Looks up a role by its unique ID.
     *
     * @param roleId     the role's UUID (e.g. {@code fdd7091a-2b90-425a-951c-042878e35ffc})
     * @param attributes optional comma-separated attribute list (null = server defaults)
     * @param embedded   optional embedded reference attributes (null = none)
     * @return {@code Result<Role>}
     */
    public Result<Role> getRole(String roleId, String attributes, String embedded) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes);
        if (embedded != null) b.embedded(embedded);
        return http.get("/roles/" + roleId, b.build(), null)
                .map(json -> toRole(HalParser.toResource(json)));
    }

    // ------------------------------------------------------------------
    //  Domain mapping helpers
    // ------------------------------------------------------------------

    private static List<Role> toRoleList(List<HalResource> resources) {
        List<Role> list = new ArrayList<>(resources.size());
        for (HalResource r : resources) {
            list.add(toRole(r));
        }
        return list;
    }

    private static Role toRole(HalResource r) {
        Role role = new Role();
        role.setId(r.getId());
        role.setHref(r.getHref());
        role.setRoleName(r.getTitle());
        Map<String, Object> attrs = r.getAttributes();
        if (!attrs.isEmpty()) {
            role.setAttributes(attrs);
            Object name = attrs.get("errolename");
            if (name != null) role.setRoleName(name.toString());
        }
        return role;
    }
}
