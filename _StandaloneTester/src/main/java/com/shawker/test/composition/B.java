package com.shawker.test.composition;

import java.util.HashSet;
import java.util.Set;

public class B
implements TheInterface
{
    private TheCommonFunctionality _helper;
    private Set<String> _instanceSet = new HashSet<>();

    public B()
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
        System.out.println("B is adding to static and instance sets");
        TheCommonFunctionality.addToStaticSet("B-static");
        _helper.addToInstanceSet("B-instance");
    }

    @Override
    public void finish()
    {
        System.out.println("B is dumping static and instance sets");
        System.out.println("Static: ");
        TheCommonFunctionality.dumpStaticSet();
        System.out.println("Instance: ");
        _helper.dumpInstanceSet();
    }

}
