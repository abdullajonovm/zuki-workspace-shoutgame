package com.meinc.jdbc.effect;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingPreparedStatement extends LoggingStatement implements PreparedStatement {
    private static final Log log = LogFactory.getLog(LoggingPreparedStatement.class);

    public LoggingPreparedStatement(PreparedStatement target, int connectionId) {
        super(target, connectionId);
    }

    public void addBatch() throws SQLException {
        log.info(connectionId+"--#addBatch()");
        ((PreparedStatement)delegate).addBatch();
    }

    public void clearParameters() throws SQLException {
        log.info(connectionId+"--#clearParameters()");
        ((PreparedStatement)delegate).clearParameters();
    }

    public boolean execute() throws SQLException {
        log.info(connectionId+"--#execute()");
        return ((PreparedStatement)delegate).execute();
    }

    public ResultSet executeQuery() throws SQLException {
        log.info(connectionId+"--#executeQuery()");
        return ((PreparedStatement)delegate).executeQuery();
    }

    public int executeUpdate() throws SQLException {
        log.info(connectionId+"--#executeUpdate()");
        return ((PreparedStatement)delegate).executeUpdate();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return ((PreparedStatement)delegate).getMetaData();
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return ((PreparedStatement)delegate).getParameterMetaData();
    }

    public void setArray(int parameterIndex, Array x) throws SQLException {
        log.info(connectionId+"--#setArray("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setArray(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        log.info(connectionId+"--#setAsciiStream("+parameterIndex+","+x+","+length+")");
        ((PreparedStatement)delegate).setAsciiStream(parameterIndex, x, length);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        log.info(connectionId+"--#setAsciiStream("+parameterIndex+","+x+","+length+")");
        ((PreparedStatement)delegate).setAsciiStream(parameterIndex, x, length);
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        log.info(connectionId+"--#setAsciiStream("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setAsciiStream(parameterIndex, x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        log.info(connectionId+"--#setBigDecimal("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setBigDecimal(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        log.info(connectionId+"--#setBinaryStream("+parameterIndex+","+x+","+length+")");
        ((PreparedStatement)delegate).setBinaryStream(parameterIndex, x, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        log.info(connectionId+"--#setBinaryStream("+parameterIndex+","+x+","+length+"L)");
        ((PreparedStatement)delegate).setBinaryStream(parameterIndex, x, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        log.info(connectionId+"--#setBinaryStream("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setBinaryStream(parameterIndex, x);
    }

    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        log.info(connectionId+"--#setBlob("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setBlob(parameterIndex, x);
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        log.info(connectionId+"--#setBlob("+parameterIndex+","+inputStream.getClass().getName()+","+length+")");
        ((PreparedStatement)delegate).setBlob(parameterIndex, inputStream, length);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        log.info(connectionId+"--#setBlob("+parameterIndex+","+inputStream.getClass().getName()+")");
        ((PreparedStatement)delegate).setBlob(parameterIndex, inputStream);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        log.info(connectionId+"--#setBoolean("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setBoolean(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        log.info(connectionId+"--#setByte("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setByte(parameterIndex, x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        log.info(connectionId+"--#setBytes("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setBytes(parameterIndex, x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        log.info(connectionId+"--#setCharacterStream("+parameterIndex+","+reader.getClass().getName()+","+length+")");
        ((PreparedStatement)delegate).setCharacterStream(parameterIndex, reader, length);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        log.info(connectionId+"--#setCharacterStream("+parameterIndex+","+reader.getClass().getName()+","+length+"L)");
        ((PreparedStatement)delegate).setCharacterStream(parameterIndex, reader, length);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        log.info(connectionId+"--#setCharacterStream("+parameterIndex+","+reader.getClass().getName()+")");
        ((PreparedStatement)delegate).setCharacterStream(parameterIndex, reader);
    }

    public void setClob(int parameterIndex, Clob x) throws SQLException {
        log.info(connectionId+"--#setClob("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setClob(parameterIndex, x);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        log.info(connectionId+"--#setClob("+parameterIndex+","+reader.getClass().getName()+","+length+"L)");
        ((PreparedStatement)delegate).setClob(parameterIndex, reader, length);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        log.info(connectionId+"--#setClob("+parameterIndex+","+reader.getClass().getName()+")");
        ((PreparedStatement)delegate).setClob(parameterIndex, reader);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        log.info(connectionId+"--#setDate("+parameterIndex+","+x+","+cal+")");
        ((PreparedStatement)delegate).setDate(parameterIndex, x, cal);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        log.info(connectionId+"--#setDate("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setDate(parameterIndex, x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        log.info(connectionId+"--#setDouble("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setDouble(parameterIndex, x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        log.info(connectionId+"--#setFloat("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setFloat(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        log.info(connectionId+"--#setInt("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setInt(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        log.info(connectionId+"--#setLong("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setLong(parameterIndex, x);
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        log.info(connectionId+"--#setNCharacterStream("+parameterIndex+","+value.getClass().getName()+","+length+")");
        ((PreparedStatement)delegate).setNCharacterStream(parameterIndex, value, length);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        log.info(connectionId+"--#setNCharacterStream("+parameterIndex+","+value.getClass().getName()+")");
        ((PreparedStatement)delegate).setNCharacterStream(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        log.info(connectionId+"--#setNClob("+parameterIndex+","+value+")");
        ((PreparedStatement)delegate).setNClob(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        log.info(connectionId+"--#setNClob("+parameterIndex+","+reader.getClass().getName()+","+length+")");
        ((PreparedStatement)delegate).setNClob(parameterIndex, reader, length);
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        log.info(connectionId+"--#setNClob("+parameterIndex+","+reader.getClass().getName()+")");
        ((PreparedStatement)delegate).setNClob(parameterIndex, reader);
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        log.info(connectionId+"--#setNString("+parameterIndex+","+value+")");
        ((PreparedStatement)delegate).setNString(parameterIndex, value);
    }

    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        log.info(connectionId+"--#setNull("+parameterIndex+","+sqlType+","+typeName+")");
        ((PreparedStatement)delegate).setNull(parameterIndex, sqlType, typeName);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        log.info(connectionId+"--#setNull("+parameterIndex+","+sqlType+")");
        ((PreparedStatement)delegate).setNull(parameterIndex, sqlType);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        log.info(connectionId+"--#setObject("+parameterIndex+","+x+","+targetSqlType+","+scaleOrLength+")");
        ((PreparedStatement)delegate).setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        log.info(connectionId+"--#setObject("+parameterIndex+","+x+","+targetSqlType+")");
        ((PreparedStatement)delegate).setObject(parameterIndex, x, targetSqlType);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        log.info(connectionId+"--#setObject("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setObject(parameterIndex, x);
    }

    public void setRef(int parameterIndex, Ref x) throws SQLException {
        log.info(connectionId+"--#setRef("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setRef(parameterIndex, x);
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        log.info(connectionId+"--#setRowId("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setRowId(parameterIndex, x);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        log.info(connectionId+"--#setSQLXML("+parameterIndex+","+xmlObject+")");
        ((PreparedStatement)delegate).setSQLXML(parameterIndex, xmlObject);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        log.info(connectionId+"--#setShort("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setShort(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        log.info(connectionId+"--#setString("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setString(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        log.info(connectionId+"--#setTime("+parameterIndex+","+x+","+cal+")");
        ((PreparedStatement)delegate).setTime(parameterIndex, x, cal);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        log.info(connectionId+"--#setTime("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setTime(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        log.info(connectionId+"--#setTimestamp("+parameterIndex+","+x+","+cal+")");
        ((PreparedStatement)delegate).setTimestamp(parameterIndex, x, cal);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        log.info(connectionId+"--#setTimestamp("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setTimestamp(parameterIndex, x);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        log.info(connectionId+"--#setURL("+parameterIndex+","+x+")");
        ((PreparedStatement)delegate).setURL(parameterIndex, x);
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        log.info(connectionId+"--#setUnicodeStream("+parameterIndex+","+x+","+length+")");
        ((PreparedStatement)delegate).setUnicodeStream(parameterIndex, x, length);
    }

}
