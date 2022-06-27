package tv.shout.reactive;

import org.apache.log4j.Logger;

import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.engine.MMECommon;

public class MockMMECommon
extends MMECommon
{
    private static Logger _logger = Logger.getLogger(MockMMECommon.class);

    @Override
    public int publishBracketOutstandingMatchCount(Round round, int numOutstandingMatches, Socket socketIoSocket, ITriggerService triggerService)
    {
        _logger.debug("MOCK: publishing outstanding match count: " + numOutstandingMatches);
        return 0;
    }
}