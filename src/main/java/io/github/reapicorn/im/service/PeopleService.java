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
import io.github.reapicorn.im.internal.JsonParser;
import io.github.reapicorn.im.model.Account;
import io.github.reapicorn.im.model.HalResource;
import io.github.reapicorn.im.model.Person;
import io.github.reapicorn.im.model.RequestResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Person Management</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET    /people}                     – search persons</li>
 *   <li>{@code POST   /people}                     – create a person</li>
 *   <li>{@code GET    /people/{id}}                – look up a person</li>
 *   <li>{@code PUT    /people/{id}}                – modify a person</li>
 *   <li>{@code PUT    /people/{id}} (suspend)      – suspend a person</li>
 *   <li>{@code PUT    /people/{id}} (restore)      – restore a suspended person</li>
 *   <li>{@code DELETE /people/{id}}                – delete a person</li>
 *   <li>{@code GET    /people/{id}/accounts}       – accounts of a person</li>
 *   <li>{@code GET    /people/me}                  – current person</li>
 *   <li>{@code GET    /people/person}              – search Person-category persons</li>
 *   <li>{@code GET    /people/bpperson}            – search BPPerson-category persons</li>
 *   <li>{@code GET    /people/profiles}            – list available person profile names</li>
 *   <li>{@code POST   /people/selfregister}        – self-register a new person</li>
 * </ul>
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class PeopleService {

    private final HttpExecutor http;

    public PeopleService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  Search / lookup
    // ------------------------------------------------------------------

    /**
     * Searches for persons across all categories.
     *
     * @param attributes comma-separated attributes to return, or {@code "*"} for all
     * @param limit      maximum number of results (null = server default)
     * @param sort       sort expression, e.g. {@code "+cn"} (null = server default)
     * @return {@code Result<List<Person>>}
     */
    public Result<List<Person>> searchPeople(String attributes, String limit, String sort) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes).sort(sort);
        if (limit != null) b.limit(Integer.parseInt(limit));
        return http.get("/people", b.build(), null)
                .map(json -> toPersonList(HalParser.toResourceList(json)));
    }

    /**
     * Searches for persons using a {@link SearchParams} object.
     *
     * @param params     query parameters (attributes, embedded, limit, sort)
     * @param range      pagination range, or {@code null} for no paging
     * @return {@code Result<List<Person>>}
     */
    public Result<List<Person>> searchPeople(SearchParams params, PageRange range) {
        return http.get("/people", params, range)
                .map(json -> toPersonList(HalParser.toResourceList(json)));
    }

    /**
     * Looks up a single person by their unique ID (the opaque Base64 token
     * from {@code _links.self.href}).
     *
     * @param personId   the person's unique identifier
     * @param attributes comma-separated attributes to return, or {@code null} for defaults
     * @return {@code Result<Person>}
     */
    public Result<Person> getPerson(String personId, String attributes) {
        SearchParams params = SearchParams.builder().attributesRaw(attributes).build();
        return http.get("/people/" + personId, params, null)
                .map(json -> toPerson(HalParser.toResource(json)));
    }

    /**
     * Returns information about the currently authenticated person.
     *
     * @param attributes comma-separated attributes to return, or {@code null} for defaults
     * @return {@code Result<Person>}
     */
    public Result<Person> getCurrentPerson(String attributes) {
        SearchParams params = SearchParams.builder().attributesRaw(attributes).build();
        return http.get("/people/me", params, null)
                .map(json -> toPerson(HalParser.toResource(json)));
    }

    /**
     * Returns all accounts belonging to the given person.
     *
     * @param personId   the person's unique identifier
     * @param attributes comma-separated attributes to return (e.g. {@code "eruid,eraccountstatus"})
     * @return {@code Result<List<Account>>}
     */
    public Result<List<Account>> getPersonAccounts(String personId, String attributes) {
        SearchParams params = SearchParams.builder().attributesRaw(attributes).build();
        return http.get("/people/" + personId + "/accounts", params, null)
                .map(json -> toAccountList(HalParser.toResourceList(json)));
    }

    /**
     * Searches for persons that belong to the <em>Person</em> profile category.
     *
     * <p>Equivalent to {@code GET /people/person}.
     *
     * @param attributes comma-separated attributes to return, or {@code "*"} for all
     * @param embedded   comma-separated reference attributes to embed, or {@code null}
     * @param limit      maximum number of results (null = server default)
     * @param sort       sort expression (null = server default)
     * @param range      pagination range, or {@code null} for no paging
     * @return {@code Result<List<Person>>}
     */
    public Result<List<Person>> searchPersonCategory(String attributes, String embedded,
                                                     String limit, String sort,
                                                     PageRange range) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes).sort(sort);
        if (embedded != null) b.embedded(embedded);
        if (limit    != null) b.limit(Integer.parseInt(limit));
        return http.get("/people/person", b.build(), range)
                .map(json -> toPersonList(HalParser.toResourceList(json)));
    }

    /**
     * Searches for persons that belong to the <em>BPPerson</em> (Business Partner Person)
     * profile category.
     *
     * <p>Equivalent to {@code GET /people/bpperson}.
     *
     * @param attributes comma-separated attributes to return, or {@code "*"} for all
     * @param embedded   comma-separated reference attributes to embed, or {@code null}
     * @param limit      maximum number of results (null = server default)
     * @param sort       sort expression (null = server default)
     * @param range      pagination range, or {@code null} for no paging
     * @return {@code Result<List<Person>>}
     */
    public Result<List<Person>> searchBpPersonCategory(String attributes, String embedded,
                                                       String limit, String sort,
                                                       PageRange range) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes).sort(sort);
        if (embedded != null) b.embedded(embedded);
        if (limit    != null) b.limit(Integer.parseInt(limit));
        return http.get("/people/bpperson", b.build(), range)
                .map(json -> toPersonList(HalParser.toResourceList(json)));
    }

    /**
     * Returns the list of available person profile names (e.g. {@code ["Person", "BPPerson"]}).
     *
     * <p>Equivalent to {@code GET /people/profiles}.
     *
     * @return {@code Result<List<String>>}
     */
    @SuppressWarnings("unchecked")
    public Result<List<String>> getPersonProfiles() {
        return http.get("/people/profiles", SearchParams.empty(), null)
                .map(json -> {
                    Object parsed = JsonParser.parse(json);
                    if (parsed instanceof Map<?, ?> map) {
                        Object profiles = map.get("profiles");
                        if (profiles instanceof List<?> list) {
                            List<String> names = new ArrayList<>();
                            for (Object item : list) {
                                if (item != null) names.add(item.toString());
                            }
                            return names;
                        }
                    }
                    return List.of();
                });
    }

    // ------------------------------------------------------------------
    //  Create
    // ------------------------------------------------------------------

    /**
     * Creates a new person via the standard provisioning flow.
     *
     * <p>Example body assembled by this method:
     * <pre>{@code
     * {
     *   "orgID": "<containerId>",
     *   "profileName": "Person",
     *   "attributes": [
     *     { "name": "cn",  "values": ["Jane Doe"] },
     *     { "name": "sn",  "values": ["Doe"] }
     *   ]
     * }
     * }</pre>
     *
     * @param orgId       container ID of the organisation
     * @param profileName person profile name, typically {@code "Person"} or {@code "BPPerson"}
     * @param attrs       attribute name → value(s) pairs
     * @return {@code Result<RequestResponse>} with the async request ID
     */
    public Result<RequestResponse> createPerson(String orgId, String profileName,
                                                Map<String, List<String>> attrs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgID", orgId);
        body.put("profileName", profileName);
        body.put("attributes", buildAttributeList(attrs));
        return http.post("/people", JsonParser.toJson(body))
                .map(HalParser::toRequestResponse);
    }

    /**
     * Self-registers a new person without requiring an authenticated IM session.
     *
     * <p>Equivalent to {@code POST /people/selfregister}.
     * The body must contain at minimum {@code profileName} and {@code orgID}.
     *
     * @param orgId       container ID of the organisation
     * @param profileName person profile name (e.g. {@code "Person"})
     * @param attrs       attribute name → value(s) pairs
     * @return {@code Result<RequestResponse>} with the async request ID
     */
    public Result<RequestResponse> selfRegisterPerson(String orgId, String profileName,
                                                      Map<String, List<String>> attrs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgID", orgId);
        body.put("profileName", profileName);
        body.put("attributes", buildAttributeList(attrs));
        return http.post("/people/selfregister", JsonParser.toJson(body))
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Modify
    // ------------------------------------------------------------------

    /**
     * Modifies attributes of an existing person.
     *
     * @param personId the person's unique identifier
     * @param attrs    attributes to update (name → values)
     * @return {@code Result<RequestResponse>} with the async request ID
     */
    public Result<RequestResponse> modifyPerson(String personId, Map<String, List<String>> attrs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("attributes", buildAttributeList(attrs));
        return http.put("/people/" + personId, JsonParser.toJson(body), null)
                .map(HalParser::toRequestResponse);
    }

    /**
     * Suspends the specified person.
     *
     * @param personId the person's unique identifier
     * @return {@code Result<RequestResponse>} with the async request ID
     */
    public Result<RequestResponse> suspendPerson(String personId) {
        return http.put("/people/" + personId, "{}", "suspend")
                .map(HalParser::toRequestResponse);
    }

    /**
     * Restores a suspended person.
     *
     * @param personId the person's unique identifier
     * @return {@code Result<RequestResponse>} with the async request ID
     */
    public Result<RequestResponse> restorePerson(String personId) {
        return http.put("/people/" + personId, "{}", "restore")
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Delete
    // ------------------------------------------------------------------

    /**
     * Deletes the specified person.
     *
     * @param personId the person's unique identifier
     * @return {@code Result<RequestResponse>} with the async request ID
     */
    public Result<RequestResponse> deletePerson(String personId) {
        return http.delete("/people/" + personId)
                .map(HalParser::toRequestResponse);
    }

    // ------------------------------------------------------------------
    //  Domain mapping helpers
    // ------------------------------------------------------------------

    private static List<Person> toPersonList(List<HalResource> resources) {
        List<Person> list = new ArrayList<>(resources.size());
        for (HalResource r : resources) {
            list.add(toPerson(r));
        }
        return list;
    }

    private static Person toPerson(HalResource r) {
        Person p = new Person();
        p.setId(r.getId());
        p.setHref(r.getHref());
        p.setName(r.getTitle());
        p.setAttributes(r.getAttributes().isEmpty() ? null : r.getAttributes());
        return p;
    }

    private static List<Account> toAccountList(List<HalResource> resources) {
        List<Account> list = new ArrayList<>(resources.size());
        for (HalResource r : resources) {
            list.add(toAccount(r));
        }
        return list;
    }

    private static Account toAccount(HalResource r) {
        Account a = new Account();
        a.setId(r.getId());
        a.setHref(r.getHref());
        a.setUid(r.getTitle());
        Map<String, Object> attrs = r.getAttributes();
        if (!attrs.isEmpty()) {
            a.setAttributes(attrs);
            Object uid = attrs.get("eruid");
            if (uid != null) a.setUid(uid.toString());
        }
        return a;
    }

    private static List<Map<String, Object>> buildAttributeList(Map<String, List<String>> attrs) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (attrs != null) {
            for (Map.Entry<String, List<String>> e : attrs.entrySet()) {
                Map<String, Object> attr = new LinkedHashMap<>();
                attr.put("name", e.getKey());
                attr.put("values", e.getValue());
                list.add(attr);
            }
        }
        return list;
    }
}
