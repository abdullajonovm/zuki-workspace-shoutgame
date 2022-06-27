package tv.shout.snowyowl.engine;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutTableRow;

public abstract class BasePayoutManager
implements PayoutManager
{
    protected static class Payout
    {
        public int rank;
        public float amount;
        public String type;
        public String category;

        public Payout(int rank, float amount, String type, String category)
        {
            this.rank = rank;
            this.amount = amount;
            this.type = type;
            this.category = category;
        }

        @Override
        public String toString()
        {
            return MessageFormat.format("Rank: {0,number,#}, Amount: {1,number}, type: {2}, category: {3}", rank, amount, type, category);
        }
    }

    @Override
    public List<PayoutTableRow> generateCollapsedPayoutTable(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout)
    throws PayoutManagerException
    {
        Map<String, Object> payoutTable = generatePayoutTable(game, rounds, numPlayers, gamePayout);
        return generateCollapsedPayoutTableFromPayoutTable(payoutTable);
    }

    @Override
    public List<PayoutTableRow> generateCollapsedPayoutTableFromPayoutTable(Map<String, Object> payoutTable)
    {
        Map<String, Object> collapsedPayoutTable = new HashMap<>();
        collapsedPayoutTable.put("playerPot", payoutTable.get("playerPot"));

        List<PayoutTableRow> rows = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Payout> payouts = (List<Payout>) payoutTable.get("payouts");
        float lastPayoutValue = -1F;
        int firstRank = -1;
        int lastRank = -1;
        String lastType = null;
        String lastCategory = null;

        //for (PayoutTuple pt : payoutTuples) {
        for (Payout payout : payouts) {
            int curRank = payout.rank;
            float curPayout = payout.amount;

            //if this is a new value, save the old row (unless lastPayoutValue == -1F) and start a new one
            if (curPayout != lastPayoutValue) {
                if (lastPayoutValue != -1F) {
                    rows.add(new PayoutTableRow(firstRank, lastRank, lastPayoutValue, payout.type, payout.category, UUID.randomUUID().toString()));
                }

                firstRank = curRank;
                lastPayoutValue = curPayout;
            }

            lastRank = curRank;
            lastType = payout.type;
            lastCategory = payout.category;
        }

        //save the final row
        rows.add(new PayoutTableRow(firstRank, lastRank, lastPayoutValue, lastType, lastCategory, UUID.randomUUID().toString()));

        return rows;
    }

    protected float getPurse(List<Round> rounds)
    {
        if (rounds == null || rounds.size() == 0) return 0F;

        //sort the rounds by sequence
        Collections.sort(rounds, new Comparator<Round>()
        {
            @Override
            public int compare(Round lhs, Round rhs)
            {
                return lhs.getRoundSequence() < rhs.getRoundSequence() ? -1 : 1;
            }
        });

        //the purse always comes from the final round
        Double dpurse = rounds.get(rounds.size()-1).getRoundPurse();
        double purse = dpurse == null ? 0D : dpurse;

        return (float) purse;
    }

    protected float getCostToJoin(List<Round> rounds)
    {
        if (rounds == null || rounds.size() == 0) return 0F;

        //sort the rounds by sequence
        Collections.sort(rounds, new Comparator<Round>()
        {
            @Override
            public int compare(Round lhs, Round rhs)
            {
                return lhs.getRoundSequence() < rhs.getRoundSequence() ? -1 : 1;
            }
        });

        //the cost to join always comes from the first round
        Double dCostToJoin = rounds.get(0).getCostPerPlayer();
        double costToJoin = dCostToJoin == null ? 0D : dCostToJoin;

        return (float) costToJoin;
    }

}
