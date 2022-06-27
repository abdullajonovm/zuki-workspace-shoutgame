package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.DelayedMessage;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.collector.PoolPlayMatchesInProgressException;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class HandlerBeginBracketPlay
extends BaseHandler
{
//    private static Logger _logger = Logger.getLogger(HandlerBeginBracketPlay.class);

    private static final String BEGIN_BRACKET_PLAY = "BEGIN_BRACKET_PLAY";
    private static final String BEGIN_NEXT_BRACKET_PLAY_AT = "BEGIN_NEXT_BRACKET_PLAY_AT";

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected CommonBusinessLogic _commonBusinessLogic;

    @Autowired
    protected MatchMaker _matchMaker;

    public static Message getBraketPlayBeginMessage(String gameId, long beginsInMs)
    {
        Map<String, Object> payload = new FastMap<>(
            "gameId", gameId,
            "beginsInMs", beginsInMs
        );

        return new Message(BEGIN_BRACKET_PLAY, payload);
    }

    public static DelayedMessage getNextBracketPlayBeginAtMessage(Game game, Round round, long delayMs)
    {
        Map<String, Object> payload = new FastMap<>(
            "game", game,
            "round", round,
            "timeoutAt", System.currentTimeMillis() + delayMs
        );

        return new DelayedMessage(BEGIN_NEXT_BRACKET_PLAY_AT, payload, delayMs);
    }

    @SuppressWarnings("unchecked")
    @Override
    //MessageProcessor
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case BEGIN_BRACKET_PLAY:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "gameId", payload.get("gameId"),
                        "beginsInMs", payload.get("beginsInMs")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerBeginBracketPlay received BEGIN_BRACKET_PLAY\n{0}", JsonUtil.print(map)));
                }
                handleBeginBracketPlay(message);
                break;

            case BEGIN_NEXT_BRACKET_PLAY_AT:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "game", ((Game)payload.get("game")).getId(),
                        "round", ((Round)payload.get("round")).getId(),
                        "timeoutAt", payload.get("timeoutAt")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerBeginBracketPlay received BEGIN_NEXT_BRACKET_PLAY_AT\n{0}", JsonUtil.print(map)));
                }
                handleNextBeginBracketPlayAt(message);
                break;

            //else ignore
        }
    }

    @SuppressWarnings("unchecked")
    private void handleBeginBracketPlay(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        String gameId = (String) payload.get("gameId");
        long beginsInMs = (Long) payload.get("beginsInMs");

        long then = System.currentTimeMillis();

        Object[] obj = (Object[]) wrapInTransaction((x) -> {
            Game game = _shoutContestService.getGame(gameId);
            if (game == null) {
                throw new IllegalStateException("game not found");
            }
            if (game.getGameStatus() != Game.GAME_STATUS.OPEN) {
                throw new IllegalStateException("game not open");
            }

            //find the first bracket round
            final List<Round> rounds = _shoutContestService.getRoundsForGame(gameId);
            Round nextRoundInSequence = rounds.stream()
                .filter(r -> r.getRoundType() == Round.ROUND_TYPE.BRACKET)
                .sorted( Comparator.comparing(Round::getRoundSequence, Comparator.nullsLast(Comparator.naturalOrder())) )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no bracket round found"));

            try {
                _commonBusinessLogic.startBracketPlay(game, rounds, null, beginsInMs);
            } catch (PoolPlayMatchesInProgressException e1) {
                throw new IllegalStateException("all pool rounds should have completed before this was called");
            }

            return new Object[] {game, rounds, nextRoundInSequence};
        }, null);

        Game game = (Game) obj[0];
        List<Round> rounds = (List<Round>) obj[1];
        Round nextRoundInSequence = (Round) obj[2];

        //start this on a separate thread so as not to block the admin client's return to the caller
        new Thread(() -> {
            _commonBusinessLogic.completeBracketPlayStart(game, rounds, nextRoundInSequence, beginsInMs, then, null, _socket);
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void handleNextBeginBracketPlayAt(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Game game = (Game) payload.get("game");
        Round round = (Round) payload.get("round");
        long timeoutAt = (Long) payload.get("timeoutAt");

        if (timeoutAt <= System.currentTimeMillis()) {
            //start the next bracket round
            _matchMaker.createBracketMatches(game, round, false);

        } else {
            //not yet time; wait and try again
            if (_busLogger.isDebugEnabled()) {
                _busLogger.debug("HandlerBeginBracketPlay BEGIN_NEXT_BRACKET_PLAY_AT message arrived too early. adding back to queue...");
            }
            _messageBus.sendDelayedMessage(getNextBracketPlayBeginAtMessage(
                game, round, timeoutAt - System.currentTimeMillis()
            ));
        }

    }
}
