package tv.shout.snowyowl.engine;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import io.socket.client.Socket;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.simplemessagebus.Message;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutTableRow;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerSendQuestion;

/**
 * Handles routing of calls to the appropriate set of engines.
 */
public class EngineCoordinator
{
    private static Logger _logger = Logger.getLogger(EngineCoordinator.class);
    private static Logger _busLogger = Logger.getLogger("messagebus");

    @Autowired
    private PayoutManager[] _payoutManagers;

    @Autowired
    private MessageBus _messageBus;

    private Map<String, PayoutManager> _payoutManagersByType;

    private boolean _started;

    //must be called when service starts
    public void start(Socket socketIoSocket)
    {
        //only start once
        if (_started) {
            return;
        }

        _payoutManagersByType = new HashMap<>(_payoutManagers.length);
//_logger.info(">>> _payoutManagersByType initialized to size: " + _payoutManagers.length);
        for (PayoutManager pm : _payoutManagers) {
//_logger.info(MessageFormat.format("\t>>> _payoutManagerByType, adding entry. key: {0}, value: {1}", pm.getType(), pm));
            _payoutManagersByType.put(pm.getType(), pm);
        }

        _started = true;
    }

    //must be called when service stops
    public void stop()
    {
        _started = false;
    }

//    public void subscriberCancelledQueuing(Game game, long subscriberId)
//    {
//        _mqesByType.get(game.getEngineType()).subscriberCancelledQueuing(subscriberId);
//    }

    public void notifyQuetionListChanged()
    {
        Message msg = HandlerSendQuestion.getQuestionsChangedMessage();
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("EngineCoordinator sending message: " + msg.type);
        }
        _messageBus.sendMessage(msg);
    }

    public float getPlayerPot(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout) throws PayoutManagerException
    {
        return getPayoutManager(game.getEngineType()).getPlayerPot(game, rounds, numPlayers, gamePayout);
    }

    public List<PayoutTableRow> generateCollapsedPayoutTable(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout) throws PayoutManagerException
    {
        return getPayoutManager(game.getEngineType()).generateCollapsedPayoutTable(game, rounds, numPlayers, gamePayout);
    }

    public List<PayoutModelRound> getAdjustedPayoutModelRounds(Game game, int numPlayers, PayoutModel payoutModel, GamePayout gamePayout)
    throws PayoutManagerException
    {
        return getPayoutManager(game.getEngineType()).getAdjustedPayoutModelRounds(numPlayers, payoutModel, gamePayout);
    }

    public int getNumberOfBracketRounds(Game game, int numPlayers)
    {
        return getPayoutManager(game.getEngineType()).getNumberOfBracketRounds(numPlayers);
    }

    public void assignPayouts(
            Game game, List<Round> rounds, Map<Long, GamePlayer> gamePlayersMap, List<RoundPlayer> players,
            Set<Long> botIdsForGame, Set<Long> sponsorPlayerIdsForGame, Set<Long> testSubscribers, GamePayout gamePayout)
    throws PayoutManagerException
    {
        getPayoutManager(game.getEngineType()).assignPayouts(game, rounds, gamePlayersMap, players, botIdsForGame, sponsorPlayerIdsForGame, testSubscribers, gamePayout);
    }

    private PayoutManager getPayoutManager(String engineType)
    {
        if (_payoutManagersByType == null) {
            throw new IllegalStateException("_payoutManagersByType is null!");
        }
        PayoutManager pm = _payoutManagersByType.get(engineType);
        if (pm == null) {
            _logger.error(MessageFormat.format("No payout manager found for engine type: {0}", engineType));
        }
        return pm;
    }
}
