package meinc.zztasks;

import java.beans.PropertyVetoException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import tv.shout.sm.db.BaseDbSupport;
import tv.shout.sm.db.BaseDbSupport.SqlMapper;

public class ReportRunner
{
    private static final int POOL_SIZE = 16;
    private static final String SQL_DRIVER = "com.mysql.jdbc.Driver";

    private ComboPooledDataSource _cpds;

    public ReportRunner()
    {
        String dbUrl = "jdbc:mysql://fctasks-production.cik3cnlqzqyo.us-east-1.rds.amazonaws.com:3306/";
        String un = "root";
        String pw = "FLV2auo5CEsO";

        _cpds = new ComboPooledDataSource();
        try {
            _cpds.setDriverClass(SQL_DRIVER);
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
        _cpds.setJdbcUrl(dbUrl);
        _cpds.setUser(un);
        _cpds.setPassword(pw);
        _cpds.setMinPoolSize(POOL_SIZE);
        _cpds.setAcquireIncrement(POOL_SIZE);
        _cpds.setMaxPoolSize(POOL_SIZE);
    }

    private static class SubscriberProviderInfo
    {
        public int subscriberId;
        public String email;
        public String subscriptionState;
        public Date subscriptionExpirationDate;
        public String providerType;
        public Date providerCreateDate;
        public Date subscriberLastActivityDate;

        @Override
        public String toString()
        {
            return MessageFormat.format(
                "{0},{1},{2},{3,date,yyyy-MM-dd}", //,date,yyyy-MM-dd hh:mm:ss.SSS
                email, subscriptionState, providerType, subscriberLastActivityDate);
        }
    }

    private Collection<SubscriberProviderInfo> getSubscriberProviderInfo()
    {
        String sql =
            "  select s.subscriber_id, s.email, s.state, s.state_expiration_date, p.provider_type, p.create_date " +
            "    from ergo.subscriber s, ergo.provider p " +
            "   where p.subscriber_id = s.subscriber_id " +
            "     and s.email not like 'tfcqa%' " +
//" and s.email in('shawker@shout.tv', 'newecxchange@okie.com') " + //TODO
            "   limit 100000";

        SqlMapper<SubscriberProviderInfo> sqlMapper = new SqlMapper<SubscriberProviderInfo>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
            }

            @Override
            public SubscriberProviderInfo mapRowToType(ResultSet rs) throws SQLException
            {
                SubscriberProviderInfo info = new SubscriberProviderInfo();

                info.subscriberId = rs.getInt("subscriber_id");
                info.email = rs.getString("email");
                info.subscriptionState = rs.getString("state");
                info.subscriptionExpirationDate = rs.getTimestamp("state_expiration_date");
                info.providerType = rs.getString("provider_type");
                info.providerCreateDate = rs.getTimestamp("create_date");

                return info;
            }

            @Override
            public Collection<SubscriberProviderInfo> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        List<SubscriberProviderInfo> rawData;
        try {
            rawData = (List<SubscriberProviderInfo>) BaseDbSupport.executeSqlForList(getConnection(), sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //now the fun part - filter it so that only the most recent provider for any given provider is kept
        Map<Integer, SubscriberProviderInfo> finalList = new HashMap<>();

        rawData.forEach(data -> {
            if (finalList.containsKey(data.subscriberId)) {
                //see which has the newer date
                SubscriberProviderInfo existingInfo = finalList.get(data.subscriberId);
                if (data.providerCreateDate.after(existingInfo.providerCreateDate)) {
                    finalList.put(data.subscriberId, data);
                }

            } else {
                finalList.put(data.subscriberId, data);
            }
        });

        Collection<SubscriberProviderInfo> data = finalList.values();

        //now for the even ore fun part - determining the last time a subscriber was active
        final ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);
        final CountDownLatch cdl = new CountDownLatch(data.size());
        _numberProcessed = new AtomicInteger(0);

        java.util.Iterator<SubscriberProviderInfo> it = data.iterator();
        while (it.hasNext()) {
            final SubscriberProviderInfo info = it.next();
            threadPool.execute(() -> populateLastActiityDate(info, cdl, data.size()));
        }

        //wait until all processing threads have completed
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();

        return data;
    }

    private AtomicInteger _numberProcessed;

    private void populateLastActiityDate(SubscriberProviderInfo info, CountDownLatch cdl, int totalToProcess)
    {
        Date mostRecentDate = new Date(0);

        Date d;

        //most recent subscriber object activity
        d = getMostRecentDateRefactor(
                "select greatest( " +
                "    ifnull(max(create_date),'1970-01-01 00:00:00'), " +
                "    ifnull(max(update_date),'1970-01-01 00:00:00') " +
                ") from ergo.subscriber where subscriber_id = ?",
                info.subscriberId
            );
        if (d.after(mostRecentDate)) {
            mostRecentDate = d;
        }

        //most recent provider activity
        d = getMostRecentDateRefactor(
                "select greatest( " +
                "    ifnull(max(create_date),'1970-01-01 00:00:00'), " +
                "    ifnull(max(update_date),'1970-01-01 00:00:00'), " +
                "    ifnull(max(access_date),'1970-01-01 00:00:00') " +
                ") from ergo.provider where subscriber_id = ?",
                info.subscriberId
            );
        if (d.after(mostRecentDate)) {
            mostRecentDate = d;
        }

        switch (info.providerType)
        {
            case "ERGO": {
                //role
                d = getMostRecentDateRefactor(
                        "select greatest( " +
                        "    ifnull(max(create_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(update_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(delete_date),'1970-01-01 00:00:00') " +
                        ") from ergo.role where subscriber_id = ?",
                        info.subscriberId
                    );
                if (d.after(mostRecentDate)) {
                    mostRecentDate = d;
                }

                //note
                d = getMostRecentDateRefactor(
                        "select greatest( " +
                        "    ifnull(max(create_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(update_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(delete_date),'1970-01-01 00:00:00') " +
                        ") from ergo.note where subscriber_id = ?",
                        info.subscriberId
                    );
                if (d.after(mostRecentDate)) {
                    mostRecentDate = d;
                }

                //task
                d = getMostRecentDateRefactor(
                        "select greatest( " +
                        "    ifnull(max(create_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(update_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(delete_date),'1970-01-01 00:00:00') " +
                        ") from ergo.task where subscriber_id = ?",
                        info.subscriberId
                    );
                if (d.after(mostRecentDate)) {
                    mostRecentDate = d;
                }
            }
            break;

            case "GOOGLE": {
                //role
                d = getMostRecentDateRefactor(
                        "select greatest(" +
                        "    ifnull(max(create_date),'1970-01-01 00:00:00')," +
                        "    ifnull(max(update_date),'1970-01-01 00:00:00')," +
                        "    ifnull(max(delete_date),'1970-01-01 00:00:00')," +
                        "    ifnull(max(last_server_sync_time),'1970-01-01 00:00:00')" +
                        ") from ergo.google_role where subscriber_id = ?",
                        info.subscriberId
                    );
                if (d.after(mostRecentDate)) {
                    mostRecentDate = d;
                }

                //note
                d = getMostRecentDateRefactor(
                        "select greatest( " +
                        "    ifnull(max(create_date),'1970-01-01 00:00:00')," +
                        "    ifnull(max(update_date),'1970-01-01 00:00:00')," +
                        "    ifnull(max(delete_date),'1970-01-01 00:00:00')," +
                        "    ifnull(max(last_server_sync_time),'1970-01-01 00:00:00')" +
                        ") from ergo.google_note where subscriber_id = ?",
                        info.subscriberId
                    );
                if (d.after(mostRecentDate)) {
                    mostRecentDate = d;
                }

                //task
                d = getMostRecentDateRefactor(
                        "select greatest(" +
                        "    ifnull(max(create_date),'1970-01-01 00:00:00')," +
                        "    ifnull(max(update_date),'1970-01-01 00:00:00')," +
                        "    ifnull(max(delete_date),'1970-01-01 00:00:00')," +
                        "    ifnull(max(last_server_sync_time),'1970-01-01 00:00:00')" +
                        ") from ergo.google_task where subscriber_id = ?",
                        info.subscriberId
                    );
                if (d.after(mostRecentDate)) {
                    mostRecentDate = d;
                }
            }
            break;

            case "EXCHANGE": {
                //role
                d = getMostRecentDateRefactor(
                        "select greatest( " +
                        "    ifnull(max(create_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(update_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(delete_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(last_server_sync_time),'1970-01-01 00:00:00') " +
                        ") from ergo.exchange_role where subscriber_id = ?",
                        info.subscriberId
                    );
                if (d.after(mostRecentDate)) {
                    mostRecentDate = d;
                }

                //note
                d = getMostRecentDateRefactor(
                        "select greatest( " +
                        "    ifnull(max(create_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(update_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(delete_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(last_server_sync_time),'1970-01-01 00:00:00') " +
                        ") from ergo.exchange_note where subscriber_id = ?",
                        info.subscriberId
                    );
                if (d.after(mostRecentDate)) {
                    mostRecentDate = d;
                }

                //task
                d = getMostRecentDateRefactor(
                        "select greatest( " +
                        "    ifnull(max(create_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(update_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(delete_date),'1970-01-01 00:00:00'), " +
                        "    ifnull(max(last_server_sync_time),'1970-01-01 00:00:00') " +
                        ") from ergo.exchange_task where subscriber_id = ?",
                        info.subscriberId
                    );
                if (d.after(mostRecentDate)) {
                    mostRecentDate = d;
                }
            }
            break;
        }

        info.subscriberLastActivityDate = mostRecentDate;

        int numberProcessed = _numberProcessed.incrementAndGet();
        System.out.println(MessageFormat.format("Processed {0} / {1}", numberProcessed, totalToProcess));
        cdl.countDown();
    }

    private Date getMostRecentDateRefactor(String sql, int subscriberId)
    {
        SqlMapper<Date> sqlMapper = new SqlMapper<Date>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setInt(1, subscriberId);
            }

            @Override
            public Date mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getTimestamp(1);
            }

            @Override
            public Collection<Date> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            return ((List<Date>) BaseDbSupport.executeSqlForList(getConnection(), sql, sqlMapper, true)).get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection()
    {
        try {
            return _cpds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void close()
    {
        if (_cpds != null) {
            try {
                DataSources.destroy(_cpds);
            } catch (SQLException ignored) {
            }
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        ReportRunner runner = new ReportRunner();
        try {
            Path path = Paths.get("/Users/shawker/Desktop/zztasks_by_provider.csv");
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("email,subscription_state,provider_type,last_activity_date\r\n");
                runner.getSubscriberProviderInfo().forEach(info -> {
                    try {
                        writer.write(info.toString());
                        writer.write("\r\n");
                    } catch (IOException e) {
                    }
                });
            }
            System.out.println("DONE");
        } finally {
            runner.close();
        }
    }

}
