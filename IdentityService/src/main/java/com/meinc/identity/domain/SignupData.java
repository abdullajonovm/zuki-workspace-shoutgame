package com.meinc.identity.domain;

import java.io.Serializable;
import java.util.Date;

public class SignupData implements Serializable {

    private static final long serialVersionUID = -2668597200352261573L;

    private String appName;
    private String deviceToken;
    private String email;
    private String password;
    private boolean passwordSet;
    private String username;
    private String emailHash;
    private String emailSignature;
    private String fbAccessToken;
    private String facebookId;
    private String facebookAppId;
    private String languageCode;
    private String phone;
    private String fromCountryCode;
    private String region;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private String photoUrlSmall;
    private String photoUrlLarge;
    private Date dateOfBirth;
    private boolean adult;

    public String getAppName() {
        return appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public String getDeviceToken() {
        return deviceToken;
    }
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isPasswordSet()
    {
        return passwordSet;
    }
    public void setPasswordSet(boolean passwordSet)
    {
        this.passwordSet = passwordSet;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmailHash() {
        return emailHash;
    }
    public void setEmailHash(String emailHash) {
        this.emailHash = emailHash;
    }
    public String getEmailSignature() {
        return emailSignature;
    }
    public void setEmailSignature(String emailSignature) {
        this.emailSignature = emailSignature;
    }
    public String getFbAccessToken() {
        return fbAccessToken;
    }
    public void setFbAccessToken(String fbAccessToken) {
        this.fbAccessToken = fbAccessToken;
    }
    public String getFacebookId() {
        return facebookId;
    }
    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }
    public String getFacebookAppId() {
        return facebookAppId;
    }
    public void setFacebookAppId(String facebookAppId) {
        this.facebookAppId = facebookAppId;
    }
    public String getLanguageCode() {
        return languageCode;
    }
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    public String getPhone()
    {
        return phone;
    }
    public void setPhone(String phone)
    {
        this.phone = phone;
    }
    public String getFromCountryCode()
    {
        return fromCountryCode;
    }
    public void setFromCountryCode(String fromCountryCode)
    {
        this.fromCountryCode = fromCountryCode;
    }
    public String getRegion()
    {
        return region;
    }
    public void setRegion(String region)
    {
        this.region = region;
    }
    public String getFirstName()
    {
        return firstName;
    }
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    public String getLastName()
    {
        return lastName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    public String getPhotoUrl()
    {
        return photoUrl;
    }
    public void setPhotoUrl(String value)
    {
        this.photoUrl = value;
    }
    public String getPhotoUrlSmall()
    {
        return photoUrlSmall;
    }
    public void setPhotoUrlSmall(String value)
    {
        this.photoUrlSmall = value;
    }
    public String getPhotoUrlLarge()
    {
        return photoUrlLarge;
    }
    public void setPhotoUrlLarge(String value)
    {
        this.photoUrlLarge = value;
    }
    public Date getDateOfBirth()
    {
        return dateOfBirth;
    }
    public void setDateOfBirth(Date value)
    {
        this.dateOfBirth = value;
    }
    public boolean isAdult()
    {
        return adult;
    }
    public void setAdult(boolean adult)
    {
        this.adult = adult;
    }

}
