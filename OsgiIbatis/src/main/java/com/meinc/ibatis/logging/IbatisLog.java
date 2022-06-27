package com.meinc.ibatis.logging;

import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.logging.Log;

public class IbatisLog implements Log {
    org.apache.commons.logging.Log logger = LogFactory.getLog("mybatis.mapper");
    private String name;
    
    public IbatisLog(String name) {
        System.err.println("Creating IbatisLog: " + name);
        this.name = name;
    }
    
    @Override
    public void debug(String s) {
        System.err.println("logger.debug: " + s);
        logger.debug(s);
    }

    @Override
    public void error(String s) {
        logger.error(s);
    }

    @Override
    public void error(String s, Throwable t) {
        logger.error(s, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String s) {
        System.err.println("logger.trace: " + s);
        logger.trace(s);
    }

    @Override
    public void warn(String s) {
        logger.warn(s);
    }
}
