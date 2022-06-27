package tv.shout.sm.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

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
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.sync.service.ISyncService;

/*
 * X A)
 * When creating the game, also create 1 tournament round, and ask how many "lives" each player gets
 *      1 life = single elimination
 *      2 lives = double elimination
 *      3 lives = triple elimination
 *      etc
 * the game starts, and plays as normal for each of the pool play rounds
 *
 * X B)
 * at the end of the final pool play round, move everyone into the tournament round
 *
 * X C)
 * the pairing engine will then pair everyone based on their rank (since type = tournament)
 * play for the tournament round is the same as for a pool round (answer N questions correct or ultimately timeout or lose too many and get eliminated)
 *
 * X C1) at the end of the round, everyone once again receives a rank, BUT NOW: anyone who lost has a life decremented.
 *
 * X D)
 * if there is more than 1 person with a life left,
 *      X clone the previous tournament round (incrementing N in the title to be "Tournament Round N")
 *      X move everyone with a life left into the new tournament (the rankings will come across)
 *      X anyone without a life left gets a new notification type: ELIMINIATED (and then the clients wait for the game to end)
 *      X start the new tournament round and go to step C
 * else
 *      X the game is over. keep winner at the top, sort everyone else by rank, end the game, and assign payouts
 *
 * DOC & TEST
 */

//to run from the command line using mvn:
//mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass="tv.shout.sm.test.ApiTest" -Dexec.args=""
public class ApiTest
{
    //gameId -> Set<SyncMessage>
    private static Map<String, Set<SyncMessage>> _processedSyncMessages = new HashMap<>();

    private static Logger _logger = Logger.getLogger(ApiTest.class);

    private CollectorToWdsResponse _collector;
    private ISyncService _syncService;

    private static String _syncMessagesDir;
    private DbProvider.DB _which;
    private int _subscriberId;
    private Map<String, String> _authHeaders;

    @SuppressWarnings("unchecked")
    public static Set<SyncMessage> getProcessedMessages(String gameId)
    {
        Set<SyncMessage> messages = _processedSyncMessages.get(gameId);

        if (messages == null) {
            File syncMessagesFile = new File(_syncMessagesDir, "syncmessages_" + gameId + ".db");

            //if the file exists, read it in
            if (syncMessagesFile.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(syncMessagesFile);
                    try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                        messages = (Set<SyncMessage>) ois.readObject();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    _logger.error("unable to load sync messages", e);
                    messages = new HashSet<>();
                }

            } else {
                //file doesn't yet exist; just create a blank set
                messages = new HashSet<>();
            }
        }

        return messages;
    }

    @SuppressWarnings("unchecked")
    public static void addProcessedMessage(String gameId, SyncMessage syncMessage)
    {
        Set<SyncMessage> messages = _processedSyncMessages.get(gameId);

        File syncMessagesFile = new File(_syncMessagesDir, "syncmessages_" + gameId + ".db");

        //if the object isn't yet in memory, check the disk
        if (messages == null) {

            //if the file exists, read it in
            if (syncMessagesFile.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(syncMessagesFile);
                    try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                        messages = (Set<SyncMessage>) ois.readObject();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    _logger.error("unable to load sync messages", e);
                    messages = new HashSet<>();
                }

            } else {
                //file doesn't yet exist; just create a blank set
                messages = new HashSet<>();
            }
        }

        //add the new message to the list
        messages.add(syncMessage);

        //serialize the messages to disk
        try {
            FileOutputStream fos = new FileOutputStream(syncMessagesFile);
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(messages);
            }
        } catch (IOException e) {
            _logger.error("unable to save sync messages", e);
        }
    }

    public static Date getLastSyncDate(String gameId)
    {
        Set<SyncMessage> syncMessages = ApiTest.getProcessedMessages(gameId);
        SyncMessage sm = syncMessages.stream()
            .sorted( (sm1, sm2) -> sm2.getCreateDate().compareTo(sm1.getCreateDate()) )
            .findFirst()
            .orElse(null);

        return sm != null ? sm.getCreateDate() : new Date(System.currentTimeMillis() - 600_000L); //ten minutes ago; can't pass null as lastSyncDate if doing a direct call to the service
    }

    private void run(DbProvider.DB which)
    throws Exception
    {
        throw new UnsupportedOperationException();
//        _which = which;
//
//        _collector = new CollectorToWdsResponse(which);
//        _syncService = new SyncServiceClientProxy(false);
//
//        //setup the service backdoor entry point based on which database is being used
//        switch (which)
//        {
//            case DC4: {
//                InetSocketAddress socket;
//                try {
//                    socket = new InetSocketAddress(Inet4Address.getByName("dc4-collector1.shoutgameplay.com"), 43911);
//                } catch (UnknownHostException e) {
//                    _logger.error("unable to connect to dc4", e);
//                    throw new IllegalStateException(e);
//                }
//                MrSoaServer server = new MrSoaServer(socket);
//                MrSoaServerMonitor monitor = MrSoaServerMonitor.getInstance();
//                monitor.registerService(
//                        ((SyncServiceClientProxy)_syncService).getEndpoint(),
//                        server);
//            }
//            break;
//        }
//
//        //begin test rig
//        StringBuilder buf = new StringBuilder();
//        buf.append(  "1) unauthenticated call...");
//        buf.append("\n2) authenticated call...");
//        buf.append("\n> ");
//        String val = getConsoleInput(buf.toString());
//
//        switch (val)
//        {
//            case "1":
//                doUnauthenticatedCalls();
//                break;
//
//            case "2":
//                doAuthenticatedCalls();
//                break;
//        }

    }

    private void doUnauthenticatedCalls()
    throws Exception
    {
        SRD.initialize(_which, null);

        StringBuilder buf = new StringBuilder();
        buf.append(  "1) getQuestionCategoriesFromCategoryKeys");
        buf.append("\n> ");
        String val = getConsoleInput(buf.toString());

        switch (val)
        {
            case "1": {
                String categories = getConsoleInput("category keys (comma-delimited): ");
                _collector.getQuestionCategoriesFromCategoryKeys(new ConsoleOutputDataReceiver(), Arrays.asList(categories.split(",")));
            }
        }
    }

    private void doAuthenticatedCalls()
    throws Exception
    {
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

        doAuthenticatedCalls2();
    }

    private void doAuthenticatedCalls2()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(  "1) game...");
        buf.append("\n2) store...");
        buf.append("\n3) player...");
        buf.append("\n> ");
        String val = getConsoleInput(buf.toString());

        switch (val)
        {
            case "1":
                doGameCalls();
                break;

            case "2":
                doStoreCalls();
                break;

            case "3":
                doPlayerCalls();
                break;
        }
    }

    private void doGameCalls()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("\n1) join game");
        buf.append("\n2) leave game");
        buf.append("\n3) play next POOL round");
        buf.append("\n4) cancel POOL play");
        buf.append("\n5) begin BRACKET play");
        buf.append("\n> ");
        String val = getConsoleInput(buf.toString());

        String gameId = getConsoleInput("gameId: ");

        switch (val)
        {
            case "1": {
                _collector.joinGame(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        waitForJoinedGameSyncMessage(gameId);
                        doAuthenticatedCalls2();
                    }
                }), _authHeaders, gameId);
            }
            break;

            case "2": {
                _collector.leaveGame(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        doAuthenticatedCalls2();
                    }
                }), _authHeaders, gameId);
            }
            break;

            case "3": {
                _collector.beginPoolPlay(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        playRound(gameId);
                    }
                }), _authHeaders, gameId);
            }
            break;

            case "4": {
                _collector.cancelPoolPlay(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        doAuthenticatedCalls2();
                    }
                }), _authHeaders, gameId);
            }
            break;

            case "5": {
                _collector.beginBracketPlay(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        playRound(gameId);
                    }
                }), _authHeaders, gameId);
            }
            break;
        }
    }

    private void playRound(String gameId)
    {
        new PlayRound(_which, _subscriberId, _authHeaders, gameId).waitForJoinedRoundSyncMessage();
    }

    //begin checking the sync messages until a joined_game (for the given gameId) is located
    private void waitForJoinedGameSyncMessage(String gameId)
    {
        _logger.info("\n\nchecking for 'joined_game' sync message...");

        //grab the sync messages and find the most recent "joined_game" that matches the _gameId - this is the starting point
        List<SyncMessage> syncMessages = _syncService.getSyncMessages(_subscriberId, getLastSyncDate(gameId), gameId);

//        _logger.info("lastSyncDate: " + lastSyncDate + ", gameId: " + gameId + ", subscriberId: " + _subscriberId);
//        syncMessages.stream()
//            .sorted( (sm1, sm2) -> sm1.getCreateDate().compareTo(sm2.getCreateDate()) )
//            .forEach(sm -> _logger.info(sm));

        Optional<SyncMessage> oJoinedGameSyncMessage = syncMessages.stream()
            .filter(sm -> sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_JOINED_GAME.toString()))
            .filter(sm -> !ApiTest.getProcessedMessages(gameId).contains(sm))
            .sorted( (sm1, sm2) -> sm2.getCreateDate().compareTo(sm1.getCreateDate()) ) //since sm1, sm2 are reversed, it will be in reversed order (i.e. newest on top)
            .findFirst();

        //if there isn't one, wait a bit and try again
        if (!oJoinedGameSyncMessage.isPresent()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) { }
            waitForJoinedGameSyncMessage(gameId);
            return;
        }

        //save the sync message
        ApiTest.addProcessedMessage(gameId, oJoinedGameSyncMessage.get());

        _logger.info(MessageFormat.format("\n\nRECEIVED 'joined_game' for game: {0}\n\n", gameId));
    }

    private void doPlayerCalls()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("\n1) getPlayerDetails");
        buf.append("\n2) getPlayerGames");
        buf.append("\n3) getPlayerRounds");
        buf.append("\n> ");
        String val = getConsoleInput(buf.toString());

        switch (val)
        {
            case "1": {
                _collector.playerGetDetails(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        doAuthenticatedCalls2();
                    }
                }), _authHeaders);
            }
            break;

            case "2": {
                String filter = getConsoleInput("filter (OPEN, MYCLOSED): ");
                _collector.playerGetGames(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        doAuthenticatedCalls2();
                    }
                }), _authHeaders, filter);
            }
            break;

            case "3": {
                String gameId = getConsoleInput("gameId: ");
                _collector.playerGetRounds(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        doAuthenticatedCalls2();
                    }
                }), _authHeaders, gameId);
            }
            break;
        }
    }

    private void doStoreCalls()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("\n1) getClientToken");
        buf.append("\n2) getItems");
        buf.append("\n3) purchaseItem");
        buf.append("\n> ");
        String val = getConsoleInput(buf.toString());

        switch (val)
        {
            case "1": {
                _collector.storeGetClientToken(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {

                    @Override
                    public void run()
                    {
                        doAuthenticatedCalls2();
                    }
                }), _authHeaders);
            }
            break;

            case "2": {
                _collector.storeGetItems(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        doAuthenticatedCalls2();
                    }
                }), _authHeaders);
            }
            break;

            case "3": {
                String itemUuid = getConsoleInput("itemUuid: ");
                _collector.storePurchaseItem(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {

                    @Override
                    public void run()
                    {
                        doAuthenticatedCalls2();
                    }
                }), _authHeaders, itemUuid);
            }
            break;
        }
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
        ApiTest test = new ApiTest();

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

        //find out where to look for/store sync messages
        Properties props = DbProvider.getProperties();
        ApiTest._syncMessagesDir = props.getProperty("sync.message.storage.dir");

        //kick it off
        test.run(DbProvider.DB.LOCAL);
    }

}
