package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

public class PayoutModel
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _payoutModelId;
    private String _name;
    private int _basePlayerCount;
    private float _entranceFeeAmount; //$10 game gets this as an option to choose
    private boolean _active;
    private String _deactivationReason;
    private long _creatorId;
    private long _deactivatorId;
    private Date _createDate;
    private Date _deactivatedDate;
    private List<PayoutModelRound> _payoutModelRounds;

    public int getPayoutModelId()
    {
        return _payoutModelId;
    }
    public void setPayoutModelId(int payoutModelId)
    {
        _payoutModelId = payoutModelId;
    }
    public String getName()
    {
        return _name;
    }
    public void setName(String name)
    {
        _name = name;
    }
    public int getBasePlayerCount()
    {
        return _basePlayerCount;
    }
    public void setBasePlayerCount(int basePlayerCount)
    {
        _basePlayerCount = basePlayerCount;
    }
    public float getEntranceFeeAmount()
    {
        return _entranceFeeAmount;
    }
    public void setEntranceFeeAmount(float entranceFeeAmount)
    {
        _entranceFeeAmount = entranceFeeAmount;
    }
    public boolean isActive()
    {
        return _active;
    }
    public void setActive(boolean active)
    {
        _active = active;
    }
    public String getDeactivationReason()
    {
        return _deactivationReason;
    }
    public void setDeactivationReason(String deactivationReason)
    {
        _deactivationReason = deactivationReason;
    }
    public long getCreatorId()
    {
        return _creatorId;
    }
    public void setCreatorId(long creatorId)
    {
        _creatorId = creatorId;
    }
    public long getDeactivatorId()
    {
        return _deactivatorId;
    }
    public void setDeactivatorId(long deactivatorId)
    {
        _deactivatorId = deactivatorId;
    }
    public Date getCreateDate()
    {
        return _createDate;
    }
    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }
    public Date getDeactivatedDate()
    {
        return _deactivatedDate;
    }
    public void setDeactivatedDate(Date deactivatedDate)
    {
        _deactivatedDate = deactivatedDate;
    }
    public List<PayoutModelRound> getPayoutModelRounds()
    {
        return _payoutModelRounds;
    }
    public void setPayoutModelRounds(List<PayoutModelRound> payoutModelRounds)
    {
        _payoutModelRounds = payoutModelRounds;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
                "{0}. basePlayerCount: {1}, entranceFee: {2,number,currency}, Rounds:\n{3}",
                _name, _basePlayerCount, _entranceFeeAmount, _payoutModelRounds);
    }

}
