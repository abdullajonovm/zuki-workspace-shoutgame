package com.meinc.store.domain;

import java.io.Serializable;

public class Entitlement implements Serializable
{
    private static final long serialVersionUID = -8717076052156980523L;
    public static final int classId = 3001;

    private int _entitlementId;
    private String _uuid;
    private String _name;
    private int _quantity = 1;

    public int getEntitlementId()
    {
        return _entitlementId;
    }

    public void setEntitlementId(int entitlementId)
    {
        _entitlementId = entitlementId;
    }

    public String getUuid()
    {
        return _uuid;
    }

    public void setUuid(String uuid)
    {
        _uuid = uuid;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public int getQuantity()
    {
        return _quantity;
    }

    public void setQuantity(int quantity)
    {
        _quantity = quantity;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{entitlementId: ").append(_entitlementId).append(", uuid: ").append(_uuid).append(", name: ").append(_name).append("}");

        return sb.toString();
    }
}
