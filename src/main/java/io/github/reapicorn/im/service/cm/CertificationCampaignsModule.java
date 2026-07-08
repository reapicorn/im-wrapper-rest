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
package io.github.reapicorn.im.service.cm;

import io.github.reapicorn.im.IMConfig;
import io.github.reapicorn.im.internal.HttpExecutor;

import java.net.URI;

/**
 * Module providing access to the IBM Verify Identity Manager <strong>Certification Campaigns</strong>
 * subsystem ({@code /itim/cm/v2.0/}).
 *
 * <p>This subsystem uses a different URL prefix from the main IM REST API
 * ({@code /itim/rest/}). The module computes the origin ({@code scheme://host:port})
 * from the configured {@code baseUrl} and passes it to each service so they can
 * construct correct absolute URLs.
 *
 * <p>Obtain an instance via {@link io.github.reapicorn.im.IMClient#certificationCampaigns()}.
 *
 * <p>Service instances are created lazily and cached.
 *
 * <pre>{@code
 * CertificationCampaignsModule cm = client.certificationCampaigns();
 *
 * // List all active campaign instances
 * Result<String> result = cm.instances().getInstances(SearchParams.empty());
 *
 * // Approve an assignment
 * cm.assignments().updateAssignment(assignmentId, null,
 *     "[{\"operation\":\"approved\",\"justification\":\"OK\"}]");
 * }</pre>
 */
public final class CertificationCampaignsModule {

    private final HttpExecutor http;
    private final String       cmBase;

    // Lazy sub-service instances
    private CampaignsService            campaignsService;
    private CampaignInstancesService    instancesService;
    private CampaignAssignmentsService  assignmentsService;
    private CampaignRecommendationsService recommendationsService;

    /**
     * Creates a new module.
     *
     * @param http   the shared {@link HttpExecutor} (authentication, cookies, CSRF)
     * @param config the {@link IMConfig} used to derive the CM origin URL
     */
    public CertificationCampaignsModule(HttpExecutor http, IMConfig config) {
        this.http   = http;
        this.cmBase = extractOrigin(config.getBaseUrl());
    }

    // ------------------------------------------------------------------
    //  Sub-service accessors (lazy, cached)
    // ------------------------------------------------------------------

    /**
     * Returns the {@link CampaignsService} for campaign configuration operations.
     * The instance is created on first call and reused thereafter.
     */
    public CampaignsService campaigns() {
        if (campaignsService == null) {
            campaignsService = new CampaignsService(http, cmBase);
        }
        return campaignsService;
    }

    /**
     * Returns the {@link CampaignInstancesService} for campaign instance operations.
     * The instance is created on first call and reused thereafter.
     */
    public CampaignInstancesService instances() {
        if (instancesService == null) {
            instancesService = new CampaignInstancesService(http, cmBase);
        }
        return instancesService;
    }

    /**
     * Returns the {@link CampaignAssignmentsService} for campaign assignment operations.
     * The instance is created on first call and reused thereafter.
     */
    public CampaignAssignmentsService assignments() {
        if (assignmentsService == null) {
            assignmentsService = new CampaignAssignmentsService(http, cmBase);
        }
        return assignmentsService;
    }

    /**
     * Returns the {@link CampaignRecommendationsService} for campaign recommendation
     * and insight operations.
     * The instance is created on first call and reused thereafter.
     */
    public CampaignRecommendationsService recommendations() {
        if (recommendationsService == null) {
            recommendationsService = new CampaignRecommendationsService(http, cmBase);
        }
        return recommendationsService;
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
