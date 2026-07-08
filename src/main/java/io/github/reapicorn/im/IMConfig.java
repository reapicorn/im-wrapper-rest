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

import javax.net.ssl.SSLContext;

/**
 * Configuration for {@link IMClient}.
 *
 * <p>Use the {@link Builder} to construct an instance:
 * <pre>{@code
 * IMConfig config = IMConfig.builder()
 *     .baseUrl("https://your-im-host:30943/itim/rest")
 *     .username("itim manager")
 *     .password("your-password")
 *     .trustAllSsl(true)
 *     .build();
 * }</pre>
 *
 * <p>If both {@code sslContext} and {@code trustAllSsl} are set, the explicit
 * {@code sslContext} takes precedence.
 */
public final class IMConfig {

    private final String baseUrl;
    private final String username;
    private final String password;
    private final int connectTimeoutSeconds;
    private final int readTimeoutSeconds;
    private final boolean trustAllSsl;
    private final SSLContext sslContext;

    private IMConfig(Builder b) {
        this.baseUrl               = b.baseUrl;
        this.username              = b.username;
        this.password              = b.password;
        this.connectTimeoutSeconds = b.connectTimeoutSeconds;
        this.readTimeoutSeconds    = b.readTimeoutSeconds;
        this.trustAllSsl           = b.trustAllSsl;
        this.sslContext            = b.sslContext;
    }

    public String getBaseUrl()               { return baseUrl; }
    public String getUsername()              { return username; }
    public String getPassword()              { return password; }
    public int getConnectTimeoutSeconds()    { return connectTimeoutSeconds; }
    public int getReadTimeoutSeconds()       { return readTimeoutSeconds; }
    public boolean isTrustAllSsl()           { return trustAllSsl; }
    /** May be {@code null} if not explicitly configured. */
    public SSLContext getSslContext()        { return sslContext; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {

        private String baseUrl;
        private String username;
        private String password;
        private int connectTimeoutSeconds = 30;
        private int readTimeoutSeconds    = 60;
        private boolean trustAllSsl       = false;
        private SSLContext sslContext;

        private Builder() {}

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder connectTimeoutSeconds(int seconds) {
            this.connectTimeoutSeconds = seconds;
            return this;
        }

        public Builder readTimeoutSeconds(int seconds) {
            this.readTimeoutSeconds = seconds;
            return this;
        }

        /** Enable trust-all SSL (for dev/test with self-signed certificates). */
        public Builder trustAllSsl(boolean trustAll) {
            this.trustAllSsl = trustAll;
            return this;
        }

        /**
         * Provide a custom {@link SSLContext} (production use with a known CA cert).
         * If set, this takes precedence over {@code trustAllSsl}.
         */
        public Builder sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public IMConfig build() {
            if (baseUrl == null || baseUrl.isBlank())
                throw new IllegalStateException("baseUrl is required");
            if (username == null || username.isBlank())
                throw new IllegalStateException("username is required");
            if (password == null)
                throw new IllegalStateException("password is required");
            return new IMConfig(this);
        }
    }
}
