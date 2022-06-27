package tv.shout.sync.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;

public class SyncMessage
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _id;
    private long _subscriberId;
    private String _messageType;
    private String _contextualId; //gameId, etc.
    private String _engineKey; //DM: DailyMillionaire, SM: ShoutMeATrillion
    private String _payload;
    private Date _createDate;

    public SyncMessage(){}

    public SyncMessage(long subscriberId, String contextualId, String messageType, String engineKey, String payload)
    {
        _id = UUID.randomUUID().toString();
        _subscriberId = subscriberId;
        _contextualId = contextualId;
        _messageType = messageType;
        _engineKey = engineKey;
        _payload = payload;
        _createDate = new Date();
    }

    public String getId()
    {
        return _id;
    }
    public void setId(String id)
    {
        _id = id;
    }

    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }

    public String getMessageType()
    {
        return _messageType;
    }
    public void setMessageType(String messageType)
    {
        _messageType = messageType;
    }

    public String getContextualId()
    {
        return _contextualId;
    }
    public void setContextualId(String contextualId)
    {
        _contextualId = contextualId;
    }

    public String getEngineKey()
    {
        return _engineKey;
    }
    public void setEngineKey(String _engineKey)
    {
        this._engineKey = _engineKey;
    }

    public String getPayload()
    {
        return _payload;
    }
    public void setPayload(String payload)
    {
        _payload = payload;
    }

    public Date getCreateDate()
    {
        return _createDate;
    }
    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof SyncMessage)) return false;

        return getId().equals( ((SyncMessage)obj).getId() );
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
                "type: {0}, contextId: {1}, date: {2,date,yyyy-MM-dd hh:mm:ss.SSS}",
                _messageType, _contextualId, _createDate);
    }
}
