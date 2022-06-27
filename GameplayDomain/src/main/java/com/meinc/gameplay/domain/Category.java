package com.meinc.gameplay.domain;

import java.io.Serializable;

public class Category
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _categoryId;
    private String _categoryName;
    private String _categoryAlias;
    private boolean _active;
    private boolean _default;
    private int _order;
    
    public Category()
    {
    }

    public Category(int categoryId, String categoryName, String categoryAlias, boolean active, boolean def, int order)
    {
        _categoryId = categoryId;
        _categoryName = categoryName;
        _categoryAlias = categoryAlias;
        _active = active;
        _default = def;
        _order = order;
    }

    public int getCategoryId()
    {
        return _categoryId;
    }

    public void setCategoryId(int categoryId)
    {
        _categoryId = categoryId;
    }

    public String getCategoryName()
    {
        return _categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        _categoryName = categoryName;
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean active)
    {
        _active = active;
    }

    public void setDefault(boolean _default)
    {
        this._default = _default;
    }

    public boolean isDefault()
    {
        return _default;
    }

    public void setCategoryAlias(String categoryAlias)
    {
        _categoryAlias = categoryAlias;
    }

    public String getCategoryAlias()
    {
        return _categoryAlias;
    }

    public int getOrder()
    {
        return _order;
    }

    public void setOrder(int order)
    {
        _order = order;
    }

}
