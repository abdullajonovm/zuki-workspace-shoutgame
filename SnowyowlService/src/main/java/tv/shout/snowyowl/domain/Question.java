package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("serial")
public class Question
implements Serializable, Cloneable
{
    public enum STATUS {UNPUBLISHED, PUBLISHED, RETIRED}
    public enum TYPE {REGULAR, TIEBREAKER}

    private String _id;
    private TYPE _type = TYPE.REGULAR;
    private int _difficulty; //0-10 (0=no difficulty specified, 1=easy, 10=hard)
    private String _source; //where did the question come from?

    private Set<String> _languageCodes;
    private Set<String> _forbiddenCountryCodes;

    private Map<String,String> _questionText;
    private String _mediaUrl;
    private String _mediaType; //png, jpg, mp3, mp4, avi, etc...

    private Set<String> _questionCategoryUuids;

    private List<QuestionAnswer> _answers;

    private Date _createDate;
    private Date _expirationDate;

    private int _usageCount;

    @JsonIgnore
    private STATUS _status = STATUS.UNPUBLISHED;

//    public Question() {}
//
//    public Question(TYPE type, int difficulty, String source, Set<String> languageCodes, Set<String> forbiddenCountryCodes,
//            Map<String, String> questionText, String mediaUrl, String mediaType, Set<String> questionCategoryUuids,
//            List<QuestionAnswer> answers, Date expirationDate)
//    {
//        _type = type;
//        _id = UUID.randomUUID().toString();
//        _difficulty = difficulty;
//        _source = source;
//        _languageCodes = languageCodes;
//        _forbiddenCountryCodes = forbiddenCountryCodes;
//        _questionText = questionText;
//        _mediaUrl = mediaUrl;
//        _mediaType = mediaType;
//        _questionCategoryUuids = questionCategoryUuids;
//        _answers = answers;
//        _expirationDate = expirationDate;
//        _createDate = new Date();
//    }

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public TYPE getType()
    {
        return _type;
    }

    public void setType(TYPE type)
    {
        _type = type;
    }

    public int getDifficulty()
    {
        return _difficulty;
    }

    public void setDifficulty(int difficulty)
    {
        _difficulty = difficulty;
    }

    public String getSource()
    {
        return _source;
    }

    public void setSource(String source)
    {
        _source = source;
    }

    public Set<String> getLanguageCodes()
    {
        return _languageCodes;
    }

    public void setLanguageCodes(Set<String> languageCodes)
    {
        _languageCodes = languageCodes;
    }

    public Set<String> getForbiddenCountryCodes()
    {
        return _forbiddenCountryCodes;
    }

    public void setForbiddenCountryCodes(Set<String> forbiddenCountryCodes)
    {
        _forbiddenCountryCodes = forbiddenCountryCodes;
    }

    public Map<String, String> getQuestionText()
    {
        return _questionText;
    }

    public void setQuestionText(Map<String, String> questionText)
    {
        _questionText = questionText;
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

    public Set<String> getQuestionCategoryUuids()
    {
        return _questionCategoryUuids;
    }

    public void setQuestionCategoryUuids(Set<String> questionCategoryUuids)
    {
        _questionCategoryUuids = questionCategoryUuids;
    }

    public List<QuestionAnswer> getAnswers()
    {
        return _answers;
    }

    public void setAnswers(List<QuestionAnswer> answers)
    {
        _answers = answers;
    }

    public Date getCreateDate()
    {
        return _createDate;
    }

    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }

    public Date getExpirationDate()
    {
        return _expirationDate;
    }

    public void setExpirationDate(Date expirationDate)
    {
        _expirationDate = expirationDate;
    }

    public int getUsageCount()
    {
        return _usageCount;
    }

    public void setUsageCount(int usageCount)
    {
        _usageCount = usageCount;
    }

    public STATUS getStatus()
    {
        return _status;
    }

    public void setStatus(STATUS status)
    {
        _status = status;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof Question))
            return false;
        else
            return _id.equals( ((Question)obj).getId() );
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Question q = (Question) super.clone();

        //need to deep copy the answers because they will be changed on the cloned object but we don't want that reflected on the original object
        List<QuestionAnswer> clonedAnswers = new ArrayList<>();
        for(QuestionAnswer qa : _answers) {
            QuestionAnswer clonedAnswer = (QuestionAnswer) qa.clone();
            clonedAnswers.add(clonedAnswer);
        }
        q._answers = clonedAnswers;

        return q;
    }
}
