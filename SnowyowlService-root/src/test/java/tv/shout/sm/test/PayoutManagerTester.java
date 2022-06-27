package tv.shout.sm.test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;

public class PayoutManagerTester
{
//    private void checkPayoutModelForConsistency(PayoutModel pm)
//    throws PayoutManagerException
//    {
//        int numPlayers = pm.getBasePlayerCount();
//        List<PayoutModelRound> rounds = VariableRoundPayoutManager.getDefaultPayoutModelRounds(numPlayers);
//        //Collections.reverse(rounds);
//        System.out.println("SERVER ROUNDS:");
//        rounds.stream().forEach(r -> System.out.println("\t" + r));
//
//        System.out.println("CLIENT ROUNDS:");
//        pm.getPayoutModelRounds().stream().forEach(r -> System.out.println("\t" + r));
//
//        //make sure the number of rounds is the same
//        if (rounds.size() != pm.getPayoutModelRounds().size()) {
//            throw new PayoutManagerException("incorrect number of rounds", null);
//        }
//
//        int lastClientStartingPlayerCount = -1;
//
//        //for each round, make sure that the starting/eliminated player count is the same
//        for (int i=0; i<rounds.size(); i++) {
//            PayoutModelRound generatedRound = rounds.get(i);
//            PayoutModelRound clientRound = pm.getPayoutModelRounds().get(i);
//
//            if (clientRound.getStartingPlayerCount() < lastClientStartingPlayerCount) {
//                throw new PayoutManagerException(MessageFormat.format("starting player count for round {0}, is smaller than last round player count", i+1), null) ;
//            }
//            lastClientStartingPlayerCount = clientRound.getStartingPlayerCount();
//
//            if (generatedRound.getStartingPlayerCount() != clientRound.getStartingPlayerCount()) {
//                throw new PayoutManagerException(MessageFormat.format("starting player counts do not match for round {0}, should be {1,number,#}", i+1, generatedRound.getStartingPlayerCount()), null) ;
//            }
//
//            if (generatedRound.getEliminatedPlayerCount() != clientRound.getEliminatedPlayerCount()) {
//                throw new PayoutManagerException(MessageFormat.format("eliminated player counts do not match for round {0}, should be {1,number,#}", i+1, generatedRound.getEliminatedPlayerCount()), null) ;
//            }
//        }
//    }

    public static void main2(String[] args)
    throws Exception
    {
        float minPayoutAmount = 1F;
        int numPlayers = 1;
        int payoutModelId = 14;

        GamePayout gp = new GamePayout();
        gp.setPayoutModelId(payoutModelId);
        gp.setMinimumPayoutAmount(minPayoutAmount);
        MockVariableRoundPayoutManager payoutManager = new MockVariableRoundPayoutManager();
        PayoutModel pm = payoutManager.getPayoutModel(gp);

        //make sure that the given payout table passes the internal sanity checks
        //PayoutManagerTester tester = new PayoutManagerTester();
        //tester.checkPayoutModelForConsistency(pm);

        //see if the given payout table scales properly
        float income = numPlayers * pm.getEntranceFeeAmount();
        List<Float> payouts = new ArrayList<>(1);
        payouts.add(0F);
        List<PayoutModelRound> adjustedPayoutModelRounds = payoutManager.getAdjustedPayoutModelRounds(numPlayers, pm, gp);

        System.out.println(MessageFormat.format(
            "PayoutModel: {0}, entranceFee: ${1}, minPayoutAmount: ${2}, #players: {3}",
            pm.getName(), pm.getEntranceFeeAmount(), minPayoutAmount, numPlayers));

        adjustedPayoutModelRounds.forEach(pmr -> {
            System.out.println("\t" + pmr);
            float roundPayout = ( pmr.getEliminatedPlayerCount() * pmr.getEliminatedPayoutAmount());
            float previousPayout = payouts.get(0);
            payouts.set(0, roundPayout + previousPayout);
        });

        double netProfitPerPlayer = payoutManager.getNetProfitPerPlayer(numPlayers, pm, gp);

        System.out.println(MessageFormat.format(
            "income: ${0}, payout: ${1}, payout: {2}%, netProfitPerPlayer: ${3}",
            income, payouts.get(0), (payouts.get(0)/income)*100, netProfitPerPlayer));


        //Map<String, Object> map = JsonUtil.getObjectMapper().convertValue(pm, new TypeReference<Map<String, Object>>() {});
        //System.out.println(MessageFormat.format("{0}", payoutTable));

        //Game game = new Game();
        //game.setEngineType(Game.ENGINE_TYPE.VARIABLE_ROUND);
        //Round round = new Round();
        //List<Round> rounds = Arrays.asList(round);
        //List<PayoutTableRow> result = payoutManager.generateCollapsedPayoutTable(game, rounds, expectedNumPlayers, gamePayout);
        //result.forEach(System.out::println);


    }

    public static void main(String[] args)
    {
        Integer mintParentId = 2241;
        int affiliatePlanId = 4;
        float percentToGiveToAffiliate = 0.0F;
        String nickname = "aidanf";

        System.out.println(MessageFormat.format(
            "affiliatePayout::affiliate {0,number,#} via affiliatePlanId {1,number,#} is getting initialPayoutPct of {2} due to subscriber {3} ",
            mintParentId == null ? -1 : mintParentId, affiliatePlanId, percentToGiveToAffiliate, nickname));

    }
}
