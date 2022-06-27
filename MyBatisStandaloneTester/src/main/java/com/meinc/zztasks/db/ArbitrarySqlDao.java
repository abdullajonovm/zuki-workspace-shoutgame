package com.meinc.zztasks.db;

import org.apache.ibatis.annotations.SelectProvider;

public interface ArbitrarySqlDao
{
    static class ArbitrarySqlProvider
    {
        public String allNonExpiringPremiumSubscriberIdsSql(String sql)
        {
            return sql;
        }

        public String count(String sql)
        {
            return sql;
        }
    }

    @SelectProvider(type=ArbitrarySqlProvider.class, method="count")
    int getCount(String sql);
}
