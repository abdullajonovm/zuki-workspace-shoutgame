package tv.shout.snowyowl.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.bonecp.BoneCpDataSource;

public class DistributedVisorV2
{
    private static Log _logger = LogFactory.getLog(DistributedVisorV2.class);
    private static DataSource _dataSource = BoneCpDataSource.getInstance();

    public static boolean doesClusterLockExist(String lockName)
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = _dataSource.getConnection();
            c.setAutoCommit(false);

            ps = c.prepareStatement("SELECT COUNT(*) FROM distdata.`lock` WHERE lock_name = ?");
            ps.setString(1, lockName);
            rs = ps.executeQuery();
            rs.next();

            c.commit();
            c.setAutoCommit(true);

            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            _logger.warn("unable to check cluster lock for: " + lockName, e);
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) { }
                rs = null;
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ignored) { }
                ps = null;
            }
            if (c != null) {
                try {
                    c.setAutoCommit(true);
                    c.close();
                } catch (SQLException ignored) { }
                c = null;
            }

        }
    }
}
