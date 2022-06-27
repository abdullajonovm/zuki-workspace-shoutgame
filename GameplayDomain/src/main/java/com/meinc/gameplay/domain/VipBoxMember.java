package com.meinc.gameplay.domain;

/**
 * A vip box member is a specialized version of a subscriber.  One could argue that we should extend
 * subscriber to achieve this "vip box member" concept.
 * 
 * @author bxgrant
 */
public class VipBoxMember extends BaseDomainObject
{
    private static final long serialVersionUID = -7935663443907304000L;
    
    /** Defines the roles a member of a vip box may have */
    public static enum MemberRole
    {
        /** An owner of the vip box, can do anything with it. Is also considered a member. */
        OWNER,
        /** Can add/remove members and invite others to join. Is also considered a member. */
        ADMIN,
        /** A member of the vip box. */
        MEMBER,
        /** Someone who may view a vip box but doesn't show up within it.*/
        VIEWER,
        /** A celebrity who owns the VIP Box, but doesn't show up within it */
        CELEBRITY
    };

    /** Defines the statuses a member of the vip box may have. */
    public static enum MemberStatus
    {
        /** Active member of the vip box, shows up in container lists. */
        ACTIVE,
        /** A suspended member of the vip box, doesn't show up in container lists.  */
        INACTIVE,
        /** We're waiting to add this member to the vip box. */
        PENDING_ADDITION,
        /** We're waiting to remove this member from the vip box. */
        PENDING_REMOVAL,
        /** We're waiting for the member to approve joining this vip box. */
        PENDING_APPROVAL
    };

    private int _vipboxMemberId; //auto inc id

    /** Id of subscriber who is the member. */
    private int _subscriberId;

    /** Nickname of the subscriber who is the member. */
    private String _nickname;

    /** The URL to the image representing the member. */
    private String _avatarUrl;
    
    /** The members Twitter handle */
    private String _twitterHandle;

    /** Whether to show this member publicly inside the vip box (some admin's may not want to show up). */
    private boolean _hiddenMember;
    
    /** The role of the member in the vip box.  @see MemberRole */
    private MemberRole _role;

    /** The status of the member in the vip box.  @see MemberStatus */
    private MemberStatus _status;
    
    private String _emailHash;
    private String _countryCode;

    public VipBoxMember()
    {
    }

    public int getSubscriberId()
    {
        return _subscriberId;
    }
    
    public void setSubscriberId(int subscriberId)
    {
        _subscriberId = subscriberId;
    }
    
    public String getNickname()
    {
        return _nickname;
    }
    
    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }
    
    public String getAvatarUrl()
    {
        return _avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl)
    {
        this._avatarUrl = avatarUrl;
    }
    
    public String getTwitterHandle() {
        return _twitterHandle;
    }

    public void setTwitterHandle(String twitterHandle) {
        _twitterHandle = twitterHandle;
    }

    public boolean isHiddenMember()
    {
        return _hiddenMember;
    }
    
    public void setHiddenMember(boolean hiddenMember)
    {
        _hiddenMember = hiddenMember;
    }
    
    public MemberRole getRole()
    {
        return _role;
    }
    
    public void setRole(MemberRole role)
    {
        _role = role;
    }
    
    public MemberStatus getStatus()
    {
        return _status;
    }
    
    public void setStatus(MemberStatus status)
    {
        _status = status;
    }

    public void setVipboxMemberId(int vipboxMemberId)
    {
        _vipboxMemberId = vipboxMemberId;
    }

    public int getVipboxMemberId()
    {
        return _vipboxMemberId;
    }
    
    public String getEmailHash()
    {
        return _emailHash;
    }

    public void setEmailHash(String emailHash)
    {
        _emailHash = emailHash;
    }

    public String getCountryCode()
    {
        return _countryCode;
    }

    public void setCountryCode(String countryCode)
    {
        _countryCode = countryCode;
    }

    @Override
    public String toString() {
        return String.format("vmId=%d subId=%d nick=%s country=%s emailHash=%s", _vipboxMemberId, _subscriberId, _nickname, _countryCode, _emailHash);
    }
}
