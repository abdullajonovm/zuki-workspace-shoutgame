package tv.shout.snowyowl.domain;

import java.io.Serializable;

public class GamePayout
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _gameId;
    private int _payoutModelId;
    private float _minimumPayoutAmount;
    private boolean _giveSponsorPlayerWinningsBackToSponsor;

    public String getGameId()
    {
        return _gameId;
    }
    public void setGameId(String gameId)
    {
        _gameId = gameId;
    }
    public int getPayoutModelId()
    {
        return _payoutModelId;
    }
    public void setPayoutModelId(int payoutModelId)
    {
        _payoutModelId = payoutModelId;
    }
    public float getMinimumPayoutAmount()
    {
        return _minimumPayoutAmount;
    }
    public void setMinimumPayoutAmount(float minimumPayoutAmount)
    {
        _minimumPayoutAmount = minimumPayoutAmount;
    }
    public boolean isGiveSponsorPlayerWinningsBackToSponsor()
    {
        return _giveSponsorPlayerWinningsBackToSponsor;
    }
    public void setGiveSponsorPlayerWinningsBackToSponsor(boolean giveSponsorPlayerWinningsBackToSponsor)
    {
        _giveSponsorPlayerWinningsBackToSponsor = giveSponsorPlayerWinningsBackToSponsor;
    }
}
