package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.util.Date;

import com.meinc.identity.domain.Subscriber;

/**
 * a minimal version of a subscriber returned from search criteria
 */
@SuppressWarnings("serial")
public class SubscriberFromSearch
implements Serializable
{
    private long _subscriberId;
    private String _email;
    private String _nickname;
    private String _firstname;
    private String _lastname;
    private Date _createDate;

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
    public String getFirstname()
    {
        return _firstname;
    }
    public void setFirstname(String firstname)
    {
        _firstname = firstname;
    }
    public String getLastname()
    {
        return _lastname;
    }
    public void setLastname(String lastname)
    {
        _lastname = lastname;
    }
    public Date getCreateDate()
    {
        return _createDate;
    }
    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }

    public static SubscriberFromSearch fromSubscriber(Subscriber subscriber)
    {
        SubscriberFromSearch ss = new SubscriberFromSearch();

        ss._subscriberId = subscriber.getSubscriberId();
        ss._email = subscriber.getEmail();
        ss._nickname = subscriber.getNickname();
        ss._firstname = subscriber.getFirstname();
        ss._lastname = subscriber.getLastname();
        ss._createDate = subscriber.getCreateDate();

        return ss;
    }
}
