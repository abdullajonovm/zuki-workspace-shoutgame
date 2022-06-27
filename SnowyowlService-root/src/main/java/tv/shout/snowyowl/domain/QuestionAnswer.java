package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("serial")
public class QuestionAnswer
implements Serializable
{
    private String _id;
    private String _questionId;
    private Map<String,String> _answerText;
    private String _mediaUrl;
    private String _mediaType; //png, jpg, mp3, mp4, avi, etc...

    private Boolean _correct;

    private Integer _surveyPercent; //survey says ... 42

    private Date _createDate;

    public QuestionAnswer() {}

    public QuestionAnswer(String questionId, Map<String, String> answerText, Boolean correct)
    {
        this(questionId, answerText, null, null, correct, null);
    }

    public QuestionAnswer(String questionId, Map<String, String> answerText, String mediaUrl, String mediaType, Boolean correct, Integer surveyPercent)
    {
        _id = UUID.randomUUID().toString();
        _questionId = questionId;
        _answerText = answerText;
        _mediaUrl = mediaUrl;
        _mediaType = mediaType;
        _correct = correct;
        _surveyPercent = surveyPercent;
        _createDate = new Date();
    }

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public String getQuestionId()
    {
        return _questionId;
    }

    public void setQuestionId(String questionId)
    {
        _questionId = questionId;
    }

    public Map<String, String> getAnswerText()
    {
        return _answerText;
    }

    public void setAnswerText(Map<String, String> answerText)
    {
        _answerText = answerText;
    }

    public String getMediaUrl()
    {
        return _mediaUrl;
    }

    public void setMediaUrl(String mediaUrl)
    {
        _mediaUrl = mediaUrl;
    }

    public String getMediaType()
    {
        return _mediaType;
    }

    public void setMediaType(String mediaType)
    {
        _mediaType = mediaType;
    }

    public Boolean getCorrect()
    {
        return _correct;
    }

    public void setCorrect(Boolean correct)
    {
        _correct = correct;
    }

    public Integer getSurveyPercent()
    {
        return _surveyPercent;
    }

    public void setSurveyPercent(Integer surveyPercent)
    {
        _surveyPercent = surveyPercent;
    }

    public Date getCreateDate()
    {
        return _createDate;
    }

    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }
}
