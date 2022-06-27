package com.meinc.mrsoa.distdata.visor;

import static com.meinc.jdbc.SQLError.isTransactionLost;
import static java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;
import static java.sql.Connection.TRANSACTION_REPEATABLE_READ;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.bonecp.BoneCpDataSource;
import com.meinc.jdbc.SQLError;

public class DistributedVisor {
    private static Log log = LogFactory.getLog(DistributedVisor.class);

    private static DataSource dataSource = BoneCpDataSource.getInstance();

    public static boolean tryClusterLock(String lockName) {
        // We must be able to commit quickly so we don't block other cluster-locks with MySQL gap locks
        Connection c = null;
        PreparedStatement s = null;
        int oldIsolation = -1;
        try {
            c = dataSource.getConnection();
            oldIsolation = c.getTransactionIsolation();
            c.setTransactionIsolation(TRANSACTION_READ_UNCOMMITTED);
            c.setAutoCommit(false);
            s = c.prepareStatement(" INSERT IGNORE INTO distdata.`lock` (lock_name, create_date) VALUES ( ?, NOW() ) ");
            s.setString(1, lockName);

            boolean inserted = false;
            try {
                inserted = s.executeUpdate() > 0;
            } catch (SQLException e) {
                if (isTransactionLost(e)) {
                    log.error("Error while trying cluster lock: " + e.getMessage(), e);
                    return false;
                }
                if (log.isDebugEnabled())
                    log.debug("Trying lock '" + lockName + "' determined another thread owns it - skipping: " + e.getMessage(), e);
                inserted = false;
            }

            if (!inserted) {
                // The row already exists
                s.close();
                s = c.prepareStatement(" SELECT create_date FROM distdata.`lock` WHERE lock_name = ? ");
                s.setString(1, lockName);
                ResultSet rs = s.executeQuery();
                if (!rs.next()) {
                    if (log.isDebugEnabled())
                        log.debug("Must have just missed cluster lock '" + lockName + "'");
                    return false;
                }
                Timestamp lockedDate = rs.getTimestamp(1);
                long lockedEpochMs = lockedDate.getTime();
                long nowEpochMs = System.currentTimeMillis();
                long lockedMinutes = (nowEpochMs - lockedEpochMs) / 1000 / 60;
                if (lockedMinutes >= 5)
                    log.warn("*** CLUSTER LOCK '" + lockName + "' HAS BEEN LOCKED FOR " + lockedMinutes + " MINUTES ***");
                else if (log.isDebugEnabled())
                    log.debug(" Cluster lock '" + lockName + "' has been locked for " + lockedMinutes + " minutes");
                return false;
            }

        } catch (SQLException e) {
            log.error("Error while trying cluster lock: " + e.getMessage(), e);
            if (isTransactionLost(e))
                return false;
            try {
                c.rollback();
            } catch (SQLException e1) { }
            return false;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;

        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    return false;
                }
                s = null;
            }
            if (c != null) {
                boolean autoCommit = true;
                try {
                    autoCommit = c.getAutoCommit();
                } catch (SQLException e1) { }
                // autoCommit can be set to true if we lost the transaction, in which case we shouldn't try to commit anything
                if (!autoCommit) {
                    try {
                        c.commit();
                    } catch (SQLException e) {
                        return false;
                    }
                }
                try {
                    if (oldIsolation != -1)
                        c.setTransactionIsolation(oldIsolation);
                } catch (SQLException e) { }
                try {
                    c.setAutoCommit(true);
                } catch (SQLException e) { }
                try {
                    c.close();
                } catch (SQLException e) { }
                c = null;
            }
        }

        return true;
    }
    
    public static void releaseClusterLock(String lockName) {
        // We must be able to commit quickly so we don't block other cluster-locks with MySQL gap locks
        Connection c = null;
        PreparedStatement s = null;
        try {
            c = dataSource.getConnection();
            c.setAutoCommit(false);
            s = c.prepareStatement(" DELETE FROM distdata.`lock` WHERE lock_name = ? ");
            s.setString(1, lockName);
            int updatedCount = s.executeUpdate();
            if (updatedCount == 0)
                log.warn("Attempted to release lock '" + lockName + "' but none existed");
            
            c.commit();
            c.setAutoCommit(true);

        } catch (SQLException e) {
            log.error("Error while releasing cluster lock: " + e.getMessage(), e);
            if (isTransactionLost(e))
                return;
            try {
                c.rollback();
            } catch (SQLException e1) { }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) { }
                s = null;
            }
            if (c != null) {
                try {
                    c.setAutoCommit(true);
                    c.close();
                } catch (SQLException e) { }
                c = null;
            }
        }
    }
}
