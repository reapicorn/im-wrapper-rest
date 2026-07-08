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
 * Service wrapper for the IBM Verify Identity Manager <strong>Authorization</strong> REST API
 * ({@code /authz/v1.0/}).
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code POST /authz/v1.0/assignments/{usercode}}                        – edit assignment dates</li>
 *   <li>{@code POST /authz/v1.0/entitlements/{entitlement}}                    – grant/revoke entitlement</li>
 *   <li>{@code PUT  /authz/v1.0/access/assignments/assignment-attributes}      – create assignment attribute values</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class AuthzService {

    private final HttpExecutor http;

    public AuthzService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  /authz/v1.0/assignments/{usercode}
    // ------------------------------------------------------------------

    /**
     * Edits assignment data (start date and expiry date) for the specified user.
     *
     * <p>Required body field: {@code user} — the person ID.
     * Optional body fields: {@code startDate}, {@code expiryDate}.
     *
     * @param usercode the encoded user code (person ID)
     * @param body     the assignment edit payload
     * @return {@code Result<String>} containing the raw JSON response
     */
    public Result<String> editAssignment(String usercode, Map<String, Object> body) {
        return http.post("/authz/v1.0/assignments/" + usercode,
                JsonParser.toJson(body));
    }

    // ------------------------------------------------------------------
    //  /authz/v1.0/entitlements/{entitlement}
    // ------------------------------------------------------------------

    /**
     * Grants or revokes an entitlement for one or multiple users.
     *
     * <p>Required body field: {@code user} — the person ID.
     * Use the {@code operation} field to specify {@code "grant"} or {@code "revoke"}.
     *
     * @param entitlement the encoded entitlement identifier
     * @param body        the grant/revoke payload (see {@code BulkGrantRevokeUserEntitlement} schema)
     * @return {@code Result<String>} containing the raw JSON response array
     */
    public Result<String> grantRevokeEntitlement(String entitlement, Map<String, Object> body) {
        return http.post("/authz/v1.0/entitlements/" + entitlement,
                JsonParser.toJson(body));
    }

    // ------------------------------------------------------------------
    //  /authz/v1.0/access/assignments/assignment-attributes
    // ------------------------------------------------------------------

    /**
     * Creates assignment attribute values for a role-type entitlement for one
     * or multiple users.
     *
     * <p>Required body fields: {@code Entitlement}, {@code User},
     * {@code AssignmentAttributeValue}.
     *
     * @param body the bulk assignment attribute value payload
     *             (see {@code BulkAssignementAttributeValueGrant} schema)
     * @return {@code Result<String>} containing the raw JSON response
     */
    public Result<String> createAssignmentAttributeValues(Map<String, Object> body) {
        return http.put("/authz/v1.0/access/assignments/assignment-attributes",
                JsonParser.toJson(body), null);
    }
}
