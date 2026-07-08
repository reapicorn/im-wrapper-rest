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

class PageRangeTest {

    @Test
    void of_validRange_createsInstance() {
        PageRange r = PageRange.of(0, 49);
        assertEquals(0, r.getFrom());
        assertEquals(49, r.getTo());
    }

    @Test
    void size_returnsCorrectCount() {
        assertEquals(50, PageRange.of(0, 49).size());
        assertEquals(1,  PageRange.of(5, 5).size());
        assertEquals(10, PageRange.of(10, 19).size());
    }

    @Test
    void toHeaderValue_format() {
        assertEquals("items=0-49",   PageRange.of(0, 49).toHeaderValue());
        assertEquals("items=50-99",  PageRange.of(50, 99).toHeaderValue());
    }

    @Test
    void toString_includesHeaderValue() {
        assertTrue(PageRange.of(0, 9).toString().contains("items=0-9"));
    }

    @Test
    void of_negativeFrom_throws() {
        assertThrows(IllegalArgumentException.class, () -> PageRange.of(-1, 10));
    }

    @Test
    void of_toSmallerThanFrom_throws() {
        assertThrows(IllegalArgumentException.class, () -> PageRange.of(10, 5));
    }

    @Test
    void of_equalFromTo_valid() {
        PageRange r = PageRange.of(3, 3);
        assertEquals(1, r.size());
    }
}
