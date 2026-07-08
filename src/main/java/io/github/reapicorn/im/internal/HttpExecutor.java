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

import io.github.reapicorn.im.IMConfig;
import io.github.reapicorn.im.IMException;
import io.github.reapicorn.im.PageRange;
import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.SearchParams;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import javax.net.ssl.SSLContext;

/**
 * Internal HTTP executor for the IBM Verify Identity Manager REST API.
 *
 * <p>Centralises: Basic authentication, CSRF token management, session cookies,
 * Range header injection, and conversion of non-2xx responses to
 * {@link Result#failure(IMException)}.
 *
 * <p>This class is intentionally package-accessible only — consumers interact
 * exclusively through {@link io.github.reapicorn.im.IMClient} and its services.
 */
public final class HttpExecutor {

    private static final String ACCEPT       = "application/vnd.ibm.isim-v1+json, application/json";
    private static final String CONTENT_TYPE = "application/json";
    private static final String CSRF_HEADER  = "CSRFToken";

    private final String        baseUrl;
    private final String        credentials;
    private final String        username;
    private final String        password;
    private final int           readTimeoutSeconds;
    private final CookieManager cookieManager;
    private final HttpClient    httpClient;
    private final HttpClient    noRedirectClient;

    private volatile String csrfToken;

    // ------------------------------------------------------------------
    //  Construction
    // ------------------------------------------------------------------

    /**
     * Constructs an {@code HttpExecutor} from an {@link IMConfig}.
     *
     * <p>Two {@link HttpClient} instances are created, sharing the same
     * {@link CookieManager} so the LTPA session cookie obtained during login
     * is available to all subsequent API calls:
     * <ul>
     *   <li>{@code httpClient}      – follows redirects; used for all API requests.</li>
     *   <li>{@code noRedirectClient} – never follows redirects; used only for the
     *       {@code j_security_check} POST so we capture the cookie from the 302
     *       response without chasing the redirect target.</li>
     * </ul>
     */
    public HttpExecutor(IMConfig config) {
        String url = config.getBaseUrl();
        this.baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.username = config.getUsername();
        this.password = config.getPassword();
        this.credentials = Base64.getEncoder()
                .encodeToString((this.username + ":" + this.password)
                        .getBytes(StandardCharsets.UTF_8));
        this.readTimeoutSeconds = config.getReadTimeoutSeconds();

        SSLContext ssl = config.getSslContext() != null
                ? config.getSslContext()
                : (config.isTrustAllSsl() ? TrustAllSslContext.create() : null);

        this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

        HttpClient.Builder base = HttpClient.newBuilder()
                .cookieHandler(this.cookieManager)
                .connectTimeout(Duration.ofSeconds(config.getConnectTimeoutSeconds()));
        if (ssl != null) base.sslContext(ssl);

        this.httpClient       = base.followRedirects(HttpClient.Redirect.NORMAL).build();
        this.noRedirectClient = base.followRedirects(HttpClient.Redirect.NEVER).build();
    }

    // ------------------------------------------------------------------
    //  Session bootstrap
    // ------------------------------------------------------------------

    /**
     * Establishes a session with the IM server.
     *
     * <ol>
     *   <li>POSTs credentials to {@code j_security_check} (form-based auth)
     *       to obtain the LTPA session cookie.</li>
     *   <li>GETs {@code /systemusers/me} to capture the {@code CSRFToken}
     *       response header required for all write operations.</li>
     * </ol>
     *
     * <p>Must be called once before any operation.  Safe to call multiple
     * times — each call refreshes the session and CSRF token.
     *
     * @return {@code Result.success(responseBody)} or {@code Result.failure}
     */
    public Result<String> initSession() {
        try {
            // Step 1 – form login to obtain the LTPA session cookie.
            // j_security_check lives one level above the REST base path,
            // e.g. https://host/itim/j_security_check
            String loginUrl = deriveLoginUrl(baseUrl);
            String formBody = "j_username=" + urlEncode(username)
                    + "&j_password=" + urlEncode(password);

            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(loginUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(readTimeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(formBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> loginResponse =
                    noRedirectClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());

            // j_security_check always redirects (302) on success; it returns 200 with the
            // login HTML on bad credentials.  We stop following the redirect so we only
            // need the session cookie, not the redirect destination.
            int loginStatus = loginResponse.statusCode();
            if (loginStatus >= 400) {
                return Result.failure(new IMException(
                        "initSession login failed: HTTP " + loginStatus
                        + " (POST " + loginUrl + ")",
                        loginStatus, loginResponse.body()));
            }

            // Step 2 – fetch /systemusers/me to capture the CSRFToken header.
            HttpRequest meRequest = baseRequest("/systemusers/me").GET().build();
            HttpResponse<String> meResponse =
                    httpClient.send(meRequest, HttpResponse.BodyHandlers.ofString());

            Result<String> result = toResult(meResponse);
            if (result.isSuccess()) {
                csrfToken = meResponse.headers().firstValue(CSRF_HEADER).orElse(null);
                if (csrfToken == null) {
                    csrfToken = meResponse.headers().firstValue("csrftoken").orElse(null);
                }
            }
            return result;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Result.failure(new IMException(
                    "initSession failed: " + e.getMessage(), e));
        }
    }

    /** Returns the CSRF token captured during {@link #initSession()}, or {@code null}. */
    public String getCsrfToken() { return csrfToken; }

    // ------------------------------------------------------------------
    //  HTTP methods
    // ------------------------------------------------------------------

    /**
     * Performs {@code GET path?queryString} with an optional {@code Range} header.
     *
     * @param path   relative path (e.g. {@code "/people"})
     * @param params query parameters — use {@link SearchParams#empty()} for none
     * @param range  pagination range — may be {@code null} for no Range header
     * @return {@code Result<String>} containing the raw JSON body
     */
    public Result<String> get(String path, SearchParams params, PageRange range) {
        try {
            String qs = (params != null) ? params.toQueryString() : "";
            String fullPath = qs.isBlank() ? path : path + "?" + qs;
            HttpRequest.Builder req = baseRequest(fullPath).GET();
            if (range != null) {
                req.header("Range", range.toHeaderValue());
            }
            HttpResponse<String> response =
                    httpClient.send(req.build(), HttpResponse.BodyHandlers.ofString());
            return toResult(response);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Result.failure(new IMException(
                    "GET " + path + " failed: " + e.getMessage(), e));
        }
    }

    /**
     * Performs {@code POST path} with a JSON body.
     *
     * @param path     relative path
     * @param jsonBody request body as a JSON string
     * @return {@code Result<String>} containing the raw JSON body
     */
    public Result<String> post(String path, String jsonBody) {
        try {
            HttpRequest request = baseRequest(path)
                    .header(CSRF_HEADER, requireCsrf())
                    .header("Content-Type", CONTENT_TYPE)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return toResult(response);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Result.failure(new IMException(
                    "POST " + path + " failed: " + e.getMessage(), e));
        }
    }

    /**
     * Performs {@code PUT path} with a JSON body and an optional
     * {@code X-HTTP-Method-Override} header (e.g. {@code "suspend"}, {@code "restore"}).
     *
     * @param path           relative path
     * @param jsonBody       request body as a JSON string
     * @param methodOverride value for {@code X-HTTP-Method-Override}, or {@code null}
     * @return {@code Result<String>} containing the raw JSON body
     */
    public Result<String> put(String path, String jsonBody, String methodOverride) {
        try {
            HttpRequest.Builder builder = baseRequest(path)
                    .header(CSRF_HEADER, requireCsrf())
                    .header("Content-Type", CONTENT_TYPE)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
            if (methodOverride != null && !methodOverride.isBlank()) {
                builder.header("X-HTTP-Method-Override", methodOverride);
            }
            HttpResponse<String> response =
                    httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return toResult(response);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Result.failure(new IMException(
                    "PUT " + path + " failed: " + e.getMessage(), e));
        }
    }

    /**
     * Performs {@code PATCH path} with a JSON body.
     *
     * @param path     relative path or absolute URL
     * @param jsonBody request body as a JSON string
     * @return {@code Result<String>} containing the raw JSON body
     */
    public Result<String> patch(String path, String jsonBody) {
        try {
            HttpRequest request = baseRequest(path)
                    .header(CSRF_HEADER, requireCsrf())
                    .header("Content-Type", CONTENT_TYPE)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return toResult(response);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Result.failure(new IMException(
                    "PATCH " + path + " failed: " + e.getMessage(), e));
        }
    }

    /**
     * Performs {@code DELETE path}.
     *
     * @param path relative path
     * @return {@code Result<String>} containing the raw JSON body (often empty)
     */
    public Result<String> delete(String path) {
        try {
            HttpRequest request = baseRequest(path)
                    .header(CSRF_HEADER, requireCsrf())
                    .DELETE()
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return toResult(response);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Result.failure(new IMException(
                    "DELETE " + path + " failed: " + e.getMessage(), e));
        }
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    /**
     * Converts an {@link HttpResponse} to {@code Result<String>}.
     *
     * <p>A 2xx status → {@code Result.success(body)}.
     * Any other status → {@code Result.failure(IMException)}.
     */
    private Result<String> toResult(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return Result.success(response.body());
        }
        return Result.failure(new IMException(
                "HTTP " + status, status, response.body()));
    }

    private HttpRequest.Builder baseRequest(String path) {
        String url = path.startsWith("http") ? path : baseUrl + path;
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + credentials)
                .header("Accept", ACCEPT)
                .timeout(Duration.ofSeconds(readTimeoutSeconds));
    }

    /**
     * Returns the v1.2 base URL derived from the configured REST base URL.
     *
     * <p>e.g. {@code https://host/itim/rest} → {@code https://host/itim/rest/v1.2}
     */
    public String getBaseUrlV12() {
        return baseUrl + "/v1.2";
    }

    private String requireCsrf() {
        if (csrfToken == null) {
            throw new IllegalStateException(
                    "No CSRF token available. Call initSession() before write operations.");
        }
        return csrfToken;
    }

    /**
     * Derives the {@code j_security_check} URL from the configured REST base URL.
     *
     * <p>The REST base URL is typically {@code https://host:port/itim/rest}.
     * WebSphere form login is at {@code https://host:port/itim/j_security_check}.
     * We strip everything from {@code /rest} onward and append the login path.
     *
     * <p>If the URL doesn't contain {@code /rest}, we fall back to appending
     * {@code /j_security_check} directly (handles non-standard deployments).
     */
    static String deriveLoginUrl(String restBaseUrl) {
        // Find the last occurrence of "/rest" as a path segment
        int idx = restBaseUrl.lastIndexOf("/rest");
        if (idx >= 0) {
            return restBaseUrl.substring(0, idx) + "/j_security_check";
        }
        // Fallback: just append alongside whatever path is there
        return restBaseUrl + "/j_security_check";
    }

    /** Percent-encodes a string for use in an {@code application/x-www-form-urlencoded} body. */
    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
