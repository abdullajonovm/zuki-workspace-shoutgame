package com.meinc.mrsoa.monitor.gauge;

public class SlowServerGauge extends ServerLoadGauge {
  public SlowServerGauge(int mquips) {
    super(mquips);
  }

  protected short calculateScoreForLoad(int serverLoad) {
    double x = serverLoad;
    return (short) (89.0 - (1.452 * x) + (0.005618 * Math.pow(x, 2)));
  }
}
