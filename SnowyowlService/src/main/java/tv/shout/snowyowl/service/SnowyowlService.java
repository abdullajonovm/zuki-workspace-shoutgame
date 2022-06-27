package tv.shout.snowyowl.service;

import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.meinc.commons.postoffice.service.EmailAddress;
import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.identity.service.IIdentityService;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.notification.service.INotificationService;
import com.meinc.trigger.domain.Trigger;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.IWebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.IMessageTypeHandler;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webdatastore.service.IWebDataStoreService;

import clientproxy.webdatastoreservice.WebDataStoreServiceClientProxy;
import io.socket.client.IO;
import io.socket.client.Socket;
import tv.shout.collector.ICollectorMessageHandler;
import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.simplemessagebus.MessageProcessor;
import tv.shout.snowyowl.collector.AdminHandler;
import tv.shout.snowyowl.collector.BaseSmMessageHandler;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.collector.GameHandler;
import tv.shout.snowyowl.collector.NoQuestionsPossibleException;
import tv.shout.snowyowl.collector.PoolPlayMatchesInProgressException;
import tv.shout.snowyowl.collector.SubscriberHandler;
import tv.shout.snowyowl.common.EmailSender;
import tv.shout.snowyowl.common.FileHandler;
import tv.shout.snowyowl.common.GamePublisher;
import tv.shout.snowyowl.common.GameStatsHandler;
import tv.shout.snowyowl.common.SmsSender;
import tv.shout.snowyowl.common.SocketIoLogger;
import tv.shout.snowyowl.common.SubscriberStatsHandler;
import tv.shout.snowyowl.common.WdsPublisher;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.AffiliatePlan;
import tv.shout.snowyowl.domain.QuestionCategory;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayout;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayoutSubscriberInfo;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayoutSubscriberNetworkSize;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayoutSubscriberWinnings;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.util.FastMap;
import tv.shout.util.StringUtil;

@Service(
    namespace=       ISnowyowlService.SERVICE_NAMESPACE,
    name=            ISnowyowlService.SERVICE_NAME,
    interfaces=      ISnowyowlService.SERVICE_INTERFACE,
    version=         ISnowyowlService.SERVICE_VERSION,
    exposeAs=        ISnowyowlService.class
)
public class SnowyowlService
implements ISnowyowlService, SmsSender, FileHandler, WdsPublisher, GamePublisher, EmailSender
{
    public static final Logger SUB_EVENT_LOGGER = Logger.getLogger("subeventlogger");

    private static Logger _logger = Logger.getLogger(SnowyowlService.class);
    //protected static ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
    public static final ServiceEndpoint ENDPOINT = new ServiceEndpoint(SERVICE_NAMESPACE, SERVICE_NAME, SERVICE_VERSION);

    private final List<String> _validTriggerKeys = Arrays.asList(
        ISnowyowlService.TRIGGER_KEY_SOCKET_IO_MESSAGE, ISnowyowlService.TRIGGER_KEY_SUBSCRIBER_STATS_MESSAGE
    );

    @Value("${hostname.suffix}")
    private String _servername;

    @Value("${sm.doc.producer:false}")
    private boolean _docProducer;

    @Value("${sm.engine.runner:false}")
    private boolean _engineRunner;

    @Value("${socket.io.ips}")
    private String _socketIoIpsUnparsed;

    @Value("${sm.signup.bonus:0}")
    private double _signupBonus;

    @Value("${twilio.from.number}")
    private String _twilioFromNumber;

    @Value("${twilio.account.sid}")
    private String _twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String _twilioAuthToken;

//    @Value("${pregame.sms.notification_time_mins}")
//    private long _pregameSmsNotificationTimeMins;

    @Value("${sm.engine.statedir}")
    private String _stateDir;

    @Value("${sm.app.displayname}")
    private String _appDisplayName;

    @Value("${sm.socketio.monitor.subscriber_ids}")
    private String _socketIoMonitoredSubscriberIdCommaDelimitedString;
    private Set<Integer> _socketIoMonitoredSubscriberIds;

    @Autowired
    private IWebCollectorService _webCollectorService;

    @Autowired
    private IShoutContestService _shoutContestService;

    @Autowired
    private INotificationService _notificationService;

    @Autowired
    private IIdentityService _identityService;

    @Autowired
    private IPostOffice _postOfficeService;

    @Autowired
    private ITriggerService _triggerService;

    @Autowired
    private IDaoMapper _dao;

    @Autowired
    private EngineCoordinator _engineCoordinator;

    @Autowired
    private LogFileAnalyzer _logFileAnalyzer;

    @Autowired
    private SubscriberStatsHandler _subscriberStatsHandler;

    @Autowired
    protected PlatformTransactionManager _transactionManager;

    @Autowired
    private CommonBusinessLogic _commonBusinessLogic;

    @Autowired
    private MessageBus _messageBus;

    @Autowired
    private GameStatsHandler _gameStatsHandler;

    @Autowired
    private MessageProcessor[] _allMessageProcessors;

    @Resource(name="webDataStoreService")
    private IWebDataStoreService _wdsService;

    @Resource(name="gameHandler")
    private GameHandler _gameHandler;

    @Resource(name="adminHandler")
    private AdminHandler _adminHandler;

    @Resource(name="subscriberHandler")
    private SubscriberHandler _subscriberHandler;

    @Autowired
    private ICollectorMessageHandler[] allMessageHandlers;

    //hold each of the collector handlers (key=PATH)
    private Map<String, IMessageTypeHandler> _collectorMessageHandlersByPath = new HashMap<String, IMessageTypeHandler>();
    //hold each of the collector handlers (key=MESSAGE_TYPE)
    private Map<String, IMessageTypeHandler> _collectorMessageHandlerByType = new HashMap<String, IMessageTypeHandler>();

    private Socket _socket;
    private ScheduledExecutorService _autoStartScheduler = Executors.newScheduledThreadPool(1);

    // **********************************
    // mrsoa integration methods
    // **********************************

    // if you don't keep a reference around, it will be garbage collected along with any level settings
    private static java.util.logging.Logger julRootLogger;

    private void configureJulLogging() {
        //LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        julRootLogger = java.util.logging.Logger.getLogger("");
        julRootLogger.setLevel(Level.FINEST);
    }

    @PostConstruct
    public void onPostConstruct()
    {
        ServerPropertyHolder.addPropertyChangeListener(
            "sm\\.",
            (properties) -> {
                properties.forEach(change -> {
                    if ("sm.signup.bonus".equals(change.key)) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format("sm.signup.bonus changed from {0} to {1}", change.oldValue, change.newValue));
                        }
                        _signupBonus = Double.parseDouble(change.newValue);
                    }
                });
            }
        );
    }

    @Override
    @OnStart
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void start()
    {
        // used by socket.io library
        configureJulLogging();

        if (_logger.isDebugEnabled()) {
            _logger.debug("SnowyowlService starting...");
        }

        ServiceEndpoint serviceEndpoint = new ServiceEndpoint(SERVICE_NAMESPACE, SERVICE_NAME, SERVICE_VERSION);
        registerMessageHandlers(serviceEndpoint, allMessageHandlers);

        publishOpenGames();
        publishCanSeeContentWithoutLoginDoc();

        //do notification service registrations
        _logger.debug("waiting for the notification service to load..");
        ServiceMessage.waitForServiceRegistration(new ServiceEndpoint("meinc-service", "NotificationService", "1.0"));
        _logger.debug("registering with notification service for notification types...");

        //_notificationService.addNotificationType(xxx);
        //_notificationService.setNotificationLimit(xxx);
        //_notificationService.setNotificationSound(xxx);
        //_notificationService.setNotificationCategory(xxx);

        _notificationService.addNotificationType(NOTIFICATION_TYPE_NOTIFY_ON_ROUND_START, NOTIFICATION_PREF_TYPE_ROUND_START);
        _notificationService.setNotificationCategory(ISnowyowlService.NOTIFICATION_TYPE_NOTIFY_ON_ROUND_START, "gameplay");

        //do identity service registrations
        _logger.debug("waiting for the identity service to load..");
        ServiceMessage.waitForServiceRegistration(new ServiceEndpoint("meinc-service", "IdentityService", "1.0"));
        _logger.debug("registering callbacks with identity service for new signups...");

        _identityService.registerSignupCallback(serviceEndpoint, "subscriberSignupCallback");

        //wait for the trigger service to startup and register for callbacks
        ServiceEndpoint triggerServiceEndpoint = new ServiceEndpoint("meinc-service", "TriggerService", "1.0");
        ServiceMessage.waitForServiceRegistration(triggerServiceEndpoint);
        _triggerService.registerCallback(serviceEndpoint, "processTriggerMessages", ISnowyowlService.TRIGGER_SERVICE_ROUTE);

        if (_engineRunner) {
            //setup socket.io

            //this works for now since there's just 1 server. but in the future, this will need to expand
            String[] socketIoIps = _socketIoIpsUnparsed.split(",");
            String socketIoIp = "http://" + socketIoIps[0];

            try {
                _socket = IO.socket(socketIoIp);
            } catch (URISyntaxException e) {
                _logger.error("unable to create socket.io connection to: " + socketIoIp, e);
            }

            _socket
            .on(Socket.EVENT_CONNECTING, args -> {
                _logger.info("SOCKETIO: connecting");
            })
            .on(Socket.EVENT_CONNECT, args -> {
                _logger.info("SOCKETIO: connected");
                SocketIoLogger.log(_triggerService, null, "set_as_controller", null, "SENDING");
                _socket.emit("set_as_controller");
                SocketIoLogger.log(_triggerService, null, "set_as_controller", null, "SENT");

                //don't start the engines until socket.io is ready
                _engineCoordinator.start(_socket);

                //see if there are any games that need to be auto started
                BaseSmMessageHandler.wrapInTransaction(_transactionManager, SnowyowlService.this::checkForAutoStartGamesInTransaction, null);
            })
            .on(Socket.EVENT_CONNECT_ERROR, args -> {
                if (args.length > 0 && args[0] instanceof Exception) {
                    _logger.error("SOCKETIO: connection error", (Exception)args[0]);
                } else {
                    _logger.error("SOCKETIO: connection error");
                }
            })
            .on(Socket.EVENT_DISCONNECT, args -> {
                _logger.info("SOCKETIO: disconnected");
            });

            _socket.connect();
        }

        _gameHandler.setSocketIoSocket(_socket);
        _adminHandler.setSocketIoSocket(_socket);
        _subscriberHandler.setSocketIoSocket(_socket);

        _logFileAnalyzer.start();

        //parse out the list of socketio subscriber ids to monitor
        if (!StringUtil.isEmpty(_socketIoMonitoredSubscriberIdCommaDelimitedString)) {
            _socketIoMonitoredSubscriberIds = Arrays.asList(_socketIoMonitoredSubscriberIdCommaDelimitedString.split(",")).stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        } else {
            _socketIoMonitoredSubscriberIds = new HashSet<>();
        }

        //start the message bus and register all the handlers
        _messageBus.start();
        Arrays.asList(_allMessageProcessors).forEach(messageProcessor -> {
            messageProcessor.init(_socket);
            _messageBus.register(messageProcessor);
        });

        _logger.info("SnowyowlService started");
    }

    @Override
    @OnStop
    @ServiceMethod
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

        if (_engineRunner) {
            _engineCoordinator.stop();

            //disconnect from socket.io
            if (_socket != null && _socket.connected()) {
                _socket.disconnect();
            }
        }

        _logFileAnalyzer.stop();
        _autoStartScheduler.shutdown();

        //unregister all the message processors and stop the message bus
        Arrays.asList(_allMessageProcessors).forEach(messageProcessor -> {
            _messageBus.unregister(messageProcessor);
        });
        _messageBus.stop();

        if (_logger.isDebugEnabled()) {
            _logger.debug("ShoutContestService stopped");
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public boolean processTriggerMessages(Trigger trigger)
    {
        //ensure the message is intended for this service
        if (trigger == null || trigger.getPayload() == null || !isMyTrigger(trigger.getKey())) {
            return true; //meaning pass it along because it's been handled
        }

        switch (trigger.getKey())
        {
            case ISnowyowlService.TRIGGER_KEY_SOCKET_IO_MESSAGE:
                handleTriggerSocketIoMessage(trigger);
                break;

            case ISnowyowlService.TRIGGER_KEY_SUBSCRIBER_STATS_MESSAGE:
                _subscriberStatsHandler.handleTriggerMessage(trigger);
                break;
        }

        return false; //meaning don't pass it along because it's been handled
    }

    private boolean isMyTrigger(String triggerKey)
    {
        return _validTriggerKeys.contains(triggerKey);
    }

    private void handleTriggerSocketIoMessage(Trigger trigger)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) trigger.getPayload();
        int subscriberId = (int) payload.get("subscriberId");

        //filter out any subscriber that's not actively being monitored
        if (!_socketIoMonitoredSubscriberIds.contains(subscriberId)) return;

        Date sentDate = (Date) payload.get("sentDate");
        String message = (String) payload.get("message");
        String messageType = (String) payload.get("messageType");
        String status = (String) payload.get("status");

        if (_logger.isDebugEnabled()) {
            _logger.debug(MessageFormat.format(
                "SOCKET.io LOG; subscriberId: {0}, type: {1}, date: {2}, status: {3}, payload: ...", subscriberId,
                messageType, sentDate, status));
        }

        //store in the database
        _dao.addSocketIoLog(subscriberId, messageType, message, status, sentDate);
    }

    private void registerMessageHandlers(ServiceEndpoint serviceEndpoint, IMessageTypeHandler ... handlers)
    {
        Arrays.asList(handlers).forEach(
            (messageTypeHandler) -> {
                _collectorMessageHandlerByType.put(messageTypeHandler.getHandlerMessageType(), messageTypeHandler);

                for (int i=0; i < messageTypeHandler.getCollectorEndpoints().length; i++) {
                    _collectorMessageHandlersByPath.put(messageTypeHandler.getCollectorEndpoints()[i].getPath(), messageTypeHandler);
                    _webCollectorService.registerMessageTypeHandler(messageTypeHandler.getCollectorEndpoints()[i], messageTypeHandler.getHandlerMessageType(), serviceEndpoint);
                }
            }
        );
    }

    //publish the games.json and each of the associated game.json documents
    private void publishOpenGames()
    {
        if (!_docProducer) return;
        _logger.info("publishing <wds>" + ISnowyowlService.GAMES_LIST_NAME);

        List<String> gameIds = new ArrayList<>();
        gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.DEFAULT, ISnowyowlService.GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));
        gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.TESTER, ISnowyowlService.TEST_GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));

        for (String gameIdToPublish : gameIds) {
            publishGameWithExtrasToWds(gameIdToPublish, _dao, _shoutContestService, _gameStatsHandler);
        }
    }

    //publish the doc that states whether or not players can see game content without being logged in
    private void publishCanSeeContentWithoutLoginDoc()
    {
        boolean canSeeContentWithoutLogin = true;

        //rather than store this in the db, i've opted to just make it a quick config value on the hard drive
        File configFile = new File(_stateDir, "canSeeContentWithoutLogin.dat");
        if (configFile.exists()) {
            try {
                canSeeContentWithoutLogin = (Boolean) readFromFile(configFile);
            } catch (ClassNotFoundException | IOException e) {
                _logger.warn("unable to read config val fron canSeeContentWithoutLogin.dat", e);
            }
        }

        publishJsonWdsDoc(_logger, new WebDataStoreServiceClientProxy(false), null, "/canSeeContentWithoutLogin.json", new FastMap<>("canSeeContentWithoutLogin", canSeeContentWithoutLogin));
    }

    // **********************************
    // service methods
    // **********************************

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<QuestionCategory> getQuestionCategoriesFromCategoryIds(List<String> categoryUuids)
    {
        List<QuestionCategory> questionCategories = new ArrayList<>(categoryUuids.size());
        for (String categoryUuid : categoryUuids) {
            questionCategories.add(QuestionCategoryHelper.getQuestionCategoryById(categoryUuid, _dao));
        }

        return questionCategories;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void subscriberSignupCallback(long subscriberId)
    {
        if (_signupBonus > 0D) {
            if (_logger.isDebugEnabled()) {
                _logger.debug(MessageFormat.format("giving subscriber {0,number,#} a ${1} signup bonus", subscriberId, _signupBonus));
            }
            _shoutContestService.addCashPoolTransaction(
                    subscriberId, _signupBonus, CashPoolTransaction2.TYPE.BONUS, "signup bonus", null, null);
        }

        //add to the action log
        _dao.addSubscriberActionLog(subscriberId, "ACCOUNT_CREATED", null, null);

        //see if there's an affiliate id that needs to be set
        AffiliatePlan currentAffiliatePlan = _dao.getCurrentAffiliatePlan();
        if (currentAffiliatePlan != null) {
            _subscriberStatsHandler.incrementSubscriberStat(subscriberId, SubscriberStats.STATS_TYPE.AFFILIATE_PLAN_ID, currentAffiliatePlan.getAffiliatePlanId());
        }

    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void checkForAutoStartGames()
    {
        checkForAutoStartGamesInTransaction(null);
    }


    // **********************************
    // webcollector integration methods
    // **********************************

    /** INTERNAL METHOD. No public access. */
    @Override
    @ServiceMethod
    public CollectorMessageResult createMessage(String requestPath, Map<String, String> requestHeaders, Map<String, String> requestParameters)
    throws BadRequestException
    {
        return Optional
            .ofNullable(_collectorMessageHandlersByPath.get(requestPath))
            .orElseThrow(() -> {
                _logger.warn("received createMessage for unregistered path type: " + requestPath);
                return new BadRequestException(); })
            .createMessage(requestPath, requestHeaders, requestParameters);
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
    @Override
    @ServiceMethod
    public void handleMessages(List<CollectorMessage> messages, IWebCollectorMessageBuffer messageBuffer)
    throws BadRequestException
    {
        Optional
            .ofNullable(_collectorMessageHandlerByType.get(messages.get(0).getMessageType()))
            .orElseThrow(() -> {
                _logger.warn("received createMessage for unregistered type: " + messages.get(0).getMessageType());
                return new BadRequestException(); })
            .handleMessages(messages, messageBuffer);
    }

    /** INTERNAL METHOD. No public access. */
    @Override
    @ServiceMethod
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException
    {
        return Optional
            .ofNullable(_collectorMessageHandlersByPath.get(request.getPath()))
            .orElseThrow(() -> {
                _logger.warn("received handleSyncRequest for unregistered path: " + request.getPath());
                return new BadRequestException(); })
            .handleSyncRequest(request);
    }

    //run every 10 minutes
    //note: to manage email aliases: https://admin.google.com - go to "groups"
    @Override
    @ServiceMethod
    @Scheduled(fixedRate = 600_000L)
    public void sendDailyStatusEmail()
    {
//_logger.info("sendDailyStatusEmail::running");
        //only have one sever do this
        if (!_engineRunner) return;

        //only run between midnight at 1am
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
//_logger.info("sendDailyStatusEmail::hour: " + hour);
        if (hour != 0) return;

        //see when the email was last sent.
        Calendar then = null;
        File configFile = new File(_stateDir, "dailyemailtimestamp.dat");
        if (configFile.exists()) {
            try {
                then = (Calendar) readFromFile(configFile);
            } catch (ClassNotFoundException | IOException e) {
                _logger.warn("unable to read from dailyemailtimestamp.dat", e);
            }
        }
//_logger.info("sendDailyStatusEmail::then: " + then);

        //don't send more than once per 24hrs
        if (then != null) {
            long hours = ChronoUnit.HOURS.between(then.toInstant(), now.toInstant());
//_logger.info("sendDailyStatusEmail::hours since last send: " + hours);
            if (hours < 23) return;
        } else {
            then = Calendar.getInstance();
            then.add(Calendar.HOUR, -24); //go back one day
//_logger.info("sendDailyStatusEmail::never sent before, so going back 1 day");
        }

        //get the data
//_logger.info("sendDailyStatusEmail::about to grab data...");
        @SuppressWarnings("unchecked")
        List<ReportStructAffiliatePayout> data = (List<ReportStructAffiliatePayout>)
            BaseSmMessageHandler.wrapInTransaction(_transactionManager, this::sendDailyStatusEmailDao, then.getTime());
//_logger.info("sendDailyStatusEmail::data has been grabbed");

        //sort by winnings, high to low and convert to csv format
        StringBuilder buf = new StringBuilder();
        buf.append("email,nickname,network size,winnings\r\n");
        data.stream()
            .sorted( Comparator.comparing(ReportStructAffiliatePayout::getWinnings, Comparator.nullsLast(Comparator.reverseOrder())) )
            .forEach(s -> {
                buf.append(s.toCsv()).append("\r\n");
            });
//_logger.info("sendDailyStatusEmail::data:\n" + buf);

        String attachmentFilename = MessageFormat.format("{0}_affiliates_{1,date,yyyy-MM-dd hh:mm:ss.SSS}.csv", _servername, new Date());
//_logger.info("sendDailyStatusEmail::attachment filename: " + attachmentFilename);

        //send the email
        EmailAddress toEmail = new EmailAddress("snowyowlaffiliategroup@shout.tv", "");
        EmailAddress fromEmail = new EmailAddress("support@shout.tv", "");
        String emailSubject = _appDisplayName + " - Daily Affiliate Payouts Report";
        String emailMessage = MessageFormat.format("Daily Affiliate Payout: {0} at {1,date,yyyy-MM-dd hh:mm:ss.SSS}", _servername, new Date());

//_logger.info("sendDailyStatusEmail::about to send email");
        sendEmail(_logger, toEmail, _identityService, _postOfficeService, fromEmail, emailSubject, emailMessage, buf.toString(), attachmentFilename);
//_logger.info("sendDailyStatusEmail::email sent");

        //update the last sent time
        try {
//_logger.info("sendDailyStatusEmail::about to update last sent timestamp");
            writeToFile(configFile, Calendar.getInstance());
//_logger.info("sendDailyStatusEmail::updated last sent timestamp");
        } catch (IOException e) {
            _logger.warn("unable to write to dailyemailtimestamp.dat", e);
        }
    }

    private List<ReportStructAffiliatePayout> sendDailyStatusEmailDao(Object param)
    {
        Date fromDate = (Date) param;

        Map<Long, ReportStructAffiliatePayout> map = new HashMap<>();

        //get the winnings for each subscriber since the given date
        List<ReportStructAffiliatePayoutSubscriberWinnings> winningsBySubscriber = _dao.getReportStructAffiliatePayoutSubscriberWinnings(fromDate);
        winningsBySubscriber.forEach(w -> {
            ReportStructAffiliatePayout struct = new ReportStructAffiliatePayout();
            struct.setWinnings(w.total);
            map.put(w.subscriberId, struct);
        });

        //get the network size for each of those subscribers
        String subscriberIdsAsCommaDelimitedList = map.keySet().stream().map(i->i.toString()).collect(Collectors.joining(","));
        List<ReportStructAffiliatePayoutSubscriberNetworkSize> networkSizeBySubscriber = _dao.getReportStructAffiliatePayoutSubscriberNetworkSize(subscriberIdsAsCommaDelimitedList);
        networkSizeBySubscriber.stream().forEach( s -> {
            ReportStructAffiliatePayout struct = map.get(s.subscriberId);
            struct.setNetworkSize(s.size);
        });

        //get the nickname/email of each of those subscribers
        List<ReportStructAffiliatePayoutSubscriberInfo> info = _dao.getReportStructAffiliatePayoutSubscriberInfo(subscriberIdsAsCommaDelimitedList);
        info.stream().forEach(s -> {
            ReportStructAffiliatePayout struct = map.get(s.subscriberId);
            struct.setEmail(s.email);
            struct.setNickname(s.nickname);
        });

        return map.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
    }

    private Void checkForAutoStartGamesInTransaction(Object param)
    {
_logger.info("\tchecking for auto start games...");
        //grab all the upcoming/ongoing games
        List<Game> pendingGames = _shoutContestService.getGamesByStatusAndEngine(ISnowyowlService.GAME_ENGINE, Game.GAME_STATUS.PENDING, Game.GAME_STATUS.OPEN);
        if (pendingGames == null || pendingGames.size() == 0) {
_logger.info("\tno upcoming games");
            return null;
        }

        //check for game that need POOL autostart
        List<Game> autoStartPoolGames = new ArrayList<>();
        for (Game g : pendingGames) {
            if (!g.isAutoStartPoolPlay()) continue;

            if (g.getGameStatus() == Game.GAME_STATUS.PENDING) {
_logger.info("\tautostart POOL game (game not open): " + g.getGameName("en"));
                autoStartPoolGames.add(g);

            } else if (g.getGameStatus() == Game.GAME_STATUS.OPEN) {
                Round firstPoolRound = _commonBusinessLogic.getFirstRoundOfGame(g.getId());
                if (firstPoolRound.getOpenDate() == null) {
_logger.info("\tautostart POOL game (game open but pool not started): " + g.getGameName("en"));
                    autoStartPoolGames.add(g);
                }
            }
        }

        //check for game that need BRACKET autostart
        List<Game> autoStartBracketGames = pendingGames.stream()
                .filter(g -> g.isAutoStartBracketPlay())
.peek(g -> _logger.info("\tautostart BRACKET game: " + g.getGameName("en")))
                .collect(Collectors.toList());

        Date now = new Date();
        Date nearestFutureStartDate = null;

        //check the start times of the auto start pool games
        for (Game poolGame : autoStartPoolGames) {
            Round firstPoolRound = _commonBusinessLogic.getFirstRoundOfGame(poolGame.getId());
            Date poolStartTime = firstPoolRound.getExpectedOpenDate();
            if (poolStartTime == null) continue;

            //if auto start date is <=NOW(), start immediately
            if (!poolStartTime.after(now)) {
_logger.info("\ttime to auto start pool play for game: " + poolGame.getGameName("en"));
                //make sure game is open
                if (poolGame.getOpenDate() == null) {
                    _commonBusinessLogic.openGame(poolGame, _wdsService);
                }

                //start pool play if not already started
                if (firstPoolRound.getOpenDate() == null) {
                    try {
                        _commonBusinessLogic.startPoolPlay(poolGame, null);
                    } catch (NoQuestionsPossibleException e1) {
                        _logger.warn(MessageFormat.format(
                            "attempted to auto start pool play for game {0}, but round {1} has no questions possible. turning off auto start",
                            poolGame.getId(), e1.getRoundId()));

                        //turn off auto start
                        poolGame.setAutoStartPoolPlay(false);
                        poolGame.setAutoStartBracketPlay(false);
                        _shoutContestService.updateGameThin(poolGame);

                        continue;
                    }
                }

            } else {
                //compare with nearestFutureStartDate. if it's before that, set it as the new nearestFutureStartDate
                if (nearestFutureStartDate == null || poolStartTime.before(nearestFutureStartDate)) {
                    nearestFutureStartDate = poolStartTime;
_logger.info("\tset next autostart timer to be at: " + nearestFutureStartDate);
                }
            }
        }

        //check the start times of the auto start bracket games
        for (Game bracketGame : autoStartBracketGames) {
_logger.info("\texamining autostart bracket game: " + bracketGame.getGameName("en"));
            Round firstBracketRound = _commonBusinessLogic.getFirstBracketRoundOfGame(bracketGame.getId());
            Date bracketStartTime = firstBracketRound.getExpectedOpenDate();
_logger.info("\tbracket starts at: " + bracketStartTime);
            if (bracketStartTime == null) continue;

            long preNotificationOffset = bracketGame.getAutoBracketPlayPreStartNotificationTimeMs();
_logger.info("\tprenotification offset (ms): " + preNotificationOffset);
            Date adjustedBracketStartTime = new Date(bracketStartTime.getTime() - preNotificationOffset);
_logger.info("\tadjusted bracket start time: " + adjustedBracketStartTime);

            //if auto start date is <=NOW(), start immediately
            if (!adjustedBracketStartTime.after(now)) {
_logger.info("\ttime to auto start bracket play for game: " + bracketGame.getGameName("en"));
                if (bracketGame.getOpenDate() == null) {
                    _commonBusinessLogic.openGame(bracketGame, _wdsService);
                }

                //make sure pool play has been started (if there are any pool rounds)
                Round firstPoolRound = _commonBusinessLogic.getFirstRoundOfGame(bracketGame.getId());
                if (firstPoolRound != null && firstPoolRound.getOpenDate() == null) {
                    try {
                        _commonBusinessLogic.startPoolPlay(bracketGame, null);
                    } catch (NoQuestionsPossibleException e1) {
                        _logger.warn(MessageFormat.format(
                            "attempted to auto start pool play for game {0}, but round {1} has no questions possible. turning off auto start",
                            bracketGame.getId(), e1.getRoundId()));

                        //turn off auto start
                        bracketGame.setAutoStartPoolPlay(false);
                        bracketGame.setAutoStartBracketPlay(false);
                        _shoutContestService.updateGameThin(bracketGame);

                        continue;
                    }
                }

                //start bracket play
                List<Round> rounds;
                try {
                    rounds = _commonBusinessLogic.startBracketPlay(bracketGame, null, _wdsService, preNotificationOffset);
                } catch (PoolPlayMatchesInProgressException e) {
                    //in this case, set the timer for one minute and try again
                    if (nearestFutureStartDate == null || nearestFutureStartDate.getTime() > (now.getTime() + 60_000L)) {
                        nearestFutureStartDate = new Date(now.getTime() + 60_000L);
                    }
                    continue;
                }

                new Thread() {
                    @Override
                    public void run()
                    {
                        try {
                            _commonBusinessLogic.completeBracketPlayStart(bracketGame, rounds, firstBracketRound, preNotificationOffset, now.getTime(), _wdsService, _socket);
                        } catch (Exception e) {
                            _logger.error(e.getMessage(), e);
                        }
                    }
                }.start();

            } else {
                //compare with nearestFutureStartDate. if it's before that, set it as the new nearestFutureStartDate
                if (nearestFutureStartDate == null || adjustedBracketStartTime.before(nearestFutureStartDate)) {
                    nearestFutureStartDate = adjustedBracketStartTime;
_logger.info("\tset next autostart timer to be at: " + nearestFutureStartDate);
                }
            }
        }

        //set a timer for when this method should run next
        if (nearestFutureStartDate != null) {
            long delay = nearestFutureStartDate.getTime() - System.currentTimeMillis();
_logger.info("\tsetting auto start timer for: " + delay + " ms" + ": " + new Date(System.currentTimeMillis() + delay));
            _autoStartScheduler.schedule(
                    ()->checkForAutoStartGamesInTransaction(null),
                    delay, TimeUnit.MILLISECONDS);
        }

        return null;
    }

}

