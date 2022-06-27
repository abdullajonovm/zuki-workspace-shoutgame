package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerBeginBracketPlay;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerScoreQuestionFixedLife;
import tv.shout.snowyowl.service.CurrentRankCalculator;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.service.ISyncService;

public class MockHandlerScoreQuestion
extends HandlerScoreQuestionFixedLife
{
    private static Logger _logger = Logger.getLogger(MockHandlerScoreQuestion.class);
    private static AtomicInteger outstandingMatchResultCount = new AtomicInteger(0);

    public MockHandlerScoreQuestion(MessageBus messageBus, IDaoMapper dao, IShoutContestService shoutContestService, IIdentityService identityService,
            MockQuestionSupplier questionSupplier, MockSubscriberStatsHandler subscriberStatsHandler, CurrentRankCalculator currentRankCalculator,
            CommonBusinessLogic commonBusinessLogic, BotEngine botEngine, SponsorEngine sponsorEngine,
            int cumulativeScore_WON_CORRECT, int cumulativeScore_WON_TIME, int cumulativeScore_WON_TIMEOUT,
            int cumulativeScore_LOST_TIME, int cumulativeScore_LOST_INCORRECT, int cumulativeScore_LOST_TIMEOUT, int cumulativeScore_LOST_ALL_TIMEOUT,
            long tiebreakerDelayMs)
    {
        _messageBus = messageBus;
        _dao = dao;
        _shoutContestService = shoutContestService;
        _identityService = identityService;
        _questionSupplier = questionSupplier;
        _subscriberStatsHandler = subscriberStatsHandler;
        _currentRankCalculator = currentRankCalculator;
        _commonBusinessLogic = commonBusinessLogic;
        _botEngine = botEngine;
        _sponsorEngine = sponsorEngine;

        _cumulativeScore_WON_CORRECT = cumulativeScore_WON_CORRECT;             //10
        _cumulativeScore_WON_TIME = cumulativeScore_WON_TIME;                   //10
        _cumulativeScore_WON_TIMEOUT = cumulativeScore_WON_TIMEOUT;             //10
        _cumulativeScore_LOST_TIME = cumulativeScore_LOST_TIME;                 // 5
        _cumulativeScore_LOST_INCORRECT = cumulativeScore_LOST_INCORRECT;       // 2
        _cumulativeScore_LOST_TIMEOUT = cumulativeScore_LOST_TIMEOUT;           // 0
        _cumulativeScore_LOST_ALL_TIMEOUT = cumulativeScore_LOST_ALL_TIMEOUT;   // 0

        _tiebreakerDelayMs = tiebreakerDelayMs; //10000
    }

    public static void incrementMatchResultCount()
    {
        outstandingMatchResultCount.incrementAndGet();
    }

    @Override
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;
        result = transactionalMethod.apply(params);
        return result;
    }

    @Override
    public void enqueueSyncMessage(
        ObjectMapper jsonMapper, ISyncService syncService, Logger logger,
        String contextualId, String messageType, Map<String, Object> payload,
        Subscriber subscriber, Socket socketIoSocket, ITriggerService triggerService)
    {
        _logger.debug(MessageFormat.format(
                "enqueueing sync message. recipient: {0,number,#}, messageType: {1}",
                subscriber.getSubscriberId(), messageType));

        if (messageType.equals(ISnowyowlService.SYNC_MESSAGE_MATCH_RESULT) && ((Round.ROUND_TYPE)payload.get("roundType")).equals(Round.ROUND_TYPE.POOL)) {
            _logger.info(MessageFormat.format("POOL PLAY match is over. sId: {0,number,#}", payload.get("subscriberId")));

            int outstandingMatchResults = outstandingMatchResultCount.decrementAndGet();
            if (outstandingMatchResults == 0) {
                new Thread(() -> {
                    _logger.info("waiting 5s for things to settle...");
                    try { Thread.sleep(5_000L); } catch (InterruptedException ignored) {}
                    beginBracketPlay(contextualId);
                }).start();
            }
        }
    }

    @Override
    protected void sendTwitchUpdate(Round round, Match match, Long twitchSubscriberId, List<MatchPlayer> matchPlayers)
    {
        _logger.debug(MessageFormat.format(
                "sending twitch update [TWITCH_MATCH_OVER]. twitch subscriberId: {0,number,#}",
                twitchSubscriberId));
    }

    @Override
    protected void sendPlayerCountSocketIoMessage(int remainingPlayerCount)
    {
        _logger.debug(MessageFormat.format(
                "sending updated player count socket.io msg. remainingPlayerCount: {0}",
                remainingPlayerCount));
    }

    private void beginBracketPlay(String gameId)
    {
        _logger.info("\n\n>Beginning BRACKET play for game: " + gameId + "\n\n");
        long beginsInMs = 10_000L;

        _messageBus.sendMessage(HandlerBeginBracketPlay.getBraketPlayBeginMessage(gameId, beginsInMs));
    }
}
