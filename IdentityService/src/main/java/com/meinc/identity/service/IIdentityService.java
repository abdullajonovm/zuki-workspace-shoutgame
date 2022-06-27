package com.meinc.identity.service;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.meinc.facebook.domain.FbSubscriber;
import com.meinc.facebook.exception.FacebookAuthenticationNeededException;
import com.meinc.facebook.exception.FacebookGeneralException;
import com.meinc.facebook.exception.FacebookUserExistsException;
import com.meinc.facebook.exception.InvalidAccessTokenException;
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.http.domain.NotAuthorizedException;
import com.meinc.identity.domain.FacebookIdentityInfo;
import com.meinc.identity.domain.ForeignHostIdentityInfo;
import com.meinc.identity.domain.SignupData;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.SubscriberAddress;
import com.meinc.identity.domain.SubscriberEmail;
import com.meinc.identity.domain.SubscriberIdAndLanguageCode;
import com.meinc.identity.domain.SubscriberProfile;
import com.meinc.identity.domain.SubscriberSession;
import com.meinc.identity.domain.SubscriberSessionLight;
import com.meinc.identity.exception.DeviceNotFoundException;
import com.meinc.identity.exception.EmailAlreadyUsedException;
import com.meinc.identity.exception.FacebookAuthTokenNotFoundException;
import com.meinc.identity.exception.FacebookLogoutProhibitedException;
import com.meinc.identity.exception.InvalidEmailException;
import com.meinc.identity.exception.InvalidEmailPasswordException;
import com.meinc.identity.exception.InvalidSessionException;
import com.meinc.identity.exception.InvalidSubscriberUpdateException;
import com.meinc.identity.exception.MissingRequiredParameterException;
import com.meinc.identity.exception.NicknameAlreadyUsedException;
import com.meinc.identity.exception.NicknameInvalidException;
import com.meinc.identity.exception.RecruiterAlreadySetException;
import com.meinc.identity.exception.SubscriberInactiveException;
import com.meinc.identity.exception.SubscriberRequiresEulaException;
import com.meinc.identity.exception.SubscriberRequiresUpdateException;
import com.meinc.mrsoa.service.ServiceEndpoint;

public interface IIdentityService
{
    public enum PASSWORD_SCHEME {USE_MYSQL_PASSWORD, USE_PASSWORD_AS_IS}

    public static final String NO_NAME = "NYI";
    public static Pattern EMAIL_PATTERN = Pattern.compile("^[\\w\\!#$%&\'\\*\\+\\-/=\\?\\^`{|}~](?:\\.?[\\w\\!#$%&\'\\*\\+\\-/=\\?\\^`{|}~]+)*@(?:[0-9A-Za-z\\-]+\\.)+[A-Za-z]+$", Pattern.CASE_INSENSITIVE);
    public static final int DEFAULT_CONTEXT_ID = 1;

    void start();
    void stop();

    /**
     * Call this to be notified whenenver a new subscriber does a successful signup. The callback method needs to have the signature:
     *      <pre>void [methodname](int subscriberId);</pre>
     */
    void registerSignupCallback(ServiceEndpoint endpoint, String methodName);

    /**
     * Called when a request comes to the /fb endpoint
     * @param request the request
     * @return the response
     */
    HttpResponse doGetFb(HttpRequest request);

    /**
     * Called when a POST request comes to the /fb endpoint
     * @param request
     * @return the reponse
     */
    HttpResponse doPostFb(HttpRequest request);

    /** callback from the postoffice service once an email address has been verified */
    void emailVerified(long subscriberId, String email);

    Subscriber signup(int contextId, SignupData signupData, SubscriberSession session)
    throws InvalidEmailException, EmailAlreadyUsedException, FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException,
    InvalidSessionException, NicknameInvalidException, FacebookAuthenticationNeededException;

    Subscriber signup(int contextId, SignupData signupData, SubscriberSession session, String encryptKey)
    throws InvalidEmailException, EmailAlreadyUsedException, FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException,
    InvalidSessionException, NicknameInvalidException, FacebookAuthenticationNeededException;

    Subscriber signupForeignHostSubscriber(int contextId, SubscriberSession session, SignupData signupData)
    throws InvalidEmailException, NicknameInvalidException, EmailAlreadyUsedException, InvalidSessionException, MissingRequiredParameterException;

    Subscriber authenticate(int contextId, String email, String password, SubscriberSession session)
    throws InvalidEmailPasswordException, SubscriberInactiveException, SubscriberRequiresEulaException, SubscriberRequiresUpdateException, InvalidSessionException;

    Subscriber authenticateViaFacebook(int contextId, String accessToken, String facebookAppId, SubscriberSession session)
    throws InvalidEmailPasswordException, SubscriberInactiveException, SubscriberRequiresEulaException, SubscriberRequiresUpdateException,
        FacebookGeneralException, InvalidAccessTokenException, InvalidSessionException, FacebookAuthenticationNeededException;

    Subscriber authenticateViaFacebookOrLink(int contextId, String accessToken, String email, String facebookId, String facebookAppId, SubscriberSession session)
    throws InvalidEmailPasswordException, SubscriberInactiveException, SubscriberRequiresEulaException, SubscriberRequiresUpdateException,
        FacebookGeneralException, InvalidAccessTokenException, InvalidSessionException;

    String authenticateForToolUse(int contextId, String email, String password)
    throws InvalidEmailPasswordException, InvalidSessionException;

    Subscriber deviceCheckin(SubscriberSession session)
    throws DeviceNotFoundException, InvalidSessionException;

    void logout(long subscriberId, String deviceId) throws InvalidSessionException;

    /* Util method to update server facebook photos */
    void updateFacebookPhotos(long subscriberId)
    throws NotAuthorizedException;

    /**
     * @param subscriberId
     * @return the facebook integration info, or null if not found
     */
    List<FacebookIdentityInfo> getFacebookIntegrationInfo(long subscriberId);

    FacebookIdentityInfo getFacebookIntegrationInfoForFacebookApp(long subscriberId, String facebookAppId);

    List<SubscriberProfile> getProfileInfoForFacebookUsers(List<String> facebookIds, int contextId);

    Long getSubscriberIdFromFacebookId(String facebookId, int contextId);

    List<Subscriber> getSubscribersByFacebookId(String facebookId);

    List<FbSubscriber> getFacebookSubscribers(long subscriberId) throws FacebookAuthenticationNeededException;

    Subscriber getSubscriberById(long subscriberId);

    Subscriber getSubscriberByPrimaryIdHash(String primaryIdHash);

    List<Subscriber> getSubscribers(List<Long> subscriberIds);

    List<Subscriber> getSubscribersByPhones(List<String> phones);

    Subscriber getSubscriberByNickname(int contextId, String nickname);

    List<Subscriber> getSubscribersByEmail(String email);

    Subscriber getSubscriberByEmail(int contextId, String email);

    Subscriber getSubscriberAndUpdatedSessionByEmail(int contextId, String email, SubscriberSession session)
    throws SubscriberInactiveException, SubscriberRequiresUpdateException;

    Subscriber getSubscriberAndUpdatedSessionById(long subscriberId, SubscriberSession session)
    throws SubscriberInactiveException, SubscriberRequiresUpdateException;

    Subscriber getSubscriberByPhone(String phone);

    SubscriberSession getSubscriberSession(long subscriberId, String deviceId);

    void updateSubscriber(Subscriber subscriber)
    throws InvalidSubscriberUpdateException, EmailAlreadyUsedException, InvalidEmailException, NicknameAlreadyUsedException, NicknameInvalidException;

    void setSubscriberMintParentId(long subscriberId, long mintParentId);
    List<Subscriber> getMintChildren(long subscriberId);

    void setSubscriberNickname(int contextId, long subscriberId, String nickname)
    throws NicknameAlreadyUsedException, NicknameInvalidException;

    void setSubscriberEmail(int contextId, long subscriberId, String newEmail)
    throws InvalidEmailException, EmailAlreadyUsedException, InvalidSubscriberUpdateException;

    /**
     * @param pwScheme how the password is coming in (clear text, already scrypted)
     * @param subscriberId
     * @param newPassword
     */
    void setSubscriberPassword(PASSWORD_SCHEME pwScheme, long subscriberId, String newPassword);

    void setRecruiter(int contextId, long subscriberId, String recruiterNicknamee)
    throws NicknameInvalidException, RecruiterAlreadySetException;

    int setSubscriberAddress(SubscriberAddress address);
    List<SubscriberAddress> getSubscriberAddresses(long subscriberId);

    long getSubscriberIdByDeviceSessionKey(String deviceId, String sessionKey, String applicationId, String applicationVersion)
    throws InvalidSessionException;

    Subscriber getSubscriberByDeviceSessionKey(String deviceId, String sessionKey, String applicationId, String applicationVersion)
    throws InvalidSessionException;

//    boolean isNicknameUnique(String nickname);
    /**
     * @param partialNickname
     * @return a list of nicknames that partially match the given input. It will perform a case-insensitive LIKE operation with this form:
     * LIKE '<val>%'. If there are no matches, the list will be null.
     */
    List<String> findPartialNicknameMatches(String partialNickname);

    /**
     * Retrieves the MintUser for subscriberId (mintUser), and for ringSubscriberId (ringParent), and fills the next
     * available ring of the mintUser with ringParent.id
     * @param subscriberId the id of the subscriber whose ring we want to fill
     * @param ringSubscriberId the subscriberId of the MintUser who will fill the ring
     * @return true if a ring is filled, false if all rings are already filled
     * @throws InvalidSubscriberUpdateException
     * @throws EmailAlreadyUsedException
     * @throws InvalidEmailException
     */
    boolean fillNextRing(long subscriberId, long ringSubscriberId);

    /**
     * @return a list of subscriber ids for all active subscriber who are playing via device (i.e. not web)
     */
    List<Long> getActiveSubscriberIdsWithDevice();

    List<SubscriberIdAndLanguageCode> getActiveSubscriberIdAndLanguageCodesWithDevice();

    int getNumberOfActiveDevicesForSubscriber(long subscriberId);

    List<SubscriberSessionLight> getSubscribersForSessionTokens(List<String> sessionTokens);

    Integer getContextIdFromDeviceId(long subscriberId, String deviceId);

    List<Subscriber> searchSubs(String searchTerm, int pageIndex, int pageSize);
    List<Subscriber> searchSubsByEmail(int contextId, String partialEmail);

    void setSubscriberLanguageCode(long subscriberId, String languageCode);

    void clearSubscriberCache(long subscriberId);

    long getTotalSubscriberCountAsOfDate(Date date);
    List<String> getUsernamesInDateRange(Date startDate, Date stopDate);

    List<Long> getAllSubscribersForContext(int contextId);

    List<Long> getSubscriberIdsForRole(Subscriber.ROLE role);

    String setFacebookAccessToken(final long subscriberId, String accessToken, String facebookAppId)
    throws FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException, FacebookAuthenticationNeededException;
    void removeFacebookAccessToken(final long subscriberId, String facebookAppId)
    throws FacebookLogoutProhibitedException, FacebookAuthTokenNotFoundException, URISyntaxException;

    void markPhoneAsVerified(long subscriberId);
    void updatePhone(long subscriberId, String phone);

    /// FOREIGN HOST SUBSCRIBER support ///
    List<Subscriber> getSubscribersByForeignHostId(String foreignHostSubscriberId);
    List<ForeignHostIdentityInfo> getForeignHostIdentityInfoList(long subscriberId);
    ForeignHostIdentityInfo getForeignHostIdentityInfoForForeignHostApp(long subscriberId, String foreignHostAppId);
    Long getSubscriberIdFromForeignHostId(String foreignHostSubscriberId, int contextId);
    void deleteForeignHostMapping(String foreignHostSubscriberId, int contextId);
    void updateForeignHostMappingForSubscriber(long subscriberId, String foreignHostSubscriberId);
    void deleteAllForeignHostMappingsForForeignHostId(String foreignHostSubscriberId);
    int addIdentityMappingForeignHost(long subscriberId, String foreignHostSubscriberId, String foreignHostAppId, int contextId);
    List<ForeignHostIdentityInfo> getForeignHostSubscriberIds();
    void addSubscriberSession(SubscriberSession session);

    void addSubscriberEmail(SubscriberEmail subscriberEmail);
    void verifySubscriberEmail(long subscriberId, String email);
    List<SubscriberEmail> getSubscriberEmails(long subscriberId);

    String generateEncryptKey(int length);
    boolean hasRole(long subscriberId, Set<String> validRoles);
    boolean hasRole(long subscriberId, Set<String> validRoles, boolean ignoreSuperuserRole);
    void addRole(long subscriberId, String role);
    void removeRole(long subscriberId, String role);
    List<String> getSubscriberRoles(long subscriberId);
    List<Long> getSubscriberIdsWithRole(String role);
}
