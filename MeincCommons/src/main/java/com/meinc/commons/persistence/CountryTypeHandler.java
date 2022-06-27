package com.meinc.commons.persistence;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.meinc.commons.domain.Country;

public class CountryTypeHandler implements TypeHandler
{

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException
	{
		Country c;
		int countryCode = rs.getInt(columnName);
		
		if (!rs.wasNull())
			c = Country.getCounty(countryCode);
		else
			c = Country.getCounty(Country.DEFAULT_COUNTRY_NUMBER);

		return c;
	}

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException
	{
		int countryCode;
		if (parameter == null)
			countryCode = Country.DEFAULT_COUNTRY_NUMBER;
		else
		{
			Country c = (Country) parameter;
			countryCode = c.getNumber();
		}
		
		ps.setInt(i, countryCode);
	}

	public Object valueOf(String s)
	{
		return s;
	}

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException
    {
        Country c;
        int countryCode = cs.getInt(columnIndex);
        
        if (!cs.wasNull())
            c = Country.getCounty(countryCode);
        else
            c = Country.getCounty(Country.DEFAULT_COUNTRY_NUMBER);

        return c;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        Country c;
        int countryCode = rs.getInt(columnIndex);
        
        if (!rs.wasNull())
            c = Country.getCounty(countryCode);
        else
            c = Country.getCounty(Country.DEFAULT_COUNTRY_NUMBER);

        return c;
    }

}
