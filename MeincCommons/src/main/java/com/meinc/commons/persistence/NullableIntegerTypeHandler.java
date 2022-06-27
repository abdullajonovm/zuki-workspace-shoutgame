package com.meinc.commons.persistence;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class NullableIntegerTypeHandler implements TypeHandler
{

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException
    {
        if (parameter != null)
        {
            if (parameter instanceof Integer)
            {
                Integer val = (Integer) parameter;
                if (val == 0)
                {
                    ps.setNull(i, jdbcType.TYPE_CODE);
                }
                else
                {
                    ps.setInt(i, val);
                }
            }
            else
            {
                throw new IllegalArgumentException("Unsupported numeric type found: " + parameter.getClass().getName());
            }
        }
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException
    {
        Integer result = (Integer) rs.getInt(columnName);

        if (rs.wasNull())
        {
            result = 0;
        }

        return result;
    }

    public Object valueOf(String s)
    {
        return Integer.parseInt(s);
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException
    {
        Integer result = cs.getInt(columnIndex);

        if (cs.wasNull())
        {
            result = 0;
        }

        return result;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        Integer result = (Integer) rs.getInt(columnIndex);

        if (rs.wasNull())
        {
            result = 0;
        }

        return result;
    }

}
