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
 * Service wrapper for the IBM Verify Identity Manager <strong>Entity Search</strong> REST API.
 *
 * <p>These endpoints work in conjunction with form template REST API. The encrypted
 * access token (filter or object class) is obtained from a form template widget and
 * passed as the {@code io.github.reapicorn.im.rest.accesstoken} header.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET /entities}                – widget filter search using an encrypted LDAP filter</li>
 *   <li>{@code GET /entities/attributeslist} – attribute names lookup using an encrypted object class</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class EntitiesService {

    private final HttpExecutor http;

    public EntitiesService(HttpExecutor http) {
        this.http = http;
    }

    /**
     * Searches for entities matching the encrypted LDAP filter token.
     *
     * <p>The {@code accessToken} must be the encrypted filter obtained from a form template
     * widget via the form template REST API. The server decrypts it and executes the LDAP search.
     *
     * <p><strong>Note:</strong> The {@code io.github.reapicorn.im.rest.accesstoken} header is passed as
     * an additional query parameter appended to the path because the standard
     * {@code SearchParams} does not carry custom headers. Callers that need to pass this
     * token should use the lower-level {@link io.github.reapicorn.im.internal.HttpExecutor} directly
     * or extend this method with header support if needed.
     *
     * @param accessToken the encrypted filter token from a form template widget (required)
     * @param attributes  single attribute name to return, or {@code null}
     * @param limit       maximum results, or {@code null}
     * @param range       pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchEntities(String accessToken, String attributes,
                                                     Integer limit, PageRange range) {
        SearchParams.Builder b = SearchParams.builder();
        if (attributes != null) b.attributesRaw(attributes);
        if (limit != null)      b.limit(limit);
        // The access token is passed as a custom header internally.
        // Build the path with the token as a query param so HttpExecutor can forward it.
        String path = "/entities?io.github.reapicorn.im.rest.accesstoken=" + accessToken;
        return http.get(path, SearchParams.empty(), range)
                .map(HalParser::toResourceList);
    }

    /**
     * Returns the list of attributes configured for an object class encoded in the access token.
     *
     * <p>The {@code accessToken} must be the encrypted object class obtained from a form template
     * widget.
     *
     * @param accessToken the encrypted object class token from a form template widget (required)
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> getAttributesList(String accessToken) {
        String path = "/entities/attributeslist?io.github.reapicorn.im.rest.accesstoken=" + accessToken;
        return http.get(path, SearchParams.empty(), null)
                .map(HalParser::toResourceList);
    }
}
