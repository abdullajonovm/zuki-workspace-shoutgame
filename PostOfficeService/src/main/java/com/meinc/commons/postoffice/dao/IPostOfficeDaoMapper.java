package com.meinc.commons.postoffice.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface IPostOfficeDaoMapper
{
    @Insert("INSERT INTO postoffice.email_optout (from_address, to_address, opted_out_date) VALUES(#{0}, #{1}, NOW()) " +
            "ON DUPLICATE KEY UPDATE from_address=from_address, to_address=to_address, opted_out_date=opted_out_date")
    public void addEmailOptout(String senderAddress, String recipientAddress);
    
    @Delete("DELETE FROM postoffice.email_optout WHERE from_address = #{0} AND to_address = #{1}")
    public void removeEmailOptout(String senderAddress, String recipientAddress);
    
    @Delete("DELETE FROM postoffice.email_optout WHERE to_address = #{0}")
    public void removeAllEmailOptout(String recipientAddress);
    
    @Select("SELECT count(*) FROM postoffice.email_optout WHERE from_address = #{0} AND to_address = #{1}")
    public boolean isEmailOptedOut(String senderAddress, String recipientAddress);
    
    @Select("SELECT count(*) FROM postoffice.email_optout WHERE to_address = #{0}")
    public boolean isEmailAnyOptedOut(String recipientAddress);
}
