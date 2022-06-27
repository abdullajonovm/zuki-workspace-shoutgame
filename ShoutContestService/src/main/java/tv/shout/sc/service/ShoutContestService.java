package tv.shout.sc.service;

import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.gameplay.domain.App;
import com.meinc.gameplay.domain.Language;
import com.meinc.gameplay.domain.Tuple;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.IWebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.IMessageTypeHandler;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;

import tv.shout.collector.ICollectorMessageHandler;
import tv.shout.sc.dao.IContestDaoMapper;
import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.Country;
import tv.shout.sc.domain.CouponBatch;
import tv.shout.sc.domain.CouponCode;
//import tv.shout.sc.domain.ActivityCategory;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Game.GAME_STATUS;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.GamePlayer.GAME_PLAYER_DETERMINATION;
import tv.shout.sc.domain.ManualRedeemRequest;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.Match.MATCH_STATUS;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.MatchQueue;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.util.JsonUtil;

@Service(
    namespace=       IShoutContestService.SERVICE_NAMESPACE,
    name=            IShoutContestService.SERVICE_NAME,
    interfaces=      IShoutContestService.SERVICE_INTERFACE,
    version=         IShoutContestService.SERVICE_VERSION,
    exposeAs=        IShoutContestService.class
)
public class ShoutContestService
implements IShoutContestService
{
    private static Logger _logger = Logger.getLogger(ShoutContestService.class);
    private static ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();

    @Autowired
    private IContestDaoMapper dao;

    @Autowired
    private PlatformTransactionManager _transactionManager;

    @Autowired
    private ICollectorMessageHandler[] allMessageHandlers;

    @Autowired
    private IWebCollectorService _webCollectorService;

    @Autowired@Resource(name="webDataStoreService")
    private IWebDataStoreService _wdsService;

    private Map<Integer, App> _appByIdMap;
    private Map<String, App> _appByNameMap;

    //hold each of the collector handlers (key=PATH)
    private Map<String, IMessageTypeHandler> _collectorMessageHandlersByPath = new HashMap<String, IMessageTypeHandler>();
    //hold each of the collector handlers (key=MESSAGE_TYPE)
    private Map<String, IMessageTypeHandler> _collectorMessageHandlerByType = new HashMap<String, IMessageTypeHandler>();

    @Override
    @ServiceMethod
    @OnStart
    public void start()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("ShoutContestService starting...");
        }

        //register the collector handlers
        registerMessageHandlers(allMessageHandlers);

        initAppMaps();
        publishCountries();

        //ServiceEndpoint myEndpoint = new ServiceEndpoint();
        //myEndpoint.setServiceName(IShoutContestService.SERVICE_NAME);
        //myEndpoint.setVersion(IShoutContestService.SERVICE_VERSION);

        _logger.info("ShoutContestService started");
    }

    @Override
    @ServiceMethod
    @OnStop
    public void stop()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("ShoutContestService stopping...");
        }

        //unregister all of the collector handlers
        try {
            _collectorMessageHandlersByPath.forEach(
                    (k,v) -> _webCollectorService.unregisterMessageTypeHandler(_collectorMessageHandlersByPath.get(k).getHandlerMessageType()) );

        } catch (Exception e) {
            //oh well; ignore it.
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("ShoutContestService stopped");
        }
    }

//    public <T> RollbackActions addRollbackAction(T item, Consumer<T> action, String errorMessage, Object...errorMessageArgs) {
//        return null;
//    }
//
//    public <T> RollbackActions addRollbackActions(Collection<T> items, Consumer<T> action, String errorMessage, Object...errorMessageArgs) {
//        return null;
//    }

    private void registerMessageHandlers(IMessageTypeHandler ... handlers)
    {
        ServiceEndpoint serviceEndpoint = new ServiceEndpoint();
        serviceEndpoint.setNamespace(SERVICE_NAMESPACE);
        serviceEndpoint.setServiceName(SERVICE_NAME);
        serviceEndpoint.setVersion(SERVICE_VERSION);

        Arrays.asList(handlers).forEach(
            (handler) -> {
                _collectorMessageHandlerByType.put(handler.getHandlerMessageType(), handler);

                for (int i=0; i < handler.getCollectorEndpoints().length; i++) {
                    _collectorMessageHandlersByPath.put(handler.getCollectorEndpoints()[i].getPath(), handler);
                    _webCollectorService.registerMessageTypeHandler(handler.getCollectorEndpoints()[i], handler.getHandlerMessageType(), serviceEndpoint);
                }
            }
        );
    }

    @Override
    @ServiceMethod
    public Integer getContextId(Map<String, String> props)
    {
        String appName = props.get("appId");
        if (appName == null || appName.trim().length() == 0 || appName.trim().equalsIgnoreCase("null")) {
            return null;
        }

        App app = _appByNameMap.get(appName);
        return app == null ? null : app.getAppId();
    }

    @Override
    @ServiceMethod
    public App getAppById(int appId)
    {
        return _appByIdMap.get(appId);
    }

    @Override
    @ServiceMethod
    public Set<String> getAppBundleIds(int appId)
    {
        App app = _appByIdMap.get(appId);
        Set<String> appBundleIds = new HashSet<String>();
        if (app != null) {
            if (app.getAndroidBundleId() != null) {
                appBundleIds.add(app.getAndroidBundleId());
            }
            if (app.getiOSBundleId() != null) {
                appBundleIds.add(app.getiOSBundleId());
            }
            if (app.getWindowsBundleId() != null) {
                appBundleIds.add(app.getWindowsBundleId());
            }
        }
        return appBundleIds;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<CashPoolTransaction2> getCashPoolTransactionsForSubscriberForTypes(long subscriberId, List<String> transactionTypes)
    {
        String transactionTypeAsCommaDelimitedList = transactionTypes.stream().collect(Collectors.joining(","));
        return dao.getCashPoolTransactionsForSubscriberForTypes(subscriberId, transactionTypeAsCommaDelimitedList);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public Double getTotalBalance(long subscriberId)
    {
        CashPoolTransaction2 cpt = dao.getMostRecentCashPoolTransactionForSubscriber(subscriberId);
        return cpt == null ? null : cpt.getCurrentBonusAmount() + cpt.getCurrentPoolAmount();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public Double getAvailableBalance(long subscriberId)
    {
        CashPoolTransaction2 cpt = dao.getMostRecentCashPoolTransactionForSubscriber(subscriberId);
        return cpt == null ? null : cpt.getCurrentPoolAmount();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void addCashPoolTransaction(long subscriberId, double amount, CashPoolTransaction2.TYPE type, String desc, Integer receiptId, String contextUuid)
    {
        CashPoolTransaction2 cpt2 = new CashPoolTransaction2();

        //these copy straight over
        cpt2.setSubscriberId(subscriberId);
        cpt2.setAmount(amount);
        cpt2.setType(type);
        cpt2.setDescription(desc);
        cpt2.setReceiptId(receiptId);
        cpt2.setContextUuid(contextUuid);
        cpt2.setTransactionDate(new Date());

        //the previous row is needed to compute various values in the new row
        CashPoolTransaction2 previousCpt2 =  dao.getMostRecentCashPoolTransactionForSubscriber(subscriberId);
        if (previousCpt2 == null) {
            previousCpt2 = new CashPoolTransaction2();
        }

        switch (cpt2.getType())
        {
            case BONUS: {
                cpt2.setCurrentPoolAmount(previousCpt2.getCurrentPoolAmount());
                cpt2.setCurrentBonusAmount(previousCpt2.getCurrentBonusAmount() + cpt2.getAmount());
            }
            break;

            case PURCHASE:
            case PAYOUT:
            case PAYOUT_REFERRAL:
            case PAID:
                cpt2.setCurrentPoolAmount(previousCpt2.getCurrentPoolAmount() + cpt2.getAmount());
                cpt2.setCurrentBonusAmount(previousCpt2.getCurrentBonusAmount());
            break;

            case JOINED_ROUND:
            case PURCHASED_EXTRA_LIFE:
                cpt2.setCurrentPoolAmount(previousCpt2.getCurrentPoolAmount());
                cpt2.setCurrentBonusAmount(previousCpt2.getCurrentBonusAmount());

                //deduct from bonus first if there's enough
                double totalToDeduct = -(cpt2.getAmount());
                double amountTakenFromBonus = 0D;
                double amountTakenFromPool = 0D;

                if (cpt2.getCurrentBonusAmount() > 0 && cpt2.getCurrentBonusAmount() >= totalToDeduct) {
                    //take full amount from bonus
                    cpt2.setCurrentBonusAmount(cpt2.getCurrentBonusAmount() - totalToDeduct);
                    amountTakenFromBonus = totalToDeduct;
                    totalToDeduct = 0D;

                } else if (cpt2.getCurrentBonusAmount() > 0) {
                    //take as much as possible from bonus
                    amountTakenFromBonus = cpt2.getCurrentBonusAmount();
                    cpt2.setCurrentBonusAmount(0D);
                    totalToDeduct -= amountTakenFromBonus;
                }

                if (totalToDeduct > 0) {
                    //take remaining from pool
                    amountTakenFromPool = totalToDeduct;
                    cpt2.setCurrentPoolAmount(cpt2.getCurrentPoolAmount() - amountTakenFromPool);
                    //totalToDeduct = 0D;
                }

                cpt2.setUsedBonusAmount(amountTakenFromBonus);
                cpt2.setUsedPoolAmount(amountTakenFromPool);
            break;

            case ABANDONED_ROUND: {
                cpt2.setCurrentPoolAmount(previousCpt2.getCurrentPoolAmount());
                cpt2.setCurrentBonusAmount(previousCpt2.getCurrentBonusAmount());

                cpt2.setCurrentPoolAmount(cpt2.getCurrentPoolAmount() +  (previousCpt2.getUsedPoolAmount() == null ? 0D : previousCpt2.getUsedPoolAmount()) );
                cpt2.setCurrentBonusAmount(cpt2.getCurrentBonusAmount() + (previousCpt2.getUsedBonusAmount() == null ? 0D : previousCpt2.getUsedBonusAmount()) );
            }
            break;
        }

        dao.addCashPoolTransaction(cpt2);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public ManualRedeemRequest getManualRedeemRequest(int manualRedeemRequestId)
    {
        return dao.getManualRedeemRequest(manualRedeemRequestId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void markManualRedeemRequestFulfilled(int manualRedeemRequestId)
    {
        dao.markManualRedeemRequestFulfilled(manualRedeemRequestId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<ManualRedeemRequest> getOutstandingManualRedeemRequests()
    {
        return dao.getOutstandingManualRedeemRequests();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<CashPoolTransaction2> getJoinedAndAbandonedForContext(String contextId)
    {
        return dao.getJoinedAndAbandonedForContext(contextId);
    }

    // Game

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Game> getGamesByStatus(GAME_STATUS status)
    {
        //filter in the db
        List<String> dbGameIds = dao.getGameIdsByStatus(status);

        //grab all the game data
        List<Game> games = new ArrayList<>(dbGameIds.size());
        dbGameIds.forEach(gameId -> {
            games.add(getGameById(gameId));
        });

        return games;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Game> getGamesByStatusAndAllowableAppId(GAME_STATUS status, int appId)
    {
        //filter in the db
        List<String> dbGameIds = dao.getGameIdsByStatusAndAllowableAppId(status, appId);

        //grab all the game data
        List<Game> games = new ArrayList<>(dbGameIds.size());
        dbGameIds.forEach(gameId -> {
            games.add(getGameById(gameId));
        });

        return games;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Game> getGamesByStatusAndEngine(String gameEngine, Game.GAME_STATUS... statuses)
    {
        //convert the varargs to a comma-delimited string
        String val = Arrays.stream(statuses).map(s -> s.toString()).collect(Collectors.joining(","));

        //filter in the db
        List<String> dbGameIds = dao.getGameIdsByEngineAndStatus(gameEngine, val);

        //grab all the game data
        List<Game> games = new ArrayList<>(dbGameIds.size());
        dbGameIds.forEach(gameId -> {
            games.add(getGameById(gameId));
        });

        return games;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void addGame(Game game)
    {
        if (game.getId() == null) {
            throw new IllegalStateException("game has no ID!");
        }

        if (game.getAdditionalLifeCost() == null) {
            game.setAdditionalLifeCost(0D);
        }

        //add the game itself
        dao.insertOrReplaceGame(game);

        //add the localization rows
        game.getGameNames().forEach( (k,v) -> {
            dao.insertOrReplaceMultiLocalizationValue(game.getId(), "gameName", k, v);
        } );
        game.getGameDescriptions().forEach( (k,v) -> {
            dao.insertOrReplaceMultiLocalizationValue(game.getId(), "gameDescription", k, v);
        } );
        game.getFetchingActivityTitles().forEach( (k,v) -> {
            dao.insertOrReplaceMultiLocalizationValue(game.getId(), "fetchingActivityTitle", k, v);
        } );
        game.getSubmittingActivityTitles().forEach( (k,v) -> {
            dao.insertOrReplaceMultiLocalizationValue(game.getId(), "submittingActivityTitle", k, v);
        } );
        if (game.getGuideHtmls() != null) {
            game.getGuideHtmls().forEach( (k,v) -> {
                dao.insertOrReplaceMultiLocalizationValue(game.getId(), "guideHtml", k, v);
            } );
        }

        //add the join rows
        game.getAllowableAppIds().forEach(appId -> {
            dao.insertOrReplaceGameAppId(game.getId(), appId);
        });
        game.getAllowableLanguageCodes().forEach(languageCode -> {
            dao.insertOrReplaceGameLanguageCodes(game.getId(), languageCode);
        });
        game.getForbiddenCountryCodes().forEach(countryCode -> {
            dao.insertOrReplaceGameForbiddenCountryCodes(game.getId(), countryCode);
        });

    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateGame(Game game)
    {
        updateGameHelper(game);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateGameThin(Game game)
    {
        dao.updateGame(game);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateGameStatus(String gameId, Game.GAME_STATUS newStatus)
    {
        switch (newStatus)
        {
            case CANCELLED:
                dao.cancelGame(gameId);
                break;

            case CLOSED:
                dao.setGameStatusClosed(gameId);
                break;

            case INPLAY:
                dao.setGameStatusInplay(gameId);
                break;

            case OPEN:
                dao.setGameStatusOpen(gameId);
                break;
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void setGameSmsSent(String gameId)
    {
        dao.setGameSmsSentDate(gameId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public Game getGame(String gameId)
    {
        return getGameById(gameId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public Game getGameNoFat(String gameId)
    {
        Game game = dao.getGame(gameId);
        game.setAllowableLanguageCodes(dao.getGameAllowableLanguageCodes(game.getId()));
        return game;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public Game getGameViaInviteCode(String inviteCode)
    {
        if (inviteCode == null) return null;
        String gameId = dao.getGameIdViaInviteCode(inviteCode);
        if (gameId == null) return null;
        return getGameById(gameId);
    }

//    @Override
//    @ServiceMethod
//    @Transactional(propagation=NESTED)
//    public void resetGame(String gameId)
//    {
//        Game game = getGameById(gameId);
//        if (game == null) return;
//
//        // Treat this as if the game was won, closed and re-opened as if it were new.
//        // Remove all game players, and all round players and all match players for this game.
//        // Reset all game and round counters and dates.
//        dao.resetGamePlayerTable(gameId);
//        dao.resetRoundPlayerTable(gameId);
//        dao.resetMatchPlayerTable(gameId);
//        dao.resetMatchTable(gameId);
//        dao.resetMatchQueueTable(gameId);
//
//        Date now = new Date();
//        game.setActualStartDate(now);
//        game.setExpectedStartDate(now);
//        game.setGameStatus(GAME_STATUS.OPEN);
//
//        List<Round> rounds = dao.getRoundsForGame(gameId); //no need to fatten; not using any of those fields
//        for(Round round : rounds) {
//            round.setCurrentPlayerCount(0);
//            round.setRoundStatus(ROUND_STATUS.OPEN);
//            round.setStartDate(now);
//
//            //since only a few known fields have changed, no need to remove/restore the join table or localization values
//            dao.updateRound(round);
//        }
//
//        updateGameHelper(game);
//    }

    // GamePlayer
    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public boolean addGamePlayer(GamePlayer gamePlayer, String nextRoundId, Round.ROUND_STATUS... roundStatuses)
    {
        if (nextRoundId == null) {
            String statusesAsCommaDelimitedString = Arrays.stream(roundStatuses).map(rs -> rs.toString()).collect(Collectors.joining(","));

            //find the "first" round in the game (don't need the extra stuff; just seeing if it passes muster to use the id)
            List<Round> roundsForGame = dao.getRoundsForGameForStatus(
                    gamePlayer.getGameId(), statusesAsCommaDelimitedString);

            Optional<Round> oRound = roundsForGame.stream()
                .sorted( Comparator.comparing(Round::getRoundSequence, Comparator.nullsLast(Comparator.naturalOrder())) )
                .findFirst();
            if (!oRound.isPresent()) {
                _logger.warn(MessageFormat.format("game {0} has no rounds; unable to add player", gamePlayer.getGameId()));
                return false;
            }

            nextRoundId = oRound.get().getId();
        }

        gamePlayer.setNextRoundId(nextRoundId);
        dao.insertOrReplaceGamePlayer(gamePlayer);

        return true;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Game> getSubscriberGamesByStatus(Game.GAME_STATUS status, int appId, long subscriberId)
    {
        //filter in the db
        List<String> dbGameIds = dao.getSubscriberGameIdsByStatus(status, appId, subscriberId);

        //grab all the game data
        List<Game> games = new ArrayList<>(dbGameIds.size());
        dbGameIds.forEach(gameId -> {
            games.add(getGameById(gameId));
        });

        return games;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<String> getSubscriberGameIdsByStatuses(int appId, long subscriberId, Game.GAME_STATUS... statuses)
    {
        String val = Arrays.stream(statuses).map(s -> s.toString()).collect(Collectors.joining(","));
        return dao.getSubscriberGameIdsByStatuses(appId, subscriberId, val);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public GamePlayer getGamePlayer(String gameId, long subscriberId)
    {
        return dao.getGamePlayer(gameId, subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void removeGamePlayer(long subscriberId, String gameId)
    {
        dao.removeGamePlayer(subscriberId, gameId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<GamePlayer> getGamePlayers(long subscriberId)
    {
        return dao.getGamePlayers(subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<GamePlayer> getGamePlayersForGame(String gameId)
    {
        return dao.getGamePlayersForGame(gameId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<GamePlayer> getGamePlayersWithoutBots(String gameId, List<Long> botIds)
    {
        String botIdsAsCommaDelimitedString = botIds.stream().map(s -> s.toString()).collect(Collectors.joining(","));
        List<GamePlayer> gamePlayers = dao.getCurrentGamePlayerCount(gameId, botIdsAsCommaDelimitedString);
        return gamePlayers;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Game> getGamesByStatusByGameIds(int appId, Game.GAME_STATUS status, List<String> gameIds)
    {
        //filter in the db
        List<String> dbGameIds = dao.getGameIdsByStatusAndAllowableAppId(status, appId);

        //further filtering in code
        List<String> filteredGameIds = dbGameIds.stream()
                .filter(id -> gameIds.contains(id))
                .collect(Collectors.toList());

        //whatever survived the filtering, grab all the game data
        List<Game> games = new ArrayList<>(filteredGameIds.size());
        filteredGameIds.forEach(gameId -> {
            games.add(getGameById(gameId));
        });

        return games;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void cancelGame(String gameId)
    {
        dao.cancelGame(gameId);
        dao.cancelGamePlayersForGame(gameId);
        dao.cancelRoundsForGame(gameId);
        dao.cancelRoundPlayersForGame(gameId);
        dao.cancelMatchesForGame(gameId);
        dao.cancelMatchPlayersForGame(gameId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void openGame(String gameId)
    {
        dao.setGameStatusOpen(gameId);
        dao.setRoundStatusesVisibleForNewlyOpenedGame(gameId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void cloneGame(String gameId, String inviteCode, List<Date> expectedStartDatesForEachRoundBeingCloned, Map<String, String> gameNames, List<String> roundIdsToClone)
    {
        String newGameId = UUID.randomUUID().toString();
        String newInviteCode = inviteCode == null ? null : generateRandomString(Game.INVITE_CODE_LENGTH);

        dao.cloneGame(gameId, newGameId, newInviteCode);
        dao.cloneGameMultilocalizationValues(gameId, newGameId);
        dao.cloneGameAllowableAppIds(gameId, newGameId);
        dao.cloneGameAllowableLanguageCodes(gameId, newGameId);
        dao.cloneGameForbiddenCountryCodes(gameId, newGameId);

        for (String languageCode : gameNames.keySet()) {
            dao.cloneUpdateGameName(newGameId, languageCode, gameNames.get(languageCode));
        }

        for (int i=0; i<expectedStartDatesForEachRoundBeingCloned.size(); i++) {
            String roundId = roundIdsToClone.get(i);
            Date expectedStartDate = expectedStartDatesForEachRoundBeingCloned.get(i);
            String newRoundId = UUID.randomUUID().toString();

            dao.cloneRound(newGameId, roundId, newRoundId, expectedStartDate);
            dao.cloneRoundMultilocalizationValues(roundId, newRoundId);
            dao.cloneRoundCategories(roundId, newRoundId);
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Round> getRoundsForGameForStatus(String gameId, Round.ROUND_STATUS... statuses)
    {
        //convert the varargs to a comma-delimited string
        String val = Arrays.stream(statuses).map(s -> s.toString()).collect(Collectors.joining(","));

        List<Round> rounds = dao.getRoundsForGameForStatus(gameId, val);
        rounds.stream().forEach(r -> fattenRound(r));

        return rounds;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void addRound(Round round)
    {
        //add the round
        dao.insertOrReplaceRound(round);

        //add the localization rows
        round.getRoundNames().forEach( (k,v) -> {
            dao.insertOrReplaceMultiLocalizationValue(round.getId(), "roundName", k, v);
        } );

        //add the joins
        round.getCategories().forEach( category -> {
            dao.insertOrReplaceRoundCategory(round.getId(), category);
        } );
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateRound(Round round)
    {
        //remove join data
        dao.removeMutliLocalizationValues(round.getId(), "roundName");
        round.getCategories().forEach(category -> {
            dao.removeRoundCategory(round.getId(), category);
        });

        //update the round
        dao.updateRound(round);

        //add the localization rows
        round.getRoundNames().forEach( (k,v) -> {
            dao.insertOrReplaceMultiLocalizationValue(round.getId(), "roundName", k, v);
        } );

        //add the joins
        round.getCategories().forEach( category -> {
            dao.insertOrReplaceRoundCategory(round.getId(), category);
        } );
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateRoundStatus(String roundId, boolean finalRound, Round.ROUND_STATUS newStatus)
    {
        switch (newStatus)
        {
            case CLOSED:
                dao.setRoundStatusClosed(roundId, finalRound);
                break;

            case FULL:
                dao.setRoundStatusFull(roundId, finalRound);
                break;

            case INPLAY:
                dao.setRoundStatusInplay(roundId, finalRound);
                break;

            case OPEN:
                dao.setRoundStatusOpen(roundId, finalRound);
                break;

            case VISIBLE:
                dao.setRoundStatusVisible(roundId, finalRound);
                break;
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateRoundStatusAndPlayerCount(String roundId, Round.ROUND_STATUS newStatus, int newPlayerCount)
    {
        dao.updateRoundStatusAndPlayerCount(roundId, newStatus, newPlayerCount);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void addRoundPlayer(RoundPlayer roundPlayer, Round.ROUND_TYPE roundType)
    {
        switch (roundType)
        {
            case  BINARY:
                dao.insertRoundPlayer(roundPlayer);
                break;
            case BRACKET:
            case POOL:
                dao.insertOrReplaceRoundPlayer(roundPlayer);
                break;
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public Round getRound(String roundId)
    {
        return getRoundById(roundId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public RoundPlayer getRoundPlayerByDetermination(String gameId, String roundId, long subscriberId, RoundPlayer.ROUND_PLAYER_DETERMINATION determination)
    {
        //this makes the assumption that the dao layer is ordering the list by create_date desc so that the first
        //one in the list is the most recent. if this isn't true after switching out the dao, do a code sort first
        List<RoundPlayer> roundPlayers = dao.getRoundPlayerForDetermination(gameId, roundId, subscriberId, determination);
        return roundPlayers != null && roundPlayers.size() > 0 ? roundPlayers.get(0) : null;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public RoundPlayer getMostRecentRoundPlayer(String gameId, long subscriberId)
    {
        //this makes the assumption that the dao layer is ordering the list by create_date desc so that the first
        //one in the list is the most recent. if this isn't true after switching out the dao, do a code sort first
        return dao.getMostRecentRoundPlayer(gameId, subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public RoundPlayer getRoundPlayer2(String roundId, long subscriberId)
    {
        return dao.getRoundPlayer2(roundId, subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<RoundPlayer> getMostRecentRoundPlayersForGame(String gameId, List<Round.ROUND_TYPE> roundTypes)
    {
        String roundTypeCommaDelimitedList = roundTypes.stream().map(rt -> rt.toString()).collect(Collectors.joining(","));
        return dao.getMostRecentRoundPlayersForGame(gameId, roundTypeCommaDelimitedList);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Round> getRoundsForGame(String gameId)
    {
        List<Round> rounds = dao.getRoundsForGame(gameId);
        rounds.stream().forEach(r -> fattenRound(r));
        return rounds;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<RoundPlayer> getRoundPlayersForGame(String gameId, long subscriberId)
    {
        return dao.getRoundPlayersForGame(gameId, subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<RoundPlayer> getMostRecentRoundPlayerForEachPlayerForGame(String gameId)
    {
        //returns all round players, order by create date (asc)
        List<RoundPlayer> allRoundPlayers = dao.getAllRoundPlayersForGame(gameId);

        //by putting them into a map, each time a round player is overridden, it's a newer one, and thus we're left with the newest ones
        Map<Long, RoundPlayer> map = new HashMap<>();
        for (RoundPlayer rp : allRoundPlayers) {
            map.put(rp.getSubscriberId(), rp);
        }

        //map down to a list
        List<RoundPlayer> result = new ArrayList<>();
        for (Entry<Long, RoundPlayer> x : map.entrySet()) {
            result.add(x.getValue());
        }

        return result;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Long> getSubscriberIdsForRound(String roundId)
    {
        return dao.getSubscriberIdsForRound(roundId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateRoundPlayerDetermination(String roundPlayerId, RoundPlayer.ROUND_PLAYER_DETERMINATION determination)
    {
        dao.updateRoundPlayerDetermination(roundPlayerId, determination);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public int getMatchCountForRound(String roundId)
    {
        return dao.getMatchCountForRound(roundId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public Match addMatch(Match match)
    {
        dao.insertOrReplaceMatch(match);
        return match;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public Match getMatch(String matchId)
    {
        return dao.getMatch(matchId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public MatchPlayer addMatchPlayer(String gameId, String roundId, String matchId, String roundPlayerId, long subscriberId)
    {
        if (roundPlayerId == null) throw new IllegalArgumentException("roundPlayerId may not be null!");
        MatchPlayer mp = new MatchPlayer(gameId, roundId, matchId, roundPlayerId, subscriberId);
        dao.insertOrReplaceMatchPlayer(mp);
        return mp;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void enqueueMatchQueue(String gameId, String roundId, String roundPlayerId, long subscriberId/*, boolean demoStore*/)
    {
        MatchQueue matchQueue = new MatchQueue(
                UUID.randomUUID().toString(),
                gameId, roundId, roundPlayerId, subscriberId//, demoStore
        );
        matchQueue.setEnqueueTimestamp(new Date());
        dao.insertOrReplaceMatchQueue(matchQueue);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<MatchQueue> getMatchPlayersNotQueuedOlderThan(String gameId, String roundId, long ageMs)
    {
        return dao.getMatchPlayersNotQueuedOlderThan(gameId, roundId, ageMs);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Long> getSubscriberIdsThatWereNeverMatchedForGame(String gameId)
    {
        return dao.getSubscriberIdsThatWereNeverMatchedForGame(gameId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void removeSubscribersThatWereNeverMatchedForGame(String gameId)
    {
        dao.removeSubscribersThatWereNeverMatchedForGame(gameId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public MatchQueue cancelMatchQueue(String gameId, String roundId, long subscriberId)
    {
        // Update the record specified with the dequeueTimestamp
        // Update the record as isCancelled = true
        dao.cancelMatchQueue(gameId, roundId, subscriberId, new Date());

        List<MatchQueue> mqs = dao.getPlayerMatchQueue(gameId, roundId, subscriberId);
        return mqs != null && mqs.size() > 0 ? mqs.get(0) : null;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public MatchQueue getPlayerAvailableMatchQueue(String gameId, String roundId, long subscriberId)
    {
        List<MatchQueue> mqs = dao.getPlayerAvailableMatchQueue(gameId, roundId, subscriberId);
        return mqs != null && mqs.size() > 0 ? mqs.get(0) : null;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public List<List<MatchQueue>> getMatchQueues(String gameId, String roundId, int matchPlayerCount, MATCH_QUEUE_ALGORITHM algorithm)
    {
        //get any match queues that are open for building matches
        List<MatchQueue> matchQueues = dao.getOpenMatchQueues(gameId, roundId);
//_logger.info(MessageFormat.format("# of matchQueues for game {0} and round {1}: {2}", gameId, roundId, matchQueues.size()));

        //order the list according to the given algorithm
        if (algorithm == null) algorithm = MATCH_QUEUE_ALGORITHM.NATURAL_ORDER;
        switch (algorithm)
        {
            case RANDOM:
                Collections.shuffle(matchQueues);
                break;

            case BY_OPPOSITE_SKILL:
                Map<String, RoundPlayer> matchQueueIdToRoundPlayerMap = new HashMap<>();

                //grab each RoundPlayer object for each MatchQueue and store in lookup map
                matchQueues.stream().forEach(mq -> {
                    String roundPlayerId = mq.getRoundPlayerId();
                    RoundPlayer roundPlayer = dao.getRoundPlayer(roundPlayerId);
                    matchQueueIdToRoundPlayerMap.put(mq.getId(), roundPlayer);
                });

                //map each matchQueue to a roundPlayer and use the roundPlayer rank to order high to low
                Collections.sort(matchQueues, new Comparator<MatchQueue>() {
                    @Override
                    public int compare(MatchQueue o1, MatchQueue o2)
                    {
                        RoundPlayer rp1 = matchQueueIdToRoundPlayerMap.get(o1.getId());
                        RoundPlayer rp2 = matchQueueIdToRoundPlayerMap.get(o2.getId());

                        double r1 = rp1.getSkill() == null ? 0 : rp1.getSkill();
                        double r2 = rp2.getSkill() == null ? 0 : rp2.getSkill();

                        return r1 == r2 ? 0 : r1 < r2 ? 1 : -1;
                    }
                });

                //now do the bracket ranking, which is first paired with last, second paired with next to last, etc.
                List<MatchQueue> list = new ArrayList<MatchQueue>(matchQueues); //wrap it in a modifiable list first
                int idx = 0;
                while (idx < list.size()-2) {
                    list.add(idx+1, list.remove(list.size()-1));
                    idx += 2;
                }
                matchQueues = list;

                break;

            default: //NATURAL_ORDER
                //no-op
                break;
        }

        //pair them off
        List<List<MatchQueue>> matches = new ArrayList<>();
        int offset = 0;
        while (matchQueues.size() - offset >= matchPlayerCount) {
            List<MatchQueue> match = new ArrayList<>(matchPlayerCount);
            for (int i=0; i<matchPlayerCount; i++) {
                MatchQueue mq = matchQueues.get(offset+i);
                match.add(mq);

                mq.setDequeueTimestamp(new Date());
                dao.updateMatchQueue(mq);
            }
            matches.add(match);

            offset+= matchPlayerCount;
        }

        return matches;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<MatchQueue> getOpenMatchQueues(String gameId, String roundId, int matchPlayerCount)
    {
        return dao.getOpenMatchQueues(gameId, roundId);
    }

//    @Override
//    @ServiceMethod
//    @Transactional(propagation=NESTED)
//    public List<MatchQueue> bundleMatchQueues(String gameId, String roundId, Integer matchPlayerCount, MATCH_QUEUE_ALGORITHM algorithm)
//    {
//        //
//        // Get the open match queues for this game and this round
//        // If there are no records - no match can be made
//        // If there unsufficient records (x < N) then no match can be made
//        // If there enough queued players, loop through them based on the algorithm
//        // For each one, try and perform the update of the "dequeueTimestamp"
//        //   If that effort is successful then this bundle 'owns' that MatchQueue record
//        // If the process is successful at updating N MatchQueue records, then a full match can be created
//        //   return that list of updated MatchQueue records as a bundle for the game engine to create a match and match player records.
//        // If the update attempt fails, then this thread failed to 'own' that record, move onto the next.
//        // If at the end of the loop, insufficient MatchQueue records were 'owned' then
//        //   undo the update of the "dequeueTimestamp" so that those records can be matched at a later time
//        //
//
//        //
//        // Get any match queues that are open for building at match
//        //
//        if (algorithm == null) algorithm = MATCH_QUEUE_ALGORITHM.NATURAL_ORDER;
//
//        List<MatchQueue> matchQueues;
//        switch (algorithm)
//        {
//            case RANDOM:
//                matchQueues = dao.getOpenMatchQueues(gameId, roundId);
//                Collections.shuffle(matchQueues);
//                break;
//
//            case RANKED:
//                break;
//
//            default: //NATURAL_ORDER
//                matchQueues = dao.getOpenMatchQueues(gameId, roundId);
//                break;
//        }
//
//        if (matchQueues != null && matchQueues.size() >= matchPlayerCount) {
//
//            int count = 0;
//            List<MatchQueue> bundledMatchQueues = new ArrayList<MatchQueue>(matchPlayerCount);
//
//            for (MatchQueue mq : matchQueues) {
//                //
//                // Try to update the dequeueTimestamp
//                //
//                mq.setDequeueTimestamp(new Date());
//                MatchQueue dequeuedMq = this.dequeueMatchQueue(mq);
//
//                //
//                // If we dequeued this record then we 'own' it
//                //
//                if (dequeuedMq != null){
//                    bundledMatchQueues.add(dequeuedMq);
//                    count++;
//                }
//
//                //
//                // If we dequeued enough records - return them now.
//                //
//                if (count == matchPlayerCount){
//                    return bundledMatchQueues;
//                }
//            }
//
//            //
//            // Unclaim any dequeued matchQueues
//            // execution gets here only if there are not enough MatchQueue records to create a bundle
//            //
//            for (MatchQueue mq : bundledMatchQueues){
//                mq.setDequeueTimestamp(null);
//                dao.insertOrReplaceMatchQueue(mq);
//            }
//        }
//
//        //
//        // Not enough queued players to build a match
//        //
//        return null;
//    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Match> getMatchesByEngineAndStatus(String gameEngine, String engineType, MATCH_STATUS... statuses)
    {
        //convert the varargs to a comma-delimited string
        String val = Arrays.stream(statuses).map(s -> s.toString()).collect(Collectors.joining(","));

        return dao.getMatchesByStatusAndEngine(gameEngine, engineType, val);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Match> getMatchesByEngineAndStatusAndGame(String gameEngine, String engineType, String gameId, MATCH_STATUS... statuses)
    {
        //convert the varargs to a comma-delimited string
        String val = Arrays.stream(statuses).map(s -> s.toString()).collect(Collectors.joining(","));

        return dao.getMatchesByEngineAndStatusAndGame(gameEngine, engineType, gameId, val);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void removeGamePlayer(GamePlayer existingGamePlayerRecord)
    {
        existingGamePlayerRecord.setDetermination(GAME_PLAYER_DETERMINATION.REMOVED);
        dao.updateGamePlayer(existingGamePlayerRecord);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public Round getRoundForGameAndSequence(String gameId, int sequence)
    {
        return getRoundById(dao.getRoundIdForGameAndSequence(gameId, sequence));
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public RoundPlayer getRoundPlayer(String roundPlayerId)
    {
        return dao.getRoundPlayer(roundPlayerId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateGamePlayer(GamePlayer gamePlayer)
    {
        dao.updateGamePlayer(gamePlayer);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateRoundPlayer(RoundPlayer roundPlayer)
    {
        dao.updateRoundPlayer(roundPlayer);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateMatchPlayer(MatchPlayer matchPlayer)
    {
        dao.updateMatchPlayer(matchPlayer);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<MatchPlayer> getMatchPlayersForMatch(String matchId)
    {
        return dao.getMatchPlayersForMatch(matchId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateMatch(Match match)
    {
        dao.updateMatch(match);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Match> getMatchesByRoundAndStatus(String roundId, MATCH_STATUS... statuses)
    {
        //convert the varargs to a comma-delimited string
        String val = Arrays.stream(statuses).map(s -> s.toString()).collect(Collectors.joining(","));

        return dao.getMatchesByRoundAndStatus(roundId, val);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public CouponBatch createCouponBatch(CouponBatch couponBatch, int quantity)
    {
        dao.createCouponBatch(couponBatch); //the newly generated will be set on the object that was passed in
        couponBatch = dao.getCouponBatch(couponBatch.getBatchId()); //to get the proper createDate

        CouponCode couponCode = new CouponCode();
        couponCode.setBatchId(couponBatch.getBatchId());
        couponCode.setAmount(couponBatch.getAmount());
        couponCode.setExpireDate(couponBatch.getExpireDate());

        for (int i=0; i<quantity; i++) {
            couponCode.setCouponCode(generateRandomString(8));
            dao.createCouponCode(couponCode);
        }

        return couponBatch;
    }

    @Override
    @ServiceMethod
    public String generateRandomString(int len)
    {
        return Game.generateRandomString(len);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<String> getPrivateGameIdsForSubscriberByStatusAndEngine(long subscriberId, String gameEngine, GAME_STATUS... statuses)
    {
        String val = Arrays.stream(statuses).map(s -> s.toString()).collect(Collectors.joining(","));
        return dao.getPrivateGameIdsForSubscriberByStatusAndEngine(subscriberId, gameEngine, val);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void cancelCouponBatch(int batchId)
    {
        dao.cancelCouponCodesInBatch(batchId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public CouponCode getCouponCode(String couponCode)
    {
        return dao.getCouponCode(couponCode);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void cancelCouponCode(String couponCode)
    {
        dao.cancelCouponCode(couponCode);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<CouponBatch> getCouponBatches()
    {
        return dao.getCouponBatches();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<CouponCode> getCouponsForBatch(int batchId)
    {
        return dao.getCouponsForBatch(batchId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<CouponCode> getUnusedCoupons()
    {
        return dao.getUnusedCoupons();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<CouponCode> getCouponsRedeemedSince(Date since)
    {
        return dao.getCouponsRedeemedSince(since);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void assignCouponsToSubscribersFromBatch(List<Long> subscriberIds, int batchId)
    {
        //get all of the available coupons in thie batch
        List<CouponCode> allCouponsInBatch = getCouponsForBatch(batchId);
        List<CouponCode> availableCoupons = allCouponsInBatch.stream()
                .filter(c ->
                    c.getRedeemedDate() == null &&
                    (c.getExpireDate() == null || c.getExpireDate().after(new Date())) &&
                    !c.isCancelled())
                .collect(Collectors.toList());

        //if there are less available coupons in the batch than there are subscribers, new coupons will be created to meet the demand
        if (subscriberIds.size() > availableCoupons.size()) {
            //grab the batch
            CouponBatch couponBatch = dao.getCouponBatch(batchId);

            int numToCreate = subscriberIds.size() - availableCoupons.size();
            for (int i=0; i<numToCreate; i++) {
                CouponCode couponCode = new CouponCode();
                couponCode.setBatchId(batchId);
                couponCode.setAmount(couponBatch.getAmount());
                couponCode.setExpireDate(couponBatch.getExpireDate());
                couponCode.setCouponCode(UUID.randomUUID().toString());

                //add to db and list
                dao.createCouponCode(couponCode);
                availableCoupons.add(couponCode);
            }
        }

        //assign the coupons (at this point the coupon list is at least as large as the subscriber id list
        for (int i=0; i<subscriberIds.size(); i++) {
            long subscriberId = subscriberIds.get(i);
            CouponCode couponCode = availableCoupons.get(i);

            //mark in db
            markCouponAsRedeemed(couponCode.getCouponCode(), subscriberId);

            //give bonus cash
            addCashPoolTransaction(subscriberId, couponCode.getAmount(), CashPoolTransaction2.TYPE.BONUS, "redeemed coupon", null, null);
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void markCouponAsRedeemed(String couponCode, long subscriberId)
    {
        dao.markCouponAsRedeemed(couponCode, subscriberId);
    }

//    @Override
//    @ServiceMethod
//    @Transactional(propagation=NESTED)
//    public Map<String, Object> joinGame(Map<String, String> props, String messageId, Subscriber subscriber)
//    throws PublishResponseError
//    {
//        // Verify they are allowed to play the game
//
//        String gameId = props.get("gameId");
//        if (gameId == null) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "joinGame", false, "missingRequiredParam", "gameId");
//        }
//        Game game = this.getGame(gameId);
//        if (game == null) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "joinGame", false, "invalidParam", "gameId");
//        }
//        if (subscriber.getFromCountryCode() != null && game.getForbiddenCountryCodes() != null && game.getForbiddenCountryCodes().contains(subscriber.getFromCountryCode())) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "joinGame", false, "accessDenied", "fromCountryCode");
//        }
//        if (game.getAllowableLanguageCodes() != null && !game.getAllowableLanguageCodes().contains(subscriber.getLanguageCode())) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "joinGame", false, "accessDenied", "languageCode");
//        }
//        if (game.getAllowableAppIds() != null && !game.getAllowableAppIds().contains(subscriber.getContextId())) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "joinGame", false, "accessDenied", "appId");
//        }
//
//        //see if already in the game
//        GamePlayer existingGamePlayerRecord = this.getGamePlayer(gameId, subscriber.getSubscriberId());
//        if (existingGamePlayerRecord != null) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "joinGame", false, "alreadyJoined", null);
//        }
//
//        // Add (or update) the GamePlayer record
//        GamePlayer gamePlayer = new GamePlayer(gameId, subscriber.getSubscriberId());
//        this.addGamePlayer(gamePlayer);
//
//        // Null is OK - nothing to return, will still be success = true
//        return null;
//    }

//    @Override
//    @ServiceMethod
//    @Transactional(propagation=NESTED)
//    public Map<String, Object> ensureGamePlayer(Map<String, String> props, String messageId, Subscriber subscriber)
//    throws PublishResponseError
//    {
//        // Either join the player or ensure they have already joined
//        try{
//            return this.joinGame(props, messageId, subscriber);
//        }catch(PublishResponseError ex){
//            if (ex.getFailureType() == "alreadyJoined")
//                return null;
//            else
//                throw ex;
//        }
//    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<String> publishWebDataStoreGameListDoc(String gameEngineName, Game.GAME_TYPE gameType, String documentName, Game.GAME_STATUS... statuses)
    {
        //get the list of games matching the status and filter by gameType
        //business rule: do NOT publish private games
        List<Game> filteredGames = getGamesByStatusAndEngine(gameEngineName, statuses).stream()
                .filter(g -> g.getGameType() == gameType)
                .filter(g -> !g.isPrivateGame())
                .collect(Collectors.toList());

        WebDataStoreObject object = new WebDataStoreObject();
        object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
        //object.setExpirationDate(new Date(new Date().getTime() + EXPIRES_1_HOUR_MS));
        object.setPath(documentName);
        try {
            object.setData(_jsonMapper.writeValueAsBytes(filteredGames));
        } catch (JsonProcessingException e) {
            _logger.error("unable to convert games list to json", e);
            return null;
        }
        try {
            _wdsService.createOrUpdateObjectSync(object, 0);
        } catch (WebDataStoreException | InterruptedException e) {
            _logger.error("unable to publish " + documentName, e);
        }

        return filteredGames.stream().map(g -> g.getId()).collect(Collectors.toList());
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public void publishGameToWds(String gameId, Map<String, Object> extras)
    {
        Game game = dao.getGame(gameId);
        fattenGame(game);
        publishWdsGame(game, extras);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public Map<String, Integer> publishGamePlayerCountToWds(String gameId, List<Long> botIds)
    {
//nobody can recall why bots were filtered out, and they want the bot count as well, so add them back in ...
//rather than change the api, keep them here in case someone wants them filtered again later on
        //count all GamePlayer objects where determination != REMOVED, CANCELLED and not a bot
        //String botIdsAsCommaDelimitedString = botIds.stream().map(s -> s.toString()).collect(Collectors.joining(","));
        String botIdsAsCommaDelimitedString = "";

        List<GamePlayer> gamePlayers = dao.getCurrentGamePlayerCount(gameId, botIdsAsCommaDelimitedString);
        int freePlayerCount = (int) gamePlayers.stream()
            .filter(gp -> gp.isFreeplay())
            .count();
        int payedPlayerCount = (int) gamePlayers.stream()
                .filter(gp -> !gp.isFreeplay())
                .count();

        _logger.info(MessageFormat.format(
            "publishGamePlayerCountToWds, gameId: {0}, payedPlayerCount: {1}, freePlayerCount: {2}, botIds: {3}",
            gameId, payedPlayerCount, freePlayerCount, botIdsAsCommaDelimitedString));

        Map<String, Integer> map = new HashMap<>();
        map.put("payedPlayerCount", payedPlayerCount);
        map.put("freePlayerCount", freePlayerCount);

        WebDataStoreObject object = new WebDataStoreObject();
        object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
        //object.setExpirationDate(new Date(new Date().getTime() + EXPIRES_1_HOUR_MS));
        object.setPath("/" +gameId + "/currentPlayerCount.json");
        try {
            object.setData(_jsonMapper.writeValueAsBytes(map));
        } catch (JsonProcessingException e) {
            _logger.error("unable to convert currentPlayerCount to json", e);
            return map;
        }
        try {
            _wdsService.createOrUpdateObjectSync(object, 0);
        } catch (WebDataStoreException | InterruptedException e) {
            _logger.error("unable to publish currentPlayerCount.json for game: " + gameId, e);
        }

        return map;
    }

    private static class GameWithRounds
    extends Game
    {
        private static final long serialVersionUID = 1L;
        private List<Round> _rounds;
        private Map<String, Object> _extras;

        public static GameWithRounds fromGame(Game game, List<Round> rounds, Map<String, Object> extras)
        {
            GameWithRounds g = new GameWithRounds();
            g.setId(game.getId());
            g.setGameEngine(game.getGameEngine());
            g.setGameNames(game.getGameNames());
            g.setGameDescriptions(game.getGameDescriptions());
            g.setProducer(game.getProducer());
            g.setGamePhotoUrl(game.getGamePhotoUrl());
            g.setGameStatus(game.getGameStatus());
            g.setBracketEliminationCount(game.getBracketEliminationCount());
            g.setAllowBots(game.isAllowBots());
            g.setUseDoctoredTimeForBots(game.isUseDoctoredTimeForBots());
            g.setFillWithBots(game.isFillWithBots());
            g.setMaxBotFillCount(game.getMaxBotFillCount());
            g.setPairImmediately(game.isPairImmediately());
            g.setCanAppearInMobile(game.isCanAppearInMobile());
            g.setProductionGame(game.isProductionGame());
            g.setPrivateGame(game.isPrivateGame());
            g.setInviteCode(game.getInviteCode());
            g.setFetchingActivityTitles(game.getFetchingActivityTitles());
            g.setSubmittingActivityTitles(game.getSubmittingActivityTitles());
            g.setAllowableAppIds(game.getAllowableAppIds());
            g.setAllowableLanguageCodes(game.getAllowableLanguageCodes());
            g.setForbiddenCountryCodes(game.getForbiddenCountryCodes());
            g.setEngineType(game.getEngineType());
            g.setIncludeActivityAnswersBeforeScoring(game.isIncludeActivityAnswersBeforeScoring());
            g.setStartingLivesCount(game.getStartingLivesCount());
            g.setAdditionalLifeCost(game.getAdditionalLifeCost());
            g.setMaxLivesCount(game.getMaxLivesCount());
            g.setGuideUrl(game.getGuideUrl());
            g.setGuideHtmls(game.getGuideHtmls());
            g.setAutoStartPoolPlay(game.isAutoStartPoolPlay());
            g.setAutoStartBracketPlay(game.isAutoStartBracketPlay());
            g.setAutoBracketPlayPreStartNotificationTimeMs(game.getAutoBracketPlayPreStartNotificationTimeMs());
            g.setPendingDate(game.getPendingDate());
            g.setCancelledDate(game.getCancelledDate());
            g.setOpenDate(game.getOpenDate());
            g.setInplayDate(game.getInplayDate());
            g.setClosedDate(game.getClosedDate());

            g._rounds = rounds;
            g._extras = extras = extras == null ? new HashMap<>() : extras;

            return g;
        }

        @SuppressWarnings("unused")
        public List<Round> getRounds()
        {
            return _rounds;
        }

        @SuppressWarnings("unused")
        public Map<String, Object> getExtras()
        {
            return _extras;
        }
    }

    private void publishWdsGame(Game game, Map<String, Object> extras)
    {
        if (game == null) return;

        List<Round> rounds = getRoundsForGameForStatus(game.getId(),
                Round.ROUND_STATUS.OPEN, Round.ROUND_STATUS.FULL, Round.ROUND_STATUS.INPLAY, Round.ROUND_STATUS.CLOSED, Round.ROUND_STATUS.VISIBLE, Round.ROUND_STATUS.PENDING);
        GameWithRounds g = GameWithRounds.fromGame(game, rounds, extras);

        WebDataStoreObject object = new WebDataStoreObject();
        object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
        //object.setExpirationDate(new Date(new Date().getTime() + EXPIRES_1_HOUR_MS));
        object.setPath("/" + game.getId() + "/game.json");
        try {
            object.setData(_jsonMapper.writeValueAsBytes(g));
        } catch (JsonProcessingException e) {
            _logger.error("unable to convert game to json", e);
            return;
        }
        try {
            _wdsService.createOrUpdateObjectSync(object, 0);
        } catch (WebDataStoreException | InterruptedException e) {
            _logger.error("unable to publish game.json for game: " + game.getId(), e);
        }
    }

    private void publishCountries()
    {
        List<Country> countries = null;

        //grab the countries information from the database
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            countries = dao.getCountries();
            for (Country country : countries) {
                String countryCode = country.getCountryCode();
                List<Tuple<String>> countryNamesAsList = dao.getMultiLocalizationValues(IShoutContestService.COUNTRY_UUID, "cc_" + countryCode);
                country.setCountryNames(tupleListToMap(countryNamesAsList));
            }

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
                return;
            }
        }

        //publish to the wds
        WebDataStoreObject object = new WebDataStoreObject();
        object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
        object.setPath("/countries.json");
        try {
            object.setData(_jsonMapper.writeValueAsBytes(countries));
        } catch (JsonProcessingException e) {
            _logger.error("unable to convert countries to json", e);
            return;
        }
        try {
            _wdsService.createOrUpdateObjectSync(object, 0);
        } catch (WebDataStoreException | InterruptedException e) {
            _logger.error("unable to publish countries.json", e);
        }
    }

    /** INTERNAL METHOD. No public access. */
    @Override
    @ServiceMethod
    public CollectorMessageResult createMessage(String requestPath, Map<String, String> requestHeaders, Map<String, String> requestParameters)
    throws BadRequestException
    {
        IMessageTypeHandler handler = _collectorMessageHandlersByPath.get(requestPath);
        if (handler != null) {
            return handler.createMessage(requestPath, requestHeaders, requestParameters);
        } else {
            _logger.warn("received createMessage for unregistered path type: " + requestPath);
            throw new BadRequestException();
        }
    }

    /** INTERNAL METHOD. No public access. */
    @Override
    @ServiceMethod
    public String getHandlerMessageType()
    {
        throw new UnsupportedOperationException();
    }

    /** INTERNAL METHOD. No public access. */
    @Override
    @ServiceMethod
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        throw new UnsupportedOperationException();
    }

    /** INTERNAL METHOD. No public access. */
    //pass the Message off for handling. (all messages in the list will be of the same type)
    @Override
    @ServiceMethod
    public void handleMessages(List<CollectorMessage> messages, IWebCollectorMessageBuffer messageBuffer)
    throws BadRequestException
    {
        IMessageTypeHandler handler = _collectorMessageHandlerByType.get(messages.get(0).getMessageType());
        if (handler != null) {
            handler.handleMessages(messages, messageBuffer);
        } else {
            _logger.warn("received createMessage for unregistered type: " + messages.get(0).getMessageType());
            throw new BadRequestException();
        }
    }

    @Override
    @ServiceMethod
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException {
        throw new BadRequestException();
    }


    //http://stackoverflow.com/questions/15554296/simple-java-aes-encrypt-decrypt-example
    @Override
    @ServiceMethod
    public String aesEncrypt(String key, String initVector, String plainTextMessage)
    {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(plainTextMessage.getBytes());

            return new String(Base64.getEncoder().encode(encrypted), "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    //this will grab a game and all associated join data
    private Game getGameById(String gameId)
    {
        Game game = dao.getGame(gameId);
        fattenGame(game);
        return game;
    }

    private void fattenGame(Game game)
    {
        if (game == null) return;

        game.setGameNames(tupleListToMap(dao.getMultiLocalizationValues(game.getId(), "gameName")));
        game.setGameDescriptions(tupleListToMap(dao.getMultiLocalizationValues(game.getId(), "gameDescription")));
        game.setFetchingActivityTitles(tupleListToMap(dao.getMultiLocalizationValues(game.getId(), "fetchingActivityTitle")));
        game.setSubmittingActivityTitles(tupleListToMap(dao.getMultiLocalizationValues(game.getId(), "submittingActivityTitle")));
        game.setGuideHtmls(tupleListToMap(dao.getMultiLocalizationValues(game.getId(), "guideHtml")));
        game.setAllowableAppIds(dao.getGameAllowableAppIds(game.getId()));
        game.setAllowableLanguageCodes(dao.getGameAllowableLanguageCodes(game.getId()));
        game.setForbiddenCountryCodes(dao.getGameForbiddenCountryCodes(game.getId()));
    }

    private void updateGameHelper(Game game)
    {
        if (game == null) return;

        //remove all the old join records
        dao.deleteGameAppId(game.getId());
        dao.deleteGameLanguageCodes(game.getId());
        dao.deleteGameForbiddenCountryCodes(game.getId());
        dao.removeMutliLocalizationValues(game.getId(), "gameName");
        dao.removeMutliLocalizationValues(game.getId(), "gameDescription");
        dao.removeMutliLocalizationValues(game.getId(), "fetchingActivityTitle");
        dao.removeMutliLocalizationValues(game.getId(), "submittingActivityTitle");
        dao.removeMutliLocalizationValues(game.getId(), "guideHtml");

        //update the game itself
        dao.updateGame(game);

        //add the localization rows
        game.getGameNames().forEach( (k,v) -> {
            dao.insertOrReplaceMultiLocalizationValue(game.getId(), "gameName", k, v);
        } );
        game.getFetchingActivityTitles().forEach( (k,v) -> {
            dao.insertOrReplaceMultiLocalizationValue(game.getId(), "fetchingActivityTitle", k, v);
        } );
        game.getSubmittingActivityTitles().forEach( (k,v) -> {
            dao.insertOrReplaceMultiLocalizationValue(game.getId(), "submittingActivityTitle", k, v);
        } );
        if (game.getGuideHtmls() != null) {
            game.getGuideHtmls().forEach( (k,v) -> {
                dao.insertOrReplaceMultiLocalizationValue(game.getId(), "guideHtml", k, v);
            } );
        }

        //add the join rows
        game.getAllowableAppIds().forEach(appId -> {
            dao.insertOrReplaceGameAppId(game.getId(), appId);
        });
        game.getAllowableLanguageCodes().forEach(languageCode -> {
            dao.insertOrReplaceGameLanguageCodes(game.getId(), languageCode);
        });
        game.getForbiddenCountryCodes().forEach(countryCode -> {
            dao.insertOrReplaceGameForbiddenCountryCodes(game.getId(), countryCode);
        });
    }

    private Round getRoundById(String roundId)
    {
        if (roundId == null) return null;
        Round round = dao.getRound(roundId);
        fattenRound(round);
        return round;
    }

    private void fattenRound(Round round)
    {
        if (round == null) return;

        //add the localization rows
        round.setRoundNames(tupleListToMap(dao.getMultiLocalizationValues(round.getId(), "roundName")));

        //add the categories
        round.setCategories(dao.getRoundCategories(round.getId()));
    }

    public static <T> Map<T, T> tupleListToMap(List<Tuple<T>> list)
    {
        if (list == null) return null;

        Map<T, T> map = new HashMap<>(list.size());
        list.forEach(tuple -> {
            map.put(tuple.getKey(), tuple.getVal());
        });

        return map;
    }

    private void initAppMaps()
    {
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            _appByIdMap = new HashMap<>();
            _appByNameMap = new HashMap<>();
            List<App> apps = dao.getApps();
            for (App app : apps) {
                List<Language> languages = dao.getLanguageForApp(app.getAppId());
                app.setLanguageCodes(languages);
                _appByIdMap.put(app.getAppId(), app);
                _appByNameMap.put(app.getAppName(), app);
            }
            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
                return;
            }
        }
    }

}
