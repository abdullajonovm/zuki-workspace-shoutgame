package tv.shout.sm.test;

public class BrucePayoutManager
//extends BasePayoutManager
{
//    @Override
//    public ENGINE_TYPE getType()
//    {
//        return Game.ENGINE_TYPE.VARIABLE_ROUND;
//    }
//
//    @Override
//    public Map<String, Object> generatePayoutTable(Map<String, Object> payoutManagerData)
//    {
//        Game game = (Game) payoutManagerData.get("game");
//        @SuppressWarnings("unchecked")
//        List<Round> rounds = (List<Round>) payoutManagerData.get("rounds");
//        int numPlayers = (Integer) payoutManagerData.get("numPlayers");
//
//        //how much money will be paid (up to this much. will be less with rounding)
//        float playerPot = getPlayerPot(payoutManagerData);
//        float amountLeftAfterPayouts = playerPot;
//
//        //how many players will be getting a payout
//        int playerAwardCount = (int) (numPlayers * (game.getPayoutPercentageOfUsersToAward()/100F));
//
//        //the return structure
//        List<PayoutTuple> payouts = new ArrayList<>();
//
//        //figure out how many bracket rounds, based on the number of players
//        int numPlayersLeft = numPlayers;
//        int numBracketRounds = 0;
//        int numBotsAdded = 0;
//        int numRounds = 1;
//        List<Integer> roundEliminations = new ArrayList<>();
//
//        while (numPlayersLeft > 2) {
//            numRounds++;
//
//            //make sure the number of players is even
//            if (numPlayersLeft % 2 != 0) {
//                numBotsAdded++;
//                numPlayersLeft++;
//            }
//
//            //divide by 2 to find out how many get eliminated this round
//            numPlayersLeft /= 2;
//
//            roundEliminations.add(0, numPlayersLeft);
//        }
//
//        //add in the final round
//        roundEliminations.add(0, 2);
//
////        //walk backwards to see how many rounds will get payouts
////        int bottomRoundReceivingPayouts = roundEliminations.size(); //i.e. Round 7 of 7
////        int hypotheticalPayoutsRemaining = playerAwardCount;
////        boolean partialRoundPayouts = false;
////
////        for (int numEliminatedThisRound : roundEliminations) {
////            if (hypotheticalPayoutsRemaining > numEliminatedThisRound) {
////                hypotheticalPayoutsRemaining -= numEliminatedThisRound;
////
////                bottomRoundReceivingPayouts--;
////            } else if (hypotheticalPayoutsRemaining == numEliminatedThisRound) {
////                partialRoundPayouts = false;
////
////            } else { // less than
////                partialRoundPayouts = true;
////            }
////        }
//
//        //start assigning payouts
//        int numPayoutsRemaining = playerAwardCount;
//
//        //assign any top down hardcoded payouts
//        float totalAssignedPayouts = 0F;
//        if (game.getAssignedPayoutsFromTop() != null) {
//            for (Payout payout : game.getAssignedPayoutsFromTop()) {
//
//                //edge case: there are less players than there are hardcoded payouts
//                if (payouts.size() >= numPlayers) break;
//
//                switch (payout.getPayoutType())
//                {
//                    case "CASH": {
//                        PayoutTuple val = new PayoutTuple();
//                        val.setKey(payouts.size()+1);
//                        val.setVal(payout.getPayoutAmount());
//                        payouts.add(val);
//                        totalAssignedPayouts += payout.getPayoutAmount();
//
//                        numPayoutsRemaining--;
//                        amountLeftAfterPayouts -= payout.getPayoutAmount();
//                    }
//                    break;
//
//                    default:
//                        throw new IllegalArgumentException("unknown payout type for top payouts: " + payout.getPayoutType() + ", allowed types are: 'CASH'");
//                }
//            }
//        }
//
//        //find the top payout round (i.e. after the hardcoded values are paid, what round do the automated payouts start at)
//        int topEliminatedIdx = -1;
//        int numPayoutsRemainingInTopRound = 0;
//        int numPaidOutNotAccountedFor = payouts.size();
//        for (int numEliminationsInRound : roundEliminations) {
//            topEliminatedIdx++;
//
//            if (numPaidOutNotAccountedFor > 0) {
//                if (numPaidOutNotAccountedFor < numEliminationsInRound) {
//                    numPayoutsRemainingInTopRound = numEliminationsInRound - numPaidOutNotAccountedFor;
//                    break;
//
//                } else if (numPaidOutNotAccountedFor == numEliminationsInRound) {
//                    topEliminatedIdx++;
//                    break;
//
//                } else { //>
//                    numPaidOutNotAccountedFor -= numEliminationsInRound;
//                }
//            } else {
//                break;
//            }
//        }
//
//        //find the bottom payout round (where the entire round can be paid out)
//        int bottomEliminatedIdx = topEliminatedIdx;
//        int numPayoutsRemainingProvisional = numPayoutsRemaining;
//        for (int i=topEliminatedIdx; i<roundEliminations.size(); i++) {
//            int numEliminationsInRound = roundEliminations.get(i);
//
//            if (numPayoutsRemainingInTopRound > 0) {
//                numEliminationsInRound -= numPayoutsRemainingInTopRound;
//                numPayoutsRemainingInTopRound = 0;
//            }
//
//            if (numPayoutsRemainingProvisional > numEliminationsInRound) {
//                numPayoutsRemainingProvisional -= numEliminationsInRound;
//                bottomEliminatedIdx++;
//            } else if (numPayoutsRemainingProvisional == numEliminationsInRound) {
//                break;
//            } else { //<
//                bottomEliminatedIdx--;
//                break;
//            }
//        }
//
//        //TODO: knowing how many rounds get paid out, divvy up an even amount to each round and divide by number of players in eacn round; save off rounding errors
//
//        //TODO: at end, if rounding errors are large enough to distribute $1 to each winner, give them each $1, and keep doing that until there's less than $1 to distribute among the winners
//        // but don't use $1, use the game.minimumPayoutAmount value
//
//        //TODO: at the end, make note of the remaining amount
//
////        //walk backwards to see how which round starts automated payouts after the hardcoded tops are assigned
////        int topRoundReceivingPayouts = roundEliminations.size(); //i.e. Round 7 of 7
////        int whileThisIsGreaterThan0KeepGoing = game.getAssignedPayoutsFromTop().size();
////        for (int numPayoutsThisRound : roundEliminations) {
////            if (whileThisIsGreaterThan0KeepGoing <= 0) {
////                break;
////            }
////            whileThisIsGreaterThan0KeepGoing -= numPayoutsThisRound;
////            topRoundReceivingPayouts--;
////        }
////
////        //how many rounds get automated payouts, and how much is it for each round
////        float roundPayout = 0F;
////        int numRoundsWithAutomatedPayouts = topRoundReceivingPayouts - bottomRoundReceivingPayouts;
////        if (numRoundsWithAutomatedPayouts > 0) {
////            roundPayout = (playerPot - totalAssignedPayouts) / numRoundsWithAutomatedPayouts;
////        }
////
////        //take each round with an automated payout and assign values
////        for (int roundNum = roundEliminations.size() - (roundEliminations.size() - topRoundReceivingPayouts) ; roundNum > bottomRoundReceivingPayouts; roundNum--) {
////            int numPayoutsToAssignThisRound = roundEliminations.get(roundEliminations.size() - roundNum);
////
////            //if one or more of the hardcoded top payouts ate into this round, take that into account
////            if (whileThisIsGreaterThan0KeepGoing < 0) {
////                numPayoutsToAssignThisRound += whileThisIsGreaterThan0KeepGoing;
////                whileThisIsGreaterThan0KeepGoing = 0;
////            }
////
////            //find how much each person in this round gets (by doing int math, it will chop it to a whole dollar amount)
////            int individualPayoutAmount = (int) (roundPayout / numPayoutsToAssignThisRound);
////
////            //assign the payouts
////            for (int i=0; i<numPayoutsToAssignThisRound; i++) {
////                PayoutTuple val = new PayoutTuple();
////                val.setKey(payouts.size()+1);
////                val.setVal((float) individualPayoutAmount);
////                payouts.add(val);
////                //totalAssignedPayouts++;
////                numPayoutsRemaining--;
////
////                amountLeftAfterPayouts -= individualPayoutAmount;
////            }
////
////            //System.out.println(numPayoutsToAssignThisRound + ": " + individualPayoutAmount);
////        }
////
////        if (numPayoutsRemaining > 0) {
////            //assign anyone remaining the minimum amount
////            for (int i=0; i<numPayoutsRemaining; i++) {
////                PayoutTuple val = new PayoutTuple();
////                val.setKey(payouts.size()+1);
////                val.setVal(game.getMinimumPayoutAmount());
////                payouts.add(val);
////                //totalAssignedPayouts++;
////                //numPayoutsRemaining--;
////
////                amountLeftAfterPayouts -= game.getMinimumPayoutAmount();
////            }
////        }
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("playerPot", playerPot);
//        map.put("payouts", payouts);
//        map.put("amountLeftAfterPayouts", amountLeftAfterPayouts);
//
//        return map;
//    }
//
//    @Override
//    public float getPlayerPot(Map<String, Object> payoutManagerData)
//    {
//        Game game = (Game) payoutManagerData.get("game");
//        @SuppressWarnings("unchecked")
//        List<Round> rounds = (List<Round>) payoutManagerData.get("rounds");
//        int numPlayers = (Integer) payoutManagerData.get("numPlayers");
//
//        float playerPot;
//        switch (game.getPayoutCalculationMethod())
//        {
//            case DYNAMIC:
//                float gameRevenue = numPlayers * getCostToJoin(rounds);
//                playerPot = gameRevenue - ((game.getPayoutHouseTakePercentage()/100F) * gameRevenue);
//                break;
//
//            //case STATIC:
//            default:
//                playerPot = getPurse(rounds);
//                break;
//        }
//
//        return playerPot;
//    }
//
//
//    @Override
//    public void assignPayouts(Map<String, Object> payoutManagerData, Map<Integer, GamePlayer> gamePlayersMap,
//            List<RoundPlayer> players, IShoutContestService shoutContestService, List<Integer> botIdsForGame,
//            List<Integer> testSubscribers)
//    {
//        Game game = (Game) payoutManagerData.get("game");
//        @SuppressWarnings("unchecked")
//        List<Round> rounds = (List<Round>) payoutManagerData.get("rounds");
//
//        //convert bot id list to lookup set
//        Set<Integer> botLookup = new HashSet<>(botIdsForGame);
//        for (int botSubId : botIdsForGame) {
//            botLookup.add(botSubId);
//        }
//
//        //convert test subscriber id list to lookup set
//        Set<Integer> testSubscriberLookup = new HashSet<>(testSubscribers);
//        for (int sId: testSubscribers) {
//            testSubscriberLookup.add(sId);
//        }
//
//        int numPlayers = players.size();
//
//        Map<String, Object> payoutMap = generatePayoutTable(payoutManagerData);
//        float amountLeftAfterPayouts = (float) payoutMap.get("amountLeftAfterPayouts");
//
//        //sort players by rank, high to low
//        players = players.stream()
//            .sorted( Comparator.comparing(RoundPlayer::getRank, Comparator.nullsLast(Comparator.reverseOrder())) )
//            .collect(Collectors.toList());
//
//        //assign the payouts
//        @SuppressWarnings("unchecked")
//        List<PayoutTuple> payouts = (List<PayoutTuple>) payoutMap.get("payouts");
//        for (int i=0; i<payouts.size(); i++) {
//            PayoutTuple payout = payouts.get(i);
//            float playerPayoutAmount = (float) payout.getVal();
//            RoundPlayer roundPlayer = players.get(i);
//
//            //is the winner a bot
//            if (botLookup.contains(roundPlayer.getSubscriberId())) {
//                //TODO: waiting for definition of what to do here: log it, add to db? etc
//                amountLeftAfterPayouts += playerPayoutAmount;
//                continue;
//            }
//
////_logger.info(MessageFormat.format("PAYOUT ASSIGNED TO: {0}, rank: {1}, payout%: {2}, amount: {3}", roundPlayer.getSubscriberId(), (i+1), payoutPercentage, playerPayoutAmount));
//
//            //update the objects and db with the payout information
//            roundPlayer.setAmountPaid((double)playerPayoutAmount);
//            shoutContestService.updateRoundPlayer(roundPlayer);
//
//            GamePlayer gamePlayer = gamePlayersMap.get(roundPlayer.getSubscriberId());
//            gamePlayer.setPayoutAwardedAmount((double)playerPayoutAmount);
//            gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.AWARDED);
//            shoutContestService.updateGamePlayer(gamePlayer);
//
//            //mark the subscriber as having won in the transaction table, UNLESS they are a test subscriber (i.e. ios test user using the app for their testing)
//            if (!testSubscriberLookup.contains(roundPlayer.getSubscriberId())) {
//                shoutContestService.addCashPoolTransaction(
//                        roundPlayer.getSubscriberId(), playerPayoutAmount, CashPoolTransaction2.TYPE.PAYOUT, null, null, game.getId());
//            }
//
////TODO: put back
//            //send a push
////            Map<String, Object> extras = new HashMap<>();
////            extras.put("amount", playerPayoutAmount);
////            sendGamePush(
////                _transactionManager, _dao, _pushService, _logger, roundPlayer.getSubscriberId(), game,
////                "apsCategory", "apsMessage", "SM_PAYOUT_NOTIFICATION", extras
////            );
//        }
//
//        if (amountLeftAfterPayouts > 0) {
//            //TODO: do something with the leftover amount ? bruce is working with darl on deciding what to do
//        }
//    }
//
//    public String payoutSanityCheck(Map<String, Object> payoutTableMap)
//    {
//        @SuppressWarnings("unchecked")
//        List<PayoutTuple> payouts = (List<PayoutTuple>) payoutTableMap.get("payouts");
//
//        float amountLeftAfterPayouts = (float) payoutTableMap.get("amountLeftAfterPayouts");
//
//        if (amountLeftAfterPayouts < 0) {
//            return "FAILED SANITY CHECK: too much money paid out. try reducing hardcoded amounts at the top, reducing the number of people getting payouts, decreasing the house take percentage, or decreasing the minimum payout amount";
//        }
//
//        if (payouts != null && payouts.size() > 0) {
//            float lastPayoutVal = (float) payouts.get(0).getVal();
//            for (PayoutTuple pt : payouts) {
//                float payoutVal = (float) pt.getVal();
//                if (payoutVal > lastPayoutVal) {
//                    return "FAILED SANITY CHECK: some lower payouts are for more than the higher payouts";
//                }
//                lastPayoutVal = payoutVal;
//            }
//        }
//
//        return "PASSED SANITY CHECK";
//    }
//
//    public static void main2(String[] args)
//    {
//        BrucePayoutManager m = new BrucePayoutManager();
//        //MockShoutContestService shoutContestService = new MockShoutContestService((MockShoutContestServiceDaoMapper)null);
//
//        Game game = new Game();
//        game.setPayoutCalculationMethod(Game.PAYOUT_CALCULATION_METHOD.STATIC);
//        //game.setPayoutHouseTakePercentage(10F); set this if type==DYNAMIC
//        game.setPayoutPercentageOfUsersToAward(50F);
//
//        game.setAssignedPayoutsFromTop(Arrays.asList(new Payout("CASH", 100F), new Payout("CASH", 50F)));
//        game.setMinimumPayoutAmount(1F);
//
//        Round bracketRound = new Round();
//        bracketRound.setRoundType(Round.ROUND_TYPE.BRACKET);
//        bracketRound.setRoundPurse(500D);
//
//        int maxPlayers = 10;
//
//        List<Round> rounds = Arrays.asList(bracketRound);
//
//        System.out.println(MessageFormat.format(
//            "payoutMethod: {0}, % of users to award: {1}, maxPlayers: {2}, houseTake%: {3}",
//            game.getPayoutCalculationMethod(), game.getPayoutPercentageOfUsersToAward(), maxPlayers, game.getPayoutHouseTakePercentage()));
//        System.out.println("===============================================\n");
//
//        Map<String, Object> payoutManagerData = new HashMap<>();
//        payoutManagerData.put("game", game);
//        payoutManagerData.put("rounds", rounds);
//
//        for (int i=0; i<=maxPlayers; i++) {
//            payoutManagerData.put("numPlayers", i);
//            Map<String, Object> payoutTableMap = m.generatePayoutTable(payoutManagerData);
//            List<PayoutTuple> payouts = (List<PayoutTuple>) payoutTableMap.get("payouts");
//            float amountLeftAfterPayouts = (float) payoutTableMap.get("amountLeftAfterPayouts");
//            String sanityCheckResult = m.payoutSanityCheck(payoutTableMap);
//
//            System.out.println(MessageFormat.format(
//                "#players: {0}\n$left after payouts: {1}\npayouts: {2}\nsanity check: {3}\n",
//                i, amountLeftAfterPayouts, payouts, sanityCheckResult));
//        }
//    }
//
//    public static void main(String[] args)
//    {
//        List<Integer> eliminationsByRound = Arrays.asList(2,2,3,5);
//        int numPaidOutNotAccountedFor = 1; //# of top hardcoded payouts
//        int numPayoutsRemaining = 4;
//
//        //find the top payout index
//        int topEliminatedIdx = -1;
//        int numPayoutsRemainingInTopRound = 0;
//        for (int numEliminationsInRound : eliminationsByRound) {
//            topEliminatedIdx++;
//
//            if (numPaidOutNotAccountedFor > 0) {
//                if (numPaidOutNotAccountedFor < numEliminationsInRound) {
//                    numPayoutsRemainingInTopRound = numEliminationsInRound - numPaidOutNotAccountedFor;
//                    break;
//
//                } else if (numPaidOutNotAccountedFor == numEliminationsInRound) {
//                    topEliminatedIdx++;
//                    break;
//
//                } else { //>
//                    numPaidOutNotAccountedFor -= numEliminationsInRound;
//                }
//            } else {
//                break;
//            }
//        }
//
//        //System.out.println("topEliminatedIdx [1]: " + topEliminatedIdx + ", numPayoutsRemainingInTopRound [2]: " + numPayoutsRemainingInTopRound);
//
//        numPayoutsRemaining-=1; //top 2 spots paid out
//
//        //find the bottom payout round (where the entire round can be paid out)
//        int bottomEliminatedIdx = topEliminatedIdx;
//        int numPayoutsRemainingProvisional = numPayoutsRemaining;
//        for (int i=topEliminatedIdx; i<eliminationsByRound.size(); i++) {
//            int numEliminationsInRound = eliminationsByRound.get(i);
//
//            if (numPayoutsRemainingInTopRound > 0) {
//                numEliminationsInRound -= numPayoutsRemainingInTopRound;
//                numPayoutsRemainingInTopRound = 0;
//            }
//
//            if (numPayoutsRemainingProvisional > numEliminationsInRound) {
//                numPayoutsRemainingProvisional -= numEliminationsInRound;
//                bottomEliminatedIdx++;
//            } else if (numPayoutsRemainingProvisional == numEliminationsInRound) {
//                break;
//            } else { //<
//                bottomEliminatedIdx--;
//                break;
//            }
//        }
//
//        System.out.println("bottomEliminatedIdx: [1]: " + bottomEliminatedIdx);
//    }
}




/*

PHASE 0: do these "smaller" feature requests first as they come in

x republish the payout table every time someone joins or leaves the game

x add new game field: boolean:fillWithBots. discontinue use of existing property file value and either go all or nothing

x add a new game field (boolean:pairImmediately)) that determines whether or not pairing waits for another human, or just immediately assigns a bot (but only for POOL play)

* new game flag to allow filling up bots to a certain amount (i.e. if game allows 1000, but only want to test with 100, for example): game.maxBotFillCount

* more api's around the management of PayoutModels

* vantiv integration

--------------------

x PHASE 1: deal with payout changes (but still assume everything is cash)

PHASE 2: add in virtual payouts

modify payout to be a structure with a "type" (cash, creds, motorcycle, etc), and an amount ($1,000,000.00, 10,000, 1)

add in ability to specify virtual payouts at both the top (winner gets a motorcycle, everyone else gets cash), AND coming in at the bottom (losers get a warm fuzzy AND 200 shout creds if they made it to round 2, even though cash payouts didn't kick in until round 3)


PHASE 3: extra life

hard code in the one known type of "extra life" - an extra tie-breaker question on a match if a user chooses to use their life

i forsee this WILL cause future pain since darl is going to come up with 15 other types of powerups he wants and it'll get messy.


PHASE 4: better question management

ability to reserve some amount (or percentage) of questions "until the end rounds"

question lists to prevent people from seeing the same question more than once (unless it's impossible since they've seen them all)

PHASE 5: templating

associate a saved payout structure with a game
...



*/