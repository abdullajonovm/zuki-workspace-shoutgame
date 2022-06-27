package com.meinc.mrsoa.monitor;

import java.util.Random;

/**
 * Artificially consumes CPU for testing purposes.
 * 
 * @author Matt
 */
public class CpuGobbler extends Thread {
  private static CpuGobbler gobbler;
  
  public static void gobble() {
    die();
    gobbler = new CpuGobbler();
    gobbler.setDaemon(true);
    gobbler.setPriority(Thread.NORM_PRIORITY + 1);
    gobbler.start();
  }
  
  public static void die() {
    if (gobbler != null) {
      gobbler.interrupt();
      try {
        gobbler.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      gobbler = null;
    }
  }

  @Override
  public void run() {
    // This code is designed to load the cpu without pegging it. If this isn't
    // working then you can always peg the cpu by emptying the while loop so it
    // is empty.
    while (!isInterrupted()) {
      Random rand = new Random();
      for (int i = 0; i < 10000; i++) {
        double d1 = Math.pow(2.0, rand.nextDouble());
        d1 += 2.0;
      }
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        interrupt();
      }
    }
  }
}
