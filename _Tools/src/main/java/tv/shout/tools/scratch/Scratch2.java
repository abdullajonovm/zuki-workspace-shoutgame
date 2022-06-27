package tv.shout.tools.scratch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.meinc.identity.domain.Subscriber;

public class Scratch2
{
    private static class ReferralReferredSubscriberStruct
    {
        //it is used by the json mapper
        @SuppressWarnings("unused")
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

    public static void main(String[] args)
    throws Exception
    {
        Subscriber subscriber = new Subscriber(); subscriber.setSubscriberId(8); subscriber.setNickname("yarell"); subscriber.setCreateDate(new Date());
        Subscriber yarell001 = new Subscriber(); yarell001.setSubscriberId(2236); yarell001.setNickname("yarell_001"); yarell001.setCreateDate(new Date()); yarell001.setMintParentSubscriberId(8);
        Subscriber yarell002 = new Subscriber(); yarell002.setSubscriberId(2237); yarell002.setNickname("yarell_002"); yarell002.setCreateDate(new Date()); yarell002.setMintParentSubscriberId(2236);
        Subscriber yarell003 = new Subscriber(); yarell003.setSubscriberId(2238); yarell003.setNickname("yarell_003"); yarell003.setCreateDate(new Date()); yarell003.setMintParentSubscriberId(2237);
        Subscriber yarell004 = new Subscriber(); yarell004.setSubscriberId(2239); yarell004.setNickname("yarell_004"); yarell004.setCreateDate(new Date()); yarell004.setMintParentSubscriberId(9);
        Subscriber bxgrant = new Subscriber(); bxgrant.setSubscriberId(9); bxgrant.setNickname("bxgrant"); bxgrant.setCreateDate(new Date()); bxgrant.setMintParentSubscriberId(8);

        Map<Integer, Subscriber> identityMap = new HashMap<>();
        identityMap.put(8, subscriber);
        identityMap.put(9, bxgrant);
        identityMap.put(2236, yarell001);
        identityMap.put(2237, yarell002);
        identityMap.put(2238, yarell003);
        identityMap.put(2239, yarell004);

        Map<Integer, List<Subscriber>> mintMap = new HashMap<>();
        mintMap.put(8, Arrays.asList(bxgrant, yarell001));
        mintMap.put(9, Arrays.asList(yarell004));
        mintMap.put(2236, Arrays.asList(yarell002));
        mintMap.put(2237, Arrays.asList(yarell003));
        mintMap.put(2238, Arrays.asList());
        mintMap.put(2239, Arrays.asList());

        boolean isAffiliate = true;

        List<ReferralReferredSubscriberStruct> referredSubscribersList = new ArrayList<>();

        List<Subscriber> referredSubscribersTier1 = Arrays.asList(yarell001, bxgrant);

        //primary
        List<Subscriber> referredSubscribersTier2 = new ArrayList<>();
        if (isAffiliate) {
            for (Subscriber s : referredSubscribersTier1) {
                int subscriberId = s.getSubscriberId();
                Subscriber referredSubscriber = identityMap.get(subscriberId);
                referredSubscribersTier2.addAll(mintMap.get(referredSubscriber.getSubscriberId()));
                referredSubscribersList.add(new ReferralReferredSubscriberStruct(referredSubscriber.getNickname(), referredSubscriber.getCreateDate(), subscriber.getNickname() ));
            }
        }

        //secondary
        List<Subscriber> referredSubscribersTier3 = new ArrayList<>();
        if (isAffiliate) {
            for (Subscriber s : referredSubscribersTier2) {
                int subscriberId = s.getSubscriberId();
                Subscriber referredSubscriber = identityMap.get(subscriberId);
                referredSubscribersTier3.addAll(mintMap.get(referredSubscriber.getSubscriberId()));
                referredSubscribersList.add(new ReferralReferredSubscriberStruct(referredSubscriber.getNickname(), referredSubscriber.getCreateDate(), identityMap.get(s.getMintParentSubscriberId()).getNickname() ));
            }
        }

        //tertiary
        if (isAffiliate) {
            for (Subscriber s : referredSubscribersTier3) {
                int subscriberId = s.getSubscriberId();
                Subscriber referredSubscriber = identityMap.get(subscriberId);
                referredSubscribersList.add(new ReferralReferredSubscriberStruct(referredSubscriber.getNickname(), referredSubscriber.getCreateDate(), identityMap.get(s.getMintParentSubscriberId()).getNickname() ));
            }
        }

        //System.out.println(MessageFormat.format("{0}", referredSubscribersList));
        System.out.println(JsonUtil.getObjectMapper().writeValueAsString(referredSubscribersList));
    }
}
