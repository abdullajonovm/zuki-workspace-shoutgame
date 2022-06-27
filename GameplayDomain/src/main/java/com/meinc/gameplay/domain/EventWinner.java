package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.meinc.identity.domain.Subscriber;

public class EventWinner
implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String _winnerTitle;
    private Subscriber _winner;
    private List<String> _prizes;
    private EventWinner _recruitedBy;
    private String _additionalInfo;
    private String _nickname;

//    public EventWinner()
//    {
//    }
    
    public EventWinner(Subscriber winner)
    {
        if (winner == null)
            throw new IllegalArgumentException("winner is required");
        
        _winner = winner;
        _prizes = new ArrayList<String>();
    }
    
    public List<String> getPrizes() { return _prizes; }
    public void addPrize(String prize)
    {
        if (prize != null)
            _prizes.add(prize);
    }

    public void setWinnerTitle(String winnerTitle)
    {
        _winnerTitle = winnerTitle;
    }

    public String getWinnerTitle()
    {
        return _winnerTitle;
    }
    
    public Subscriber getWinner()
    {
        return _winner;
    }
    
    public void setRecruitedBy(EventWinner recruitedBy)
    {
        _recruitedBy = recruitedBy;
    }
    
    public EventWinner getRecruitedBy()
    {
        return _recruitedBy;
    }
    
    public String getAdditionalInfo()
    {
        return _additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo)
    {
        _additionalInfo = additionalInfo;
    }

    public String getNickname()
    {
        return _nickname;
    }

    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        String fullname = _winner.getFirstname() + " " + _winner.getLastname();
        
        buf.append("Title: ").append(_winnerTitle).append(", ").append(fullname).append(" [").append(_winner.getSubscriberId()).append("], prizes: ");
        for (String prize : _prizes) {
            buf.append(" [").append(prize).append("]");
        }
        if (_additionalInfo != null) {
            buf.append(", ").append(_additionalInfo);
        }
        buf.append(" - ").append(_winner.getEmail());
        if (_recruitedBy != null) {
            buf.append("\nRECRUITED BY: ").append(_recruitedBy.toString());
        }

        return buf.toString();
    }

}
