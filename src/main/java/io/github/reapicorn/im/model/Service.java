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

import java.util.Map;

/**
 * Represents an IBM Verify Identity Manager Service connector ({@code /services} resource).
 *
 * <p>The {@code name} field corresponds to {@code _links.self.title} which
 * holds the service name. All additional server attributes are available via
 * {@link #getAttributes()}.
 */
public class Service {

    private String id;
    private String href;
    private String name;
    private Map<String, Object> attributes;

    public Service() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    /** Service display name ({@code _links.self.title}). */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    /**
     * Convenience accessor — returns the raw attribute value as a String,
     * or {@code null} if the attribute is absent.
     *
     * @param key attribute name, e.g. {@code "erservicename"}
     * @return attribute value, or {@code null}
     */
    public String getAttribute(String key) {
        if (attributes == null) return null;
        Object val = attributes.get(key);
        return val == null ? null : val.toString();
    }

    @Override
    public String toString() {
        return "Service{id='" + id + "', name='" + name
                + "', href='" + href + "', attributes=" + attributes + "}";
    }
}
