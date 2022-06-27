package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class QuestionMediaRef implements Serializable
{
    private static final long serialVersionUID = -3582680497715697860L;
    public static final int classId = 1007;

    public static final int BEFORE = 0;
    public static final int AFTER = 1;
    
    private int _questionId;
    private int _type = BEFORE;
    private String _photoRefUuid;
    private String _videoRefUuid;
    private String _audioRefUuid;
    private String _photoUrl;
    private String _videoUrl;
    private String _audioUrl;
    private String _text;
    private String _caption;
    private Date _createdDate;
    private Date _lastUpdated;
    
    public QuestionMediaRef()
    {
    }

    public QuestionMediaRef(int questionId, int type, String photoRefUuid, String videoRefUuid, String audioRefUuid, String text, String caption)
    {
        _questionId = questionId;
        _type = type;
        _photoRefUuid = photoRefUuid;
        _videoRefUuid = videoRefUuid;
        _audioRefUuid = audioRefUuid;
        _text = text;
        _caption = caption;
    }

    public int getQuestionId()
    {
        return _questionId;
    }

    public void setQuestionId(int questionId)
    {
        _questionId = questionId;
    }

    public int getType()
    {
        return _type;
    }

    public void setType(int type)
    {
        _type = type;
    }

    public String getPhotoRefUuid()
    {
        return _photoRefUuid;
    }

    public void setPhotoRefUuid(String photoRefUuid)
    {
        _photoRefUuid = photoRefUuid;
    }

    public String getVideoRefUuid()
    {
        return _videoRefUuid;
    }

    public void setVideoRefUuid(String videoRefUuid)
    {
        _videoRefUuid = videoRefUuid;
    }

    public String getAudioRefUuid()
    {
        return _audioRefUuid;
    }

    public void setAudioRefUuid(String audioRefUuid)
    {
        _audioRefUuid = audioRefUuid;
    }
    
    public String getPhotoUrl()
    {
        return _photoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        _photoUrl = photoUrl;
    }

    public String getVideoUrl()
    {
        return _videoUrl;
    }

    public void setVideoUrl(String videoUrl)
    {
        _videoUrl = videoUrl;
    }

    public String getAudioUrl()
    {
        return _audioUrl;
    }

    public void setAudioUrl(String audioUrl)
    {
        _audioUrl = audioUrl;
    }

    public String getText()
    {
        return _text;
    }

    public void setText(String text)
    {
        _text = text;
    }

    public String getCaption()
    {
        return _caption;
    }

    public void setCaption(String caption)
    {
        _caption = caption;
    }
    
    public Date getCreatedDate()
    {
        return _createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        _createdDate = createdDate;
    }

    public Date getLastUpdated()
    {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        _lastUpdated = lastUpdated;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("{");
        
        sb.append("questionId:");
        sb.append(_questionId);
        sb.append(", ");
        
        sb.append("type:");
        sb.append(_type);
        sb.append(", ");
        
        sb.append("photoRefUuid:");
        sb.append(_photoRefUuid == null ? "null" : _photoRefUuid);
        sb.append(", ");
        
        sb.append("videoRefUuid:");
        sb.append(_videoRefUuid == null ? "null" : _videoRefUuid);
        sb.append(", ");
        
        sb.append("audioRefUuid:");
        sb.append(_audioRefUuid == null ? "null" : _audioRefUuid);
        sb.append(", ");
        
        sb.append("text:");
        sb.append(_text == null ? "null" : _text);
        sb.append(", ");
        
        sb.append("caption:");
        sb.append(_caption == null ? "null" : _caption);

        sb.append("}");
        
        return sb.toString();
    }
}
