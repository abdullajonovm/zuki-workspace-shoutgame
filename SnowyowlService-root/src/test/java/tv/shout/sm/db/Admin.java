package tv.shout.sm.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.sm.admin.AdminGame;
import tv.shout.sm.admin.User;
import tv.shout.sm.admin.Users;
import tv.shout.sm.test.CertificateManager;
import tv.shout.sm.test.CollectorToWdsResponse;
import tv.shout.sm.test.ConsoleOutputWithRunnableDataReceiver;
import tv.shout.sm.test.JsonRunnable;
import tv.shout.sm.test.SRD;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class Admin
extends BaseDbSupport
implements Users
{
    private static Logger _logger = Logger.getLogger(Admin.class);

    private Map<String, String> _authHeaders;
    private DbProvider.DB _which;
    private User _user;

    private CollectorToWdsResponse _collector;

    private boolean _running;

    public Admin(DbProvider.DB which)
    throws Exception
    {
        super(which);
    }

    @Override
    public void init(DbProvider.DB which)
    throws Exception
    {
        _which = which;
        _collector = new CollectorToWdsResponse(which);
    }

    @Override
    public void run()
    throws Exception
    {
        //get which user will be making the calls
        String userEmails = Users.getUserEmails(_which);
        String firstEmail = userEmails.split(",")[0];
        String email = getConsoleInput("Use which user: " + userEmails + " > ["+firstEmail+"]");
        if (email.trim().equals("")) email = firstEmail;
        _user = Users.getUser(_which, email);

        //initialize the SRD
        SRD.initialize(_which, email);

        _authHeaders = new HashMap<>(2);
        _authHeaders.put("X-REST-SESSION-KEY", _user.sessionKey);
        _authHeaders.put("X-REST-DEVICE-ID", _user.deviceId);

        _running = true;

        while (_running) {
            final CountDownLatch cdl = new CountDownLatch(1);

            displayMainMenu(cdl);

            cdl.await();
        }
    }

    private class JsonGame
    {
        public String id;
        public Game.GAME_STATUS gameStatus;
        public Map<String, String> gameNames;
        public Set<String> allowableLanguageCodes;
    }

    private class JsonRound
    {
        public String id;
        public Round.ROUND_STATUS roundStatus;
        public int roundSequence;
        public Round.ROUND_TYPE roundType;
        public Map<String, String> roundNames;
    }

    private class ListGamesResult
    {
        public List<JsonGame> games;
    }

    private class GameResult
    {
        public JsonGame game;
    }

    private class ListRoundsResult
    {
        public List<JsonRound> rounds;
    }

    private class JsonCategoriesToIds
    {
        public List<JsonTuple<String>> result;
    }

    private class JsonTuple<T>
    {
        private T key;
        private T val;
    }
    private void displayMainMenu(final CountDownLatch cdl)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("\n\n");
        buf.append("\n0) list games");
        buf.append("\n1) open game");
        buf.append("\n2) list rounds");
        buf.append("\n3) start pool play");
        buf.append("\n4) start bracket play");
        buf.append("\n5) cancel game");
        buf.append("\n6) clone game");
        buf.append("\n7) create game");
        buf.append("\n8) create category");
        buf.append("\n9) upload photo");
        buf.append("\n> ");

        String val = getConsoleInput(buf.toString());

        switch (val)
        {
            case "0": {
                _collector.adminListGames(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable(false) {
                    @Override
                    public void run()
                    {
                        Gson gson = new Gson();
                        ListGamesResult result = gson.fromJson(json.toString(), ListGamesResult.class);
                        List<JsonGame> games = result.games;

                        _logger.info("             GAME NAME               STATUS                  GAME ID");
                        _logger.info("======================================================================================");

                        for (JsonGame game : games) {
                            String gameName = game.gameNames.get("en");
                            String gameStatus = game.gameStatus.toString();
                            String gameId = game.id;

                            StringBuilder buf = new StringBuilder();
                            int size = 35 - gameName.length();
                            for (int i=0; i<size; i++) {
                                buf.append(" ");
                            }
                            buf.append(gameName);

                            size = 10 - gameStatus.length();
                            for (int i=0; i<size; i++) {
                                buf.append(" ");
                            }
                            buf.append(gameStatus);
                            buf.append("     ").append(gameId);
                            _logger.info(buf.toString());
                        }

                        cdl.countDown();
                    }
                }), _authHeaders, Arrays.asList(Game.GAME_STATUS.PENDING, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));
            }
            break;

            case "1": {
                String gameId = getConsoleInput("gameId: ");
                _collector.adminOpenGame(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        cdl.countDown();
                    }
                }), _authHeaders, gameId);
            }
            break;

            case "2": {
                String gameId = getConsoleInput("gameId: ");
                _collector.adminGetGameRounds(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable(false) {
                    @Override
                    public void run()
                    {
                        Gson gson = new Gson();
                        ListRoundsResult result = gson.fromJson(json.toString(), ListRoundsResult.class);
                        List<JsonRound> rounds = result.rounds;

                        _logger.info("ROUND NAME          ROUND ID                              STATUS         SEQUENCE   TYPE");
                        _logger.info("==============================================================================================");

                        for (JsonRound round : rounds) {
                            String roundName = round.roundNames.get("en");
                            String roundId = round.id;
                            String roundStatus = round.roundStatus.toString();
                            int roundSequence = round.roundSequence;
                            String roundType = round.roundType.toString();

                            StringBuilder buf = new StringBuilder();
                            buf.append(roundName);
                            int size = 20 - roundName.length();
                            for (int i=0; i<size; i++) {
                                buf.append(" ");
                            }

                            buf.append(roundId).append("  ");

                            buf.append(roundStatus);
                            size = 15 - roundStatus.length();
                            for (int i=0; i<size; i++) {
                                buf.append(" ");
                            }

                            if (roundSequence < 10) {
                                buf.append(" ");
                            }
                            buf.append(roundSequence);

                            buf.append("          ").append(roundType);

                            _logger.info(buf.toString());
                        }

                        cdl.countDown();
                    }
                }), _authHeaders, gameId);
            }
            break;

            case "3": {
                String gameId = getConsoleInput("gameId: ");
                _collector.adminStartPoolPlay(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        cdl.countDown();
                    }
                }), _authHeaders, gameId);
            }
            break;

            case "4": {
                String gameId = getConsoleInput("gameId: ");
                long beginsInMs = Long.parseLong(getConsoleInput("beginsInMs: "));
                _collector.adminStartBracketPlay(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        cdl.countDown();
                    }
                }), _authHeaders, gameId, beginsInMs);
                return;
            }

            case "5": {
                String gameId = getConsoleInput("gameId: ");
                _collector.adminCancelGame(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        cdl.countDown();
                    }
                }), _authHeaders, gameId);
            }
            break;

            case "6": {
                String gameId = getConsoleInput("gameId: ");

                Date expectedStartDateForPoolPlay = null;
                while (expectedStartDateForPoolPlay == null) {
                    val = getConsoleInput("expected start date for pool play (yyyy-MM-dd kk:mm zz  --- ex: 2017-05-24 12:30 MST), ''=now: ");
                    if (val == null || val.trim().length() == 0) {
                        expectedStartDateForPoolPlay = new Date();
                    } else {
                        try {
                            expectedStartDateForPoolPlay = new SimpleDateFormat("yyyy-MM-dd kk:mm zz").parse(val);
                        } catch (ParseException e) {
                            _logger.warn("unparseable date. try again...");
                        }
                    }
                }
                final Date pp = expectedStartDateForPoolPlay;

                Date expectedStartDateForBracketPlay = null;
                while (expectedStartDateForBracketPlay == null) {
                    val = getConsoleInput("expected start date for bracket play (yyyy-MM-dd kk:mm zz  --- ex: 2017-05-24 12:30 MST), ''=now: ");
                    if (val == null || val.trim().length() == 0) {
                        expectedStartDateForBracketPlay = new Date();
                    } else {
                        try {
                            expectedStartDateForBracketPlay = new SimpleDateFormat("yyyy-MM-dd kk:mm zz").parse(val);
                        } catch (ParseException e) {
                            _logger.warn("unparseable date. try again...");
                        }
                    }
                }
                final Date bp = expectedStartDateForBracketPlay;

                _collector.adminGetGame(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable(false) {
                    @Override
                    public void run()
                    {
                        //grab the game
                        Gson gson = new Gson();
                        GameResult result = gson.fromJson(json.toString(), GameResult.class);
                        JsonGame game = result.game;

                        //for each language, get the new game name
                        Map<String, String> newGameNames = new HashMap<>();
                        for (String languageCode : game.allowableLanguageCodes) {
                            String newGameName = getConsoleInput("Game Name (" + languageCode + "): ");
                            newGameNames.put(languageCode, newGameName);
                        }

                        _collector.adminCloneGame(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                            @Override
                            public void run()
                            {
                                cdl.countDown();
                            }
                        }), _authHeaders, gameId, pp, bp, newGameNames);

                    }
                }), _authHeaders, gameId);
            }
            break;

            case "7": {
                //create game
                _logger.info("\n\n");
                _collector.adminGetQuestionCategoryIdsToKeys(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable(false) {
                    @Override
                    public void run()
                    {
                        //convert the server response into the reverse lookup map
                        Gson gson = new Gson();
                        JsonCategoriesToIds obj = gson.fromJson(json.toString(), JsonCategoriesToIds.class);

                        Map<String, String> questionCategoriesReverseMap = new HashMap<>();
                        for (JsonTuple<String> tuple : obj.result) {
                            //_logger.info(tuple.key + ": " + tuple.val);
                            questionCategoriesReverseMap.put(tuple.val, tuple.key);
                        }

                        boolean useTheUsualValues = "y".equals((getConsoleInput("Use the usual values (y/n)? ")));

                        //create the game
                        AdminGame game;
                        try {
                            game = SnowyOwlGameCreator.getGame(questionCategoriesReverseMap, useTheUsualValues);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        String val;

                        final Date pp;
                        final Date bp;
                        float minimumPayoutAmount;
                        int payoutModelId;

                        if (useTheUsualValues) {
                            pp = new Date();
                            bp = new Date();
                            minimumPayoutAmount = 1.00F;
                            payoutModelId = 1;

                        } else {
                            Date expectedStartDateForPoolPlay = null;
                            while (expectedStartDateForPoolPlay == null) {
                                val = getConsoleInput("expected start date for pool play (yyyy-MM-dd kk:mm zz  --- ex: 2017-05-24 12:30 MST), ''=now: ");
                                if (val == null || val.trim().length() == 0) {
                                    expectedStartDateForPoolPlay = new Date();
                                } else {
                                    try {
                                        expectedStartDateForPoolPlay = new SimpleDateFormat("yyyy-MM-dd kk:mm zz").parse(val);
                                    } catch (ParseException e) {
                                        _logger.warn("unparseable date. try again...");
                                    }
                                }
                            }
                            pp = expectedStartDateForPoolPlay;

                            Date expectedStartDateForBracketPlay = null;
                            while (expectedStartDateForBracketPlay == null) {
                                val = getConsoleInput("expected start date for bracket play (yyyy-MM-dd kk:mm zz  --- ex: 2017-05-24 12:30 MST), ''=now: ");
                                if (val == null || val.trim().length() == 0) {
                                    expectedStartDateForBracketPlay = new Date();
                                } else {
                                    try {
                                        expectedStartDateForBracketPlay = new SimpleDateFormat("yyyy-MM-dd kk:mm zz").parse(val);
                                    } catch (ParseException e) {
                                        _logger.warn("unparseable date. try again...");
                                    }
                                }
                            }
                            bp = expectedStartDateForBracketPlay;

                            minimumPayoutAmount = Float.parseFloat(getConsoleInput("minimumPayoutAmount: "));
                            payoutModelId = Integer.parseInt(getConsoleInput("payoutModelId: "));
                        }

                        //send to the server
                        _collector.adminGameCreate(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                            @Override
                            public void run()
                            {
                                cdl.countDown();
                            }
                        }), _authHeaders, game, pp, bp, minimumPayoutAmount, payoutModelId);
                    }
                }), _authHeaders);
            }
            break;

            //create category
            case "8": {
                String categoryKey = getConsoleInput("KEY: ");
                String categoryName = getConsoleInput("Name (en): ");

                ObjectMapper mapper = JsonUtil.getObjectMapper();
                Map<String,String> categoryNameMap = new FastMap<>("en", categoryName);
                Map<String, Object> categoryMap = new FastMap<>("categoryKey", categoryKey, "categoryName", categoryNameMap);
                try {
                    String categoryJson = mapper.writeValueAsString(categoryMap);

                    _collector.adminCreateCategory(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                        @Override
                        public void run()
                        {
                            cdl.countDown();
                        }
                    }), _authHeaders, categoryJson);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    _running = false;
                    cdl.countDown();
                }
            }
            break;

            //upload photo
            case "9": {
                String filename = getConsoleInput("filename (full path): ");

                _collector.uploadPhoto(new ConsoleOutputWithRunnableDataReceiver(new JsonRunnable() {
                    @Override
                    public void run()
                    {
                        cdl.countDown();

                    }
                }), filename);
            }
            break;

            default: {
                _logger.info("___DONE___");
                _running = false;
                cdl.countDown();
            }
        }

    }

    public static void main(String[] args)
    throws Exception
    {
        //CertificateManager.addTrustedCertificate("/Users/shawker/temp/jcert/nc10.shoutgameplay.com.crt", "/Users/shawker/temp/jcert/nc10.shoutgameplay.com.pem");
        CertificateManager.trustAllCertificates();

        String which = getConsoleInput("Which server?\n\n\t1)Local, 2)NC10-1, 3)NC11-1: ");
        switch (which)
        {
            case "1": new Admin(DbProvider.DB.LOCAL); break;
            case "2": new Admin(DbProvider.DB.NC10_1); break;
            case "3": new Admin(DbProvider.DB.NC11_1); break;
        }

    }

}
