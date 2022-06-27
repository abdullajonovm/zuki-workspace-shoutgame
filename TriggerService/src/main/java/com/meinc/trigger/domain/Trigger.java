package com.meinc.trigger.domain;

import java.io.Serializable;
import java.util.Set;

public class Trigger 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _key;
    private Object _payload;
    private String _route;
    private String _source;
    private Set<String> _bundleIds;
    private int _contextId;
    
    public Trigger() 
    {
        this("*", "");
    }
    public Trigger(String key, Object payload)
    {
        this(key, payload, "8", key);
    }
    public Trigger(String key, Object payload, String route, String source)
    {
        this(key, payload, route, source, null, 0);
    }
    public Trigger(String key, Object payload, String route, String source, Set<String> bundleIds, int contextId)
    {
        _key = key;
        _payload = payload;
        _route = route;
        _source = source;
        if (route == null || route.length() == 0)
            _route = "*";
        if (source == null || source.length() == 0)
            _source = key;
        _bundleIds = bundleIds;
        _contextId = contextId;
    }
    public String getKey()
    {
        return _key;
    }
    public Object getPayload()
    {
        return _payload;
    }
    public String getRoute()
    {
        return _route;
    }
    public String getSource() {
        return _source;
    }
    public Set<String> getBundleIds()
    {
        return _bundleIds;
    }
    public int getContextId()
    {
        return _contextId;
    }
    public void setKey(String k)
    {
        _key = k;
    }
    public void setPayload(Object p)
    {
        _payload = p;
    }
    public void setRoute(String r)
    {
        _route = r;
    }
    public void setSource(String s) {
        _source = s;
    }
    public void setBundleIds(Set<String> b)
    {
        _bundleIds = b;
    }
    public void setContextId(int i)
    {
        _contextId = i;
    }
}