/**
 * 
 */
package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class BlogEntry 
implements Serializable
{
    public static String IMAGE_SOURCE_FACEBOOK = "FACEBOOK";
    public static String IMAGE_SOURCE_TWITTER = "TWITTER";
    private static final long serialVersionUID = -4115611108975830477L;
    
    private int _blogEntryId;
    private String _author;
    private int _subscriberId;
    private int _eventId;
    private int _vipBoxId;
    private String _message;
    private Date _postDate;
    private boolean _approved;
    private String _photoUrl;
    private String _attachedImageUrl;
    private String _attachedImageSource;
    
    public BlogEntry()
    {
    }
    
    public BlogEntry(int eventId, String message)
    {
        _eventId = eventId;
        _message = message;
    }

    public void setBlogEntryId(int blogEntryId)
    {
        _blogEntryId = blogEntryId;
    }

    @JsonProperty(value="itemId")
    public int getBlogEntryId()
    {
        return _blogEntryId;
    }

    public int getEventId()
    {
        return _eventId;
    }

    public void setEventId(int eventId)
    {
        _eventId = eventId;
    }

    public int getVipBoxId() {
        return _vipBoxId;
    }

    public void setVipBoxId(int vipBoxId) {
        _vipBoxId = vipBoxId;
    }

    public String getMessage()
    {
        return _message;
    }

    public void setMessage(String message)
    {
        _message = message;
    }

    public Date getPostDate()
    {
        return _postDate;
    }

    public void setPostDate(Date postDate)
    {
        _postDate = postDate;
    }

    @JsonIgnore
    public boolean isApproved()
    {
        return _approved;
    }

    public void setApproved(boolean approved)
    {
        _approved = approved;
    }

    public String getAuthor() {
        return _author;
    }

    public void setAuthor(String author) {
        _author = author;
    }

    public int getSubscriberId() {
        return _subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        _subscriberId = subscriberId;
    }

    public String getPhotoUrl() {
        return _photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        _photoUrl = photoUrl;
    }

    public String getAttachedImageUrl()
    {
        return _attachedImageUrl;
    }

    public void setAttachedImageUrl(String attachedImageUrl)
    {
        _attachedImageUrl = attachedImageUrl;
    }

    public String getAttachedImageSource()
    {
        return _attachedImageSource;
    }

    public void setAttachedImageSource(String attachedImageSource)
    {
        _attachedImageSource = attachedImageSource;
    }
}
