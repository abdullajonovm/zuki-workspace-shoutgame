package tv.shout.snowyowl.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.HandlerStyle;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.service.WebDataStoreException;

import io.socket.client.Socket;
import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_STATUS;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.snowyowl.common.PayoutTablePublisher;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.QuestionCategory;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.snowyowl.engine.PayoutManagerException;
import tv.shout.snowyowl.service.CurrentRankCalculator;
import tv.shout.snowyowl.service.CurrentRankCalculator.CurrentRankCalculatorCallback;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.snowyowl.service.SnowyowlService;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class GameHandler
extends BaseSmMessageHandler
implements SyncMessageSender, PayoutTablePublisher
{
    private static Logger _logger = Logger.getLogger(GameHandler.class);

    // Valid form post param names
    private static final List<String> _validFormVars = Arrays.asList(
        "categories", "gameId", "filter", "subscriberQuestionAnswerId", "selectedAnswerId",
        "cashPoolTransactionTypes", "appId", "inviteCode", "twitchConsoleFollowedSubscriberId",
        "numLivesToPurchase"
    );

    @Autowired
    private ISnowyowlService _service;

    @Autowired
    private ISyncService _syncService;

    @Autowired
    private ITriggerService _triggerService;

    @Autowired
    private CurrentRankCalculator _currentRankCalculator;

    @Autowired
    private CommonBusinessLogic _commonBusinessLogic;

    @Autowired
    private EngineCoordinator _engineCoordinator;

    @Autowired
    protected BotEngine _botEngine;

    private Socket _socketIoSocket;

    public void setSocketIoSocket(Socket socketIoSocket)
    {
        _socketIoSocket = socketIoSocket;
    }

    @Override
    public String getHandlerMessageType()
    {
        return "SNOWL_GAMEAPI";
    }

    @Override
    protected List<String> getValidFormVars()
    {
        return _validFormVars;
    }

    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        CollectorEndpointHandler[] handlers = new CollectorEndpointHandler[] {

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/getQuestionCategoriesFromCategoryIds", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    getQuestionCategoriesFromCategoryIds(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/player/games", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    getPlayerGames(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/player/rounds", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    getPlayerRounds(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/player/currentRank", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    getPlayerCurrentRank(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/player/cashPoolTransactions", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    getPlayerCashPoolTransactions(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/previewPayoutTable", ConnectionType.ANY, HandlerStyle.SYNC_REQUEST))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withSyncRequestHandlerFunction(
                    (request, logMessageTag) ->
                    previewPayoutTable(request)),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/freeplay", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    joinGameAsFreeplay(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/join", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    joinGame(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/joinViaInviteCode", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    joinGameViaInviteCode(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/extralives/purchase", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    purchaseExtraLives(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/leave", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    leaveGame(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/games/getPrivateList", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    getPrivateList(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/beginPoolPlay", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    beginPoolPlay(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/cancelPoolPlay", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    cancelPoolPlay(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/beginBracketPlay", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    beginBracketPlay(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/twitch/followSubscriber", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    twitchFollowSubscriber(message.getProperties(), message.getMessageId())),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/question/getDecryptKey", ConnectionType.ANY, HandlerStyle.SYNC_REQUEST))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withSyncRequestHandlerFunction(
                    (request, logMessageTag) ->
                    getQuestionDecryptKey(request)),

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/question/submitAnswer", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    submitAnswer(message.getProperties(), message.getMessageId())),

        };

        for (CollectorEndpointHandler handler : handlers) {
            _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
        }

        return Arrays.stream(handlers)
                .map(CollectorEndpointHandler::getCollectorEndpoint)
                .toArray(CollectorEndpoint[]::new);
    }

    private Map<String, Object> getQuestionCategoriesFromCategoryIds(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        //grab the incoming properties, split to a list based on a "," delimiter, and pass to the service
        String val = getParamFromProps(props, messageId, "getQuestionCategoriesFromCategoryIds", "categories", true);
        List<QuestionCategory> questionCategories = _service.getQuestionCategoriesFromCategoryIds(Arrays.asList(val.split(",")));

        return new FastMap<>("questionCategories", questionCategories);
    }

    private Map<String, Object> joinGameAsFreeplay(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "joinGameAsFreeplay";

        String gameId = getParamFromProps(props, messageId, docType, "gameId", true);
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "gameId");
        }

        joinGameRefactor(props, messageId, docType, game, true);

        return new FastMap<>("gameId", game.getId());
    }

    private Map<String, Object> joinGame(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "joinGame";

        String gameId = getParamFromProps(props, messageId, docType, "gameId", true);
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "gameId");
        }

        joinGameRefactor(props, messageId, docType, game, false);

        return new FastMap<>("gameId", game.getId());
    }

    private Map<String, Object> joinGameViaInviteCode(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "joinGameViaInviteCode";

        String inviteCode = getParamFromProps(props, messageId, docType, "inviteCode", true);
        Game game = _shoutContestService.getGameViaInviteCode(inviteCode);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "inviteCode");
        }

        joinGameRefactor(props, messageId, docType, game, false);

        return new FastMap<>("gameId", game.getId());
    }

    private void joinGameRefactor(Map<String, String> props, String messageId, String docType, Game game, boolean asFreeplay)
    {
        Subscriber applicant = getSubscriber(props, messageId, docType);
        Round round = _commonBusinessLogic.getFirstRoundOfGame(game.getId());

        _commonBusinessLogic.joinGame(props.get(PARM_TO_WDS), messageId, "joinGame", applicant, game.getId(), _socketIoSocket, _triggerService, round, asFreeplay);
        wrapInTransaction(this::joinGameRefactorDao, new Object[] {game, round});
    }

    private Void joinGameRefactorDao(Object params)
    {
        Game game = (Game) ((Object[])params)[0];
        Round round = (Round) ((Object[])params)[1];

        try {
            publishPayoutTable(game, _shoutContestService.getRoundsForGame(game.getId()), round.getCurrentPlayerCount(), round.getMaximumPlayerCount(),
                    _engineCoordinator, _wdsService, _dao, _logger);
        } catch (PayoutManagerException e) {
            _logger.error(MessageFormat.format("UNABLE TO PUBLISH PAYOUT TABLE: {0}\n\t{1}", e.getErrorTypeCode(), e.getErrorDetails()));
        }

        return null;
    }

    private Map<String, Object> purchaseExtraLives(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "purchaseExtraLives";

        Subscriber subscriber = getSubscriber(props, messageId, docType);
        String gameId = getParamFromProps(props, messageId, docType, "gameId", true);
        int numLivesToPurchase = getIntParamFromProps(props, messageId, docType, "numLivesToPurchase", true);

        if (numLivesToPurchase < 1) return null;

        //does the game exist
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "gameId");
        }

        //is it a multi-life game
        if (!game.getEngineType().equals(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife)) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "notMultiLifeGame");
        }

        //is the player in the game
        GamePlayer existingGamePlayerRecord = _shoutContestService.getGamePlayer(gameId, subscriber.getSubscriberId());
        if (existingGamePlayerRecord == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "notInGame");
        }

        //is the game open? if not, they can't change this value
        if (game.getGameStatus() != Game.GAME_STATUS.OPEN) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "gameNotOpen");
        }

        //are they allowed to purchase this many lives?
        int currentNumLives = existingGamePlayerRecord.getCountdownToElimination();
        if (currentNumLives + numLivesToPurchase > game.getMaxLivesCount()) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "tooManyLives");
        }

        //can't be a freeplayer
        if (existingGamePlayerRecord.isFreeplay()) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "freeplayer");
        }

        //check for payment
        if (game.isProductionGame()) {
            double balance = (Double) wrapInTransaction(this::purchaseExtraLivesDao, new Object[] {subscriber});
            double fundsNeeded = numLivesToPurchase * game.getAdditionalLifeCost();

            if (balance < fundsNeeded) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "insufficientFunds");
            }

            //charge them
            _shoutContestService.addCashPoolTransaction(
                subscriber.getSubscriberId(),
                - fundsNeeded,
                CashPoolTransaction2.TYPE.PURCHASED_EXTRA_LIFE,
                null,
                null,
                game.getId());
        }

        //add the lives
        existingGamePlayerRecord.setCountdownToElimination(existingGamePlayerRecord.getCountdownToElimination() + numLivesToPurchase);
        _shoutContestService.updateGamePlayer(existingGamePlayerRecord);

        return null;
    }

    private Double purchaseExtraLivesDao(Object param)
    {
        Object[] params = (Object[]) param;
        Subscriber subscriber = (Subscriber) params[0];
        double balance = Optional.of(_shoutContestService.getTotalBalance(subscriber.getSubscriberId())).orElse(0D);

        return balance;
    }

    private Map<String, Object> leaveGame(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber applicant = getSubscriber(props, messageId, "leaveGame");

        String gameId = getParamFromProps(props, messageId, "leaveGame", "gameId", true);
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "leaveGame", false, "invalidParam", "gameId");
        }

        GamePlayer existingGamePlayerRecord = _shoutContestService.getGamePlayer(gameId, applicant.getSubscriberId());
        if (existingGamePlayerRecord == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "leaveGame", false, "notInGame");
        }

        //can't leave a game once in progress (they can choose not to play, but their player must stay in the game)
        if (game.getGameStatus() != Game.GAME_STATUS.OPEN) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "leaveGame", false, "gameNotOpen");
        }

        //remove the GamePlayer record
        _shoutContestService.removeGamePlayer(existingGamePlayerRecord);

        //refund the payment
        Round round = _commonBusinessLogic.getFirstRoundOfGame(gameId);
        if (round != null && round.getCostPerPlayer()!= null && round.getCostPerPlayer() > 0D) {

            if (applicant.getRole() != Subscriber.ROLE.TESTER) {
                _shoutContestService.addCashPoolTransaction(
                    applicant.getSubscriberId(), round.getCostPerPlayer(), CashPoolTransaction2.TYPE.ABANDONED_ROUND, null, null,round.getId());
            }
        }

        wrapInTransaction(this::leaveGameTransaction, new Object[] {game, round, applicant});

        return null;
    }

    private Void leaveGameTransaction(Object params)
    {
        Object[] o = (Object[]) params;
        Game game = (Game) o[0];
        Round round = (Round) o[1];
        Subscriber subscriber = (Subscriber) o[2];

        try {
            //decrement the round's player count and set to OPEN
            if (round != null && round.getRoundType() == Round.ROUND_TYPE.BRACKET) {
                round.setCurrentPlayerCount(round.getCurrentPlayerCount() - 1);
                round.setRoundStatus(ROUND_STATUS.VISIBLE);
                _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());

                publishGameWithExtrasToWds(game.getId());
            }

            //send the "abandoned_game" sync message
            enqueueSyncMessage(
                _jsonMapper, _syncService, _logger,
                game.getId(), ISnowyowlService.SYNC_MESSAGE_ABANDONED_GAME, null, subscriber, _socketIoSocket, _triggerService);

            //update game player count document
            _shoutContestService.publishGamePlayerCountToWds(game.getId(), _botEngine.getBotsForGame(game.getId()));

            publishPayoutTable(game, _shoutContestService.getRoundsForGame(game.getId()), round.getCurrentPlayerCount(), round.getMaximumPlayerCount(),
                    _engineCoordinator, _wdsService, _dao, _logger);

        } catch (PayoutManagerException e) {
            _logger.error(MessageFormat.format("UNABLE TO PUBLISH PAYOUT TABLE: {0}\n\t{1}", e.getErrorTypeCode(), e.getErrorDetails()));
        }

        return null;
    }

    private Map<String, Object> getPrivateList(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "getPrivateList";
        Subscriber applicant = getSubscriber(props, messageId, docType);

        List<String> gameIds = _shoutContestService.getPrivateGameIdsForSubscriberByStatusAndEngine(
                applicant.getSubscriberId(), ISnowyowlService.GAME_ENGINE, Game.GAME_STATUS.PENDING, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY);

        return new FastMap<>("gameIds", gameIds);
    }

    private Map<String, Object> getPlayerGames(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "getPlayerGames");

        int appId = getAppId();

        String filter = "OPEN";
        if (props.containsKey("filter")) {
            filter = props.get("filter");
            if (!filter.equals("OPEN") && !filter.equals("MYCLOSED")) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "getPlayerGames", false, "invalidParam", "filter");
            }
        }

        List<GamePlayer> gamesSubscriberHasPlayed = _shoutContestService.getGamePlayers(subscriber.getSubscriberId());
        List<Game> games;
        switch (filter)
        {
            case "OPEN":
                games = _shoutContestService.getGamesByStatusAndAllowableAppId(Game.GAME_STATUS.OPEN, appId);
                games.addAll(_shoutContestService.getGamesByStatusAndAllowableAppId(Game.GAME_STATUS.INPLAY, appId));

                List<String> openGameIds = games
                        .stream()
                        .map(g -> g.getId())
                        .collect(Collectors.toList());

                gamesSubscriberHasPlayed = gamesSubscriberHasPlayed
                        .stream()
                        .filter(gp -> openGameIds.contains(gp.getGameId()))
                        .collect(Collectors.toList());
                break;

            case "MYCLOSED":
                List<String> playerGameIds = gamesSubscriberHasPlayed
                    .stream()
                    .map(gp -> gp.getGameId())
                    .collect(Collectors.toList());

                games = _shoutContestService.getGamesByStatusByGameIds(appId, Game.GAME_STATUS.CLOSED, playerGameIds);

                List<String> closedGameIds = games
                    .stream()
                    .map(g -> g.getId())
                    .collect(Collectors.toList());

                gamesSubscriberHasPlayed = gamesSubscriberHasPlayed
                    .stream()
                    .filter(gp -> closedGameIds.contains(gp.getGameId()))
                    .collect(Collectors.toList());
                break;

            default:
                throw new IllegalStateException("it should not be possible to get here");
        }

        return new FastMap<>("games", games, "gamePlayers", gamesSubscriberHasPlayed);
    }

    private Map<String, Object> getPlayerRounds(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "getPlayerRounds");

        String gameId = props.get("gameId");
        if (gameId == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "getPlayerRounds", false, "missingRequiredParam", "gameId");
        }
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "getPlayerRounds", false, "invalidParam", "gameId");
        }

        List<Round> rounds = _shoutContestService.getRoundsForGame(gameId);
        List<RoundPlayer> roundPlayers = _shoutContestService.getRoundPlayersForGame(gameId, subscriber.getSubscriberId());

        return new FastMap<>("rounds", rounds, "roundPlayers", roundPlayers);
    }

    private Map<String, Object> getPlayerCurrentRank(final Map<String, String> props, final String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "getPlayerCurrentRank");

        String gameId = props.get("gameId");
        if (gameId == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "getPlayerCurrentRank", false, "missingRequiredParam", "gameId");
        }
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "getPlayerCurrentRank", false, "invalidParam", "gameId");
        }

        //make the call to get the current rank. it may take a while so it's done via a callback and publishes the result to the WDS
        _currentRankCalculator.getCurrentRank(gameId, subscriber.getSubscriberId(), new CurrentRankCalculatorCallback() {
            @Override
            public void currentRank(String gameId, long xsubscriberId, Double rank)
            {
                //publish to the WDS
                WebDataStoreObject object = new WebDataStoreObject();
                object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
                object.setToWds(props.get(PARM_TO_WDS));

                Map<String, Object> resultMap = new FastMap<>("rank", rank, "gameId", gameId);

                try {
                    object.setData(_jsonMapper.writeValueAsBytes(resultMap));
                } catch (IOException e) {
                    //this shouldn't happen in a properly configured system with checks in place on the data
                    _logger.error(MessageFormat.format("unable to create rank document for message: {0}, docType: {1}", messageId, "getPlayerCurrentRank"), e);
                    return;
                }
                object.setExpirationDate(new Date(new Date().getTime() + EXPIRES_1_HOUR_MS));
                object.setPath(subscriber.getEmailSha256Hash() + "/rank_" + gameId + ".json");

                try {
                    _wdsService.createOrUpdateObjectSync(object, 0);
                } catch (WebDataStoreException e) {
                    _logger.error(MessageFormat.format("unable to publish response document for message: {0}, docType: {1}", messageId, "getPlayerCurrentRank"), e);
                } catch (InterruptedException e) {
                    _logger.error(MessageFormat.format("unable to publish response document for message: {0}, docType: {1}", messageId, "getPlayerCurrentRank"), e);
                }
            }
        });

        return null;
    }

    private Map<String, Object> getPlayerCashPoolTransactions(final Map<String, String> props, final String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "getPlayerCashPoolTransactions");

        String val = getParamFromProps(props, messageId, "", "cashPoolTransactionTypes", true);

        List<CashPoolTransaction2> transactions = _shoutContestService.getCashPoolTransactionsForSubscriberForTypes(subscriber.getSubscriberId(), Arrays.asList(val.split(",")));

        return new FastMap<>("cashPoolTransactions", transactions);
    }

    private HttpResponse previewPayoutTable(HttpRequest request)
    {
        ObjectMapper jsonMapper = JsonUtil.getObjectMapper();
        HttpResponse response = new HttpResponse();
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();

        String s;

        s = request.getFirstParameter("expectedNumPlayers");
        if (s == null) {
            writer.print("{ \"success\": false, \"missingRequiredParam\": true, \"message\": \"expectedNumPlayers\" }");
            return response;
        }
        int expectedNumPlayers = Integer.parseInt(s);

        s = request.getFirstParameter("minimumPayoutAmount");
        if (s == null) {
            writer.print("{ \"success\": false, \"missingRequiredParam\": true, \"message\": \"minimumPayoutAmount\" }");
            return response;
        }
        float minimumPayoutAmount = Float.parseFloat(s);

        s = request.getFirstParameter("payoutModelId");
        if (s == null) {
            writer.print("{ \"success\": false, \"missingRequiredParam\": true, \"message\": \"payoutModelId\" }");
            return response;
        }
        int payoutModelId = Integer.parseInt(s);


        s = request.getFirstParameter("engineType");
//TODO: remove once new client code comes online
if (s == null) s = ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife;
        if (s == null) {
            writer.print("{ \"success\": false, \"missingRequiredParam\": true, \"message\": \"engineType\" }");
            return response;
        }
        if (!ISnowyowlService.ENGINE_TYPES.contains(s)) {
            writer.print("{ \"success\": false, \"invalidParam\": true, \"message\": \"engineType\" }");
            return response;
        }
        String engineType = s;

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBodyMap = (Map<String, Object>) wrapInTransaction(
                this::previewPayoutTableTransaction,
                new Object[] {expectedNumPlayers, minimumPayoutAmount, payoutModelId, engineType});

        try {
            String body = jsonMapper.writeValueAsString(responseBodyMap);
            _logger.debug("\n\t" + body);
            writer.print(body);
        } catch (JsonProcessingException e) {
            _logger.error("unable to write response json", e);
        }
        writer.flush();

        return response;
    }

    private Map<String, Object> previewPayoutTableTransaction(Object param)
    {
        Object[] o = (Object[]) param;
        int expectedNumPlayers = (Integer) o[0];
        float minimumPayoutAmount = (Float) o[1];
        int payoutModelId = (Integer) o[2];
        String engineType = (String) o[3];

        Game game = new Game();
        game.setEngineType(engineType);

        GamePayout gamePayout = new GamePayout();
        gamePayout.setPayoutModelId(payoutModelId);
        gamePayout.setMinimumPayoutAmount(minimumPayoutAmount);

        Map<String, Object> responseBodyMap = new HashMap<>();
        responseBodyMap.put("success", true);

        PayoutModel payoutModel = (PayoutModel) wrapInTransaction(this::getPayoutModelDao, gamePayout);

        try {
            responseBodyMap.put("payouts", _engineCoordinator.getAdjustedPayoutModelRounds(game, expectedNumPlayers, payoutModel, gamePayout));

        } catch (PayoutManagerException e) {
            if (! e.getErrorTypeCode().equals("playerCountLargerThanMaxPlayerCountForGivenPayoutModel")) {
                _logger.warn(MessageFormat.format("Unable to generate payout table: {0}\n\t{1}", e.getErrorTypeCode(), e.getErrorDetails()));
            }
            responseBodyMap.put("success", false);
            responseBodyMap.put(e.getErrorTypeCode(), true);
        }

        return responseBodyMap;
    }

    private PayoutModel getPayoutModelDao(Object param)
    {
        GamePayout gp = (GamePayout) param;
        return _dao.getPayoutModel(gp.getPayoutModelId());
    }

    private Map<String, Object> beginPoolPlay(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "beginPoolPlay");

        _commonBusinessLogic.setPlayerAvailability(
            props.get(PARM_TO_WDS), messageId, "beginPoolPlay", subscriber,
            props.get("gameId"), true, _socketIoSocket, _triggerService);

        return null;
    }

    private Map<String, Object> cancelPoolPlay(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "cancelPoolPlay");

        _commonBusinessLogic.setPlayerAvailability(
            props.get(PARM_TO_WDS), messageId, "cancelPoolPlay", subscriber,
            props.get("gameId"), false, _socketIoSocket, _triggerService);

        return null;
    }

    private Map<String, Object> beginBracketPlay(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "beginBracketPlay");

        String gameId = props.get("gameId");
        String toWds = props.get(PARM_TO_WDS);

        //make sure the game exists
        if (gameId == null) {
            throw new PublishResponseError(toWds, messageId, "beginBracketPlay", false, "missingRequiredParam", "gameId");
        }
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(toWds, messageId, "beginBracketPlay", false,  "invalidParam", "gameId");
        }
        //the game must be INPLAY (i.e. bracket play has begun)
        if (game.getGameStatus() != Game.GAME_STATUS.INPLAY) {
            throw new PublishResponseError(toWds, messageId, "beginBracketPlay", false,  "gameNotInBracketPlay");
        }

        //are they in the game
        GamePlayer gamePlayer = _shoutContestService.getGamePlayer(gameId, subscriber.getSubscriberId());
        if (gamePlayer == null) {
            throw new PublishResponseError(toWds, messageId, "beginBracketPlay", false, "notInGame");
        }

        return null;
    }

    private Map<String, Object> twitchFollowSubscriber(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "twitchFollowSubscriber";

        String gameId = getParamFromProps(props, messageId, docType, "gameId", true);
        long twitchConsoleFollowedSubscriberId = getLongParamFromProps(props, messageId, docType, "twitchConsoleFollowedSubscriberId", true);

        Game game = _shoutContestService.getGame(gameId);
        if (game == null || game.getGameStatus() != Game.GAME_STATUS.INPLAY) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "gameId");
        }

        Subscriber subscriber = _identityService.getSubscriberById(twitchConsoleFollowedSubscriberId);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "subscriberNotFound");
        }

        GamePlayer gp = _shoutContestService.getGamePlayer(gameId, twitchConsoleFollowedSubscriberId);
        if (gp == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "twitchConsoleFollowedSubscriberId");
        }

        //store it
        _dao.setGameStats(new GameStats(gameId).withTwitchConsoleFollowedSubscriberId(twitchConsoleFollowedSubscriberId));

        return null;
    }

    @SuppressWarnings("unchecked")
    private HttpResponse getQuestionDecryptKey(HttpRequest request)
    {
        ObjectMapper jsonMapper = JsonUtil.getObjectMapper();
        Map<String, Object> responseBodyMap = new HashMap<>();
        responseBodyMap.put("success", true);

        String subscriberQuestionAnswerId = request.getFirstParameter("subscriberQuestionAnswerId");
        Subscriber subscriber = getSubscriber(request, "getQuestionDecryptKey");

        responseBodyMap = (Map<String, Object>) wrapInTransaction(
                this::getQuestionDecryptKeyTransaction, new Object[] {subscriber, subscriberQuestionAnswerId, responseBodyMap});

        HttpResponse response = new HttpResponse();
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        try {
            String body = jsonMapper.writeValueAsString(responseBodyMap);
            _logger.debug("\n\t" + body);
            writer.print(body);
        } catch (JsonProcessingException e) {
            _logger.error("unable to write response json", e);
        }
        writer.flush();

        return response;
    }

    private Map<String, Object> getQuestionDecryptKeyTransaction(Object param)
    {
        Object[] o = (Object[]) param;
        Subscriber subscriber = (Subscriber) o[0];
        String subscriberQuestionAnswerId = (String) o[1];
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBodyMap = (Map<String, Object>) o[2];

        SubscriberQuestionAnswer sqa = _commonBusinessLogic.getQuestionDecryptKey("", "", "getQuestionDecryptKey", subscriber, subscriberQuestionAnswerId);

        //see if there is a monitored twitch subscriber
        Long twitchSubscriberId = null;
        GameStats gameStats = _dao.getGameStats(sqa.getGameId());
        if (gameStats != null) {
            twitchSubscriberId = gameStats.getTwitchConsoleFollowedSubscriberId();
        }
        if (twitchSubscriberId != null) {
            List<MatchPlayer> matchPlayers = _shoutContestService.getMatchPlayersForMatch(sqa.getMatchId());
            boolean isMatchTwitchMonitored = false;
            for (MatchPlayer mp : matchPlayers) {
                if (mp.getSubscriberId() == twitchSubscriberId) {
                    isMatchTwitchMonitored = true;
                    break;
                }
            }

            if (isMatchTwitchMonitored) {
                long opId = 0;
                for (MatchPlayer mp : matchPlayers) {
                    if (mp.getSubscriberId() != twitchSubscriberId) {
                        opId = mp.getSubscriberId();
                        break;
                    }
                }

                Map<String, Object> twitchMap = new FastMap<>(
                    "type", "TWITCH_QUESTION_TIMER_STARTED",
                    "gameid", sqa.getGameId(),
                    "roundId", sqa.getRoundId(),
                    "matchId", sqa.getMatchId(),
                    sqa.getSubscriberId() == twitchSubscriberId ? "subscriberId" : "opponentId", sqa.getSubscriberId() == twitchSubscriberId ? twitchSubscriberId : opId,
                    "questionPresentedTimestamp", sqa.getQuestionPresentedTimestamp()
                );

                if (_socketIoSocket != null) {
                    try {
                        _socketIoSocket.emit("send_twitch_message", _jsonMapper.writeValueAsString(twitchMap));
                    } catch (JsonProcessingException e) {
                        _logger.error("unable to emit send_twitch_message", e);
                    }
                }
            }
        }

        try {
            responseBodyMap.put("decryptKey", sqa.getQuestionDecryptKey());
            responseBodyMap.put("questionPresentedTimestamp", sqa.getQuestionPresentedTimestamp());

            if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                    "requested decryptKey. sId: {0,number,#}, sqaId: {1}, timestamp: {2,date,yyyy-MM-dd hh:mm:ss.SSS}",
                    sqa.getSubscriberId(), sqa.getId(), sqa.getQuestionPresentedTimestamp()));
            }

        } catch (PublishResponseError e) {
            _logger.error("unable to grab decryptKey", e);

            responseBodyMap.put("success", false);
            responseBodyMap.put(e.getFailureType(), true);
            if (e.getFailureMessage() != null) {
                responseBodyMap.put("message", e.getFailureMessage());
            }
        }

        return responseBodyMap;
    }

    private Map<String, Object> submitAnswer(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "submitAnswer");

        wrapInTransaction(this::submitAnswerTransaction, new Object[] {props, messageId, subscriber});

        return null;
    }

    private Void submitAnswerTransaction(Object param)
    {
        Object[] o = (Object[]) param;
        @SuppressWarnings("unchecked")
        Map<String, String> props = (Map<String, String>) o[0];
        String messageId = (String) o[1];
        Subscriber subscriber = (Subscriber) o[2];

        String sqaId = props.get("subscriberQuestionAnswerId");
        String selectedAnswerId = props.get("selectedAnswerId");

        long durationMilliseconds = _commonBusinessLogic.submitAnswer(
            props.get(PARM_TO_WDS), messageId, "submitAnswer", subscriber,
            sqaId, selectedAnswerId);

        //see if there is a monitored twitch subscriber
        SubscriberQuestionAnswer sqa = _dao.getSubscriberQuestionAnswer(sqaId);

        Long twitchSubscriberId = null;
        GameStats gameStats = _dao.getGameStats(sqa.getGameId());
        if (gameStats != null) {
            twitchSubscriberId = gameStats.getTwitchConsoleFollowedSubscriberId();
        }
        if (twitchSubscriberId != null) {
            List<MatchPlayer> matchPlayers = _shoutContestService.getMatchPlayersForMatch(sqa.getMatchId());
            boolean isMatchTwitchMonitored = false;
            for (MatchPlayer mp : matchPlayers) {
                if (mp.getSubscriberId() == twitchSubscriberId) {
                    isMatchTwitchMonitored = true;
                    break;
                }
            }

            if (isMatchTwitchMonitored) {
                long opId = 0;
                for (MatchPlayer mp : matchPlayers) {
                    if (mp.getSubscriberId() != twitchSubscriberId) {
                        opId = mp.getSubscriberId();
                        break;
                    }
                }

                Map<String, Object> twitchMap = new FastMap<>(
                    "type", "TWITCH_QUESTION_ANSWERED",
                    "gameid", sqa.getGameId(),
                    "roundId", sqa.getRoundId(),
                    "matchId", sqa.getMatchId(),
                    sqa.getSubscriberId() == twitchSubscriberId ? "subscriberId" : "opponentId", sqa.getSubscriberId() == twitchSubscriberId ? twitchSubscriberId : opId,
                    "selectedAnswerId", selectedAnswerId,
                    "durationMilliseconds", durationMilliseconds
                );

                if (_socketIoSocket != null) {
                    try {
                        _socketIoSocket.emit("send_twitch_message", _jsonMapper.writeValueAsString(twitchMap));
                    } catch (JsonProcessingException e) {
                        _logger.error("unable to emit send_twitch_message", e);
                    }
                }
            }
        }

        return null;
    }

}
