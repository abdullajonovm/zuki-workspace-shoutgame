package tv.shout.snowyowl.domain;

import java.io.Serializable;

import com.meinc.identity.domain.Subscriber;

public class AwaitingPayout
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _manualRedeemRequestId;
    private long _subscriberId;
    private float _amount;
    private String _nickname;
    private String _email;
    private String _phone;
    private String _name;

    public int getManualRedeemRequestId()
    {
        return _manualRedeemRequestId;
    }
    public void setManualRedeemRequestId(int manualRedeemRequestId)
    {
        _manualRedeemRequestId = manualRedeemRequestId;
    }
    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public float getAmount()
    {
        return _amount;
    }
    public void setAmount(float amount)
    {
        _amount = amount;
    }
    public String getNickname()
    {
        return _nickname;
    }
    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }
    public String getEmail()
    {
        return _email;
    }
    public void setEmail(String email)
    {
        _email = email;
    }
    public String getPhone()
    {
        return _phone;
    }
    public void setPhone(String phone)
    {
        _phone = phone;
    }
    public String getName()
    {
        return _name;
    }
    public void setName(String name)
    {
        _name = name;
    }

    public static AwaitingPayout fromSubscriber(Subscriber subscriber, float amount, int manualRedeemRequestId)
    {
        AwaitingPayout ap = new AwaitingPayout();

        ap.setManualRedeemRequestId(manualRedeemRequestId);
        ap.setSubscriberId(subscriber.getSubscriberId());
        ap.setAmount(amount);
        ap.setNickname(subscriber.getNickname());
        ap.setEmail(subscriber.getEmail());
        ap.setPhone(subscriber.getPhone());
        ap.setName(subscriber.getFirstname() + " " + subscriber.getLastname());

        return ap;
    }

}
