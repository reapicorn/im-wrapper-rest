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
package io.github.reapicorn.im.service;

import io.github.reapicorn.im.PageRange;
import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.SearchParams;
import io.github.reapicorn.im.internal.HalParser;
import io.github.reapicorn.im.internal.HttpExecutor;
import io.github.reapicorn.im.model.HalResource;

import java.util.List;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Forms and Form Templates</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET  /forms}                            – form template lookup (by requestee/access)</li>
 *   <li>{@code POST /forms}                            – label lookup for attributes</li>
 *   <li>{@code GET  /forms/people/{profileName}}        – form for a person profile</li>
 *   <li>{@code GET  /forms/resourcebundle}             – resource bundle (all attribute labels)</li>
 *   <li>{@code GET  /formtemplates}                    – list form templates</li>
 *   <li>{@code GET  /formtemplates/{profileName}}       – form template for a profile</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class FormsService {

    private final HttpExecutor http;

    public FormsService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  /forms
    // ------------------------------------------------------------------

    /**
     * Returns the form of the specified entity identified by requestee and optionally an access ID.
     *
     * @param requesteeId the unique identifier of the person (required)
     * @param accessId    the unique identifier of the access, or {@code null}
     * @param embedded    embedded reference attributes (e.g. {@code "form.resourcebundle"}), or {@code null}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getForm(String requesteeId, String accessId, String embedded) {
        StringBuilder path = new StringBuilder("/forms?requestee=").append(requesteeId);
        if (accessId != null && !accessId.isBlank()) {
            path.append("&access=").append(accessId);
        }
        if (embedded != null && !embedded.isBlank()) {
            path.append("&embedded=").append(embedded);
        }
        return http.get(path.toString(), SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Returns label information for the given comma-separated attribute names.
     * For example the label of attribute {@code cn} is {@code Full Name}.
     *
     * @param attributeCsv comma-separated attribute names, e.g. {@code "cn,sn,mail"}
     * @return {@code Result<String>} containing the raw JSON label array
     */
    public Result<String> getAttributeLabels(String attributeCsv) {
        return http.post("/forms", attributeCsv);
    }

    /**
     * Returns the form for the specified person profile.
     *
     * @param profileName the profile name, e.g. {@code "Person"} or {@code "BPPerson"}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getFormForPeopleProfile(String profileName) {
        return http.get("/forms/people/" + profileName, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    /**
     * Returns the resource bundle containing all attribute labels for Identity Governance.
     *
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getResourceBundle() {
        return http.get("/forms/resourcebundle", SearchParams.empty(), null)
                .map(HalParser::toResource);
    }

    // ------------------------------------------------------------------
    //  /formtemplates
    // ------------------------------------------------------------------

    /**
     * Retrieves a list of form templates, optionally filtered by custom class, form name, or profile name.
     *
     * @param ercustomclass  filter by custom class, or {@code null}
     * @param erformname     filter by form name, or {@code null}
     * @param profileName    filter by profile name, or {@code null}
     * @param params         additional query parameters (limit, etc.)
     * @param range          pagination range, or {@code null}
     * @return {@code Result<List<HalResource>>}
     */
    public Result<List<HalResource>> searchFormTemplates(String ercustomclass, String erformname,
                                                          String profileName, SearchParams params,
                                                          PageRange range) {
        StringBuilder path = new StringBuilder("/formtemplates");
        String qs = params.toQueryString();
        boolean first = true;
        if (ercustomclass != null && !ercustomclass.isBlank()) {
            path.append(first ? "?" : "&").append("ercustomclass=").append(ercustomclass);
            first = false;
        }
        if (erformname != null && !erformname.isBlank()) {
            path.append(first ? "?" : "&").append("erformname=").append(erformname);
            first = false;
        }
        if (profileName != null && !profileName.isBlank()) {
            path.append(first ? "?" : "&").append("profileName=").append(profileName);
            first = false;
        }
        if (!qs.isBlank()) {
            path.append(first ? "?" : "&").append(qs);
        }
        return http.get(path.toString(), SearchParams.empty(), range)
                .map(HalParser::toResourceList);
    }

    /**
     * Returns the form template for the specified profile.
     *
     * @param profileName the profile name, e.g. {@code "Person"}, {@code "WinLocalAccount"}
     * @return {@code Result<HalResource>}
     */
    public Result<HalResource> getFormTemplate(String profileName) {
        return http.get("/formtemplates/" + profileName, SearchParams.empty(), null)
                .map(HalParser::toResource);
    }
}
