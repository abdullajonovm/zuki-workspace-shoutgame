package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoleSyncData 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<String> deleted = new ArrayList<String>();
    private List<Role> roles = new ArrayList<Role>();
    
    public List<String> getDeleted()
    {
        return deleted;
    }
    public void setDeleted(List<String> deleted)
    {
        this.deleted = deleted;
    }
    public void addDeleted(String deletedRoleId)
    {
        this.deleted.add(deletedRoleId);
    }
    public List<Role> getRoles()
    {
        return roles;
    }
    public void setRoles(List<Role> roles)
    {
        this.roles = roles;
    }
    public void addRole(Role modifiedOrNewRole)
    {
        this.roles.add(modifiedOrNewRole);
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("ROLES:");
        for (Role role : roles) {
            buf.append("\n\t").append(role);
        }
        buf.append("\nDELETED:");
        for (String sid : deleted) {
            buf.append("\n\t").append(sid);
        }

        return buf.toString();
    }
}
