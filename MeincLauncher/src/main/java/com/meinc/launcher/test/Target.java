package com.meinc.launcher.test;

/**
 * Simple object to be used for testing bytecode injection.
 * 
 * @author Matt
 */
public class Target {
  public static synchronized void main(String[] args) throws InterruptedException {
    System.out.println(">>>Target.main() called<<<");
    Target.class.wait();
  }
}
