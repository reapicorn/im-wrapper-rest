/*
 * Copyright (C) 2025  reapicorn
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package io.github.reapicorn.im.internal;

import io.github.reapicorn.im.model.HalResource;
import io.github.reapicorn.im.model.RequestResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HalParserTest {

    private static final String SIMPLE_HAL = """
            {
              "_links": {
                "self": { "href": "/people/123", "title": "Jane Doe" }
              },
              "_attributes": {
                "cn": "Jane Doe",
                "mail": "jane@example.com"
              }
            }
            """;

    @Test
    void toResource_parsesLinks() {
        HalResource r = HalParser.toResource(SIMPLE_HAL);
        assertNotNull(r.getLinks());
        assertEquals("/people/123", r.getLinks().self().getHref());
        assertEquals("Jane Doe",    r.getLinks().self().getTitle());
    }

    @Test
    void toResource_parsesAttributes() {
        HalResource r = HalParser.toResource(SIMPLE_HAL);
        assertEquals("Jane Doe",         r.getAttribute("cn"));
        assertEquals("jane@example.com", r.getAttribute("mail"));
    }

    @Test
    void toResource_missingAttribute_returnsNull() {
        HalResource r = HalParser.toResource(SIMPLE_HAL);
        assertNull(r.getAttribute("nonexistent"));
    }

    @Test
    void toResource_onArray_returnsFirstElement() {
        String json = "[" + SIMPLE_HAL + "]";
        HalResource r = HalParser.toResource(json);
        assertEquals("/people/123", r.getLinks().self().getHref());
    }

    @Test
    void toResource_emptyJson_returnsEmptyResource() {
        HalResource r = HalParser.toResource("{}");
        assertNotNull(r);
        // no _links key → self() returns null
        assertNull(r.getLinks().self());
    }

    @Test
    void toResourceList_parsesAll() {
        String json = "[" + SIMPLE_HAL + "," + SIMPLE_HAL + "]";
        List<HalResource> list = HalParser.toResourceList(json);
        assertEquals(2, list.size());
    }

    @Test
    void toResourceList_emptyArray_returnsEmptyList() {
        List<HalResource> list = HalParser.toResourceList("[]");
        assertTrue(list.isEmpty());
    }

    @Test
    void toRequestResponse_parsesFields() {
        String json = "{\"requestId\":\"req-001\",\"status\":202,\"changeComplete\":false}";
        RequestResponse rr = HalParser.toRequestResponse(json);
        assertEquals("req-001", rr.getRequestId());
        assertEquals(202,       rr.getStatus());
        assertFalse(rr.isChangeComplete());
    }

    @Test
    void toRequestResponse_changeComplete_true() {
        String json = "{\"requestId\":\"req-002\",\"status\":200,\"changeComplete\":true}";
        RequestResponse rr = HalParser.toRequestResponse(json);
        assertTrue(rr.isChangeComplete());
    }

    @Test
    void toRequestResponse_emptyJson_returnsDefaults() {
        RequestResponse rr = HalParser.toRequestResponse("{}");
        assertNull(rr.getRequestId());
        assertEquals(0, rr.getStatus());
    }
}
