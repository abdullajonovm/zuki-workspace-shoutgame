package com.meinc.mrsoa.monitor;

import java.util.ArrayList;

public class MemGobbler {
  private static ArrayList<Long> list = new ArrayList<Long>();
  
  public static void gobble() {
    for (int i = 0; i < 1000000; i++) {
      list.add((long)i);
    }
  }
  
  public static void release() {
    list.clear();
    System.gc();
  }
}
