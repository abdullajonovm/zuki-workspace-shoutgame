package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.meinc.gameplay.media.domain.Tag;

public class SandboxQuestion 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _sandboxQuestionId;
    private int _questionId;
    private String _context;
    private int _appId;
    private Integer _questionNumber;
    private String _questionImageUrl;
    private List<Tag> _tags;
    private List<Localized> _localizedQuestionText;
    private String _localizedQuestionTextUuid;
    private Date _createDate;
    
    public int getSandboxQuestionId()
    {
        return _sandboxQuestionId;
    }
    public void setSandboxQuestionId(int sandboxQuestionId)
    {
        _sandboxQuestionId = sandboxQuestionId;
    }
    public int getQuestionId()
    {
        return _questionId;
    }
    public void setQuestionId(int questionId)
    {
        _questionId = questionId;
    }
    public String getContext()
    {
        return _context;
    }
    public void setContext(String context)
    {
        _context = context;
    }
    public int getAppId()
    {
        return _appId;
    }
    public void setAppId(int appId)
    {
        _appId = appId;
    }
    public Integer getQuestionNumber()
    {
        return _questionNumber;
    }
    public void setQuestionNumber(Integer questionNumber)
    {
        _questionNumber = questionNumber;
    }
    public String getQuestionImageUrl()
    {
        return _questionImageUrl;
    }
    public void setQuestionImageUrl(String questionImageUrl)
    {
        _questionImageUrl = questionImageUrl;
    }
    public List<Tag> getTags()
    {
        return _tags;
    }
    public void setTags(List<Tag> tags)
    {
        _tags = tags;
    }
    public List<Localized> getLocalizedQuestionText()
    {
        return _localizedQuestionText;
    }
    public void setLocalizedQuestionText(List<Localized> localizedQuestionText)
    {
        _localizedQuestionText = localizedQuestionText;
    }
    public String getLocalizedQuestionTextUuid()
    {
        return _localizedQuestionTextUuid;
    }
    public void setLocalizedQuestionTextUuid(String localizedQuestionTextUuid)
    {
        _localizedQuestionTextUuid = localizedQuestionTextUuid;
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
