package tv.shout.reactive;

import java.util.List;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutTableRow;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.snowyowl.engine.PayoutManagerException;
import tv.shout.snowyowl.engine.fixedround.PayoutManagerFixedRoundSingleLife;

public class MockEngineCoordinator
extends EngineCoordinator
{
    private PayoutManagerFixedRoundSingleLife _payoutManager;

    public MockEngineCoordinator(PayoutManagerFixedRoundSingleLife payoutManager)
    {
        _payoutManager = payoutManager;
    }

    @Override
    public int getNumberOfBracketRounds(Game game, int numPlayers)
    {
        return _payoutManager.getNumberOfBracketRounds(numPlayers);
    }

    @Override
    public List<PayoutModelRound> getAdjustedPayoutModelRounds(Game game, int numPlayers, PayoutModel payoutModel, GamePayout gamePayout)
    throws PayoutManagerException
    {
        return _payoutManager.getAdjustedPayoutModelRounds(numPlayers, payoutModel, gamePayout);
    }

    @Override
    public List<PayoutTableRow> generateCollapsedPayoutTable(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout)
    throws PayoutManagerException
    {
        return _payoutManager.generateCollapsedPayoutTable(game, rounds, numPlayers, gamePayout);
    }
}
