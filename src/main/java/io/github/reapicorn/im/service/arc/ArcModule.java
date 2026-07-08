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
package io.github.reapicorn.im.service.arc;

import io.github.reapicorn.im.IMConfig;
import io.github.reapicorn.im.internal.HttpExecutor;

import java.net.URI;

/**
 * Module providing access to the IBM Verify Identity Manager <strong>ARC (Analytics and Risk Console)</strong>
 * subsystem ({@code /itim/arc/v1.0/}).
 *
 * <p>This subsystem uses a different URL prefix from the main IM REST API
 * ({@code /itim/rest/}). The module computes the origin ({@code scheme://host:port})
 * from the configured {@code baseUrl} and passes it to each service so they can
 * construct correct absolute URLs.
 *
 * <p>Obtain an instance via {@link io.github.reapicorn.im.IMClient#arc()}.
 *
 * <p>Service instances are created lazily and cached.
 *
 * <pre>{@code
 * ArcModule arc = client.arc();
 *
 * // Search for risks
 * Result<String> risks = arc.risks().searchRisks(SearchParams.empty(), null);
 *
 * // Get ARC statistics
 * Result<String> stats = arc.stats().getStats(
 *     SearchParams.builder().param("resourceType", "All").build());
 * }</pre>
 */
public final class ArcModule {

    private final HttpExecutor http;
    private final String       arcBase;

    // Lazy sub-service instances
    private ArcActivitiesService  activitiesService;
    private ArcRisksService       risksService;
    private ArcMitigationsService mitigationsService;
    private ArcUsersService       usersService;
    private ArcStatsService       statsService;

    /**
     * Creates a new module.
     *
     * @param http   the shared {@link HttpExecutor} (authentication, cookies, CSRF)
     * @param config the {@link IMConfig} used to derive the ARC origin URL
     */
    public ArcModule(HttpExecutor http, IMConfig config) {
        this.http    = http;
        this.arcBase = extractOrigin(config.getBaseUrl());
    }

    // ------------------------------------------------------------------
    //  Sub-service accessors (lazy, cached)
    // ------------------------------------------------------------------

    /**
     * Returns the {@link ArcActivitiesService} for business activity and activity
     * folder operations. The instance is created on first call and reused thereafter.
     */
    public ArcActivitiesService activities() {
        if (activitiesService == null) {
            activitiesService = new ArcActivitiesService(http, arcBase);
        }
        return activitiesService;
    }

    /**
     * Returns the {@link ArcRisksService} for risk management operations.
     * The instance is created on first call and reused thereafter.
     */
    public ArcRisksService risks() {
        if (risksService == null) {
            risksService = new ArcRisksService(http, arcBase);
        }
        return risksService;
    }

    /**
     * Returns the {@link ArcMitigationsService} for mitigation management operations.
     * The instance is created on first call and reused thereafter.
     */
    public ArcMitigationsService mitigations() {
        if (mitigationsService == null) {
            mitigationsService = new ArcMitigationsService(http, arcBase);
        }
        return mitigationsService;
    }

    /**
     * Returns the {@link ArcUsersService} for ARC user analysis and risk-preview operations.
     * The instance is created on first call and reused thereafter.
     */
    public ArcUsersService users() {
        if (usersService == null) {
            usersService = new ArcUsersService(http, arcBase);
        }
        return usersService;
    }

    /**
     * Returns the {@link ArcStatsService} for ARC statistics, entity search,
     * and bulk operations. The instance is created on first call and reused thereafter.
     */
    public ArcStatsService stats() {
        if (statsService == null) {
            statsService = new ArcStatsService(http, arcBase);
        }
        return statsService;
    }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    /**
     * Extracts the origin ({@code scheme://host[:port]}) from a full URL.
     *
     * <p>Example: {@code "https://your-im-host:30943/itim/rest"} → {@code "https://your-im-host:30943"}
     */
    private static String extractOrigin(String baseUrl) {
        try {
            URI uri = URI.create(baseUrl);
            int port = uri.getPort();
            if (port == -1) {
                return uri.getScheme() + "://" + uri.getHost();
            }
            return uri.getScheme() + "://" + uri.getHost() + ":" + port;
        } catch (IllegalArgumentException e) {
            // Fallback: strip everything from the first path separator after the authority
            int slashIdx = baseUrl.indexOf('/', baseUrl.indexOf("//") + 2);
            return slashIdx == -1 ? baseUrl : baseUrl.substring(0, slashIdx);
        }
    }
}
