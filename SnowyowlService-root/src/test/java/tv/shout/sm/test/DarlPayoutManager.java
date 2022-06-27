package tv.shout.sm.test;

public class DarlPayoutManager
//extends BasePayoutManager
//implements PushSender
{
//    private static Logger _logger = Logger.getLogger(DarlPayoutManager.class);
//
//    @Autowired
//    private PlatformTransactionManager _transactionManager;
//
//    @Autowired
//    private IDaoMapper _dao;
//
//    @Autowired
//    private IPushService _pushService;
//
//    @Autowired
//    private IShoutContestService _shoutContestService;
//
//    @Override
//    public ENGINE_TYPE getType()
//    {
//        return Game.ENGINE_TYPE.VARIABLE_ROUND;
//    }
//
//    @Override
//    public Map<String, Object> generatePayoutTable(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout)
//    {
//        //grab the payout model being used for this game
//        PayoutModel payoutModel = getPayoutModel(gamePayout);
//
//        //get the adjusted payout model
//        List<PayoutModelRound> adjustedPayoutModelRounds = getAdjustedPayoutModelRounds(numPlayers, payoutModel, gamePayout);
//
//        Map<String, Object> map = new HashMap<>();
//        List<PayoutTuple> payouts = new ArrayList<>();
//        map.put("playerPot", getPlayerPotFromPayoutModel(adjustedPayoutModelRounds));
//        map.put("payouts", payouts);
//
//        //build up the payouts map
//        int payoutPosition = 0;
//        for (PayoutModelRound pmr : adjustedPayoutModelRounds) {
//            int numPayoutsThisRound = pmr.getEliminatedPlayerCount();
//            for (int i=0; i<numPayoutsThisRound; i++) {
//                payoutPosition++;
//                float payoutAmount = pmr.getEliminatedPayoutAmount();
//                PayoutTuple val = new PayoutTuple();
//                val.setKey(payoutPosition);
//                val.setVal(payoutAmount);
//                payouts.add(val);
//            }
//        }
//
//        return map;
//    }
//
//    @Override
//    public float getPlayerPot(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout)
//    {
//        //grab the payout model being used for this game
//        PayoutModel payoutModel = getPayoutModel(gamePayout);
//
//        //get the adjusted payout model
//        List<PayoutModelRound> adjustedPayoutModelRounds = getAdjustedPayoutModelRounds(numPlayers, payoutModel, gamePayout);
//
//        return getPlayerPotFromPayoutModel(adjustedPayoutModelRounds);
//    }
//
//    private float getPlayerPotFromPayoutModel(List<PayoutModelRound> pmrs)
//    {
//        //add up how much each round is going to pay out
//        float totalPayout = 0.00F;
//        for (PayoutModelRound pmr : pmrs) {
//            totalPayout += pmr.getEliminatedPayoutAmount() * pmr.getEliminatedPlayerCount();
//        }
//
//        return totalPayout;
//    }
//
//    @Override
//    public void assignPayouts(Game game, List<Round> rounds, Map<Integer, GamePlayer> gamePlayersMap,
//            List<RoundPlayer> players, List<Integer> botIdsForGame,
//            List<Integer> testSubscribers, GamePayout gamePayout)
//    {
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
//        //sort players by rank, high to low
//        players = players.stream()
//            .sorted( Comparator.comparing(RoundPlayer::getRank, Comparator.nullsLast(Comparator.reverseOrder())) )
//            .collect(Collectors.toList());
//
//        //grab the payout model being used for this game
//        PayoutModel payoutModel = getPayoutModel(gamePayout);
//
//        //get the adjusted payout model
//        List<PayoutModelRound> adjustedPayoutModelRounds = getAdjustedPayoutModelRounds(numPlayers, payoutModel, gamePayout);
//
//        int payoutPosition = 0;
//        float amountLeftAfterPayouts = 0.00F;
//
//        for (PayoutModelRound pmr : adjustedPayoutModelRounds) {
//            int numPayoutsThisRound = pmr.getEliminatedPlayerCount();
//            float payoutAmount = pmr.getEliminatedPayoutAmount();
//            for (int i=0; i<numPayoutsThisRound; i++) {
//                RoundPlayer roundPlayer = players.get(payoutPosition);
//
//                //is the winner a bot?
//                if (botLookup.contains(roundPlayer.getSubscriberId())) {
//                    //TODO: waiting for definition of what to do here: log it, add to db? etc
//                    _logger.info(MessageFormat.format("winner contained bot. payoutAmount: {0,number,currency}", payoutAmount));
//                    amountLeftAfterPayouts += payoutAmount;
//                    continue;
//                }
//
//                //update the RoundPlayer object
//                roundPlayer.setAmountPaid((double)payoutAmount);
//                _shoutContestService.updateRoundPlayer(roundPlayer);
//
//                //update the GamePlayer object
//                GamePlayer gamePlayer = gamePlayersMap.get(roundPlayer.getSubscriberId());
//                gamePlayer.setPayoutAwardedAmount((double)payoutAmount);
//                gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.AWARDED);
//                _shoutContestService.updateGamePlayer(gamePlayer);
//
//                //mark the subscriber as having won in the transaction table, UNLESS they are a test subscriber (i.e. ios test user using the app for their testing)
//                if (!testSubscriberLookup.contains(roundPlayer.getSubscriberId())) {
//                    _shoutContestService.addCashPoolTransaction(
//                        roundPlayer.getSubscriberId(), payoutAmount, CashPoolTransaction2.TYPE.PAYOUT, null, null, game.getId());
//                }
//
//                //send a push
//                Map<String, Object> extras = new HashMap<>();
//                extras.put("amount", gamePlayer);
//                sendGamePush(
//                    _transactionManager, _dao, _pushService, _logger, roundPlayer.getSubscriberId(), game,
//                    "apsCategory", "apsMessage", "SM_PAYOUT_NOTIFICATION", extras
//                );
//
//                payoutPosition++;
//            }
//        }
//
//        if (amountLeftAfterPayouts > 0) {
//            //TODO: do something with the leftover amount ? bruce is working with darl on deciding what to do
//            _logger.info(MessageFormat.format("bots won some of the winnings. amount: {0,number,currency}", amountLeftAfterPayouts));
//        }
//    }
//
//    private float getScaleFactor(int maxPlayerCount, int actualPlayerCount)
//    {
//        return (float) actualPlayerCount / (float) maxPlayerCount;
//    }
//
//    private List<PayoutModelRound> getAdjustedPayoutModelRounds(int numPlayers, PayoutModel payoutModel, GamePayout gamePayout)
//    {
//        int payoutModelId = gamePayout.getPayoutModelId();
//        float scaleFactor = getScaleFactor(payoutModel.getBasePlayerCount(), numPlayers);
//
//        //figure out how many bracket rounds, based on the number of players
//        int numPlayersLeft = numPlayers;
//        List<Integer> roundEliminations = new ArrayList<>();
//        List<Integer> roundStarts = new ArrayList<>();
//
//        while (numPlayersLeft > 2) {
//            //make sure the number of players is even
//            if (numPlayersLeft % 2 != 0) {
//                numPlayersLeft++;
//            }
//
//            roundStarts.add(0, numPlayersLeft);
//
//            //divide by 2 to find out how many get eliminated this round
//            numPlayersLeft /= 2;
//
//            roundEliminations.add(0, numPlayersLeft);
//        }
//
//        //add in the final round (runner up)
//        roundStarts.add(0, 2);
//        roundEliminations.add(0, 1);
//
//        //add in the winner
//        roundStarts.add(0, 1);
//        roundEliminations.add(0, 1);
//
//        //go back and convert each of those to a PayoutModelRound
//        List<PayoutModelRound> pmrs = new ArrayList<>(roundEliminations.size());
//
//        //add in the winner
//        float scaledPayout = (int) (payoutModel.getPayoutModelRounds().get(0).getEliminatedPayoutAmount() * scaleFactor);
//        pmrs.add(createPayoutModelRound(payoutModelId, 0, "winner", 1, 1, scaledPayout));
//
//        //add in each of the other rounds
//        int roundIndex = roundEliminations.size()-1;
//        for (int i=1; i<roundEliminations.size(); i++) {
//            scaledPayout = (int) (payoutModel.getPayoutModelRounds().get(i).getEliminatedPayoutAmount() * scaleFactor);
//            if (scaledPayout < gamePayout.getMinimumPayoutAmount()) scaledPayout = 0.00F;
//            pmrs.add(createPayoutModelRound(payoutModelId, i, "Round" + roundIndex, roundStarts.get(i), roundEliminations.get(i), scaledPayout));
//            roundIndex--;
//        }
//
//        //System.out.println("adjusted payout model rounds:");
//        //for (PayoutModelRound pmr : pmrs) {
//        //    System.out.println(pmr);
//        //}
//
//        return pmrs;
//    }
//
//    private PayoutModel getPayoutModel(GamePayout gamePayout)
//    {
//        int payoutModelId = gamePayout.getPayoutModelId();
//        return _dao.getPayoutModel(payoutModelId);
//
////        List<PayoutModelRound> pmrs = Arrays.asList(
////            createPayoutModelRound(payoutModelId,  0,   "winner", 1, 1, 1000000),
////            createPayoutModelRound(payoutModelId,  1, "Round 20", 2, 2/2, 100000),
////            createPayoutModelRound(payoutModelId,  2, "Round 19", 4, 4/2, 50000),
////            createPayoutModelRound(payoutModelId,  3, "Round 18", 8, 8/2, 25000),
////            createPayoutModelRound(payoutModelId,  4, "Round 17", 16, 16/2, 10000),
////            createPayoutModelRound(payoutModelId,  5, "Round 16", 32, 32/2, 5000),
////            createPayoutModelRound(payoutModelId,  6, "Round 15", 62, 62/2, 1000),
////            createPayoutModelRound(payoutModelId,  7, "Round 14", 124, 124/2, 500),
////            createPayoutModelRound(payoutModelId,  8, "Round 13", 246, 246/2, 250),
////            createPayoutModelRound(payoutModelId,  9, "Round 12", 490, 490/2, 100),
////            createPayoutModelRound(payoutModelId, 10, "Round 11", 978, 978/2, 50),
////            createPayoutModelRound(payoutModelId, 11, "Round 10", 1954, 1954/2, 25),
////            createPayoutModelRound(payoutModelId, 12,  "Round 9", 3908, 3908/2, 20),
////            createPayoutModelRound(payoutModelId, 13,  "Round 8", 7814, 7814/2, 10),
////            createPayoutModelRound(payoutModelId, 14,  "Round 7", 15626, 15626/2, 7),
////            createPayoutModelRound(payoutModelId, 15,  "Round 6", 31250, 31250/2, 5),
////            createPayoutModelRound(payoutModelId, 16,  "Round 5", 62500, 62500/2, 5),
////            createPayoutModelRound(payoutModelId, 17,  "Round 4", 125000, 125000/2, 5),
////            createPayoutModelRound(payoutModelId, 18,  "Round 3", 250000, 250000/2, 5),
////            createPayoutModelRound(payoutModelId, 19,  "Round 2", 500000, 500000/2, 5),
////            createPayoutModelRound(payoutModelId, 20,  "Round 1", 1000000, 1000000/2, 0)
////        );
////
////        PayoutModel pm = new PayoutModel();
////        pm.setPayoutModelId(1);
////        pm.setName("Scott Slightly Modified Payout Model 1");
////        pm.setBasePlayerCount(1000000);
////        pm.setEntranceFeeAmount(10.00F);
////        pm.setPayoutModelRounds(pmrs);
////
////        return pm;
//    }
//
//    private PayoutModelRound createPayoutModelRound(int payoutModelId, int sortOrder, String description, int startingPlayerCount, int eliminatedPlayerCount, float eliminatedPayoutAmount)
//    {
//        PayoutModelRound pmr = new PayoutModelRound();
//
//        pmr.setPayoutModelId(payoutModelId);
//        pmr.setSortOrder(sortOrder);
//        pmr.setDescription(description);
//        pmr.setStartingPlayerCount(startingPlayerCount);
//        pmr.setEliminatedPlayerCount(eliminatedPlayerCount);
//        pmr.setEliminatedPayoutAmount(eliminatedPayoutAmount);
//
//        return pmr;
//    }
//
//    public static void main(String[] args)
//    {
//        PayoutManager payoutManager = new DarlPayoutManager();
//        int numPlayers = 10_000;
//
//        Game game = new Game();
//
//        List<Round> rounds = new ArrayList<Round>();
//        Round round = new Round();
//        round.setMaximumPlayerCount(numPlayers);
//        rounds.add(round);
//
//        GamePayout gamePayout = new GamePayout();
//        gamePayout.setPayoutModelId(1);
//        gamePayout.setMinimumPayoutAmount(1.00F);
//
//        float playerPot = payoutManager.getPlayerPot(game, rounds, numPlayers, gamePayout);
//        System.out.println(MessageFormat.format("#players: {0}, playerPot: {1,number,currency}", numPlayers, playerPot));
//
//        List<PayoutTableRow> collapsedPayoutTable = payoutManager.generateCollapsedPayoutTable(game, rounds, numPlayers, gamePayout);
//        System.out.println(MessageFormat.format("collapsed payout table: {0}", collapsedPayoutTable));
//    }

}
