package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.meinc.deal.domain.AdInstance;

public class Question 
implements Serializable
{
    private static final long serialVersionUID = -8006483751534938626L;
    public static final int classId = 1005;
    
    public static final String NEW = "NEW";
    public static final String IN_PROGRESS = "INPROGRESS";
    public static final String COMPLETED = "COMPLETE";
    
    public static final int SCORING_MODE_NORMAL = 1;
    public static final int SCORING_MODE_INSTANT_WINNER = 2;
    
    public static final int TYPE_PREDICTIVE = 1;
    public static final int TYPE_PLAYERS_CHOICE = 2;
    public static final int TYPE_TRIVIA = 3;
    public static final int TYPE_OPINION_POLL = 4;
    
    public static final int GRACE_PERIOD_NOT_SET = -2;

    private int _questionId;

    /** The question to ask the user. */
    private String _questionText;
    
    private String _questionTextUuid;
    private List<Localized> _questionTextLocalized;

    /** 1=normal, 2=instant winner */
    private int _scoringMode;
    
    /** question type (aka predictive, etc) */
    private int _type;
        
    private int _campaignId;

    /**
     * These are the possible answers. If the type is YES_NO, the yes question
     * is first, the no question is second. If multiple choice, then the first
     * question that will appear is first and so on.
     */
    private List<Answer> _answers;

    /** One of: NEW, INPROGRESS, COMPLETE */
    private String _status;
    
    /** whether or not the question should count towards scoring */
    private boolean _notScored;

    /**
     * How many points the poll is worth. Only applicable if the type is NORMAL
     */
    private int _pointValue;

    /**
     * How many people can win this poll. Only applicable if the type is
     * INSTANT_WINNER.  -1=infinite
     */
    private int _numberOfWinners;
    
    /**
     * How many seconds of a 'grace period' the user has from the startedDate before time begins to count against them when answering (for scoring purposes).  -1=don't show clock, 0=infinite grace period, x=x second grace period.
     */
    private int _gracePeriod = GRACE_PERIOD_NOT_SET;
    
    private Date _startedDate;
    private Date _stoppedDate;
    
    /**
     * After this many minutes (once started), the question will automatically close.
     * 
     * NULL = don't auto close
     */
    private int _minutesToAutoClose = 0;
    
    /**
     * A message to be sent before the question text is sent.  This is optional.  Typical
     * use-case is for a sponsor message such as "the following question is brought to you by coke".
     * 
     * If this message is not null, it will be sent, and then after a short delay the actual
     * question will be sent.
     */
    private String _sponsorMessage;
    
    private Date _lastUpdated;
    
    private List<AdInstance> _adInstances;
    
    private List<QuestionMediaRef> _media;
    
    /**
     * This is used only by the Shout Console.  When a question is published it will be set to zero.
     */
    private int _sortOrder;
    
    /**
     * The question number is used by all clients and the shout console to know how to sort and display
     * questions.
     */
    private Integer _questionNumber;

    public Question() {
    }

    public int getQuestionId() {
        return _questionId;
    }

    public void setQuestionId(int questionId) {
        _questionId = questionId;
    }

    @JsonProperty(value="question")
    public String getQuestionText() {
        return _questionText;
    }

    @JsonProperty(value="question")
    public void setQuestionText(String question) {
        _questionText = question;
    }
    
    public String getQuestionTextUuid()
    {
        return _questionTextUuid;
    }

    public void setQuestionTextUuid(String questionTextUuid)
    {
        _questionTextUuid = questionTextUuid;
    }

    public List<Localized> getQuestionTextLocalized()
    {
        return _questionTextLocalized;
    }

    public void setQuestionTextLocalized(List<Localized> questionTextLocalized)
    {
        _questionTextLocalized = questionTextLocalized;
    }

    public int getScoringMode() {
        return _scoringMode;
    }

    public void setScoringMode(int type) {
        if (type != SCORING_MODE_NORMAL && type != SCORING_MODE_INSTANT_WINNER)
            throw new IllegalArgumentException("invalid type");
        
        _scoringMode = type;
    }

    public void setCampaignId(Integer campaignId) {
        if (campaignId != null) {
            _campaignId = campaignId;
        }
    }

    public Integer getCampaignId() {
        return _campaignId;
    }

    public List<Answer> getAnswers() {
        return _answers;
    }

    public void setAnswers(List<Answer> answers) {
        _answers = answers;
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String status) {
        if (!NEW.equals(status) && !IN_PROGRESS.equals(status) && !COMPLETED.equals(status))
            throw new IllegalArgumentException("invalid status: >" + status + "<");
        
        _status = status;
    }

    public boolean isNotScored()
    {
        return _notScored;
    }

    public void setNotScored(boolean notScored)
    {
        _notScored = notScored;
    }

    public int getPointValue() {
        return _pointValue;
    }

    public void setPointValue(int pointValue) {
        _pointValue = pointValue;
    }

    @JsonIgnore
    public int getNumberOfWinners() {
        return _numberOfWinners;
    }

    @JsonIgnore
    public void setNumberOfWinners(int numberOfWinners) {
        _numberOfWinners = numberOfWinners;
    }
    
    public int getGracePeriod()
    {
        return _gracePeriod;
    }
    
    public void setGracePeriod(int gracePeriod)
    {
        _gracePeriod = gracePeriod;
    }

    @JsonProperty(value="openTime")
    public Date getStartedDate()
    {
        return _startedDate;
    }

    @JsonProperty(value="openTime")
    public void setStartedDate(Date startedDate)
    {
        _startedDate = startedDate;
    }

    @JsonProperty(value="closeTime")
    public Date getStoppedDate()
    {
        return _stoppedDate;
    }

    @JsonProperty(value="closeTime")
    public void setStoppedDate(Date stoppedDate)
    {
        _stoppedDate = stoppedDate;
    }

    public int getMinutesToAutoClose()
    {
        return _minutesToAutoClose;
    }

    public void setMinutesToAutoClose(int minutesToAutoClose)
    {
        _minutesToAutoClose = minutesToAutoClose;
    }

    public String getSponsorMessage()
    {
        return _sponsorMessage;
    }

    public void setSponsorMessage(String sponsorMessage)
    {
        _sponsorMessage = sponsorMessage;
    }

    public void setAdInstances(List<AdInstance> adInstances)
    {
        _adInstances = adInstances;
    }

    public List<AdInstance> getAdInstances()
    {
        return _adInstances;
    }

    public Date getLastUpdated() {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        _lastUpdated = lastUpdated;
    }

    public List<QuestionMediaRef> getMedia() {
        return _media;
    }

    public void setMedia(List<QuestionMediaRef> media) {
        _media = media;
    }

    public int getType() {
        return _type;
    }

    public void setType(int type) {
        _type = type;
    }

    public int getSortOrder()
    {
        return _sortOrder;
    }

    public void setSortOrder(int sortOrder)
    {
        _sortOrder = sortOrder;
    }
    
    public Integer getQuestionNumber()
    {
        return _questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber)
    {
        _questionNumber = questionNumber;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(String.format("Question %d,\"%s\",Answers:[", _questionId, _questionText));
        if (_answers == null || _answers.isEmpty())
            result.append("<no answers>");
        else
            for (Answer answer : _answers) {
                result.append(String.format("%d:\"%s\",", answer.getAnswerId(), answer.getAnswerText()));
            }
        result.append("]");
        return result.toString();
    }
}
