package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.meinc.facebook.exception.FacebookAuthenticationNeededException;
import com.meinc.facebook.exception.FacebookGeneralException;
import com.meinc.facebook.exception.FacebookUserExistsException;
import com.meinc.facebook.exception.InvalidAccessTokenException;
import com.meinc.identity.domain.SignupData;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.exception.EmailAlreadyUsedException;
import com.meinc.identity.exception.InvalidEmailException;
import com.meinc.identity.exception.InvalidSessionException;
import com.meinc.identity.exception.NicknameInvalidException;
import com.meinc.identity.service.IIdentityService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_STATUS;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutModelRound.CATEGORY;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerBeginPoolPlay;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.snowyowl.service.QuestionHelper;
import tv.shout.util.FastMap;

public class TestBeginPoolPlay
{
    private static Logger _logger = Logger.getLogger(TestBeginPoolPlay.class);

    private MessageBus _messageBus;
    private IIdentityService _identityService;
    private IShoutContestService _shoutContestService;
    private MockSnowyowlDao _snowyowlDao;
    private MockGameStatsDao _gameStatsDao;

    final static ExecutorService threadPool = Executors.newFixedThreadPool(1);
    final static CountDownLatch cdl = new CountDownLatch(1);

    public TestBeginPoolPlay()
    {
        _messageBus = new MessageBus();
        _identityService = new MockIdentityService(new MockIdentityDao());
        _shoutContestService = new MockShoutContestService(new MockContestDao());
        _snowyowlDao = new MockSnowyowlDao();
        _gameStatsDao = new MockGameStatsDao();
    }

    public static void shutdown()
    {
        cdl.countDown();
    }

    public void start(Long twitchSubscriberId)
    {
        MockGameStatsHandler gameStatsHandler = new MockGameStatsHandler(_gameStatsDao);

        MockPayoutManager payoutManager = new MockPayoutManager(_snowyowlDao, _shoutContestService, _identityService);
        MockEngineCoordinator engineCoordinator = new MockEngineCoordinator(payoutManager);
        MockCurrentRankCalculator currentRankCalculator = new MockCurrentRankCalculator(_shoutContestService);
        MockCommonBusinessLogic commonBusinessLogic = new MockCommonBusinessLogic(_shoutContestService, _snowyowlDao, currentRankCalculator, engineCoordinator, _identityService, gameStatsHandler);

        MockBotEngine botEngine = new MockBotEngine(_messageBus, _snowyowlDao, _identityService, _shoutContestService, commonBusinessLogic);
        MockSponsorEngine sponsorEngine = new MockSponsorEngine(_messageBus, _snowyowlDao, commonBusinessLogic);
        MockQuestionSupplier questionSupplier = new MockQuestionSupplier(_snowyowlDao);
        MockSubscriberStatsHandler subscriberStatsHandler = new MockSubscriberStatsHandler();
        MockMMECommon mmeCommon = new MockMMECommon();

        commonBusinessLogic.setBotEngine(botEngine);
        MockMatchMaker matchMaker = new MockMatchMaker(_messageBus, _snowyowlDao, _identityService, _shoutContestService, botEngine, sponsorEngine);
        commonBusinessLogic.setMatchMaker(matchMaker);

        MockHandlerBeginPoolPlay beginPoolPlayHandler = new MockHandlerBeginPoolPlay(_messageBus, _snowyowlDao, _identityService, _shoutContestService, matchMaker, botEngine, sponsorEngine);
        MockHandlerBeginMatch beginMatchHandler = new MockHandlerBeginMatch(_messageBus, _snowyowlDao, _shoutContestService);
        MockHandlerSendQuestion sendQuestionHandler = new MockHandlerSendQuestion(_messageBus, _snowyowlDao, questionSupplier, _shoutContestService, botEngine, sponsorEngine, ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife);
        MockHandlerAnswer answerHandler = new MockHandlerAnswer(_messageBus, _snowyowlDao, _shoutContestService, commonBusinessLogic, botEngine, sponsorEngine);
        MockHandlerScoreQuestion scoreQuestionHandler = new MockHandlerScoreQuestion(
                _messageBus, _snowyowlDao, _shoutContestService, _identityService,
                questionSupplier, subscriberStatsHandler, currentRankCalculator,
                commonBusinessLogic, botEngine, sponsorEngine,
                10, 10, 10,
                5, 2, 0, 0,
                10_000L
        );
        MockHandlerScoreBracket scoreBracketHandler = new MockHandlerScoreBracket(_messageBus, _snowyowlDao, _identityService, _shoutContestService, subscriberStatsHandler, currentRankCalculator, matchMaker);
        MockHandlerBeginBracketPlay beginBracktePlayHandler = new MockHandlerBeginBracketPlay(_shoutContestService, commonBusinessLogic, matchMaker);
        MockHandlerDocPublisher docPublisherHandler = new MockHandlerDocPublisher(_messageBus, _snowyowlDao, _identityService, _shoutContestService, subscriberStatsHandler, mmeCommon);
        MockHandlerCloseGame closeGameHandler = new MockHandlerCloseGame(_messageBus, _snowyowlDao, _shoutContestService, _identityService, engineCoordinator);

        _messageBus.register(beginPoolPlayHandler);
        _messageBus.register(beginMatchHandler);
        _messageBus.register(sendQuestionHandler);
        _messageBus.register(answerHandler);
        _messageBus.register(scoreQuestionHandler);
        _messageBus.register(scoreBracketHandler);
        _messageBus.register(beginBracktePlayHandler);
        _messageBus.register(docPublisherHandler);
        _messageBus.register(closeGameHandler);
        _messageBus.start();
    }

    public void stop()
    {
        _messageBus.stop();
    }

    public Subscriber createSubscriber(String firstname, String lastname, String nickname, String email)
    {
        SignupData signupData = new SignupData();
        signupData.setAppName(ISnowyowlService.APP_ID);
        signupData.setPasswordSet(true);
        signupData.setLanguageCode("en");
        signupData.setFromCountryCode("US");
        signupData.setFirstName(firstname);
        signupData.setDateOfBirth(new Date());
        signupData.setPassword(UUID.randomUUID().toString());
        signupData.setEmail(email);
        signupData.setUsername(nickname);
        signupData.setLastName(lastname);

        Subscriber s = null;
        try {
            s = _identityService.signup(0, signupData, null);
        } catch (InvalidEmailException | EmailAlreadyUsedException | FacebookGeneralException
                | InvalidAccessTokenException | FacebookUserExistsException | InvalidSessionException
                | NicknameInvalidException | FacebookAuthenticationNeededException e) {
            _logger.error("unable to createSubscriber", e);
        }

        return s;
    }

    public void setSubscriberAsSponsorSubscriber(Subscriber s)
    {
        MockSponsorPlayer sponsorPlayer = new MockSponsorPlayer();
        sponsorPlayer.subscriberId = s.getSubscriberId();
        _snowyowlDao.addSponsorPlayer(sponsorPlayer);
    }

    public void populateDbWithQuestions()
    {
        String generalCategoryUuid = UUID.randomUUID().toString();

        Question q1 = new Question();
        q1.setId(UUID.randomUUID().toString());
        q1.setLanguageCodes(new HashSet<>(Arrays.asList("en")));
        q1.setForbiddenCountryCodes(new HashSet<>());
        q1.setQuestionCategoryUuids(new HashSet<>(Arrays.asList(generalCategoryUuid)));
        q1.setQuestionText(new FastMap<String, String>("en", "Question 1"));
        q1.setAnswers(Arrays.asList(
            new QuestionAnswer(UUID.randomUUID().toString(), new FastMap<String, String>("en", "Answer 1.1"), true),
            new QuestionAnswer(UUID.randomUUID().toString(), new FastMap<String, String>("en", "Answer 1.2"), false),
            new QuestionAnswer(UUID.randomUUID().toString(), new FastMap<String, String>("en", "Answer 1.3"), false),
            new QuestionAnswer(UUID.randomUUID().toString(), new FastMap<String, String>("en", "Answer 1.4"), false)
        ));

        Question q2 = new Question();
        q2.setId(UUID.randomUUID().toString());
        q2.setLanguageCodes(new HashSet<>(Arrays.asList("en")));
        q2.setForbiddenCountryCodes(new HashSet<>());
        q2.setQuestionCategoryUuids(new HashSet<>(Arrays.asList(generalCategoryUuid)));
        q2.setQuestionText(new FastMap<String, String>("en", "Question 2"));
        q2.setAnswers(Arrays.asList(
            new QuestionAnswer(UUID.randomUUID().toString(), new FastMap<String, String>("en", "Answer 2.1"), true),
            new QuestionAnswer(UUID.randomUUID().toString(), new FastMap<String, String>("en", "Answer 2.2"), false),
            new QuestionAnswer(UUID.randomUUID().toString(), new FastMap<String, String>("en", "Answer 2.3"), false),
            new QuestionAnswer(UUID.randomUUID().toString(), new FastMap<String, String>("en", "Answer 2.4"), false)
        ));

        _snowyowlDao.createQuestion(q1);
_logger.debug(MessageFormat.format("added question: {0}, answers: {1}", q1.getId(), q1.getAnswers()));
        _snowyowlDao.createQuestion(q2);
_logger.debug(MessageFormat.format("added question: {0}, answers: {1}", q2.getId(), q2.getAnswers()));

    }

    public void populateDbWithMultiLocalizationValues()
    {
        _snowyowlDao.insertOrReplaceMultiLocalizationValue("ee994158-0133-4bb7-af72-58e9176d567f", "systemMessage", "en", "You won a cash prize!");
        _snowyowlDao.insertOrReplaceMultiLocalizationValue("2454f9d3-c16e-4d13-bd4f-5e0ffa8203c0", "systemMessage", "en", "Congratulations! You won ${0,number,#,###.##}");
    }

    public static void PRINT_DB(IDaoMapper dao, String msg)
    {
        Question q;
        List<String> questionUuids = dao.getQuestionIdsBasedOnFiltersSansCategory(0, 10, "en");
        for (String questionUuid : questionUuids) {
            q = QuestionHelper.getQuestion(questionUuid, dao);
            _logger.info(MessageFormat.format(">>> {0}: retrieved qId: {1}, answers: {2}", msg, q.getId(), q.getAnswers()));
        }
    }

    public PayoutModel createPayoutModel()
    {
        PayoutModel pm = new PayoutModel();
        pm.setPayoutModelId(2);
        pm.setName("$2 Premium");
        pm.setBasePlayerCount(1048576);
        pm.setEntranceFeeAmount(2F);
        pm.setActive(true);
        pm.setCreatorId(0);
        pm.setCreateDate(new Date());
        pm.setPayoutModelRounds(Arrays.asList(
            createPayoutModelRound(pm,  0, "Champion",       1, 1, 1000000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm,  1, "Round 20",       2, 1,   10000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm,  2, "Round 19",       4, 2,    1000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm,  3, "Round 18",       8, 4,     500F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm,  4, "Round 17",      16, 8,     100F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm,  5, "Round 16",      32, 16,     50F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm,  6, "Round 15",      64, 32,     25F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm,  7, "Round 14",     128, 64,     20F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm,  8, "Round 13",     256, 128,    15F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm,  9, "Round 12",     512, 256,    10F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 10, "Round 11",    1024, 512,     9F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 11, "Round 10",    2048, 1024,    8F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 12,  "Round 9",    4096, 2048,    7F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 13,  "Round 8",    8192, 4096,    6F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 14,  "Round 7",   16384, 8192,    5F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 15,  "Round 6",   32768, 16384,   0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 16,  "Round 5",   65536, 32768,   0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 17,  "Round 4",  131072, 65536,   0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 18,  "Round 3",  262144, 131072,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 19,  "Round 2",  524288, 262144,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
            createPayoutModelRound(pm, 20,  "Round 1", 1048576, 524288,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL)
        ));

        _snowyowlDao.insertPayoutModel(pm);

        return pm;
    }

    private PayoutModelRound createPayoutModelRound(
        PayoutModel pm, int sortOrder, String description, int startingPlayerCount, int eliminatedPlayerCount, float eliminatedPayoutAmount, String type, CATEGORY category)
    {
        PayoutModelRound pmr = new PayoutModelRound();

        pmr.setPayoutModelId(pm.getPayoutModelId());
        pmr.setSortOrder(sortOrder);
        pmr.setDescription(description);
        pmr.setStartingPlayerCount(startingPlayerCount);
        pmr.setEliminatedPlayerCount(eliminatedPlayerCount);
        pmr.setEliminatedPayoutAmount(eliminatedPayoutAmount);
        pmr.setType(type);
        pmr.setCategory(category);
        pmr.setRoundNumber(sortOrder);

        return pmr;
    }

    public Game createGame(String gameName, boolean pairImmediately)
    {
        Game game = new Game();
        game.setId(UUID.randomUUID().toString());
        game.setGameStatus(Game.GAME_STATUS.PENDING);
        game.setGameEngine(ISnowyowlService.GAME_ENGINE);
        game.setEngineType(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife);
        game.setBracketEliminationCount(1);
        game.setPairImmediately(pairImmediately);
        game.setGameNames(new FastMap<String,String>("en", gameName));
        game.setAllowableLanguageCodes(new HashSet<>(Arrays.asList("en")));

        _shoutContestService.addGame(game);

        return game;
    }

    public Round addRoundToGame(
            String gameId, Round.ROUND_TYPE roundType, int roundSequence, int maxPlayerCount, int minQuestionsToWin, Integer maxQuestionsToAsk, int numSecondsBetweenQuestions, boolean finalRound)
    {
        Round round = new Round();
        round.setGameId(gameId);
        round.setId(UUID.randomUUID().toString());
        round.setRoundType(roundType);
        round.setRoundStatus(Round.ROUND_STATUS.PENDING);
        round.setRoundSequence(roundSequence);
        round.setMaximumPlayerCount(maxPlayerCount);
        round.setMinimumActivityToWinCount(minQuestionsToWin);
        round.setMaximumActivityCount(maxQuestionsToAsk);
        round.setDurationBetweenActivitiesSeconds(numSecondsBetweenQuestions);
        round.setFinalRound(finalRound);
        round.setCategories(new HashSet<>(Arrays.asList("*")));
        round.setPlayerMaximumDurationSeconds(10);
        round.setActivityMaximumDurationSeconds(10);
        round.setMatchPlayerCount(2);
        round.setRoundNames(new FastMap<String, String>("en", "Round " + roundSequence));

        _shoutContestService.addRound(round);

        return round;
    }

    public void createGamePayout(String gameId, float minimumPayoutAmount, int payoutModelId, boolean giveSponsorPlayerWinningsBackToSponsor)
    {
        GamePayout gamePayout = new GamePayout();
        gamePayout.setGameId(gameId);
        gamePayout.setMinimumPayoutAmount(minimumPayoutAmount);
        gamePayout.setPayoutModelId(payoutModelId);
        gamePayout.setGiveSponsorPlayerWinningsBackToSponsor(giveSponsorPlayerWinningsBackToSponsor);

        _snowyowlDao.addGamePayout(gamePayout);
    }

    public void setGameTwitchSubscriber(String gameId, long twitchSubscriberId)
    {
        _gameStatsDao.setGameStats(new GameStats(gameId).withTwitchConsoleFollowedSubscriberId(twitchSubscriberId));
    }

    public void openGame(String gameId)
    {
        _shoutContestService.openGame(gameId);
    }

    public void beginGamePoolPlay(String gameId)
    {
        _shoutContestService.getRoundsForGame(gameId).stream()
                .filter(r -> r.getRoundType() == Round.ROUND_TYPE.POOL)
                .forEach(r -> {
                    _shoutContestService.updateRoundStatus(r.getId(), r.isFinalRound(), Round.ROUND_STATUS.OPEN);
                });
    }

    public void joinGame(Game game, long subscriberId)
    {
        GamePlayer gamePlayer = new GamePlayer(game.getId(), subscriberId);

        //number of lives comes from two different sources, depending on the engineType
        switch (game.getEngineType())
        {
            case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife:
                if (game.getBracketEliminationCount() != null) {
                    gamePlayer.setCountdownToElimination(game.getBracketEliminationCount());
                }
                break;

            case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife:
                gamePlayer.setCountdownToElimination(game.getStartingLivesCount());
                break;
        }

        if (!_shoutContestService.addGamePlayer(gamePlayer, null, Round.ROUND_STATUS.VISIBLE, Round.ROUND_STATUS.OPEN, Round.ROUND_STATUS.FULL)) {
            _logger.error("no open rounds");
        }

    }

    public void playPoolRound(Game game, Round round, Subscriber subscriber, long pairingWaitTimeMs)
    {
        round.setCurrentPlayerCount(round.getCurrentPlayerCount() + 1);
        if (round.getCurrentPlayerCount() >= round.getMaximumPlayerCount() && round.getRoundStatus() == Round.ROUND_STATUS.OPEN) {
            round.setRoundStatus(ROUND_STATUS.FULL);
        }
        _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());

        //add the RoundPlayer record
        RoundPlayer roundPlayer = new RoundPlayer(game.getId(), round.getId(), subscriber.getSubscriberId());

        //if this is not the first round, move forward the rank values from the previous round
        if (round.getRoundSequence() != 1) {
            RoundPlayer oldRoundPlayer = _shoutContestService.getMostRecentRoundPlayer(game.getId(), subscriber.getSubscriberId());
            if (oldRoundPlayer != null) {
                roundPlayer.setRank(oldRoundPlayer.getRank());
                roundPlayer.setSkillAnswerCorrectPct(oldRoundPlayer.getSkillAnswerCorrectPct());
                roundPlayer.setSkillAverageAnswerMs(oldRoundPlayer.getSkillAverageAnswerMs());
                roundPlayer.setSkill(oldRoundPlayer.getSkill());
            }
        }

        _shoutContestService.addRoundPlayer(roundPlayer, round.getRoundType());

        //send a message to the bus
        _messageBus.sendMessage(HandlerBeginPoolPlay.createPlayPoolRoundMessage(game, round, subscriber.getSubscriberId(), roundPlayer.getId(), pairingWaitTimeMs));
    }

    /**
     * Player joins, waits for a bit, and gets paired with a bot.
     */
    public void scenario1(Game game, Round round, Subscriber subscriber)
    {
        MockHandlerScoreQuestion.incrementMatchResultCount();

        long pairingWaitTimeMs = 10_000L;
        playPoolRound(game, round, subscriber, pairingWaitTimeMs);
    }

    /**
     * Player joins,  game is set to pair immediately, and immediately gets paired with a bot.
     */
    public void scenario2(Game game, Round round, Subscriber subscriber)
    {
        MockHandlerScoreQuestion.incrementMatchResultCount();

        long pairingWaitTimeMs = 10_000L;
        game.setPairImmediately(true);
        playPoolRound(game, round, subscriber, pairingWaitTimeMs);
    }

    /**
     * Player 1 joins, waits a bit, player 2 joins, they are paired together.
     */
    public void scenario3(Game game, Round round, Subscriber s1, Subscriber s2)
    {
        MockHandlerScoreQuestion.incrementMatchResultCount();
        MockHandlerScoreQuestion.incrementMatchResultCount();

        long pairingWaitTimeMs = 10_000L;
        playPoolRound(game, round, s1, pairingWaitTimeMs);

        try {
            Thread.sleep(2_000L);
        } catch (InterruptedException ignored) {
        }
        playPoolRound(game, round, s2, pairingWaitTimeMs);
    }

    /**
     * Player 1 join, is the twitch subscriber, eventually gets paird with a bot
     */
    public void scenario4(Game game, Round round, Subscriber s)
    {
        MockHandlerScoreQuestion.incrementMatchResultCount();

        long pairingWaitTimeMs = 10_000L;
        playPoolRound(game, round, s, pairingWaitTimeMs);
    }

    /**
     * Player 1 joins, waits for a bit, player 2 joins, gets paired with 1. then player 3 joins, waits for a bit and gets paired with a bot.
     */
    public void scenario5(Game game, Round round, Subscriber s1, Subscriber s2, Subscriber s3)
    {
        MockHandlerScoreQuestion.incrementMatchResultCount();
        MockHandlerScoreQuestion.incrementMatchResultCount();
        MockHandlerScoreQuestion.incrementMatchResultCount();

        long pairingWaitTimeMs = 10_000L;
        playPoolRound(game, round, s1, pairingWaitTimeMs);

        try {
            Thread.sleep(2_000L);
        } catch (InterruptedException ignored) {
        }
        playPoolRound(game, round, s2, pairingWaitTimeMs);

        try {
            Thread.sleep(2_000L);
        } catch (InterruptedException ignored) {
        }
        playPoolRound(game, round, s3, pairingWaitTimeMs);
    }

    public static void main(String[] args)
    {
        Logger.getRootLogger().setLevel(Level.DEBUG);

        //instantiate all the mock objects and initialize the message bus
        TestBeginPoolPlay tester = new TestBeginPoolPlay();
        Subscriber twitchSubscriber = tester.createSubscriber("twitch", "subscriber", "twitchy", "twitchy@shout.tv");
_logger.debug("twitch subscriber id: " + twitchSubscriber.getSubscriberId());

        tester.populateDbWithQuestions();
        tester.populateDbWithMultiLocalizationValues();
_logger.debug("questions and multilocalizations added to database");

        //create some sponsor players that can be available to use
        Subscriber sponsorPlayer1 = tester.createSubscriber("sponsor", "one", "sponsorone", "sponsor1@shout.tv");
        Subscriber sponsorPlayer2 = tester.createSubscriber("sponsor", "two", "sponsortwo", "sponsor2@shout.tv");
        Subscriber sponsorPlayer3 = tester.createSubscriber("sponsor", "three", "sponsorthree", "sponsor3@shout.tv");
        tester.setSubscriberAsSponsorSubscriber(sponsorPlayer1);
        tester.setSubscriberAsSponsorSubscriber(sponsorPlayer2);
        tester.setSubscriberAsSponsorSubscriber(sponsorPlayer3);
_logger.debug("sponsor players added to database");

        //create the game (w/rounds)
        Game game = tester.createGame("test game 1", false);
        Round poolPlayRound = tester.addRoundToGame(game.getId(), Round.ROUND_TYPE.POOL, 1, 10, 2, 3, 10, false);
        /*Round bracketRound1 = */tester.addRoundToGame(game.getId(), Round.ROUND_TYPE.BRACKET, 2, 10, 2, 3, 10, true); //remaining bracket rounds are created dynamically when bracket play begins
        tester.setGameTwitchSubscriber(game.getId(), twitchSubscriber.getSubscriberId());
_logger.debug("game: " + game.getId() + ", round: " + poolPlayRound.getId());

        //the moola pieces needed for the end of the game
        PayoutModel pm = tester.createPayoutModel();
        tester.createGamePayout(game.getId(), 1.0F, pm.getPayoutModelId(), true);
_logger.debug("added payoutmodel and gamepayout");

        //open the game, and start pool play
        tester.openGame(game.getId());
        tester.beginGamePoolPlay(game.getId());
_logger.debug("game is opened, pool play has begun");

        //signup players
        Subscriber shawker = tester.createSubscriber("scott", "h", "shawker", "shawker@shout.tv");
        Subscriber bxgrant = tester.createSubscriber("bruce", "g", "bxgrant", "bxgrant@shout.tv");

        //have players join the game
        tester.joinGame(game, twitchSubscriber.getSubscriberId());
        tester.joinGame(game, shawker.getSubscriberId());
        tester.joinGame(game, bxgrant.getSubscriberId());

        //start the message bus and begin sending messages
        tester.start(twitchSubscriber.getSubscriberId());

        //run some tests
        threadPool.execute(() -> {
            //tester.scenario1(game, poolPlayRound, shawker);
            //tester.scenario2(game, poolPlayRound, shawker);
            //tester.scenario3(game, poolPlayRound, shawker, bxgrant);
            //tester.scenario4(game, poolPlayRound, twitchSubscriber);
            tester.scenario5(game, poolPlayRound, shawker, bxgrant, twitchSubscriber);
        });

        try {
            cdl.await();
        } catch (InterruptedException e) {
            _logger.error("cdl interrupted", e);
        }
        threadPool.shutdown();

        tester.stop();
    }

    //TODO:
    /*
     * don't forget when doing the question retrieval, to request decrypt key (update the question presented timestamp) and at the same time send this bus message:
     * _messageBus.sendDelayedMessage(HandlerAnswer.getAnswerTimeoutMessage(round.getPlayerMaximumDurationSeconds()*1_000L, sqa.getId()));
     *
     * make sure all the twitch socket.io messages are being sent at the right time
     *   _ TWITCH_QUESTION_TIMER_STARTED (when decrypt key is retrieved)
     *   X TWITCH_QUESTION_ANSWERED
     *   X TWITCH_MATCH_OVER
     *   X TWITCH_QUESTION
     *   X TWITCH_PAIRED
     *
     * don't forget that in the bot answer code, they send off a BOT_ANSWER message
     *
     * touchpoints for pairing needs to be changed in the existing code
     *
     * go through each handler and pull out any doc publishing code into the new doc publishing handler
     * when converting the rest of the code base, also consider pulling out any doc publishing code into handler messages
     */
}
