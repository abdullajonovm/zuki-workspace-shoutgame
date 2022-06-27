package tv.shout.snowyowl.collector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.meinc.identity.domain.Subscriber;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;

import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.util.FastMap;

public class AffiliateHandler
extends BaseSmMessageHandler
{
//    private static Logger _logger = Logger.getLogger(SubscriberHandler.class);

    private static final List<String> _validFormVars = Arrays.asList(
    );


    @Override
    public String getHandlerMessageType()
    {
        return "AFFILIATE_HANDLER";
    }

    @Override
    protected List<String> getValidFormVars()
    {
        return _validFormVars;
    }

    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        CollectorEndpointHandler[] handlers = new CollectorEndpointHandler[] {

                new CollectorEndpointHandler(new CollectorEndpoint("/snowl/subscriber/referralInfo", ConnectionType.ANY))
                    .withValidRoles(new HashSet<>(Arrays.asList()))
                    .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        referralInfo(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/referralInfo", ConnectionType.ANY))
                    .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                    .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        allReferralInfo(message.getProperties(), message.getMessageId())),
        };

        for (CollectorEndpointHandler handler : handlers) {
            _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
        }

        return Arrays.stream(handlers)
                .map(CollectorEndpointHandler::getCollectorEndpoint)
                .toArray(CollectorEndpoint[]::new);
    }

    private Map<String, Object> referralInfo(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "referralInfo";

        Subscriber subscriber = getSubscriber(props, messageId, docType);
        boolean isAffiliate = _identityService.hasRole(subscriber.getSubscriberId(), new HashSet<>(Arrays.asList(BaseSmMessageHandler.AFFILIATE)), true);
        List<ReferralReferredSubscriberStruct> referredSubscribersList = new ArrayList<>();
        List<ReferralTransactionsStruct> referralTransactionsList = new ArrayList<>();

        populateReferralInfoForSubscriber(subscriber, isAffiliate, referredSubscribersList, referralTransactionsList, new ArrayList<>());

        return new FastMap<>(
            "referredSubscribers", referredSubscribersList,
            "referralTransactions", referralTransactionsList
        );
    }

    private Map<String, Object> allReferralInfo(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        //String docType = "allReferralInfo";

        List<ReferralReferredSubscriberStruct> referredSubscribersList = new ArrayList<>();
        List<ReferralTransactionsStruct> referralTransactionsList = new ArrayList<>();
        List<String> affiliateNicknameList = new ArrayList<>();

        List<Long> allAffiliateSubscriberIds = _identityService.getSubscriberIdsWithRole(BaseSmMessageHandler.AFFILIATE);

        for (long subscriberId : allAffiliateSubscriberIds) {
            populateReferralInfoForSubscriber(subscriberId, referredSubscribersList, referralTransactionsList, affiliateNicknameList);
        }

        return new FastMap<>(
            "referredSubscribers", referredSubscribersList,
            "referralTransactions", referralTransactionsList,
            "affiliateNicknames", affiliateNicknameList
        );
    }

    private void populateReferralInfoForSubscriber(
        long subscriberId,
        List<ReferralReferredSubscriberStruct> referredSubscribersList, List<ReferralTransactionsStruct> referralTransactionsList, List<String> affiliateNicknameList)
    {
        populateReferralInfoForSubscriber(_identityService.getSubscriberById(subscriberId), true, referredSubscribersList, referralTransactionsList, affiliateNicknameList);
    }

    private void populateReferralInfoForSubscriber(
        Subscriber subscriber, boolean isAffiliate,
        List<ReferralReferredSubscriberStruct> referredSubscribersList, List<ReferralTransactionsStruct> referralTransactionsList, List<String> affiliateNicknameList)
    {
        if (isAffiliate) {
            affiliateNicknameList.add(subscriber.getNickname());
        }

        //get referredSubscribers - i.e. anyone who has this subscriber as their mint parent
        List<Subscriber> referredSubscribersTier1 = _identityService.getMintChildren(subscriber.getSubscriberId());

        //primary
        List<Subscriber> referredSubscribersTier2 = new ArrayList<>();
        for (Subscriber s : referredSubscribersTier1) {
            long subscriberId = s.getSubscriberId();
            Subscriber referredSubscriber = _identityService.getSubscriberById(subscriberId);
            referredSubscribersTier2.addAll(_identityService.getMintChildren(referredSubscriber.getSubscriberId()));
            referredSubscribersList.add(new ReferralReferredSubscriberStruct(referredSubscriber.getNickname(), referredSubscriber.getCreateDate(), subscriber.getNickname() ));
        }

        //secondary
        List<Subscriber> referredSubscribersTier3 = new ArrayList<>();
        if (isAffiliate) {
            for (Subscriber s : referredSubscribersTier2) {
                long subscriberId = s.getSubscriberId();
                Subscriber referredSubscriber = _identityService.getSubscriberById(subscriberId);
                referredSubscribersTier3.addAll(_identityService.getMintChildren(referredSubscriber.getSubscriberId()));
                referredSubscribersList.add(
                        new ReferralReferredSubscriberStruct(
                                referredSubscriber.getNickname(), referredSubscriber.getCreateDate(), _identityService.getSubscriberById(s.getMintParentSubscriberId()).getNickname() ));
            }
        }

        //tertiary
        if (isAffiliate) {
            for (Subscriber s : referredSubscribersTier3) {
                long subscriberId = s.getSubscriberId();
                Subscriber referredSubscriber = _identityService.getSubscriberById(subscriberId);
                referredSubscribersList.add(
                        new ReferralReferredSubscriberStruct(
                                referredSubscriber.getNickname(), referredSubscriber.getCreateDate(), _identityService.getSubscriberById(s.getMintParentSubscriberId()).getNickname() ));
            }
        }

        //convert the referredSubscribersList to a map via the nickname for quick lookup below
        Map<String, ReferralReferredSubscriberStruct> map = new HashMap<>(referredSubscribersList.size());
        for (ReferralReferredSubscriberStruct struct : referredSubscribersList) {
            map.put(struct.nickname, struct);
        }

        //get referralTransactions - all cash transactions that were from referrals
        List<CashPoolTransaction2> referralTransactions = _shoutContestService.getCashPoolTransactionsForSubscriberForTypes(
                subscriber.getSubscriberId(), Arrays.asList(CashPoolTransaction2.TYPE.PAYOUT_REFERRAL.toString()));

        for (CashPoolTransaction2 cpt : referralTransactions) {
            Date transactionDate = cpt.getTransactionDate();
            String referralNickname = cpt.getDescription();
            referralTransactionsList.add(new ReferralTransactionsStruct(referralNickname, transactionDate, cpt.getAmount()));

            //the previously created list of referrers (referredSubscribersList) needs to have an entry updated with this new date so that referrers with more recent transcations float to the top
            ReferralReferredSubscriberStruct struct = map.get(referralNickname);
            if (struct != null) {
                struct.date = transactionDate;
            }
        }
    }

    private static class ReferralReferredSubscriberStruct
    {
        public String nickname;

        //it is used by the json mapper
        @SuppressWarnings("unused")
        public Date date;

        //it is used by the json mapper
        @SuppressWarnings("unused")
        public String referrerNickname;

        public ReferralReferredSubscriberStruct(String nickname, Date date, String referrerNickname)
        {
            this.nickname = nickname;
            this.date = date;
            this.referrerNickname = referrerNickname;
        }
    }

    private static class ReferralTransactionsStruct
    {
        //it is used by the json mapper
        @SuppressWarnings("unused")
        public String nickname;

        //it is used by the json mapper
        @SuppressWarnings("unused")
        public Date date;

        //it is used by the json mapper
        @SuppressWarnings("unused")
        public double amount;

        public ReferralTransactionsStruct(String nickname, Date date, double amount)
        {
            this.nickname = nickname;
            this.date = date;
            this.amount = amount;
        }
    }

}
