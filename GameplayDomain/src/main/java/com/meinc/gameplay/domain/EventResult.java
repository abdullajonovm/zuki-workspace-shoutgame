/**
 * 
 */
package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Replaced by {@link EventResults}
 */
public class EventResult 
implements Serializable 
{
    private static final long serialVersionUID = -7942003055250300493L;
    
    private int _eventId;
    private String _eventName;
    
    private String _primaryTitle;
    private String _secondaryTitle;
    private String _grandprizeSectionTitle;
    private String _shoutNetworkSectionTitle;
    private String _questionWinnersSectionTitle;
    private String _instantWinnersSectionTitle;
    
    private List<EventWinner> _grandPrizeWinners;
    private List<EventWinner> _shoutNetworkWinners;
    private List<QuestionResult> _questionWinners;
    private List<InstantWinner> _instantWinners;
    private List<VipBoxScore> _vipboxWinners;
    
    /**
     * @deprecated
     */
    public EventResult()
    {
    }
    
    /**
     * @deprecated
     * @param eventId
     * @param eventName
     */
    public EventResult(int eventId, String eventName)
    {
        if (eventName == null) throw new IllegalArgumentException("eventName is required");
        
        _eventId = eventId;
        _eventName = eventName;
        
        _grandPrizeWinners = new ArrayList<EventWinner>();
        _shoutNetworkWinners = new ArrayList<EventWinner>();
        _questionWinners = new ArrayList<QuestionResult>();
        _instantWinners = new ArrayList<InstantWinner>();
        _vipboxWinners = new ArrayList<VipBoxScore>();
    }
    
    public int getEventId() { return _eventId; }
    public String getEventName() { return _eventName; }

    public void setPrimaryTitle(String primaryTitle)
    {
        _primaryTitle = primaryTitle;
    }

    public String getPrimaryTitle()
    {
        if (_primaryTitle == null)
            return "Winners for " + _eventName;
        else
            return _primaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle)
    {
        _secondaryTitle = secondaryTitle;
    }

    public String getSecondaryTitle()
    {
        return _secondaryTitle;
    }
    
    public void addGrandprizeWinner(EventWinner winner)
    {
        if (winner != null)
            _grandPrizeWinners.add(winner);
    }
    
    public List<EventWinner> getGrandprizeWinners() { return _grandPrizeWinners; }

    public void setGrandprizeSectionTitle(String grandprizeSectionTitle)
    {
        _grandprizeSectionTitle = grandprizeSectionTitle;
    }

    public String getGrandprizeSectionTitle()
    {
        if (_grandprizeSectionTitle == null)
            return "Grand Prize Winner" + (_grandPrizeWinners.size() > 1 ? "s" : "");
        else
            return _grandprizeSectionTitle;
    }

    public void addShoutNetworkWinner(EventWinner winner)
    {
        if (winner != null)
            _shoutNetworkWinners.add(winner);
    }
    
    public List<EventWinner> getShoutNetworkWinners() { return _shoutNetworkWinners; }
    
    public void setShoutNetworkSectionTitle(String shoutNetworkSectionTitle)
    {
        _shoutNetworkSectionTitle = shoutNetworkSectionTitle;
    }

    public String getShoutNetworkSectionTitle()
    {
        if (_shoutNetworkSectionTitle == null)
            return "Shout Network Winner" + (_shoutNetworkWinners.size() > 1 ? "s" : "");
        else
            return _shoutNetworkSectionTitle;
    }
    
    public void addQuestionWinner(QuestionResult winner)
    {
        if (winner != null)
            _questionWinners.add(winner);
    }
    
    public List<QuestionResult> getQuestionWinners() { return _questionWinners; }

    public void setQuestionWinnersSectionTitle(String questionWinnersSectionTitle)
    {
        _questionWinnersSectionTitle = questionWinnersSectionTitle;
    }

    public String getQuestionWinnersSectionTitle()
    {
        if (_questionWinnersSectionTitle == null)
            return "Question Winner" + (_questionWinners.size() > 1 ? "s" : "");
        else
            return _questionWinnersSectionTitle;
    }
    
    public void addInstantWinner(InstantWinner winner)
    {
        if (winner != null)
            _instantWinners.add(winner);
    }
    
    public List<InstantWinner> getInstantWinners() { return _instantWinners; }

    public void setInstantWinnersSectionTitle(String instantWinnersSectionTitle)
    {
        _instantWinnersSectionTitle = instantWinnersSectionTitle;
    }

    public String getInstantWinnersSectionTitle()
    {
        if (_instantWinnersSectionTitle == null)
            return "Instant Question Winner" + (_instantWinners.size() > 1 ? "s" : "");
        else
            return _instantWinnersSectionTitle;
    }
    
    public void addVipBoxWinner(VipBoxScore winner)
    {
        if (winner != null)
            _vipboxWinners.add(winner);
    }
    
    public List<VipBoxScore> getVipBoxWinners() { return _vipboxWinners; }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append("id: ").append(_eventId).append("\n");
        buf.append("name: ").append(_eventName).append("\n");
        buf.append("primary title: ").append(_primaryTitle).append("\n");
        buf.append("secondary title: ").append(_secondaryTitle).append("\n");
        buf.append("Grand Prize Winners [title: '").append(_grandprizeSectionTitle).append("']:\n");
        for (EventWinner winner : _grandPrizeWinners) {
            buf.append("\t").append(winner).append("\n");
        }
        buf.append("Shout Network Winners [title: '").append(_shoutNetworkSectionTitle).append("']:\n");
        for (EventWinner winner : _shoutNetworkWinners) {
            buf.append("\t").append(winner).append("\n");
        }
        buf.append("Question Winners [title: '").append(_questionWinnersSectionTitle).append("']:\n");
        for (QuestionResult winner : _questionWinners) {
            buf.append("\t").append(winner).append("\n");
        }
        buf.append("Instant Winners [title: '").append(_instantWinnersSectionTitle).append("']:\n");
        for (InstantWinner winner : _instantWinners) {
            buf.append("\t").append(winner).append("\n");
        }
        buf.append("VIP Box Winners:\n");
        for (VipBoxScore winner : _vipboxWinners) {
            buf.append("\t").append(winner).append("\n");
        }
        
        return buf.toString();
    }
    
}