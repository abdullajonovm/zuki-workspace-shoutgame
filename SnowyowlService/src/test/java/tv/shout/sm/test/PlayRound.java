package tv.shout.sm.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.meinc.commons.encryption.EncryptUtils;

import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.sm.db.DbProvider;
import tv.shout.sm.test.CollectorToWdsResponse.DataReceiver;
import tv.shout.sm.test.CollectorToWdsResponse.ERROR_TYPE;
import tv.shout.sm.test.CollectorToWdsResponse.REQUEST_TYPE;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.sync.service.ISyncService;

public class PlayRound
{
    private static Logger _logger = Logger.getLogger(PlayRound.class);

    private ISyncService _syncService;
    private IShoutContestService _shoutContestService;
    private CollectorToWdsResponse _collector;

    private int _subscriberId;
    private Map<String, String> _authHeaders;

    private String _gameId;
    private Round.ROUND_TYPE _roundType;
    private String _matchId;
    private String _matchPlayerId;
    private int _opponentSubscriberId;

    public PlayRound(DbProvider.DB which, int subscriberId, Map<String, String> authHeaders, String gameId)
    {
        throw new UnsupportedOperationException();
//        _subscriberId = subscriberId;
//        _authHeaders = authHeaders;
//        _gameId = gameId;
//
//        _syncService = new SyncServiceClientProxy(false);
//        _shoutContestService = new ShoutContestServiceClientProxy(false);
//        _collector = new CollectorToWdsResponse(which);
//
//        //setup the service backdoor entry point based on which database is being used
//        switch (which)
//        {
//            case DC4: {
//                InetSocketAddress socket;
//                try {
//                    socket = new InetSocketAddress(Inet4Address.getByName("dc4-collector1.shoutgameplay.com"), 43911);
//                } catch (UnknownHostException e) {
//                    _logger.error("unable to connect to dc4", e);
//                    throw new IllegalStateException(e);
//                }
//                MrSoaServer server = new MrSoaServer(socket);
//                MrSoaServerMonitor monitor = MrSoaServerMonitor.getInstance();
//                monitor.registerService(
//                        ((SyncServiceClientProxy)_syncService).getEndpoint(),
//                        server);
//                monitor.registerService(
//                        ((ShoutContestServiceClientProxy)_shoutContestService).getEndpoint(),
//                        server);
//            }
//            break;
//        }

    }

    private class JoinedRoundPayloadWrapper
    {
        public MyRoundPlayer roundPlayer;
    }

    public void waitForJoinedRoundSyncMessage()
    {
        _logger.info("checking for 'joined_round' sync message...");

        //grab the sync messages and find the most recent "joined_game" that matches the _gameId
        List<SyncMessage> syncMessages = _syncService.getSyncMessages(_subscriberId, ApiTest.getLastSyncDate(_gameId), _gameId);
        Optional<SyncMessage> oJoinedRoundSyncMessage = syncMessages.stream()
            .filter(sm -> sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_JOINED_ROUND.toString()))
            .sorted( (sm1, sm2) -> sm2.getCreateDate().compareTo(sm1.getCreateDate()) ) //since sm1, sm2 are reversed, it will be in reversed order (i.e. newest on top)
            .findFirst();

        //if there isn't one, wait a bit and try again
        if (!oJoinedRoundSyncMessage.isPresent()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) { }
            waitForJoinedRoundSyncMessage();
            return;
        }

        ApiTest.addProcessedMessage(_gameId, oJoinedRoundSyncMessage.get());

        //parse the message to get relevant data
        Gson gson = new Gson();
        JoinedRoundPayloadWrapper wrapper = gson.fromJson(oJoinedRoundSyncMessage.get().getPayload(), JoinedRoundPayloadWrapper.class);

        String roundId = wrapper.roundPlayer.roundId;
        Round round = _shoutContestService.getRound(roundId);
        _roundType = round.getRoundType();

        _logger.info("\n\nRECEIVED 'joined_round'\n\n");

        waitForUserMatchedSyncMessage();
    }

    private class MyMatchPlayer
    {
        public String id;
        public String matchId;
        public int subscriberId;
    }
    private class UserMatchedPayloadWrapper
    {
        public List<MyMatchPlayer> players;
    }

    private void waitForUserMatchedSyncMessage()
    {
        _logger.info("checking for 'user_matched' sync message...");

        //grab the sync messages and find the most recent "user_matched" that matches the _gameId
        List<SyncMessage> syncMessages = _syncService.getSyncMessages(_subscriberId, ApiTest.getLastSyncDate(_gameId), _gameId);
        Optional<SyncMessage> oUserMatchedSyncMessage = syncMessages.stream()
            .filter(sm -> sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_USER_MATCHED.toString()))
            .sorted( (sm1, sm2) -> sm2.getCreateDate().compareTo(sm1.getCreateDate()) ) //since sm1, sm2 are reversed, it will be in reversed order (i.e. newest on top)
            .findFirst();

        //if there isn't one, wait a bit and try again
        if (!oUserMatchedSyncMessage.isPresent()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) { }
            waitForUserMatchedSyncMessage();
            return;
        }

        //update the last sync date
        ApiTest.addProcessedMessage(_gameId, oUserMatchedSyncMessage.get());

        //parse the message to get relevant data to use later
//_logger.debug("raw user_matched payload:\n\n" + oUserMatchedSyncMessage.get().getPayload() + "\n");
        Gson gson = new Gson();
        UserMatchedPayloadWrapper wrapper = gson.fromJson(oUserMatchedSyncMessage.get().getPayload(), UserMatchedPayloadWrapper.class);

        wrapper.players.stream().forEach(mp -> {
            if (mp.subscriberId == _subscriberId) {
                _matchId = mp.matchId;
                _matchPlayerId = mp.id;
            } else {
                _opponentSubscriberId = mp.subscriberId;
            }
        });

        _logger.info(MessageFormat.format(
                "\n\nYOU ARE PAIRED AGAINST: {0,number,#}, in match: {1}, and your matchPlayerId is: {2}\n\n",
                _opponentSubscriberId, _matchId, _matchPlayerId));

        waitForQuestion();
    }

    private class QuestionPayloadWrapper
    {
        public String subscriberQuestionAnswerId;
        public String question; //encrypted json of the question/answers
    }

    private void waitForQuestion()
    {
        _logger.info("checking for 'question' sync message...");

        //grab the sync messages and find the most recent "question" that matches the _gameId
        List<SyncMessage> syncMessages = _syncService.getSyncMessages(_subscriberId, ApiTest.getLastSyncDate(_gameId), _gameId);
        Optional<SyncMessage> oQuestionSyncMessage = syncMessages.stream()
            .filter(sm -> sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_QUESTION.toString()))
            .filter(sm -> !ApiTest.getProcessedMessages(_gameId).contains(sm))
            .sorted( (sm1, sm2) -> sm2.getCreateDate().compareTo(sm1.getCreateDate()) ) //since sm1, sm2 are reversed, it will be in reversed order (i.e. newest on top)
            .findFirst();

        //if there isn't one, wait a bit and try again
        if (!oQuestionSyncMessage.isPresent()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) { }
            waitForQuestion();
            return;
        }

        //update the last sync date
        ApiTest.addProcessedMessage(_gameId, oQuestionSyncMessage.get());

        //parse the message to get relevant data to use later
//_logger.debug("raw question payload:\n\n" + oQuestionSyncMessage.get().getPayload() + "\n");
        Gson gson = new Gson();
        QuestionPayloadWrapper wrapper = gson.fromJson(oQuestionSyncMessage.get().getPayload(), QuestionPayloadWrapper.class);

//_logger.debug("subscriberQuestionAnswerId (from sync message payload): " + wrapper.subscriberQuestionAnswerId);

        parseQuestion(wrapper);
    }

    private class MyI8tn
    {
        public String en;
    }
    private class MyAnswer
    {
        public String id;
        public boolean correct;
        public MyI8tn answerText;
    }
    private class MyQuestion
    {
        public MyI8tn questionText;
        public List<MyAnswer> answers;
    }

    private void parseQuestion(QuestionPayloadWrapper wrapper)
    {
        _logger.info("received question. requesting decrypt key for: " + wrapper.subscriberQuestionAnswerId);

        Gson gson = new Gson();

        _collector.getQuestionDecryptKey(new DataReceiver() {
            @Override
            public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
            {
                String decryptKey = json.get("decryptKey").asText();
//_logger.debug("decryptKey: " + decryptKey);

                String questionJson = decode(wrapper.question, decryptKey);
//_logger.debug("raw question json:\n" + questionJson + "\n");

                MyQuestion question = gson.fromJson(questionJson, MyQuestion.class);
                displayQuestionForAnswer(question, wrapper.subscriberQuestionAnswerId);
            }

            @Override
            public void dataCallbackFailure(REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode,
                    String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
            {
                _logger.error(MessageFormat.format("FAILURE. type: {0}, http response code: {1}, message: {2}, body:\n{3}", errorType, httpResponseCode, responseMessage, responseBody));
            }
        }, _authHeaders, wrapper.subscriberQuestionAnswerId);
    }

    private String decode(String encodedMessage, String encryptKey)
    {
        byte[] base64EncodedAes256EncodedBytes;
        try {
            base64EncodedAes256EncodedBytes = encodedMessage.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); //won't happen
        }

        byte[] aes256EncodedBytes = Base64.getDecoder().decode(base64EncodedAes256EncodedBytes);
        byte[] plainTextBytes = EncryptUtils.aes256Decode(aes256EncodedBytes, encryptKey);
        try {
            return new String(plainTextBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); //won't happen
        }
    }

    private void displayQuestionForAnswer(MyQuestion question, String subscriberQuestionAnswerId)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("\n").append(question.questionText.en);
        for (int i=0; i<question.answers.size(); i++) {
            MyAnswer answer = question.answers.get(i);
            buf.append("\n").append(i).append(")     ");
            if (answer.correct) buf.append("* ");
            buf.append(answer.answerText.en);
        }
        buf.append("\n");

        System.out.println("\n" + buf.toString());

        int selectedAnswerIdx = -1;
        while (selectedAnswerIdx == -1) {
            try {
                selectedAnswerIdx = Integer.parseInt(getConsoleInput("> "));
            } catch (Exception ignored) {
                //try again
            }
        }

        String selectedAnswerId = question.answers.get(selectedAnswerIdx).id;

        _collector.submitAnswer(new DataReceiver() {
            @Override
            public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
            {
                waitForQuestionResultSyncMessage();
            }

            @Override
            public void dataCallbackFailure(REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode,
                    String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
            {
                _logger.error(MessageFormat.format("FAILURE. type: {0}, http response code: {1}, message: {2}, body:\n{3}", errorType, httpResponseCode, responseMessage, responseBody));
            }
        }, _authHeaders, subscriberQuestionAnswerId, selectedAnswerId);
    }

    private class MyMatchQuestion
    {
        public String determination;
    }
    private class MySubscriberQuestionAnswer
    {
        public int subscriberId;
        public String determination;
    }
    private class QuestionResultPayloadWrapper
    {
        public MyMatchQuestion matchQuestion;
        public List<MySubscriberQuestionAnswer> subscriberQuestionAnswers;
    }

    private void waitForQuestionResultSyncMessage()
    {
        _logger.info("checking for 'question_result' sync message...");

        //grab the sync messages and find the most recent "user_matched" that matches the _gameId
        List<SyncMessage> syncMessages = _syncService.getSyncMessages(_subscriberId, ApiTest.getLastSyncDate(_gameId), _gameId);
        Optional<SyncMessage> oQuestionResultSyncMessage = syncMessages.stream()
            .filter(sm -> sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_QUESTION_RESULT.toString()))
            .filter(sm -> !ApiTest.getProcessedMessages(_gameId).contains(sm))
            .sorted( (sm1, sm2) -> sm2.getCreateDate().compareTo(sm1.getCreateDate()) ) //since sm1, sm2 are reversed, it will be in reversed order (i.e. newest on top)
            .findFirst();

        //if there isn't one, wait a bit and try again
        if (!oQuestionResultSyncMessage.isPresent()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) { }
            waitForQuestionResultSyncMessage();
            return;
        }

        //update the last sync date
        ApiTest.addProcessedMessage(_gameId, oQuestionResultSyncMessage.get());

        //parse the message to get relevant
        Gson gson = new Gson();
        QuestionResultPayloadWrapper wrapper = gson.fromJson(oQuestionResultSyncMessage.get().getPayload(), QuestionResultPayloadWrapper.class);

        //log the outcome of the question
        StringBuilder msg = new StringBuilder();
        msg.append("\n\nQUESTION DETERMINATION: ").append(wrapper.matchQuestion.determination).append(" (");
        for (MySubscriberQuestionAnswer sqa : wrapper.subscriberQuestionAnswers) {
            if (sqa.subscriberId == _subscriberId) {
                msg.append(sqa.determination).append(")\n\n");
                break;
            }
        }
        _logger.info(msg.toString());

        waitForQuestionOrMatchResult();
    }

    private class MyRoundPlayer
    {
        public String roundId;
        public String determination;
        //public Double rank;
    }

    private class MyMatchResult
    {
        public MyRoundPlayer roundPlayer;
        public MyGamePlayer gamePlayer;
    }

    private void waitForQuestionOrMatchResult()
    {
        _logger.info("checking for 'question' or 'match_result' sync message...");

        //grab the sync messages and find the next unhandled "question" or "match_result" that matches the _gameId
        List<SyncMessage> syncMessages = _syncService.getSyncMessages(_subscriberId, ApiTest.getLastSyncDate(_gameId), _gameId);

        Optional<SyncMessage> oSyncMessage = syncMessages.stream()
            .filter(sm ->
                sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_QUESTION.toString())  ||
                sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_MATCH_RESULT.toString()) )
            .filter(sm -> !ApiTest.getProcessedMessages(_gameId).contains(sm))
            .sorted( (sm1, sm2) -> sm1.getCreateDate().compareTo(sm2.getCreateDate()) ) //oldest on top (asc order)
            .findFirst();

        //if there isn't one, wait a bit and try again
        if (!oSyncMessage.isPresent()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) { }
            waitForQuestionOrMatchResult();
            return;
        }

        //update the last sync date
        ApiTest.addProcessedMessage(_gameId, oSyncMessage.get());

        Gson gson = new Gson();
        if (oSyncMessage.get().getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_QUESTION.toString())) {
            //parse the message to get relevant data to use later
            QuestionPayloadWrapper wrapper = gson.fromJson(oSyncMessage.get().getPayload(), QuestionPayloadWrapper.class);
            parseQuestion(wrapper);

        } else if (oSyncMessage.get().getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_MATCH_RESULT.toString())) {
            MyMatchResult wrapper = gson.fromJson(oSyncMessage.get().getPayload(), MyMatchResult.class);
            _logger.info("\n\nMATCH RESULT: " + wrapper.roundPlayer.determination + ", RANK: " + wrapper.gamePlayer.rank + "\n\n");

            switch (_roundType)
            {
                case POOL:
                    //nothing to do; the pool round has completed
                    break;

                case BRACKET:
                    waitForJoinedRoundOrEliminatedOrGameResult();
                    break;

                default:
                    throw new IllegalStateException("unexpected round state: " + _roundType);
            }
        }
    }

    private void waitForJoinedRoundOrEliminatedOrGameResult()
    {
        _logger.info("checking for 'game_result' or 'joined_round' or 'eliminated' sync message...");

        List<SyncMessage> syncMessages = _syncService.getSyncMessages(_subscriberId, ApiTest.getLastSyncDate(_gameId), _gameId);

        Optional<SyncMessage> oSyncMessage = syncMessages.stream()
            .filter(sm ->
                sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_JOINED_ROUND.toString()) ||
                sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_ELIMINATED.toString()) ||
                sm.getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_GAME_RESULT.toString()) )
            .filter(sm -> !ApiTest.getProcessedMessages(_gameId).contains(sm))
            .sorted( (sm1, sm2) -> sm1.getCreateDate().compareTo(sm2.getCreateDate()) ) //asc
            .findFirst();

        //if there isn't one, wait a bit and try again
        if (!oSyncMessage.isPresent()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) { }
            waitForQuestionOrMatchResult();
            return;
        }

        //update the last sync date
        ApiTest.addProcessedMessage(_gameId, oSyncMessage.get());

        if (oSyncMessage.get().getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_JOINED_ROUND.toString())) {
            //handle next round
            waitForUserMatchedSyncMessage();

        } else if (oSyncMessage.get().getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_ELIMINATED.toString())) {
            _logger.info("\n\nYOU HAVE BEEN ELIMINATED. Waiting for 'game_result'...");
            waitForJoinedRoundOrEliminatedOrGameResult();

        } else if (oSyncMessage.get().getMessageType().equals(ISnowyowlService.SYNC_MESSAGE_GAME_RESULT.toString())) {
            handleGameResult(oSyncMessage.get());
        }
    }

    private class MyGamePlayer
    {
        public String determination;
        public double rank;
    }
    private class GameResultSyncMessageWrapper
    {
        public MyGamePlayer gamePlayer;
        //public List<MyRoundPlayer> roundPlayers;
    }

    private void handleGameResult(SyncMessage gameResultSyncMessage)
    {
        Gson gson = new Gson();
        GameResultSyncMessageWrapper wrapper = gson.fromJson(gameResultSyncMessage.getPayload(), GameResultSyncMessageWrapper.class);
        _logger.info(MessageFormat.format(
                "\n\nFINAL RESULTS: {0}, rank: {1}\n\n",
                wrapper.gamePlayer.determination, wrapper.gamePlayer.rank));

        System.exit(0);
    }

    private static String getConsoleInput(String message)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        try {
            return br.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("oops", e);
        }
    }
}
