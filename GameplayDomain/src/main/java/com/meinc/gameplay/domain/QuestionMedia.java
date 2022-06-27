package com.meinc.gameplay.domain;

import java.io.Serializable;

public class QuestionMedia implements Serializable
{
    private static final long serialVersionUID = -3582680497715697860L;

    public static final int BEFORE = 0;
    public static final int AFTER = 1;
    
    private int _questionId;
    private int _type = BEFORE;
    private String _photoUrl;
    private String _videoUrl;
    private String _audioUrl;
    private String _text;
    private String _caption;
    
    public QuestionMedia()
    {
    }

    public QuestionMedia(int questionId, int type, String photoUrl, String videoUrl, String audioUrl, String text,
                         String caption)
    {
        _questionId = questionId;
        _type = type;
        _photoUrl = photoUrl;
        _videoUrl = videoUrl;
        _audioUrl = audioUrl;
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
        
        sb.append("photoUrl:");
        sb.append(_photoUrl == null ? "null" : _photoUrl);
        sb.append(", ");
        
        sb.append("videoUrl:");
        sb.append(_videoUrl == null ? "null" : _videoUrl);
        sb.append(", ");
        
        sb.append("audioUrl:");
        sb.append(_audioUrl == null ? "null" : _audioUrl);
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
