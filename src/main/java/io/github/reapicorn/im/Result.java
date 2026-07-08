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

import java.util.function.Function;

/**
 * A discriminated-union type that represents either a successful value ({@link #success})
 * or a failure ({@link #failure}).
 *
 * <p>All public library operations return {@code Result<T>} instead of throwing exceptions.
 * The consumer decides how to handle errors:
 * <pre>{@code
 * Result<List<Person>> result = client.people().searchPeople(params);
 * if (result.isSuccess()) {
 *     result.getValue().forEach(System.out::println);
 * } else {
 *     System.err.println(result.getError());
 * }
 * }</pre>
 *
 * @param <T> type of the success value
 */
public final class Result<T> {

    private final T value;
    private final IMException error;

    private Result(T value, IMException error) {
        this.value = value;
        this.error = error;
    }

    // ------------------------------------------------------------------
    //  Factory methods
    // ------------------------------------------------------------------

    /** Creates a successful result wrapping {@code value}. */
    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    /** Creates a failure result wrapping {@code error}. */
    public static <T> Result<T> failure(IMException error) {
        return new Result<>(null, error);
    }

    // ------------------------------------------------------------------
    //  Accessors
    // ------------------------------------------------------------------

    /** Returns {@code true} if this result represents success. */
    public boolean isSuccess() { return error == null; }

    /**
     * Returns the success value.
     *
     * @throws IllegalStateException if this is a failure result
     */
    public T getValue() {
        if (!isSuccess()) throw new IllegalStateException("Result is a failure: " + error);
        return value;
    }

    /**
     * Returns the error.
     *
     * @throws IllegalStateException if this is a success result
     */
    public IMException getError() {
        if (isSuccess()) throw new IllegalStateException("Result is a success, no error present");
        return error;
    }

    // ------------------------------------------------------------------
    //  Transformation
    // ------------------------------------------------------------------

    /**
     * Applies {@code mapper} to the success value and returns a new {@code Result}.
     * If this is a failure, the failure is propagated without invoking {@code mapper}.
     */
    public <U> Result<U> map(Function<T, U> mapper) {
        if (!isSuccess()) return Result.failure(error);
        try {
            return Result.success(mapper.apply(value));
        } catch (Exception e) {
            String detail = e.getMessage() + (value != null ? "; input=" + value : "");
            return Result.failure(new IMException("map() threw an exception: " + detail, e));
        }
    }

    /**
     * Applies {@code mapper} to the success value, where {@code mapper} itself returns a
     * {@code Result}. If this is a failure, the failure is propagated.
     */
    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        if (!isSuccess()) return Result.failure(error);
        try {
            return mapper.apply(value);
        } catch (Exception e) {
            return Result.failure(new IMException("flatMap() threw an exception: " + e.getMessage(), e));
        }
    }

    @Override
    public String toString() {
        return isSuccess() ? "Result.success(" + value + ")" : "Result.failure(" + error + ")";
    }
}
