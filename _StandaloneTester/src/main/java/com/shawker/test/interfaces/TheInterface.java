package com.shawker.test.interfaces;

import java.util.HashSet;
import java.util.Set;

public interface TheInterface
{
    static Set<String> _staticSet = new HashSet<>();

    static void addToStaticSet(String value)
    {
        _staticSet.add(value);
    }

    default void addToInstanceSet(String value)
    {
        getInstanceSet().add(value);
    }

    static void dumpStaticSet()
    {
        System.out.println(_staticSet);
    }

    default void dumpInstanceSet()
    {
        System.out.println(getInstanceSet());
    }

    Set<String> getInstanceSet();
    void run();
    void finish();
}
