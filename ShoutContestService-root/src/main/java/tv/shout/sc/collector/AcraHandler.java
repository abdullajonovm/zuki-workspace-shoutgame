package tv.shout.sc.collector;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meinc.identity.domain.Subscriber;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.HandlerStyle;

import tv.shout.collector.BaseMessageHandler;
import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.sc.dao.IContestDaoMapper;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.util.DateUtil;
import tv.shout.util.FastMap;
import tv.shout.util.StringUtil;

public class AcraHandler
extends BaseMessageHandler
{
    private static Logger _logger = Logger.getLogger(AcraHandler.class);

    // Valid form post param names
    //https://github.com/ACRA/acra/wiki/ReportContent
    private static final List<String> validFormVars = Arrays.asList(
        "REPORT_ID", "APP_VERSION_CODE", "APP_VERSION_NAME", "PACKAGE_NAME",
        "PHONE_MODEL", "BRAND", "PRODUCT", "ANDROID_VERSION", "BUILD",
        "TOTAL_MEM_SIZE", "AVAILABLE_MEM_SIZE",
        "CUSTOM_DATA", "STACK_TRACE", "INITIAL_CONFIGURATION", "CRASH_CONFIGURATION",
        "DISPLAY", "USER_APP_START_DATE", "USER_CRASH_DATE", "LOGCAT", "USER_EMAIL"
    );

    @Autowired
    private IContestDaoMapper _dao;

    @Autowired
    private IShoutContestService _shoutContestService;

    @Autowired
    private PlatformTransactionManager _transactionManager;

    @Override
    public String getHandlerMessageType()
    {
        return "ACRA";
    }

    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        CollectorEndpointHandler[] handlers = new CollectorEndpointHandler[] {
            new CollectorEndpointHandler(new CollectorEndpoint("/acra/report2", ConnectionType.ANY, HandlerStyle.SYNC_REQUEST))
            .withValidRoles(new HashSet<>(Arrays.asList()))
            .withSyncRequestHandlerFunction(
                (request, logMessageTag) ->
                handleAcraReport2(request)),

            new CollectorEndpointHandler(new CollectorEndpoint("/acra/report", ConnectionType.ANY))
            .withValidRoles(new HashSet<>(Arrays.asList()))
            .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    handleAcraReport(message.getProperties(), message.getMessageId())),
        };

        for (CollectorEndpointHandler handler : handlers) {
            _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
        }

        return Arrays.stream(handlers)
            .map(CollectorEndpointHandler::getCollectorEndpoint)
            .toArray(CollectorEndpoint[]::new);
    }

    @Override
    public CollectorMessageResult createMessage(String requestPath, Map<String, String> requestHeaders, Map<String, String> requestParameters)
    throws BadRequestException
    {
        logCreateMessage();

        CollectorMessage m = new CollectorMessage();
        m.setMessageType(getHandlerMessageType());

        //there is no toWDS in the case of ACRA
        //Map<String, String> props = createProps(requestPath, requestHeaders, requestParameters);
        Map<String, String> props = new HashMap<>();
        props.put("__requestPath", requestPath);
        for(String header : requestHeaders.keySet()) {
            props.put("HEADER_" + header, requestHeaders.get(header));
        }

        // Add all allowed params to the collector message
        requestParameters.entrySet().stream()
            .filter(map -> validFormVars.contains(map.getKey()))
            .forEach(map -> props.put(map.getKey(), map.getValue()));

        m.setProperties(props);
        CollectorMessageResult result = new CollectorMessageResult(m);
        return result;
    }

    @Override
    public void handleMessage(CollectorMessage message)
    throws PublishResponseError, BadRequestException
    {
        Map<String, String> props = message.getProperties();
        String requestPath = props.get("__requestPath");
        _logger.info("processing " + getHandlerMessageType() + " message: " + requestPath);

        //String toWds = props.get(PARM_TO_WDS);

        CollectorEndpointHandler collectorEndpointHandler = Optional
                .ofNullable(_collectorEndpointHandlerByPath.get(requestPath))
                .orElseThrow(BadRequestException::new);

        String logMessageTag = getLogMessageTag(requestPath);

        /*Map<String,Object> extraResponseParms = */collectorEndpointHandler.getMessageHandlerFunction().apply(message, logMessageTag);

        //for this, don't publish a response
        //publishResponseWdsDoc(toWds, message.getMessageId(), logMessageTag, true, null, null, extraResponseParms);
    }

    @Override
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException
    {
        _logger.info("processing sync request: " + request.getPath());

        String logMessageTag = getLogMessageTag(request.getPath());

        CollectorEndpointHandler collectorEndpointHandler = Optional
            .ofNullable(_collectorEndpointHandlerByPath.get(request.getPath()))
            .orElseThrow(BadRequestException::new);

        return collectorEndpointHandler.getSyncRequestHandlerFunction().apply(request, logMessageTag);
    }

    private Map<String, Object> handleAcraReport(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //CUSTOM_DATA : appId = snowyowl
            Integer contextId = null;
            String CUSTOM_DATA = props.get("CUSTOM_DATA");
            if (!StringUtil.isEmpty(CUSTOM_DATA)) {
                String[] args = CUSTOM_DATA.split(" = ");
                if (args[0].equals("appId")) {
                    Map<String, String> p = new FastMap<>("appId", args[1].trim());
                    contextId = _shoutContestService.getContextId(p);
                }
            }

            String USER_EMAIL = props.get("USER_EMAIL");
            Long subscriberId = null;
            if (!StringUtil.isEmpty(USER_EMAIL) && contextId != null) {
                Subscriber s = _identityService.getSubscriberByEmail(contextId, USER_EMAIL);
                if (s != null) {
                    //s could be null if this is happening during a signup
                    subscriberId = s.getSubscriberId();
                }
            }

            Date USER_APP_START_DATE = DateUtil.iso8601ToDate(props.get("USER_APP_START_DATE"));
            Date USER_CRASH_DATE = DateUtil.iso8601ToDate(props.get("USER_CRASH_DATE"));

            Map<String, Object> insertParams = new HashMap<>();
            insertParams.put("REPORT_ID", props.get("REPORT_ID"));
            try {
                insertParams.put("APP_VERSION_CODE", Integer.parseInt(props.get("APP_VERSION_CODE")));
            } catch (NumberFormatException e) {
                insertParams.put("APP_VERSION_CODE", 0);
            }
            insertParams.put("APP_VERSION_NAME", props.get("APP_VERSION_NAME"));
            insertParams.put("PACKAGE_NAME", props.get("PACKAGE_NAME"));
            insertParams.put("PHONE_MODEL", props.get("PHONE_MODEL"));
            insertParams.put("BRAND", props.get("BRAND"));
            insertParams.put("PRODUCT", props.get("PRODUCT"));
            insertParams.put("ANDROID_VERSION", props.get("ANDROID_VERSION"));
            insertParams.put("BUILD", props.get("BUILD"));
            try {
                insertParams.put("TOTAL_MEM_SIZE", Long.parseLong(props.get("TOTAL_MEM_SIZE")));
            } catch (NumberFormatException e) {
                insertParams.put("TOTAL_MEM_SIZE", 0L);
            }
            try {
                insertParams.put("AVAILABLE_MEM_SIZE", Long.parseLong(props.get("AVAILABLE_MEM_SIZE")));
            } catch (NumberFormatException e) {
                insertParams.put("AVAILABLE_MEM_SIZE", 0L);
            }
            insertParams.put("STACK_TRACE", props.get("STACK_TRACE"));
            insertParams.put("INITIAL_CONFIGURATION", props.get("INITIAL_CONFIGURATION"));
            insertParams.put("CRASH_CONFIGURATION", props.get("CRASH_CONFIGURATION"));
            insertParams.put("DISPLAY", props.get("DISPLAY"));
            insertParams.put("LOGCAT", props.get("LOGCAT"));
            insertParams.put("USER_EMAIL", props.get("USER_EMAIL"));
            insertParams.put("contextId", contextId);
            insertParams.put("subscriberId", subscriberId);
            insertParams.put("userAppStartDate", USER_APP_START_DATE);
            insertParams.put("userCrashDate", USER_CRASH_DATE);

            _dao.insertAcraReport(insertParams);

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

    private HttpResponse handleAcraReport2(HttpRequest request)
    {
//        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
//        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
//        try {
//            _logger.debug("ACRA KEY/VALUE pairs:");
//            MultiMap<String, String> params = request.getParameters();
//            params.keySet().forEach(key -> {
//                String value = params.get(key).get(0);
//                if (value == null) value = "[ NULL ]";
//                if (value.length() > 80) value = value.substring(0, 80) + " ...";
//                _logger.debug("\t" + key + " : " + value);
//            });
//
//            _transactionManager.commit(txStatus);
//            txStatus = null;
//        } finally {
//            if (txStatus != null) {
//                _transactionManager.rollback(txStatus);
//                txStatus = null;
//            }
//        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);

        HttpResponse response = new HttpResponse();
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        try {
            writer.print(_jsonMapper.writeValueAsString(responseMap));
        } catch (JsonProcessingException e) {
            _logger.error("unable to write response json", e);
        }
        writer.flush();

        return response;
    }

}
