package tv.shout.sm.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import tv.shout.sm.admin.CashPoolTransaction;
import tv.shout.sm.admin.CashPoolTransaction2;
import tv.shout.sm.db.DbProvider.DB;

public class ConvertToNewPayout
extends BaseDbSupport
{
    public ConvertToNewPayout(DB which) throws Exception
    {
        super(which);
    }

    @Override
    public void init(DB which) throws Exception
    {
        //no-op
    }

    private List<Integer> getSubscriberIds()
    {
        String sql = "SELECT DISTINCT(subscriber_id) FROM contest.cash_pool_transaction";

        SqlMapper<Integer> sqlMapper = new SqlMapper<Integer>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
            }

            @Override
            public Integer mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getInt(1);
            }

            @Override
            public Collection<Integer> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            return (List<Integer>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CashPoolTransaction> getCashPoolTransactionsForSubscriber(int subscriberId)
    {
        String sql = "SELECT * FROM contest.cash_pool_transaction WHERE subscriber_id = ? ORDER BY transaction_date ASC";

        SqlMapper<CashPoolTransaction> sqlMapper = new SqlMapper<CashPoolTransaction>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setInt(1, subscriberId);
            }

            @Override
            public CashPoolTransaction mapRowToType(ResultSet rs) throws SQLException
            {
                CashPoolTransaction cpt = new CashPoolTransaction();

                cpt.setCashpoolTransactionId(rs.getLong("cashpool_transaction_id"));
                cpt.setSubscriberId(subscriberId);
                cpt.setAmount(rs.getDouble("amount"));
                cpt.setDescription(rs.getString("description"));
                cpt.setReceiptId(getNullableInt(rs, "receipt_id"));
                cpt.setContextUuid(rs.getString("context_uuid"));
                cpt.setExternalRefId(rs.getString("external_ref_id"));
                cpt.setTransactionDate(rs.getTimestamp("transaction_date"));

                return cpt;
            }

            @Override
            public Collection<CashPoolTransaction> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            return (List<CashPoolTransaction>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addCashPoolTransaction(CashPoolTransaction2 cp2)
    {
        String sql =
            "INSERT INTO contest.`cash_pool_transaction2` (" +
            "   subscriber_id, `amount`, `type`, `description`, current_pool_amount, current_bonus_amount, used_pool_amount, used_bonus_amount, " +
            "   receipt_id, context_uuid, transaction_date " +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try {
            PreparedStatement ps = null;
            Connection con = _db.getConnection();

            try {
                ps = con.prepareStatement(sql);
                ps.setInt(1, cp2.getSubscriberId());
                ps.setDouble(2, cp2.getAmount());
                ps.setString(3, cp2.getType().toString());
                ps.setString(4, cp2.getDescription());
                ps.setDouble(5, cp2.getCurrentPoolAmount());
                ps.setDouble(6, cp2.getCurrentBonusAmount());
                if (cp2.getUsedPoolAmount() == null) {
                    ps.setNull(7, java.sql.Types.DOUBLE);
                } else {
                    ps.setDouble(7, cp2.getUsedPoolAmount());
                }
                if (cp2.getUsedBonusAmount() == null) {
                    ps.setNull(8, java.sql.Types.DOUBLE);
                } else {
                    ps.setDouble(8, cp2.getUsedBonusAmount());
                }
                if (cp2.getReceiptId() == null) {
                    ps.setNull(9, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(9, cp2.getReceiptId());
                }
                ps.setString(10, cp2.getContextUuid());
                ps.setTimestamp(11, new Timestamp(cp2.getTransactionDate().getTime()));

                ps.execute();

            } finally {
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
                if (con != null) {
                    con.close();
                    con = null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void convert(int subscriberId, CountDownLatch cdl, int total)
    {
        int count = _numProcessed.incrementAndGet();
        //System.out.println("converting data for: " + subscriberId + " (x / y)");
        System.out.println(MessageFormat.format("converting data for {0,number,#} ({1} / {2})", subscriberId, count, total));

        try {
            //get the existing data
            List<CashPoolTransaction> cpts = getCashPoolTransactionsForSubscriber(subscriberId);

            //convert from the old to new format
            CashPoolTransaction2 previousCpt2 = null;
            List<CashPoolTransaction2> cpt2s = new ArrayList<>(cpts.size());
            for (CashPoolTransaction cpt : cpts) {
                CashPoolTransaction2 newCpt2 = CashPoolTransaction2.fromCashPoolTransaction(previousCpt2, cpt);
                cpt2s.add(newCpt2);
                previousCpt2 = newCpt2;
            }

            //store in the new format
            cpt2s.forEach(this::addCashPoolTransaction);
        } finally {
            cdl.countDown();
        }
    }

    private static AtomicInteger _numProcessed = new AtomicInteger(0);
    private static final int POOL_SIZE = 16;

    @Override
    public void run() throws Exception
    {
        List<Integer> subscriberIds = getSubscriberIds();

        CountDownLatch cdl = new CountDownLatch(subscriberIds.size());
        final ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);

        for (int subscriberId : subscriberIds) {
            threadPool.execute(new Runnable() {
                @Override
                public void run()
                {
                    try {
                        convert(subscriberId, cdl, subscriberIds.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        cdl.await();

        System.out.println("DONE");
    }

    public static void main(String[] args)
    throws Exception
    {
        new ConvertToNewPayout(DbProvider.DB.NC11_1);
    }

}
