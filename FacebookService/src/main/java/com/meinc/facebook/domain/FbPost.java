package com.meinc.facebook.domain;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FbPost
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _message;
    private String _link;
    private String _name;
    private String _caption;
    private String _description;
    private String _actionName;
    private String _actionLink;
    private String _picture;
    private Map<String, File> _attachments;
    
    public String getMessage() {
        return _message;
    }
    
    public void setMessage(String message) {
        _message = message;
    }
    
    public String getLink() {
        return _link;
    }
    
    public void setLink(String link) {
        _link = link;
    }
    
    public String getName() {
        return _name;
    }
    
    public void setName(String name) {
        _name = name;
    }
    
    public String getCaption() {
        return _caption;
    }
    
    public void setCaption(String caption) {
        _caption = caption;
    }
    
    public String getDescription() {
        return _description;
    }
    
    public void setDescription(String description) {
        _description = description;
    }
    
    public String getActionName() {
        return _actionName;
    }
    
    public void setActionName(String actionName) {
        _actionName = actionName;
    }
    
    public String getActionLink() {
        return _actionLink;
    }
    
    public void setActionLink(String actionLink) {
        _actionLink = actionLink;
    }

    public String getPicture() {
        return _picture;
    }

    public void setPicture(String iconUrl) {
        _picture = iconUrl;
    }
    
    public void addAttachment(String name, File file) {
        if (_attachments == null) {
            _attachments = new HashMap<String, File>();
        }
        _attachments.put(name, file);
    }

    public Map<String, File> getAttachments() {
        return _attachments;
    }

    public void setAttachments(Map<String, File> attachments) {
        _attachments = attachments;
    }
    


}
