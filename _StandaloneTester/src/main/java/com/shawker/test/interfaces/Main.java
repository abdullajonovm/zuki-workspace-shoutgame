package com.shawker.test.interfaces;

public class Main
{
    public static void main(String[] args)
    {
        A a = new A();
        B b = new B();
        a.run();
        b.run();
        a.finish();
        b.finish();
    }

}
