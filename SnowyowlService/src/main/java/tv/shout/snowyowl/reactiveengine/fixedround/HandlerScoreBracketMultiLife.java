package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.service.ISnowyowlService;

//TODO: i don't actually think this is necessary. the functionality of scoring the bracket should be identical, either way. it's only the ScoreQuestion that's different. this class is not needed ... probably. needs testing.
public class HandlerScoreBracketMultiLife
extends BaseHandlerScoreBracket
{
    private static Logger _logger = Logger.getLogger(HandlerScoreBracketMultiLife.class);

    @Override
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case SCORE_BRACKET_ROUND:
                @SuppressWarnings("unchecked") String engineType = ((Game)((Map<String, Object>)message.payload).get("game")).getEngineType();
                if (!engineType.equals(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife)) return;

if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("received message: {0}", message.type));
                handleScoreBracketRound(message);
                break;

            //else ignore
        }
    }

    @SuppressWarnings("unchecked")
    private void handleScoreBracketRound(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Game game = (Game) payload.get("game");
        Round round = (Round) payload.get("round");
        List<Long> botIds = (List<Long>) payload.get("botIds");
        List<Long> sponsorIds = (List<Long>) payload.get("sponsorIds");

        //TODO
    }

}
