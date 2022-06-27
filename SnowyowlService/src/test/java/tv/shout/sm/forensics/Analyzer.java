package tv.shout.sm.forensics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import tv.shout.sm.admin.User;
import tv.shout.sm.admin.Users;
import tv.shout.sm.db.DbProvider;
import tv.shout.sm.test.CollectorToWdsResponse;
import tv.shout.sm.test.ConsoleOutputWithRunnableDataReceiver;
import tv.shout.sm.test.JsonRunnable;
import tv.shout.sm.test.SRD;
import tv.shout.snowyowl.forensics.FGame;

public class Analyzer
{
    private static Logger _logger = Logger.getLogger(Analyzer.class);

    private DbProvider.DB _which;
    private CollectorToWdsResponse _collector;
    private int _subscriberId;
    private Map<String, String> _authHeaders;

    private FGame _game;

    public Analyzer(DbProvider.DB which) throws InterruptedException
    {
        _which = which;
        _collector = new CollectorToWdsResponse(which);

        //get which user will be making the calls
        String userEmails = Users.getUserEmails(_which);
        String firstEmail = userEmails.split(",")[0];
        String email = getConsoleInput("Use which user: " + userEmails + " > ["+firstEmail+"]");
        if (email.trim().equals("")) email = firstEmail;
        User user = Users.getUser(_which, email);

        _authHeaders = new HashMap<>(2);
        _authHeaders.put("X-REST-SESSION-KEY", user.sessionKey);
        _authHeaders.put("X-REST-DEVICE-ID", user.deviceId);

        _subscriberId = user.subscriberId;

        //initialize the SRD as that user
        SRD.initialize(_which, email);

        getGame();
    }

    private void getGame() throws InterruptedException
    {
        final CountDownLatch cdl = new CountDownLatch(1);

        String gameId = getConsoleInput("gameId: ");
        _collector.adminGetGameForensics(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
            @Override
            public void run()
            {
                //TODO: _game = ...
                _logger.info("game name: " + this.json.get("name"));
                cdl.countDown();
            }
        }), _authHeaders, gameId);

        cdl.await();

        processGame();
    }

    private void processGame()
    {
        //TODO
        _logger.info("here be where the game be processed... arrrr");
    }

    private static String getConsoleInput(String message)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        try {
            return br.readLine();
        } catch (IOException e) {
            _logger.error("unable to get console input", e);
            return null;
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        //initialize the local logging to go to the console
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.com.meinc.mrsoa.net.outbound.MrSoaConnectionPool", "WARN");

        System.setProperty("log4j.defaultInitOverride", "true");
        Properties log4jProperties = new Properties();
        log4jProperties.setProperty("log4j.rootLogger", "DEBUG, stdout");
        log4jProperties.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        log4jProperties.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        log4jProperties.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d{MM/dd HH:mm:ss.SSS} %-5p [%c] %m%n");
        log4jProperties.setProperty("log4j.logger.org.apache.http.wire", "DEBUG");
        log4jProperties.setProperty("log4j.logger.org.apache.httpclient", "DEBUG");
        PropertyConfigurator.configure(log4jProperties);

        //trust all ssl certs (only do this for testing purposes!)
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }
        } };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) { return true; }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        Analyzer a = new Analyzer(DbProvider.DB.LOCAL);
    }
}
