package tv.shout.snowyowl.reactiveengine.fixedround;

import java.util.List;
import java.util.Map;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.util.FastMap;

public abstract class BaseHandlerScoreBracket
extends BaseHandler
implements SyncMessageSender
{
    protected static final String SCORE_BRACKET_ROUND = "SCORE_BRACKET_ROUND";

    public static Message getScoreBracketRoundMessage(Game game, Round round, List<Long> botIds, List<Long> sponsorIds)
    {
        Map<String, Object> payload = new FastMap<>(
            "game", game,
            "round", round,
            "botIds", botIds,
            "sponsorIds", sponsorIds
        );

        return new Message(SCORE_BRACKET_ROUND, payload);
    }

}
