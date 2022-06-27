package com.meinc.ibatis.spring;

import java.io.IOException;

import org.apache.ibatis.session.SqlSessionFactory;

import com.meinc.ibatis.logging.IbatisLog;

public class SqlSessionFactoryBean extends org.mybatis.spring.SqlSessionFactoryBean {
    @Override
    protected SqlSessionFactory buildSqlSessionFactory() throws IOException {
        SqlSessionFactory factory = super.buildSqlSessionFactory();
        factory.getConfiguration().setLogImpl(IbatisLog.class);
        return factory;
    }
}
