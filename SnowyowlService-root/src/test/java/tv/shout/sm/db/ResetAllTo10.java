package tv.shout.sm.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tv.shout.sm.db.DbProvider.DB;

public class ResetAllTo10
extends BaseDbSupport
{

    public ResetAllTo10(DB which) throws Exception
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
        String sql =
            "select cp.subscriber_id " +
            "  from contest.cash_pool cp " +
            " where cp.amount > 10 " +
            "   and cp.subscriber_id not in ( " +
            "      select bp.subscriber_id from snowyowl.bot_player bp " +
            "   ) and cp.subscriber_id not in ( " +
            "       select s.subscriber_id from gameplay.s_subscriber s where s.firstname = 'Load' and s.lastname = 'Tester' " +
            "   )";

        SqlMapper<Integer> mapper = new SqlMapper<Integer>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                //no-op
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
            List<Integer> list = (List<Integer>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private double getBalance(int subscriberId)
    {
        String sql = "SELECT amount FROM contest.cash_pool WHERE subscriber_id = ?";

        SqlMapper<Double> mapper = new SqlMapper<Double>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setInt(1, subscriberId);
            }

            @Override
            public Double mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getDouble(1);
            }

            @Override
            public Collection<Double> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            List<Double> list = (List<Double>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
            return list.get(0);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void decrementBalance(int subscriberId, double currentBalance)
    {
        double amountToDeduct = 10D - currentBalance;
        System.out.println(MessageFormat.format("sId: {0,number,#}, balance: {1}, deduct: {2}", subscriberId, currentBalance, amountToDeduct));

        String sql1 = "update contest.cash_pool set amount = 10 where subscriber_id = ?";
        String sql2 =
                "insert into contest.cash_pool_transaction (subscriber_id, amount, description, external_ref_id, transaction_date) values ( " +
                "    ?, ?, 'PAID', 'resetting to 10', now() " +
                ")";

        Connection con = null;
        PreparedStatement ps = null;
        try {
            try {
                con = _db.getConnection();

                ps = con.prepareStatement(sql1);
                ps.setInt(1, subscriberId);
                ps.executeUpdate();
                ps.close();

                ps = con.prepareStatement(sql2);
                ps.setInt(1, subscriberId);
                ps.setDouble(2, amountToDeduct);
                ps.executeUpdate();
                ps.close();

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

    @Override
    public void run() throws Exception
    {
        List<Integer> subscriberIds = getSubscriberIds();

        for (int subscriberId : subscriberIds) {
            double balance = getBalance(subscriberId);
            if (balance > 10) {
                decrementBalance(subscriberId, balance);
            }
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        new ResetAllTo10(DbProvider.DB.NC11_1);
    }

}

//TODO: admin api to add bonus cash to someone