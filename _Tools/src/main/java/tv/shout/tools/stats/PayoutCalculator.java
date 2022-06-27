package tv.shout.tools.stats;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.meinc.gameplay.domain.Tuple;

public class PayoutCalculator
{
//    //https://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
//    public static int log2(int n)
//    {
//        if (n <= 0) throw new IllegalArgumentException();
//        return 31 - Integer.numberOfLeadingZeros(n);
//    }

    public static class RoundPlayer
    {
        public int subscriberId;
        public Double rank;
        public double payout;

        public RoundPlayer(int subscriberId, double rank)
        {
            this.subscriberId = subscriberId;
            this.rank = rank;
        }
    }

    @SuppressWarnings("serial")
    public static class PayoutTuple
    extends Tuple<Number>
    {
        @Override
        public String toString()
        {
            return MessageFormat.format("Rank: {0,number,#}, Payout: {1,number,currency}", getKey(), getVal());
        }
    }

    //call this as soon as the number of players becomes locked (i.e. when bracket play begins)
    public List<Float> calculatePayoutPercentages(int numPlayers, float percentOfPlayersToAward)
    {
        List<Float> playerPercentages = new ArrayList<>(1);
        playerPercentages.add(100F);

        int playerAwardCount = (int) (numPlayers * percentOfPlayersToAward);

        //expand the percentages array until all payout amounts are determined
        while (playerPercentages.size() < playerAwardCount) {
            List<Float> newPlayerPercentages = new ArrayList<>();
            for (int i=playerPercentages.size()-1; i>=0; i--) {
                float percentage = playerPercentages.get(i);
                float newPercentage = percentage * percentOfPlayersToAward;
                newPlayerPercentages.add(0, newPercentage);
                newPlayerPercentages.add(0, percentage - newPercentage);
            }
            playerPercentages = newPlayerPercentages;
        }

        //reverse sort to get high to low
        Collections.sort(playerPercentages, new Comparator<Float>() {
            @Override
            public int compare(Float lhs, Float rhs)
            {
                return rhs.compareTo(lhs);
            }
        });

        //trim down to the correct size (it will always be a power of 2 due to the nature of the loop, so it will probably be too large)
        if (playerPercentages.size() > playerAwardCount) {
            List<Float> newPlayerPercentages = new ArrayList<>(playerAwardCount);
            for (int i=0; i<playerAwardCount; i++) {
                newPlayerPercentages.add(playerPercentages.get(i));
            }
            playerPercentages = newPlayerPercentages;
        }

        return playerPercentages;
    }

    public Map<String, Object> generatePayoutTable(float playerPot, List<Float> playerPercentages)
    {
        Map<String, Object> map = new HashMap<>();
        List<PayoutTuple> payouts = new ArrayList<>(playerPercentages.size());
        map.put("playerPot", playerPot);
        map.put("payouts", payouts);

        for (int i=0; i<playerPercentages.size(); i++) {
            float payoutPercentage = playerPercentages.get(i) / 100F;
            float playerPayoutAmount = playerPot * payoutPercentage;

            PayoutTuple val = new PayoutTuple();
            val.setKey(i+1);
            val.setVal(playerPayoutAmount);

            payouts.add(val);
        }

        return map;
    }

    public void assignPayouts(float playerPot, List<RoundPlayer> players, List<Float> playerPercentages)
    {
        //sort by rank, high to low
        Collections.sort(players, new Comparator<RoundPlayer>() {
            @Override
            public int compare(RoundPlayer lhs, RoundPlayer rhs)
            {
                return rhs.rank.compareTo(lhs.rank);
            }
        });

        for (int i=0; i<playerPercentages.size(); i++) {
            RoundPlayer sortedPlayer = players.get(i);
            float payoutPercentage = playerPercentages.get(i) / 100F;
            float playerPayoutAmount = playerPot * payoutPercentage;
            sortedPlayer.payout = playerPayoutAmount;
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        PayoutCalculator calculator = new PayoutCalculator();

        //payout dials set at game creation
        float houseTake = .3F;
        float costPerPlayer = 5F;
        float percentOfPlayersToAward = .7F;

        //variable based on users (just make sure it's even, which is what gameplay requires; if not, bots are added until it is even)
        int numPlayers = 24;

        //calculated
        float gameRevenue = numPlayers * costPerPlayer;
        float playerPot = gameRevenue - (houseTake * gameRevenue);

        System.out.println("revenue: " + gameRevenue);
        System.out.println("player pot: " + playerPot);

        List<Float> payoutPercentages = calculator.calculatePayoutPercentages(numPlayers, percentOfPlayersToAward);
        System.out.println(payoutPercentages);

        Map<String, Object> payoutTable = calculator.generatePayoutTable(playerPot, payoutPercentages);
        System.out.println(payoutTable);

//        int numRounds = log2(numPlayers);
//        System.out.println("# of rounds: " + numRounds);

        Random r = new Random();
        double rangeMin = 0d;
        double rangeMax = 1000d;
        List<RoundPlayer> players = new ArrayList<>(numPlayers);
        for (int i=0; i<numPlayers; i++) {
            double rank = rangeMin + (rangeMax - rangeMin) * r.nextDouble();;
            players.add(new RoundPlayer(i, rank));
        }

        calculator.assignPayouts(playerPot, players, payoutPercentages);

        for (RoundPlayer rp : players) {
            System.out.println("sid: " + rp.subscriberId + ", rank: " + rp.rank + ", payout: " + rp.payout);
        }

    }
}
