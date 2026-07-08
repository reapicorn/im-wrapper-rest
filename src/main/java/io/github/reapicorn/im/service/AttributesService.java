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
import io.github.reapicorn.im.internal.HttpExecutor;

import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Attributes</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET /attributes/erroleclassification} – returns the {@code erRoleClassification}
 *       definitions (role classification labels)</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class AttributesService {

    private final HttpExecutor http;

    public AttributesService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  /attributes/erroleclassification
    // ------------------------------------------------------------------

    /**
     * Returns the {@code erRoleClassification} definitions configured in Identity Manager.
     *
     * <p>The response is a flat JSON object whose keys are classification identifiers
     * (e.g. {@code "role.classification.application"}) and values are the human-readable
     * labels.
     *
     * @return {@code Result<String>} containing the raw JSON object
     */
    public Result<String> getRoleClassifications() {
        return http.get("/attributes/erroleclassification", SearchParams.empty(), null);
    }
}
