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
 * Internal exception representing an IM API error (non-2xx HTTP response or
 * a client-side failure such as a network error).
 *
 * <p>This exception is never thrown directly to the library consumer — it is
 * always wrapped inside a {@link Result#failure(IMException)} value.
 */
public final class IMException extends Exception {

    private final int statusCode;
    private final String responseBody;

    /**
     * Creates an {@code IMException} from an HTTP error response.
     *
     * @param message      human-readable description
     * @param statusCode   HTTP status code (0 if not HTTP-related)
     * @param responseBody raw response body, or {@code null}
     */
    public IMException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode   = statusCode;
        this.responseBody = responseBody;
    }

    /** Wraps a low-level (non-HTTP) exception. */
    public IMException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode   = 0;
        this.responseBody = null;
    }

    /** HTTP status code, or {@code 0} for non-HTTP failures. */
    public int getStatusCode()    { return statusCode; }

    /** Raw response body, or {@code null} if not available. */
    public String getResponseBody() { return responseBody; }

    @Override
    public String toString() {
        return "IMException{statusCode=" + statusCode
                + ", message='" + getMessage() + "'"
                + (responseBody != null ? ", body='" + responseBody + "'" : "")
                + "}";
    }
}
