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
 * Represents the {@code _links} section of a HAL resource.
 *
 * <p>Each entry maps a relation name (e.g. {@code "self"}) to a {@link HalLink}.
 */
public final class HalLinks {

    private final Map<String, HalLink> links;

    public HalLinks(Map<String, HalLink> links) {
        this.links = links != null ? Collections.unmodifiableMap(new LinkedHashMap<>(links))
                                   : Collections.emptyMap();
    }

    /** Returns the link for the given relation, or {@code null} if absent. */
    public HalLink get(String rel) { return links.get(rel); }

    /** Returns the {@code self} link, or {@code null} if absent. */
    public HalLink self() { return links.get("self"); }

    /** Returns an unmodifiable view of all links. */
    public Map<String, HalLink> all() { return links; }

    @Override
    public String toString() { return "HalLinks" + links; }
}
