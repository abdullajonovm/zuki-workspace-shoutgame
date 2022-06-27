package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.text.MessageFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PayoutModelRound
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum CATEGORY { PHYSICAL, VIRTUAL }

    public static final String TYPE_CASH = "CASH";

    @JsonIgnore private int _payoutModelId;
    private int _sortOrder;  //sorts from winner, end round, backwards to first round
    private String _description; //ex: winner, round 20, round 1, etc
    private int _startingPlayerCount;
    private int _eliminatedPlayerCount;
    private float _eliminatedPayoutAmount;
    private String _type; //examples might include CASH, shout_creds, iPad, motorcycle, etc.
    private CATEGORY _category;
    private int _roundNumber; //1 based

    public int getPayoutModelId()
    {
        return _payoutModelId;
    }
    public void setPayoutModelId(int payoutModelId)
    {
        _payoutModelId = payoutModelId;
    }
    public int getSortOrder()
    {
        return _sortOrder;
    }
    public void setSortOrder(int sortOrder)
    {
        _sortOrder = sortOrder;
    }
    public String getDescription()
    {
        return _description;
    }
    public void setDescription(String description)
    {
        _description = description;
    }
    public int getStartingPlayerCount()
    {
        return _startingPlayerCount;
    }
    public void setStartingPlayerCount(int startingPlayerCount)
    {
        _startingPlayerCount = startingPlayerCount;
    }
    public int getEliminatedPlayerCount()
    {
        return _eliminatedPlayerCount;
    }
    public void setEliminatedPlayerCount(int eliminatedPlayerCount)
    {
        _eliminatedPlayerCount = eliminatedPlayerCount;
    }
    public float getEliminatedPayoutAmount()
    {
        return _eliminatedPayoutAmount;
    }
    public void setEliminatedPayoutAmount(float eliminatedPayoutAmount)
    {
        _eliminatedPayoutAmount = eliminatedPayoutAmount;
    }
    public String getType()
    {
        return _type;
    }
    public void setType(String type)
    {
        _type = type;
    }
    public CATEGORY getCategory()
    {
        return _category;
    }
    public void setCategory(CATEGORY category)
    {
        _category = category;
    }
    public int getRoundNumber()
    {
        return _roundNumber;
    }
    public void setRoundNumber(int roundNumber)
    {
        _roundNumber = roundNumber;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
            "startCount: {0}, elimCount: {1}, payout: {2,number}, type: {3}, category: {4}, roundNumber: {5}",
            _startingPlayerCount, _eliminatedPlayerCount, _eliminatedPayoutAmount, _type, _category, _roundNumber);
    }
}
