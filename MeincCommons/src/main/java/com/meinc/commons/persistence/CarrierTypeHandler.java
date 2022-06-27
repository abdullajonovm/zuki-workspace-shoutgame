package com.meinc.commons.persistence;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.meinc.commons.domain.Carrier;

public class CarrierTypeHandler implements TypeHandler
{

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException
    {
        int carrierCode;
        Carrier carrier;

        if (parameter == null)
            carrierCode = Carrier.DEFAULT_CARRIER_CODE;
        else
        {
            carrier = (Carrier) parameter;
            carrierCode = carrier.getValue();
        }

        ps.setInt(i, carrierCode);
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException
    {
        Carrier result = null;
        int carrierCode = rs.getInt(columnName);

        if (!rs.wasNull())
        {
            result = Carrier.getCarrier(carrierCode);
        }

        return result;
    }

    public Object valueOf(String s)
    {
        return s;
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException
    {
        Carrier result = null;
        int carrierCode = cs.getInt(columnIndex);

        if (!cs.wasNull())
        {
            result = Carrier.getCarrier(carrierCode);
        }

        return result;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        Carrier result = null;
        int carrierCode = rs.getInt(columnIndex);

        if (!rs.wasNull())
        {
            result = Carrier.getCarrier(carrierCode);
        }

        return result;
    }
}
