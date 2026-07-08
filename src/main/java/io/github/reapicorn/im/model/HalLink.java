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
 * Lesser General Public License /for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package io.github.reapicorn.im.model;

/**
 * Represents a single HAL link (href + optional title).
 */
public final class HalLink {

    private final String href;
    private final String title;

    public HalLink(String href, String title) {
        this.href  = href;
        this.title = title;
    }

    /** The link URL (may be relative or absolute). */
    public String getHref()  { return href; }

    /** Human-readable title, or {@code null} if not present. */
    public String getTitle() { return title; }

    /**
     * Extracts the last path segment of {@link #getHref()}, which is used
     * as the resource identifier in IM responses.
     */
    public String getId() {
        if (href == null) return null;
        String[] parts = href.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : href;
    }

    @Override
    public String toString() {
        return "HalLink{href='" + href + "', title='" + title + "'}";
    }
}
