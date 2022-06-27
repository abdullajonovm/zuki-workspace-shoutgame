package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.identity.domain.Subscriber;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.DelayedMessage;
import tv.shout.simplemessagebus.Message;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class HandlerBeginMatch
extends BaseHandler
{
    private static final String BEGIN_MATCH = "BEGIN_MATCH";

    private static Logger _logger = Logger.getLogger(HandlerBeginMatch.class);

    @Autowired
    protected IShoutContestService _shoutContestService;

    //call this to create a properly formatted payload for this handler and message type
    static Message createMessage(
        Game game, Round round, Match match, List<MatchPlayer> matchPlayers, Map<Long, Subscriber> subscribers,
        List<Long> botsInGame, List<Long> sponsorsInGame, boolean isTieBreaker)
    {
        Map<String, Object> payload = new FastMap<>(
                "game", game,
                "round", round,
                "match", match,
                "matchPlayers", matchPlayers,
                "subscribers", subscribers,
                "botsInGame", botsInGame,
                "sponsorsInGame", sponsorsInGame,
                "isTieBreaker", isTieBreaker
            );

        return new Message(BEGIN_MATCH, payload);
    }

    @SuppressWarnings({ "unused", "unchecked" })
    @Override
    //MessageProcessor
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case BEGIN_MATCH:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "game", ((Game)payload.get("game")).getId(),
                        "round", ((Round)payload.get("round")).getId(),
                        "match", ((Match)payload.get("match")).getId(),
                        "matchPlayers", getMatchPlayersFromPayload(payload, "matchPlayers"),
                        "subscribers", getSubscribersFromPayload(payload, "subscribers"),
                        "isTieBreaker", payload.get("isTieBreaker")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerBeginMatch received BEGIN_MATCH\n{0}", JsonUtil.print(map)));
                }
                handleBeginMatch(message);
                break;

            //else ignore
        }
    }

    @SuppressWarnings("unchecked")
    private void handleBeginMatch(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Game game = (Game) payload.get("game");
        Round round = (Round) payload.get("round");
        Match match = (Match) payload.get("match");
        List<MatchPlayer> matchPlayers = (List<MatchPlayer>) payload.get("matchPlayers");
        Map<Long, Subscriber> subscribers = (Map<Long, Subscriber>) payload.get("subscribers");
        List<Long> botsInGame = (List<Long>) payload.get("botsInGame");
        List<Long> sponsorsInGame = (List<Long>) payload.get("sponsorsInGame");
        boolean isTieBreaker= (Boolean) payload.get("isTieBreaker");

        //change the status from NEW to OPEN
        match.setMatchStatus(Match.MATCH_STATUS.OPEN);
        match.setMatchStatusSetAt(new Date());
        wrapInTransaction((x) -> {
            _shoutContestService.updateMatch(match);
            return null;
        }, null);

        //queue up to send the first question
if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("sending delayed SEND_QUESTION_AT msg, scheduled for: {0} ms", round.getDurationBetweenActivitiesSeconds()*1_000L));
        DelayedMessage msg = HandlerSendQuestion.getDelayedMessage(
            round.getDurationBetweenActivitiesSeconds()*1_000L,
            game, round, match, matchPlayers, subscribers,
            botsInGame, sponsorsInGame, isTieBreaker
        );
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("HandlerBeginMatch sending delayed message: " + msg.type);
        }
        _messageBus.sendDelayedMessage(msg);
    }

}
