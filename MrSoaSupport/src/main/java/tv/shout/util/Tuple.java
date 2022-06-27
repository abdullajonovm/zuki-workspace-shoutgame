package tv.shout.util;

import java.io.Serializable;

public class Tuple<T> 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private T _key;
    private T _val;
    
    public T getKey()
    {
        return _key;
    }
    public void setKey(T key)
    {
        _key = key;
    }
    public T getVal()
    {
        return _val;
    }
    public void setVal(T val)
    {
        _val = val;
    }
    
}
