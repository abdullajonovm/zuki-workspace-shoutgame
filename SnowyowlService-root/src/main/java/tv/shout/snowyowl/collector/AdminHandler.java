package tv.shout.snowyowl.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.meinc.gameplay.domain.Tuple;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint.Root;
import com.meinc.webdatastore.domain.WebDataStoreObjectOperation;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;

import io.socket.client.Socket;
import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.ManualRedeemRequest;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.common.FileHandler;
import tv.shout.snowyowl.common.MessageBus;
import tv.shout.snowyowl.common.PayoutTablePublisher;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.common.WdsPublisher;
import tv.shout.snowyowl.domain.AwaitingPayout;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.GameWinner;
import tv.shout.snowyowl.domain.Message;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.domain.QuestionCategory;
import tv.shout.snowyowl.domain.Sponsor;
import tv.shout.snowyowl.domain.SponsorCashPool;
import tv.shout.snowyowl.domain.SubscriberFromSearch;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.snowyowl.engine.NotEnoughSponsorPoolCashException;
import tv.shout.snowyowl.engine.NotEnoughSponsorsException;
import tv.shout.snowyowl.engine.PayoutManagerException;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.snowyowl.service.QuestionCategoryHelper;
import tv.shout.snowyowl.service.QuestionHelper;
import tv.shout.util.FastMap;
import tv.shout.util.StringUtil;

public class AdminHandler
extends BaseSmMessageHandler
implements SyncMessageSender, WdsPublisher, FileHandler, PayoutTablePublisher
{
    private static Logger _logger = Logger.getLogger(AdminHandler.class);

    // Valid form post param names
    private static final List<String> _validFormVars = Arrays.asList(
        "gameId", "beginsInMs", "statuses", "game",
        "expectedStartDateForPoolPlay", "expectedStartDateForBracketPlay", "gameNames", "question", "state", "questionId", "category", "appId",
        "subscriberId", "categoryIds", "categoryId", "primaryIsHash", "message", "imageExternallyManaged",
        "fromDate", "toDate", "partialEmail", "expectedNumPlayers",
        "canSeeContentWithoutLogin", "minimumPayoutAmount", "fillWithBots", "pairImmediately", "payoutModelId", "manualRedeemRequestId",
        "numberOfPlayers", "sponsorEmail", "giveSponsorPlayerWinningsBackToSponsor", "role"
    );

    private static final String poolPlayBeingsTitleLocalizationUuid = "7651664a-7327-4082-aec6-ebba179a4630";
    private static final String poolPlayBeginsMessageLocalizationUuid = "835650ff-7e1e-11e7-970d-0242ac110004";
    private static final String poolPlayBeginsEmailSubjectUuid = "e1f9d489-7928-42ec-93c8-63fa53664885";

    private static final String freeplayJoinSmsUuid = "d335b70e-76d8-4c87-b73f-2bc56bb1a214";
    private static final String freeplayJoinTitleUuid = "d8d50785-279d-48e1-9b90-0d32585c86d3";
    private static final String freeplayJoinPushMessageUuid = "c8cdbdb1-2e5a-4169-87ae-ba3669ade8c7";
    private static final String freeplayJoinEmailMessageUuid = "987a2362-4d09-49f5-a771-fc64ef4fa420";

//    @Value("${sm.fillwithbots:0}")
//    private double _percentageToFillBracketWithBots;

    @Value("${sm.maxplayercount:1024}")
    private int _maxPlayerCount;

    @Value("${shorten.url.domain}")
    private String _shortUrlDomain;

    @Value("${shorten.url.short.url.prefix}")
    private String _shortUrlPrefix;

    private static final int MINIMUM_PLAYER_COUNT = 1;

    @Value("${sm.engine.statedir}")
    private String _stateDir;

    @Resource(name="webMediaStoreService")
    private IWebDataStoreService _wmsService;

    @Autowired
    private ISnowyowlService _snowyowlService;

    @Autowired
    private IIdentityService _identityService;

    @Autowired
    private EngineCoordinator _engineCoordinator;

    @Autowired
    private BotEngine _botEngine;

    @Autowired
    private SponsorEngine _sponsorEngine;

    @Autowired
    private CommonBusinessLogic _commonBusinessLogic;

    private Socket _socketIoSocket;

    public void setSocketIoSocket(Socket socketIoSocket)
    {
        _socketIoSocket = socketIoSocket;
    }

    @Override
    protected List<String> getValidFormVars()
    {
        return _validFormVars;
    }

    @PostConstruct
    public void onPostConstruct()
    {
        ServerPropertyHolder.addPropertyChangeListener(
            "sm\\..*",
            (properties) -> {
                properties.forEach(change -> {
                    if ("sm.maxplayercount".equals(change.key)) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format("sm.maxplayercount changed from {0} to {1}", change.oldValue, change.newValue));
                        }
                        _maxPlayerCount = Integer.parseInt(change.newValue);
                    }
                });
            }
        );
    }

    @Override
    public String getHandlerMessageType()
    {
        return "SNOWYOWL_ADMIN";
    }

    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        CollectorEndpointHandler[] handlers = new CollectorEndpointHandler[] {
                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/games/list", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        listGames(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/get", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getGame(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/create", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        createGame(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/open", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        openGame(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/getrounds", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getRoundsForGame(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/notifyFreeplayers", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        notifyFreeplayers(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/beginPoolPlay", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        beginPoolPlay(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/beginBracketPlay", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        beginBracketPlay(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/cancel", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        cancelGame(message.getProperties(), message.getMessageId())),

//not used by admin client. not going to bother maintaining it
//                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/clone", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        cloneGame(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/categories/getIdsToKeys", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER,GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getCategoryIdsToKeys(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/category/create", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        createCategory(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/category/update", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        updateCategory(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/category/delete", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        deleteCategory(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/question/create", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        createQuestion(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/question/getByState", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getQuestionsByState(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/question/changeState", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        changeQuestionState(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/question/update", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        updateQuestion(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/question/delete", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        deleteQuestion(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/question/changeCategories", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        changeQuestionCategories(message.getProperties(), message.getMessageId())),

//                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/push/sendToSubscriber", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        sendPushToDevice(message.getProperties(), message.getMessageId())),

//                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/socketio/sendMessage", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        sendSocketIoMessage(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/getWinners", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        gameGetWinners(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/payout/markAsPaid", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        payoutMarkAsPaid(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/payouts/getOustandingManualRequests", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(GAME_ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getOustandingManualRequests(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/subscriber/search", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        subscriberSearch(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/setCanSeeContentWithoutLogin", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        setCanSeeContentWithoutLogin(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/viewPlayerInfo", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        gameViewPlayerInfo(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/game/addSponsorPlayers", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        gameAddSponsorPlayers(message.getProperties(), message.getMessageId())),

//                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/pushTest", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        pushTest(message.getProperties(), message.getMessageId())),

        };

        for (CollectorEndpointHandler handler : handlers) {
            _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
        }

        return Arrays.stream(handlers)
                .map(CollectorEndpointHandler::getCollectorEndpoint)
                .toArray(CollectorEndpoint[]::new);
    }

    private Map<String, Object> listGames(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String statusesParam = getParamFromProps(props, messageId, "listGames", "statuses", true);
        List<Game.GAME_STATUS> statuses = new ArrayList<>();
        for (String status : statusesParam.split(",")) {
            statuses.add(Game.GAME_STATUS.valueOf(status));
        }

        List<Game> games = _shoutContestService.getGamesByStatusAndEngine(ISnowyowlService.GAME_ENGINE, statuses.toArray(new Game.GAME_STATUS[statuses.size()]));
        return new FastMap<>("games", games);
    }

    @SuppressWarnings("serial")
    public static class GameWithRounds
    extends Game
    {
        public List<Round> rounds;

        public Game asGame()
        {
            try {
                return (Game) this.clone();
            } catch (CloneNotSupportedException e) {
                //shouldn't happen
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, Object> getGame(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String gameId = getParamFromProps(props, messageId, "getGame", "gameId", true);

        Game game = _shoutContestService.getGame(gameId);

        return new FastMap<>(
            "game", game
        );
    }

    private Map<String, Object> createGame(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "createGame";

        TypeReference<GameWithRounds> typeRef = new TypeReference<GameWithRounds>(){};
        GameWithRounds gameWithRounds = getJsonObjectFromProps(props, messageId, docType, "game", true, typeRef);
        String gameId = UUID.randomUUID().toString();
        gameWithRounds.setId(gameId);

////TODO: debug; remove
//String gameName = gameWithRounds.getGameName("en");
//if (gameName.startsWith("stest:")) {
//    String[] flags = gameName.split(":");
//    boolean autostartPoolPlay = flags[1].equals("1");
//    boolean autostartBracketPlay = flags[2].equals("1");
//    long bracketAutostartDelayMs = flags.length == 4 ? Long.parseLong(flags[3]) : 0L;
//
    _logger.info(MessageFormat.format(
            ">>> p: {0}, b: {1}, d: {2}",
            gameWithRounds.isAutoStartPoolPlay(), gameWithRounds.isAutoStartBracketPlay(), gameWithRounds.getAutoBracketPlayPreStartNotificationTimeMs()));
//
//    gameWithRounds.setAutoStartPoolPlay(autostartPoolPlay);
//    gameWithRounds.setAutoStartBracketPlay(autostartBracketPlay);
//    gameWithRounds.setAutoBracketPlayPreStartNotificationTimeMs(bracketAutostartDelayMs);
//}

        if (gameWithRounds.getEngineType() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredParam", "engineType");
        }

//TODO: a temporary fix until the client catches up with the server change which eliminates VARIABLE_ROUND as an engineType
if (gameWithRounds.getEngineType().equals("VARIABLE_ROUND")) {
    gameWithRounds.setEngineType(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife);
}

        //make sure the engineType is valid
        if (!ISnowyowlService.ENGINE_TYPES.contains(gameWithRounds.getEngineType())) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "engineType");
        }

        if (gameWithRounds.rounds == null) {
            _logger.warn("received game with no rounds");
            gameWithRounds.rounds = new ArrayList<>();
        }

//_logger.info(MessageFormat.format("just created game. gameType: {0}", gameWithRounds.getGameType()));

        Date expectedStartDateForPoolPlay = getDateParamFromProps(props, messageId, docType, "expectedStartDateForPoolPlay", true);
        Date expectedStartDateForBracketPlay = getDateParamFromProps(props, messageId, docType, "expectedStartDateForBracketPlay", true);

        //make sure bracket play starts AFTER pool play (or at least at the same time)
        if (expectedStartDateForPoolPlay.getTime() != expectedStartDateForBracketPlay.getTime()) {
            if (expectedStartDateForPoolPlay.after(expectedStartDateForBracketPlay)) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "bracketPlayMustStartAtOrAfterPoolPlay");
            }
        }

        //see if the game image (if any) is new, or externally managed
        Boolean imageExternallyManaged = getBooleanParamFromProps(props, messageId, docType, "imageExternallyManaged", false);
        if (imageExternallyManaged == null) imageExternallyManaged = false;

        //make sure all required fields are present and have valid values
        if (StringUtil.isEmpty(gameWithRounds.getId())) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredParam", "id");
        }
        if (gameWithRounds.getAllowableAppIds() == null || gameWithRounds.getAllowableAppIds().size() == 0) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredParam", "allowableAppIds");
        }
        gameWithRounds.getAllowableAppIds().forEach(appId -> {
            if (_shoutContestService.getAppById(appId) == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "appId: " + appId);
            }
        });
        if (gameWithRounds.getAllowableLanguageCodes() == null || gameWithRounds.getAllowableLanguageCodes().size() == 0) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredParam", "allowableLanguageCodes");
        }

        float minimumPayoutAmount = getFloatParamFromProps(props, messageId, docType, "minimumPayoutAmount", true);
        int payoutModelId = getIntParamFromProps(props, messageId, docType, "payoutModelId", true);

        boolean giveSponsorPlayerWinningsBackToSponsor = false;
        if (props.containsKey("giveSponsorPlayerWinningsBackToSponsor")) {
            giveSponsorPlayerWinningsBackToSponsor = getBooleanParamFromProps(props, messageId, docType, "giveSponsorPlayerWinningsBackToSponsor", false);
        }

        PayoutModel pm = (PayoutModel) wrapInTransaction(this::getPayoutModelTransaction, payoutModelId);
        if (!pm.isActive()) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "payoutModelNotActive");
        }

        //the current payout engine is only designed to work with single elimination
        if (gameWithRounds.getBracketEliminationCount() == null || gameWithRounds.getBracketEliminationCount() != 1) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "bracketEliminationCount MUST be 1");
        }

        //sanity checks

        if (gameWithRounds.getEngineType().equals(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife)) {
            if (gameWithRounds.getStartingLivesCount() < 1) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "startingLivesCount MUST be > 0");
            }

            if (gameWithRounds.getAdditionalLifeCost() < 0) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "additionalLifeCost MUST be >= 0");
            }

            if (gameWithRounds.getMaxLivesCount() < gameWithRounds.getStartingLivesCount()) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "maxLivesCount MUST be >= startingLivesCount");
            }
        }

        gameWithRounds.rounds.forEach(round -> {
            round.setGameId(gameId);
            round.setId(UUID.randomUUID().toString());
            //this will now be set from the payoutModel - 0 it out here and set it further down
            //if (round.getCostPerPlayer() == null) {
            round.setCostPerPlayer(0D);
            //}

            if (gameWithRounds.getEngineType().equals(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife)) {
                int minimumActivityToWinCount = round.getMinimumActivityToWinCount();
                if (minimumActivityToWinCount < 1) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false,
                            "minimumActivityToWinCountTooSmall", "minimumActivityToWinCount must be AT LEAST 1");
                }

                if (round.getMaximumActivityCount() == null) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false,
                            "maximumActivityCountNullNotAllowed", "maximumActivityCount MUST NOT be null");
                } else {
                    if (round.getMaximumActivityCount() < minimumActivityToWinCount) {
                        throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false,
                            "maximumActivityCountTooSmall", "maximumActivityCount must be AT LEAST equal to minimumActivityToWinCount");
                    }
                }
            }

            if (round.getActivityMaximumDurationSeconds() < 10) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false,
                        "activityMaximumDurationSecondsTooSmall", "activityMaximumDurationSeconds MUST NOT be less than 10 seconds");
            }

            if (round.getPlayerMaximumDurationSeconds() < 5) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false,
                        "playerMaximumDurationSecondsTooSmall", "playerMaximumDurationSeconds MUST NOT be less than 5 seconds (10 or 15 is preferable)");
            }

            if (round.getDurationBetweenActivitiesSeconds() < 3) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false,
                        "durationBetweenActivitiesSecondsTooSmall", "durationBetweenActivitiesSeconds MUST NOT be less than 3 seconds (5 or 10 is preferable)");
            }

            if (round.getMaximumPlayerCount() > _maxPlayerCount && gameWithRounds.isFillWithBots()) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "maximumPlayerCountTooLarge", _maxPlayerCount+"");
            }

            //make sure the max number of players isn't higher than the payout model allows (PayoutModel can scale down, but not up)
            if (round.getMaximumPlayerCount() > pm.getBasePlayerCount()) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "maximumPlayerCountTooLarge", pm.getBasePlayerCount()+"");
            }

            if (round.getMaximumPlayerCount() < MINIMUM_PLAYER_COUNT) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "maximumPlayerCountTooSmall", MINIMUM_PLAYER_COUNT+"");
            }

            //if this is a bracket round AND if the game contains a non-null maxBotFillCount, make sure the value makes sense
            if (round.getRoundType() == Round.ROUND_TYPE.BRACKET && gameWithRounds.getMaxBotFillCount() != null) {
                if (gameWithRounds.getMaxBotFillCount() < 0) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "maxBotFillCountIsNegative");
                }

                if (gameWithRounds.getMaxBotFillCount() > round.getMaximumPlayerCount()) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "maxBotFillCountLargerThanBracketRoundMaximumPlayerCount");
                }

                if (gameWithRounds.getMaxBotFillCount() % 2 != 0) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "maxBotFillCountMustBeEven");
                }
            }
        });

        //make sure each of the text fields has a value for each language
        gameWithRounds.getAllowableLanguageCodes().forEach(languageCode -> {
            if (gameWithRounds.getGameNames().get(languageCode) == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "gameNames missing entry for languageCode: " + languageCode);
            }
            if (gameWithRounds.getGameDescriptions().get(languageCode) == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "gameDescriptions missing entry for languageCode: " + languageCode);
            }
            if (gameWithRounds.getFetchingActivityTitles().get(languageCode) == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "fetchingActivityTitles missing entry for languageCode: " + languageCode);
            }
            if (gameWithRounds.getSubmittingActivityTitles().get(languageCode) == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "submittingActivityTitles missing entry for languageCode: " + languageCode);
            }
        });

        //if there is a photo url, move it from the sandbox
        String photoUrl = gameWithRounds.getGamePhotoUrl();
        if (photoUrl != null && !imageExternallyManaged) {
            try {
                URL url = new URL(photoUrl);

                //move the image from the sandbox to the public web directory
                WebDataStoreObject.Endpoint e = new WebDataStoreObject.Endpoint(url.getHost(), Root.USER_UPLOAD, url.getPath());
                _wmsService.operateObjectSync(
                    e,
                    new WebDataStoreObjectOperation.ResizeOperation(2048, 2048),
                    new WebDataStoreObjectOperation.StripOperation(),
                    new WebDataStoreObjectOperation.SetRootOperation(Root.WWW)
                );

            } catch (MalformedURLException e) {
                _logger.warn("unable to parse image url: " + photoUrl, e);
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "malformedUrl", photoUrl);
            } catch (WebDataStoreException e) {
                _logger.warn("unexpected exception while parsing images", e);
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", e.getMessage());
            } catch (InterruptedException e) {
                _logger.warn("unexpected exception while parsing images", e);
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", e.getMessage());
            }
        }

        //if auto start pool play, make sure there is at least one pool play round
        if (gameWithRounds.isAutoStartPoolPlay()) {
            long numPoolPlayRounds = gameWithRounds.rounds.stream().filter(r -> r.getRoundType() == Round.ROUND_TYPE.POOL).count();
            if (numPoolPlayRounds == 0) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "autoStartPoolPlayWithNoPoolPlayRounds");
            }
        }

        //if auto start bracket play, make sure the notification time is >= 0 (if it's not, don't throw an error, just set it to 0)
        if (gameWithRounds.isAutoStartBracketPlay()) {
            if (gameWithRounds.getAutoBracketPlayPreStartNotificationTimeMs() < 0) {
                gameWithRounds.setAutoBracketPlayPreStartNotificationTimeMs(0L);
            }
        }

        //create the GamePayout (won't have the gameId yet since it's not created, but needs to exist to calculate the player pot)
        GamePayout gamePayout = new GamePayout();
        gamePayout.setMinimumPayoutAmount(minimumPayoutAmount);
        gamePayout.setPayoutModelId(payoutModelId);
        gamePayout.setGiveSponsorPlayerWinningsBackToSponsor(giveSponsorPlayerWinningsBackToSponsor);

        //the cost to join is now coming from the PayoutModel
        gameWithRounds.rounds.get(0).setCostPerPlayer((double) pm.getEntranceFeeAmount());

        //the purse is also now coming from the PayoutModel
        float playerPot;
        try {
            playerPot = _engineCoordinator.getPlayerPot(gameWithRounds, gameWithRounds.rounds, gameWithRounds.rounds.get(0).getMaximumPlayerCount(), gamePayout);
        } catch (PayoutManagerException e) {
            _logger.error(MessageFormat.format("BAD VALUES IN GAME FOR GIVEN PAYOUT MODEL: {0}\n\t{1}", e.getErrorTypeCode(), e.getErrorDetails()));
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, e.getErrorTypeCode());
        }
        gameWithRounds.rounds.get(gameWithRounds.rounds.size()-1).setRoundPurse((double) playerPot);

        //set the 'canAppearInMobile' flag based on whether there are any virtual payouts
        gameWithRounds.setCanAppearInMobile(
            pm.getPayoutModelRounds().stream()
                .filter(pmr -> pmr.getCategory().equals(PayoutModelRound.CATEGORY.VIRTUAL))
                .count() == 0
        );

        //if this is a private event, generate an inviteCode
        if (gameWithRounds.isPrivateGame()) {
            gameWithRounds.setInviteCode(_shoutContestService.generateRandomString(Game.INVITE_CODE_LENGTH));
        }

        //add the game and each of the rounds to the db
        Game justTheGame = gameWithRounds.asGame();
        _shoutContestService.addGame(justTheGame);
        for (Round round : gameWithRounds.rounds) {
            round.setExpectedOpenDate(round.getRoundType() == Round.ROUND_TYPE.POOL ? expectedStartDateForPoolPlay : expectedStartDateForBracketPlay);
            _shoutContestService.addRound(round);
        }

        //now that the gameId is known, save the gamepayout for this game
        gamePayout.setGameId(gameWithRounds.getId());

        wrapInTransaction(this::addGamePayoutTransaction, new Object[] {gamePayout, pm, gameWithRounds});

        //update game player count document (just so it exists, even though count starts at 0)
        _shoutContestService.publishGamePlayerCountToWds(gameWithRounds.getId(), new ArrayList<>());

        //if the game is supposed to atuo start, let the service know so it can begin checking when it's time
        if (gameWithRounds.isAutoStartPoolPlay() || gameWithRounds.isAutoStartBracketPlay()) {
            _snowyowlService.checkForAutoStartGames();
        }

        return null;
    }

    private PayoutModel getPayoutModelTransaction(Object param)
    {
        int payoutModelId = (Integer) param;
        return _dao.getPayoutModel(payoutModelId);
    }

    private Void addGamePayoutTransaction(Object param)
    {
        Object[] o = (Object[]) param;
        GamePayout gamePayout = (GamePayout) o[0];
        PayoutModel pm = (PayoutModel) o[1];
        GameWithRounds gameWithRounds = (GameWithRounds) o[2];

        _dao.addGamePayout(gamePayout);

        //publish (even though it's just PENDING, the admin tools need access to it)
        publishGameWithExtrasToWds(gameWithRounds.getId(), pm, gamePayout, null);

        return null;
    }

    private Map<String, Object> openGame(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String gameId = getParamFromProps(props, messageId, "openGame", "gameId", true);

        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "openGame", false, "invalidParam", "gameId");
        }

        if (game.getOpenDate() != null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "openGame", false, "gameAlreadyOpen");
        }

        _commonBusinessLogic.openGame(game, _wdsService);

        return null;
    }

    private Map<String, Object> getRoundsForGame(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String gameId = getParamFromProps(props, messageId, "getRoundsForGame", "gameId", true);
        List<Round> rounds = _shoutContestService.getRoundsForGame(gameId);
        return new FastMap<>("rounds", rounds);
    }

    private Map<String, Object> notifyFreeplayers(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "notifyFreeplayers";

        //get the game
        String gameId = getParamFromProps(props, messageId, docType, "gameId", true);
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "gameId");
        }

        //get all the free players
        List<GamePlayer> gamePlayers = _shoutContestService.getGamePlayersForGame(gameId);
        List<Long> freePlayerIds = gamePlayers.stream()
                .filter(gp -> gp.isFreeplay())
                .map(gp -> gp.getSubscriberId())
                .collect(Collectors.toList());

        wrapInTransaction(
                this::notifyFreePlayersTransaction,
                new Object[] {new GameStats(gameId).withFreeplayNotificationSent(true), game, freePlayerIds});

        return null;
    }

    private Void notifyFreePlayersTransaction(Object params)
    {
        Object[] o = (Object[]) params;
        GameStats gameStats = (GameStats) o[0];
        Game game = (Game) o[1];
        @SuppressWarnings("unchecked")
        List<Long> freePlayerIds = (List<Long>) o[2];

        if (freePlayerIds.size() > 0) {
            _commonBusinessLogic.sendGameNotifications(
                freePlayerIds, ISnowyowlService.NOTIFICATION_PREF_TYPE_ROUND_START, "PUSH", game,

                freeplayJoinSmsUuid, null,

                "FREEPLAY_JOIN",
                freeplayJoinTitleUuid, null,
                freeplayJoinPushMessageUuid, null,
                null,

                freeplayJoinTitleUuid, null,
                freeplayJoinEmailMessageUuid, new Object[] {_shortUrlDomain, game.getId()}
            );
        }

        _dao.setGameStats(gameStats);

        publishGameWithExtrasToWds(gameStats.getGameId(), null, null, gameStats);

        return null;
    }

    private Map<String, Object> beginPoolPlay(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String gameId = getParamFromProps(props, messageId, "beginPoolPlay", "gameId", true);

        //make sure the game exists and is in the OPEN state
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "beginPoolPlay", false, "invalidParam", "gameId");
        }
        if (game.getGameStatus() != Game.GAME_STATUS.OPEN) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "beginPoolPlay", false, "gameNotOpen");
        }

        List<Round> rounds = _shoutContestService.getRoundsForGame(gameId);

        //if there are no POOL play rounds for this game, throw an error
        Optional<Round> oFirstPoolRound = rounds.stream().filter(round -> round.getRoundType() == Round.ROUND_TYPE.POOL).findFirst();
        if (!oFirstPoolRound.isPresent()) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "beginPoolPlay", false, "noPoolRounds");
        }

        try {
            _commonBusinessLogic.startPoolPlay(game, rounds);
        } catch (NoQuestionsPossibleException e1) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "beginPoolPlay", false, "roundParamsResultsInNoQuestionsPossible", "roundId: " + e1.getRoundId());
        }

        //notify everyone who is currently in the game that they can begin playing
        new Thread()
        {
            @Override
            public void run()
            {
                List<GamePlayer> gamePlayers = _shoutContestService.getGamePlayersForGame(gameId);

                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                try {
                    List<Long> subscriberIds = gamePlayers.stream().map(gp -> gp.getSubscriberId()).collect(Collectors.toList());
                    _commonBusinessLogic.sendGameNotifications(
                        subscriberIds, ISnowyowlService.NOTIFICATION_PREF_TYPE_ROUND_START, null, game,

                        poolPlayBeginsMessageLocalizationUuid, new Object[] {"~GAMENAME~"},

                        ISnowyowlService.NOTIFICATION_TYPE_NOTIFY_ON_ROUND_START,
                        poolPlayBeingsTitleLocalizationUuid, null,
                        poolPlayBeginsMessageLocalizationUuid, new Object[] {"~GAMENAME~"},
                        null,

                        poolPlayBeginsEmailSubjectUuid, null,
                        poolPlayBeginsMessageLocalizationUuid, new Object[] {"~GAMENAME~"}
                    );

                    _transactionManager.commit(txStatus);
                    txStatus = null;
                } catch (Exception e) {
                    _logger.error(e.getMessage(), e);
                } finally {
                    if (txStatus != null) {
                        _transactionManager.rollback(txStatus);
                        txStatus = null;
                        return;
                    }
                }
            }
        }.start();

        return null;
    }

    private Map<String, Object> beginBracketPlay(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String gameId = getParamFromProps(props, messageId, "beginBracketPlay", "gameId", true);
        long beginsInMs = getLongParamFromProps(props, messageId, "beginBracketPlay", "beginsInMs", true);
        long then = System.currentTimeMillis();

        //make sure the game exists and is in the OPEN state
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "beginBracketPlay", false, "invalidParam", "gameId");
        }
        if (game.getGameStatus() != Game.GAME_STATUS.OPEN) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "beginBracketPlay", false, "gameNotOpen");
        }

        //find the first bracket round
        final List<Round> rounds = _shoutContestService.getRoundsForGame(gameId);
        Round nextRoundInSequence = rounds.stream()
            .filter(r -> r.getRoundType() == Round.ROUND_TYPE.BRACKET)
            .sorted( Comparator.comparing(Round::getRoundSequence, Comparator.nullsLast(Comparator.naturalOrder())) )
            .findFirst()
            .orElseThrow(() -> new PublishResponseError(props.get(PARM_TO_WDS), messageId, "beginBracketPlay", false, "noBracketRound"));

        try {
            _commonBusinessLogic.startBracketPlay(game, rounds, _wdsService, beginsInMs);
        } catch (PoolPlayMatchesInProgressException e1) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "beginBracketPlay", false, "poolPlayMatchesInProgress");
        }

        //do the rest of the work on a separate thread so the admin call can return in a timely manner
        new Thread() {
            @Override
            public void run()
            {
                try {
                    _commonBusinessLogic.completeBracketPlayStart(game, rounds, nextRoundInSequence, beginsInMs, then, _wdsService, _socketIoSocket);
                } catch (Exception e) {
                    _logger.error(e.getMessage(), e);
                }
            }
        }.start();

        return null;
    }

    private Map<String, Object> cancelGame(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String gameId = getParamFromProps(props, messageId, "cancelGame", "gameId", true);

        //make sure the game exists
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "cancelGame", false, "invalidParam", "gameId");
        }

        //can't cancel a game that is either closed or cancelled already
        if (game.getGameStatus() == Game.GAME_STATUS.CANCELLED || game.getGameStatus() == Game.GAME_STATUS.CLOSED) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "cancelGame", false, "gameAlreadyClosed");
        }

        _shoutContestService.cancelGame(gameId);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
//_logger.info(MessageFormat.format("BotEngine (adminHandler, cancelGame): releasing bots for game: {0}", gameId));
            //clear out the bots/subscriber question list
            _dao.releaseBotsForGame(gameId);
            _dao.clearSubscriberQuestions(gameId);

            List<String> gameIds = new ArrayList<>();
            gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.DEFAULT, ISnowyowlService.GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));
            gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.TESTER, ISnowyowlService.TEST_GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));

            //publish each of the individual game docs
            for (String gameIdToPublish : gameIds) {
                publishGameWithExtrasToWds(gameIdToPublish);
            }

            //the above calls only republished open/inplay games, but this game is no longer open or inplay, it's cancelled, so it also needs a republish
            publishGameWithExtrasToWds(gameId);

            //all JOINED_ROUND transactions (and refunds) will be using this round:
            List<Round> rounds = _shoutContestService.getRoundsForGame(gameId);
            Collections.sort(rounds, (lhs, rhs) -> lhs.getRoundSequence() < rhs.getRoundSequence() ? -1 : 1);
            Round firstRoundOfGame = rounds.get(0);

            if (game.isProductionGame() && firstRoundOfGame.getCostPerPlayer() != null) {
                //get a list of all the joined_round/abandoned_round cashpool transactions
                List<CashPoolTransaction2> joinedAndAbandonedList = _shoutContestService.getJoinedAndAbandonedForContext(firstRoundOfGame.getId());

                //filter this list to the most recent transaction type for each subscriber
                Map<Long, CashPoolTransaction2> transactionTypeMap = new HashMap<>();
                for (CashPoolTransaction2 cpt : joinedAndAbandonedList) {
                    CashPoolTransaction2 existingCpt = transactionTypeMap.get(cpt.getSubscriberId());
                    if (existingCpt == null) {
                        //first time, add it in
                        transactionTypeMap.put(cpt.getSubscriberId(), cpt);
                    } else {
                        //see if this is newer or older. if newer, replace
                        if (cpt.getTransactionDate().after(existingCpt.getTransactionDate())) {
                            transactionTypeMap.put(cpt.getSubscriberId(), cpt);
                        }
                    }
                }

                //refund any players who have the most recent transaction type of JOINED_ROUND
                for (long sId : transactionTypeMap.keySet()) {
                    CashPoolTransaction2 cpt = transactionTypeMap.get(sId);
                    if (cpt.getType() == CashPoolTransaction2.TYPE.JOINED_ROUND) {
                        //issue the refund
                        _shoutContestService.addCashPoolTransaction(
                                sId, firstRoundOfGame.getCostPerPlayer(), CashPoolTransaction2.TYPE.ABANDONED_ROUND, "game cancelled", null, firstRoundOfGame.getId());
                    }
                }

                //refund any sponsored players
                List<Sponsor> sponsors = _dao.getSponsorsForGame(gameId);
_logger.info(">>> there are " + sponsors.size() + " sponsor players");
                Map<Integer, SponsorCashPool> sponsorCashPoolMap = new HashMap<>();
                for (Sponsor sponsor : sponsors) {
                    //grab the pool (fetch/cache if necessary)
                    SponsorCashPool pool = sponsorCashPoolMap.get(sponsor.getSponsorCashPoolId());
                    if (pool == null) {
                        pool = _dao.getSponsorCashPoolById(sponsor.getSponsorCashPoolId());
                        sponsorCashPoolMap.put(sponsor.getSponsorCashPoolId(), pool);
                    }

                    //add the amount back to the pool
                    pool.setAmount(pool.getAmount() + firstRoundOfGame.getCostPerPlayer());
_logger.info(">>> refunding " + firstRoundOfGame.getCostPerPlayer() + " back to sponsor pool " + pool.getSponsorCashPoolId());
                    _dao.updateSponsorCashPool(pool);
                    _dao.addSponsorCashPoolTransaction(pool.getSponsorCashPoolId(), firstRoundOfGame.getCostPerPlayer(), "REFUND_GAME_CANCELLED");
                }
            }

            //release any sponsored players
_logger.info(">>> releasing sponsor players for game: " + gameId);
            _dao.releaseSponsorPlayersForGame(gameId);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        //notify any interested parties that this game has been cancelled
        MessageBus.sendMessage(new Message(Message.MESSAGE_TYPE_GAME_CANCELLED, gameId));

        return null;
    }

//    private Map<String, Object> cloneGame(Map<String, String> props, String messageId)
//    throws PublishResponseError
//    {
////BUG - where is GamePayout?
//        String gameId = getParamFromProps(props, messageId, "cloneGame", "gameId", true);
//        Date expectedStartDateForPoolPlay = getDateParamFromProps(props, messageId, "cloneGame", "expectedStartDateForPoolPlay", true);
//        Date expectedStartDateForBracketPlay = getDateParamFromProps(props, messageId, "cloneGame", "expectedStartDateForBracketPlay", true);
//
//        String val = getParamFromProps(props, messageId, "cloneGame", "gameNames", true);
//        TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
//        Map<String, String> gameNames;
//        try {
//            gameNames = _jsonMapper.readValue(val, typeRef);
//        } catch (IOException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "cloneGame", false, "invalidParam", "gameNames");
//        }
//
//        //see if the game being cloned is using an inactive payout model. if so, don't allow
//        boolean isPayoutModelActive = (Boolean) wrapInTransaction(this::isPayoutModelActiveTransaction, gameId);
//        if (!isPayoutModelActive) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "cloneGame", false, "payoutModelNotActive");
//        }
//
//        Game game = _shoutContestService.getGame(gameId);
//
//        //clone all the pool rounds and the first bracket round
//        List<String> roundIdsToClone = new ArrayList<>();
//        List<Date> expectedStartDatesForEachRoundBeingCloned = new ArrayList<>();
//        List<Round> rounds = _shoutContestService.getRoundsForGame(gameId);
//        Collections.sort(rounds, new Comparator<Round>() {
//            @Override
//            public int compare(Round lhs, Round rhs)
//            {
//                if (lhs.getRoundSequence() == rhs.getRoundSequence()) return 0;
//                else return lhs.getRoundSequence() < rhs.getRoundSequence() ? -1 : 1;
//            }
//        });
//        for (Round round : rounds) {
//            roundIdsToClone.add(round.getId());
//
//            if (round.getRoundType() == Round.ROUND_TYPE.BRACKET) {
//                expectedStartDatesForEachRoundBeingCloned.add(expectedStartDateForBracketPlay);
//                break;
//            } else {
//                expectedStartDatesForEachRoundBeingCloned.add(expectedStartDateForPoolPlay);
//            }
//        }
//
//        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
//        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
//        try {
//            _shoutContestService.cloneGame(gameId, game.getInviteCode(), expectedStartDatesForEachRoundBeingCloned, gameNames, roundIdsToClone);
//
//            //publish (even though it's just PENDING, the admin tools need access to it)
//            publishGameWithExtrasToWds(gameId);
//
//            //update game player count document (just so it exists, even though count starts at 0)
//            _shoutContestService.publishGamePlayerCountToWds(gameId, new ArrayList<>());
//
//            /*List<PayoutTableRow> rows =*/ publishPayoutTable(game, rounds, null, rounds.get(0).getMaximumPlayerCount(), _engineCoordinator, _wdsService, _dao, _logger);
//
//            _transactionManager.commit(txStatus);
//            txStatus = null;
//        } catch (PayoutManagerException e) {
//            //this shouldn't happen - any errors should have been caught before this
//            _logger.error(MessageFormat.format("Unable to generate payout table: {0}\n\t{1}", e.getErrorTypeCode(), e.getErrorDetails()));
//
//        } finally {
//            if (txStatus != null) {
//                _transactionManager.rollback(txStatus);
//                txStatus = null;
//            }
//        }
//
//        return null;
//    }

//    private Boolean isPayoutModelActiveTransaction(Object param)
//    {
//        String gameId = (String) param;
//
//        int payoutModelId = _dao.getGamePayout(gameId).getPayoutModelId();
//        boolean isPayoutModelActive = _dao.getPayoutModel(payoutModelId).isActive();
//
//        return isPayoutModelActive;
//    }

    private Map<String, Object> getCategoryIdsToKeys(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        List<Tuple<String>> result;

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            result = _dao.getQuestionCategoryIdToKey();

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return new FastMap<>("result", result);
    }

    private Map<String, Object> createCategory(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        TypeReference<QuestionCategory> typeRef = new TypeReference<QuestionCategory>(){};
        QuestionCategory category = getJsonObjectFromProps(props, messageId, "createCategory", "category", true, typeRef);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //make sure this category key hasn't already been used
            String categoryKey = category.getCategoryKey();
            QuestionCategory existingCategory = QuestionCategoryHelper.getQuestionCategoryByKey(categoryKey, _dao);
            if (existingCategory != null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createCategory", false, "categoryAlreadyExists");
            }

            //make sure the category has at least an english translation
            if (category.getCategoryName() == null || category.getCategoryName().get("en") == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createCategory", false, "englishTranslationRequired");
            }

            //ignore whatever uuid they may have passed and create a new one
            category.setId(UUID.randomUUID().toString());

            //add to the db
            _dao.insertQuestionCategory(category);

            for (String languageCode : category.getCategoryName().keySet()) {
                String val = category.getCategoryName().get(languageCode);
                _dao.insertOrReplaceMultiLocalizationValue(category.getId(), "categoryName", languageCode, val);
            }

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return new FastMap<>("category", category);
    }

    private Map<String, Object> updateCategory(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        TypeReference<QuestionCategory> typeRef = new TypeReference<QuestionCategory>(){};
        QuestionCategory category = getJsonObjectFromProps(props, messageId, "updateCategory", "category", true, typeRef);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //make sure this category exists
            String categoryId = category.getId();
            QuestionCategory existingCategory = QuestionCategoryHelper.getQuestionCategoryById(categoryId, _dao);
            if (existingCategory == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "updateCategory", false, "categoryNotFound");
            }

            //make sure the category has at least an english translation
            if (category.getCategoryName() == null || category.getCategoryName().get("en") == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "updateCategory", false, "englishTranslationRequired");
            }

            //remove any existing category names
            _dao.removeMutliLocalizationValues(categoryId, "categoryName");

            //add the category names
            for (String languageCode : category.getCategoryName().keySet()) {
                String val = category.getCategoryName().get(languageCode);
                _dao.insertOrReplaceMultiLocalizationValue(category.getId(), "categoryName", languageCode, val);
            }

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

    private Map<String, Object> deleteCategory(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String categoryId = getParamFromProps(props, messageId, "deleteCategory", "categoryId", true);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //make sure the category exists
            QuestionCategory category = _dao.getQuestionCategoryById(categoryId);
            if (category == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "deleteCategory", false, "categoryNotFound");
            }

            //if there are any questions that use this category, fail and return the list of questionId's
            List<String> questionIds = _dao.getQuestionIdsForCategory(categoryId);
            if (questionIds != null && questionIds.size() > 0) {
                String questionIdsAsCommaDelimitedList = questionIds.stream().collect(Collectors.joining(","));
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "deleteCategory", false, "categoryHasQuestions", questionIdsAsCommaDelimitedList);
            }

            //delete the category (and multilocalization values)
            QuestionCategoryHelper.deleteQuestionCategory(categoryId, _dao);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

    private Map<String, Object> createQuestion(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        TypeReference<Question> typeRef = new TypeReference<Question>(){};
        Question question = getJsonObjectFromProps(props, messageId, "createQuestion", "question", true, typeRef);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            validateQuestion(question, props, "createQuestion", messageId, true);
            createQuestion(question);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

    private Map<String, Object> changeQuestionCategories(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String questionId = getParamFromProps(props, messageId, "changeQuestionCategories", "questionId", true);
        String categoryIdCommaDelimitedList = getParamFromProps(props, messageId, "changeQuestionCategories", "categoryIds", true);
        List<String> categoryIds = Arrays.asList(categoryIdCommaDelimitedList.split(","));

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //make sure the question exists
            Question question = _dao.getQuestion(questionId);
            if (question == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changeQuestionCategories", false, "invalidParam", "questionId");
            }

            //make sure each category exists
            categoryIds.forEach(categoryId -> {
                QuestionCategory category = _dao.getQuestionCategoryById(categoryId);
                if (category == null) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changeQuestionCategories", false, "invalidParam", "categoryIds");
                }
            });

            //remove all the old categories
            _dao.deleteQuestionCategories(questionId);

            //add each of the new categories
            categoryIds.forEach(categoryId -> {
                _dao.addQuestionCategory(questionId, categoryId);
            });

            //notify the MME that questions have changed
            _engineCoordinator.notifyQuetionListChanged();

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

    private Map<String, Object> getQuestionsByState(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String val = getParamFromProps(props, messageId, "getQuestionsByState", "state", true);

        Question.STATUS status;
        try {
            status = Question.STATUS.valueOf(val);
        } catch (IllegalArgumentException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "getQuestionsByState", false, "invalidParam", "state");
        }

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        List<Question> questions;
        try {
            List<String> questionIds = _dao.getQuestionIdsByState(status);
            questions = new ArrayList<>(questionIds.size());
            for (String questionId : questionIds) {
                questions.add(QuestionHelper.getQuestion(questionId, _dao));
            }

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return new FastMap<>("questions", questions);
    }

    private Map<String, Object> changeQuestionState(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String val = getParamFromProps(props, messageId, "changeQuestionState", "state", true);
        String questionId = getParamFromProps(props, messageId, "changeQuestionState", "questionId", true);

        Question.STATUS status;
        try {
            status = Question.STATUS.valueOf(val);
        } catch (IllegalArgumentException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changeQuestionState", false, "invalidParam", "state");
        }

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            Question question = _dao.getQuestion(questionId);
            if (question == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changeQuestionState", false, "invalidParam", "questionId");
            }

            //the following are the only valid state changes:
            //UNPUBLISHED -> PUBLISHED
            //PUBLISHED -> RETIRED
            //RETIRED -> PUBLISHED

            boolean validStateChange;
            switch (question.getStatus())
            {
                case UNPUBLISHED:
                    validStateChange = status == Question.STATUS.PUBLISHED;
                    break;

                case PUBLISHED:
                    validStateChange = status == Question.STATUS.RETIRED;
                    break;

                case RETIRED:
                    validStateChange = status == Question.STATUS.PUBLISHED;
                    break;

                default:
                    //this won't actually happen, but the compiler isn't that smart i guess...
                    validStateChange = false;
            }

            if (!validStateChange) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changeQuestionState", false, "invalidStateChange");
            }

            _dao.updateQuestionStatus(question.getId(), status);

            //if the new state is PUBLISHED or RETIRED, notify the MME that questions have changed
            if (status == Question.STATUS.PUBLISHED || status == Question.STATUS.RETIRED) {
                _engineCoordinator.notifyQuetionListChanged();
            }

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

    private Map<String, Object> updateQuestion(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        TypeReference<Question> typeRef = new TypeReference<Question>(){};
        Question question = getJsonObjectFromProps(props, messageId, "updateQuestion", "question", true, typeRef);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //make sure the question exists
            Question origQuestion = _dao.getQuestion(question.getId());
            if (origQuestion == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "updateQuestion", false, "invalidParam", "questionId");
            }

            //question can only be updated if in the UNPUBLISHED state
            if (origQuestion.getStatus() != Question.STATUS.UNPUBLISHED) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "updateQuestion", false, "invalidStatus", origQuestion.getStatus().toString());
            }

            validateQuestion(question, props, "updateQuestion", messageId, false);
            deleteQuestion(question);
            createQuestion(question);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

    private Map<String, Object> deleteQuestion(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String questionId = getParamFromProps(props, messageId, "deleteQuestion", "questionId", true);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //make sure the question exists
            Question origQuestion = _dao.getQuestion(questionId);
            if (origQuestion == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "deleteQuestion", false, "invalidParam", "questionId");
            }
            origQuestion = QuestionHelper.getQuestion(questionId, _dao);

            //question can only be deleted if in the UNPUBLISHED state
            if (origQuestion.getStatus() != Question.STATUS.UNPUBLISHED) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "deleteQuestion", false, "invalidStatus", origQuestion.getStatus().toString());
            }

            //delete everything from the db
            deleteQuestion(origQuestion);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

//    private Map<String, Object> sendPushToDevice(Map<String, String> props, String messageId)
//    throws PublishResponseError
//    {
//        int subscriberId = getIntParamFromProps(props, messageId, "sendPushToDevice", "subscriberId", true);
//        //_logger.info(">>> subscriberId: " + subscriberId);
//        //Subscriber subscriber = _identityService.getSubscriberById(subscriberId);
//        //_logger.info(MessageFormat.format(">>> subscriber: {0}", subscriber.getNickname()));
//
////        sendCustomPush(_transactionManager, _dao, _pushService, _logger,
////                subscriber.getSubscriberId(), subscriber.getContextId(), "apsCategory", "{\"hello\":\"world\"}", "JUST_A_TEST", null);
//
//        Game game = new Game();
//        game.setId("0195d2c3-fb1b-4b16-a1cf-827262f49ee8");
//        game.setAllowableAppIds(new HashSet<>(Arrays.asList(6)));
//
//        //simulate a payout notification
//        Map<String, Object> extrasMap = new HashMap<>();
//        extrasMap.put("amount", 3.6F);
//        sendGamePush(_transactionManager, _dao, _pushService, _logger, subscriberId, game, "apsCategory", "apsMessage", "SM_PAYOUT_NOTIFICATION", extrasMap);
//
//        return null;
//    }

//    private Map<String, Object> sendSocketIoMessage(Map<String, String> props, String messageId)
//    throws PublishResponseError
//    {
//        String primaryIdHash = getParamFromProps(props, messageId, "sendSocketIoTest", "primaryIsHash", true);
//        String message = getParamFromProps(props, messageId, "sendSocketIoTest", "message", true);
//
//        if (_socketIoSocket != null) {
//            Map<String, Object> msg = new HashMap<>();
//            msg.put("recipient", primaryIdHash);
//            msg.put("message", message);
//
//            ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
//            String payload;
//            try {
//                payload = _jsonMapper.writeValueAsString(msg);
//                SocketIoLogger.log(_triggerService, null, "send_arbitrary_msg", payload, "SENDING");
//                _socketIoSocket.emit("send_arbitrary_msg", payload);
//                SocketIoLogger.log(_triggerService, null, "send_arbitrary_msg", payload, "SENT");
//
//            } catch (JsonProcessingException e) {
//                _logger.error("unable to wrap payload as json", e);
//            }
//        } else {
//            _logger.error("_socketIoSocket is null");
//        }
//
//        return null;
//    }

    private Map<String, Object> gameGetWinners(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        List<GameWinner> winners;

        String gameId = getParamFromProps(props, messageId, "gameGetWinners", "gameId", true);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            winners = _dao.getGameWinners(gameId);
            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return new FastMap<>("winners", winners);
    }

    private Map<String, Object> payoutMarkAsPaid(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        List<GameWinner> payees;

        String gameId = getParamFromProps(props, messageId, "payoutMarkAsPaid", "gameId", false);
        if (gameId != null) {
            //pay out everyone in the game who won something
            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
            try {
                payees = _dao.getGameWinners(gameId);
                _transactionManager.commit(txStatus);
                txStatus = null;
            } finally {
                if (txStatus != null) {
                    _transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }

        } else {
            //paying out a single person
            int manualRedeemRequestId = getIntParamFromProps(props, messageId, "payoutMarkAsPaid", "manualRedeemRequestId", true);
            ManualRedeemRequest mrr = _shoutContestService.getManualRedeemRequest(manualRedeemRequestId);

            long subscriberId = mrr.getSubscriberId();
            float amount = mrr.getAmount();

            GameWinner gw = new GameWinner();
            gw.setSubscriberId(subscriberId);
            gw.setAmount((double) amount);

            _shoutContestService.markManualRedeemRequestFulfilled(manualRedeemRequestId);

            payees = Collections.singletonList(gw);
        }

        //mark them all as paid
        payees.stream()
            .filter(payee -> payee.getAmount() != null && payee.getAmount() > 0)
            .forEach(payee -> {
                _shoutContestService.addCashPoolTransaction(
                        payee.getSubscriberId(), -payee.getAmount(), CashPoolTransaction2.TYPE.PAID, null, null, payee.getGameId());
            });

        return null;
    }

    private Map<String, Object> getOustandingManualRequests(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        List<AwaitingPayout> awaitingPayout = new ArrayList<>();

        List<ManualRedeemRequest> outstandingRequests = _shoutContestService.getOutstandingManualRedeemRequests();
        outstandingRequests.forEach(mrr -> {
            Subscriber s = _identityService.getSubscriberById(mrr.getSubscriberId());
            awaitingPayout.add(AwaitingPayout.fromSubscriber(s, mrr.getAmount(), mrr.getManualRedeemRequestId()));
        });

        return new FastMap<>("awaitingPayout", awaitingPayout);
    }

    private Map<String, Object> subscriberSearch(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "subscriberSearch";
        boolean useDate = props.containsKey("fromDate") || props.containsKey("toDate");

        Date from = getDateParamFromProps(props, messageId, docType, "fromDate", false);
        if (from == null) {
            from = new Date(0);
        }

        Date to = getDateParamFromProps(props, messageId, docType, "toDate", false);
        if (to == null) {
            to = new Date();
        }

        String partialEmail = getParamFromProps(props, messageId, docType, "partialEmail", false);

        String role = getParamFromProps(props, messageId, docType, "role", false);

        //no valid search criteria passed. return empty list
        if (!useDate && StringUtil.isEmpty(partialEmail) && StringUtil.isEmpty(role)) {
            return new FastMap<>("subscribers", new ArrayList<>());
        }

        @SuppressWarnings("unchecked")
        List<SubscriberFromSearch> subscribers = (List<SubscriberFromSearch>) wrapInTransaction(this::subscriberSearchDao, new Object[] {useDate, from, to, partialEmail, role});

        return new FastMap<>("subscribers", subscribers);
    }

    private List<SubscriberFromSearch> subscriberSearchDao(Object param)
    {
        Object[] params = (Object[]) param;
        boolean useDate = (Boolean) params[0];
        Date from = (Date) params[1];
        Date to = (Date) params[2];
        String partialEmail = (String) params[3];
        String role = (String) params[4];

        if (useDate) {
            //search by date
            return _dao.getSubscribersInSignupDateRange(from, to);

        } else if (!StringUtils.isEmpty(partialEmail)) {
            //search by (partial) email
            List<Subscriber> results = _identityService.searchSubsByEmail(getAppId(), partialEmail);
            return results.stream()
                .map(SubscriberFromSearch::fromSubscriber)
                .collect(Collectors.toList());

        } else {
            //search by role
            List<Long> subscriberIds = _identityService.getSubscriberIdsWithRole(role);
            List<Subscriber> results = _identityService.getSubscribers(subscriberIds);
            return results.stream()
                .map(SubscriberFromSearch::fromSubscriber)
                .collect(Collectors.toList());
        }
    }

    private Map<String, Object> setCanSeeContentWithoutLogin(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "canSeeContentWithoutLogin";
        boolean canSeeContentWithoutLogin = getBooleanParamFromProps(props, messageId, docType, "canSeeContentWithoutLogin", true);

        //save it in the config file
        File configFile = new File(_stateDir, "canSeeContentWithoutLogin.dat");
        try {
            writeToFile(configFile, canSeeContentWithoutLogin);
        } catch (IOException e) {
            _logger.warn("unable to write config val for canSeeContentWithoutLogin.dat", e);
        }

        //publish to the wds
        publishJsonWdsDoc(
                _logger, _wdsService, null,
                "/canSeeContentWithoutLogin.json",
                new FastMap<>("canSeeContentWithoutLogin", canSeeContentWithoutLogin));

        return null;
    }

    private class PlayerInfo
    {
        @SuppressWarnings("unused")
        public long subscriberId;
        @SuppressWarnings("unused")
        public String nickname;
        @SuppressWarnings("unused")
        public String email;
        @SuppressWarnings("unused")
        public boolean freePlayer;

        public PlayerInfo(long subscriberId, String nickname, String email, boolean freePlayer)
        {
            this.subscriberId = subscriberId;
            this.nickname = nickname;
            this.email = email;
            this.freePlayer = freePlayer;
        }
    }

    private Map<String, Object> gameViewPlayerInfo(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "gameViewPlayerInfo";

        String gameId = getParamFromProps(props, messageId, docType, "gameId", true);

        //get the bots
        @SuppressWarnings("unchecked")
        List<Long> botIds = (List<Long>) wrapInTransaction(this::getBotsForGameTransaction, gameId);

        //get the GamePlayers (minus the bots)
        List<GamePlayer> gamePlayers = _shoutContestService.getGamePlayersWithoutBots(gameId, botIds);

        //merge into the required format
        List<PlayerInfo> playerInfo = new ArrayList<>(gamePlayers.size());
        for (GamePlayer gp : gamePlayers) {
            Subscriber s = _identityService.getSubscriberById(gp.getSubscriberId());
            playerInfo.add(new PlayerInfo(gp.getSubscriberId(), s.getNickname(), s.getEmail(), gp.isFreeplay()));
        }

        return new FastMap<>("playerInfo", playerInfo);
    }

    private List<Long> getBotsForGameTransaction(Object param)
    {
        String gameId = (String) param;

        return _botEngine.getBotsForGame(gameId);
    }

    private Map<String, Object> gameAddSponsorPlayers(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "gameAddSponsorPlayers";

        String gameId = getParamFromProps(props, messageId, docType, "gameId", true);
        int numberOfPlayers = getIntParamFromProps(props, messageId, docType, "numberOfPlayers", true);
        String sponsorEmail = getParamFromProps(props, messageId, docType, "sponsorEmail", true);

        //look up the subscriber via the email
        int appId = getAppId();
        Subscriber subscriber = _identityService.getSubscriberByEmail(appId, sponsorEmail);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "sponsorEmail");
        }

        //make sure the given subscriber is a sponsor
        if (!_identityService.hasRole(subscriber.getSubscriberId(), new HashSet<>(Arrays.asList(BaseSmMessageHandler.SPONSOR)), true)) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "notASponsor");
        }

        //make sure the numberOfPlayers is positive
        if (numberOfPlayers <= 0) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "numberOfPlayersMustBePositive");
        }

        //make sure the game exists and is in a correct state to allow adding sponsor players
        Game game = _shoutContestService.getGame(gameId);
        if (game == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "gameId");
        }
        if (game.getGameStatus() != Game.GAME_STATUS.OPEN) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "gameNotOpen");
        }

        //the payout model
        PayoutModel pm = (PayoutModel) wrapInTransaction(this::gameAddSponsorPlayers1Dao, gameId);

        //the first bracket round
        Round round = _shoutContestService.getRoundsForGame(game.getId()).stream()
            .filter(r -> r.getRoundType() == Round.ROUND_TYPE.BRACKET)
            .findFirst().orElseThrow(() -> new IllegalStateException());
//_logger.info(">>> round: id: " + round.getId() + ", #: " + round.getRoundSequence() + ", #players: " + round.getMaximumPlayerCount());

        double costToJoin = pm.getEntranceFeeAmount();
        double totalCost = costToJoin * numberOfPlayers;

        Object[] result = (Object[]) wrapInTransaction(this::gameAddSponsorPlayers2Dao, new Object[] {subscriber, gameId});
        SponsorCashPool pool = (SponsorCashPool) result[0];
        int availableSponsorPlayers = (Integer) result[1];
        @SuppressWarnings("unchecked") List<Long> botIdsForgame = (List<Long>) result[2];

        //make sure the sponsor cash pool has enough money to cover adding numberOfPlayers
_logger.info(">>> costToJoin: " + costToJoin + " , poolAmount: " + (pool == null ? "NO POOL" : pool.getAmount()) + ", totalCost: " + totalCost);
        if (pool == null || pool.getAmount() < totalCost) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "notEnoughSponsorPoolCash");
        }

        //make sure there are enough sponsor players available to cover the requested amount
        if (availableSponsorPlayers < numberOfPlayers) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "notEnoughSponsorPlayers");
        }

        //see how many game slots are available and make sure adding numberOfPlayers won't exceed that (totalAllowed - numberAlreadyIn)
        int availablePlayerCount = round.getMaximumPlayerCount() - round.getCurrentPlayerCount();
//_logger.info(">>> numberOfPlayers: " + numberOfPlayers + ", availablePlayerCount: " + availablePlayerCount);
        if (numberOfPlayers > availablePlayerCount) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "tooManySponsorPlayers");
        }

        //add the sponsor players to the game
        Exception result2 = (Exception) wrapInTransaction(this::gameAddSponsorPlayers3Dao, new Object[] {
                pool, game, numberOfPlayers, round, costToJoin, totalCost
        });
        if (result2 != null) {
            if (result2 instanceof NotEnoughSponsorsException) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "notEnoughSponsorPlayers");
            } else if (result2 instanceof NotEnoughSponsorPoolCashException) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "notEnoughSponsorPoolCash");
            }
        }

        //update the game player count document
        _shoutContestService.publishGamePlayerCountToWds(gameId, botIdsForgame);

        return null;
    }

    private PayoutModel gameAddSponsorPlayers1Dao(Object param)
    {
        String gameId = (String) param;
        GamePayout gp = _dao.getGamePayout(gameId);
        PayoutModel pm = _dao.getPayoutModel(gp.getPayoutModelId());
        return pm;
    }

    private Object[] gameAddSponsorPlayers2Dao(Object param)
    {
        Object[] params = (Object[]) param;
        Subscriber sponsorSubscriber = (Subscriber) params[0];
        String gameId = (String) params[1];

        SponsorCashPool pool = _dao.getSponsorCashPoolByPoolOwnerSubscriberId(sponsorSubscriber.getSubscriberId());
        int availableSponsorPlayers = _dao.getNumberOfAvailableSponsors();
        List<Long> botIdsForGame = _botEngine.getBotsForGame(gameId);

        return new Object[] {pool, availableSponsorPlayers, botIdsForGame};
    }

    private Exception gameAddSponsorPlayers3Dao(Object param)
    {
        Object[] params = (Object[]) param;
        SponsorCashPool pool = (SponsorCashPool) params[0];
        Game game = (Game) params[1];
        int numberOfPlayers = (Integer) params[2];
        Round round = (Round) params[3];
        double costToJoin = (Double) params[4];
        double totalCost = (Double) params[5];
_logger.info("numberOfPlayers: " + numberOfPlayers + ", costToJoin(per): " + costToJoin + ", totalCost: " + totalCost);

        //add the sponsor players to the game
        try {
            _sponsorEngine.addSponsorsToGame(pool.getSponsorCashPoolId(), game, numberOfPlayers, round, costToJoin, totalCost);
        } catch (NotEnoughSponsorsException | NotEnoughSponsorPoolCashException e) {
            return e;
        }

        return null;
    }

//    private Map<String, Object> pushTest(Map<String, String> props, String messageId)
//    throws PublishResponseError
//    {
//        String docType = "pushTest";
//
//        Subscriber subscriber = getSubscriber(props, messageId, docType);
//        int appId = getAppId(props, messageId, docType);
//
//        sendCustomPush(
//            _transactionManager, _dao, _pushService, _logger,
//            subscriber.getSubscriberId(), "en", appId, "apsCategory", "test push title", "test push body",
//            "TEST_PUSH", null);
//
//        return null;
//    }

    //assumes caller has set up a transaction prior to this call
    private void validateQuestion(Question question, Map<String, String> props, String docType, String messageId, boolean generateUuids)
    throws PublishResponseError
    {
        //make sure there are at least 2 answers
        if (question.getAnswers() == null || question.getAnswers().size() < 2) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createQuestion", false, "notEnoughAnswers");
        }

        //make sure there is at least 1 supported language
        if (question.getLanguageCodes() == null || question.getLanguageCodes().size() < 1) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createQuestion", false, "mustHaveAtLeastOneLanguageCode");
        }

        //make sure a question was supplied
        if (question.getQuestionText() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createQuestion", false, "noQuestionGiven");
        }

        //make sure there is a question text for every supported language
        for (String languageCode : question.getLanguageCodes()) {
            if (question.getQuestionText().get(languageCode) == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createQuestion", false, "noQuestionForLanguageCode", languageCode);
            }
        }

        //ignore whatever uuid may have been passed and create our own
        String questionId;
        if (generateUuids) {
            questionId = UUID.randomUUID().toString();
            question.setId(questionId);
        } else {
            questionId = question.getId();
        }

        //ditto with the answers - use our own uuid's
        //also, make sure at least (and at most) 1 answer has the correct flag set
        //also, make sure there is an answer for every language code
        int numCorrect = 0;
        for (QuestionAnswer answer : question.getAnswers()) {
            answer.setQuestionId(questionId);
            if (generateUuids) {
                answer.setId(UUID.randomUUID().toString());
            }

            for (String languageCode : question.getLanguageCodes()) {
                if (answer.getAnswerText().get(languageCode) == null) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createQuestion", false, "noAnswerForLanguageCode", languageCode);
                }
            }

            if (answer.getCorrect() != null && answer.getCorrect()) {
                numCorrect++;
            }
        }

        if (numCorrect != 1) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createQuestion", false, "exactlyOneAnswerMustBeMarkedAsCorrect");
        }

        //make sure there is at least 1 category specified
        if (question.getQuestionCategoryUuids() == null || question.getQuestionCategoryUuids().size() == 0) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createQuestion", false, "atLeastOneCategoryMustBeSpecified");
        }

        //make sure the difficulty is valid
        if (question.getDifficulty() < 0 || question.getDifficulty() > 10) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createQuestion", false, "invalidDifficulty");
        }

        //make sure all the categories are valid
        List<QuestionCategory> categories = _dao.getAllQuestionCategories();
        Set<String> categoryUuids = new HashSet<>();
        for (QuestionCategory qc : categories) {
            categoryUuids.add(qc.getId());
        }
        for (String categoryId : question.getQuestionCategoryUuids()) {
            if (!categoryUuids.contains(categoryId)) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "createQuestion", false, "invalidCategoryUuid", categoryId);
            }
        }
    }

    //assumes caller has set up a transaction and done all checking prior to this call
    private void deleteQuestion(Question question)
    {
        for (QuestionAnswer answer : question.getAnswers()) {
            _dao.removeMutliLocalizationValues(answer.getId(), "answerText");
        }
        _dao.deleteQuestionAnswers(question.getId());

        _dao.removeMutliLocalizationValues(question.getId(), "questionText");
        _dao.deleteQuestionCategories(question.getId());
        _dao.deleteQuestionForbiddenCountryCodes(question.getId());
        _dao.deleteQuestionLanguageCodes(question.getId());
        _dao.deleteQuestion(question.getId());
    }

    //assumes caller has set up a transaction and done all checking prior to this call
    private void createQuestion(Question question)
    {
        _dao.createQuestion(question);

        for (String languageCode : question.getLanguageCodes()) {
            _dao.addQuestionLanguageCode(question.getId(), languageCode);
        }

        if (question.getForbiddenCountryCodes() != null) {
            for (String countryCode : question.getForbiddenCountryCodes()) {
                _dao.addQuestionForbiddenCountryCode(question.getId(), countryCode);
            }
        }

        for (String categoryUuid : question.getQuestionCategoryUuids()) {
            _dao.addQuestionCategory(question.getId(), categoryUuid);
        }

        for (String languageCode : question.getLanguageCodes()) {
            String questionTxt = question.getQuestionText().get(languageCode);
            _dao.insertOrReplaceMultiLocalizationValue(question.getId(), "questionText", languageCode, questionTxt);
        }

        for (QuestionAnswer answer : question.getAnswers()) {
            _dao.addQuestionAnswer(answer);

            for (String languageCode : question.getLanguageCodes()) {
                String answerTxt = answer.getAnswerText().get(languageCode);
                _dao.insertOrReplaceMultiLocalizationValue(answer.getId(), "answerText", languageCode, answerTxt);
            }
        }
    }

}
