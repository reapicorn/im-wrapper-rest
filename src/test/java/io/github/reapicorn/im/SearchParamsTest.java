/*
 * Copyright (C) 2025  reapicorn
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package io.github.reapicorn.im;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchParamsTest {

    @Test
    void empty_producesEmptyQueryString() {
        assertEquals("", SearchParams.empty().toQueryString());
    }

    @Test
    void attributes_varargs_joinedWithComma() {
        String qs = SearchParams.builder().attributes("cn", "sn", "mail").toQueryString();
        assertEquals("attributes=cn,sn,mail", qs);
    }

    @Test
    void attributesRaw_passedThrough() {
        String qs = SearchParams.builder().attributesRaw("cn,sn").toQueryString();
        assertEquals("attributes=cn,sn", qs);
    }

    @Test
    void limit_appearsInQueryString() {
        String qs = SearchParams.builder().limit(50).toQueryString();
        assertEquals("limit=50", qs);
    }

    @Test
    void sort_ascending_encodesPlus() {
        String qs = SearchParams.builder().sort("+cn").toQueryString();
        assertEquals("sort=%2Bcn", qs);
    }

    @Test
    void sort_descending_noEncoding() {
        String qs = SearchParams.builder().sort("-cn").toQueryString();
        assertEquals("sort=-cn", qs);
    }

    @Test
    void embedded_appearsInQueryString() {
        String qs = SearchParams.builder().embedded("accounts", "roles").toQueryString();
        assertEquals("embedded=accounts,roles", qs);
    }

    @Test
    void allParams_correctOrder() {
        String qs = SearchParams.builder()
                .attributes("cn", "sn")
                .embedded("accounts")
                .limit(25)
                .sort("+cn")
                .toQueryString();
        assertEquals("attributes=cn,sn&embedded=accounts&limit=25&sort=%2Bcn", qs);
    }

    @Test
    void getters_returnCorrectValues() {
        SearchParams p = SearchParams.builder()
                .attributes("cn")
                .embedded("roles")
                .limit(10)
                .sort("-sn")
                .build();
        assertEquals("cn",    p.getAttributes());
        assertEquals("roles", p.getEmbedded());
        assertEquals(10,      p.getLimit());
        assertEquals("-sn",   p.getSort());
    }

    @Test
    void blankAttributes_notIncluded() {
        String qs = SearchParams.builder().attributesRaw("  ").toQueryString();
        assertEquals("", qs);
    }
}
