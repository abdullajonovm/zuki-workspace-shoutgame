package com.meinc.identity.domain;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Subscriber
implements Serializable
{
//    private static final Logger logger = Logger.getLogger(Subscriber.class);

    private static final long serialVersionUID = 1L;
    public static final int classId = 3;

    public static enum PRIMARY_ID_SCHEME {
        FACEBOOK ("facebook"),
        EMAIL    ("email");

        private String scheme;
        private PRIMARY_ID_SCHEME(String scheme) {
            this.scheme = scheme;
        }
        public String getScheme() {
            return scheme;
        }
    }

    public static enum ROLE {
        USER, ADMIN, SHOUTCASTER, CELEBRITY, TESTER
    }

    public static enum ADMIN_ROLE {
        NONE, NORMAL, SUPER
    }

    @JsonProperty(value="subscriberId")
    private long _subscriberId;

    private int _contextId;

    @JsonProperty(value="firstName")
    private String _firstname;

    @JsonProperty(value="lastName")
    private String _lastname;

    @JsonProperty(value="username")
    private String _nickname;

    @JsonProperty(value="userSetUsername")
    private boolean _nicknameSet;

    private boolean _facebookUserFlag;

    @JsonProperty(value="photoUrl")
    private String _photoUrl;

    @JsonProperty(value="photoUrlSmall")
    private String _photoUrlSmall;

    @JsonProperty(value="photoUrlLarge")
    private String _photoUrlLarge;

    @JsonProperty(value="role")
    private ROLE _role;

    @JsonProperty(value="adminRole")
    private ADMIN_ROLE _adminRole;

    @JsonProperty(value="email")
    private String _email;

    private String _passwd;

    private boolean _passwdSet;

    private boolean _changePassword;

    @JsonProperty("encryptKey")
    private String _encryptKey;

    @JsonProperty("primaryId")
    private String _primaryId;

    private String _emailSha256Hash;

    private String _emailHashPrefix;

    //@JsonProperty(value="cellphone")
    private String _phone;

    private boolean _phoneVerified;

    //@JsonProperty("emailVerified")
    private boolean _emailVerified;

    private boolean _activeFlag;

    private boolean _eulaFlag;

    private boolean _adultFlag;

    private String _languageCode;

    private String _currencyCode;

    private String _fromCountryCode;

    private String _shipCountryCode;

    private String _region;

    private Long _mintParentSubscriberId;

    private Long _ring1SubscriberId;

    private Long _ring2SubscriberId;

    private Long _ring3SubscriberId;

    private Long _ring4SubscriberId;

    private Date _dateOfBirth;

    private Date _createDate;

    private Date _updateDate;

    private List<SubscriberAddress> _addresses;

    private SubscriberSession _subscriberSession;

    //legacy json value
    @JsonProperty(value="provisional")
    private boolean _provisional = false;

    //legacy json value
    @JsonProperty(value="mintRegistrationUrl")
    private String _mintRegistrationUrl = null;

    //legacy json value
    @JsonProperty(value="playMethod")
    private int _playMethod = 2;

    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public int getContextId() {
		return _contextId;
	}
	public void setContextId(int contextId) {
		_contextId = contextId;
	}
	public String getFirstname()
    {
        return _firstname;
    }
    public void setFirstname(String firstname)
    {
        _firstname = firstname;
    }
    public String getLastname()
    {
        return _lastname;
    }
    public void setLastname(String lastname)
    {
        _lastname = lastname;
    }
    public String getNickname()
    {
        return _nickname;
    }
    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }
    public boolean isNicknameSet()
    {
        return _nicknameSet;
    }
    public void setNicknameSet(boolean nicknameSet)
    {
        _nicknameSet = nicknameSet;
    }
    @JsonIgnore
    public boolean isFacebookUserFlag()
    {
        return _facebookUserFlag;
    }
    public void setFacebookUserFlag(boolean facebookUserFlag)
    {
        _facebookUserFlag = facebookUserFlag;
    }
    public String getPhotoUrl()
    {
        return _photoUrl;
    }
    public void setPhotoUrl(String photoUrl)
    {
        _photoUrl = photoUrl;
    }
    public String getPhotoUrlSmall()
    {
        return _photoUrlSmall;
    }
    public void setPhotoUrlSmall(String photoUrlSmall)
    {
        _photoUrlSmall = photoUrlSmall;
    }
    public String getPhotoUrlLarge()
    {
        return _photoUrlLarge;
    }
    public void setPhotoUrlLarge(String photoUrlLarge)
    {
        _photoUrlLarge = photoUrlLarge;
    }
    public ROLE getRole()
    {
        return _role;
    }
    public void setRole(ROLE role)
    {
        _role = role;
    }
    public ADMIN_ROLE getAdminRole()
    {
        return _adminRole;
    }
    public void setAdminRole(ADMIN_ROLE adminRole)
    {
        _adminRole = adminRole;
    }
    public String getEmail()
    {
        return _email;
    }
    public void setEmail(String email)
    {
        _email = email;
    }
    @JsonIgnore
    public String getPasswd()
    {
        return _passwd;
    }
    public void setPasswd(String passwd)
    {
        _passwd = passwd;
    }
    public boolean isPasswdSet()
    {
        return _passwdSet;
    }
    public void setPasswdSet(boolean passwdSet)
    {
        _passwdSet = passwdSet;
    }
    @JsonIgnore
    public boolean isChangePassword()
    {
        return _changePassword;
    }
    public void setChangePassword(boolean changePassword)
    {
        _changePassword = changePassword;
    }
    public String getEncryptKey() {
        return _encryptKey;
    }
    public void setEncryptKey(String encryptKey) {
        _encryptKey = encryptKey;
    }
    public String getPrimaryId() {
        return _primaryId;
    }
    public void setPrimaryId(String primaryId) {
        _primaryId = primaryId;
    }
    public void setPrimaryId(PRIMARY_ID_SCHEME scheme, String authority, String id) throws URISyntaxException {
        _primaryId = new URI(scheme.getScheme(), authority, "/" + id, null, null).toString();
    }
    public String getEmailSha256Hash() {
        return _emailSha256Hash;
    }
    public void setEmailSha256Hash(String emailSha256Hash) {
        _emailSha256Hash = emailSha256Hash;
    }
    public String getEmailHashPrefix() {
        return _emailHashPrefix;
    }
    public void setEmailHashPrefix(String emailHashPrefix) {
        _emailHashPrefix = emailHashPrefix;
    }
    public String getPhone()
    {
        return _phone;
    }
    public void setPhone(String phone)
    {
        _phone = phone;
    }
    @JsonIgnore
    public boolean isPhoneVerified()
    {
        return _phoneVerified;
    }
    public void setPhoneVerified(boolean phoneVerified)
    {
        _phoneVerified = phoneVerified;
    }
    public boolean isEmailVerified() {
        return _emailVerified;
    }
    public void setEmailVerified(boolean emailVerified) {
        _emailVerified = emailVerified;
    }
    @JsonIgnore
    public boolean isActiveFlag()
    {
        return _activeFlag;
    }
    public void setActiveFlag(boolean activeFlag)
    {
        _activeFlag = activeFlag;
    }
    @JsonIgnore
    public boolean isEulaFlag()
    {
        return _eulaFlag;
    }
    public void setEulaFlag(boolean eulaFlag)
    {
        _eulaFlag = eulaFlag;
    }
    public boolean isAdultFlag()
    {
        return _adultFlag;
    }
    public void setAdultFlag(boolean adultFlag)
    {
        _adultFlag = adultFlag;
    }
    public String getLanguageCode()
    {
        return _languageCode;
    }
    public void setLanguageCode(String languageCode)
    {
        _languageCode = languageCode;
    }
    public String getCurrencyCode()
    {
        return _currencyCode;
    }
    public void setCurrencyCode(String currencyCode)
    {
        _currencyCode = currencyCode;
    }
    public String getFromCountryCode()
    {
        return _fromCountryCode;
    }
    public void setFromCountryCode(String fromCountryCode)
    {
        _fromCountryCode = fromCountryCode;
    }
    public String getShipCountryCode()
    {
        return _shipCountryCode;
    }
    public void setShipCountryCode(String shipCountryCode)
    {
        _shipCountryCode = shipCountryCode;
    }
    public String getRegion()
    {
        return _region;
    }
    public void setRegion(String region)
    {
        _region = region;
    }
    @JsonIgnore
    public Long getMintParentSubscriberId()
    {
        return _mintParentSubscriberId;
    }
    public void setMintParentSubscriberId(Long mintParentSubscriberId)
    {
        _mintParentSubscriberId = mintParentSubscriberId;
    }
    @JsonIgnore
    public Long getRing1SubscriberId()
    {
        return _ring1SubscriberId;
    }
    public void setRing1SubscriberId(Long ring1SubscriberId)
    {
        _ring1SubscriberId = ring1SubscriberId;
    }
    @JsonIgnore
    public Long getRing2SubscriberId()
    {
        return _ring2SubscriberId;
    }
    public void setRing2SubscriberId(Long ring2SubscriberId)
    {
        _ring2SubscriberId = ring2SubscriberId;
    }
    @JsonIgnore
    public Long getRing3SubscriberId()
    {
        return _ring3SubscriberId;
    }
    public void setRing3SubscriberId(Long ring3SubscriberId)
    {
        _ring3SubscriberId = ring3SubscriberId;
    }
    @JsonIgnore
    public Long getRing4SubscriberId()
    {
        return _ring4SubscriberId;
    }
    public void setRing4SubscriberId(Long ring4SubscriberId)
    {
        _ring4SubscriberId = ring4SubscriberId;
    }
    @JsonIgnore
    public Date getCreateDate()
    {
        return _createDate;
    }
    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }
    @JsonIgnore
    public Date getUpdateDate()
    {
        return _updateDate;
    }
    public void setUpdateDate(Date updateDate)
    {
        _updateDate = updateDate;
    }
    public Date getDateOfBirth()
    {
        return _dateOfBirth;
    }
    public void setDateOfBirth(Date value)
    {
        _dateOfBirth = value;
    }
    @JsonIgnore
    public List<SubscriberAddress> getAddresses()
    {
        return _addresses;
    }
    public void setAddresses(List<SubscriberAddress> addresses)
    {
        _addresses = addresses;
    }
    @JsonIgnore
    public SubscriberSession getSubscriberSession()
    {
        return _subscriberSession;
    }
    public void setSubscriberSession(SubscriberSession subscriberSession)
    {
        _subscriberSession = subscriberSession;
    }

    @JsonIgnore
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("sId: ").append(_subscriberId);
        buf.append(", fn: ").append(_firstname);
        buf.append(", ln: ").append(_lastname);
        buf.append(", email: ").append(_email);
        buf.append(", primaryId: ").append(_primaryId);
        buf.append(", nick: ").append(_nickname).append(" (set: ").append(_nicknameSet).append(")");
        buf.append(", fb: ").append(_facebookUserFlag).append(" (photo: ").append(_photoUrl).append(")");
        buf.append(", photoUrlSmall: ").append(_photoUrlSmall);
        buf.append(", photoUrlLarge: ").append(_photoUrlLarge);
        buf.append(", role: ").append(_role);
        buf.append(", adminRole: ").append(_adminRole);
        buf.append(", pw: ").append("**** (isSet: ").append(_passwdSet).append(", change: ").append(_changePassword).append(")");
        buf.append(", phone: ").append(_phone).append(" (verified: ").append(_phoneVerified).append(")");
        buf.append(", active: ").append(_activeFlag);
        buf.append(", eula: ").append(_eulaFlag);
        buf.append(", adult: ").append(_adultFlag);
        buf.append(", language: ").append(_languageCode);
        buf.append(", currency: ").append(_currencyCode);
        buf.append(", fromCountryCode:" ).append(_fromCountryCode);
        buf.append(", shipCountryCode: ").append(_shipCountryCode);
        buf.append(", region: ").append(_region);
        buf.append(", mint: ").append(_mintParentSubscriberId);
        buf.append(", ring1: ").append(_ring1SubscriberId);
        buf.append(", ring2: ").append(_ring2SubscriberId);
        buf.append(", ring3: ").append(_ring3SubscriberId);
        buf.append(", ring4: ").append(_ring4SubscriberId);
        buf.append(", create: ").append(_createDate);
        buf.append(", update: ").append(_updateDate);

        return buf.toString();
    }
}
