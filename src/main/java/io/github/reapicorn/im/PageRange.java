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

/**
 * Represents an HTTP {@code Range: items=X-Y} header value for paginated IM requests.
 *
 * <p>Usage:
 * <pre>{@code
 * PageRange range = PageRange.of(0, 49);  // first 50 items
 * // header value: "items=0-49"
 * }</pre>
 */
public final class PageRange {

    private final int from;
    private final int to;

    private PageRange(int from, int to) {
        if (from < 0)   throw new IllegalArgumentException("from must be >= 0");
        if (to < from)  throw new IllegalArgumentException("to must be >= from");
        this.from = from;
        this.to   = to;
    }

    /**
     * Creates a {@code PageRange} for the given inclusive zero-based indices.
     *
     * @param from start index (inclusive, zero-based)
     * @param to   end index (inclusive, zero-based)
     */
    public static PageRange of(int from, int to) { return new PageRange(from, to); }

    /** Start index (inclusive, zero-based). */
    public int getFrom() { return from; }

    /** End index (inclusive, zero-based). */
    public int getTo()   { return to; }

    /** Returns the number of items in this range ({@code to - from + 1}). */
    public int size()    { return to - from + 1; }

    /**
     * Returns the value to use for the {@code Range} request header.
     * Example: {@code "items=0-49"}.
     */
    public String toHeaderValue() { return "items=" + from + "-" + to; }

    @Override
    public String toString() { return "PageRange{" + toHeaderValue() + "}"; }
}
