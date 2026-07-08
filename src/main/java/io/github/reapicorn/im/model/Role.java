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
 * Represents a Role (IBM Verify Identity Manager /roles resource).
 */
public class Role {

    private String id;
    private String href;
    private String roleName;
    private Map<String, Object> attributes;

    public Role() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    public String getAttribute(String key) {
        if (attributes == null) return null;
        Object val = attributes.get(key);
        return val == null ? null : val.toString();
    }

    @Override
    public String toString() {
        return "Role{id='" + id + "', roleName='" + roleName + "', href='" + href + "', attributes=" + attributes + "}";
    }
}
