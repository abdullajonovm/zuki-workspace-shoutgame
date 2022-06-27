package com.shawker.test.composition;

import java.util.HashSet;
import java.util.Set;

public abstract class TheCommonFunctionality
{
    private static Set<String> _staticSet = new HashSet<>();

    public static void addToStaticSet(String value)
    {
        _staticSet.add(value);
    }

    public void addToInstanceSet(String value)
    {
        getInstanceSet().add(value);
    }

    public static void dumpStaticSet()
    {
        System.out.println(_staticSet);
    }

    public void dumpInstanceSet()
    {
        System.out.println(getInstanceSet());
    }

    abstract Set<String> getInstanceSet();

}
