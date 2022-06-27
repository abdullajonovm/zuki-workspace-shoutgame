package com.meinc.jdbc.effect;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SideEffectConnection implements Connection {
    private static final Log log = LogFactory.getLog(SideEffectConnection.class);
    private static final Log meincLog;
    //private static final AtomicInteger counter = new AtomicInteger();
    //private static Map<Connection,Integer> connMap = new HashMap<Connection,Integer>();

    private boolean activityFlag;
    
    static {
        String pkgName = SideEffectConnection.class.getName();
        meincLog = LogFactory.getLog(pkgName.substring(0, pkgName.indexOf('.', pkgName.indexOf('.')+1))+"."+pkgName);
    }
    
    private Connection delegate;
    //private int id;
    
    private void checkForTransaction() throws SQLException {
        activityFlag = true;
        if (getAutoCommit()) {
            meincLog.warn("Detected SQL execution outside of transaction. See SQL log for details.");
            SqlExecutionOutsideOfTransactionWarning t = new SqlExecutionOutsideOfTransactionWarning();
            log.warn("Detected SQL execution outside of transaction", t);
        }
    }

    public SideEffectConnection(Connection delegate) {
        //Integer myId = connMap.get(delegate);
        //if (myId == null) {
        //    myId = counter.incrementAndGet();
        //    connMap.put(delegate, myId);
        //}
        //this.id = myId;
        this.delegate = delegate;
        TransactionSideEffectManager.newConnection(this);
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void close() throws SQLException {
        //log.info(id+"###close()");
        try {
            TransactionSideEffectManager.closeConnection(this);
        } catch (RuntimeException e) {
            meincLog.warn("Error closing side effects associated with connection: " + e.getMessage(), e);
            throw e;
        } finally {
            delegate.close();
        }
    }

    @Override
    public void commit() throws SQLException {
        //log.info(id+"###commit()");
        delegate.commit();
        // Side effects can perform database queries that, if performed without a final commit, will pull in old data at
        // the beginning of the next transaction's query, or fail to commit new data until the end of the next
        // transaction. Note that we must commit before executing side effects as per the contract of a side effect.
        activityFlag = false;
        TransactionSideEffectManager.commit();
        if (activityFlag) {
            //log.info(id+"###commit2()");
            delegate.commit();
        }
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }

    @Override
    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    @Override
    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    @Override
    public Statement createStatement() throws SQLException {
        //log.info(id+"###createStatement()");
        checkForTransaction();
        //return new LoggingStatement(delegate.createStatement(), id);
        return delegate.createStatement();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        //log.info(id+"###createStatement("+resultSetType+","+resultSetConcurrency+","+resultSetHoldability+")");
        checkForTransaction();
        //return new LoggingStatement(delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), id);
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        //log.info(id+"###createStatement("+resultSetType+","+resultSetConcurrency+")");
        checkForTransaction();
        //return new LoggingStatement(delegate.createStatement(resultSetType, resultSetConcurrency), id);
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        //log.info(id+"###createStruct("+typeName+","+attributes+")");
        checkForTransaction();
        return delegate.createStruct(typeName, attributes);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }

    @Override
    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }

    @Override
    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    @Override
    public Map<String,Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        //log.info(id+"###nativeSQL("+sql+")");
        return delegate.nativeSQL(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
        //log.info(id+"###prepareCall("+sql+","+resultSetType+","+resultSetConcurrency+","+resultSetHoldability+")");
        checkForTransaction();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        //log.info(id+"###prepareCall("+sql+","+resultSetType+","+resultSetConcurrency+")");
        checkForTransaction();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        checkForTransaction();
        //log.info(id+"###prepareCall("+sql+")");
        return delegate.prepareCall(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
        //log.info(id+"###prepareStatement("+sql+","+resultSetType+","+resultSetConcurrency+","+resultSetHoldability+")");
        checkForTransaction();
        //return new LoggingPreparedStatement(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), id);
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        //log.info(id+"###prepareStatement("+sql+","+resultSetType+","+resultSetConcurrency+")");
        checkForTransaction();
        //return new LoggingPreparedStatement(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency), id);
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        checkForTransaction();
        //log.info(id+"###prepareStatement("+sql+","+autoGeneratedKeys+")");
        //return new LoggingPreparedStatement(delegate.prepareStatement(sql, autoGeneratedKeys), id);
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        //log.info(id+"###prepareStatement("+sql+","+columnIndexes+")");
        checkForTransaction();
        //return new LoggingPreparedStatement(delegate.prepareStatement(sql, columnIndexes), id);
        return delegate.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        //log.info(id+"###prepareStatement("+sql+","+columnNames+")");
        checkForTransaction();
        //return new LoggingPreparedStatement(delegate.prepareStatement(sql, columnNames), id);
        return delegate.prepareStatement(sql, columnNames);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        //log.info(id+"###prepareStatement("+sql+")");
        checkForTransaction();
        //return new LoggingPreparedStatement(delegate.prepareStatement(sql), id);
        return delegate.prepareStatement(sql);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        //log.info(id+"###releaseSavepoint("+savepoint+")");
        delegate.releaseSavepoint(savepoint);
        TransactionSideEffectManager.releaseSavepoint();
    }

    @Override
    public void rollback() throws SQLException {
        //log.info(id+"###rollback()");
        delegate.rollback();
        TransactionSideEffectManager.rollback();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        //log.info(id+"###rollback("+savepoint+")");
        delegate.rollback(savepoint);
        TransactionSideEffectManager.rollbackToSavepoint();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        //log.info(id+"###setAutoCommit("+autoCommit+")");
        delegate.setAutoCommit(autoCommit);
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        //log.info(id+"###setCatalog("+catalog+")");
        delegate.setCatalog(catalog);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        //log.info(id+"###setClientInfo("+properties+")");
        delegate.setClientInfo(properties);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        //log.info(id+"###setClientInfo("+name+","+value+")");
        delegate.setClientInfo(name, value);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        //log.info(id+"###setHoldability("+holdability+")");
        delegate.setHoldability(holdability);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        //log.info(id+"###setReadOnly("+readOnly+")");
        delegate.setReadOnly(readOnly);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        //log.info(id+"###setSavepoint()");
        Savepoint savepoint = delegate.setSavepoint();
        TransactionSideEffectManager.addSavepoint();
        return savepoint;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        //log.info(id+"###setSavepoint("+name+")");
        Savepoint savepoint = delegate.setSavepoint(name);
        TransactionSideEffectManager.addSavepoint();
        return savepoint;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        //log.info(id+"###setTransactionIsolation("+level+")");
        delegate.setTransactionIsolation(level);
    }

    @Override
    public void setTypeMap(Map<String,Class<?>> map) throws SQLException {
        //log.info(id+"###setTypeMap("+map+")");
        delegate.setTypeMap(map);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        //log.info(id+"###unwrap("+iface+")");
        return delegate.unwrap(iface);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        delegate.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return delegate.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        delegate.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return delegate.getNetworkTimeout();
    }
    
    private static class SqlExecutionOutsideOfTransactionWarning extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
