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

class ResultTest {

    @Test
    void success_isSuccess() {
        Result<String> r = Result.success("hello");
        assertTrue(r.isSuccess());
    }

    @Test
    void success_getValue() {
        Result<String> r = Result.success("hello");
        assertEquals("hello", r.getValue());
    }

    @Test
    void success_getError_throws() {
        Result<String> r = Result.success("hello");
        assertThrows(IllegalStateException.class, r::getError);
    }

    @Test
    void failure_isNotSuccess() throws Exception {
        Result<String> r = Result.failure(new IMException("err", 400, null));
        assertFalse(r.isSuccess());
    }

    @Test
    void failure_getError() throws Exception {
        IMException ex = new IMException("err", 400, "body");
        Result<String> r = Result.failure(ex);
        assertSame(ex, r.getError());
    }

    @Test
    void failure_getValue_throws() throws Exception {
        Result<String> r = Result.failure(new IMException("err", 400, null));
        assertThrows(IllegalStateException.class, r::getValue);
    }

    @Test
    void map_onSuccess_transformsValue() {
        Result<Integer> r = Result.success(3).map(v -> v * 2);
        assertTrue(r.isSuccess());
        assertEquals(6, r.getValue());
    }

    @Test
    void map_onFailure_propagatesError() throws Exception {
        IMException ex = new IMException("err", 500, null);
        Result<Integer> r = Result.<String>failure(ex).map(String::length);
        assertFalse(r.isSuccess());
        assertSame(ex, r.getError());
    }

    @Test
    void map_mapperThrows_returnsFailure() {
        Result<String> r = Result.success("x").map(v -> { throw new RuntimeException("boom"); });
        assertFalse(r.isSuccess());
        assertTrue(r.getError().getMessage().contains("map() threw"));
    }

    @Test
    void flatMap_onSuccess_chainsResult() {
        Result<String> r = Result.success(42)
                .flatMap(v -> Result.success("value=" + v));
        assertTrue(r.isSuccess());
        assertEquals("value=42", r.getValue());
    }

    @Test
    void flatMap_onFailure_propagatesError() throws Exception {
        IMException ex = new IMException("err", 500, null);
        Result<String> r = Result.<Integer>failure(ex)
                .flatMap(v -> Result.success("x"));
        assertFalse(r.isSuccess());
        assertSame(ex, r.getError());
    }

    @Test
    void toString_success() {
        assertEquals("Result.success(hi)", Result.success("hi").toString());
    }
}
