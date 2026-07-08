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

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>API Security</strong> REST API
 * ({@code /apisecurity/v1.0/}).
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET /apisecurity/v1.0/runtime/entitlements} – list runtime entitlements</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class ApiSecurityService {

    private final HttpExecutor http;

    public ApiSecurityService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  /apisecurity/v1.0/runtime/entitlements
    // ------------------------------------------------------------------

    /**
     * Returns the list of runtime entitlements configured in the API security layer.
     *
     * @return {@code Result<HalResource>} containing the raw response
     */
    public Result<HalResource> getRuntimeEntitlements() {
        return http.get("/apisecurity/v1.0/runtime/entitlements", SearchParams.empty(), null)
                .map(raw -> raw == null || raw.isBlank()
                        ? new HalResource(null, null, null)
                        : HalParser.toResource(raw));
    }
}
