package com.meinc.mrsoa.monitor.gauge;

public abstract class ServerLoadGauge {
  /**
   * Used to calculate a servers relative speed to a "fast" server. A server's
   * score is adjusted according to how near its MQUIPS score is to this score.
   */
  private static final int maxMQUIPS = 150;
  
  public static ServerLoadGauge getGaugeForServer(int mquips) {
    ServerLoadGauge serverGauge;
    
    if (mquips < 50)
      serverGauge = new SlowServerGauge(mquips);
    else if (mquips > 100)
      serverGauge = new FastServerGauge(mquips);
    else
      serverGauge = new AverageServerGauge(mquips);
    
    return serverGauge;
  }
  
  //TODO: 101?  is that a typo?
  protected short scores[] = new short[101];
  
  protected abstract short calculateScoreForLoad(int serverLoad);
  
  public ServerLoadGauge(int mquips) {
    for (int i = 0; i <= 100; i++) {
      scores[i] = adjustScoreForMQUIPS(calculateScoreForLoad(i), mquips);
    }
  }
  
  /**
   * Adjust a server's responsive score to take into account its MQUIPS score
   * relative to other servers. This calculation is designed so that if
   * server A is a faster machine than server B, server A's scores will be
   * slightly higher than server B's scores under equal load percentages.
   * 
   * @param rawScore
   *          The raw responsive score
   * @param mquips
   *          The MQUIPS rating of this server
   * @return The adjusted responsive score
   */
  private short adjustScoreForMQUIPS(short rawScore, int mquips) {
    // 5 points are influenced by mquips score of the server
    short mquipsScore = (short) (5 * mquips / maxMQUIPS);
    short newScore = (short) (rawScore - 5 + mquipsScore);
    if (newScore < 0)        newScore = 0;
    else if (newScore > 100) newScore = 100;
    return newScore;
  }

  public short getResponsiveScoreForLoad(int serverLoad) {
    if (serverLoad < 0)        serverLoad = 0;
    else if (serverLoad > 100) serverLoad = 100;
    return scores[serverLoad];
  }
}
