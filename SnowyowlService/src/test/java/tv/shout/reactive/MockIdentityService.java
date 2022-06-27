package tv.shout.reactive;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import com.meinc.identity.domain.Subscriber.ROLE;
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
import com.meinc.identity.service.IIdentityService;
import com.meinc.mrsoa.service.ServiceEndpoint;

public class MockIdentityService
implements IIdentityService
{
    private MockIdentityDao _dao;

    public MockIdentityService(MockIdentityDao dao)
    {
        _dao = dao;
    }

    @Override
    public Subscriber getSubscriberById(long subscriberId)
    {
        return _dao.getSubscriberById(subscriberId);
    }

    @Override
    public Subscriber signup(int contextId, SignupData signupData, SubscriberSession session)
            throws InvalidEmailException, EmailAlreadyUsedException, FacebookGeneralException,
            InvalidAccessTokenException, FacebookUserExistsException, InvalidSessionException, NicknameInvalidException,
            FacebookAuthenticationNeededException
    {
        Subscriber s = new Subscriber();
        s.setFirstname(signupData.getFirstName());
        s.setLastname(signupData.getLastName());
        s.setEmail(signupData.getEmail());
        s.setNickname(signupData.getUsername());
        s.setSubscriberId(_dao.getNextAutoIncrementSubscriberId());

        _dao.addSubscriber(s);

        return s;
    }

    @Override
    public List<Long> getSubscriberIdsForRole(ROLE role)
    {
        return _dao.getSubscriberIdsForRole(role);
    }























    @Override
    public void start()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerSignupCallback(ServiceEndpoint endpoint, String methodName)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public HttpResponse doGetFb(HttpRequest request)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpResponse doPostFb(HttpRequest request)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void emailVerified(long subscriberId, String email)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Subscriber signup(int contextId, SignupData signupData, SubscriberSession session, String encryptKey)
            throws InvalidEmailException, EmailAlreadyUsedException, FacebookGeneralException,
            InvalidAccessTokenException, FacebookUserExistsException, InvalidSessionException, NicknameInvalidException,
            FacebookAuthenticationNeededException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber signupForeignHostSubscriber(int contextId, SubscriberSession session, SignupData signupData)
            throws InvalidEmailException, NicknameInvalidException, EmailAlreadyUsedException, InvalidSessionException,
            MissingRequiredParameterException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber authenticate(int contextId, String email, String password, SubscriberSession session)
            throws InvalidEmailPasswordException, SubscriberInactiveException, SubscriberRequiresEulaException,
            SubscriberRequiresUpdateException, InvalidSessionException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber authenticateViaFacebook(int contextId, String accessToken, String facebookAppId,
            SubscriberSession session) throws InvalidEmailPasswordException, SubscriberInactiveException,
            SubscriberRequiresEulaException, SubscriberRequiresUpdateException, FacebookGeneralException,
            InvalidAccessTokenException, InvalidSessionException, FacebookAuthenticationNeededException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber authenticateViaFacebookOrLink(int contextId, String accessToken, String email, String facebookId,
            String facebookAppId, SubscriberSession session) throws InvalidEmailPasswordException,
            SubscriberInactiveException, SubscriberRequiresEulaException, SubscriberRequiresUpdateException,
            FacebookGeneralException, InvalidAccessTokenException, InvalidSessionException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String authenticateForToolUse(int contextId, String email, String password)
            throws InvalidEmailPasswordException, InvalidSessionException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber deviceCheckin(SubscriberSession session) throws DeviceNotFoundException, InvalidSessionException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void logout(long subscriberId, String deviceId) throws InvalidSessionException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateFacebookPhotos(long subscriberId) throws NotAuthorizedException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<FacebookIdentityInfo> getFacebookIntegrationInfo(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacebookIdentityInfo getFacebookIntegrationInfoForFacebookApp(long subscriberId, String facebookAppId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SubscriberProfile> getProfileInfoForFacebookUsers(List<String> facebookIds, int contextId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getSubscriberIdFromFacebookId(String facebookId, int contextId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Subscriber> getSubscribersByFacebookId(String facebookId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FbSubscriber> getFacebookSubscribers(long subscriberId) throws FacebookAuthenticationNeededException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber getSubscriberByPrimaryIdHash(String primaryIdHash)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Subscriber> getSubscribers(List<Long> subscriberIds)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Subscriber> getSubscribersByPhones(List<String> phones)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber getSubscriberByNickname(int contextId, String nickname)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Subscriber> getSubscribersByEmail(String email)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber getSubscriberByEmail(int contextId, String email)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber getSubscriberAndUpdatedSessionByEmail(int contextId, String email, SubscriberSession session)
            throws SubscriberInactiveException, SubscriberRequiresUpdateException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber getSubscriberAndUpdatedSessionById(long subscriberId, SubscriberSession session)
            throws SubscriberInactiveException, SubscriberRequiresUpdateException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber getSubscriberByPhone(String phone)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SubscriberSession getSubscriberSession(long subscriberId, String deviceId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateSubscriber(Subscriber subscriber) throws InvalidSubscriberUpdateException,
            EmailAlreadyUsedException, InvalidEmailException, NicknameAlreadyUsedException, NicknameInvalidException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSubscriberMintParentId(long subscriberId, long mintParentId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Subscriber> getMintChildren(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSubscriberNickname(int contextId, long subscriberId, String nickname)
            throws NicknameAlreadyUsedException, NicknameInvalidException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSubscriberEmail(int contextId, long subscriberId, String newEmail)
            throws InvalidEmailException, EmailAlreadyUsedException, InvalidSubscriberUpdateException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSubscriberPassword(PASSWORD_SCHEME pwScheme, long subscriberId, String newPassword)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRecruiter(int contextId, long subscriberId, String recruiterNicknamee)
            throws NicknameInvalidException, RecruiterAlreadySetException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int setSubscriberAddress(SubscriberAddress address)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<SubscriberAddress> getSubscriberAddresses(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getSubscriberIdByDeviceSessionKey(String deviceId, String sessionKey, String applicationId,
            String applicationVersion) throws InvalidSessionException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Subscriber getSubscriberByDeviceSessionKey(String deviceId, String sessionKey, String applicationId,
            String applicationVersion) throws InvalidSessionException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> findPartialNicknameMatches(String partialNickname)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean fillNextRing(long subscriberId, long ringSubscriberId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Long> getActiveSubscriberIdsWithDevice()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SubscriberIdAndLanguageCode> getActiveSubscriberIdAndLanguageCodesWithDevice()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getNumberOfActiveDevicesForSubscriber(long subscriberId)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<SubscriberSessionLight> getSubscribersForSessionTokens(List<String> sessionTokens)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getContextIdFromDeviceId(long subscriberId, String deviceId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Subscriber> searchSubs(String searchTerm, int pageIndex, int pageSize)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Subscriber> searchSubsByEmail(int contextId, String partialEmail)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSubscriberLanguageCode(long subscriberId, String languageCode)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearSubscriberCache(long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public long getTotalSubscriberCountAsOfDate(Date date)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<String> getUsernamesInDateRange(Date startDate, Date stopDate)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Long> getAllSubscribersForContext(int contextId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String setFacebookAccessToken(long subscriberId, String accessToken, String facebookAppId)
            throws FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException,
            FacebookAuthenticationNeededException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeFacebookAccessToken(long subscriberId, String facebookAppId)
            throws FacebookLogoutProhibitedException, FacebookAuthTokenNotFoundException, URISyntaxException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void markPhoneAsVerified(long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatePhone(long subscriberId, String phone)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Subscriber> getSubscribersByForeignHostId(String foreignHostSubscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ForeignHostIdentityInfo> getForeignHostIdentityInfoList(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ForeignHostIdentityInfo getForeignHostIdentityInfoForForeignHostApp(long subscriberId,
            String foreignHostAppId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getSubscriberIdFromForeignHostId(String foreignHostSubscriberId, int contextId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteForeignHostMapping(String foreignHostSubscriberId, int contextId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateForeignHostMappingForSubscriber(long subscriberId, String foreignHostSubscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteAllForeignHostMappingsForForeignHostId(String foreignHostSubscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int addIdentityMappingForeignHost(long subscriberId, String foreignHostSubscriberId, String foreignHostAppId,
            int contextId)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<ForeignHostIdentityInfo> getForeignHostSubscriberIds()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addSubscriberSession(SubscriberSession session)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSubscriberEmail(SubscriberEmail subscriberEmail)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void verifySubscriberEmail(long subscriberId, String email)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<SubscriberEmail> getSubscriberEmails(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String generateEncryptKey(int length)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasRole(long subscriberId, Set<String> validRoles)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasRole(long subscriberId, Set<String> validRoles, boolean ignoreSuperuserRole)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addRole(long subscriberId, String role)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeRole(long subscriberId, String role)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getSubscriberRoles(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Long> getSubscriberIdsWithRole(String role)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
