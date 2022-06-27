package tv.shout.snowyowl.collector;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.commons.postoffice.service.EmailAddress;
import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.gameplay.domain.Tuple;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.mrsoa.distdata.visor.DistributedVisor;
import com.meinc.notification.domain.NotificationPref;
import com.meinc.notification.service.INotificationService;
import com.meinc.push.service.IPushService;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.urlshorten.exception.UrlShortenerException;
import com.meinc.urlshorten.service.IUrlShortenerService;
import com.meinc.webdatastore.service.IWebDataStoreService;

import io.socket.client.Socket;
import tv.shout.collector.PublishResponseError;
import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.LocalizationHelper;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_STATUS;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.Message;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.simplemessagebus.MessageProcessor;
import tv.shout.snowyowl.common.EmailSender;
import tv.shout.snowyowl.common.GamePublisher;
import tv.shout.snowyowl.common.GameStatsHandler;
import tv.shout.snowyowl.common.PayoutTablePublisher;
import tv.shout.snowyowl.common.PushSender;
import tv.shout.snowyowl.common.RoundQuestionRetriever;
import tv.shout.snowyowl.common.ShortUrlGenerator;
import tv.shout.snowyowl.common.SmsSender;
import tv.shout.snowyowl.common.SubscriberStatsHandler;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.Sponsor;
import tv.shout.snowyowl.domain.SponsorCashPool;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.snowyowl.engine.PayoutManagerException;
import tv.shout.snowyowl.engine.PayoutManagerFixedRoundCommon;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerBeginPoolPlay;
import tv.shout.snowyowl.reactiveengine.fixedround.MatchMaker;
import tv.shout.snowyowl.service.CurrentRankCalculator;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.snowyowl.service.SnowyowlService;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;
import tv.shout.util.MaxSizeHashMap;

/**
 * Contains business logic methods that can be called from multiple sources. Make sure that the caller has wrapped the call inside a transaction
 */
public class CommonBusinessLogic
implements SyncMessageSender, GamePublisher, PayoutTablePublisher, RoundQuestionRetriever, ShortUrlGenerator, SmsSender, PushSender, EmailSender
{
    private static Logger _logger = Logger.getLogger(CommonBusinessLogic.class);
    private static Logger _busLogger = Logger.getLogger("messagebus");
    private static ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();

    private static final String bracketPlayBeginsTitleLocalizationUuid = "15083728-33c5-4df5-bb66-cdf83f019e9b";
    private static final String bracketPlayBeginsBodyLocalizationUuid = "22dd185b-7e20-11e7-970d-0242ac110004";
    private static final String bracketPlayBeginsEmailSubjectUuid = "cd620a64-2534-45d0-98f6-1159c4443049";

    public static final long EXPIRES_1_HOUR_MS = 1000L * 60L * 60L;
    private static long WAIT_TIME_TIMEOUT_DEFAULT = 10_000L;

    @Value("${shorten.url.domain}")
    private String _shortUrlDomain;

    @Value("${shorten.url.short.url.prefix}")
    private String _shortUrlPrefix;

    @Value("${twilio.from.number}")
    private String _twilioFromNumber;

    @Value("${twilio.account.sid}")
    private String _twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String _twilioAuthToken;

    @Value("${from.email.addresses}")
    private String _emailFromAddrs;

    @Value("${from.email.names}")
    private String _emailFromNames;

    @Autowired
    protected MatchMaker _matchMaker;

    @Autowired
    private PlatformTransactionManager _transactionManager;

    @Autowired
    protected ISyncService _syncService;

    @Autowired
    protected IIdentityService _identityService;

    @Autowired
    private IPushService _pushService;

    @Autowired
    private IPostOffice _postOfficeService;

    @Autowired
    private INotificationService _notificationService;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected ITriggerService _triggerService;

    @Autowired
    private IUrlShortenerService _urlShortenerService;

    @Autowired
    protected CurrentRankCalculator _currentRankCalculator;

    @Autowired
    protected BotEngine _botEngine;

    @Autowired
    protected IDaoMapper _dao;

    @Autowired
    protected EngineCoordinator _engineCoordinator;

    @Autowired
    private SubscriberStatsHandler _subscriberStatsHandler;

    @Autowired
    private MessageBus _messageBus;

    @Resource(name="handlerBeginPoolPlay")
    private MessageProcessor _handlerBeginPoolPlay;

    @Autowired
    protected GameStatsHandler _gameStatsHandler;

    public CommonBusinessLogic()
    {
    }

    private Map<String, List<Round>> _gameRounds = new MaxSizeHashMap<String, List<Round>>().withMaxSize(10);

    public Round getFirstRoundOfGame(String gameId)
    {
        List<Round> rounds = _gameRounds.get(gameId);
        if (rounds == null) {
            rounds = _shoutContestService.getRoundsForGame(gameId);
            _gameRounds.put(gameId, rounds);
        }

        //sort the rounds by sequence
        Collections.sort(rounds, (lhs, rhs) -> lhs.getRoundSequence() < rhs.getRoundSequence() ? -1 : 1);

        return rounds.get(0);
    }

    public Round getFirstBracketRoundOfGame(String gameId)
    {
        List<Round> rounds = _gameRounds.get(gameId);
        if (rounds == null) {
            rounds = _shoutContestService.getRoundsForGame(gameId);
            _gameRounds.put(gameId, rounds);
        }

        //sort the rounds by sequence
        Collections.sort(rounds, (lhs, rhs) -> lhs.getRoundSequence() < rhs.getRoundSequence() ? -1 : 1);

        return rounds.stream()
            .filter(r -> r.getRoundType() == Round.ROUND_TYPE.BRACKET)
            .findFirst()
            .orElse(null);
    }

    public void joinGame(
        String toWds, String messageId, String logTag, Subscriber applicant, String gameId, Socket socketIoSocket, ITriggerService triggerService, Round firstRound,
        boolean asFreeplay)
    throws PublishResponseError
    {
        //make sure the game exists
        if (gameId == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "missingRequiredParam", "gameId");
        }
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false,  "invalidParam", "gameId");
        }

        //game must be in the "open" state
        if (game.getGameStatus() != Game.GAME_STATUS.OPEN) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "gameNotOpen");
        }

        //make sure subscriber is allowed to join the game
        if (applicant.getFromCountryCode() != null && game.getForbiddenCountryCodes() != null && game.getForbiddenCountryCodes().contains(applicant.getFromCountryCode())) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "accessDenied", "fromCountryCode");
        }
        if (game.getAllowableLanguageCodes() != null && !game.getAllowableLanguageCodes().contains(applicant.getLanguageCode())) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "accessDenied", "languageCode");
        }
        if (game.getAllowableAppIds() != null && !game.getAllowableAppIds().contains(applicant.getContextId())) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "accessDenied", "appId");
        }

        //make sure they're not already in the game
        GamePlayer existingGamePlayerRecord = _shoutContestService.getGamePlayer(gameId, applicant.getSubscriberId());
        if (existingGamePlayerRecord != null && !existingGamePlayerRecord.isFreeplay()) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "alreadyJoined");
        }

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //check against prohibited subscribers
            if (_dao.isSubscriberProhibited(applicant.getSubscriberId())) {
                throw new PublishResponseError(toWds, messageId, logTag, false, "accessDenied", "prohibited");
            }

            //if this is a production event, check against ineligible subscribers
            if (game.isProductionGame() && _dao.isSubscriberIneligible(applicant.getSubscriberId())) {
                throw new PublishResponseError(toWds, messageId, logTag, false, "accessDenied", "ineligible");
            }

            if (firstRound != null) {
                //see if there is a sponsored player in the game. if so, remove them first
//_logger.info("checking for sponsor player...");
                Sponsor sponsorPlayer = _dao.getSingleSponsorForGame(gameId);
                if (sponsorPlayer != null) {
                    removeSponsorPlayerFromGame(game, firstRound, sponsorPlayer);
                }

                //make sure round isn't FULL (or cancelled or closed)
                if (firstRound.getRoundStatus() == ROUND_STATUS.CANCELLED ||
                        firstRound.getRoundStatus() == ROUND_STATUS.CLOSED ||
                                firstRound.getRoundStatus() == ROUND_STATUS.FULL) {
                    throw new PublishResponseError(toWds, messageId, logTag, false, "roundNotOpen", firstRound.getRoundStatus().toString());
                }

                //check for payment
                if (game.isProductionGame() && !asFreeplay) {
                    if (firstRound.getCostPerPlayer() != null && firstRound.getCostPerPlayer() > 0D) {
                        double balance = Optional.of(_shoutContestService.getTotalBalance(applicant.getSubscriberId())).orElse(0D);
                        if (balance < firstRound.getCostPerPlayer()) {
                            throw new PublishResponseError(toWds, messageId, logTag, false, "insufficientFunds");
                        }
                        if (applicant.getRole() != Subscriber.ROLE.TESTER) {
                            _shoutContestService.addCashPoolTransaction(
                                    applicant.getSubscriberId(),
                                    - firstRound.getCostPerPlayer(),
                                    CashPoolTransaction2.TYPE.JOINED_ROUND,
                                    null,
                                    null,
                                    firstRound.getId());
                        }
                    }
                }
            }

            //add (or update) the GamePlayer record
            GamePlayer gamePlayer;
            if (existingGamePlayerRecord != null) {
                gamePlayer = existingGamePlayerRecord;
                gamePlayer.setFreeplay(asFreeplay);
            } else {
                gamePlayer = new GamePlayer(gameId, applicant.getSubscriberId());
                gamePlayer.setFreeplay(asFreeplay);
            }

            //number of lives comes from two different sources, depending on the engineType
            switch (game.getEngineType())
            {
                case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife:
                    if (game.getBracketEliminationCount() != null) {
                        gamePlayer.setCountdownToElimination(game.getBracketEliminationCount());
                        gamePlayer.setTotalLives(game.getBracketEliminationCount());
                    }
                    break;

                case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife:
                    gamePlayer.setCountdownToElimination(game.getStartingLivesCount());
                    gamePlayer.setTotalLives(game.getStartingLivesCount());
                    break;
            }

            String nextRoundId = null;
            if (existingGamePlayerRecord == null) {
                if (!_shoutContestService.addGamePlayer(gamePlayer, nextRoundId, Round.ROUND_STATUS.VISIBLE, Round.ROUND_STATUS.OPEN, Round.ROUND_STATUS.FULL)) {
                    throw new PublishResponseError(toWds, messageId, logTag, false, "noOpenRounds");
                }
            } else {
                _shoutContestService.updateGamePlayer(gamePlayer);
            }

            //send the "joined_game" sync message
            enqueueSyncMessage(
                _jsonMapper, _syncService, _logger,
                game.getId(), ISnowyowlService.SYNC_MESSAGE_JOINED_GAME, new FastMap<>("freeplay", asFreeplay), applicant, socketIoSocket, triggerService);

            //update game player count document
            _shoutContestService.publishGamePlayerCountToWds(gameId, _botEngine.getBotsForGame(gameId));

            if (game.isProductionGame()) {
                _subscriberStatsHandler.incrementSubscriberStat(applicant.getSubscriberId(), SubscriberStats.STATS_TYPE.GAMES_PLAYED, 1);
            }

            _transactionManager.commit(txStatus);
            txStatus = null;

        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }
    }

    //for performance. skip everything not necessary
    public void joinGameAsBot(Game game, long botSubscriberId, String nextRoundIdForBot)
    {
        //add (or update) the GamePlayer record
        GamePlayer gamePlayer = new GamePlayer(game.getId(), botSubscriberId);

        //number of lives comes from two different sources, depending on the engineType
        switch (game.getEngineType())
        {
            case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife:
                if (game.getBracketEliminationCount() != null) {
                    gamePlayer.setCountdownToElimination(game.getBracketEliminationCount());
                    gamePlayer.setTotalLives(game.getBracketEliminationCount());
                }
                break;

            case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife:
                gamePlayer.setCountdownToElimination(game.getStartingLivesCount());
                gamePlayer.setTotalLives(game.getStartingLivesCount());
                break;
        }

        _shoutContestService.addGamePlayer(gamePlayer, nextRoundIdForBot, Round.ROUND_STATUS.VISIBLE, Round.ROUND_STATUS.OPEN, Round.ROUND_STATUS.FULL);
    }

    //for performance. skip everything not necessary
    public void joinGameAsSponsor(SponsorCashPool pool, Game game, long sponsorSubscriberId, Round roundToJoin, double costToJoin)
    {
        //check for payment
        if (game.isProductionGame()) {
            if (costToJoin > 0D) {
_logger.info(">>> withdrawing " + costToJoin + " from sponsor pool " + pool.getSponsorCashPoolId());
                pool.setAmount(pool.getAmount() - costToJoin);
                _dao.updateSponsorCashPool(pool);
                _dao.addSponsorCashPoolTransaction(pool.getSponsorCashPoolId(), -costToJoin, "WITHDRAWL_FOR_GAME");
            }
        }

        //add (or update) the GamePlayer record
        GamePlayer gamePlayer = new GamePlayer(game.getId(), sponsorSubscriberId);

        //number of lives comes from two different sources, depending on the engineType
        switch (game.getEngineType())
        {
            case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife:
                if (game.getBracketEliminationCount() != null) {
                    gamePlayer.setCountdownToElimination(game.getBracketEliminationCount());
                    gamePlayer.setTotalLives(game.getBracketEliminationCount());
                }
                break;

            case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife:
                gamePlayer.setCountdownToElimination(game.getStartingLivesCount());
                gamePlayer.setTotalLives(game.getStartingLivesCount());
                break;
        }

        _shoutContestService.addGamePlayer(gamePlayer, roundToJoin.getId(), Round.ROUND_STATUS.VISIBLE, Round.ROUND_STATUS.OPEN, Round.ROUND_STATUS.FULL);
    }

    public void setPlayerAvailability(
            String toWds, String messageId, String logTag,
            Subscriber applicant,
            String gameId, /*boolean isAvailable, */Socket socketIoSocket, ITriggerService triggerService)
    throws PublishResponseError
    {
        //make sure the game exists
        if (gameId == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "missingRequiredParam", "gameId");
        }
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false,  "invalidParam", "gameId");
        }

        //are they in the game
        GamePlayer gamePlayer = _shoutContestService.getGamePlayer(gameId, applicant.getSubscriberId());
        if (gamePlayer == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "notInGame");
        }

        //does the round exist
        String nextRoundId = gamePlayer.getNextRoundId();
        Round round = _shoutContestService.getRound(nextRoundId);
        if (round == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "roundNotFound");
        }

        //make sure it's a pool round. if it's a bracket round, it means they've played all pool rounds and must wait for bracket play
        if (round.getRoundType() != Round.ROUND_TYPE.POOL) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "waitForBracketPlayToBegin");
        }

        //make sure they're not currently in an active pool play round
        RoundPlayer existingRoundPlayer = _shoutContestService.getRoundPlayer2(round.getId(), applicant.getSubscriberId());
        if (existingRoundPlayer != null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "alreadyPlayingPoolRound");
        }

        //make sure the round isn't currently being processed
        String lockName = "ROUND_" + round.getId().hashCode();
//        boolean startMQE = false;
        if (!DistributedVisor.tryClusterLock(lockName)) {
            _logger.warn(MessageFormat.format("failed to setPlayerAvailability: roundLocked. lockName: {0}, sId: {1,number,#}", lockName, applicant.getSubscriberId()));
            throw new PublishResponseError(toWds, messageId, logTag, false, "roundLocked");
        }

        try {
//            if (isAvailable) {
                //make sure they're not already queued
                //MatchQueue alreadyInMatchQueue = _shoutContestService.getPlayerAvailableMatchQueue(game.getId(), nextRoundId, applicant.getSubscriberId());
                boolean alreadyQueued = ((HandlerBeginPoolPlay)_handlerBeginPoolPlay).isSubscriberStillWaiting(game.getId(), applicant.getSubscriberId());
                //if (alreadyInMatchQueue != null) {
                if (alreadyQueued) {
                    throw new PublishResponseError(toWds, messageId, logTag, false, "alreadyQueued");
                }

                //make sure round is open (i.e. accepting players) - bot players get a free pass since only added after pairing begins to fill out the round
                if (round.getRoundStatus() != ROUND_STATUS.OPEN) {
                    throw new PublishResponseError(toWds, messageId, logTag, false, "roundNotOpen", round.getRoundStatus().toString());
                }

                //increment the round's player count (and set to full if nobody else can fit in)
                //unless this is a freeplayer
                if (!gamePlayer.isFreeplay()) {
                    round.setCurrentPlayerCount(round.getCurrentPlayerCount() + 1);
                    if (round.getCurrentPlayerCount() >= round.getMaximumPlayerCount() && round.getRoundStatus() == Round.ROUND_STATUS.OPEN) {
                        round.setRoundStatus(ROUND_STATUS.FULL);
                    }
                    _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());
                }

                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                try {
                    publishGameWithExtrasToWds(game.getId(), _dao, _shoutContestService, _gameStatsHandler);

                    _transactionManager.commit(txStatus);
                    txStatus = null;

                } finally {
                    if (txStatus != null) {
                        _transactionManager.rollback(txStatus);
                        txStatus = null;
                    }
                }

                //add the RoundPlayer record
                RoundPlayer roundPlayer = new RoundPlayer(game.getId(), round.getId(), applicant.getSubscriberId());

                //if this is not the first round, move forward the rank values from the previous round
                if (round.getRoundSequence() != 1) {
                    RoundPlayer oldRoundPlayer = _shoutContestService.getMostRecentRoundPlayer(game.getId(), applicant.getSubscriberId());
                    if (oldRoundPlayer != null) {
                        roundPlayer.setRank(oldRoundPlayer.getRank());
                        roundPlayer.setSkillAnswerCorrectPct(oldRoundPlayer.getSkillAnswerCorrectPct());
                        roundPlayer.setSkillAverageAnswerMs(oldRoundPlayer.getSkillAverageAnswerMs());
                        roundPlayer.setSkill(oldRoundPlayer.getSkill());
                    }
                }

_logger.info(MessageFormat.format(">>> CommonBusinessLogic::setPlayerAvailability, addRoundPlayer. roundId: {0}, subscriberId: {1,number,#}", roundPlayer.getRoundId(), roundPlayer.getSubscriberId()));
                _shoutContestService.addRoundPlayer(roundPlayer, round.getRoundType());

                //TOxDO: put this on a background thread
                if (_currentRankCalculator != null) _currentRankCalculator.clear(game.getId());

                //send the joined_round message
                enqueueSyncMessage(
                    _jsonMapper, _syncService, _logger,
                    game.getId(), ISnowyowlService.SYNC_MESSAGE_JOINED_ROUND,
                    new FastMap<String, Object>(
                        "roundPlayer", roundPlayer,
                        "currentPlayerCount", round.getCurrentPlayerCount(),
                        "roundType", "POOL"
                    ), applicant, socketIoSocket, triggerService);

                //add to the match queue (make them eligible for pairing)
                //_shoutContestService.enqueueMatchQueue(game.getId(), round.getId(), roundPlayer.getId(), applicant.getSubscriberId());
                Message msg = HandlerBeginPoolPlay.createPlayPoolRoundMessage(game, round, applicant.getSubscriberId(), roundPlayer.getId(), WAIT_TIME_TIMEOUT_DEFAULT);
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("CommonBusinessLogic sending message: " + msg.type);
                }
                _messageBus.sendMessage(msg);

//                startMQE = true;

//shawker (4 Jun 2019) - REMOVING THIS. it adds too much complexity, and isn't a hard business requirement
//            } else { //not available
//
//                //remove from the match queue
//                MatchQueue cancelledMatchQueue = _shoutContestService.cancelMatchQueue(game.getId(), nextRoundId, applicant.getSubscriberId());
//                if (cancelledMatchQueue == null) {
//                    //unable to cancel because already matched
//                    throw new PublishResponseError(toWds, messageId, logTag, false, "alreadyMatched"); //this could also mean not yet added to the queue...
//                }
//
//                //remove from the waiting list in the match queue engine
//                _engineCoordinator.subscriberCancelledQueuing(game, applicant.getSubscriberId());
//
//                //refund payment
//                if (game.isProductionGame() && round.getCostPerPlayer() != null) {
//                    _shoutContestService.addCashPoolTransaction(
//                            applicant.getSubscriberId(), round.getCostPerPlayer(), CashPoolTransaction2.TYPE.ABANDONED_ROUND, null, null, round.getId());
//                }
//
//                //mark round as abandoned
//                RoundPlayer roundPlayer = _shoutContestService.getRoundPlayerByDetermination(game.getId(), round.getId(), applicant.getSubscriberId(), RoundPlayer.ROUND_PLAYER_DETERMINATION.UNKNOWN);
//                removeFromRound(game.getId(), round, roundPlayer);
//                //possible bug: if we don't delete the roundplayer, they won't be able to join pool play again for this round. do we care?
//
//                //send the abandoned_round message
//                enqueueSyncMessage(
//                    _jsonMapper, _syncService, _logger,
//                    game.getId(), ISnowyowlService.SYNC_MESSAGE_ABANDONED_ROUND,
//                    new FastMap<String, Object>("roundPlayer", roundPlayer), applicant, socketIoSocket, triggerService);
//
//                if (game.isProductionGame()) {
//                    _subscriberStatsHandler.incrementSubscriberStat(applicant.getSubscriberId(), SubscriberStats.STATS_TYPE.GAMES_PLAYED, -1);
//                }
//
//            }

        } finally {
            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
            try {
                DistributedVisor.releaseClusterLock(lockName);

                _transactionManager.commit(txStatus);
                txStatus = null;

            } finally {
                if (txStatus != null) {
                    _transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }

//            //must be done after the lock is released, since this also grabs the lock
//            if (startMQE) {
//                //start the pairing process
//                _engineCoordinator.runMQE(game);
//            }

        }
    }

    public Round setPlayerAvailabilityAsBot(Game game, Round round, long botSubscriberId, boolean lastCall)
    {
        return setPlayerAvailabilityAsAi(game, round, botSubscriberId, lastCall, true);
    }

    public Round setPlayerAvailabilityAsSponsor(Game game, Round round, long botSubscriberId, boolean lastCall)
    {
        return setPlayerAvailabilityAsAi(game, round, botSubscriberId, lastCall, false);
    }

    protected Round setPlayerAvailabilityAsAi(Game game, Round round, long botSubscriberId, boolean lastCall, boolean makeAvailableForPairing)
    {
        GamePlayer gamePlayer = _shoutContestService.getGamePlayer(game.getId(), botSubscriberId);

        if (round == null) {
            String nextRoundId = gamePlayer.getNextRoundId();
            round = _shoutContestService.getRound(nextRoundId);
        }

        //increment the round's player count (and set to full if nobody else can fit in)
        round.setCurrentPlayerCount(round.getCurrentPlayerCount() + 1);
        if (round.getCurrentPlayerCount() >= round.getMaximumPlayerCount() && round.getRoundStatus() == Round.ROUND_STATUS.OPEN) {
            round.setRoundStatus(ROUND_STATUS.FULL);
        }

        //don't actually do this until the bots are finished filing in, otherwise it does it over and over and over
        if (lastCall) {
            _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());

            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
            try {
                publishGameWithExtrasToWds(game.getId(), _dao, _shoutContestService, _gameStatsHandler);

                _transactionManager.commit(txStatus);
                txStatus = null;

            } finally {
                if (txStatus != null) {
                    _transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }
        }

        RoundPlayer roundPlayer = new RoundPlayer(game.getId(), round.getId(), botSubscriberId);

        //add the RoundPlayer record
_logger.info(MessageFormat.format(">>> CommonBusinessLogic::setPlayerAvailabilityAsAi, roundId: {0}, subscriberId: {1}", roundPlayer.getRoundId(), roundPlayer.getSubscriberId()));
        _shoutContestService.addRoundPlayer(roundPlayer, round.getRoundType());

        //add to the match queue (make them eligible for pairing)
        if (makeAvailableForPairing) {
            _shoutContestService.enqueueMatchQueue(game.getId(), round.getId(), roundPlayer.getId(), botSubscriberId);
        }

        return round;
    }

    public void removeFromRound(String gameId, Round round, RoundPlayer roundPlayer)
    {
        _shoutContestService.updateRoundPlayerDetermination(roundPlayer.getId(), RoundPlayer.ROUND_PLAYER_DETERMINATION.ABANDONED);
        if (_currentRankCalculator != null) _currentRankCalculator.clear(gameId);

        //do the same with the gameplayer object
        GamePlayer gamePlayer = _shoutContestService.getGamePlayer(gameId, roundPlayer.getSubscriberId());
        gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.REMOVED);
        _shoutContestService.updateGamePlayer(gamePlayer);

        //decrement player count
        round.setCurrentPlayerCount(round.getCurrentPlayerCount() - 1);
        if (round.getRoundStatus() == ROUND_STATUS.FULL) {
            //doesn't necessarily go to open. might have been one over the max if a bot was playing
            if (round.getCurrentPlayerCount() < round.getMaximumPlayerCount()) {
                round.setRoundStatus(ROUND_STATUS.OPEN);
            }
        }
        _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            publishGameWithExtrasToWds(gameId, _dao, _shoutContestService, _gameStatsHandler);

            _transactionManager.commit(txStatus);
            txStatus = null;

        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }
    }

    private void removeSponsorPlayerFromGame(Game game, Round firstRound, Sponsor sponsor)
    {
        Round firstBracketRound = getFirstBracketRoundOfGame(game.getId());
        RoundPlayer roundPlayer = _shoutContestService.getRoundPlayerByDetermination(game.getId(), firstBracketRound.getId(), sponsor.getSubscriberId(), RoundPlayer.ROUND_PLAYER_DETERMINATION.UNKNOWN);
_logger.info(">>> removing sponsor player " + roundPlayer.getSubscriberId() + " from round " + firstBracketRound.getRoundSequence());
        removeFromRound(game.getId(), firstBracketRound, roundPlayer);

        //refund payment to sponsor pool
        SponsorCashPool pool = _dao.getSponsorCashPoolById(sponsor.getSponsorCashPoolId());
_logger.info(">>> refunding sponsor pool cash amount of " + firstRound.getCostPerPlayer() + " to pool " + sponsor.getSponsorCashPoolId());
        pool.setAmount(pool.getAmount() + firstRound.getCostPerPlayer());
        _dao.updateSponsorCashPool(pool);
        _dao.addSponsorCashPoolTransaction(pool.getSponsorCashPoolId(), firstRound.getCostPerPlayer(), "REFUND_REPLACED_BY_PLAYER");

        //release the sponsor player
_logger.info(">>> releasing sponsor player from game: " + game.getId());
        _dao.releaseSponsorPlayerForGame(sponsor.getSubscriberId());
    }

    public long submitAnswer(SubscriberQuestionAnswer sqa, String selectedAnswerId)
    {
        long durationMilliseconds = System.currentTimeMillis() - sqa.getQuestionPresentedTimestamp().getTime();
        submitAnswer2(sqa, selectedAnswerId, durationMilliseconds);
        return durationMilliseconds;
    }

//    //this must only be called by the bots. it is so they can doctor the duration value
//    public void submitAnswer(
//        Subscriber subscriber,
//        String subscriberQuestionAnswerId, String selectedAnswerId, long durationMilliseconds)
//    throws PublishResponseError
//    {
//        SubscriberQuestionAnswer sqa = getSubscriberQuestionAnswerWithPrechecks(null, null, null, subscriber, subscriberQuestionAnswerId, selectedAnswerId);
//        submitAnswer2(sqa, selectedAnswerId, durationMilliseconds);
//    }

    public SubscriberQuestionAnswer getSubscriberQuestionAnswerWithPrechecks(
            String toWds, String messageId, String logTag, Subscriber subscriber,
            String subscriberQuestionAnswerId, String selectedAnswerId)
    throws PublishResponseError
    {
        if (subscriberQuestionAnswerId == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "missingRequiredParam", "subscriberQuestionAnswerId");
        }
        if (selectedAnswerId == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "missingRequiredParam", "selectedAnswerId");
        }

        //grab the answer row that is to be filled in
        SubscriberQuestionAnswer sqa = _dao.getSubscriberQuestionAnswer(subscriberQuestionAnswerId);
        if (sqa == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "invalidParam", "subscriberQuestionAnswerId");
        }

        //make sure the answer row belongs to the subscriber
        if (sqa.getSubscriberId() != subscriber.getSubscriberId()) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "illegalAnswerState", "player-subscriber mismatch");
        }

        //make sure the subscriber hasn't already answered
        if (sqa.getSelectedAnswerId() != null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "duplicateAnswer");
        }

        //make sure the sqa doesn't already have a determination (e.g. attempting to answer after timing out)
        if (sqa.getDetermination() != null && sqa.getDetermination() != SubscriberQuestionAnswer.ANSWER_DETERMINATION.UNKNOWN) {
            if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                        "attempting to add answer but it is too late. sId: {0,number,#}, sqaId: {1}, answerId: {2}, sqaDetermination: {3}",
                        sqa.getSubscriberId(), sqa.getId(), selectedAnswerId, sqa.getDetermination()));
            }
            throw new PublishResponseError(toWds, messageId, logTag, false, "answerTooLate");
        }

        Date questionPresentedTimestamp = sqa.getQuestionPresentedTimestamp();
        if (questionPresentedTimestamp == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "questionAnsweredButNotShown");
        }

        //TOxDO: what about making sure the answer belongs to the question?

        return sqa;
    }

    private Map<String, Game> _gameNoFatCache = new MaxSizeHashMap<String, Game>().withMaxSize(5);

    private void submitAnswer2(SubscriberQuestionAnswer sqa, String selectedAnswerId, long durationMilliseconds)
    {
        //update the relevant data on the sqa
        sqa.setSelectedAnswerId(selectedAnswerId);
        sqa.setDurationMilliseconds(durationMilliseconds);

        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                    "adding answer. sId: {0,number,#}, sqaId: {1}, answerId: {2}, duration: {3,number,#}",
                    sqa.getSubscriberId(), sqa.getId(), selectedAnswerId, durationMilliseconds));
        }

        //update the database
        _dao.setAnswerOnSubscriberQuestionAnswer(sqa);

        //need to load the game to see if it's a production event
        Game game = _gameNoFatCache.get(sqa.getGameId());
        if (game == null) {
            game = _shoutContestService.getGameNoFat(sqa.getGameId());
            _gameNoFatCache.put(sqa.getGameId(), game);
        }

        if (game.isProductionGame()) {
            List<Long> botIds = _botEngine.getBotsForGame(sqa.getGameId());
            if (!botIds.contains(sqa.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_ANSWERED, 1);
            }
        }
    }

    public SubscriberQuestionAnswer getQuestionDecryptKey(String toWds, String messageId, String logTag, Subscriber subscriber, String subscriberQuestionAnswerId)
    throws PublishResponseError
    {
        if (subscriberQuestionAnswerId == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "missingRequiredParam", "subscriberQuestionAnswerId");
        }

        //grab the answer row that is to be filled in
        SubscriberQuestionAnswer sqa = _dao.getSubscriberQuestionAnswer(subscriberQuestionAnswerId);
        if (sqa == null) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "invalidParam", "subscriberQuestionAnswerId");
        }

        //make sure the answer row belongs to the subscriber
        if (sqa.getSubscriberId() != subscriber.getSubscriberId()) {
            throw new PublishResponseError(toWds, messageId, logTag, false, "illegalAnswerState", "player-subscriber mismatch");
        }

        //2017-10-03 per bruce: just allow it to be returned again
//        //make sure the decrypt key hasn't already been requested
//        if (sqa.getQuestionPresentedTimestamp() != null) {
//            throw new PublishResponseError(toWds, messageId, logTag, false, "alreadyRetrievedEncryptionKey", null);
//        }
        if (sqa.getQuestionPresentedTimestamp() != null) {
            return sqa;
        }

        //set the decryption key and update the database
        sqa.setQuestionPresentedTimestamp(new Date());
        _dao.setQuestionViewedTimestampOnSubscriberQuestionAnswer(sqa);

        return sqa;
    }

    public void openGame(Game game, IWebDataStoreService wdsService)
    {
        _shoutContestService.openGame(game.getId());

        List<String> gameIds = new ArrayList<>();
        gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.DEFAULT, ISnowyowlService.GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));
        gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.TESTER, ISnowyowlService.TEST_GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));

        //if this is a private game, its ID will NOT be included in the list of gameIds (private games are filtered out) and thus will need to added manually
        if (game.isPrivateGame()) {
            gameIds.add(game.getId());
        }

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //publish each of the individual game docs
            for (String gameIdToPublish : gameIds) {
                publishGameWithExtrasToWds(gameIdToPublish, _dao, _shoutContestService, _gameStatsHandler);
            }

            //publish the payouts table (even though it is subject to change up until bracket play begins)
            // this will give an approximation assuming max players join
            List<Round> rounds = _shoutContestService.getRoundsForGame(game.getId());
            publishPayoutTable(game, rounds, null, rounds.get(0).getMaximumPlayerCount(), _engineCoordinator, wdsService, _dao, _logger);

            _transactionManager.commit(txStatus);
            txStatus = null;

        } catch (PayoutManagerException e) {
            //this shouldn't happen - any errors should have been caught before this
            _logger.error(MessageFormat.format("Unable to generate payout table: {0}\n\t{1}", e.getErrorTypeCode(), e.getErrorDetails()));

        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }
    }

    public void startPoolPlay(Game game, List<Round> rounds)
    throws NoQuestionsPossibleException
    {
        if (rounds == null) {
            rounds = _shoutContestService.getRoundsForGame(game.getId());
        }

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //go through each round and make sure there are questions that will work for it. if there aren't, later on in the MME it will blow up
            for (Round r : rounds) {
                int minDifficulty = r.getActivityMinimumDifficulty() == null ? Round.MIN_DIFFICULTY : r.getActivityMinimumDifficulty();
                int maxDifficulty = r.getActivityMaximumDifficulty() == null ? Round.MAX_DIFFICULTY : r.getActivityMaximumDifficulty();
                String languageCodesAsCommaDelimitedList = r.getRoundNames().keySet().stream().collect(Collectors.joining(","));

                List<String> questionUuids = getQuestionIdsBasedOnCriteria(_dao, minDifficulty, maxDifficulty, languageCodesAsCommaDelimitedList, r.getCategories());
                if (questionUuids == null || questionUuids.size() == 0) {
                    throw new NoQuestionsPossibleException(r.getId());
                }
            }

            //grab all of the pool play rounds and set them to OPEN
            rounds.stream()
                .filter(r -> r.getRoundType() == Round.ROUND_TYPE.POOL)
                .forEach(r -> {
                    _shoutContestService.updateRoundStatus(r.getId(), r.isFinalRound(), Round.ROUND_STATUS.OPEN);
                }
            );

            //republish the WDS doc
            publishGameWithExtrasToWds(game.getId(), _dao, _shoutContestService, _gameStatsHandler);

            _transactionManager.commit(txStatus);
            txStatus = null;

        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }
    }

    //wrapped in a transaction
    public List<Round> startBracketPlay(Game game, List<Round> rounds, IWebDataStoreService wdsService, long beginsInMs)
    throws PoolPlayMatchesInProgressException
    {
        if (rounds == null) {
            rounds = _shoutContestService.getRoundsForGame(game.getId());
        }

        //grab all of the pool play rounds and set them to FULL - this will prevent anyone else from joining, and allow any in progress to complete
_logger.info(MessageFormat.format("BBP (1/13): changing all POOL rounds to FULL for game: {0} [{1}] and waiting for in progress matches to complete", game.getId(), game.getGameName("en")));
        rounds.stream()
            .filter(r -> r.getRoundType() == Round.ROUND_TYPE.POOL)
            .forEach(r -> {
                _shoutContestService.updateRoundStatus(r.getId(), r.isFinalRound(), Round.ROUND_STATUS.FULL);
            });

        //if there are any in progress games, do not continue
        int outstandingMatches = _shoutContestService.getMatchesByEngineAndStatusAndGame(
                ISnowyowlService.GAME_ENGINE, game.getEngineType(), game.getId(),
                Match.MATCH_STATUS.NEW, Match.MATCH_STATUS.OPEN, Match.MATCH_STATUS.WAITING_FOR_NEXT_QUESTION, Match.MATCH_STATUS.WAITING_FOR_TIEBREAKER_QUESTION, Match.MATCH_STATUS.PROCESSING)
                .size();
        if (outstandingMatches > 0) {
            _logger.info("unable to start bracket play. there are " + outstandingMatches + " pool play matches still in play.");
            throw new PoolPlayMatchesInProgressException();
        }

        //publish initial "game starts in" WDS doc
        publishBracketPlayStartCountdown(game.getId(), beginsInMs, wdsService);

        //change the game state to INPLAY (no new players can enter)
_logger.info(MessageFormat.format("BBP (2/13): changing staus to INPLAY for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        _shoutContestService.updateGameStatus(game.getId(), Game.GAME_STATUS.INPLAY);

        //set all of the pool play rounds to closed
_logger.info(MessageFormat.format("BBP (3/13): changing all POOL rounds to CLOSED for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        rounds.stream()
            .filter(r -> r.getRoundType() == Round.ROUND_TYPE.POOL)
            .forEach(r -> {
                _shoutContestService.updateRoundStatus(r.getId(), r.isFinalRound(), Round.ROUND_STATUS.CLOSED);
            });

        return rounds;
    }

    public void completeBracketPlayStart(
        Game game, List<Round> rounds, Round round, long beginsInMs, long then, IWebDataStoreService wdsService, Socket socketIoSocket)
    {
        List<Long> botSubscriberIds;
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
_logger.info(MessageFormat.format("BBP (4/13): releasing bots used during POOL play for game: {0} [{1}]", game.getId(), game.getGameName("en")));
            //get the bots in the game, then release all the bots
            botSubscriberIds = _botEngine.getBotsForGame(game.getId());
            _dao.releaseBotsForGame(game.getId());

            //also remove any GamePlayer records for those bots
            for (long botSubscriberId : botSubscriberIds) {
                _shoutContestService.removeGamePlayer(botSubscriberId, game.getId());
            }

            //get everyone who's in the game and move them into the first bracket round (except the bots. clear out all the bots)
            List<GamePlayer> gamePlayers = _shoutContestService.getGamePlayersForGame(game.getId());
            List<Long> bracketPlayerIds = new ArrayList<>();
            Map<Long, RoundPlayer> joinGameRoundPlayerMap = new HashMap<>();

            int actualPlayers = 0;
_logger.info(MessageFormat.format("BBP (5/13): moving POOL players to BRACKET round for game: {0} [{1}]", game.getId(), game.getGameName("en")));
            for (GamePlayer gp : gamePlayers) {
                long subscriberId = gp.getSubscriberId();

                //filter out the bots
                if (botSubscriberIds.contains(subscriberId)) {
                    continue;
                }

                //filter out free players
                if (gp.isFreeplay()) {
                    continue;
                }

                actualPlayers++;
                bracketPlayerIds.add(subscriberId);

                //add the RoundPlayer record (move the rank forward from the previous round)
                RoundPlayer roundPlayer = new RoundPlayer(game.getId(), round.getId(), subscriberId);
                RoundPlayer oldRoundPlayer = _shoutContestService.getMostRecentRoundPlayer(game.getId(), subscriberId);
                if (oldRoundPlayer != null) {
                    roundPlayer.setRank(oldRoundPlayer.getRank());
                    roundPlayer.setSkillAnswerCorrectPct(oldRoundPlayer.getSkillAnswerCorrectPct());
                    roundPlayer.setSkillAverageAnswerMs(oldRoundPlayer.getSkillAverageAnswerMs());
                    roundPlayer.setSkill(oldRoundPlayer.getSkill());
                }

                //reset the number of lives back to the full amount
_logger.info(MessageFormat.format("*** moving subscriber {0} into bracket play. # lives left: {1}, should be: {2}", gp.getSubscriberId(), gp.getCountdownToElimination(), gp.getTotalLives()));
                //if (gp.getCountdownToElimination() != gp.getTotalLives()) {
                    gp.setCountdownToElimination(gp.getTotalLives());
                    _shoutContestService.updateGamePlayer(gp);
                //}

_logger.info(MessageFormat.format(">>> CommonBusinessLogic::completeBracketPlayStart, addRoundPlayer. roundId: {0}, subscriberId: {1}", roundPlayer.getRoundId(), roundPlayer.getSubscriberId()));
                _shoutContestService.addRoundPlayer(roundPlayer, round.getRoundType());

//                //add to the match queue (make them eligible for pairing)
//                _shoutContestService.enqueueMatchQueue(game.getId(), round.getId(), roundPlayer.getId(), subscriberId);
                _matchMaker.addPlayerForMatching(roundPlayer);

                joinGameRoundPlayerMap.put(subscriberId, roundPlayer);
            }

_logger.info(MessageFormat.format("BBP (6/13): adding bots to game. actualPlayerCount (i.e. not bots and non freeplayers): {0}", actualPlayers));
            int numBotsAdded = 0;

            //if there aren't the right amount of players, add in bots as necessary (odd-man out)
            botSubscriberIds.clear();
            int matchPlayerCount = round.getMatchPlayerCount();
            if (actualPlayers < round.getMaximumPlayerCount() && actualPlayers % matchPlayerCount != 0) {
                int numBotsNeeded = matchPlayerCount - (actualPlayers % matchPlayerCount);
                numBotsAdded += numBotsNeeded;
                botSubscriberIds = _botEngine.addBotPlayers(game.getId(), round.getId(), numBotsNeeded);
                if (_logger.isDebugEnabled()) {
                    _logger.debug(MessageFormat.format("added {0} bot[s] to BRACKET play due to odd man out", numBotsNeeded));
                }
            }

            //if the debug flag is set to fill up the game with bots, do so now
            //if (_percentageToFillBracketWithBots > 0D) {
            if (game.isFillWithBots()) {
                int totalPlayers = actualPlayers + botSubscriberIds.size();
_logger.info("game.isFillWithBots is TRUE. totalPlayer count pre-fill (real players + odd man out bots): " + totalPlayers);
                //int numPlayersNeededToFillToDesiredAmount = (int) (_percentageToFillBracketWithBots / 100D * round.getMaximumPlayerCount());
                int numPlayersNeededToFillToDesiredAmount = round.getMaximumPlayerCount() - totalPlayers;

                //if specified, cap the maximum number of bots that can be added
                if (game.getMaxBotFillCount() != null && numPlayersNeededToFillToDesiredAmount > game.getMaxBotFillCount()) {
                    numPlayersNeededToFillToDesiredAmount = game.getMaxBotFillCount();
                }

                if (numPlayersNeededToFillToDesiredAmount > 0) {
                    //int numBotsNeeded = numPlayersNeededToFillToDesiredAmount - totalPlayers;
                    numBotsAdded += numPlayersNeededToFillToDesiredAmount;
                    botSubscriberIds.addAll(_botEngine.addBotPlayers(game.getId(), round.getId(), numPlayersNeededToFillToDesiredAmount));
                    if (_logger.isDebugEnabled()) {
                        _logger.debug(MessageFormat.format("added {0} bot[s] to BRACKET play due to game.fillWithBots and game.maxBotFillCount", numPlayersNeededToFillToDesiredAmount));
                    }
                }
            }

            //update the next round to have the same subscribers and set the status to READY so pairing can begin
            round.setCurrentPlayerCount(actualPlayers + numBotsAdded);
_logger.info(MessageFormat.format("BBP (7/13): updating bracket count to {0} for game: {1} [{2}]", round.getCurrentPlayerCount(), game.getId(), game.getGameName("en")));
            _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());

            //republish the player count (might have changed now that there are bots)
            Map<String, Integer> playerCountMap = _shoutContestService.publishGamePlayerCountToWds(game.getId(), _botEngine.getBotsForGame(game.getId()));

            //per bx, 25 Oct 2018, create ALL bracket rounds up front, now that the player count is known
            int numBracketRoundsToCreate = _engineCoordinator.getNumberOfBracketRounds(game, round.getCurrentPlayerCount()) -1; //-1 because the first bracket round has already been created
            Round roundToBeCloned = round;
            for (int i=0; i<numBracketRoundsToCreate; i++) {
                Round newRound = roundToBeCloned.cloneForNextRound();
                _shoutContestService.addRound(newRound);
                roundToBeCloned = newRound;
            }

            //republish the WDS doc
_logger.info(MessageFormat.format("BBP (8/13): republishing WDS doc for game: {0} [{1}]", game.getId(), game.getGameName("en")));
            publishGameWithExtrasToWds(game.getId(), _dao, _shoutContestService, _gameStatsHandler);

            try {
                //figure out the final payout table and publish it to the WDS
_logger.info(MessageFormat.format("BBP (9/13): publishing payout table for game: {0} [{1}]", game.getId(), game.getGameName("en")));
                /*List<PayoutTableRow> rows =*/ publishPayoutTable(game, rounds, round.getCurrentPlayerCount(), rounds.get(0).getMaximumPlayerCount(), _engineCoordinator, wdsService, _dao, _logger);

                //publish doc with total number of players for this round
                publishBracketRoundCount(game.getId(), round.getId(), round.getCurrentPlayerCount(), wdsService);

                //update the game stats table with the total number of players remaining in the game
                _gameStatsHandler.setGameStats(new GameStats(game.getId())
                        .withRemainingPlayers(round.getCurrentPlayerCount())
                        .withRemainingSavePlayerCount(PayoutManagerFixedRoundCommon.getNumberOfSaves(round.getCurrentPlayerCount()))
                );

                //publish the subscriber stats documents
                if (game.isProductionGame()) {
                    _subscriberStatsHandler.publishSubscriberStatsDocuments(game.getId(), bracketPlayerIds);
                }

            } catch (Exception e) {
                _logger.error("unable to publish bracket count and game stats", e);
            }

            //_logger.info("sending joined_round sync message to all players");
            for (long subscriberId : bracketPlayerIds) {

                //send the "joined_round" sync message
                Subscriber s = _identityService.getSubscriberById(subscriberId);
                enqueueSyncMessage(
                        _jsonMapper, _syncService, _logger,
                        game.getId(), ISnowyowlService.SYNC_MESSAGE_JOINED_ROUND,
                        new FastMap<String, Object>(
                                "roundPlayer", joinGameRoundPlayerMap.get(subscriberId),
                                "payedPlayerCount", playerCountMap.get("payedPlayerCount"),
                                "freePlayerCount", playerCountMap.get("freePlayerCount"),
                                "roundType", "BRACKET"
                        ), s, socketIoSocket, _triggerService);
            }

            //notify everyone that bracket play is about to begin
_logger.info(MessageFormat.format("BBP (10/13): notifying players that BRACKET play is about to begin for game: {0} [{1}]", game.getId(), game.getGameName("en")));
            float playerPot;
            GamePayout gamePayout = _dao.getGamePayout(game.getId());
            try {
                playerPot = _engineCoordinator.getPlayerPot(game, rounds, round.getCurrentPlayerCount(), gamePayout);

            } catch (PayoutManagerException e) {
                //this shouldn't happen - any errors should have been caught before this
                _logger.error(MessageFormat.format("Unable to generate payout table: {0}\n\t{1}", e.getErrorTypeCode(), e.getErrorDetails()));
                playerPot = 0F;
            }

            long beginsInMins = beginsInMs / 1000L / 60L;
            String link;
            try {
                link = getShortUrl(_urlShortenerService, _shortUrlDomain, _shortUrlPrefix, "https://" + _shortUrlDomain + "/play/game/" + game.getId());
            } catch (UrlShortenerException | IOException e) {
                _logger.error("unable to generate short url", e);
                link = "";
            }
_logger.info(MessageFormat.format("playerPot: ${0}, gameId: {1}, payoutModelId: {2}, playerCount: {3}, shortUrl: {4}", playerPot, game.getId(), gamePayout.getPayoutModelId(), actualPlayers, link));

            sendGameNotifications(
                bracketPlayerIds, ISnowyowlService.NOTIFICATION_PREF_TYPE_ROUND_START, "EMAIL", game,

                bracketPlayBeginsBodyLocalizationUuid, new Object[] {"~GAMENAME~", beginsInMins, playerPot, link},

                "BRACKET_PLAY_ABOUT_TO_BEGIN",
                bracketPlayBeginsTitleLocalizationUuid, null,
                bracketPlayBeginsBodyLocalizationUuid, new Object[] {"~GAMENAME~", beginsInMins, playerPot, link},
                new FastMap<>("beginsInMs", beginsInMs+"", "purse", playerPot, "gameName", "~GAMENAME~"),

                bracketPlayBeginsEmailSubjectUuid, null,
                bracketPlayBeginsBodyLocalizationUuid, new Object[] {"~GAMENAME~", beginsInMins, playerPot, link}
            );

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        //it took some amount of time to process everything. take that time difference into account before starting the final countdown
        long now = System.currentTimeMillis();
        beginsInMs -= (now - then);

        //wait for the (remaining) pre-arranged amount of time before beginning so that people can have a chance to get ready
_logger.info(MessageFormat.format(
        "BBP (11/13): waiting {0} ms before starting BRACKET play for game: {1} [{2}]",
        beginsInMs > 0 ? beginsInMs : 0, game.getId(), game.getGameName("en")));

        //start the wait countdown, updating the countdown WDS doc every second (or so)
        while (beginsInMs > 0) {
            long aMs = System.currentTimeMillis();
            try {
                Thread.sleep(1_000L);
                long bMs = System.currentTimeMillis();

                beginsInMs -= (bMs - aMs);
                publishBracketPlayStartCountdown(game.getId(), beginsInMs, wdsService);

            } catch (InterruptedException e) {
                _logger.warn("BRACKET play waiting thread was prematurely interrupted", e);
            }
        }

_logger.info(MessageFormat.format("BBP (12/13): setting first BRACKET round to OPEN for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        round.setRoundStatus(Round.ROUND_STATUS.OPEN);
        _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());

_logger.info(MessageFormat.format("BBP (13/13): beginning BRACKET play for game: {0} [{1}], round: {2}", game.getId(), game.getGameName("en"), round.getId()));
        //_engineCoordinator.runMQE(game);
        _matchMaker.createBracketMatches(game, round, true);
    }

    /**
     * Send either an email, an sms or a push to a list of subscribers, depending on their individual notification pref for the type of notification.
     *
     * Make sure this method is wrapped in a transaction!
     *
     * There are a few special cases where the localized value can't be known at call time and so place holders are available to use in their place
     * which will be replaced when this method runs. Put these in the xxxLocalizedxxxParams objects and the pushExtrasMap:
     * <ul>
     *      <li>~GAMENAME~</li>
     *      <li>~SUBSCRIBERNAME~</li>
     * </ul>
     *
     * @param subscriberIds the list of subscribers who will receive this notification
     * @param prefType which type of pref to send.
     * <ul>
     *      <li>ISnowyowlService.NOTIFICATION_PREF_TYPE_ROUND_START</li>
     * </ul>
     * @param defaultSendTypeIfNotSet "SMS", "EMAIL", "PUSH", or null (meaning don't send if not set)
     * @param game .
     * @param smsLocalizedUuid the "systemMessage" uuid of the localized string for sms messages of this type
     * @param smsLocalizedParams the list of params to put into the sms body (if any); null if there are none
     * @param pushType .
     * @param pushLocalizedTitleUuid the "systemMessage" uuid of the localized string for push message title of this type
     * @param pushLocalizedTitleParams the list of params to put into the push title (if any); null if there are none
     * @param pushLocalizedMessageUuid the "systemMessage" uuid of the localized string for push message messages of this type
     * @param pushLocalizedMessageParams the list of params to put into the push message (if any); null if there are none
     * @param pushExtrasMap .
     * @param emailLocalizedSubjectUuid the "systemMessage" uuid of the localized string for the email subject of this type
     * @param emailLocalizedSubjectParams the list of params to put into the email subject (if any); null if there are none
     * @param emailLocalizedBodyUuid the "systemMessage" uuid of the localized string for the email body of this type
     * @param emailLocalizedBodyParams the list of params to put into the email body (if any); null if there are none
     */
    public void sendGameNotifications(
        List<Long> subscriberIds, int prefType, String defaultSendTypeIfNotSet, Game game,
        String smsLocalizedUuid, Object[] smsLocalizedParams,
        String pushType, String pushLocalizedTitleUuid, Object[] pushLocalizedTitleParams, String pushLocalizedMessageUuid, Object[] pushLocalizedMessageParams, Map<String, Object> pushExtrasMap,
        String emailLocalizedSubjectUuid, Object[] emailLocalizedSubjectParams, String emailLocalizedBodyUuid, Object[] emailLocalizedBodyParams)
    {
        //SMS one time initialization
        List<Tuple<String>> smsLocalizedBodyList = _dao.getMultiLocalizationValues(smsLocalizedUuid, "systemMessage");
        Map<String, String> smsLocalizedBodyMap = BaseSmMessageHandler.tupleListToMap(smsLocalizedBodyList);

        //PUSH one time initialization
        List<Tuple<String>> pushLocalizedTitleList = _dao.getMultiLocalizationValues(pushLocalizedTitleUuid, "systemMessage");
        Map<String, String> pushLocalizedTitleMap = BaseSmMessageHandler.tupleListToMap(pushLocalizedTitleList);
        List<Tuple<String>> pushLocalizedMessageList = _dao.getMultiLocalizationValues(pushLocalizedMessageUuid, "systemMessage");
        Map<String, String> pushLocalizedMessageMap = BaseSmMessageHandler.tupleListToMap(pushLocalizedMessageList);

        //EMAIL one time initialization
        List<Tuple<String>> emailLocalizedSubjectList = _dao.getMultiLocalizationValues(emailLocalizedSubjectUuid, "systemMessage");
        Map<String, String> emailLocalizedSubjectMap = BaseSmMessageHandler.tupleListToMap(emailLocalizedSubjectList);
        List<Tuple<String>> emailLocalizedBodyList = _dao.getMultiLocalizationValues(emailLocalizedBodyUuid, "systemMessage");
        Map<String, String> emailLocalizedBodyMap = BaseSmMessageHandler.tupleListToMap(emailLocalizedBodyList);

        Map<Integer, EmailAddress> fromEmailMap = new HashMap<>();
        String[] addrsByAppId = _emailFromAddrs.split(",");
        String[] namesByAppId = _emailFromNames.split(",");

        for (int i=0; i<addrsByAppId.length; i++) {
            String[] ss = addrsByAppId[i].split(":");
            int appId = Integer.parseInt(ss[0]);
            String emailAddr = ss[1];
            String emailName = namesByAppId[i].split(":")[1];

            fromEmailMap.put(appId, new EmailAddress(emailAddr, emailName));
        }

        for (long subscriberId : subscriberIds) {
            Subscriber subscriber = _identityService.getSubscriberById(subscriberId);

            //determine which type of notification to send this subscriber for this notification type
            List<NotificationPref> prefs = _notificationService.getPrefsForSubscriber(subscriberId);
            Optional<NotificationPref> oPref = prefs.stream().filter(pref -> pref.getPrefType() == prefType).findFirst();

            //if there is a default pref override if not set, use it
            NotificationPref pref;
            if (!oPref.isPresent()) {
                pref = new NotificationPref();
                pref.setValue(defaultSendTypeIfNotSet != null ? defaultSendTypeIfNotSet : "NONE");
            } else {
                pref = oPref.get();
            }

            switch (pref.getValue())
            {
                case "SMS":
                    String smsLocalizedBody = smsLocalizedParams != null ?
                        MessageFormat.format(LocalizationHelper.getLocalizedString(smsLocalizedBodyMap, subscriber.getLanguageCode()), smsLocalizedParams) :
                        LocalizationHelper.getLocalizedString(smsLocalizedBodyMap, subscriber.getLanguageCode());
                    smsLocalizedBody =  replaceStringPlaceHolderWithActualString(smsLocalizedBody, subscriber, game);

                    sendSms(
                        _twilioFromNumber, subscriber.getPhone(), smsLocalizedBody, _twilioAccountSid, _twilioAuthToken,
                        _logger, subscriberId, _identityService, true);
                    break;

                case "APP_PUSH":
                    String pushLocalizedTitle = pushLocalizedTitleParams != null ?
                        MessageFormat.format(LocalizationHelper.getLocalizedString(pushLocalizedTitleMap, subscriber.getLanguageCode()), pushLocalizedTitleParams) :
                        LocalizationHelper.getLocalizedString(pushLocalizedTitleMap, subscriber.getLanguageCode());
                    pushLocalizedTitle =  replaceStringPlaceHolderWithActualString(pushLocalizedTitle, subscriber, game);

                    String pushLocalizedMessage = pushLocalizedMessageParams != null ?
                        MessageFormat.format(LocalizationHelper.getLocalizedString(pushLocalizedMessageMap, subscriber.getLanguageCode()), pushLocalizedMessageParams) :
                        LocalizationHelper.getLocalizedString(pushLocalizedMessageMap, subscriber.getLanguageCode());
                    pushLocalizedMessage =  replaceStringPlaceHolderWithActualString(pushLocalizedMessage, subscriber, game);

                    if (pushExtrasMap != null) {
                        for (String key : pushExtrasMap.keySet()) {
                            Object val = pushExtrasMap.get(key);
                            if (val instanceof String) {
                                if ("~GAMENAME~".equals(val)) {
                                    pushExtrasMap.put(key, game.getGameName(subscriber.getLanguageCode()));
                                } else if ("~SUBSCRIBERNAME~".equals(val)) {
                                    pushExtrasMap.put(key, subscriber.getFirstname() + " " + subscriber.getLastname());
                                }
                            }
                        }
                    }

                    sendGamePush(
                        _transactionManager, _dao, _pushService, _logger,
                        subscriberId, subscriber.getLanguageCode(), game, "apsCategory:TODO", pushLocalizedTitle, pushLocalizedMessage,
                        pushType, pushExtrasMap);
                    break;

                case "EMAIL":
                    String emailLocalizedSubject = emailLocalizedSubjectParams != null ?
                        MessageFormat.format(LocalizationHelper.getLocalizedString(emailLocalizedSubjectMap, subscriber.getLanguageCode()), emailLocalizedSubjectParams) :
                        LocalizationHelper.getLocalizedString(emailLocalizedSubjectMap, subscriber.getLanguageCode());
                    emailLocalizedSubject =  replaceStringPlaceHolderWithActualString(emailLocalizedSubject, subscriber, game);

                    String emailLocalizedBody = emailLocalizedBodyParams != null ?
                        MessageFormat.format(LocalizationHelper.getLocalizedString(emailLocalizedBodyMap, subscriber.getLanguageCode()), emailLocalizedBodyParams) :
                        LocalizationHelper.getLocalizedString(emailLocalizedBodyMap, subscriber.getLanguageCode());
                    emailLocalizedBody =  replaceStringPlaceHolderWithActualString(emailLocalizedBody, subscriber, game);

                    sendEmail(
                        _logger, subscriberId, _identityService, _postOfficeService,
                        fromEmailMap.get(subscriber.getContextId()),
                        emailLocalizedSubject, emailLocalizedBody, null, null);
                    break;
            }
        }
    }

    private String replaceStringPlaceHolderWithActualString(String in, Subscriber subscriber, Game game)
    {
        String out = in.replace("~GAMENAME~", game.getGameName(subscriber.getLanguageCode()));
        out = out.replace("~SUBSCRIBERNAME~", subscriber.getFirstname() + " " + subscriber.getLastname());
        return out;
    }

    private void publishBracketPlayStartCountdown(String gameId, long beginsInMs, IWebDataStoreService wdsService)
    {
        publishJsonWdsDoc(
            _logger, wdsService, new Date(new Date().getTime() + EXPIRES_1_HOUR_MS),
            "/" + gameId + "/bracketplay_countdown.json",
            new FastMap<>("beginsInMs", beginsInMs));
    }

    private void publishBracketRoundCount(String gameId, String roundId, int numPlayers, IWebDataStoreService wdsService)
    {
        publishJsonWdsDoc(
            _logger, wdsService, null,
            "/" + gameId + "/" + "numplayers_" + roundId + ".json",
            new FastMap<>("count", numPlayers));
    }


}
