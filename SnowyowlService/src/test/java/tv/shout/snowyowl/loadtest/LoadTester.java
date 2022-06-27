package tv.shout.snowyowl.loadtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.fasterxml.jackson.databind.JsonNode;
import com.meinc.identity.domain.Subscriber;

import tv.shout.sc.domain.Round;
import tv.shout.sm.db.DbProvider;
import tv.shout.sm.test.CollectorToWdsResponse;
import tv.shout.sm.test.CollectorToWdsResponse.DataReceiver;
import tv.shout.sm.test.CollectorToWdsResponse.ERROR_TYPE;
import tv.shout.sm.test.CollectorToWdsResponse.REQUEST_TYPE;
import tv.shout.sm.test.SRD;

public class LoadTester
{
    private static Logger _logger = Logger.getLogger(LoadTester.class);
    static
    {
        //initialize the local logging to go to the console
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache", "debug");

        System.setProperty("log4j.defaultInitOverride", "true");
        Properties log4jProperties = new Properties();
        log4jProperties.setProperty("log4j.rootLogger", "INFO, stdout");
        log4jProperties.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        log4jProperties.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        log4jProperties.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d{MM/dd HH:mm:ss.SSS} %-5p [%c] %m%n");
        log4jProperties.setProperty("log4j.logger.org.apache.http.wire", "DEBUG");
        log4jProperties.setProperty("log4j.logger.org.apache.httpclient", "DEBUG");
        PropertyConfigurator.configure(log4jProperties);
    }

    private static final String EMAIL_TEMPLATE = "loadtester_{0,number,#}@shout.tv";
    private static final String NICKNAME_TEMPLATE = "__player_{0,number,#}";
    private static final String PASSWORD = "loadtester_pw";

    static final Map<String, String> correctAnswerMap = new HashMap<>();
    static final Map<String, String> incorrectAnswerMap = new HashMap<>();

    private void addSubscriberPlayer(final CountDownLatch cdl, final CollectorToWdsResponse collector, final int currentCount)
    throws URISyntaxException
    {
        String email = MessageFormat.format(EMAIL_TEMPLATE, (currentCount+1));
        String password = PASSWORD;
        String deviceId = UUID.randomUUID().toString();
        String firstname = "Load";
        String lastname = "Tester";
        String nickname = MessageFormat.format(NICKNAME_TEMPLATE, (currentCount+1));
        String countryCode = "US";
        String languageCode = "en";

        _logger.info("creating subscriber with nickname: " + nickname);
        collector.subscriberSignup(new DataReceiver() {
            @Override
            public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
            {
                cdl.countDown();
            }

            @Override
            public void dataCallbackFailure(
                REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode, String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
            {
                cdl.countDown();
                throw new IllegalStateException(MessageFormat.format("unable to do signup. error: {0}, httpResponseCode: {1}", errorType, httpResponseCode));
            }
        }, email, password, deviceId, firstname, lastname, nickname, countryCode, languageCode);
    }

//    private void login(final CountDownLatch cdl, final CollectorToWdsResponse collector, final BackdoorSqlGateway dao, final int currentCount)
//    {
//        String email = MessageFormat.format(EMAIL_TEMPLATE, (currentCount+1));
//        String password = PASSWORD;
//        String deviceId = dao.getDeviceIdForSubscriber(email);
//        String nickname = MessageFormat.format(NICKNAME_TEMPLATE, (currentCount+1));
//
//        _logger.info("doing subscriber login with nickname: " + nickname);
//        collector.subscriberLogin(new DataReceiver() {
//            @Override
//            public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
//            {
//                cdl.countDown();
//            }
//
//            @Override
//            public void dataCallbackFailure(REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode,
//                    String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
//            {
//                cdl.countDown();
//                throw new IllegalStateException(MessageFormat.format("unable to do login. error: {0}, httpResponseCode: {1}", errorType, httpResponseCode));
//            }
//        }, email, password, deviceId);
//    }

    static String getStoreItemUuid(double costToJoin, double balance)
    {
        double shortByAmount = costToJoin - balance;
        if (shortByAmount <= 1D) {
            return "e8056253-7c6c-11e7-970d-0242ac110004";
        } else if (shortByAmount <= 5D) {
            return "0b7c7d65-7c6d-11e7-970d-0242ac110004";
        } else if (shortByAmount <= 10D) {
            return "2252534e-7c6d-11e7-970d-0242ac110004";
        } else if (shortByAmount <= 20D) {
            return "a05ba3bf-9887-11e7-bc47-0242ac110008";
        } else if (shortByAmount <= 50D) {
            return "a05d16b9-9887-11e7-bc47-0242ac110008";
        } else {
            //assume $100. if it's more than that, then you shouldn't be using this game to test with
            return "a05d3bc1-9887-11e7-bc47-0242ac110008";
        }
    }

    //hack to work around a long-standing core java bug where dns lookups for domains with underscores don't work
    private static void forceAlsoAllowUriChars(String[] alsoAllowChars)
    {
        String allowCharsString = "-" + String.join("", alsoAllowChars);
        try {
            // Normally L_DASH and H_DASH class variables hold a numeric mask
            // for just the dash
            // character so as to permit its presence in a URI. Here we force
            // these variables to
            // also include other characters.
            setUriMask("L_DASH", "lowMask", allowCharsString);
            setUriMask("H_DASH", "highMask", allowCharsString);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            throw new IllegalStateException("Error forcing java.net.URI to behave", e);
        }
    }

    //hack to work around a long-standing core java bug where dns lookups for domains with underscores don't work
    private static void setUriMask(String maskFieldName, String charsToMaskMethodName, String newMaskChars)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException
    {
        Method maskMethod = URI.class.getDeclaredMethod(charsToMaskMethodName, String.class);
        maskMethod.setAccessible(true);
        long mask = (long) maskMethod.invoke(null, newMaskChars);

        Field maskField = URI.class.getDeclaredField(maskFieldName);

        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(maskField, maskField.getModifiers() & ~Modifier.FINAL);

        maskField.setAccessible(true);
        maskField.setLong(null, mask);
    }

    static String getConsoleInput(String message)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * A player config file can be specified. It will use the number of players equal to the number of rows in the file.
     * The file format is:
     * <pre>
     * ANSWER_SPEED,SKILL_LEVEL
     * </pre>
     * @param args
     * @throws Exception
     */
    public static void main(String[] args)
    throws Exception
    {
        //hack to work around a long-standing core java bug where dns lookups for domains with underscores don't work
        forceAlsoAllowUriChars(new String[] {"_"});

        LoadTester tester = new LoadTester();

        BackdoorSqlGateway dao = null;
        List<Round> rounds;
        String gameId;
        int numPlayers;
        CollectorToWdsResponse collector;

        //which server to use?
        String input = getConsoleInput("Which server?\n\n\t1)Local, 2)NC10-1, 3)NC11-1: ");
        DbProvider.DB which;
        String socketIoUrl;
        switch (input)
        {
            case "1":
                which = DbProvider.DB.LOCAL;
                socketIoUrl = "http://snowl-socketio--0--dev1.shoutgameplay.com:8080";
                break;
            case "2":
                which = DbProvider.DB.NC10_1;
                socketIoUrl = "http://snowl-socketio--0--nc10-1.shoutgameplay.com";
                break;
            case "3":
                which = DbProvider.DB.NC11_1;
                socketIoUrl = "http://snowl-socketio--0--nc11-1.shoutgameplay.com";
                break;
            default:
                throw new IllegalArgumentException("invalid input");
        }
        SRD.initialize(which, null);
        collector = new CollectorToWdsResponse(which);

        List<String> configFileData = null;
        String playerConfigFileLoc = getConsoleInput("player config file (''=use random values): ");
        if (!"".equals(playerConfigFileLoc)) {
            //https://stackoverflow.com/questions/26448352/counting-the-number-of-lines-in-a-text-file-java
            configFileData = Files.readAllLines(Paths.get(playerConfigFileLoc), Charset.defaultCharset());
            numPlayers = configFileData.size();
        } else {
            numPlayers = Integer.parseInt(getConsoleInput("# players: "));
        }

        try {
            //initialize the maps of correct/incorrect answers
            _logger.info("prefetching questions/answers and sorting into correct/incorrect answer maps...");
            dao = new BackdoorSqlGateway(which);
            dao.prefetchQuestionAnswers(correctAnswerMap, incorrectAnswerMap);

            //which game to play
            String defaultGameId = dao.getIdOfMostRecentOpenGame();
            String val = getConsoleInput(MessageFormat.format("gameId (\'\'\'\'={0}): ", defaultGameId));
            if ("".equals(val)) {
                gameId = defaultGameId;
            } else {
                gameId = val;
            }

            //get the rounds for the game (needed to initialize player objects)
            _logger.info("fetching rounds for game...");
            rounds = dao.getRoundsForGame(gameId);

            //create a lookup map of roundId to roundType for quick lookups later needed by the players
            Map<String, Round.ROUND_TYPE> roundToRoundTypeMap = new HashMap<>(rounds.size());
            rounds.forEach(round -> roundToRoundTypeMap.put(round.getId(), round.getRoundType()) );

            //get the player subscribers (needed to initialize player objects)
            _logger.info("fetching subscribers for game...");
            List<Subscriber> existingSubscriberPlayers = dao.getSubscriberPlayers(numPlayers);

            //create more player subscribers if necessary
            if (existingSubscriberPlayers.size() < numPlayers) {
                _logger.info(MessageFormat.format("creating {0} subscriber players...", (numPlayers - existingSubscriberPlayers.size()) ));
                int numToCreate = numPlayers - existingSubscriberPlayers.size();
                int existingCount = existingSubscriberPlayers.size();
                CountDownLatch cdlCreate = new CountDownLatch(numToCreate);
                for (int i=0; i<numToCreate; i++) {
                    //tester.addSubscriberPlayer(dao, existingCount);
                    tester.addSubscriberPlayer(cdlCreate, collector, existingCount);
                    existingCount++;
                }
                cdlCreate.await();
            }

//            //do a login for each subscriber, just to make sure they have a valid session for later
//            CountDownLatch cdlLogin = new CountDownLatch(numPlayers);
//            for (int i=0; i<numPlayers; i++) {
//                tester.login(cdlLogin, collector, dao, i);
//            }
//            cdlLogin.await();

            List<Subscriber> subscribers = dao.getSubscriberPlayers(numPlayers);

            _logger.info("signup/login phase complete");

            //create the player objects
            List<Player> players = new ArrayList<>(numPlayers);
            for (int i=0; i<numPlayers; i++) {
                Subscriber s = subscribers.get(i);
                Player.ANSWER_SPEED answerSpeed;
                Player.SKILL_LEVEL skillLevel;

                if (configFileData != null) {
                    //use config file data
                    String[] dat = configFileData.get(i).split(",");
                    answerSpeed = Player.ANSWER_SPEED.valueOf(dat[0]);
                    skillLevel = Player.SKILL_LEVEL.valueOf(dat[1]);

                } else {
                    //use random values
                    answerSpeed = Player.ANSWER_SPEED.values()[new Random().nextInt(Player.ANSWER_SPEED.values().length)];
                    skillLevel = Player.SKILL_LEVEL.values()[new Random().nextInt(Player.SKILL_LEVEL.values().length)];
                }

                Player p = Player.create(which, collector, dao, rounds, roundToRoundTypeMap, s, answerSpeed, skillLevel);
_logger.info("PLAYER: " + p);
                players.add(p);
            }

            //make sure each player has enough credits to join the game
            _logger.info("ensuring each player has enough credit to join the game...");
            double costToJoin = dao.getCostToJoinGame(gameId);
            if (costToJoin > 0) {
                CountDownLatch cdlPurchase = new CountDownLatch(numPlayers);
                players.forEach(player -> player.ensureBalanceCanCoverEntranceFee(cdlPurchase, costToJoin));
                cdlPurchase.await();
            }

            _logger.info("balance check/purchase phase complete");

            //now that setup is complete, start up each of the players and wait for them to finish
            CountDownLatch cdl = new CountDownLatch(numPlayers);
            CountDownLatch cdlStart = new CountDownLatch(numPlayers); // to know when the "start" has been processed

            _logger.info("starting all players...");
            //socket.io only seems to work with up to 4 sockets on a local machine, so do 4 as socket.io and the rest as polling
            for (int i=0; i<numPlayers; i++) {
                if (i<4) {
                    players.get(i).start(cdl, cdlStart).withProviderSocketIo(socketIoUrl);
                } else {
                    players.get(i).start(cdl, cdlStart).withProviderPolling(gameId);
                }
            }
            cdlStart.await();

            val = getConsoleInput("\njoin game (y/n) (only join on first pool play round, or if this is a bracket-only game)? ");
            if ("y".equals(val)) {
                _logger.info("all players now joining the game...");
                CountDownLatch cdlJoin = new CountDownLatch(numPlayers);
                players.forEach(player -> player.joinGame(gameId, cdlJoin));
                cdlJoin.await();
            }

            val = getConsoleInput("\nplay (p)ool round, or wait for (b)racket play to begin? ");
            switch (val)
            {
                case "p":
                    CountDownLatch cdlPool = new CountDownLatch(numPlayers);
                    players.forEach(player -> player.beginPoolPlay(gameId, cdlPool));
                    cdlPool.await();
                    break;

                case "b":
                default:
                    players.forEach(player -> player.beginBracketPlay(gameId));
                    break;
            }

            _logger.info("\nwaiting for players to complete...");
            cdl.await();
            _logger.info("\nall players have completed");

        } finally {
            if (dao != null) {
                dao.stop();
            }
        }
    }

}
