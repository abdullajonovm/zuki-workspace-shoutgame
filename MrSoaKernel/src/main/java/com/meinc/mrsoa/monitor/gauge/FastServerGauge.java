package com.meinc.mrsoa.monitor.gauge;

public class FastServerGauge extends ServerLoadGauge {
  public FastServerGauge(int mquips) {
    super(mquips);
  }

  protected short calculateScoreForLoad(int serverLoad) {
    double x = serverLoad;
    return (short) (100.0 - (0.218 * x) - (0.007822 * Math.pow(x, 2)));
  }
}
