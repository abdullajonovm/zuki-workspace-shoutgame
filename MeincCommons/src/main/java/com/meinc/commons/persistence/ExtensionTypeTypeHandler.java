package com.meinc.commons.persistence;


import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.meinc.commons.domain.Extension;

public class ExtensionTypeTypeHandler 
implements TypeHandler
{

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException
	{
		if (parameter == null)
			ps.setNull(i, Types.VARCHAR);
		else
			ps.setString(i, Extension.getExtensionTypeAsString((Extension.EXTENSION_TYPE)parameter));		
	}

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException
	{
		String value = rs.getString(columnName);
		if (rs.wasNull())
			return null;
		
		return Extension.getExtensionTypeFromString(value);
	}

	public Object valueOf(String s)
	{
		return s;
	}

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException
    {
        String value = cs.getString(columnIndex);
        if (cs.wasNull())
            return null;
        
        return Extension.getExtensionTypeFromString(value);
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (rs.wasNull())
            return null;
        
        return Extension.getExtensionTypeFromString(value);
    }

}
