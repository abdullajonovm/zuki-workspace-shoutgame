package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.Message;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerCloseGame;
import tv.shout.sync.service.ISyncService;

public class MockHandlerCloseGame
extends HandlerCloseGame
{
    private static Logger _logger = Logger.getLogger(MockHandlerCloseGame.class);

    public MockHandlerCloseGame(MessageBus messageBus, IDaoMapper dao, IShoutContestService shoutContestService, IIdentityService identityService, EngineCoordinator engineCoordinator)
    {
        _messageBus = messageBus;
        _dao = dao;
        _shoutContestService = shoutContestService;
        _identityService = identityService;
        _engineCoordinator = engineCoordinator;
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
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;
        result = transactionalMethod.apply(params);
        return result;
    }

    @Override
    protected void closeGame(Message message)
    {
        super.closeGame(message);

        //if it got here with no exceptions, everything worked. hooray. now close down the system. wait 5s for all messages to finish processing, just in case
        try {
            Thread.sleep(5_000L);
        } catch (InterruptedException ignored) {
        }

        TestBeginPoolPlay.shutdown();
    }
}
