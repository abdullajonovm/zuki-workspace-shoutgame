package tv.shout.snowyowl.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutTableRow;

public interface PayoutManager
{
    String getType();

    float getPlayerPot(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout) throws PayoutManagerException;

    Map<String, Object> generatePayoutTable(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout) throws PayoutManagerException;

    List<PayoutTableRow> generateCollapsedPayoutTable(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout) throws PayoutManagerException;

    List<PayoutTableRow> generateCollapsedPayoutTableFromPayoutTable(Map<String, Object> payoutTable);

    List<PayoutModelRound> getAdjustedPayoutModelRounds(int numPlayers, PayoutModel payoutModel, GamePayout gamePayout)
    throws PayoutManagerException;

    int getNumberOfBracketRounds(int numPlayers);

    public default double getNetProfitPerPlayer(int numActualPlayersWhoPaidMoney, PayoutModel pm, GamePayout gp)
    throws PayoutManagerException
    {
        double income = numActualPlayersWhoPaidMoney * pm.getEntranceFeeAmount();
        List<PayoutModelRound> adjustedPayoutModelRounds = getAdjustedPayoutModelRounds(numActualPlayersWhoPaidMoney, pm, gp);

        List<Float> payouts = new ArrayList<>(1);
        payouts.add(0F);
        adjustedPayoutModelRounds.forEach(pmr -> {
            float roundPayout = ( pmr.getEliminatedPlayerCount() * pmr.getEliminatedPayoutAmount());
            float previousPayout = payouts.get(0);
            payouts.set(0, roundPayout + previousPayout);
        });

        double weAreMakingThisMuch = income - payouts.get(0);
        double netProfitPerPlayer;
        if (income * pm.getEntranceFeeAmount() == 0) {
            netProfitPerPlayer = 0.0D;
        } else {
            netProfitPerPlayer = weAreMakingThisMuch / income * pm.getEntranceFeeAmount();
        }

        return netProfitPerPlayer;
    }

    void assignPayouts(
            Game game, List<Round> rounds, Map<Long, GamePlayer> gamePlayersMap, List<RoundPlayer> players,
            Set<Long> botIdsForGame, Set<Long> sponsorPlayerIdsForGame, Set<Long> testSubscribers, GamePayout gamePayout)
    throws PayoutManagerException;

}
