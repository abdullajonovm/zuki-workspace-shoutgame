/**
 *
 */
package com.meinc.identity.domain;

import java.io.Serializable;

/**
 * @author grant
 *
 */
public class SubscriberIdAndLanguageCode
implements Serializable
{
    private static final long serialVersionUID = -9222244813073249864L;

    public static final int classId = 4;
    private Long _subscriberId;
    private String _languageCode;

    public SubscriberIdAndLanguageCode()
    {
    }

    public Long getSubscriberId()
    {
        return _subscriberId;
    }

    public void setSubscriberId(Long subscriberId)
    {
        _subscriberId = subscriberId;
    }

    public String getLanguageCode()
    {
        return _languageCode;
    }

    public void setLanguageCode(String languageCode)
    {
        _languageCode = languageCode;
    }
}
