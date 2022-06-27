package com.meinc.zztasks;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.meinc.zztasks.db.ISubscriberDao;
import com.meinc.zztasks.domain.ApplicationInformation;
import com.meinc.zztasks.domain.DeviceInformation;
import com.meinc.zztasks.domain.Subscriber;

public class SubscriberTester
extends BaseTester
implements ISubscriberDao
{
    @Override
    public Subscriber getSubscriberFromSession(String sessionKey)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            return mapper.getSubscriberFromSession(sessionKey);

        } finally {
            session.close();
        }
    }

    @Override
    public Subscriber getSubscriberViaEmail(String email)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            return mapper.getSubscriberViaEmail(email);

        } finally {
            session.close();
        }
    }

    @Override
    public Subscriber getSubscriber(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            return mapper.getSubscriber(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateSubscriber(final Subscriber s)
    {
        s.setLastUpdate(new Date());

        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            mapper.updateSubscriber(s);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public
    boolean isUsernameTaken_locking(String username)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            return mapper.isUsernameTaken_locking(username);

        } finally {
            session.close();
        }
    }

    @Override
    public
    String getSubscriberMissionStatement(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            return mapper.getSubscriberMissionStatement(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public
    void setSubscriberMissionStatement(int subscriberId, String missionStatement)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            mapper.setSubscriberMissionStatement(subscriberId, missionStatement);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addSubscriber(Subscriber s)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            mapper.addSubscriber(s);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void setSubscriberFbId(int subscriberId, String fbId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            mapper.setSubscriberFbId(subscriberId, fbId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Subscriber getSubscriberViaFbId(String fbId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            return mapper.getSubscriberViaFbId(fbId);

        } finally {
            session.close();
        }
    }

    @Override
    public boolean isUsernameTaken(String username)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            return mapper.isUsernameTaken(username);

        } finally {
            session.close();
        }
    }

    @Override
    public void addSubscriberDevice(int subscriberId, String sessionKey, DeviceInformation di,
            ApplicationInformation ai)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            mapper.addSubscriberDevice(subscriberId, sessionKey, di, ai);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public DeviceInformation getSubscriberDevice(int subscriberId, String deviceId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            return mapper.getSubscriberDevice(subscriberId, deviceId);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateSubscriberDevice(int subscriberId, String sessionKey, DeviceInformation di,
            ApplicationInformation ai)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            mapper.updateSubscriberDevice(subscriberId, sessionKey, di, ai);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Subscriber> getSubscribersWithStatePastExpiration()
    {
         SqlSession session = sqlSessionFactory.openSession();
         try {
             ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
             return mapper.getSubscribersWithStatePastExpiration();

         } finally {
             session.close();
         }
    }

    @Override
    public List<Subscriber> getTrialSubscribersCreatedFromToDaysAgo(int fromDaysAgo, int toDaysAgo)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ISubscriberDao mapper = session.getMapper(ISubscriberDao.class);
            return mapper.getTrialSubscribersCreatedFromToDaysAgo(fromDaysAgo, toDaysAgo);

        } finally {
            session.close();
        }
    }

    public static void main(String[] args)
    {
        SubscriberTester tester = new SubscriberTester();

        //Subscriber s = tester.getSubscriberViaEmail("shawker@shout.tv");
        //Subscriber s = tester.getSubscriberFromSession("c23cacb0-e4db-4463-9751-9664ab436118");
        //System.out.println(s);
        //tester.updateSubscriber(s);

        //System.out.println(tester.isUsernameTaken_locking("shawker@shout.tv"));
        //System.out.println(tester.isUsernameTaken_locking("shawker@shout123.tv55"));

        //tester.setSubscriberMissionStatement(358, "this is my mission statement");
        //System.out.println(tester.getSubscriberMissionStatement(358));

//        String s1fooUuid = "e2416687-b928-11e9-85b9-22000a66be75";
//        String s1FbId = "s1foo_fbid";
//        String s1Email = "s1foo@foo.com";
//        String s1NewFbId = "s1foo_fbid2";
//        Subscriber s = new Subscriber();
//        s.setUuid(s1fooUuid);
//        s.setEmail(s1Email);
//        s.setPaymentState(Subscriber.PAYMENT_STATE.ACTIVE);
//        s.setState(Subscriber.STATE.PREMIUM);
//        s.setStateExpirationDate(new Date(System.currentTimeMillis() + 999999999));
//        s.setFbId(s1FbId);
//        s.setTimezone("America/Denver");
//        s.setFeatureTester(true);
//        s.setAffiliate("not sure");
//        s.setCreateDate(new Date());
//        s.setLastUpdate(new Date());
//
//        System.out.println("s1foo pre: " + s);
//        tester.addSubscriber(s);
//        System.out.println("s1foo post: " + s);

//        System.out.println(tester.getSubscriberViaFbId(s1FbId));
//        System.out.println(tester.isUsernameTaken(s1Email));
//        System.out.println(tester.isUsernameTaken(s1Email+"xxx"));

//        Subscriber s = tester.getSubscriberViaFbId(s1FbId);
//        s.setFbId(s1NewFbId);
//        tester.setSubscriberFbId(s.getId(), s1NewFbId);
//        s = tester.getSubscriberViaFbId(s1NewFbId);
//        System.out.println(s);

//        Subscriber s = tester.getSubscriberViaEmail(s1Email);
//        String s1SessionKey = "834d517f-b92a-11e9-85b9-22000a66be75";
//        String s1DeviceId = "s1_device_id";
//        DeviceInformation di = new DeviceInformation();
//        di.setDeviceId(s1DeviceId);
//        di.setModel("s1_device_model");
//        di.setName("s1_device_name");
//        di.setOsName("s1_os_name");
//        di.setOsType("s1_os_type");
//        di.setVersion("s1_version");
//        ApplicationInformation ai = new ApplicationInformation();
//        ai.setApplicationId("FCTASKS");
//        ai.setApplicationVersion("2.1");
//        tester.addSubscriberDevice(s.getId(), s1SessionKey, di, ai);

//        System.out.println(tester.getSubscriberDevice(s.getId(), s1DeviceId));
//        DeviceInformation di = tester.getSubscriberDevice(s.getId(), s1DeviceId);
//        ai.setApplicationId("FCTASKSx");
//        ai.setApplicationVersion("2.1a");
//        //di.setDeviceId(di.getDeviceId()+"x");
//        di.setModel(di.getModel()+"y");
//        di.setName(di.getName()+"y");
//        di.setOsName(di.getOsName()+"y");
//        di.setOsType(di.getOsType()+"y");
//        di.setVersion(di.getVersion() + "y");
//        tester.updateSubscriberDevice(s.getId(), s1SessionKey, di, ai);

//        Subscriber s = tester.getSubscriber(358);
//        System.out.println(s);

//        //List<Subscriber> subs = tester.getSubscribersWithStatePastExpiration();
//        List<Subscriber> subs = tester.getTrialSubscribersCreatedFromToDaysAgo(16, 1);
//        subs.forEach(System.out::println);
    }

}
