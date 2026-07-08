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

import io.github.reapicorn.im.internal.HttpExecutor;
import io.github.reapicorn.im.service.arc.ArcModule;
import io.github.reapicorn.im.service.cm.CertificationCampaignsModule;
import io.github.reapicorn.im.service.AccessCategoriesService;
import io.github.reapicorn.im.service.ApiSecurityService;
import io.github.reapicorn.im.service.AttributesService;
import io.github.reapicorn.im.service.AuthzService;
import io.github.reapicorn.im.service.EntitlementsService;
import io.github.reapicorn.im.service.AccessService;
import io.github.reapicorn.im.service.AccountsService;
import io.github.reapicorn.im.service.AciService;
import io.github.reapicorn.im.service.ActivitiesService;
import io.github.reapicorn.im.service.AdoptionPoliciesService;
import io.github.reapicorn.im.service.EntitiesService;
import io.github.reapicorn.im.service.FormsService;
import io.github.reapicorn.im.service.GroupsService;
import io.github.reapicorn.im.service.IdentityPolicyService;
import io.github.reapicorn.im.service.LifecycleRuleService;
import io.github.reapicorn.im.service.OrganizationsService;
import io.github.reapicorn.im.service.PasswordManagementService;
import io.github.reapicorn.im.service.PasswordPolicyService;
import io.github.reapicorn.im.service.PeopleService;
import io.github.reapicorn.im.service.ProvisioningPoliciesService;
import io.github.reapicorn.im.service.RequestsService;
import io.github.reapicorn.im.service.RolesService;
import io.github.reapicorn.im.service.ServicesService;
import io.github.reapicorn.im.service.SystemRolesService;
import io.github.reapicorn.im.service.SystemUsersService;
import io.github.reapicorn.im.service.ViewsService;
import io.github.reapicorn.im.service.WorkflowsService;
import io.github.reapicorn.im.service.WorkitemsService;

/**
 * Main entry point for the IBM Verify Identity Manager wrapper library.
 *
 * <p>Consumers create a single {@code IMClient} and call {@link #initSession()}
 * before using any service:
 * <pre>{@code
 * IMConfig config = IMConfig.builder()
 *     .baseUrl("https://your-im-host:30943/itim/rest")
 *     .username("itim manager")
 *     .password("your-password")
 *     .trustAllSsl(true)
 *     .build();
 *
 * IMClient client = new IMClient(config);
 * Result<Void> session = client.initSession();
 * if (!session.isSuccess()) {
 *     // handle error
 * }
 *
 * Result<List<Person>> persons = client.people().searchPeople(
 *     SearchParams.builder().attributes("cn","sn").limit(50).sort("+cn").build());
 * }</pre>
 *
 * <p>Service instances are created lazily and cached — safe to call the getter
 * multiple times. {@code IMClient} itself is not thread-safe; use one instance
 * per thread or synchronise externally.
 */
public final class IMClient {

    private final IMConfig   config;
    private final HttpExecutor http;

    // Lazy service instances
    private PeopleService              peopleService;
    private AccountsService            accountsService;
    private RolesService               rolesService;
    private SystemUsersService         systemUsersService;
    private OrganizationsService       organizationsService;
    private ServicesService            servicesService;
    private AccessService              accessService;
    private AccessCategoriesService    accessCategoriesService;
    private RequestsService            requestsService;
    private ActivitiesService          activitiesService;
    private WorkitemsService           workitemsService;
    private WorkflowsService           workflowsService;
    private PasswordManagementService  passwordManagementService;
    private PasswordPolicyService      passwordPolicyService;
    private FormsService               formsService;
    private ViewsService               viewsService;
    private EntitiesService            entitiesService;
    private GroupsService              groupsService;
    private SystemRolesService         systemRolesService;
    private IdentityPolicyService      identityPolicyService;
    private ProvisioningPoliciesService provisioningPoliciesService;
    private AdoptionPoliciesService    adoptionPoliciesService;
    private LifecycleRuleService       lifecycleRuleService;
    private AciService                 aciService;
    private CertificationCampaignsModule certificationCampaignsModule;
    private ArcModule                    arcModule;
    private EntitlementsService          entitlementsService;
    private AuthzService                 authzService;
    private ApiSecurityService           apiSecurityService;
    private AttributesService            attributesService;

    /**
     * Creates a new {@code IMClient} with the given configuration.
     * Does <em>not</em> establish a session — call {@link #initSession()} next.
     */
    public IMClient(IMConfig config) {
        this.config = config;
        this.http   = new HttpExecutor(config);
    }

    // ------------------------------------------------------------------
    //  Session
    // ------------------------------------------------------------------

    /**
     * Establishes a session with the IM server by calling
     * {@code GET /systemusers/me} and capturing the CSRF token.
     *
     * <p>Must be called once before any write operation. It is safe (but
     * redundant) to call multiple times — each call refreshes the CSRF token.
     *
     * @return {@code Result.success(null)} on success, or
     *         {@code Result.failure(error)} if the server returns an error
     */
    public Result<Void> initSession() {
        return http.initSession().map(ignored -> null);
    }

    // ------------------------------------------------------------------
    //  Service accessors (lazy, cached)
    // ------------------------------------------------------------------

    /**
     * Returns the {@link PeopleService} for person management operations.
     * The instance is created on first call and reused thereafter.
     */
    public PeopleService people() {
        if (peopleService == null) {
            peopleService = new PeopleService(http);
        }
        return peopleService;
    }

    /**
     * Returns the {@link AccountsService} for account management operations.
     */
    public AccountsService accounts() {
        if (accountsService == null) {
            accountsService = new AccountsService(http);
        }
        return accountsService;
    }

    /**
     * Returns the {@link RolesService} for role management operations.
     */
    public RolesService roles() {
        if (rolesService == null) {
            rolesService = new RolesService(http);
        }
        return rolesService;
    }

    /**
     * Returns the {@link SystemUsersService} for system user management operations.
     */
    public SystemUsersService systemUsers() {
        if (systemUsersService == null) {
            systemUsersService = new SystemUsersService(http);
        }
        return systemUsersService;
    }

    /**
     * Returns the {@link OrganizationsService} for organisational container operations.
     */
    public OrganizationsService organizations() {
        if (organizationsService == null) {
            organizationsService = new OrganizationsService(http);
        }
        return organizationsService;
    }

    /**
     * Returns the {@link ServicesService} for service connector and service profile operations.
     */
    public ServicesService services() {
        if (servicesService == null) {
            servicesService = new ServicesService(http);
        }
        return servicesService;
    }

    /**
     * Returns the {@link AccessService} for access management operations.
     */
    public AccessService access() {
        if (accessService == null) {
            accessService = new AccessService(http);
        }
        return accessService;
    }

    /**
     * Returns the {@link AccessCategoriesService} for access category operations.
     */
    public AccessCategoriesService accessCategories() {
        if (accessCategoriesService == null) {
            accessCategoriesService = new AccessCategoriesService(http);
        }
        return accessCategoriesService;
    }

    /**
     * Returns the {@link RequestsService} for request tracking and polling operations.
     *
     * <p>Use {@code requests().getRequest(requestId)} to poll the status of a
     * previous 202 asynchronous operation.
     */
    public RequestsService requests() {
        if (requestsService == null) {
            requestsService = new RequestsService(http);
        }
        return requestsService;
    }

    /**
     * Returns the {@link ActivitiesService} for activity management operations.
     */
    public ActivitiesService activities() {
        if (activitiesService == null) {
            activitiesService = new ActivitiesService(http);
        }
        return activitiesService;
    }

    /**
     * Returns the {@link WorkitemsService} for work item operations.
     */
    public WorkitemsService workitems() {
        if (workitemsService == null) {
            workitemsService = new WorkitemsService(http);
        }
        return workitemsService;
    }

    /**
     * Returns the {@link WorkflowsService} for workflow management operations.
     */
    public WorkflowsService workflows() {
        if (workflowsService == null) {
            workflowsService = new WorkflowsService(http);
        }
        return workflowsService;
    }

    /**
     * Returns the {@link PasswordManagementService} for password operations
     * (change password, challenge-response, configuration, retrieve).
     */
    public PasswordManagementService passwordManagement() {
        if (passwordManagementService == null) {
            passwordManagementService = new PasswordManagementService(http);
        }
        return passwordManagementService;
    }

    /**
     * Returns the {@link PasswordPolicyService} for password policy CRUD operations.
     */
    public PasswordPolicyService passwordPolicies() {
        if (passwordPolicyService == null) {
            passwordPolicyService = new PasswordPolicyService(http);
        }
        return passwordPolicyService;
    }

    /**
     * Returns the {@link FormsService} for form and form template operations.
     */
    public FormsService forms() {
        if (formsService == null) {
            formsService = new FormsService(http);
        }
        return formsService;
    }

    /**
     * Returns the {@link ViewsService} for view management operations.
     */
    public ViewsService views() {
        if (viewsService == null) {
            viewsService = new ViewsService(http);
        }
        return viewsService;
    }

    /**
     * Returns the {@link EntitiesService} for entity/widget filter search operations.
     */
    public EntitiesService entities() {
        if (entitiesService == null) {
            entitiesService = new EntitiesService(http);
        }
        return entitiesService;
    }

    /**
     * Returns the {@link GroupsService} for group management operations.
     */
    public GroupsService groups() {
        if (groupsService == null) {
            groupsService = new GroupsService(http);
        }
        return groupsService;
    }

    /**
     * Returns the {@link SystemRolesService} for system role (ITIM Group) management operations.
     */
    public SystemRolesService systemRoles() {
        if (systemRolesService == null) {
            systemRolesService = new SystemRolesService(http);
        }
        return systemRolesService;
    }

    /**
     * Returns the {@link IdentityPolicyService} for identity policy CRUD operations.
     */
    public IdentityPolicyService identityPolicies() {
        if (identityPolicyService == null) {
            identityPolicyService = new IdentityPolicyService(http);
        }
        return identityPolicyService;
    }

    /**
     * Returns the {@link ProvisioningPoliciesService} for provisioning policy CRUD operations.
     */
    public ProvisioningPoliciesService provisioningPolicies() {
        if (provisioningPoliciesService == null) {
            provisioningPoliciesService = new ProvisioningPoliciesService(http);
        }
        return provisioningPoliciesService;
    }

    /**
     * Returns the {@link AdoptionPoliciesService} for adoption policy CRUD operations.
     */
    public AdoptionPoliciesService adoptionPolicies() {
        if (adoptionPoliciesService == null) {
            adoptionPoliciesService = new AdoptionPoliciesService(http);
        }
        return adoptionPoliciesService;
    }

    /**
     * Returns the {@link LifecycleRuleService} for lifecycle rule CRUD operations.
     */
    public LifecycleRuleService lifecycleRules() {
        if (lifecycleRuleService == null) {
            lifecycleRuleService = new LifecycleRuleService(http);
        }
        return lifecycleRuleService;
    }

    /**
     * Returns the {@link AciService} for ACI (Access Control Item) CRUD operations.
     */
    public AciService acis() {
        if (aciService == null) {
            aciService = new AciService(http);
        }
        return aciService;
    }

    /**
     * Returns the {@link CertificationCampaignsModule} for the Certification Campaigns
     * subsystem ({@code /itim/cm/v2.0/}).
     *
     * <p>The module provides four sub-services:
     * <ul>
     *   <li>{@link CertificationCampaignsModule#campaigns()}       – campaign configurations</li>
     *   <li>{@link CertificationCampaignsModule#instances()}       – campaign instances</li>
     *   <li>{@link CertificationCampaignsModule#assignments()}     – campaign assignments</li>
     *   <li>{@link CertificationCampaignsModule#recommendations()} – recommendations &amp; insights</li>
     * </ul>
     */
    public CertificationCampaignsModule certificationCampaigns() {
        if (certificationCampaignsModule == null) {
            certificationCampaignsModule = new CertificationCampaignsModule(http, config);
        }
        return certificationCampaignsModule;
    }

    /**
     * Returns the {@link ArcModule} for the ARC (Analytics and Risk Console)
     * subsystem ({@code /itim/arc/v1.0/}).
     *
     * <p>The module provides five sub-services:
     * <ul>
     *   <li>{@link ArcModule#activities()}  – business activities &amp; activity folders</li>
     *   <li>{@link ArcModule#risks()}       – risk management</li>
     *   <li>{@link ArcModule#mitigations()} – mitigation management</li>
     *   <li>{@link ArcModule#users()}       – ARC user analysis and risk-preview</li>
     *   <li>{@link ArcModule#stats()}       – statistics, entity search, and bulk ops</li>
     * </ul>
     */
    public ArcModule arc() {
        if (arcModule == null) {
            arcModule = new ArcModule(http, config);
        }
        return arcModule;
    }

    /**
     * Returns the {@link EntitlementsService} for entitlement assignment operations.
     */
    public EntitlementsService entitlements() {
        if (entitlementsService == null) {
            entitlementsService = new EntitlementsService(http);
        }
        return entitlementsService;
    }

    /**
     * Returns the {@link AuthzService} for authorization (grant/revoke entitlements
     * and assignment attribute) operations ({@code /authz/v1.0/}).
     */
    public AuthzService authz() {
        if (authzService == null) {
            authzService = new AuthzService(http);
        }
        return authzService;
    }

    /**
     * Returns the {@link ApiSecurityService} for API security runtime entitlement
     * operations ({@code /apisecurity/v1.0/}).
     */
    public ApiSecurityService apiSecurity() {
        if (apiSecurityService == null) {
            apiSecurityService = new ApiSecurityService(http);
        }
        return apiSecurityService;
    }

    /**
     * Returns the {@link AttributesService} for attribute definition lookups
     * (e.g. role classification labels).
     */
    public AttributesService attributes() {
        if (attributesService == null) {
            attributesService = new AttributesService(http);
        }
        return attributesService;
    }

    // ------------------------------------------------------------------
    //  Accessors
    // ------------------------------------------------------------------

    /** Returns the configuration used to create this client. */
    public IMConfig getConfig() { return config; }

    /**
     * Returns the underlying {@link HttpExecutor}.
     * Intended for internal use by services implemented in later sub-tasks.
     */
    HttpExecutor getHttp() { return http; }
}
