package com.meinc.notification.domain;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class NotificationPref implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3031715512260758442L;
    private int _prefType;
    private long _subscriberId;
    private String _name;
    private String _description;
    private String _value;
    private String _possibleValuesString;
    private String[] _possibleValues;
    private Date _created;
    private Date _lastUpdated;

    public NotificationPref() {
        _created = new Date();
        _lastUpdated = new Date();
        _prefType = 0;
        _subscriberId = 0;
    }

    @JsonProperty(value="prefId")
    public int getPrefType() {
        return _prefType;
    }

    @JsonProperty(value="prefId")
    public void setPrefType(int prefType) {
        _prefType = prefType;
    }

    public long getSubscriberId() {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId) {
        _subscriberId = subscriberId;
    }

    /**
     * Returns the name of the pref.  Ignored when setting new prefs.
     * @return
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the name of this pref - ignored when setting new prefs
     * @param name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Returns the decription of the pref.  Ignored when setting new prefs.
     * @return
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Sets the description of the pref. Ignored when setting new prefs.
     * @param description
     */
    public void setDescription(String description) {
        _description = description;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    @JsonIgnore
    public String getPossibleValuesString() {
        return _possibleValuesString;
    }

    @JsonIgnore
    public void setPossibleValuesString(String possibleValuesString) {
        _possibleValuesString = possibleValuesString;
    }

    public String[] getPossibleValues() {
        return _possibleValues;
    }

    public void setPossibleValues(String[] possibleValues) {
        _possibleValues = possibleValues;
    }

    public Date getCreated() {
        return _created;
    }

    public void setCreated(Date created) {
        _created = created;
    }

    public Date getLastUpdated() {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        _lastUpdated = lastUpdated;
    }

}
