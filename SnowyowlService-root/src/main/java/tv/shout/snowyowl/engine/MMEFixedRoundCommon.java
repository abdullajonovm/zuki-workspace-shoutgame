package tv.shout.snowyowl.engine;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meinc.identity.domain.Subscriber;
import com.meinc.jdbc.effect.TransactionSideEffectManager;
import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.common.SubscriberStatsHandler;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.snowyowl.service.SnowyowlService;
import tv.shout.util.FastMap;
import tv.shout.util.MaxSizeHashMap;

public abstract class MMEFixedRoundCommon
extends BaseEngine
implements MME
{
    private static Logger _logger = Logger.getLogger(MMEFixedRoundCommon.class);

    private static final long SLEEP_TIME_MS = 1_000L;
    private enum TIMEOUT_REASON {NONE, NO_DECRYPT_REQUEST, NO_ANSWER, SLOW_ANSWER}

    @Autowired
    protected ITriggerService _triggerService;

    @Autowired
    protected QuestionSupplier _questionSupplier;

    @Autowired
    protected MMECommon _mmeCommon;

    @Autowired
    protected SubscriberStatsHandler _subscriberStatsHandler;

    @Value("${sm.substats.WON_CORRECT}")
    protected int _cumulativeScore_WON_CORRECT;

    @Value("${sm.substats.WON_TIME}")
    protected int _cumulativeScore_WON_TIME;

    @Value("${sm.substats.WON_TIMEOUT}")
    protected int _cumulativeScore_WON_TIMEOUT;

    @Value("${sm.substats.LOST_TIME}")
    protected int _cumulativeScore_LOST_TIME;

    @Value("${sm.substats.LOST_INCORRECT}")
    protected int _cumulativeScore_LOST_INCORRECT;

    @Value("${sm.substats.LOST_TIMEOUT}")
    protected int _cumulativeScore_LOST_TIMEOUT;

    @Value("${sm.substats.LOST_ALL_TIMEOUT}")
    protected int _cumulativeScore_LOST_ALL_TIMEOUT;

    protected static final DefaultTransactionDefinition txDefNested = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);

//  private Lock _matchIdLock = new ReentrantLock(); //TODO: put back?
    protected Set<String> _currentlyProcessingMatchIds = new HashSet<>();

    protected Lock _lock = new ReentrantLock();
    private boolean _runnerGracefulInterrupt;
    private MMESanityChecker _sanityChecker;
    private ArrayBlockingQueue<MMEProcess> _workQueue = new ArrayBlockingQueue<>(999);
    private MMERunner _runner;
    private boolean _running;

    public abstract Match.MATCH_STATUS[] getSupportedMatchStatuses();
    public abstract RMEFixedRoundCommon getRME();
    public abstract void scoreQuestion(Game game, Round round, Match match, MatchQuestion matchQuestion, List<SubscriberQuestionAnswer> sqas, boolean isQuestionTimedOut, List<Long> botsInGame);

    private Map<String, Game> _questionGameNoFatCache = new MaxSizeHashMap<String, Game>().withMaxSize(5);
    protected Map<String, List<MatchQuestion>> _matchQuestionCache = new HashMap<>();

    //the previously asked question is complete and has been scored. this will determine if another question needs to be asked, or if the match is over
    public abstract void processMatch(Round round, Match match, List<MatchQuestion> matchQuestions, Long twitchSubscriberId);

    //this should be called anytime that the list of published questions changes in the database
    @Override
    public void notifyQuetionListChanged()
    {
        _questionSupplier.notifyQuetionListChanged();
    }

    @Override
    public void killProcess(String id)
    {
        _mmeCommon.killProcess(id);
    }

    //must be called when service starts
    @Override
    public void start(Socket socketIoSocket)
    {
        if (_runner == null) {
            _runner = new MMERunner();
            _runner.setDaemon(true);
            _runner.start();
        }

        if (_sanityChecker == null) {
            _sanityChecker = new MMESanityChecker();
            _sanityChecker.setDaemon(true);
            _sanityChecker.start();
        }

        _socketIoSocket = socketIoSocket;

        _mmeCommon.loadState(this::process);
    }

    //must be called when service stops
    @Override
    public void stop()
    {
        _runnerGracefulInterrupt = true;

        if (_runner != null) {
            _runner.interrupt();
        }

        if (_sanityChecker != null) {
            _sanityChecker.interrupt();
        }
    }

    //the MME has been told to run
    @Override
    public void run(String id, String gameId, boolean isBracket)
    {
        MMEProcess proc = new MMEProcess(id, gameId, isBracket);

        _lock.lock();
        try {
            _mmeCommon.getProcessingIds().add(proc);
            _mmeCommon.saveState();
        } finally {
            _lock.unlock();
        }

        _workQueue.add(proc);
    }

    protected String getCorretAnswerId(String questionId)
    {
        //check the cache first
        String cai = _questionSupplier.getQuestionCorrectAnswerMap().get(questionId);
        if (cai == null) {
            QuestionAnswer correctQuestionAnswer = _dao.getQuestionAnswersForQuestion(questionId).stream()
                .filter(a -> a.getCorrect())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("attempting to score question that does not have a correct answer!"));
            cai = correctQuestionAnswer.getId();
        }
        return cai;
    }

    private class MMESanityChecker
    extends Thread
    {
        @Override
        public void run()
        {
            try {
                while (!isInterrupted()) {
                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        StringBuilder buf = new StringBuilder();

                        Set<MMEProcess> processingIds = _mmeCommon.getProcessingIds();
                        if (processingIds.size() > 0) {
                            _lock.lock();
                            try {
                                buf.append("MME: sanity check: there are ");
                                buf.append(processingIds.size());
                                buf.append(" items being processed");

                                for (MMEProcess process : processingIds) {
                                    buf.append("\n\t");
                                    buf.append(process.id);
                                    buf.append(": running for: ");

                                    long runningTime = System.currentTimeMillis() - process.time;
                                    buf.append(runningTime / 60_000L);
                                    buf.append(" min");

                                    //if something's been running for more than 8 minutes, there is most likely a problem
                                    if (runningTime > 60_000L * 8 && runningTime < 60_000L * 9.5) {
                                        if (_logger.isDebugEnabled()) {
                                            _logger.debug(MessageFormat.format("MMESanityChecker, process {0} [{1}] has been running for {2} ms", process.id, process.isBracket ? "bracket" : "pool", runningTime));
                                        }
                                    }
                                }

                            } finally {
                                _lock.unlock();
                            }
                            SnowyowlService.SUB_EVENT_LOGGER.debug(buf.toString());
                        }
                    }
                    Thread.sleep(60_000L);
                }
            } catch (InterruptedException e) {
                if (!_runnerGracefulInterrupt) {
                    _logger.error("MME sanity check was interrupted!", e);
                }
            }
        }
    }

    private class MMERunner
    extends Thread
    {
        public MMERunner()
        {
            super("MMERunner");
        }

        @Override
        public void run()
        {
            try {
                while (!isInterrupted()) {
                    MMEProcess processToRun = _workQueue.take(); //block until input is available

                    try {
                        process(processToRun);
                    } catch (Throwable e) {
                        _logger.error("MME: uncaught exception while processing", e);
                    }
                }
            } catch (InterruptedException e) {
                if (!_runnerGracefulInterrupt) {
                    _logger.error("MMERunner was interrupted. work queue no longer being processed!", e);
                }
            } catch (Throwable e) {
                _logger.error("MME: uncaught exception. unable to recover", e);
            }
        }
    }

    private void process(MMEProcess processToRun)
    {
        _lock.lock();
        try {
            _running = true;
        } finally {
            _lock.unlock();
        }

        //the bots/sponsors in a game don't change from round to round, so cache them once through
        final Map<String, List<Long>> cachedBotsForGame = new HashMap<>();
        final Map<String, List<Long>> cachedSponsorsForGame = new HashMap<>();

        //many (if not all) of the matches being processed will belong to the same round, so cache it/them
        final Map<String, Round> cachedRounds = new HashMap<>();

        final Map<String, Game> cachedGames = new HashMap<>();

        final Map<String, Long> cachedTwitchSubscribers = new HashMap<>();

        while (_running) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("MME running...");
            }

            final List<Match> filteredMatches;

            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
            try {
                //find all matches that currently need to be processed
                List<Match> matches = _shoutContestService.getMatchesByEngineAndStatus(ISnowyowlService.GAME_ENGINE, getType(), getSupportedMatchStatuses());
_logger.info(MessageFormat.format("there are {0} matches ready to be processed", matches.size()));

//                //filter out any matches currently being processed
//                _matchIdLock.lock();
//                try {
                    filteredMatches = matches.stream().filter(m -> ! _currentlyProcessingMatchIds.contains(m.getId())).collect(Collectors.toList());

                    //any matches that are left are now being processed. mark as such
                    filteredMatches.stream().forEach(m -> _currentlyProcessingMatchIds.add(m.getId()));

//                } finally {
//                    _matchIdLock.unlock();
//                }

                ensureCacheIsPopulated(filteredMatches, cachedGames, cachedRounds, cachedBotsForGame, cachedSponsorsForGame, cachedTwitchSubscribers);

                _transactionManager.commit(txStatus);
                txStatus = null;

            } finally {
                if (txStatus != null) {
                    _transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }

_logger.info("about to process " + filteredMatches.size() + " matches");

//TODO: put back? consider a different approach - coroutines (via kotlin perhaps?)
            //process the matches in parallel
//            final CountDownLatch cdl = new CountDownLatch(filteredMatches.size());
//            final ExecutorService threadPool = Executors.newFixedThreadPool(CONCURRENT_THREAD_POOL_SIZE);
            for (int i=0; i<filteredMatches.size(); i++) {
                Match match = filteredMatches.get(i);
                Round round = cachedRounds.get(match.getRoundId());
                List<Long> botsInGame = cachedBotsForGame.get(round.getGameId());
                List<Long> sponsorsInGame = cachedSponsorsForGame.get(round.getGameId());

//                try {
//                    threadPool.execute(() -> asyncProcessLoop(match, round, botsInGame));
//                } finally {
//                    cdl.countDown();
//                }

                asyncProcessLoop(cachedGames.get(match.getGameId()), match, round, botsInGame, sponsorsInGame, cachedTwitchSubscribers.get(match.getGameId()));

                //every 100 matches, sleep for a bit to reduce the size of the database transaction wave
                if (i % 100 == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
            }

//            //wait until all processing threads have completed
//            try {
//                cdl.await();
//            } catch (InterruptedException e) {
//                _logger.error("interrupted while processing matches", e);
//            }
//            threadPool.shutdown();

            doPostProcessRoundProcessing(cachedRounds);

            //see if the loop should run again
            boolean runAfterLock = false;
            _lock.lock();
            try {
                if (_mmeCommon.getProcessingIds().size() > 0) {
                    runAfterLock = true;
                } else {
                    _running = false;
                }
            } finally {
                _lock.unlock();
            }
            if (runAfterLock) {
                try {
                    Thread.sleep(SLEEP_TIME_MS);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void ensureCacheIsPopulated(
            final List<Match> filteredMatches, final Map<String, Game> cachedGames, final Map<String, Round> cachedRounds,
            final Map<String, List<Long>> cachedBotsForGame, final Map<String, List<Long>> cachedSponsorsForGame,
            final Map<String, Long> cachedTwitchSubscribers)
    {
        //make sure the rounds and bots are pre-cached
        for (int i=0; i<filteredMatches.size(); i++) {
            Match match = filteredMatches.get(i);

            //get the round associated with this match
            Round round;
            if (cachedRounds.containsKey(match.getRoundId())) {
                round = cachedRounds.get(match.getRoundId());
            } else {
                round = _shoutContestService.getRound(match.getRoundId());
                cachedRounds.put(match.getRoundId(), round);
            }

            //get the bots for this game
            List<Long> botsInGame;
            if (cachedBotsForGame.get(round.getGameId()) != null) {
                ; //already cached
            } else {
                botsInGame = _botEngine.getBotsForGame(round.getGameId());
                cachedBotsForGame.put(round.getGameId(), botsInGame);
            }

            //get the sponsors for this game
            List<Long> sponsorsInGame;
            if (cachedSponsorsForGame.get(round.getGameId()) != null) {
                ; //already cached
            } else {
                sponsorsInGame = _sponsorEngine.getSponsorsForGame(round.getGameId());
                cachedSponsorsForGame.put(round.getGameId(), sponsorsInGame);
_logger.info(">>> caching " + sponsorsInGame.size() + " sponsors for game: " + round.getGameId());
            }

            //make sure the game is cached as well (basic info, doesn't need to be "fattened")
            Game game;
            if (cachedGames.containsKey(match.getGameId())) {
                ;
            } else {
                game = _shoutContestService.getGameNoFat(match.getGameId());
                cachedGames.put(match.getGameId(), game);

                //see if there is a twitch subscriber being followed for this game
                Long twitchSubscriberId = null;
                GameStats gameStats = _dao.getGameStats(game.getId());
                if (gameStats != null) {
                    twitchSubscriberId = gameStats.getTwitchConsoleFollowedSubscriberId();
                    cachedTwitchSubscribers.put(game.getId(), twitchSubscriberId);
                }
            }
        }

        //the _proecssingIds might also be a roundId, and they need to be cached as well for the bracket completion logic below to work
        for (MMEProcess process : _mmeCommon.getProcessingIds()) {
            if (process.isBracket) {
                if (!cachedRounds.containsKey(process.id)) {
                    Round round = _shoutContestService.getRound(process.id);
                    cachedRounds.put(process.id, round);
                }
            }
        }
    }

    private void doPostProcessRoundProcessing(final Map<String, Round> cachedRounds)
    {
        //find any bracket rounds being processed
        for (String roundId : cachedRounds.keySet()) {
            Round round = cachedRounds.get(roundId);

            if (round.getRoundType() == Round.ROUND_TYPE.BRACKET) {
                int matchesNotYetProcessingForRound;
                List<Match> matchesNotYetProcessingForRoundList =
                    _shoutContestService.getMatchesByRoundAndStatus(
                        roundId,
                        getSupportedMatchStatuses());

                matchesNotYetProcessingForRound = matchesNotYetProcessingForRoundList.size();

                //publish how many matches are still outstanding for this round
                /*int totalMatchesForRound = */_mmeCommon.publishBracketOutstandingMatchCount(round, matchesNotYetProcessingForRound, _socketIoSocket, _triggerService);

                if (matchesNotYetProcessingForRound == 0) {
                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "MME: all matches for BRACKET round {0} have completed",
                                roundId));
                    }

                    //remove this match from the process loop so the engine will stop if there are no other rounds being processed
                    _lock.lock();
                    try {
                        _mmeCommon.getProcessingIds().remove(new MMEProcess(round.getId(), null));
                        _mmeCommon.saveState();
                    } finally {
                        _lock.unlock();
                    }

                    //start the RME to process all of these matches and close out the round (and possibly the game)
                    //this needs to happen AFTER the current transaction commits since causing RME to run is on a different thread and it's a race condition
                    // who happens first: does this transaction commit first, or does the RME thread start and do a db query first? If it's the later, then
                    // it won't find this match in the processing state, and it will skip it
                    //TransactionSideEffectManager.runAfterThisTransactionCommit(() -> ((VariableRoundRME)_roundManagementEngine).run() );
                    getRME().run();
                }
            }
        }
    }

    private void asyncProcessLoop(
            Game game, Match match, Round round, List<Long> botsInGame, List<Long> sponsorsInGame, Long twitchSubscriberId)
    {
//_logger.info(MessageFormat.format(">>> asyncProcessLoop for match: {0}, STATE: {1}, setAt: {2,date,yyyy-MM-dd hh:mm:ss.SSS}", match.getId(), match.getMatchStatus(), match.getMatchStatusSetAt()));
        try {
            switch (match.getMatchStatus())
            {
                case NEW:
                    //see if it's time to send the next question
                    long sendFirstQuestionAt = match.getCreateDate().getTime() + round.getDurationBetweenActivitiesSeconds() * 1_000L;
                    if (System.currentTimeMillis() >= sendFirstQuestionAt) {
                        processMatchNew(round, match, botsInGame, sponsorsInGame, twitchSubscriberId);
                    }
                    break;

                case OPEN:
                    processMatchOpen(game, round, match, botsInGame, twitchSubscriberId);
                    break;

                case WAITING_FOR_NEXT_QUESTION:
                    //see if it's time to send the next question
                    if ( match.getSendNextQuestionAt() == null || System.currentTimeMillis() >= match.getSendNextQuestionAt().getTime() ) {
                        processMatchWaitingForNextQuestion(round, match, botsInGame, sponsorsInGame, twitchSubscriberId);
                    }
                    break;

                case WAITING_FOR_TIEBREAKER_QUESTION:
                    //see if it's time to send the next question
                    if ( match.getSendNextQuestionAt() == null || System.currentTimeMillis() >= match.getSendNextQuestionAt().getTime() ) {
                        processMatchWaitingForTiebreakerQuestion(round, match, botsInGame, sponsorsInGame, twitchSubscriberId);
                    }
                    break;

                default:
                    //no-op
                    break;
            }

        } catch (Exception e) {
            _logger.error(e.getMessage(), e);

        } finally {
            //processing is done for this match
//            _matchIdLock.lock();
//            try {
                _currentlyProcessingMatchIds.remove(match.getId());
//            } finally {
//                _matchIdLock.unlock();
//            }

        }
    }

    private void processMatchNew(
            Round round, Match match, List<Long> botsInGame, List<Long> sponsorsInGame, Long twitchSubscriberId)
    {
long b = System.currentTimeMillis();
        TransactionStatus txStatus = _transactionManager.getTransaction(txDefNested);
        try {
            //change status to OPEN and assign the first question
            match.setMatchStatus(Match.MATCH_STATUS.OPEN);
            match.setMatchStatusSetAt(new Date());
            _shoutContestService.updateMatch(match);
            addQuestionToMatch(round, match, botsInGame, sponsorsInGame, false, twitchSubscriberId);

            _transactionManager.commit(txStatus);
            txStatus = null;

        } catch (NoQuestionFoundException e) {
            //BAD BAD BAD
            match.setMatchStatus(Match.MATCH_STATUS.CANCELLED);
            match.setMatchStatusSetAt(new Date());
            _shoutContestService.updateMatch(match);

            _transactionManager.commit(txStatus);
            txStatus = null;

        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }
long e = System.currentTimeMillis();
long dur = (e-b);
if (dur > 1_000L) {
    _logger.info(MessageFormat.format(">>> 'NEW' match loop is over. process time: {0} ms", dur));
}
    }

    private void processMatchOpen(Game game, Round round, Match match, List<Long> botsInGame, Long twitchSubscriberId)
    {
long b = System.currentTimeMillis();
        TransactionStatus txStatus = _transactionManager.getTransaction(txDefNested);
        try {
            //see if there is an OPEN question for this match, and if so, has it timed out?
            // if so, the match is over, otherwise continue with the match processing logic
            boolean isMatchQuestionComplete = false;

            List<MatchQuestion> matchQuestions = _matchQuestionCache.containsKey(match.getId()) ? _matchQuestionCache.get(match.getId()) : _dao.getMatchQuestionsForMatch(match.getId());
            Optional<MatchQuestion> oMatchQuestion = matchQuestions.stream()
                .filter(mq -> mq.getMatchQuestionStatus() == Match.MATCH_STATUS.OPEN).findFirst();
            if (oMatchQuestion.isPresent()) {
                isMatchQuestionComplete = processMatchQuestion(game, round, match, oMatchQuestion.get(), botsInGame);
            }

            if (isMatchQuestionComplete) {
                processMatch(round, match, matchQuestions, twitchSubscriberId);
            }

            _transactionManager.commit(txStatus);
            txStatus = null;

        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }
long e = System.currentTimeMillis();
long dur = (e-b);
if (dur > 1_000L) {
    _logger.info(MessageFormat.format(">>> 'OPEN' match loop is over. process time: {0} ms", dur));
}
    }

    private void processMatchWaitingForNextQuestion(
            Round round, Match match, List<Long> botsInGame, List<Long> sponsorsInGame, Long twitchSubscriberId)
    {
long b = System.currentTimeMillis();
        TransactionStatus txStatus = _transactionManager.getTransaction(txDefNested);
        try {
            match.setMatchStatus(Match.MATCH_STATUS.OPEN);
            match.setMatchStatusSetAt(new Date());
            _shoutContestService.updateMatch(match);
            addQuestionToMatch(round, match, botsInGame, sponsorsInGame, false, twitchSubscriberId);

            _transactionManager.commit(txStatus);
            txStatus = null;

        } catch (NoQuestionFoundException e) {
            //BAD BAD BAD
            match.setMatchStatus(Match.MATCH_STATUS.CANCELLED);
            match.setMatchStatusSetAt(new Date());
            _shoutContestService.updateMatch(match);

            _transactionManager.commit(txStatus);
            txStatus = null;

        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }
long e = System.currentTimeMillis();
long dur = (e-b);
if (dur > 1_000L) {
    _logger.info(MessageFormat.format(">>> 'WAITING_FOR_NEXT_QUESTION' match loop is over. process time: {0} ms", dur));
}
    }

    private void processMatchWaitingForTiebreakerQuestion(
            Round round, Match match, List<Long> botsInGame, List<Long> sponsorsInGame, Long twitchSubscriberId)
    {
long b = System.currentTimeMillis();
        TransactionStatus txStatus = _transactionManager.getTransaction(txDefNested);
        try {
            match.setMatchStatus(Match.MATCH_STATUS.OPEN);
            match.setMatchStatusSetAt(new Date());
            _shoutContestService.updateMatch(match);
            addQuestionToMatch(round, match, botsInGame, sponsorsInGame, true, twitchSubscriberId);

            _transactionManager.commit(txStatus);
            txStatus = null;

        } catch (NoQuestionFoundException e) {
            //BAD BAD BAD
            match.setMatchStatus(Match.MATCH_STATUS.CANCELLED);
            match.setMatchStatusSetAt(new Date());
            _shoutContestService.updateMatch(match);

            _transactionManager.commit(txStatus);
            txStatus = null;

        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }
long e = System.currentTimeMillis();
long dur = (e-b);
if (dur > 1_000L) {
    _logger.info(MessageFormat.format(">>> 'WAITING_FOR_TIEBREAKER_QUESTION' match loop is over. process time: {0} ms", dur));
}
    }

    private void addQuestionToMatch(
            Round round, Match match, List<Long> botsInGame, List<Long> sponsorsInGame, boolean isTieBreaker, Long twitchSubscriberId)
    throws NoQuestionFoundException
    {
//_logger.info(MessageFormat.format(">>> addQuestionToMatch, tiebreaker: #{0}", isTieBreaker));
//long b1 = System.currentTimeMillis();
        //will need the game and the players in the match to gather needed filter criteria when choosing a question
        Game game = _questionGameNoFatCache.get(round.getGameId());
        if (game == null) {
            game = _shoutContestService.getGameNoFat(round.getGameId());
            _questionGameNoFatCache.put(round.getGameId(), game);
        }

//long b2 = System.currentTimeMillis();
//long d1 = (b2-b1);
        List<MatchPlayer> matchPlayers = _shoutContestService.getMatchPlayersForMatch(match.getId());
//long b3 = System.currentTimeMillis();
//long d2 = (b3-b2);

        //other filter criteria from data already at hand
        Set<String> roundCategoryUuids = round.getCategories();
        Set<String> allowedLanguageCodes = game.getAllowableLanguageCodes();
        int minDifficulty = round.getActivityMinimumDifficulty() != null ? round.getActivityMinimumDifficulty() : Round.MIN_DIFFICULTY;
        int maxDifficulty = round.getActivityMaximumDifficulty() != null ? round.getActivityMaximumDifficulty() : Round.MAX_DIFFICULTY;

        List<Long> subscriberIds = matchPlayers.stream().map(mp -> mp.getSubscriberId()).collect(Collectors.toList());

        //grab a question and encrypt it (along with necessary decrypt metadata)
        Question question = _questionSupplier.getQuestion(roundCategoryUuids, allowedLanguageCodes, minDifficulty, maxDifficulty, game.getId(), subscriberIds);
//long b4 = System.currentTimeMillis();
//long d3 = (b4-b3);

        //if this is a tiebreaker, mark it as such
        if (isTieBreaker) {
            question.setType(Question.TYPE.TIEBREAKER);
            _dao.addTieBreakerQuestion(game.getId(), match.getId());
        }

        //store for later for the bots to use if they need (smarter bot AI's might have a percentage of time they pick the correct answer
        String correctAnswerId = null;
        String incorrectAnswerId = null;
        for (QuestionAnswer a : question.getAnswers()) {
            if (a.getCorrect() != null && a.getCorrect()) {
                correctAnswerId = a.getId();
//_logger.info(MessageFormat.format("\n>>> correct answer: #{0}\n", a.getAnswerText().get("en")));
            } else {
                incorrectAnswerId = a.getId();
            }
        };

        String questionWithAnswers;
        String questionWithoutAnswers;
        try {
            //a version of the json which has the correct answer embedded
            questionWithAnswers = _jsonMapper.writeValueAsString(question);

            //a version of the json which does not have the correct answer embedded
            question.getAnswers().forEach(a -> a.setCorrect(null));
            questionWithoutAnswers = _jsonMapper.writeValueAsString(question);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//long b5 = System.currentTimeMillis();
//long d4 = (b5-b4);

//redundant - it's removed in the block above
//        //but first, remove the "correct" value from each answer so the client doesn't get this sensitive information
//        question.getAnswers().forEach(a -> a.setCorrect(null));

        String questionDecryptKey = UUID.randomUUID().toString().replaceAll("-", "");
        String encryptedQuestionAndAnswersBodyWithAnswers;
        String encryptedQuestionAndAnswersBodyWithoutAnswers;
        encryptedQuestionAndAnswersBodyWithAnswers = _shoutContestService.aesEncrypt(questionDecryptKey.substring(0, 16), questionDecryptKey.substring(16), questionWithAnswers);
        encryptedQuestionAndAnswersBodyWithoutAnswers = _shoutContestService.aesEncrypt(questionDecryptKey.substring(0, 16), questionDecryptKey.substring(16), questionWithoutAnswers);

//long b6 = System.currentTimeMillis();
//long d5 = (b6-b5);

        //create the MatchQuestion row and the SubscriberQuestionAnswer rows for each player
        MatchQuestion matchQuestion = new MatchQuestion(game.getId(), round.getId(), match.getId(), question.getId(), round.getRoundActivityValue());
        List<SubscriberQuestionAnswer> subQuestionAnswers = new ArrayList<SubscriberQuestionAnswer>(matchPlayers.size());
        for (MatchPlayer matchPlayer : matchPlayers) {
            SubscriberQuestionAnswer sqa = new SubscriberQuestionAnswer(
                    game.getId(), round.getId(), match.getId(), question.getId(), matchQuestion.getId(), matchPlayer.getSubscriberId(), questionDecryptKey);
            subQuestionAnswers.add(sqa);
        }

        //store them in the database and update the sync doc with the "question" message
        _dao.insertMatchQuestion(matchQuestion);

//long b7 = System.currentTimeMillis();
//long d6 = (b7-b6);

        //update the local cache
        List<MatchQuestion> mqs = _matchQuestionCache.get(matchQuestion.getMatchId());
        if (mqs == null) {
            mqs = new ArrayList<MatchQuestion>();
        }
        mqs.add(matchQuestion);
        _matchQuestionCache.put(matchQuestion.getMatchId(), mqs);

        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                "MME matchQuestion {0} asked at: {1,date,yyyy-MM-dd hh:mm:ss.SSS}",
                matchQuestion.getId(), matchQuestion.getCreateDate()));
        }

        String questionBody = game.isIncludeActivityAnswersBeforeScoring() ? encryptedQuestionAndAnswersBodyWithAnswers : encryptedQuestionAndAnswersBodyWithoutAnswers;

        final List<AiQuestionBundle> aiQuestionList = new ArrayList<>();
        for (SubscriberQuestionAnswer sqa : subQuestionAnswers) {
            _dao.insertSubscriberQuestionAnswer(sqa);

            //if this is an AI player, let it know that it's time to answer
            if (botsInGame.contains(sqa.getSubscriberId()) || sponsorsInGame.contains(sqa.getSubscriberId())) {
                aiQuestionList.add(new AiQuestionBundle(
                        sqa.getSubscriberId(), sqa.getId(), question, round.getPlayerMaximumDurationSeconds()*1_000L, correctAnswerId, incorrectAnswerId, game.isUseDoctoredTimeForBots()));
            }
        }

        //let the transaction finish first so the sqa exists in the database, otherwise it's possible (and in fact we've seen it a lot) where the sync message
        // will go out, the client will try to grab the decrypt key, but the sqa isn't there yet because this transaction hasn't completed.
        final String gameId = game.getId();
        final String fCorrectAnswerId = correctAnswerId;
        TransactionSideEffectManager.runAfterThisTransactionCommit(() -> {

            boolean doesMatchContainTwitchSubscriber = false;

            for (SubscriberQuestionAnswer sqa : subQuestionAnswers) {
                if (twitchSubscriberId != null && twitchSubscriberId == sqa.getSubscriberId()) {
                    doesMatchContainTwitchSubscriber = true;
                }

                Subscriber subscriber = _identityService.getSubscriberById(sqa.getSubscriberId());
                enqueueSyncMessage(
                        _jsonMapper, _syncService, _logger,
                        gameId, ISnowyowlService.SYNC_MESSAGE_QUESTION,
                        new FastMap<>("subscriberQuestionAnswerId", sqa.getId(), "question", questionBody), subscriber, _socketIoSocket, _triggerService);
            }

            for (AiQuestionBundle bundle : aiQuestionList) {
                if (botsInGame.contains(bundle.aiSubscriberId)) {
                    _botEngine.submitAnswer(bundle);
                } else if (sponsorsInGame.contains(bundle.aiSubscriberId)) {
_logger.info(">>> letting sponsor " + bundle.aiSubscriberId + " know it's time to answer a question");
                    _sponsorEngine.submitAnswer(bundle);
                }
            }

            //send a twitch socket.io message
            if (doesMatchContainTwitchSubscriber) {
                long opId = 0;
                for (SubscriberQuestionAnswer sqa : subQuestionAnswers) {
                    if (sqa.getSubscriberId() != twitchSubscriberId) {
                        opId = sqa.getSubscriberId();
                        break;
                    }
                }

                //put the correct answer back in
                for (QuestionAnswer a: question.getAnswers()) {
                    if (a.getId().equals(fCorrectAnswerId)) {
                        a.setCorrect(true);
                        break;
                    }
                }

                Map<String, Object> twitchMap = new FastMap<>(
                    "type", "TWITCH_QUESTION",
                    "gameid", gameId,
                    "roundId", round.getId(),
                    "matchId", match.getId(),
                    "subscriberId", twitchSubscriberId,
                    "opponentId", opId,
                    "question", question,
                    "tieBreaker", isTieBreaker
                );

                if (_socketIoSocket != null) {
                    try {
                        _socketIoSocket.emit("send_twitch_message", _jsonMapper.writeValueAsString(twitchMap));
                    } catch (JsonProcessingException e) {
                        _logger.error("unable to emit send_twitch_message", e);
                    }
                }
            }
        });

//long b8 = System.currentTimeMillis();
//long d7 = (b8-b7);
//
//return Arrays.asList(d1,d2,d3,d4,d5,d6,d7);
    }

    /**
     * determine if all subscribers have either answered or timed out. if so, score the question
     *
     * @param round
     * @param match
     * @param matchQuestion
     * @return true if the question was scored (i.e. match over due to all answering or timing out), false otherwise
     */
    private boolean processMatchQuestion(Game game, Round round, Match match, MatchQuestion matchQuestion, List<Long> botsInGame)
    {
//_logger.debug("processing match question...");
        //get all of the answers records for this match (they may or may not have an answer yet, but the rows will exist for each person in the match)
        List<SubscriberQuestionAnswer> sqas = _dao.getSubscriberQuestionAnswersViaMatchQuestion(matchQuestion.getId());

        //see which (if any) of the answer records contain an answer
        List<SubscriberQuestionAnswer> answeredAnswers = sqas.stream().filter(a -> a.getSelectedAnswerId() != null).collect(Collectors.toList());

        if (answeredAnswers.size() == round.getMatchPlayerCount()) {
            //all players have answered, score the question
            scoreQuestion(game, round, match, matchQuestion, sqas, false, botsInGame);
            return true;

        } else {
            //see if the question has timed out
            if (_questionSupplier.hasQuestionTimedOut(round, matchQuestion)) {
                scoreQuestion(game, round, match, matchQuestion, sqas, true, botsInGame);
                return true;
            }

            //question is still in play and not everyone has answered. see if individually any of the players have timed out
            sqas.stream()
                .filter(sqa -> sqa.getSelectedAnswerId() != null)
                .forEach(sqa -> {
                    TIMEOUT_REASON timeoutReason = hasPlayerTimedOut(round, matchQuestion, sqa, botsInGame);
                    if (timeoutReason != TIMEOUT_REASON.NONE) {
                        //mark player as having timed out
                        sqa.setSelectedAnswerId("n/a");
                        sqa.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT);

                        _dao.updateSubscriberQuestionAnswer(sqa);

                        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                    "MME: sId: {0,number,#}, sqaId: {1}, answer determination: LOST_TIMEOUT ({2})",
                                    sqa.getSubscriberId(), sqa.getId(), timeoutReason));
                        }
                    }
                });

            answeredAnswers = sqas.stream().filter(sqa -> sqa.getSelectedAnswerId() != null).collect(Collectors.toList());
            if (answeredAnswers.size() == round.getMatchPlayerCount()) {
                //all players have now either answered or been timed out
                scoreQuestion(game, round, match, matchQuestion, sqas, false, botsInGame);
                return true;
            } else {
                //match is still in play
                return false;
            }
        }
    }

    private TIMEOUT_REASON hasPlayerTimedOut(Round round, MatchQuestion matchQuestion, SubscriberQuestionAnswer sqa, List<Long> botsInGame)
    {
//boolean showLog = !botsInGame.contains(sqa.getSubscriberId());
        //have they requested the question?
        if (sqa.getQuestionPresentedTimestamp() != null) {
//if (showLog) _logger.info("hasPlayerTimedOut: sqa.getQuestionPresentedTimestamp(): " + sqa.getQuestionPresentedTimestamp());
            //the HAVE requested the question. have they answered?
            if (sqa.getSelectedAnswerId() != null) {
//if (showLog) _logger.info("hasPlayerTimedOut: sqa.getSelectedAnswerId(): " + sqa.getSelectedAnswerId());
                //they HAVE answered. was it in time?
                long maxTimeAnyoneCanTakeToAnswerQuestion = round.getPlayerMaximumDurationSeconds() * 1_000L;
//if (showLog) _logger.info("hasPlayerTimedOut: maxTimeAnyoneCanTakeToAnswerQuestion: " + maxTimeAnyoneCanTakeToAnswerQuestion);

                TIMEOUT_REASON reason = sqa.getDurationMilliseconds() > maxTimeAnyoneCanTakeToAnswerQuestion ? TIMEOUT_REASON.SLOW_ANSWER : TIMEOUT_REASON.NONE;
//if (showLog) _logger.info("hasPlayerTimedOut: reason: " + reason);
                return reason;

            } else {
//if (showLog) _logger.info("hasPlayerTimedOut: sqa.getSelectedAnswerId(): null");
                //they have NOT answered. has it been too long and they no longer can?
                long elapsedTimeSinceQuestionWasRequested = System.currentTimeMillis() - sqa.getQuestionPresentedTimestamp().getTime();
//if (showLog) _logger.info("hasPlayerTimedOut: elapsedTimeSinceQuestionWasRequested: " + elapsedTimeSinceQuestionWasRequested);
                long maxTimeAnyoneCanTakeToAnswerQuestion = round.getPlayerMaximumDurationSeconds() * 1_000L;
//if (showLog) _logger.info("hasPlayerTimedOut: maxTimeAnyoneCanTakeToAnswerQuestion: " + maxTimeAnyoneCanTakeToAnswerQuestion);

                TIMEOUT_REASON reason = elapsedTimeSinceQuestionWasRequested >= maxTimeAnyoneCanTakeToAnswerQuestion ? TIMEOUT_REASON.NO_ANSWER : TIMEOUT_REASON.NONE;
//if (showLog) _logger.info("hasPlayerTimedOut: reason: " + reason);
                return reason;
            }

        } else {
//if (showLog) _logger.info("hasPlayerTimedOut: sqa.getQuestionPresentedTimestamp(): null");
            //they have NOT requested question yet. has it been too long and they no longer can?
            long elapsedTimeSinceQuestionWasAsked = System.currentTimeMillis() - matchQuestion.getCreateDate().getTime();
//if (showLog) _logger.info("hasPlayerTimedOut: elapsedTimeSinceQuestionWasAsked: " + elapsedTimeSinceQuestionWasAsked);
            long maxTimeAnyoneCanWaitToRequestQuestion = round.getActivityMaximumDurationSeconds() * 1_000L;
//if (showLog) _logger.info("hasPlayerTimedOut: maxTimeAnyoneCanWaitToRequestQuestion: " + maxTimeAnyoneCanWaitToRequestQuestion);

            TIMEOUT_REASON reason = elapsedTimeSinceQuestionWasAsked >= maxTimeAnyoneCanWaitToRequestQuestion ? TIMEOUT_REASON.NO_DECRYPT_REQUEST : TIMEOUT_REASON.NONE;
//if (showLog) _logger.info("hasPlayerTimedOut: reason: " + reason);
            return reason;
        }
    }

}
