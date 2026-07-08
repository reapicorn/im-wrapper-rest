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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base representation of a HAL resource as returned by the IM REST API.
 *
 * <p>Every IM response object has:
 * <ul>
 *   <li>{@code _links}      — navigation links (including {@code self})</li>
 *   <li>{@code _attributes} — domain attributes (key → value)</li>
 *   <li>{@code _embedded}   — nested HAL resources (optional)</li>
 * </ul>
 *
 * <p>Domain-specific model classes (Person, Account, Role …) extend or wrap
 * this class to provide typed accessors for their own attributes.
 */
public class HalResource {

    private final HalLinks links;
    private final Map<String, Object> attributes;
    private final Map<String, HalResource> embedded;

    public HalResource(HalLinks links,
                       Map<String, Object> attributes,
                       Map<String, HalResource> embedded) {
        this.links      = links      != null ? links      : new HalLinks(null);
        this.attributes = attributes != null ? Collections.unmodifiableMap(new LinkedHashMap<>(attributes))
                                             : Collections.emptyMap();
        this.embedded   = embedded   != null ? Collections.unmodifiableMap(new LinkedHashMap<>(embedded))
                                             : Collections.emptyMap();
    }

    /** Returns the {@code _links} section. Never {@code null}. */
    public HalLinks getLinks() { return links; }

    /**
     * Returns the {@code _attributes} map.
     * Values may be {@code String}, {@code Number}, {@code Boolean}, {@code List}, or nested {@code Map}.
     */
    public Map<String, Object> getAttributes() { return attributes; }

    /** Returns the {@code _embedded} map. Never {@code null}, may be empty. */
    public Map<String, HalResource> getEmbedded() { return embedded; }

    // ── Convenience accessors ─────────────────────────────────────────────

    /** Returns the resource's self href, extracted from {@code _links.self}. */
    public String getHref() {
        HalLink self = links.self();
        return self != null ? self.getHref() : null;
    }

    /** Returns the last path segment of the self href, used as the resource ID. */
    public String getId() {
        HalLink self = links.self();
        return self != null ? self.getId() : null;
    }

    /** Returns the title of the self link. */
    public String getTitle() {
        HalLink self = links.self();
        return self != null ? self.getTitle() : null;
    }

    /** Returns a single attribute value as a String, or {@code null} if absent. */
    public String getAttribute(String key) {
        Object v = attributes.get(key);
        return v == null ? null : v.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{id='" + getId() + "', title='" + getTitle() + "'}";
    }
}
