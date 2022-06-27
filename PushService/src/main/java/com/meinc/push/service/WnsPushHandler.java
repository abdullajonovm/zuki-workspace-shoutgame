package com.meinc.push.service;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ar.com.fernandospr.wns.WnsService;
import ar.com.fernandospr.wns.model.WnsNotificationResponse;
import ar.com.fernandospr.wns.model.WnsRaw;
import ar.com.fernandospr.wns.model.builders.WnsRawBuilder;

import com.meinc.push.domain.SubscriberToken;

/**
 * Send push notifications to windows phone devices
 */
public class WnsPushHandler
{
    private static Logger _logger = Logger.getLogger(WnsPushHandler.class);
    
    private String _sid;
    private String _clientSecret;
    private WnsService _wnsWervice;
    
    public void setSid(String sid)
    {
        _sid = sid;
    }
    
    public void setClientSecret(String clientSecret)
    {
        _clientSecret = clientSecret;
    }
    
    public void push(WnsDelegate delegate, List<SubscriberToken> tokens, String payload)
    {
        if (_wnsWervice == null) {
            _logger.info("about to initialize the WNS Service...");
//_logger.info("QQQ initializing WNS service, sid: " + _sid + ", clientSecret: " + _clientSecret);
            _wnsWervice = new WnsService(_sid, _clientSecret, true/* _logger.isDebugEnabled()*/);
//_logger.info("QQQ WNS service initialized");
        }
        
        //convert the SubscriberTokens into a list of Channel URI's
        List<String> channelUris = new ArrayList<String>(tokens.size());
        for (SubscriberToken token : tokens) {
//_logger.info("\tQQQ token: " + token.getDeviceToken());
            channelUris.add(token.getDeviceToken());
        }
        
        //send the pushes
//_logger.info("WNS sending push: " + payload);
        try {
            WnsRaw raw =  new WnsRawBuilder().stream(payload.getBytes()).build();
//_logger.info("raw payload: " + raw.toString());
            List<WnsNotificationResponse> responses = _wnsWervice.pushRaw(channelUris, raw);
            
            for (WnsNotificationResponse response : responses) {
                if (response.code != 200) {
                    _logger.info("\tWNS response code: " + response.code);
                    _logger.info("\tWNS deviceConnectionStatus: " + response.deviceConnectionStatus);
                    _logger.info("\tWNS notificationStatus: " + response.notificationStatus);
                    _logger.info("\tWNS errorDescription: " + response.errorDescription);
                    _logger.info("\tWNS channelUri: " + response.channelUri);
                    _logger.info("\tWNS===========================");
                } else {
                    _logger.info("\tWNS response code: " + response.code);
                    _logger.info("\tWNS channelUri: " + response.channelUri);
                    _logger.info("\tWNS===========================");
                }
                
                //https://msdn.microsoft.com/en-us/library/windows/apps/hh465435.aspx
                switch (response.code)
                {
                    case 403: //forbidden
                    case 404: //not found
                    case 410: //gone
                        delegate.removeWnsToken(response.channelUri);
                        break;
                        
                    case 406: //not acceptable - sending too many too quickly; throttle
                        //TODO
                        break;
                }
            }
        } catch (Throwable t) {
            _logger.error("unknown exception while sending WNS push.\npayload:>>>" + payload + "<<<", t);
        }
    }
    
//    public static void main(String[] args)
//    throws Exception
//    {
//        //initialize logging
//        ConsoleAppender ca = new ConsoleAppender();
//        ca.setWriter(new OutputStreamWriter(System.out));
//        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
//        Logger.getLogger(WnsPushHandler.class).setLevel(Level.DEBUG);
//        Logger.getLogger(WnsPushHandler.class).addAppender(ca);
//        
//        //initialize WNS
//        WnsPushHandler handler = new WnsPushHandler();
//        handler.setSid("ms-app://s-1-15-2-1131822461-2694193888-2074825612-1530059408-3792690066-691977040-3875609775");
//        handler.setClientSecret("bbMT50N67+U9yLxIxjYtacWNg98OzQwq");
//
//        //send
//        List<SubscriberToken> tokens = new ArrayList<SubscriberToken>();
//        tokens.add(new SubscriberToken("https://bn1.notify.windows.com/?token=AwYAAAA%2bN%2fP2iOEYJXz5cTbrXuA%2f2jRppBJ%2bZOkbz5IHk9mSsfpB1K1CMmGSLo9Mf99BTVSXf5qS04aLnHS%2fXvQtPSIDbUebadrf%2fXEt%2fxSBsN80zzsEqtM3SHkYEkElSZj3eJs%3d"));
//        String payload = "{\"questionId\":11710,\"aps\":{\"sound\":\"winner.caf\",\"alert\":\"You've received question #12 from 'EN: Scott test' to answer.\"},\"gameId\":2114,\"type\":\"q\"}";
//        handler.push(tokens, payload);
//    }

}
