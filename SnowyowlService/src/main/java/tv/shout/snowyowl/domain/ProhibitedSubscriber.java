package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.util.Date;

import com.meinc.identity.domain.Subscriber;

public class ProhibitedSubscriber
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long _subscriberId;
    private String _email;
    private String _nickname;
    private String _reason;
    private String _note;
    private Date _createDate;

    public ProhibitedSubscriber() {}

    public ProhibitedSubscriber(Subscriber subscriber, String reason, String note)
    {
        _subscriberId = subscriber.getSubscriberId();
        _email = subscriber.getEmail();
        _nickname = subscriber.getNickname();
        _reason = reason;
        _note = note;
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
    public String getNickname()
    {
        return _nickname;
    }
    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }
    public String getReason()
    {
        return _reason;
    }
    public void setReason(String reason)
    {
        _reason = reason;
    }
    public String getNote()
    {
        return _note;
    }
    public void setNote(String note)
    {
        _note = note;
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
