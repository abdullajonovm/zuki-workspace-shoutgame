package com.meinc.mrsoa.monitor.gauge;

public class AverageServerGauge extends ServerLoadGauge {
  public AverageServerGauge(int mquips) {
    super(mquips);
  }

  protected short calculateScoreForLoad(int serverLoad) {
    double x = serverLoad;
    return (short) (95.0 - (0.95 * x));
  }
}
