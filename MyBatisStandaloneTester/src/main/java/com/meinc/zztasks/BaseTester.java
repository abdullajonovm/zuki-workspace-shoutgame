package com.meinc.zztasks;

import org.apache.ibatis.session.SqlSessionFactory;

import com.meinc.zztasks.db.MyBatisConnectionFactory;

public abstract class BaseTester
{
    protected SqlSessionFactory sqlSessionFactory;

    public BaseTester()
    {
        sqlSessionFactory = MyBatisConnectionFactory.getSqlSessionFactory();
    }

/*
    private Object selectOne() / selectMany()
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            return mapper.xxx

        } finally {
            session.close();
        }
    }

    private void insert() / update() / delete()
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IProviderDao mapper = session.getMapper(IProviderDao.class);
            mapper.xxx

            session.commit();
        } finally {
            session.close();
        }
    }

*/

}
