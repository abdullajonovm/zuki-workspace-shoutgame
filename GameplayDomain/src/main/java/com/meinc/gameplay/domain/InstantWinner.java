package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.meinc.identity.domain.Subscriber;


public class InstantWinner 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private Question _question;
    private List<Subscriber> _subscribers;
    private List<String> _prizes;
    private List<String> _nicknames;
    
    public InstantWinner()
    {
        _subscribers = new ArrayList<Subscriber>();
        _prizes = new ArrayList<String>();
        _nicknames = new ArrayList<String>();
    }

    public void setQuestion(Question question)
    {
        _question = question;
    }

    public Question getQuestion()
    {
        return _question;
    }
    
    public void addSubscriber(Subscriber s, String nickname)
    {
        _subscribers.add(s);
        _nicknames.add(nickname);
    }

    public List<Subscriber> getSubscribers()
    {
        return _subscribers;
    }
    
    public List<String> getNicknames()
    {
        return _nicknames;
    }
    
    public List<String> getPrizes() { return _prizes; }
    public void addPrize(String prize)
    {
        if (prize != null)
            _prizes.add(prize);
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append("Q: ").append(_question.getQuestionText()).append(", winners:");
        for (Subscriber s : _subscribers) {
            buf.append("\n");
            String fullname = s.getFirstname() + " " + s.getLastname();
            buf.append(" [").append(s.getSubscriberId()).append(":").append(fullname).append("]");
            buf.append(" - ").append(s.getEmail());
        }
        buf.append(", prizes: ");
        for (String prize : _prizes) {
            buf.append(" [").append(prize).append("]");
        }
        
        
        return buf.toString();
    }
    
}
