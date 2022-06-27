package tv.shout.snowyowl.engine;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import io.socket.client.Socket;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutTableRow;

/**
 * Handles routing of calls to the appropriate set of engines.
 */
public class EngineCoordinator
{
    private static Logger _logger = Logger.getLogger(EngineCoordinator.class);

    @Autowired
    private MQE[] _mqes;

    @Autowired
    private MME[] _mmes;

    @Autowired
    private RME[] _rmes;

    @Autowired
    private PayoutManager[] _payoutManagers;

    private Map<String, MQE> _mqesByType;
    private Map<String, MME> _mmesByType;
    private Map<String, RME> _rmesByType;
    private Map<String, PayoutManager> _payoutManagersByType;

    private boolean _started;

    //must be called when service starts
    public void start(Socket socketIoSocket)
    {
        //only start once
        if (_started) {
            return;
        }

        //loop each of the autowired fields and:
        //  convert to lookup map based on type
        //  call start

        _mqesByType = new HashMap<>(_mqes.length);
        for (MQE mqe : _mqes) {
            _mqesByType.put(mqe.getType(), mqe);
            mqe.start(socketIoSocket);
        }

        _mmesByType = new HashMap<>(_mmes.length);
        for (MME mme : _mmes) {
            _mmesByType.put(mme.getType(), mme);
            mme.start(socketIoSocket);
        }

        _rmesByType = new HashMap<>(_rmes.length);
        for (RME rme : _rmes) {
            _rmesByType.put(rme.getType(), rme);
            rme.start(socketIoSocket);
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
        for (MQE mqe : _mqes) {
            mqe.stop();
        }

        for (MME mme : _mmes) {
            mme.stop();
        }

        for (RME rme : _rmes) {
            rme.stop();
        }

        _started = false;
    }

    public void subscriberCancelledQueuing(Game game, long subscriberId)
    {
        _mqesByType.get(game.getEngineType()).subscriberCancelledQueuing(subscriberId);
    }

    public void runMQE(Game game)
    {
//_logger.debug("runMQE");
//_logger.debug("engineType: " + game.getEngineType());
        _mqesByType.get(game.getEngineType()).run(null);
    }

    public void notifyQuetionListChanged()
    {
        for (MME mme : _mmes) {
            mme.notifyQuetionListChanged();
        }
    }

    public float getPlayerPot(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout) throws PayoutManagerException
    {
        return getPayoutManager(game.getEngineType()).getPlayerPot(game, rounds, numPlayers, gamePayout);
    }

//    public Map<String, Object> generatePayoutTable(Game game, List<Round> rounds, int numPlayers)
//    {
//        return getPayoutManager(game.getEngineType()).generatePayoutTable(game, rounds, numPlayers);
//    }

    public List<PayoutTableRow> generateCollapsedPayoutTable(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout) throws PayoutManagerException
    {
        return getPayoutManager(game.getEngineType()).generateCollapsedPayoutTable(game, rounds, numPlayers, gamePayout);
    }

//    public List<PayoutTableRow> generateCollapsedPayoutTableFromPayoutTable(Game game, Map<String, Object> payoutTable)
//    {
//        return getPayoutManager(game.getEngineType()).generateCollapsedPayoutTableFromPayoutTable(payoutTable);
//    }

    public List<PayoutModelRound> getAdjustedPayoutModelRounds(Game game, int numPlayers, PayoutModel payoutModel, GamePayout gamePayout)
    throws PayoutManagerException
    {
        return getPayoutManager(game.getEngineType()).getAdjustedPayoutModelRounds(numPlayers, payoutModel, gamePayout);
    }

    public void killMMEProcess(String id)
    {
        for (MME mme : _mmes) {
            mme.killProcess(id);
        }
    }

    public int getNumberOfBracketRounds(Game game, int numPlayers)
    {
        return getPayoutManager(game.getEngineType()).getNumberOfBracketRounds(numPlayers);
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
