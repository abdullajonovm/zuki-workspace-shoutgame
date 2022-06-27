package com.meinc.notification.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.gson.Gson;

public class Notification implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8885179080889619497L;
    private int _id;
    private String _status;
    private String _type;
    private int _contextId;
    private String _actionType;
    private String _actionTaken;
    private String _message;
    private String _description;
    private long _sender;
    private long _recipient;
    private String _bundleIds;
    private String _payload;
    private Map<String, Object> _extras;
    private String _extrasAsString;
    private Date _created;
    private long _lastUpdatedBy;
    private Date _lastUpdated;
    public static final String STATUS_DELETED = "DELETED";
    public static final String STATUS_HANDLED = "HANDLED";
    public static final String STATUS_VIEWED = "VIEWED";
    public static final String STATUS_NEW = "NEW";

    public Notification() {
        _contextId = 0;
        _status = STATUS_NEW;
        _created = new Date();
        _lastUpdated = new Date();
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String status) {
        _status = status;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public int getContextId() {
        return _contextId;
    }

    public void setContextId(int contextId) {
        _contextId = contextId;
    }

    public String getActionType() {
        return _actionType;
    }

    public void setActionType(String actionType) {
        _actionType = actionType;
    }

    public String getActionTaken() {
        return _actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        _actionTaken = actionTaken;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public long getSender() {
        return _sender;
    }

    public void setSender(long sender) {
        _sender = sender;
    }

    public long getRecipient() {
        return _recipient;
    }

    public void setRecipient(long recipient) {
        _recipient = recipient;
    }

    public String getBundleIds() {
		return _bundleIds;
	}

	public void setBundleIds(String bundleIds) {
		_bundleIds = bundleIds;
	}

	public String getPayload() {
        return _payload;
    }

    public void setPayload(String payload) {
        _payload = payload;
    }

    public Map<String, Object> getExtras()
    {
        return _extras;
    }

    public void setExtras(Map<String, Object> extras)
    {
        _extras = extras;
        _extrasAsString = new Gson().toJson(extras);
    }

    @JsonIgnore
    public String getExtrasAsString()
    {
        return _extrasAsString;
    }

    public void setExtrasAsString(String extrasAsString)
    {
        _extrasAsString = extrasAsString;
//        Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
//        _extras = new Gson().fromJson(_extrasAsString, type);

        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
        try {
            _extras = new ObjectMapper().readValue(_extrasAsString, typeRef);
        } catch (Exception e) {
            _extras = new HashMap<String, Object>();
            e.printStackTrace();
        }
    }

    public Date getCreated() {
        return _created;
    }

    public void setCreated(Date created) {
        _created = created;
    }

    public long getLastUpdatedBy() {
        return _lastUpdatedBy;
    }

    public void setLastUpdatedBy(long lastUpdatedBy) {
        _lastUpdatedBy = lastUpdatedBy;
    }

    public Date getLastUpdated() {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        _lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Notification<");
        buf.append("sender=").append(_sender);
        buf.append(" recipient=").append(_recipient);
        buf.append(" type=").append(_actionType);
        buf.append(" message='").append(_message).append("'");
        buf.append(">");
        return buf.toString();
    }

}
