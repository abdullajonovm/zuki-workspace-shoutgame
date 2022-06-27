package com.meinc.webdatastore.dao;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.meinc.webdatastore.domain.WebDataStoreObject;

public class WebDataStoreDao {
    private static final Log log = LogFactory.getLog(WebDataStoreDao.class);

    @Autowired
    private SqlSessionTemplate sqlSession;
    @Autowired
    private PlatformTransactionManager transactionManager;

    public void getObjectsByType(String objectType, WebDataStoreObjectProcessor processor)
    {
        TransactionDefinition txDef = new DefaultTransactionDefinition(PROPAGATION_NESTED);
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);
        try {
            String sql = "SELECT * FROM webdatastore.object WHERE internal_type = ?";
            
            Connection con = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                con = sqlSession.getConnection();
                ps = con.prepareStatement(sql);
                ps.setString(1, objectType);
                rs = ps.executeQuery();
                WebDataStoreObject object = new WebDataStoreObject();
                while (rs.next()) {
                    //log.info("EMAIL: " + rs.getString(1));
                    //object.setId                 (rs.getInt   ("id"));
                    object.setPath               (rs.getString("path"));
                    object.setExpirationDate     (rs.getDate  ("expiration_date"));
                    object.setInternalObjectType (rs.getString("internal_type"));
                    object.setInternalObjectId   (rs.getLong  ("internal_id"));
                    object.setServiceCallback    (rs.getString("service_callback"));
                    object.setCallbackPassthrough(rs.getString("callback_passthrough"));
                    object.setCreateDate         (rs.getDate  ("create_date"));
                    object.setUpdateDate         (rs.getDate  ("update_date"));
                    try {
                        processor.process(object);
                    } catch (Exception e) {
                        log.error(String.format("Error while processing WDS object %s:%d", object.getInternalObjectType(), object.getInternalObjectId()), e);
                    }
                }
                transactionManager.commit(txStatus);
                txStatus = null;
            } catch (SQLException sqle) {
                log.error("Unable to getObjectsByType, objectType: " + objectType, sqle);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                        rs = null;
                    } catch (SQLException e) {
                        log.error("unable to close result set!", e);
                    }
                }
                if (ps != null) {
                    try {
                        ps.close();
                        ps = null;
                    } catch (SQLException e) {
                        log.error("unable to close prepared statement!", e);
                    }
                }
                //do NOT close the connection! Spring handles it
            }
        } finally {
            if (txStatus != null)
                transactionManager.rollback(txStatus);
            txStatus = null;
        }
    } 

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSession = sqlSessionTemplate;
    }
    
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public static interface WebDataStoreObjectProcessor {
        void process(WebDataStoreObject object);
    }
}

