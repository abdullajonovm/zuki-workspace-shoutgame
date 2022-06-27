package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.snowyowl.reactiveengine.fixedround.MatchMaker;
import tv.shout.sync.service.ISyncService;

public class MockMatchMaker
extends MatchMaker
{
    private static Logger _logger = Logger.getLogger(MockMatchMaker.class);

    public MockMatchMaker(
            MessageBus messageBus, IDaoMapper dao,
            IIdentityService identityService, IShoutContestService shoutContestService, BotEngine botEngine, SponsorEngine sponsorEngine)
    {
        _messageBus = messageBus;
        _dao = dao;
        _identityService = identityService;
        _shoutContestService = shoutContestService;
        _botEngine = botEngine;
        _sponsorEngine = sponsorEngine;
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
    }

    @Override
    protected void sendTwitchUpdate(Game game, Round round, Match match, List<MatchPlayer> players, Long twitchSubscriberId)
    {
        _logger.debug(MessageFormat.format(
                "sending twitch update [TWITCH_PAIRED]. twitch subscriberId: {0,number,#}",
                twitchSubscriberId));
    }

    @Override
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;
        result = transactionalMethod.apply(params);
        return result;
    }

}
