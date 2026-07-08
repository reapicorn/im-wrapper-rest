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
package io.github.reapicorn.im.model;

/**
 * Represents an asynchronous request response from IM (HTTP 202 Accepted).
 *
 * <p>After an operation that returns 202, poll {@code /requests/{requestId}}
 * until {@link #isChangeComplete()} is {@code true}.
 */
public final class RequestResponse {

    private final String requestId;
    private final int status;
    private final boolean changeComplete;

    public RequestResponse(String requestId, int status, boolean changeComplete) {
        this.requestId      = requestId;
        this.status         = status;
        this.changeComplete = changeComplete;
    }

    /** The IM request identifier — use this to poll for completion. */
    public String getRequestId() { return requestId; }

    /** Current status code of the request. */
    public int getStatus() { return status; }

    /** {@code true} when the operation has finished processing. */
    public boolean isChangeComplete() { return changeComplete; }

    @Override
    public String toString() {
        return "RequestResponse{requestId='" + requestId
                + "', status=" + status
                + ", changeComplete=" + changeComplete + "}";
    }
}
