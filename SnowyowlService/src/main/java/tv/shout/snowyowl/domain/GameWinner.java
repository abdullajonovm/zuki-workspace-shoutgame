package tv.shout.snowyowl.domain;

import java.io.Serializable;

public class GameWinner
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _gameId;
    private Integer _rank;
    private String _email;
    private String _nickname;
    private long _subscriberId;
    private Double _amount;

    public String getGameId()
    {
        return _gameId;
    }
    public void setGameId(String gameId)
    {
        _gameId = gameId;
    }
    public Integer getRank()
    {
        return _rank;
    }
    public void setRank(Integer rank)
    {
        _rank = rank;
    }
    public String getEmail()
    {
        return _email;
    }
    public void setEmail(String email)
    {
        _email = email;
    }
    public String getNickname()
    {
        return _nickname;
    }
    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }
    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public Double getAmount()
    {
        return _amount;
    }
    public void setAmount(Double amount)
    {
        _amount = amount;
    }

}
