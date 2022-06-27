package com.shawker.test.interfaces;

import java.util.HashSet;
import java.util.Set;

public class B
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
        System.out.println("B is adding to static and instance sets");
        TheInterface.addToStaticSet("B-static");
        addToInstanceSet("B-instance");
    }

    @Override
    public void finish()
    {
        System.out.println("B is dumping static and instance sets");
        System.out.println("Static: ");
        TheInterface.dumpStaticSet();
        System.out.println("Instance: ");
        dumpInstanceSet();
    }

}
