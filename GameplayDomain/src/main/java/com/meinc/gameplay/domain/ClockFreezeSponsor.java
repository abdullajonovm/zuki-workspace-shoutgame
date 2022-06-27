package com.meinc.gameplay.domain;

import java.io.Serializable;

public class ClockFreezeSponsor 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int classId = 1004;

    private int _clockFreezeSponsorId;    //DB PK
    private String _name;
    private String _logo1xUrl;     // http://fully.qualified/url.png
    private String _logo2xUrl;     // http://fully.qualified/url.png
    private String _watch1xUrl;    // http://fully.qualified/url.png
    private String _watch2xUrl;    // http://fully.qualified/url.png
    private String _textColorRGB;  // #000000
    private String _textBgColorRGB;    // #000000
    
    public int getClockFreezeSponsorId()
    {
        return _clockFreezeSponsorId;
    }
    public void setClockFreezeSponsorId(int clockFreezeSponsorId)
    {
        _clockFreezeSponsorId = clockFreezeSponsorId;
    }
    public String getName()
    {
        return _name;
    }
    public void setName(String name)
    {
        _name = name;
    }
    public String getLogo1xUrl()
    {
        return _logo1xUrl;
    }
    public void setLogo1xUrl(String logo1xUrl)
    {
        _logo1xUrl = logo1xUrl;
    }
    public String getLogo2xUrl()
    {
        return _logo2xUrl;
    }
    public void setLogo2xUrl(String logo2xUrl)
    {
        _logo2xUrl = logo2xUrl;
    }
    public String getWatch1xUrl()
    {
        return _watch1xUrl;
    }
    public void setWatch1xUrl(String watch1xUrl)
    {
        _watch1xUrl = watch1xUrl;
    }
    public String getWatch2xUrl()
    {
        return _watch2xUrl;
    }
    public void setWatch2xUrl(String watch2xUrl)
    {
        _watch2xUrl = watch2xUrl;
    }
    public String getTextColorRGB()
    {
        return _textColorRGB;
    }
    public void setTextColorRGB(String textColorRGB)
    {
        _textColorRGB = textColorRGB;
    }
    public String getTextBgColorRGB()
    {
        return _textBgColorRGB;
    }
    public void setTextBgColorRGB(String textBgColorRGB)
    {
        _textBgColorRGB = textBgColorRGB;
    }
}
