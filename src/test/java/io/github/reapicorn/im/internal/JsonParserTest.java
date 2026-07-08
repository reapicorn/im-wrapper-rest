/*
 * Copyright (C) 2025  reapicorn
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package io.github.reapicorn.im.internal;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTest {

    @Test
    void parse_null_returnsNull() {
        assertNull(JsonParser.parse(null));
        assertNull(JsonParser.parse("  "));
    }

    @Test
    void parse_string() {
        assertEquals("hello", JsonParser.parse("\"hello\""));
    }

    @Test
    void parse_stringWithEscapes() {
        assertEquals("a\"b", JsonParser.parse("\"a\\\"b\""));
        assertEquals("a\nb", JsonParser.parse("\"a\\nb\""));
        assertEquals("a/b",  JsonParser.parse("\"a\\/b\""));
    }

    @Test
    void parse_integer() {
        assertEquals(42L, JsonParser.parse("42"));
    }

    @Test
    void parse_negativeInteger() {
        assertEquals(-7L, JsonParser.parse("-7"));
    }

    @Test
    void parse_double() {
        assertEquals(3.14, (Double) JsonParser.parse("3.14"), 0.0001);
    }

    @Test
    void parse_booleanTrue() {
        assertEquals(Boolean.TRUE, JsonParser.parse("true"));
    }

    @Test
    void parse_booleanFalse() {
        assertEquals(Boolean.FALSE, JsonParser.parse("false"));
    }

    @Test
    void parse_null_literal() {
        assertNull(JsonParser.parse("null"));
    }

    @Test
    void parse_emptyObject() {
        Map<String, Object> m = JsonParser.parseObject("{}");
        assertTrue(m.isEmpty());
    }

    @Test
    void parse_simpleObject() {
        Map<String, Object> m = JsonParser.parseObject("{\"a\":\"b\",\"n\":1}");
        assertEquals("b", m.get("a"));
        assertEquals(1L,  m.get("n"));
    }

    @Test
    void parse_emptyArray() {
        List<Object> l = JsonParser.parseArray("[]");
        assertTrue(l.isEmpty());
    }

    @Test
    void parse_simpleArray() {
        List<Object> l = JsonParser.parseArray("[1,2,3]");
        assertEquals(3, l.size());
        assertEquals(1L, l.get(0));
    }

    @Test
    void parse_nestedObject() {
        Map<String, Object> m = JsonParser.parseObject("{\"outer\":{\"inner\":\"val\"}}");
        @SuppressWarnings("unchecked")
        Map<String, Object> inner = (Map<String, Object>) m.get("outer");
        assertEquals("val", inner.get("inner"));
    }

    @Test
    void parseObject_onArray_returnsEmpty() {
        Map<String, Object> m = JsonParser.parseObject("[1,2,3]");
        assertTrue(m.isEmpty());
    }

    @Test
    void parseArray_onObject_wrapsSingle() {
        List<Object> l = JsonParser.parseArray("{\"a\":1}");
        assertEquals(1, l.size());
    }

    @Test
    void toJson_map() {
        Map<String, Object> m = Map.of("k", "v");
        String json = JsonParser.toJson(m);
        assertTrue(json.contains("\"k\""));
        assertTrue(json.contains("\"v\""));
    }

    @Test
    void toJson_list() {
        String json = JsonParser.toJson(List.of("a", "b"));
        assertEquals("[\"a\",\"b\"]", json);
    }

    @Test
    void toJson_emptyMap() {
        assertEquals("{}", JsonParser.toJson((Map<String, Object>) null));
    }

    @Test
    void toJson_emptyList() {
        assertEquals("[]", JsonParser.toJson((List<?>) null));
    }
}
