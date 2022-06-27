package com.meinc.bonecp;

import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.Statistics;
import com.meinc.jdbc.effect.SideEffectConnection;
import com.meinc.launcher.serverprops.ServerPropertyHolder;

public class BoneCpDataSource implements DataSource {
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(BoneCpDataSource.class);
    private static BoneCpDataSourceLogger dsLogger = new BoneCpDataSourceLogger();
    private static Timer dsLoggerTimer = new Timer("BoneCP Logger", true);
    private static BoneCPDataSource dataSource = new BoneCPDataSource();
    
    private static BoneCpDataSource singleton = new BoneCpDataSource();
    
    public static BoneCpDataSource getInstance() {
        return singleton;
    }
    
    static {
        boolean useSystemProps = Boolean.parseBoolean(System.getProperty("osgi.bonecp.use.system.props", "false"));

        Properties serverProperties = useSystemProps ? System.getProperties() : ServerPropertyHolder.getProps();
        
        dataSource.setDriverClass(serverProperties.getProperty("jdbc.driverClassName"));
        dataSource.setJdbcUrl(serverProperties.getProperty("jdbc.url"));
        dataSource.setUsername(serverProperties.getProperty("jdbc.username"));
        dataSource.setPassword(serverProperties.getProperty("jdbc.password"));
        dataSource.setPoolName("Combined");
        dataSource.setIdleMaxAgeInMinutes(Long.parseLong(serverProperties.getProperty("jdbc.conn.pool.idle_max_age_min", "30")));
        dataSource.setIdleConnectionTestPeriodInMinutes(Long.parseLong(serverProperties.getProperty("jdbc.conn.pool.idle_conn_test_min", "10")));
        dataSource.setConnectionTestStatement("/* ping */ SELECT 1");
        dataSource.setMinConnectionsPerPartition(Integer.parseInt(serverProperties.getProperty("jdbc.conn.pool.min", "10")));
        dataSource.setMaxConnectionsPerPartition(Integer.parseInt(serverProperties.getProperty("jdbc.conn.pool.max", "100")));
        dataSource.setPartitionCount(Integer.parseInt(serverProperties.getProperty("jdbc.conn.pool.partitions", "1")));
        dataSource.setAcquireIncrement(Integer.parseInt(serverProperties.getProperty("jdbc.conn.pool.acquire_increment", "1")));
        dataSource.setStatementsCacheSize(Integer.parseInt(serverProperties.getProperty("jdbc.conn.pool.stmt_cache_size", "100")));
        dataSource.setReleaseHelperThreads(Integer.parseInt(serverProperties.getProperty("jdbc.conn.pool.conn_release_threads", "0")));
        dataSource.setStatementReleaseHelperThreads(Integer.parseInt(serverProperties.getProperty("jdbc.conn.pool.stmt_release_threads", "0")));
        dataSource.setStatisticsEnabled(Boolean.parseBoolean(serverProperties.getProperty("jdbc.conn.pool.stats", "true")));
        dataSource.setCloseConnectionWatch(Boolean.parseBoolean(serverProperties.getProperty("jdbc.conn.pool.conn_watcher", "false")));
        dataSource.setCloseConnectionWatchTimeoutInMs(Long.parseLong(serverProperties.getProperty("jdbc.conn.pool.conn_watcher_timeout_ms", "3000")));
        dataSource.setTransactionRecoveryEnabled(Boolean.parseBoolean(serverProperties.getProperty("jdbc.conn.pool.tx.retry", "true")));
        
        dataSource.setAcquireRetryAttempts(2);
        dataSource.setAcquireRetryDelayInMs(500);
        
        long statsInteveralMs = Long.parseLong(serverProperties.getProperty("jdbc.conn.pool.stats_interval_sec", "60")) * 1000;
        dsLoggerTimer.schedule(dsLogger, statsInteveralMs, statsInteveralMs);
    }
    
    private boolean capturedPool = false;
    
    private BoneCpDataSource() { }
    
    private Connection prepareConnection(ConnectionHandle connection) throws SQLException {
        if (!capturedPool && dataSource.isStatisticsEnabled()) {
            dsLogger.addPool(connection.getPool(), dataSource.getPoolName());
            capturedPool = true;
        }
        
        connection.setDebugHandle(this);
        
        return new SideEffectConnection(connection);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        ConnectionHandle connection = (ConnectionHandle) dataSource.getConnection();
        
        return prepareConnection(connection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        ConnectionHandle connection = (ConnectionHandle) dataSource.getConnection(username, password);
        
        return prepareConnection(connection);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        dataSource.setLogWriter(logWriter);
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        //return dataSource.getParentLogger();
        return null;
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        dataSource.setLoginTimeout(loginTimeout);
    }

    @Override
    public boolean isWrapperFor(Class<?> clazz) throws SQLException {
        return dataSource.isWrapperFor(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> clazz) throws SQLException {
        return (T) dataSource.unwrap(clazz);
    }

    public void close() {
        dataSource.close();
    }

    private static class BoneCpDataSourceLogger extends TimerTask {
        private static final Logger log = LoggerFactory.getLogger(BoneCpDataSourceLogger.class);
        
        private static class BonePool {
            private WeakReference<BoneCP> pool;
            private String poolName;
        }
        
        private List<BonePool> bonePools = new ArrayList<BonePool>();

        /** Idempotent */
        private synchronized void addPool(BoneCP newPool, String poolName) {
            // Check if pool has been added already
            boolean containsPool = false;
            Iterator<BonePool> bonePoolsIt = bonePools.iterator();
            while (bonePoolsIt.hasNext()) {
                BonePool bonePool = bonePoolsIt.next();
                BoneCP bcp = bonePool.pool.get();
                if (bcp == null)
                    bonePoolsIt.remove();
                else if (bcp == newPool) {
                    containsPool = true;
                    break;
                }
            }
            if (!containsPool) {
                BonePool bonePool = new BonePool();
                bonePool.pool = new WeakReference<BoneCP>(newPool);
                bonePool.poolName = poolName;
                bonePools.add(bonePool);
            }
        }

        private final String logMessageTemplate = "\n" +
                "=============== %s Connection Pool Statistics ===============\n" +
                "active=%d idle=%d total=%d\n" +
                "totalGetConn=%d avgGetConnMs=%1.0f\n" +
                "avgStmtLoadMs=%1.0f avgStmtExecMs=%1.0f\n" +
                "totalCachedStmts=%d stmtCacheHits=%d stmtCacheMisses=%d\n" +
                "totalGetConnMs=%d totalGetStmtMs=%d totalStmtExecMs=%d";

        @Override
        public synchronized void run() {
            StringBuffer logMessage = new StringBuffer();
            Iterator<BonePool> bonePoolsIt = bonePools.iterator();
            while (bonePoolsIt.hasNext()) {
                BonePool bonePool = bonePoolsIt.next();
                BoneCP bcp = bonePool.pool.get();
                if (bcp == null)
                    bonePoolsIt.remove();
                else {
                    Statistics stats = bcp.getStatistics();
                    String logMessagePart = String.format(logMessageTemplate,
                            bonePool.poolName,
                            stats.getTotalLeased(), stats.getTotalFree(), stats.getTotalCreatedConnections(),
                            stats.getConnectionsRequested(), stats.getConnectionWaitTimeAvg(),
                            stats.getStatementPrepareTimeAvg(), stats.getStatementExecuteTimeAvg(),
                            stats.getStatementsCached(), stats.getCacheHits(), stats.getCacheMiss(),
                            stats.getCumulativeConnectionWaitTime(), stats.getCumulativeStatementPrepareTime(), stats.getCumulativeStatementExecutionTime());
                    logMessage.append(logMessagePart);
                }
            }
            if (logMessage.length() > 0)
                log.info("*************************" + logMessage.toString());
        }
    }
}
