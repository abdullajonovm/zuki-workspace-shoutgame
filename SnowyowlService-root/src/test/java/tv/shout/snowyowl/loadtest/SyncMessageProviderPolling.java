package tv.shout.snowyowl.loadtest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import tv.shout.sm.test.CollectorToWdsResponse;
import tv.shout.sm.test.CollectorToWdsResponse.DataReceiver;
import tv.shout.sm.test.CollectorToWdsResponse.ERROR_TYPE;
import tv.shout.sm.test.CollectorToWdsResponse.REQUEST_TYPE;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.util.JsonUtil;

class SyncMessageProviderPolling
implements SyncMessageProvider
{
    private static final long POLL_PERIOD_MS = 2_000L;
    private static Logger _logger = Logger.getLogger(SyncMessageProviderPolling.class);
    private static ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();

    private ScheduledExecutorService _executor = Executors.newScheduledThreadPool(1);
    private Date _highWaterMark;
    private SyncMessageReceiver _receiver;
    private CollectorToWdsResponse _collector;
    private Map<String, String> _authHeaders;
    private String _gameId;
    private String _nickname;

    SyncMessageProviderPolling(
        final SyncMessageReceiver receiver, final CollectorToWdsResponse collector, Date highWaterMark, final Map<String, String> authHeaders,
        final String nickname)
    {
        if (receiver == null) throw new IllegalArgumentException("receiver is null");
        if (collector == null) throw new IllegalArgumentException("collector is null");
        if (authHeaders == null) throw new IllegalArgumentException("authHeaders is null");
        if (nickname == null) throw new IllegalArgumentException("nickname is null");

        _receiver = receiver;
        _collector = collector;
        _highWaterMark = highWaterMark;
        _authHeaders = authHeaders;
        _nickname = nickname;

        _receiver.syncMessageReceiverReady();
    }

    @Override
    public void start(String gameId)
    {
        _gameId = gameId;

        //just so everything doesn't start at exactly the same time, give a little random wiggle room
        long waitTime = new Random().nextInt(5_000);

        _executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run()
            {
//if (_nickname.equals("__player_5")) _logger.info(MessageFormat.format("{0} about to retrieve sync messages using highwatermark: {1,date,yyyy-MM-dd hh:mm:ss.SSS}", _nickname, _highWaterMark));
                _collector.getSyncMessages(new DataReceiver() {
                    @Override
                    public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
                    {
                        try {
                            //convert to a list
                            ArrayNode jsonList = (ArrayNode) json.get("syncMessages");
                            String jsonListAsStr = _jsonMapper.writeValueAsString(jsonList);
                            List<SyncMessage> syncMessages = _jsonMapper.readValue(jsonListAsStr, new TypeReference<List<SyncMessage>>() {});

                            //sort, find first > highwatermark
                            Optional<SyncMessage> oSyncMessage = syncMessages.stream()
                                    .filter(sm -> sm.getCreateDate().after(_highWaterMark))
                                    .sorted((f1, f2) -> Long.compare(f1.getCreateDate().getTime(), f2.getCreateDate().getTime()))
                                    .findFirst();
                            if (oSyncMessage.isPresent()) {
//                                _logger.info(MessageFormat.format(
//                                        "POLLING: {0} received sync message: {1} ({2,date,yyyy-MM-dd hh:mm:ss.SSS})",
//                                        _nickname, oSyncMessage.get().getMessageType(), oSyncMessage.get().getCreateDate()));
                                _highWaterMark = oSyncMessage.get().getCreateDate();
                                _receiver.syncMessageReceived(oSyncMessage.get());
                            } else {
//if (_nickname.equals("__player_5")) _logger.info(MessageFormat.format("{0} had no new sync messages", _nickname));
                            }

                        } catch (IOException e) {
                            _logger.error(MessageFormat.format("{0} uable to parse sync messages json response", _nickname), e);
                            _receiver.syncMessageReceiverError();
                        }
                    }

                    @Override
                    public void dataCallbackFailure(REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode,
                            String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
                    {
                        _logger.error(MessageFormat.format("{0} network error. error: {1}, responseCode: {2}, msg: {3}", _nickname, errorType, httpResponseCode, responseMessage));
                        _receiver.syncMessageReceiverError();
                    }
                }, _authHeaders, _gameId, _highWaterMark);
            }
        }, waitTime, POLL_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop()
    {
        _executor.shutdown();
    }

}
