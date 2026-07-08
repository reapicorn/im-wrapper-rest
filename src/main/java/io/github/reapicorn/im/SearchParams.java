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
package io.github.reapicorn.im;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for common IM REST query parameters.
 *
 * <p>Usage:
 * <pre>{@code
 * String qs = SearchParams.builder()
 *     .attributes("cn", "sn", "uid", "erpersonstatus")
 *     .limit(50)
 *     .sort("+cn")
 *     .toQueryString();
 * // → "attributes=cn,sn,uid,erpersonstatus&limit=50&sort=%2Bcn"
 * }</pre>
 */
public final class SearchParams {

    private final String attributes;
    private final String embedded;
    private final Integer limit;
    private final String sort;

    private SearchParams(Builder b) {
        this.attributes = b.attributes;
        this.embedded   = b.embedded;
        this.limit      = b.limit;
        this.sort       = b.sort;
    }

    /** Returns the URL query string (without leading {@code ?}), or empty string if no params. */
    public String toQueryString() {
        List<String> parts = new ArrayList<>();
        if (attributes != null && !attributes.isBlank()) parts.add("attributes=" + encode(attributes));
        if (embedded   != null && !embedded.isBlank())   parts.add("embedded="   + encode(embedded));
        if (limit      != null)                          parts.add("limit="      + limit);
        if (sort       != null && !sort.isBlank())        parts.add("sort="       + encode(sort));
        return String.join("&", parts);
    }

    public String getAttributes() { return attributes; }
    public String getEmbedded()   { return embedded; }
    public Integer getLimit()     { return limit; }
    public String getSort()       { return sort; }

    /** Creates a new {@link Builder}. */
    public static Builder builder() { return new Builder(); }

    /** Creates an empty {@code SearchParams} (produces an empty query string). */
    public static SearchParams empty() { return builder().build(); }

    // ── URL-encoding helper (minimal — only encodes + sign for sort) ──────

    private static String encode(String value) {
        // Encode only the characters that must be encoded in query values.
        // The + sign is used by IM sort expressions and must be %2B.
        return value.replace("+", "%2B").replace(" ", "%20");
    }

    // ── Builder ───────────────────────────────────────────────────────────

    public static final class Builder {

        private String  attributes;
        private String  embedded;
        private Integer limit;
        private String  sort;

        private Builder() {}

        /**
         * Comma-separated list of attribute names to return, or {@code "*"} for all.
         * Calling this method multiple times replaces the previous value.
         */
        public Builder attributes(String... names) {
            this.attributes = String.join(",", names);
            return this;
        }

        /** Raw comma-separated attribute string (e.g. from a config value). */
        public Builder attributesRaw(String csv) {
            this.attributes = csv;
            return this;
        }

        /**
         * Comma-separated list of embedded reference attributes.
         * Calling this method multiple times replaces the previous value.
         */
        public Builder embedded(String... names) {
            this.embedded = String.join(",", names);
            return this;
        }

        /** Maximum number of results to return. */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        /** Sort expression, e.g. {@code "+cn"} or {@code "-eruid"}. */
        public Builder sort(String sort) {
            this.sort = sort;
            return this;
        }

        public SearchParams build() { return new SearchParams(this); }

        /** Shortcut: calls {@link #build()} and then {@link SearchParams#toQueryString()}. */
        public String toQueryString() { return build().toQueryString(); }
    }
}
