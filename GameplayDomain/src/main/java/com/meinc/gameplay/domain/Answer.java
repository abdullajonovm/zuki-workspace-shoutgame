/**
 * 
 */
package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author grant
 *
 */
public class Answer 
implements Serializable
{
    private static final long serialVersionUID = 4733696935434471135L;
    public static final int classId = 1006;
    private int _answerId;
    private int _questionId;
    private String _answerText;
    private String _answerTextUuid;
    private List<Localized> _answerTextLocalized;
    private String _answerCode;
    private int _displayOrder;
    private boolean _answerCorrect;
    
    /** of all the respondents, what percentage chose this answer (as opposed to the other answers for the question) */
    private double _percentageChosen;
    
    public int getAnswerId() {
        return _answerId;
    }
    public void setAnswerId(int answerId) {
        _answerId = answerId;
    }
    
    public int getQuestionId() {
        return _questionId;
    }
    public void setQuestionId(int questionId) {
        _questionId = questionId;
    }
    
    @JsonProperty(value="answer")
    public String getAnswerText() {
        return _answerText;
    }
    
    @JsonProperty(value="answer")
    public void setAnswerText(String answerText) {
        _answerText = answerText;
    }
    
    public String getAnswerTextUuid()
    {
        return _answerTextUuid;
    }
    public void setAnswerTextUuid(String answerTextUuid)
    {
        _answerTextUuid = answerTextUuid;
    }
    public List<Localized> getAnswerTextLocalized()
    {
        return _answerTextLocalized;
    }
    public void setAnswerTextLocalized(List<Localized> answerTextLocalized)
    {
        _answerTextLocalized = answerTextLocalized;
    }
    @JsonProperty(value="correct")
    public boolean isAnswerCorrect() {
        return _answerCorrect;
    }
    
    @JsonProperty(value="correct")
    public void setAnswerCorrect(boolean answerCorrect) {
        _answerCorrect = answerCorrect;
    }
    
    public int getDisplayOrder() {
        return _displayOrder;
    }
    public void setDisplayOrder(int displayOrder) {
        _displayOrder = displayOrder;
    }
    
    public String getAnswerCode() {
        return _answerCode;
    }
    public void setAnswerCode(String answerCode) {
        _answerCode = answerCode;
    }

    @JsonProperty(value="percentChoseThisAnswer")
    public double getPercentageChosen()
    {
        return _percentageChosen;
    }
    
    @JsonProperty(value="percentChoseThisAnswer")
    public void setPercentageChosen(double percentageChosen)
    {
        _percentageChosen = percentageChosen;
    }
}
