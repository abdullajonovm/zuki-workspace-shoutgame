package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.util.Date;

public class IneligibleSubscriber
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _isId;
    private long _subscriberId;
    private String _email;
    private Long _linkedSubscriberId;
    private String _linkedEmail;
    private String _reason;
    private Date _createDate;

    public IneligibleSubscriber() {}

    public IneligibleSubscriber(long subscriberId, String email, Long linkedSubscriberId, String linkedEmail, String reason)
    {
        _subscriberId = subscriberId;
        _email = email;
        _linkedSubscriberId = linkedSubscriberId;
        _linkedEmail = linkedEmail;
        _reason = reason;
    }

    public int getIsId()
    {
        return _isId;
    }
    public void setIsId(int isId)
    {
        _isId = isId;
    }
    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public String getEmail()
    {
        return _email;
    }

    public void setEmail(String email)
    {
        _email = email;
    }

    public Long getLinkedSubscriberId()
    {
        return _linkedSubscriberId;
    }
    public void setLinkedSubscriberId(Long linkedSubscriberId)
    {
        _linkedSubscriberId = linkedSubscriberId;
    }
    public String getLinkedEmail()
    {
        return _linkedEmail;
    }

    public void setLinkedEmail(String linkedEmail)
    {
        _linkedEmail = linkedEmail;
    }

    public String getReason()
    {
        return _reason;
    }
    public void setReason(String reason)
    {
        _reason = reason;
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
