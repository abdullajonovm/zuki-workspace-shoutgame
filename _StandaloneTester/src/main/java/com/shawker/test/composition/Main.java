package com.shawker.test.composition;

public class Main
{
    public static void main(String[] args)
    {
        System.out.println("COMPOSITION");
        A a = new A();
        B b = new B();
        a.run();
        b.run();
        a.finish();
        b.finish();
    }

}
