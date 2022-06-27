package com.meinc.identity.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * <pre>
 * {
 *   "subscriberId": int,
 *   "email": string,
 *   "emailType": enum[PAYPAL],
 *   "verified": bool,
 *   "createDate": iso8601 date,
 *   "verifiedDate": iso8601 date
 * }
 * </pre>
 */
public class SubscriberEmail
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum EMAIL_TYPE {PAYPAL}

    private long _subscriberId;
    private String _email;
    private EMAIL_TYPE _emailType;
    private boolean _verified;
    private Date _createDate;
    private Date _verifiedDate;

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
    public EMAIL_TYPE getEmailType()
    {
        return _emailType;
    }
    public void setEmailType(EMAIL_TYPE emailType)
    {
        _emailType = emailType;
    }
    public boolean isVerified()
    {
        return _verified;
    }
    public void setVerified(boolean verified)
    {
        _verified = verified;
    }
    public Date getCreateDate()
    {
        return _createDate;
    }
    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }
    public Date getVerifiedDate()
    {
        return _verifiedDate;
    }
    public void setVerifiedDate(Date verifiedDate)
    {
        _verifiedDate = verifiedDate;
    }

}
