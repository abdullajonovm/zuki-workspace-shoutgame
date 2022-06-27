package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.restfb.Facebook;

public class FbSubscriber
implements Serializable
{
    private static final long serialVersionUID = 2L;
    
    private static Pattern _profileJson = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");
    
    private boolean _profileNormalized;
    
    @Facebook(value="id")    
    private String _fbId;
    
    private int _subscriberId;
    
    @Facebook(value="name")
    private String _name;
    
    @Facebook(value="first_name")
    private String _firstName;
    
    @Facebook(value="last_name")
    private String _lastName;
    
    @Facebook(value="picture")
    private String _picture;
    
    @Facebook(value="link")
    private String _profileLink;
    
    @Facebook(value="gender")
    private String _gender;
    
    @Facebook(value="email")
    private String _email;
    
    @Facebook(value="timezone")
    private int _timezone; //UTC-hour offset
    
    @Facebook(value="locale")
    private String _locale; //localization
    
    @Facebook(value="verified")
    private boolean _verified;
    
    @Facebook(value="username")
    private String _username;
    
    private Date _lastUpdated;
    
    public FbSubscriber()
    {
        _profileNormalized = false;
    }

    public String getFbId()
    {
        return _fbId;
    }

    public void setFbId(String fbId)
    {
        _fbId = fbId;
    }

    public int getSubscriberId()
    {
        return _subscriberId;
    }

    public void setSubscriberId(int subscriberId)
    {
        _subscriberId = subscriberId;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getFirstName()
    {
        return _firstName;
    }

    public void setFirstName(String firstName)
    {
        _firstName = firstName;
    }

    public String getLastName()
    {
        return _lastName;
    }

    public void setLastName(String lastName)
    {
        _lastName = lastName;
    }

    public String getPicture() {
        if (_profileNormalized) return _picture;
        if (_picture != null && _picture.length() > 0) {
            if (_picture.startsWith("{")) {
                Matcher m = _profileJson.matcher(_picture);
                if (m.find() && m.groupCount() > 0) {
                    _picture = m.group(1);
                }
            }
            _profileNormalized = true;
        }
        return _picture;
    }

    public void setPicture(String picture) {
        _picture = picture;
    }

    public String getProfileLink()
    {
        return _profileLink;
    }

    public void setProfileLink(String profileLink)
    {
        _profileLink = profileLink;
    }

    public String getGender()
    {
        return _gender;
    }

    public void setGender(String gender)
    {
        _gender = gender;
    }

    public String getEmail()
    {
        return _email;
    }

    public void setEmail(String email)
    {
        _email = email;
    }

    public int getTimezone()
    {
        return _timezone;
    }

    public void setTimezone(int timezone)
    {
        _timezone = timezone;
    }

    public String getLocale()
    {
        return _locale;
    }

    public void setLocale(String locale)
    {
        _locale = locale;
    }

    public boolean isVerified()
    {
        return _verified;
    }

    public void setVerified(boolean verified)
    {
        _verified = verified;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public Date getLastUpdated()
    {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        _lastUpdated = lastUpdated;
    }

}
