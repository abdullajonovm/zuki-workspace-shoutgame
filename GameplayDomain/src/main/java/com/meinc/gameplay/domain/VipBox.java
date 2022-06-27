package com.meinc.gameplay.domain;

import java.util.List;

import com.meinc.deal.domain.Sponsor;

/**
 * Represents a VIP Box, containing a custom leaderboard and shout out view.
 * @author bxgrant
 */
public class VipBox extends BaseDomainObject
{
    private static final long serialVersionUID = 5941698780115738998L;

    /** Defines the statuses a vip box may ave. */
    public static enum VipBoxStatus
    {
        /** Active VIP Boxes show up in client lists. */
        ACTIVE,
        /** A suspended VIP Box doesn't show up in client lists.  */
        INACTIVE,
        /** We're waiting to add this VIP Box, shouldn't show up in client lists. */
        PENDING_ADDITION,
        /** We're waiting to remove this VIP Box, shouldn't show up in client lists. */
        PENDING_REMOVAL,
        /** This VIP Box has been deleted. */
        DELETED
    };

    public static enum VipBoxType
    {
        /** A personal vipbox of a normal subscriber. */
        PERSONAL,
        /** The Celebrity VIP Box of a celebrity (should only be 1 per celeb).  */
        CELEBRITY,
        /** The VIP Box of a sponsor. */
        SPONSOR,
        /** A 1 on 1 vip box (part of a champions challenge, should only ever be 2 members in the box) */
        ONE_ON_ONE,
        /** A champions challenge round (will be 1 per event per round of a cc) */
        CC_LEVEL
    };

    /** Unique ID of the VIP Box */
    private int _vipBoxId;

    /** Name of VIP Box. */
    private String _name;

    /** Type of VIP Box */
    private VipBoxType _type;

    /** ownerName of VIP Box */
    private String _ownerName;

    /** an optional invite code to associate with the vip box */
    private String _inviteCode;

    /** The URL to the image representing the vip box. */
    private String _avatarUrl;

    /** The subscriber who created this vip box. */
    private int _ownerId;

    /** The payout rule engine to use.  If null, uses default. */
    private String _payoutRuleEngineName;

    /** The deal engine to use.  If null, uses default. */
    private String _dealEngineName;

    /** Whether this is the default vip box for the owning subscriber. Defaults to false.*/
    private boolean _isDefault = false;

    /** The status of the vip box.  @see VipBoxStatus */
    private VipBoxStatus _status;

    /**
     * Anyone may join this vip box by simply making a request to join it
     * (say I'm at a sports bar and I do a location-based checkin, find the vip box, and
     *  then say join.)
     */
    private boolean _openMembership = false;

    /** If must checkin at location to play either using mobile app or text (@see EventVipBox.textCheckinCode) */
    private boolean _locationCheckinRequired = false;

    /**
     * By default autoCreate is true, meaning that as soon as a single member of the vip box
     * answers a single question of an event, then the vip box is created and may be returned.
     *
     * False means if an association between a vip box and an event isn't expressly made then
     * whether or not any subscribers play the event or not, the vip box isn't created
     * and doesn't exist and if asked for returns the next event the vip box is associated
     * with so we can tell them when that vip box is in action next.
     */
    private boolean _autoCreate = true;

    /**
     * The members of the VIP Box, returned in a pageable list in case there are too many
     * to return at once.
     */
    private PageableList<VipBoxMember> _members;

    /**
     * Optional organization (which may have a location) associated with this leaderboard.
     * If this organization has child organizations, then the leaderboard applies
     * to the locations of all child organizations.  So, if I want to have a leaderboard
     * at all McDonalds' across the country, I would associate this leaderboard with the
     * parent McD's corporate org.
     */
    private Org _organization;

    /**
     * Optional location associated with this leaderboard (say I'm having a party).
     * If this and _organization is set, will favor organization's location.
     */
    private Location _location;

    /** If present, it's the vendor to use to skin the app with when players join the vipbox. */
    private Integer _vendorId;

    /** The sponsors of the leaderboard, if any. */
    private List<Sponsor> _sponsors;

    private int _numMembers;

    public VipBox()
    {
    }

    public int getVipBoxId()
    {
        return _vipBoxId;
    }

    public void setVipBoxId(int vipBoxId)
    {
        _vipBoxId = vipBoxId;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public VipBoxType getType()
    {
        return _type;
    }

    public void setType(VipBoxType type)
    {
        _type = type;
    }

    public String getOwnerName() {
        return _ownerName;
    }

    public void setOwnerName(String ownerName) {
        _ownerName = ownerName;
    }

    public String getAvatarUrl()
    {
        return _avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl)
    {
        _avatarUrl = avatarUrl;
    }

    public int getOwnerId()
    {
        return _ownerId;
    }

    public void setOwnerId(int ownerId)
    {
        _ownerId = ownerId;
    }

    public String getPayoutRuleEngineName()
    {
        return _payoutRuleEngineName;
    }

    public void setPayoutRuleEngineName(String payoutRuleEngineName)
    {
        _payoutRuleEngineName = payoutRuleEngineName;
    }

    public String getDealEngineName()
    {
        return _dealEngineName;
    }

    public void setDealEngineName(String dealEngineName)
    {
        _dealEngineName = dealEngineName;
    }

    public boolean isDefault()
    {
        return _isDefault;
    }

    public void setDefault(boolean isDefault)
    {
        _isDefault = isDefault;
    }

    public VipBoxStatus getStatus()
    {
        return _status;
    }

    public void setStatus(VipBoxStatus status)
    {
        _status = status;
    }

    public boolean isOpenMembership()
    {
        return _openMembership;
    }

    public void setOpenMembership(boolean openMembership)
    {
        _openMembership = openMembership;
    }

    public boolean isLocationCheckinRequired()
    {
        return _locationCheckinRequired;
    }

    public void setLocationCheckinRequired(boolean locationCheckinRequired)
    {
        _locationCheckinRequired = locationCheckinRequired;
    }

    public boolean isAutoCreate()
    {
        return _autoCreate;
    }

    public void setAutoCreate(boolean autoCreate)
    {
        _autoCreate = autoCreate;
    }

    public PageableList<VipBoxMember> getMembers()
    {
        return _members;
    }

    public void setMembers(PageableList<VipBoxMember> members)
    {
        _members = members;
    }

    public Org getOrganization()
    {
        return _organization;
    }

    public void setOrganization(Org organization)
    {
        _organization = organization;
    }

    public Location getLocation()
    {
        return _location;
    }

    public void setLocation(Location location)
    {
        _location = location;
    }

    public Integer getVendorId()
    {
      return _vendorId;
    }

    public void setVendorId(Integer vendorId)
    {
      _vendorId = vendorId;
    }

    public List<Sponsor> getSponsors()
    {
        return _sponsors;
    }

    public void setSponsors(List<Sponsor> sponsors)
    {
        _sponsors = sponsors;
    }

    public int getNumMembers() {
        return _numMembers;
    }

    public void setNumMembers(int numMembers) {
        _numMembers = numMembers;
    }

    public String getInviteCode()
    {
        return _inviteCode;
    }

    public void setInviteCode(String inviteCode)
    {
        _inviteCode = inviteCode;
    }

}
