package com.meinc.zztasks;

import org.apache.ibatis.session.SqlSession;

import com.meinc.zztasks.db.ArbitrarySqlDao;

public class ArbitrarySqlTester
extends BaseTester
implements ArbitrarySqlDao
{
    @Override
    public int getCount(String sql)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            ArbitrarySqlDao mapper = session.getMapper(ArbitrarySqlDao.class);
            return mapper.getCount(sql);

        } finally {
            session.close();
        }
    }

    public static void main(String[] args)
    {
        ArbitrarySqlTester tester = new ArbitrarySqlTester();
        System.out.println(tester.getCount("select count(*) FROM ergo.google_role"));
        System.out.println(tester.getCount("select count(*) FROM ergo.google_note"));
        System.out.println(tester.getCount("select count(*) FROM ergo.google_task"));
    }

}
