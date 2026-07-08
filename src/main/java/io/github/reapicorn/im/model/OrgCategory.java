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
package io.github.reapicorn.im.model;

/**
 * Valid values for the {@code {category}} path parameter used in
 * {@code /organizationcontainers/{category}} and
 * {@code /organizationcontainers/{category}/{orgContainerId}} endpoints.
 *
 * <p>The string representation (via {@link #value()}) matches the exact token
 * expected by the IM REST API (case-insensitive on the server side, but
 * lower-case is canonical).
 */
public enum OrgCategory {

    /** Root-level organisations. */
    ORGANIZATIONS("organizations"),

    /** Organisational units. */
    ORGANIZATIONAL_UNITS("organizationunits"),

    /** Locations. */
    LOCATIONS("locations"),

    /** Business-partner organisations. */
    BP_ORGANIZATIONS("bporganizations"),

    /** Administrative domains. */
    ADMIN_DOMAINS("admindomains");

    private final String value;

    OrgCategory(String value) {
        this.value = value;
    }

    /**
     * Returns the lower-case token used in the REST API path.
     *
     * @return path segment value, e.g. {@code "organizationunits"}
     */
    public String value() {
        return value;
    }
}
