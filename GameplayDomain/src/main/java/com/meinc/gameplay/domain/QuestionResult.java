package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.meinc.identity.domain.Subscriber;



public class QuestionResult
implements Serializable
{
    private static final long serialVersionUID = 4186335097659045210L;
    private Question _question;
    private Subscriber _winner;
    private List<String> _prizes;
    private String _nickname;
    
    public QuestionResult()
    {
        _prizes = new ArrayList<String>();
    }

    public Question getQuestion()
    {
        return _question;
    }

    public void setQuestion(Question question)
    {
        _question = question;
    }

    public Subscriber getWinner()
    {
        return _winner;
    }

    public void setWinner(Subscriber winner)
    {
        _winner = winner;
    }
    
    public List<String> getPrizes() { return _prizes; }
    public void addPrize(String prize)
    {
        if (prize != null)
            _prizes.add(prize);
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
        
        String qText = "";
        if (_question == null)
            qText = "<NO QUESTION>";
        else if (_question.getQuestionText() == null)
            qText = "<NO QUESTION TEXT>";
        buf.append("Q: ").append(qText).append(", [");
        if (_winner != null) {
            String fullname = _winner.getFirstname() + " " + _winner.getLastname();
            buf.append(_winner.getSubscriberId()).append("] ").append(fullname);
        } else
            buf.append("-] NONE");
        buf.append(", prizes: ");
        for (String prize : _prizes) {
            buf.append(" [").append(prize).append("]");
        }
        buf.append(" - ").append(_winner.getEmail());
        
        return buf.toString();
    }
}
