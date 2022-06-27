package com.meinc.zztasks;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.meinc.zztasks.db.IProviderDao;
import com.meinc.zztasks.domain.AsyncProviderAddStatus;
import com.meinc.zztasks.domain.Provider;

public class ProviderTester
extends BaseTester
implements IProviderDao
{
    @Override
    public boolean doesDefaultProviderExist(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            return mapper.doesDefaultProviderExist(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public Provider getProvider(int subscriberId, String providerUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            return mapper.getProvider(subscriberId, providerUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Provider> getProviders(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            return mapper.getProviders(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public void addProvider(int subscriberId, Provider provider)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            mapper.addProvider(subscriberId, provider);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void removeProvider(String providerUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            mapper.removeProvider(providerUuid);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateProvider(Provider provider,
            boolean makeSureToEncryptFirstIfNecessary_seeBaseDataManager_resetSync)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            mapper.updateProvider(provider, false);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void beginAddProviderAsync(
        String transactionId, int subscriberId, String providerType, String providerUuid, String status)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            mapper.beginAddProviderAsync(transactionId, subscriberId, providerType, providerUuid, status);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public String getAddProviderAsyncUserStatus(String transactionId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            return mapper.getAddProviderAsyncUserStatus(transactionId);

        } finally {
            session.close();
        }
    }

    @Override
    public void finishAddProviderAsync(String transactionId, String status)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            mapper.finishAddProviderAsync(transactionId, status);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public AsyncProviderAddStatus getProviderAsyncAddStatus(String transactionId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            return mapper.getProviderAsyncAddStatus(transactionId);

        } finally {
            session.close();
        }
    }

    @Override
    public void cancelProviderAsyncAdd(String transactionId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            mapper.cancelProviderAsyncAdd(transactionId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Provider getRecentProvider(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            return mapper.getRecentProvider(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public Provider getDefaultProvider(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            return mapper.getDefaultProvider(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public void touchProvider(String providerUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            mapper.touchProvider(providerUuid);

            session.commit();
        } finally {
            session.close();
        }
    }

    public static void main(String[] args)
    {
        int SHAWKER = 358;
        String ergoProviderUuid = "893eede5-8a48-4bff-81d2-40557dcc26da";
        String googleProviderUuid = "f5a28db8-c1ec-4d97-80eb-3285bed68ae4";

        ProviderTester tester = new ProviderTester();

        //System.out.println(tester.doesDefaultProviderExist(SHAWKER));
        //System.out.println(tester.getDefaultProvider(SHAWKER));
//        System.out.println(tester.getRecentProvider(SHAWKER));

//        List<Provider> providers = tester.getProviders(SHAWKER);
//        providers.forEach(System.out::println);

//        Provider ergoProvider = tester.getProvider(SHAWKER, ergoProviderUuid);
//        System.out.println(ergoProvider);
//        ergoProvider.setDomain("whatever-testing");
//        tester.updateProvider(ergoProvider, false);
//
//        ergoProvider = tester.getProvider(SHAWKER, ergoProviderUuid);
//        System.out.println(ergoProvider);

//        Provider p1 = new Provider();
//        p1.setProviderUuid(UUID.randomUUID().toString());
//        p1.setDisplayName("p1 display name");
//        p1.setUserName("p1 username");
//        p1.setEmail("shawker@shout.tv");
//        p1.setDomain("p1 domain");
//        p1.setServer("p1 server");
//        p1.setWebServer("p1 webserver");
//        p1.setType(Provider.TYPE.ERGO);
//        p1.setCreateDate(new Date());
//        p1.setLastUpdate(new Date());
//        p1.setAccessDate(new Date());
//        p1.setSyncStateTasks("p1 sync state tasks");
//        p1.setSyncStateNotes("p1 sync state notes");
//        p1.setResetSync(1);
//        tester.addProvider(SHAWKER, p1);

//        tester.removeProvider("2242aaee-35bf-4282-8482-ff438e56a5b1");

        //String transactionId = "e90d1fac-b86f-11e9-85b9-22000a66be75";
        //tester.beginAddProviderAsync(transactionId, SHAWKER, "GOOGLE", googleProviderUuid, "s1");
        //System.out.println(tester.getAddProviderAsyncUserStatus(transactionId));
        //System.out.println(tester.getProviderAsyncAddStatus(transactionId));
        //tester.finishAddProviderAsync(transactionId, "done");
        //tester.cancelProviderAsyncAdd(transactionId);

        tester.touchProvider(ergoProviderUuid);
    }

}
