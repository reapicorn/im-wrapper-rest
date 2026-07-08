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
import io.github.reapicorn.im.model.Account;
import io.github.reapicorn.im.model.HalResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service wrapper for the IBM Verify Identity Manager <strong>Account</strong> REST API.
 *
 * <p>Covered endpoints:
 * <ul>
 *   <li>{@code GET /accounts}             – search accounts</li>
 *   <li>{@code GET /accounts/{accountId}} – look up a specific account</li>
 * </ul>
 *
 * <p>Account creation and deletion are performed indirectly through
 * person provisioning policies. Use {@link PeopleService#getPersonAccounts}
 * to retrieve accounts that belong to a specific person.
 *
 * <p>All methods return {@link Result}{@code <T>} — no exceptions escape to the caller.
 */
public class AccountsService {

    private final HttpExecutor http;

    public AccountsService(HttpExecutor http) {
        this.http = http;
    }

    // ------------------------------------------------------------------
    //  Search / lookup
    // ------------------------------------------------------------------

    /**
     * Searches accounts using the IM filter engine.
     *
     * @param attributes comma-separated attributes to return, or {@code "*"} for all
     *                   (e.g. {@code "eruid,eraccountstatus,eraccountownershiptype"})
     * @param embedded   comma-separated embedded reference attributes, or {@code null}
     *                   (e.g. {@code "erservice.erservicename"})
     * @param limit      maximum number of results (null = server default)
     * @param sort       sort expression (e.g. {@code "-eruid"})
     * @param rangeFrom  zero-based start index for paging (null = no range header)
     * @param rangeTo    zero-based end index for paging (null = no range header)
     * @return {@code Result<List<Account>>}
     */
    public Result<List<Account>> searchAccounts(String attributes,
                                                String embedded,
                                                String limit,
                                                String sort,
                                                Integer rangeFrom,
                                                Integer rangeTo) {
        SearchParams.Builder b = SearchParams.builder()
                .attributesRaw(attributes)
                .sort(sort);
        if (embedded != null) b.embedded(embedded);
        if (limit    != null) b.limit(Integer.parseInt(limit));

        PageRange range = (rangeFrom != null && rangeTo != null)
                ? PageRange.of(rangeFrom, rangeTo) : null;

        return http.get("/accounts", b.build(), range)
                .map(json -> toAccountList(HalParser.toResourceList(json)));
    }

    /**
     * Searches accounts using a {@link SearchParams} object.
     *
     * @param params query parameters (attributes, embedded, limit, sort)
     * @param range  pagination range, or {@code null} for no paging
     * @return {@code Result<List<Account>>}
     */
    public Result<List<Account>> searchAccounts(SearchParams params, PageRange range) {
        return http.get("/accounts", params, range)
                .map(json -> toAccountList(HalParser.toResourceList(json)));
    }

    /**
     * Returns all accounts provisioned on the specified service.
     *
     * <p>Equivalent to {@code GET /accounts?serviceId={serviceId}}.
     *
     * @param serviceId  the service's unique identifier
     * @param attributes comma-separated attributes to return, or {@code null} for defaults
     * @return {@code Result<List<Account>>}
     */
    public Result<List<Account>> getAccountsByService(String serviceId, String attributes) {
        String qs = "serviceId=" + serviceId;
        if (attributes != null && !attributes.isBlank()) {
            qs += "&attributes=" + attributes;
        }
        return http.get("/accounts?" + qs, SearchParams.empty(), null)
                .map(json -> toAccountList(HalParser.toResourceList(json)));
    }

    /**
     * Looks up a single account by its unique ID.
     *
     * @param accountId  the opaque Base64 account identifier from {@code _links.self.href}
     * @param attributes optional comma-separated attribute list (null = server defaults)
     * @param embedded   optional embedded attributes (null = none)
     * @return {@code Result<Account>}
     */
    public Result<Account> getAccount(String accountId, String attributes, String embedded) {
        SearchParams.Builder b = SearchParams.builder().attributesRaw(attributes);
        if (embedded != null) b.embedded(embedded);
        return http.get("/accounts/" + accountId, b.build(), null)
                .map(json -> toAccount(HalParser.toResource(json)));
    }

    // ------------------------------------------------------------------
    //  Domain mapping helpers
    // ------------------------------------------------------------------

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
}
