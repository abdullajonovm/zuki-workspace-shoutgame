package tv.shout.snowyowl.loadtest;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.meinc.identity.domain.Subscriber;

import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_TYPE;
import tv.shout.sm.db.DbProvider;
import tv.shout.sm.test.CollectorToWdsResponse;
import tv.shout.sm.test.CollectorToWdsResponse.DataReceiver;
import tv.shout.sm.test.CollectorToWdsResponse.ERROR_TYPE;
import tv.shout.sm.test.CollectorToWdsResponse.REQUEST_TYPE;
import tv.shout.sm.test.HttpLibrary;
import tv.shout.sm.test.NetworkException;
import tv.shout.snowyowl.domain.Question;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.util.JsonUtil;

class Player
implements SyncMessageReceiver
{
    enum ANSWER_SPEED {SLOW, MEDIUM, FAST}
    enum SKILL_LEVEL {NOVICE, AVERAGE, EXPERT}
    private enum STATE
    {
        WAITING_FOR_JOINED_OR_ABANDONED_ROUND,
        WAITING_FOR_PAIRING,
        WAITING_FOR_QUESTION,
        WAITING_FOR_QUESTION_RESULT,
        WAITING_FOR_POST_QUESTION_RESULT,
        WAITING_FOR_BRACKET_POST_MATCH_RESULT
    }

    private static Logger _logger = Logger.getLogger(Player.class);

    private double _correctAnswerPercentage;
    private Map<String, Double> _answerSpeedForPoolRounds = new HashMap<>();
    private double _answerSpeedForBracketRounds;
    private String _primaryIdHash;
    private Map<String, Round.ROUND_TYPE> _roundToRoundTypeMap;
    private String _nickname;
    private long _subscriberId;
    private ANSWER_SPEED _answerSpeed;
    private SKILL_LEVEL _skillLevel;

    private SyncMessageProvider _syncMessageProvider;
    private CountDownLatch _cdl;
    private boolean _finish;
    private CollectorToWdsResponse _collector;
    private BackdoorSqlGateway _dao;
    private Map<String, String> _authHeaders;
    private ArrayBlockingQueue<Integer> _queue = new ArrayBlockingQueue<>(1);
    private boolean _gracefulInterrupt;
    private PlayerThread _runner;
    private ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
    private STATE _currentState = STATE.WAITING_FOR_JOINED_OR_ABANDONED_ROUND;
    private long _highWaterMark;
    private List<SyncMessage> _syncMessages = new ArrayList<>();
    private Round.ROUND_TYPE _roundType;
    private String _roundId;

    private CountDownLatch _cdlStart;

    private Player() {}

    static Player create(
        DbProvider.DB which, CollectorToWdsResponse collector,
        List<Round> rounds, Map<String, Round.ROUND_TYPE> roundToRoundTypeMap, Subscriber subscriber,
        ANSWER_SPEED answerSpeed, SKILL_LEVEL skillLevel)
    {
        return null;
    }

    static Player create(
        DbProvider.DB which, CollectorToWdsResponse collector, BackdoorSqlGateway dao,
        List<Round> rounds, Map<String, Round.ROUND_TYPE> roundToRoundTypeMap, Subscriber subscriber,
        ANSWER_SPEED answerSpeed, SKILL_LEVEL skillLevel)
    {
        if (rounds == null) throw new IllegalArgumentException("rounds is null");
        if (roundToRoundTypeMap == null) throw new IllegalArgumentException("roundToRoundTypeMap is null");
        if (subscriber == null) throw new IllegalArgumentException("subscriber is null");
        if (answerSpeed == null) throw new IllegalArgumentException("answerSpeed is null");
        if (skillLevel == null) throw new IllegalArgumentException("skillLevel is null");

        Player player = new Player();

        player._roundToRoundTypeMap = roundToRoundTypeMap;
        player._answerSpeed = answerSpeed;
        player._skillLevel = skillLevel;

        //for each POOL round determine how long this player should take to answer (a range) based on the ANSWER_SPEED and the round.playerMaximumDurationSeconds
        rounds.stream()
            .filter(r -> r.getRoundType() == Round.ROUND_TYPE.POOL)
            .forEach(r -> {
                int maxTimeToAnswerS = r.getPlayerMaximumDurationSeconds();
                int min, max;

                switch (answerSpeed)
                {
                    case SLOW:
                        min = 80;
                        max = 90;
                        break;

                    case MEDIUM:
                        min = 50;
                        max = 70;
                        break;

                    case FAST:
                        min = 20;
                        max = 30;
                        break;

                    default: throw new IllegalStateException("can't get here but the compiler isn't smart enough to know that");
                }
                int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
                double timeToAnswerForPoolRound = ((double) randomNum / 100) * maxTimeToAnswerS;
                player._answerSpeedForPoolRounds.put(r.getId(), timeToAnswerForPoolRound);
            });

        //do the same speed determination for the bracket round (all dynamically added bracket rounds will have the same playerMaximumDurationSeconds)
        Round bracketRound = rounds.stream().filter(r -> r.getRoundType() == Round.ROUND_TYPE.BRACKET).findFirst().orElseThrow(() -> new IllegalStateException("no BRACKET round found in game"));
        int maxTimeToAnswerS = bracketRound.getPlayerMaximumDurationSeconds();
        int min, max;
        switch (answerSpeed)
        {
            case SLOW:
                min = 80;
                max = 90;
                break;

            case MEDIUM:
                min = 50;
                max = 70;
                break;

            case FAST:
                min = 20;
                max = 30;
                break;

            default: throw new IllegalStateException("can't get here but the compiler isn't smart enough to know that");
        }
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        double timeToAnswerForPoolRound = ((double) randomNum / 100) * maxTimeToAnswerS;
        player._answerSpeedForBracketRounds = timeToAnswerForPoolRound;
        player._subscriberId = subscriber.getSubscriberId();

        //determine how often (a range) a player should get a question correct based on the SKILL_LEVEL
        switch (skillLevel)
        {
            case NOVICE:
                min=20;
                max=30;
                break;

            case AVERAGE:
                min=50;
                max=70;
                break;

            case EXPERT:
                min=70;
                max=90;
                break;

            default: throw new IllegalStateException("can't get here but the compiler isn't smart enough to know that");
        }
        randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        player._correctAnswerPercentage = randomNum / 100D;

        //pull out necessary data from the subscriber
        player._primaryIdHash = subscriber.getEmailSha256Hash();
        player._nickname = subscriber.getNickname();

        player._collector = collector;
        player._dao = dao;

        player._authHeaders = new HashMap<>(2);
        player._authHeaders.put("X-REST-SESSION-KEY", subscriber.getSubscriberSession().getSessionKey());
        player._authHeaders.put("X-REST-DEVICE-ID", subscriber.getSubscriberSession().getDeviceId());

        return player;
    }

    void ensureBalanceCanCoverEntranceFee(CountDownLatch cdl, double costToJoin)
    {
        double balance = _dao.getSubscriberBalance(_subscriberId);
        if (balance < costToJoin) {
            String itemUuid = LoadTester.getStoreItemUuid(costToJoin, balance);
            _logger.info(_nickname + " is purchase credits...");

            _collector.storePurchaseItem(new DataReceiver() {
                @Override
                public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
                {
                    cdl.countDown();
                }

                @Override
                public void dataCallbackFailure(REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode,
                        String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
                {
                    cdl.countDown();

                    throw new IllegalStateException(MessageFormat.format(
                        "player {0} was unable to purchase credits. error: {1}, httpResponseCode: {2}",
                        _nickname, errorType, httpResponseCode));
                }
            }, _authHeaders, itemUuid);
        } else {
            cdl.countDown();
        }
    }

    void joinGame(String gameId, CountDownLatch cdlJoin)
    {
        if (gameId == null) throw new IllegalArgumentException("gameId is null");

        //join the game
        _collector.joinGame(new DataReceiver() {
            @Override
            public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
            {
                //_logger.info(MessageFormat.format("{0} joined game", _nickname));
                cdlJoin.countDown();
            }

            @Override
            public void dataCallbackFailure(
                REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode, String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
            {
                cdlJoin.countDown();
                _logger.error(MessageFormat.format("{0} failed to join game. errorType: {1}, httpResponse: {2}", _nickname, errorType, httpResponseCode));
                Player.this.stop();
            }
        }, _authHeaders, gameId);
    }

    Player start(final CountDownLatch cdl, CountDownLatch cdlStart)
    {
        if (cdl == null) throw new IllegalArgumentException("cdl is null");

        _cdl = cdl;
        _runner = new PlayerThread();
        _runner.start();

        _cdlStart = cdlStart;

        return this;
    }

    //call this to use socket.io as the sync message provider
    Player withProviderSocketIo(String socketIoUrl)
    {
        _syncMessageProvider = new SyncMessageProviderSocketIo(this, socketIoUrl, _nickname, _primaryIdHash);

        return this;
    }

    //call this to use polling as the sync message provider
    Player withProviderPolling(String gameId)
    {
        //find the most recent sync message to get the initial high water mark
        _collector.getSyncMessages(new DataReceiver() {
            @Override
            public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
            {
                try {
                    //convert to a list
                    ArrayNode jsonList = (ArrayNode) json.get("syncMessages");
                    String jsonListAsStr = _jsonMapper.writeValueAsString(jsonList);
                    List<SyncMessage> syncMessages = _jsonMapper.readValue(jsonListAsStr, new TypeReference<List<SyncMessage>>() {});

                    Date highWaterMark;
                    if (syncMessages == null || syncMessages.size() == 0) {
                        highWaterMark = new Date(0);
                    }
                    else {
                        Collections.sort(syncMessages, new Comparator<SyncMessage>() {
                            @Override
                            public int compare(SyncMessage f1, SyncMessage f2)
                            {
                                return Long.compare(f1.getCreateDate().getTime(), f2.getCreateDate().getTime());
                            }
                        });
                        Collections.reverse(syncMessages);
                        highWaterMark = syncMessages.get(0).getCreateDate();
                    }

                    _logger.info(MessageFormat.format("{0} highWaterMark set to {1,date,yyyy-MM-dd hh:mm:ss.SSS}", _nickname, highWaterMark));
                    _syncMessageProvider = new SyncMessageProviderPolling(Player.this, _collector, highWaterMark, _authHeaders, _nickname);

                } catch (IOException e) {
                    _logger.error(MessageFormat.format("{0} uable to parse sync messages json response", _nickname), e);
                    throw new IllegalStateException();
                }
            }

            @Override
            public void dataCallbackFailure(REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode,
                    String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
            {
                _logger.error(MessageFormat.format("{0} network error. error: {1}, responseCode: {2}, msg: {3}", _nickname, errorType, httpResponseCode, responseMessage));
                throw new IllegalStateException();
            }
        }, _authHeaders, gameId, new Date(0));

        return this;
    }

    void beginPoolPlay(String gameId, CountDownLatch cdlPool)
    {
        _collector.beginPoolPlay(new DataReceiver() {
            @Override
            public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
            {
                cdlPool.countDown();

                _syncMessageProvider.start(gameId);
            }

            @Override
            public void dataCallbackFailure(REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode,
                    String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
            {
                if (errorType == ERROR_TYPE.ROUND_LOCKED) {
                    //this is ok, just try again
                    _logger.info(MessageFormat.format("{0} received roundLocked. trying again...", _nickname));
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                    }
                    beginPoolPlay(gameId, cdlPool);
                } else {
                    cdlPool.countDown();
                    _logger.warn(MessageFormat.format(
                            "{0} unable to begin pool play: {1}, httpResponseCode: {2}, msg: {3}, body: {4}",
                            _nickname, errorType, httpResponseCode, responseMessage, responseBody));
                    Player.this.stop();
                }
            }
        }, _authHeaders, gameId);
    }

    void beginBracketPlay(String gameId)
    {
        _syncMessageProvider.start(gameId);
    }

    private void stop()
    {
        _logger.info(MessageFormat.format("{0} shutting down", _nickname));
        _gracefulInterrupt = true;
        _runner.interrupt();

        _syncMessageProvider.stop();

        _cdl.countDown();
    }

    //SyncMessageReceiver
    @Override
    public void syncMessageReceiverReady()
    {
        _logger.info(MessageFormat.format("{0} is ready to begin play", _nickname));
        _cdlStart.countDown();
    }

    //SyncMessageReceiver
    @Override
    public void syncMessageReceiverError()
    {
        _cdlStart.countDown();
        stop();
    }

    //SyncMessageReceiver
    @Override
    public void syncMessageReceived(SyncMessage sm)
    {
        _syncMessages.add(sm);
        try {
            _queue.put(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append(_nickname);
        buf.append(", ").append(_answerSpeed).append("/").append(_skillLevel);
        buf.append(", correctAnswerPercentage: ").append(_correctAnswerPercentage);
        buf.append(", POOL round answer speeds: ");
        for (String key : _answerSpeedForPoolRounds.keySet()) {
            buf.append(", ").append(_answerSpeedForPoolRounds.get(key)).append(" s");
        }
        buf.append(", BRACKET round answer speeds: ").append(_answerSpeedForBracketRounds).append(" s");

        return buf.toString();
    }

    private class PlayerThread
    extends Thread
    {
        @Override
        public void run()
        {
            try {
                while (!isInterrupted()) {
                    _queue.take(); //block until input is available
                    process();
                }
            } catch (InterruptedException e) {
                if (!_gracefulInterrupt) {
                    _logger.error(MessageFormat.format("{0} PlayerThread was interrupted", _nickname), e);
                }
            }
        }
    }

    private void process()
    {
        switch (_currentState)
        {
            case WAITING_FOR_JOINED_OR_ABANDONED_ROUND: {
                SyncMessage sm = getSyncMessageOfTypeGreaterThanHighWaterMark(new HashSet<>(Arrays.asList("joined_round", "abandoned_round")));
                if (sm != null) {
                    switch (sm.getMessageType())
                    {
                        case "joined_round":
                            processJoinedRoundSyncMessage(sm);
                            break;

                        case "abandoned_round":
                            processAbandonedRoundSyncMessage(sm);
                            break;
                    }
                }
            }
            break;

            case WAITING_FOR_PAIRING: {
                SyncMessage sm = getSyncMessageOfTypeGreaterThanHighWaterMark(new HashSet<>(Arrays.asList("user_matched", "abandoned_round")));
                if (sm != null) {
                    switch (sm.getMessageType())
                    {
                        case "user_matched":
                            processUserMatchedSyncMessage(sm);
                            break;

                        case "abandoned_round":
                            processAbandonedRoundSyncMessage(sm);
                            break;
                    }
                }
            }
            break;

            case WAITING_FOR_QUESTION: {
                SyncMessage sm = getSyncMessageOfTypeGreaterThanHighWaterMark(new HashSet<>(Collections.singletonList("question")));
                if (sm != null) {
                    processQuestionSyncMessage(sm);
                }
            }
            break;

            case WAITING_FOR_QUESTION_RESULT: {
                SyncMessage sm = getSyncMessageOfTypeGreaterThanHighWaterMark(new HashSet<>(Collections.singletonList("question_result")));
                if (sm != null) {
                    processQuestionResultSyncMessage(sm);
                }
            }
            break;

            case WAITING_FOR_POST_QUESTION_RESULT: {
                SyncMessage sm = getSyncMessageOfTypeGreaterThanHighWaterMark(new HashSet<>(Arrays.asList("question", "match_result")));
                if (sm != null) {
                    switch (sm.getMessageType())
                    {
                        case "question":
                            processQuestionSyncMessage(sm);
                            break;

                        case "match_result":
                            processMatchResultSyncMessage(sm);
                            break;
                    }
                }
            }
            break;

            case WAITING_FOR_BRACKET_POST_MATCH_RESULT: {
                SyncMessage sm = getSyncMessageOfTypeGreaterThanHighWaterMark(new HashSet<>(Arrays.asList("eliminated", "game_result", "joined_round")));
                if (sm != null) {
                    switch (sm.getMessageType())
                    {
                        case "eliminated":
                            processEliminatedSyncMessage(sm);
                            break;

                        case "game_result":
                            processGameResultSyncMessage(sm);
                            break;

                        case "joined_round":
                            processJoinedRoundSyncMessage(sm);
                            break;
                    }
                }
            }
            break;
        }

        if (_finish) {
            stop();
        }
    }

    private void processJoinedRoundSyncMessage(SyncMessage sm)
    {
        _highWaterMark = sm.getCreateDate().getTime();
        _currentState = STATE.WAITING_FOR_PAIRING;
//        _syncMessageProvider.setHighWaterMark(_highWaterMark);

        //grab the round type and store for later
        JsonNode json;
        try {
            json = _jsonMapper.readTree(sm.getPayload());
            JsonNode roundPlayerJson = json.get("roundPlayer");
            _roundId = roundPlayerJson.get("roundId").asText();

            //it's possible the round isn't in the map - in the case of a dynamically added bracket round. that's ok. the first bracket round
            // will be in the map. so if this returns a null, just use the previously round type (will will be bracket)
            ROUND_TYPE roundType = _roundToRoundTypeMap.get(_roundId);
            _roundType =  roundType != null ? roundType : _roundType;
_logger.info(MessageFormat.format("{0} joined_round. type: {1}, id: {2}", _nickname, _roundType, _roundId));

        } catch (IOException e) {
            _logger.error(MessageFormat.format("{0} received invalid sync message json for payload of joined_round", _nickname), e);
            stop();
        }
    }

    private void processAbandonedRoundSyncMessage(SyncMessage sm)
    {
        _highWaterMark = sm.getCreateDate().getTime();
//        _syncMessageProvider.setHighWaterMark(_highWaterMark);
        _finish = true;
    }

    private void processUserMatchedSyncMessage(SyncMessage sm)
    {
        _highWaterMark = sm.getCreateDate().getTime();
        _currentState = STATE.WAITING_FOR_QUESTION;
//        _syncMessageProvider.setHighWaterMark(_highWaterMark);
    }

    private void processQuestionSyncMessage(SyncMessage sm)
    {
        _highWaterMark = sm.getCreateDate().getTime();
        _currentState = STATE.WAITING_FOR_QUESTION_RESULT;
//        _syncMessageProvider.setHighWaterMark(_highWaterMark);

        //grab the sqaId from the sync message (and the encrypted question text for later)
        String sqaId = null;
        String encryptedQuestionStr;
        JsonNode json;
        try {
            json = _jsonMapper.readTree(sm.getPayload());
            sqaId = json.get("subscriberQuestionAnswerId").asText();
            encryptedQuestionStr = json.get("question").asText();

        } catch (IOException e) {
            _logger.error(MessageFormat.format("{0} received invalid sync message json for payload of question", _nickname), e);
            stop();
            return;
        }

        //grab the decrypt key
        String postUrl = _collector.getCollectorUrlsFromSRD()[0] + "/snowl/question/getDecryptKey";
        Map<String, String>  params = new HashMap<>();
        params.put("subscriberQuestionAnswerId", sqaId);
        String responseHtml;
        try {
            responseHtml = HttpLibrary.httpPost(postUrl, _authHeaders, params);
        } catch (IOException | NetworkException e) {
            _logger.error(MessageFormat.format("{0} was unable to retrieve decrypt key", _nickname), e);
            stop();
            return;
        }

        //decrypt the question
        Question question = null;
        try {
            json = _jsonMapper.readTree(responseHtml);
            String decryptKey = json.get("decryptKey").asText();
            String passphrase = decryptKey.substring(0, 16);
            String initializationVector = decryptKey.substring(16);
            String unencryptedQuestionStr = aesDecrypt(encryptedQuestionStr, "UTF-8", passphrase, initializationVector);

            question = _jsonMapper.readValue(unencryptedQuestionStr, new TypeReference<Question>(){});

        } catch (IOException | GeneralSecurityException e) {
            _logger.error(MessageFormat.format("{0} unable to decrypt question", _nickname), e);
            stop();
            return;
        }

        //determine if the question should be answered correctly or incorrectly and grab the appropriate answerId
        boolean answerCorrectly = new Random().nextDouble() <= _correctAnswerPercentage;
        String answerId = answerCorrectly ? LoadTester.correctAnswerMap.get(question.getId()) : LoadTester.incorrectAnswerMap.get(question.getId());

        //wait the specified amount of time
        double waitTimeMs;
        if (_roundType == Round.ROUND_TYPE.POOL) {
            waitTimeMs = _answerSpeedForPoolRounds.get(_roundId) * 1_000D;
        } else {
            waitTimeMs = _answerSpeedForBracketRounds * 1_000D;
        }
        try {
            Thread.sleep((long)waitTimeMs);
        } catch (InterruptedException ignored) {
        }

        //submit the answer
        //_logger.info(MessageFormat.format("{0} submitting answer. sqaId: {1}, answerId: {2}", _nickname, sqaId, answerId));
        _collector.submitAnswer(new DataReceiver() {
            @Override
            public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
            {
                //no-op
            }

            @Override
            public void dataCallbackFailure(
                REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode, String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
            {
                _logger.error(MessageFormat.format("{0} failed to answer question. errorType: {1}, httpResponse: {2}", _nickname, errorType, httpResponseCode));
                Player.this.stop();
            }
        }, _authHeaders, sqaId, answerId);
    }

    private void processQuestionResultSyncMessage(SyncMessage sm)
    {
        _highWaterMark = sm.getCreateDate().getTime();
        _currentState = STATE.WAITING_FOR_POST_QUESTION_RESULT;
//        _syncMessageProvider.setHighWaterMark(_highWaterMark);
    }

    @SuppressWarnings("incomplete-switch")
    private void processMatchResultSyncMessage(SyncMessage sm)
    {
        _highWaterMark = sm.getCreateDate().getTime();

        String determination = null;
        int livesLeft = 0;
        try {
            JsonNode json = _jsonMapper.readTree(sm.getPayload());
            determination = json.get("roundPlayer").get("determination").asText();
            livesLeft = json.get("gamePlayer").get("countdownToElimination").asInt();
        } catch (IOException e) {
            //ignore; shouldn't happen
        }

        switch (_roundType)
        {
            case POOL:
                _finish = true;
                _logger.info(MessageFormat.format("{0} MATCH RESULT: {1}", _nickname, determination));
                break;

            case BRACKET:
                _logger.info(MessageFormat.format("{0} MATCH RESULT: {1}, #lives left: {2}", _nickname, determination, livesLeft));
                _currentState = STATE.WAITING_FOR_BRACKET_POST_MATCH_RESULT;
                break;
        }

//        _syncMessageProvider.setHighWaterMark(_highWaterMark);
    }

    private void processEliminatedSyncMessage(SyncMessage sm)
    {
        _highWaterMark = sm.getCreateDate().getTime();
//        _syncMessageProvider.setHighWaterMark(_highWaterMark);
        _finish = true;
    }

    private void processGameResultSyncMessage(SyncMessage sm)
    {
        _highWaterMark = sm.getCreateDate().getTime();
//        _syncMessageProvider.setHighWaterMark(_highWaterMark);
        _finish = true;
    }

    private SyncMessage getSyncMessageOfTypeGreaterThanHighWaterMark(Set<String> types)
    {
        return _syncMessages.stream()
                .filter(sm -> sm.getCreateDate().getTime() > _highWaterMark && types.contains(sm.getMessageType()))
                .findFirst()
                .orElse(null);
    }

    private static String aesDecrypt(String message, String encoding, String passphrase, String initializationVector)
    throws GeneralSecurityException, IOException
    {
        IvParameterSpec iv = new IvParameterSpec(initializationVector.getBytes(encoding));
        SecretKeySpec skeySpec = new SecretKeySpec(passphrase.getBytes(encoding), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        //Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] inBytes = Base64.getDecoder().decode(message);
        return new String(cipher.doFinal(inBytes), encoding);
    }

}
