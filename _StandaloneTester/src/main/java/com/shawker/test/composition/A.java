package com.shawker.test.composition;

import java.util.HashSet;
import java.util.Set;

public class A
implements TheInterface
{
    private TheCommonFunctionality _helper;
    private Set<String> _instanceSet = new HashSet<>();

    public A()
    {
        _helper = new TheCommonFunctionality() {
            @Override
            Set<String> getInstanceSet()
            {
                return _instanceSet;
            }
        };
    }

    @Override
    public void run()
    {
        System.out.println("A is adding to static and instance sets");
        TheCommonFunctionality.addToStaticSet("A-static");
        _helper.addToInstanceSet("A-instance");
    }

    @Override
    public void finish()
    {
        System.out.println("A is dumping static and instance sets");
        System.out.println("Static: ");
        TheCommonFunctionality.dumpStaticSet();
        System.out.println("Instance: ");
        _helper.dumpInstanceSet();
    }

}
