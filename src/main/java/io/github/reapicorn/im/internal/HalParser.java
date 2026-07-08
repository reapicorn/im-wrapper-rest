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
package io.github.reapicorn.im.internal;

import io.github.reapicorn.im.model.HalLink;
import io.github.reapicorn.im.model.HalLinks;
import io.github.reapicorn.im.model.HalResource;
import io.github.reapicorn.im.model.RequestResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralised HAL (Hypertext Application Language) parsing utilities.
 *
 * <p>All IM REST responses follow the HAL pattern:
 * <pre>{@code
 * {
 *   "_links":      { "self": { "href": "...", "title": "..." } },
 *   "_attributes": { ... },
 *   "_embedded":   { ... }
 * }
 * }</pre>
 *
 * <p>This class is internal — services use it to avoid duplicating parse logic.
 */
public final class HalParser {

    private HalParser() {}

    // ------------------------------------------------------------------
    //  Public parsing methods
    // ------------------------------------------------------------------

    /**
     * Parses a single HAL object from a JSON string.
     *
     * <p>If the JSON is an array, the first element is returned.
     *
     * @param json raw JSON string from the API
     * @return parsed {@link HalResource}, or an empty resource if parsing fails
     */
    @SuppressWarnings("unchecked")
    public static HalResource toResource(String json) {
        Object parsed = JsonParser.parse(json);
        if (parsed instanceof Map) {
            return mapToResource((Map<String, Object>) parsed);
        }
        if (parsed instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map) {
            return mapToResource((Map<String, Object>) list.get(0));
        }
        return new HalResource(null, null, null);
    }

    /**
     * Parses a JSON array of HAL objects.
     *
     * <p>If the JSON is a single object rather than an array, it is wrapped in a list.
     *
     * @param json raw JSON string from the API
     * @return list of parsed {@link HalResource} objects (never {@code null})
     */
    @SuppressWarnings("unchecked")
    public static List<HalResource> toResourceList(String json) {
        List<Object> items = JsonParser.parseArray(json);
        List<HalResource> result = new ArrayList<>(items.size());
        for (Object item : items) {
            if (item instanceof Map) {
                result.add(mapToResource((Map<String, Object>) item));
            }
        }
        return result;
    }

    /**
     * Parses an async {@link RequestResponse} (HTTP 202 body) from a JSON string.
     *
     * @param json raw JSON string from the API
     * @return parsed {@link RequestResponse}
     */
    @SuppressWarnings("unchecked")
    public static RequestResponse toRequestResponse(String json) {
        Object parsed = JsonParser.parse(json);
        String  requestId      = null;
        int     status         = 0;
        boolean changeComplete = false;
        if (parsed instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) parsed;
            Object rid = map.get("requestId");
            if (rid != null) requestId = rid.toString();
            Object st = map.get("status");
            if (st instanceof Number) status = ((Number) st).intValue();
            Object cc = map.get("changeComplete");
            if (cc instanceof Boolean) changeComplete = (Boolean) cc;
        }
        return new RequestResponse(requestId, status, changeComplete);
    }

    // ------------------------------------------------------------------
    //  Private helpers
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static HalResource mapToResource(Map<String, Object> map) {
        // Parse _links
        HalLinks halLinks = null;
        Object linksObj = map.get("_links");
        if (linksObj instanceof Map) {
            Map<String, HalLink> linkMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) linksObj).entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> linkData = (Map<String, Object>) entry.getValue();
                    String href  = linkData.get("href")  instanceof String s ? s : null;
                    String title = linkData.get("title") instanceof String t ? t : null;
                    linkMap.put(entry.getKey(), new HalLink(href, title));
                }
            }
            halLinks = new HalLinks(linkMap);
        }

        // Parse _attributes
        Map<String, Object> attributes = null;
        Object attrsObj = map.get("_attributes");
        if (attrsObj instanceof Map) {
            attributes = (Map<String, Object>) attrsObj;
        }

        // Parse _embedded (shallow — each value is treated as a nested HAL object)
        Map<String, HalResource> embedded = null;
        Object embObj = map.get("_embedded");
        if (embObj instanceof Map) {
            embedded = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) embObj).entrySet()) {
                if (entry.getValue() instanceof Map) {
                    embedded.put(entry.getKey(), mapToResource((Map<String, Object>) entry.getValue()));
                }
            }
        }

        return new HalResource(halLinks, attributes, embedded);
    }
}
