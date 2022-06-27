package com.shawker.test.interfaces;

import java.util.HashSet;
import java.util.Set;

public class A
implements TheInterface
{
    private Set<String> _instanceSet = new HashSet<>();

    @Override
    public Set<String> getInstanceSet()
    {
        return _instanceSet;
    }

    @Override
    public void run()
    {
        System.out.println("A is adding to static and instance sets");
        TheInterface.addToStaticSet("A-static");
        addToInstanceSet("A-instance");
    }

    @Override
    public void finish()
    {
        System.out.println("A is dumping static and instance sets");
        System.out.println("Static: ");
        TheInterface.dumpStaticSet();
        System.out.println("Instance: ");
        dumpInstanceSet();
    }

}
