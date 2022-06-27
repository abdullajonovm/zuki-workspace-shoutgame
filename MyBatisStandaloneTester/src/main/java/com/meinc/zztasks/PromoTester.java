package com.meinc.zztasks;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.meinc.zztasks.db.IPromoDao;
import com.meinc.zztasks.domain.PromoBatch;
import com.meinc.zztasks.domain.PromoBatch.STATUS;
import com.meinc.zztasks.domain.PromoCode;

public class PromoTester
extends BaseTester
implements IPromoDao
{

    @Override
    public boolean isUserAuthorizedToAdministerPromoData(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            return mapper.isUserAuthorizedToAdministerPromoData(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public void insertPromoBatch(PromoBatch batch)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            mapper.insertPromoBatch(batch);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void insertPromoCode(PromoCode code, int batchId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            mapper.insertPromoCode(code, batchId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<PromoBatch> getAllPromoBatches()
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            return mapper.getAllPromoBatches();

        } finally {
            session.close();
        }
    }

    @Override
    public List<PromoBatch> getPromoBatchesByStatus(STATUS status)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            return mapper.getPromoBatchesByStatus(status);

        } finally {
            session.close();
        }
    }

    @Override
    public PromoBatch getPromoBatch(int batchId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            return mapper.getPromoBatch(batchId);

        } finally {
            session.close();
        }
    }

    @Override
    public void updatePromoBatch(PromoBatch batch)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            mapper.updatePromoBatch(batch);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public int getNumberAssignedPromoCodesForBatch(int batchId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            return mapper.getNumberAssignedPromoCodesForBatch(batchId);

        } finally {
            session.close();
        }
    }

    @Override
    public void deletePromoCodesForBatch(int batchId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            mapper.deletePromoCodesForBatch(batchId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void deletePromoBatch(int batchId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            mapper.deletePromoBatch(batchId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<PromoCode> getAllPromoCodesForBatch(int batchId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            return mapper.getAllPromoCodesForBatch(batchId);

        } finally {
            session.close();
        }
    }

    @Override
    public List<PromoCode> getPromoCodesForBatchByStatus(int batchId, com.meinc.zztasks.domain.PromoCode.STATUS status)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            return mapper.getPromoCodesForBatchByStatus(batchId, status);

        } finally {
            session.close();
        }
    }

    @Override
    public PromoCode getPromoCodeViaId(int codeId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            return mapper.getPromoCodeViaId(codeId);

        } finally {
            session.close();
        }
    }

    @Override
    public PromoCode getPromoCodeViaCode(String code)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            return mapper.getPromoCodeViaCode(code);

        } finally {
            session.close();
        }
    }

    @Override
    public void markPromoCodeAssigned(int codeId, int subscriberId, int receiptId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IPromoDao mapper = session.getMapper(IPromoDao.class);
            mapper.markPromoCodeAssigned(codeId, subscriberId, receiptId);

            session.commit();
        } finally {
            session.close();
        }
    }

    public static void main(String[] args)
    {
        int SHAWKER = 358;
        int batchId = 18;
        PromoTester tester = new PromoTester();

//        System.out.println(tester.isUserAuthorizedToAdministerPromoData(SHAWKER));
//        System.out.println(tester.isUserAuthorizedToAdministerPromoData(384));

//        PromoBatch batch = new PromoBatch();
//        batch.setName("test batch 1");
//        batch.setDateCreated(new Date());
//        batch.setCreatorId(SHAWKER);
//        batch.setLastUpdate(new Date());
//        batch.setStatus(PromoBatch.STATUS.NEW);
//        batch.setType(PromoBatch.TYPE.PREMIUM);
//        batch.setNumCodes(10);
//        batch.setUnclaimedExpireDate(new Date(System.currentTimeMillis() + 99_999_999L));
//
//        System.out.println(batch);
//        tester.insertPromoBatch(batch);
//        System.out.println(batch);

//        List<PromoBatch> batches = tester.getAllPromoBatches();
//        List<PromoBatch> batches = tester.getPromoBatchesByStatus(PromoBatch.STATUS.NEW);
//        batches.forEach(System.out::println);

//        PromoBatch batch = tester.getPromoBatch(batchId);
//        System.out.println(batch);

//        batch.setName(batch.getName()+"x");
//        batch.setLastUpdate(new Date());
//        batch.setStatus(PromoBatch.STATUS.ACTIVE);
//        batch.setType(PromoBatch.TYPE.PRO);
//        batch.setUnclaimedExpireDate(new Date(System.currentTimeMillis() + 999_999_999L));
//        tester.updatePromoBatch(batch);

//        for (int i=0; i<5; i++) {
//            PromoCode code = new PromoCode();
//            code.setCode(PromoCode.generateCode());
//            tester.insertPromoCode(code, batchId);
//        }

//        System.out.println(tester.getNumberAssignedPromoCodesForBatch(batchId));
//        tester.deletePromoCodesForBatch(batchId);

//        List<PromoCode> codes = tester.getAllPromoCodesForBatch(batchId);
//        List<PromoCode> codes = tester.getPromoCodesForBatchByStatus(batchId, PromoCode.STATUS.NEW);
//        codes.forEach(System.out::println);

//        System.out.println(tester.getPromoCodeViaCode("CRSVSCWEZ7F3VGB8"));
//        System.out.println(tester.getPromoCodeViaId(150));

//        tester.markPromoCodeAssigned(150, SHAWKER, 10101);

//        tester.deletePromoBatch(batchId);
    }

}
