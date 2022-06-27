package com.meinc.webdatastore.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("ReaperService")
public class ReaperService {
    private static final Log logger = LogFactory.getLog(ReaperService.class);
    
    @Scheduled(cron="0 * * * * *")
    public void run() {
        logger.info("ReaperService running");
        try {
            Thread.sleep(70000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
