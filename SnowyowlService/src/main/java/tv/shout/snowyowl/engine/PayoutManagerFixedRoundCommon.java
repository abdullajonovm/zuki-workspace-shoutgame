package tv.shout.snowyowl.engine;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.meinc.commons.postoffice.service.EmailAddress;
import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.gameplay.domain.Tuple;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.notification.domain.NotificationPref;
import com.meinc.notification.service.INotificationService;
import com.meinc.push.service.IPushService;
import com.meinc.urlshorten.exception.UrlShortenerException;
import com.meinc.urlshorten.service.IUrlShortenerService;

import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.LocalizationHelper;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_TYPE;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.domain.RoundPlayer.ROUND_PLAYER_DETERMINATION;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.collector.BaseSmMessageHandler;
import tv.shout.snowyowl.common.EmailSender;
import tv.shout.snowyowl.common.PushSender;
import tv.shout.snowyowl.common.ShortUrlGenerator;
import tv.shout.snowyowl.common.SmsSender;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.AffiliatePlan;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutModelRound.CATEGORY;
import tv.shout.snowyowl.domain.Sponsor;
import tv.shout.snowyowl.domain.SponsorCashPool;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.util.FastMap;
import tv.shout.util.MathUtil;
import tv.shout.util.MaxSizeHashMap;

public abstract class PayoutManagerFixedRoundCommon
extends BasePayoutManager
implements ShortUrlGenerator, PushSender, EmailSender, SmsSender
{
    private static Logger _logger = Logger.getLogger(PayoutManagerFixedRoundCommon.class);

    private static final String cashPayoutNotificationTitleUuid = "ee994158-0133-4bb7-af72-58e9176d567f";
    private static final String cashPayoutNotificationBodyUuid = "2454f9d3-c16e-4d13-bd4f-5e0ffa8203c0";
    private static final String UUID_REFERRAL_PAYOUT_SUBJECT = "6f7a6d63-fbf5-43bd-958e-23748e63870d";
    private static final String UUID_REFERRAL_MESSAGE_FORM1 = "7bf6cc56-24ed-4c9b-8933-f1cb01cf2bb2";
    private static final String UUID_REFERRAL_MESSAGE_FORM2 = "abd2dbb5-d7ef-4ba5-9f73-36b8705854a4";
    private static final String UUID_REFERRAL_MESSAGE_FORM3 = "a9eb161d-59c3-44e5-999e-8029313f87bf";

    private static final int MAX_SMS_LENGTH = 160;

    @Value("${from.email.addresses}")
    private String _emailFromAddrs;

    @Value("${from.email.names}")
    private String _emailFromNames;

    @Value("${shorten.url.domain}")
    private String _shortUrlDomain;

    @Value("${shorten.url.short.url.prefix}")
    private String _shortUrlPrefix;

    @Value("${twilio.from.number}")
    private String _twilioFromNumber;

    @Value("${twilio.account.sid}")
    private String _twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String _twilioAuthToken;

    @Value("${sm.app.displayname}")
    private String _appName;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected IPushService _pushService;

    @Autowired
    protected IIdentityService _identityService;

    @Autowired
    private INotificationService _notificationService;

    @Autowired
    private IUrlShortenerService _urlShortenerService;

    @Autowired
    private IPostOffice _postOfficeService;

    @Autowired
    protected PlatformTransactionManager _transactionManager;

    @Autowired
    protected IDaoMapper _dao;

    public static int getNumberOfSaves(int numPlayers)
    {
        if (numPlayers % 2 != 0) {
            throw new IllegalStateException("call this method AFTER odd man out bot has been added");
        }

        int numOfMatches = numPlayers / 2;
        int powerOfTwo = MathUtil.getNearestEqualToOrLargerPowerOf2(numOfMatches);
        return powerOfTwo - numOfMatches;
    }

    protected static int getNumPayoutModelRounds(int numPlayers)
    {
        if (numPlayers <= 0) throw new IllegalArgumentException();

        int numSaves = getNumberOfSaves(numPlayers);
        int secondRoundPlayerCount = numPlayers / 2 + numSaves;

        // Ceiled logarithm tells you the # necessary rounds.  Add 1 for the first place player and another for the first round
        return (int) Math.ceil(Math.log(secondRoundPlayerCount) / Math.log(2)) + 2;
    }

    private static PayoutModelRound createPayoutModelRound(int payoutModelId, int sortOrder, String description, int startingPlayerCount, int eliminatedPlayerCount,
            float eliminatedPayoutAmount, String type, CATEGORY category, int roundNumber)
    {
        PayoutModelRound pmr = new PayoutModelRound();

        pmr.setPayoutModelId(payoutModelId);
        pmr.setSortOrder(sortOrder);
        pmr.setDescription(description);
        pmr.setStartingPlayerCount(startingPlayerCount);
        pmr.setEliminatedPlayerCount(eliminatedPlayerCount);
        pmr.setEliminatedPayoutAmount(eliminatedPayoutAmount);
        pmr.setType(type);
        pmr.setCategory(category);
        pmr.setRoundNumber(roundNumber);

        return pmr;
    }

    public static List<PayoutModelRound> getDefaultPayoutModelRounds(int numPlayers) throws PayoutManagerException
    {
        if (numPlayers == 0) {
            return new ArrayList<>(0);
        }

        if (numPlayers % 2 != 0) {
            numPlayers++;
        }

        // We start with the total number of people, adding 1 if necessary to make the number even
        // Represents the startingPlayerCount
        int fromPeople = numPlayers;
        // Represents the eliminatedPlayerCount
        int toPeople = fromPeople / 2 + getNumberOfSaves(numPlayers);

        // The number of PayoutNumberRounds for the given player count
        int numRounds = getNumPayoutModelRounds(numPlayers);

        // The result
        PayoutModelRound rounds[] = new PayoutModelRound[numRounds];
        Arrays.fill(rounds, null); // So we can start at the back of the array

        int i = numRounds - 1;
        for (; fromPeople >= 1; fromPeople = toPeople, toPeople /= 2, i--) {
            if (toPeople <= 1) {
                toPeople = 1; // The last player
            } else if (toPeople % 2 != 0) {
                toPeople++; // Add one if necessary
            }

            int eliminatedPlayerCount = fromPeople-toPeople;

            //special case: on the final round when only 1 person is left, even though technically they're not eliminated, this counts
            // them as eliminated so that the payout logic can work without branching when assigning payouts
            if (eliminatedPlayerCount == 0 && fromPeople == 1) {
                eliminatedPlayerCount = 1;
            }

            // Makes a copy of the existing round, overwriting some of the values
            rounds[i] = createPayoutModelRound(0, i, null,
                    fromPeople, eliminatedPlayerCount, 0F, null, null, numRounds-i);

            if (fromPeople == 1)
                break;
        }

        return Arrays.asList(rounds);
    }

    @Override
    public int getNumberOfBracketRounds(int numPlayers)
    {
        if (numPlayers == 0) return 0;

        if (numPlayers % 2 != 0) numPlayers++;

        return getNumPayoutModelRounds(numPlayers) - 1; //the getNumPayoutModelRounds adds an extra round for the first place winner (but this round doesn't really exist since they've won and don't play against nobody)
    }


    protected PayoutModel getPayoutModel(GamePayout gamePayout)
    {
        int payoutModelId = gamePayout.getPayoutModelId();
        PayoutModel pm;

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            pm = _dao.getPayoutModel(payoutModelId);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return pm;
    }

    @Override
    public float getPlayerPot(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout)
    throws PayoutManagerException
    {
        //grab the payout model being used for this game
        PayoutModel payoutModel = getPayoutModel(gamePayout);

        //get the adjusted payout model
        List<PayoutModelRound> adjustedPayoutModelRounds = getAdjustedPayoutModelRounds(numPlayers, payoutModel, gamePayout);

        return getPlayerPotFromPayoutModel(adjustedPayoutModelRounds);
    }

    @Override
    public Map<String, Object> generatePayoutTable(Game game, List<Round> rounds, int numPlayers, GamePayout gamePayout)
    throws PayoutManagerException
    {
        //grab the payout model being used for this game
        PayoutModel payoutModel = getPayoutModel(gamePayout);

        if (payoutModel == null) throw new PayoutManagerException("payoutModelNotFound", new FastMap<>("payoutModelId", gamePayout.getPayoutModelId()));

        //get the adjusted payout model
        List<PayoutModelRound> adjustedPayoutModelRounds = getAdjustedPayoutModelRounds(numPlayers, payoutModel, gamePayout);

        Map<String, Object> map = new HashMap<>();
        List<Payout> payouts = new ArrayList<>();
        map.put("playerPot", getPlayerPotFromPayoutModel(adjustedPayoutModelRounds));
        map.put("payouts", payouts);

//TODO: don't do this intermediate step. just generate the collapsed value
        //build up the payouts map
        int payoutPosition = 0;
        for (PayoutModelRound pmr : adjustedPayoutModelRounds) {
            int numPayoutsThisRound = pmr.getEliminatedPlayerCount();
            for (int i=0; i<numPayoutsThisRound; i++) {
                payoutPosition++;
                float payoutAmount = pmr.getEliminatedPayoutAmount();
                String type = pmr.getType();
                String category = pmr.getCategory().toString();
                Payout val = new Payout(payoutPosition, payoutAmount, type, category);
                payouts.add(val);
            }
        }

        return map;
    }

    private float getPlayerPotFromPayoutModel(List<PayoutModelRound> pmrs)
    {
        //add up how much each round is going to pay out
        float totalPayout = 0.00F;
        for (PayoutModelRound pmr : pmrs) {
            if (pmr.getType().equals(PayoutModelRound.TYPE_CASH)) {
                totalPayout += pmr.getEliminatedPayoutAmount() * pmr.getEliminatedPlayerCount();
            }
        }

        return totalPayout;
    }

    /**
    *
    * @param payoutModel
    * @param playerCount
    * @return
    * @throws PayoutManagerException
    */
   protected float getScaleFactor(PayoutModel payoutModel, int playerCount)
   throws PayoutManagerException
   {
       if (payoutModel.isScalePayout()) {
           return getScaleFactor_aidan(payoutModel, playerCount);
       } else {
           return 1.0F;
       }
   }

   @SuppressWarnings("unused")
   private float getScaleFactor_scott(PayoutModel payoutModel, int playerCount)
   throws PayoutManagerException
   {
       int maxPlayerCount = payoutModel.getBasePlayerCount();

       //this would mean things scale UP, which is incompatible with the algorithm. don't allow it
       if (playerCount > maxPlayerCount) {
           throw new PayoutManagerException("playerCountLargerThanMaxPlayerCountForGivenPayoutModel",
                   new FastMap<>("maxPlayerCount", maxPlayerCount+"", "actualPlayerCount", playerCount+""));
       }

       return (float) playerCount / (float) maxPlayerCount;
   }

   /**
    * Calculates how much the payout model payout amounts should be adjusted by.
    * Does more than just return the % of people playing as we had before - also
    * adjusts for the % of payout models that we aren't giving money away from because
    * there aren't enough players.
    *
    * @param payoutModel which PayoutModel is being used
    * @param playerCount how many players is this going to be scaled down to accommodate
    * @return the scale factor (1.0F = no scaling, 0.5F means there are half the max players, etc)
    * @throws PayoutManagerException if actual > max, which would cause a >1 scale factor
    */
   //@SuppressWarnings("unused")
   private float getScaleFactor_aidan(PayoutModel payoutModel, int playerCount)
   throws PayoutManagerException
   {
       //this would mean things scale UP, which is incompatible with the algorithm. don't allow it
       if (playerCount > payoutModel.getBasePlayerCount()) {
           throw new PayoutManagerException("playerCountLargerThanMaxPlayerCountForGivenPayoutModel",
                   new FastMap<>("maxPlayerCount", payoutModel.getBasePlayerCount()+"", "actualPlayerCount", playerCount+""));
       }

       // The % of people playing
       float playerScale = (float) playerCount / (float) payoutModel.getBasePlayerCount();

       int eventPlayerCount = playerCount;
       if (eventPlayerCount % 2 != 0) {
           eventPlayerCount++;
       }

       //the number of rounds for the given player count
       int numPayoutModelRounds = getNumPayoutModelRounds(eventPlayerCount);

       // The total amount of money given away for all PayoutModelRounds
       float oldPayoutAmount = 0F;
       // The amount of money given to our shortened PayoutModelRounds
       float newPayoutAmount = 0F;

       for (int i=0; i<payoutModel.getPayoutModelRounds().size(); i++) {
           PayoutModelRound payoutModelRound = payoutModel.getPayoutModelRounds().get(i);
           float payoutAmount = payoutModelRound.getEliminatedPayoutAmount() * payoutModelRound.getEliminatedPlayerCount();
           oldPayoutAmount += payoutAmount;

           if (i<numPayoutModelRounds) {
               newPayoutAmount += payoutAmount;
           }
       }

       float payoutAmountScale = oldPayoutAmount / newPayoutAmount;

       return payoutAmountScale * playerScale;
   }

    /**
     * Take an existing PayoutModel and adjust the payouts DOWN to account for a
     * smaller number of players.
     *
     * @param numPlayers
     *            how many players to scale down to
     * @param payoutModel
     *            used to get the base player count, and also each of the
     *            unscaled rounds
     * @param gamePayout
     *            used to get the minimum payout amount allowed
     * @return the list of adjusted PayoutModelRound objects
     * @throws PayoutManagerException
     *             if the scaling factor would go UP with the given inputs
     */
    @Override
    public List<PayoutModelRound> getAdjustedPayoutModelRounds(int numPlayers, PayoutModel payoutModel, GamePayout gamePayout)
    throws PayoutManagerException
    {
        int payoutModelId = payoutModel.getPayoutModelId();

        if (numPlayers == 0) {
            return new ArrayList<>(0);
        }

        int evenNumPlayers = numPlayers;
        if (evenNumPlayers % 2 != 0) {
            evenNumPlayers++;
        }

        float scale = getScaleFactor(payoutModel, numPlayers);
//System.out.println("scale: " + scale);

        // sorted high-round to low-round just like payout models
        List<PayoutModelRound> defaultRounds = getDefaultPayoutModelRounds(numPlayers);
        if (payoutModel.getPayoutModelRounds().size() < defaultRounds.size()) {
            throw new IllegalStateException("payoutModel doesn't have enough rounds");
        }

        //calculate the original total payout (before any scaling)
        float totalNonScaledPayoutAmount = 0F;
        for (int i = 0; i < defaultRounds.size(); i++) {
            PayoutModelRound existingRound = payoutModel.getPayoutModelRounds().get(i);
            totalNonScaledPayoutAmount += (existingRound.getEliminatedPayoutAmount() * existingRound.getEliminatedPlayerCount());
        }
//System.out.println("non scaled payout amount: " + totalNonScaledPayoutAmount);

        //keep track of what percentage of the payout each round was, of the original payout before any scaling occurred
        //(ex: 1st place got $100, which was 10% of the overall payout)
        List<Float> nonScaledPayoutPercentages = new ArrayList<>(defaultRounds.size());
        for (int i = 0; i < defaultRounds.size(); i++) {
            PayoutModelRound existingRound = payoutModel.getPayoutModelRounds().get(i);
            float roundPayoutAmount = existingRound.getEliminatedPayoutAmount() * existingRound.getEliminatedPlayerCount();
            nonScaledPayoutPercentages.add(roundPayoutAmount / totalNonScaledPayoutAmount);
        }
//System.out.println(MessageFormat.format("round payout percentages: {0}", nonScaledPayoutPercentages));

        for (int i = 0; i < defaultRounds.size(); i++) {
            PayoutModelRound defaultRound = defaultRounds.get(i);
            PayoutModelRound existingRound = payoutModel.getPayoutModelRounds().get(i);

            float eliminatedPayoutAmount = (float) Math.floor(existingRound.getEliminatedPayoutAmount() * scale);
            if (eliminatedPayoutAmount < gamePayout.getMinimumPayoutAmount()) {
                eliminatedPayoutAmount = 0;
            }

            defaultRound.setPayoutModelId(payoutModelId);
            defaultRound.setEliminatedPayoutAmount(eliminatedPayoutAmount);
            defaultRound.setCategory(existingRound.getCategory());
            defaultRound.setType(existingRound.getType());
            defaultRound.setDescription(existingRound.getDescription());
        }

        if (payoutModel.isScalePayout()) {
            if (payoutModel.getMinimumFirstPlacePayoutAmount() != null && payoutModel.getMinimumFirstPlacePayoutAmount() > 0F) {
                //get the first place payout model round
                PayoutModelRound firstPlacePmr = defaultRounds.get(0);

                //only bother checking if it's giving away cash
                if (firstPlacePmr.getCategory() == PayoutModelRound.CATEGORY.PHYSICAL && firstPlacePmr.getType().equals(PayoutModelRound.TYPE_CASH)) {
                    //check to see if the 1st place winner payout amount is below this. if so, use this instead
                    if (firstPlacePmr.getEliminatedPayoutAmount() < payoutModel.getMinimumFirstPlacePayoutAmount()) {
                        firstPlacePmr.setEliminatedPayoutAmount(payoutModel.getMinimumFirstPlacePayoutAmount());
                    }
                }
            }

            if (payoutModel.getMinimumSecondPlacePayoutAmount() != null && payoutModel.getMinimumSecondPlacePayoutAmount() > 0F) {
                //get the second place payout model round
                PayoutModelRound secondPlacePmr = defaultRounds.get(1);

                //only bother checking if it's giving away cash
                if (secondPlacePmr.getCategory() == PayoutModelRound.CATEGORY.PHYSICAL && secondPlacePmr.getType().equals(PayoutModelRound.TYPE_CASH)) {
                    //check to see if the 2nd place winner payout amount is below this. if so, use this instead
                    if (secondPlacePmr.getEliminatedPayoutAmount() < payoutModel.getMinimumSecondPlacePayoutAmount()) {
                        secondPlacePmr.setEliminatedPayoutAmount(payoutModel.getMinimumSecondPlacePayoutAmount());
                    }

                }
            }

            if (payoutModel.getMinimumOverallPayoutAmount() != null && payoutModel.getMinimumOverallPayoutAmount() > 0F) {
                //find the total payout amount
                List<Float> payouts = new ArrayList<>(1);
                payouts.add(0F);
                defaultRounds.forEach(pmr -> {
                    float roundPayout = ( pmr.getEliminatedPlayerCount() * pmr.getEliminatedPayoutAmount());
                    float previousPayout = payouts.get(0);
                    payouts.set(0, roundPayout + previousPayout);
                });
                float totalPayoutAmount = payouts.get(0);

                if (totalPayoutAmount < payoutModel.getMinimumOverallPayoutAmount()) {
                    //TODO: spread the money around
                    float amountToSpreadAround = payoutModel.getMinimumOverallPayoutAmount() - totalPayoutAmount;

                    //go through each round and add in the extra money
                    for (int i = 0; i < defaultRounds.size(); i++) {
                        PayoutModelRound pmr = defaultRounds.get(i);
                        float amountToAdd = (float) Math.ceil( (nonScaledPayoutPercentages.get(i) * amountToSpreadAround) / pmr.getEliminatedPlayerCount());

//System.out.println(MessageFormat.format("round {0} gets an extra ${1}", i, amountToAdd));
                        pmr.setEliminatedPayoutAmount(pmr.getEliminatedPayoutAmount() + amountToAdd);
                    }

//System.out.println("amount that must be 'spread around': " + amountToSpreadAround);
                }
            }
        }

        return defaultRounds;
    }

    //wrapped in a transaction
    @Override
    public void assignPayouts(Game game, List<Round> rounds, Map<Long, GamePlayer> gamePlayersMap,
            List<RoundPlayer> players, Set<Long> botLookup, Set<Long> sponsorPlayerIdsForGame,
            Set<Long> testSubscriberLookup, GamePayout gamePayout)
    throws PayoutManagerException
    {
        // sorted from high-round to low-round
        List<Round> bracketRounds = rounds.stream()
            .filter(r -> r.getRoundType() == ROUND_TYPE.BRACKET)
            .sorted(Comparator.comparing(Round::getRoundSequence, Comparator.reverseOrder()))
            .collect(Collectors.toList());

        int numPlayers = players.size();
if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format(
        "Assigning payouts. gameId: {0}, #rounds: {1}, payoutModelId: {2}, minPayoutAmount: ${3}, numPlayers: {4}",
        game.getId(), rounds.size(), gamePayout.getPayoutModelId(), gamePayout.getMinimumPayoutAmount(), numPlayers));
}

        //grab the payout model being used for this game
        PayoutModel payoutModel = getPayoutModel(gamePayout);

        //get the adjusted payout model
        // sorted high-round to low-round
        List<PayoutModelRound> adjustedPayoutModelRounds = getAdjustedPayoutModelRounds(numPlayers, payoutModel, gamePayout);
if (_logger.isDebugEnabled()) {
    _logger.debug("Adjusted PayoutModelRounds:");
    adjustedPayoutModelRounds.stream().forEach(r -> _logger.debug("\t" + r));
}

        if (adjustedPayoutModelRounds.size() != bracketRounds.size() + 1) {
            StringBuilder buf = new StringBuilder();
            bracketRounds.stream().forEach(r -> buf.append("\n\t" + r));
            throw new IllegalStateException("Payout model rounds do not match bracket rounds. gameId: "+game.getId()+ "\nBRACKET ROUNDS:\n" + buf.toString());
        }

        //grab the localization values needed for the pushes
        Map<String, String> cashPayoutNotificationTitleMap=null;
        Map<String, String> cashPayoutNotificationBodyMap=null;
//        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
//        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
//        try {
            List<Tuple<String>> cashPayoutNotificationTitle = _dao.getMultiLocalizationValues(cashPayoutNotificationTitleUuid, "systemMessage");
            List<Tuple<String>> cashPayoutNotificationBody = _dao.getMultiLocalizationValues(cashPayoutNotificationBodyUuid, "systemMessage");

            cashPayoutNotificationTitleMap = BaseSmMessageHandler.tupleListToMap(cashPayoutNotificationTitle);
            cashPayoutNotificationBodyMap = BaseSmMessageHandler.tupleListToMap(cashPayoutNotificationBody);

//            _transactionManager.commit(txStatus);
//            txStatus = null;
//        } finally {
//            if (txStatus != null) {
//                _transactionManager.rollback(txStatus);
//                txStatus = null;
//            }
//        }

        //find out exactly how many players joined the game and paid actual money to get in (i.e. filter out bots and test subscribers)
        int numActualPlayersWhoPaidMoney = 0;
        for (int i=0; i<players.size(); i++) {
            long subscriberId = players.get(i).getSubscriberId();

            if (botLookup.contains(subscriberId)) continue;
            if (testSubscriberLookup.contains(subscriberId)) continue;

            numActualPlayersWhoPaidMoney++;
        }
        double netProfitPerPlayer = getNetProfitPerPlayer(numActualPlayersWhoPaidMoney, payoutModel, gamePayout);
if (_logger.isDebugEnabled()) {
    _logger.debug("netProfitPerPlayer (pre negative adjust): " + netProfitPerPlayer);
}

        //it's possible that a badly designed payout model along with a small number of players can cause a negative profit
        //don't allow it to go negative
        netProfitPerPlayer = Math.max(0, netProfitPerPlayer);

        float amountLeftAfterPayouts = 0.00F;

        //key: sponsor cash pool id, val: cash pool
        Map<Integer, SponsorCashPool> sponsorCashPoolMap = new HashMap<>();

        for (int i = 0; i < adjustedPayoutModelRounds.size(); i++) {
            PayoutModelRound pmr = adjustedPayoutModelRounds.get(i);
if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("--- processing round: {0}", pmr.getRoundNumber()));
}

            Round bracketRound;
            List<RoundPlayer> roundPayoutPlayers;
            if (i == 0) {
                // Give the grand-champion prize
                bracketRound = bracketRounds.get(i);
                roundPayoutPlayers = players.stream()
                    .filter(rp -> rp.getDetermination() == ROUND_PLAYER_DETERMINATION.WON)
                    .filter(rp -> rp.getRoundId().equals(bracketRound.getId()))
                    .collect(Collectors.toList());
            } else {
                // Give the regular bracket prize
                bracketRound = bracketRounds.get(i-1);
                roundPayoutPlayers = players.stream()
                    .filter(rp -> rp.getDetermination() == ROUND_PLAYER_DETERMINATION.LOST)
                    .filter(rp -> rp.getRoundId().equals(bracketRound.getId()))
                    .collect(Collectors.toList());
            }

            float roundPayoutAmount = pmr.getEliminatedPayoutAmount();

            for (RoundPlayer roundPayoutPlayer : roundPayoutPlayers) {
                if (botLookup.contains(roundPayoutPlayer.getSubscriberId())) {
                    //TOxDO: waiting for definition of what to do here. for now just log it
                    if (pmr.getType().equals(PayoutModelRound.TYPE_CASH)) {
if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format(
        "(NOT AWARDING: BOT). payoutAmount: ${0,number}, subId: {1,number,#}",
        roundPayoutAmount, roundPayoutPlayer.getSubscriberId()));
}
                        amountLeftAfterPayouts += roundPayoutAmount;
                    }
                    continue; //make sure any final loop processing happens here as well (such as incrementing the payoutPosition)

                } else if (sponsorPlayerIdsForGame.contains(roundPayoutPlayer.getSubscriberId())) {
                    if (pmr.getType().equals(PayoutModelRound.TYPE_CASH)) {
                        //calculate how much gets put back into the sponsor pool
                        double sponsorPlayerRefundAmount = netProfitPerPlayer + (gamePayout.isGiveSponsorPlayerWinningsBackToSponsor() ? roundPayoutAmount : 0);
                        Sponsor sponsor = _dao.getSponsorById(roundPayoutPlayer.getSubscriberId());

                        //put it back in the pool
                        SponsorCashPool pool = sponsorCashPoolMap.get(sponsor.getSponsorCashPoolId());
                        if (pool == null) {
                            pool = _dao.getSponsorCashPoolBySponsorPlayerSubscriberId(roundPayoutPlayer.getSubscriberId());
                            sponsorCashPoolMap.put(sponsor.getSponsorCashPoolId(), pool);
                        }
                        pool.setAmount(pool.getAmount() + sponsorPlayerRefundAmount);
//_logger.info(">>> sponsor " + roundPayoutPlayer.getSubscriberId() + " won " + roundPayoutAmount + ", returning " + sponsorPlayerRefundAmount + " to pool " + pool.getSponsorCashPoolId());
                        _dao.updateSponsorCashPool(pool);
                        _dao.addSponsorCashPoolTransaction(pool.getSponsorCashPoolId(), sponsorPlayerRefundAmount, "REFUND_END_OF_GAME_WINNER");

                        //release the sponsor player
//_logger.info(">>> releasing sponsor " + roundPayoutPlayer.getSubscriberId() + " from game " + game.getId());
                        _dao.releaseSponsorPlayerForGame(roundPayoutPlayer.getSubscriberId());

if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format(
        "(NOT AWARDING: SPONSOR). payoutAmount: ${0,number}, subId: {1,number,#}",
        roundPayoutAmount, roundPayoutPlayer.getSubscriberId()));
}
                    }
                    continue;
                }

                if (pmr.getType().equals(PayoutModelRound.TYPE_CASH)) {
                    //update the RoundPlayer object
                    roundPayoutPlayer.setAmountPaid((double)roundPayoutAmount);
                    _shoutContestService.updateRoundPlayer(roundPayoutPlayer);

                    //update the GamePlayer object
                    GamePlayer gamePlayer = gamePlayersMap.get(roundPayoutPlayer.getSubscriberId());
                    gamePlayer.setPayoutAwardedAmount((double)roundPayoutAmount);
                    gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.AWARDED);
                    _shoutContestService.updateGamePlayer(gamePlayer);

                    Subscriber s = _identityService.getSubscriberById(roundPayoutPlayer.getSubscriberId());

                    //mark the subscriber as having won in the transaction table, UNLESS they are a test subscriber (i.e. ios test user using the app for their testing)
                    //but only do this if it's a production game
                    if (game.isProductionGame() && !testSubscriberLookup.contains(roundPayoutPlayer.getSubscriberId())) {
                        _shoutContestService.addCashPoolTransaction(
                            roundPayoutPlayer.getSubscriberId(), roundPayoutAmount, CashPoolTransaction2.TYPE.PAYOUT, null, null, game.getId());

                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format(
                                "roundPayoutAmount: ${0,number}, gamePlayerId: {1}, subId: {2,number,#}",
                                roundPayoutAmount, gamePlayer.getId(), roundPayoutPlayer.getSubscriberId()));
                        }

                    } else {
                        if (_logger.isDebugEnabled()) {
                            String nonAwardReason = !game.isProductionGame() ? "NOT PRODUCTION GAME" : "TEST SUB";
                            _logger.debug(MessageFormat.format(
                                    "(NOT AWARDING: {0}) roundPayoutAmount: ${1,number}, gamePlayerId: {2}, subId: {3,number,#}",
                                    nonAwardReason, roundPayoutAmount, gamePlayer.getId(), roundPayoutPlayer.getSubscriberId()));
                        }
                    }

                    String localizedTitle = LocalizationHelper.getLocalizedString(cashPayoutNotificationTitleMap, s.getLanguageCode());
                    String localizedBody = MessageFormat.format(LocalizationHelper.getLocalizedString(cashPayoutNotificationBodyMap, s.getLanguageCode()), roundPayoutAmount);

                    //send a push
                    Map<String, Object> extras = new HashMap<>();
                    extras.put("amount", gamePlayer.getPayoutAwardedAmount());
                    sendGamePush(
                        _transactionManager, _dao, _pushService, _logger,
                        roundPayoutPlayer.getSubscriberId(), s.getLanguageCode(), game, "apsCategory:TODO", localizedTitle, localizedBody,
                        "SM_PAYOUT_NOTIFICATION", extras
                    );

                } else {
                    //won something other than cash
                    //TOxDO: waiting on definition of what to do in this case. for now just log it
                    _logger.info(MessageFormat.format("winner won a non cash prize. amount: {0,number}, type: {1}, category: {2}", pmr.getEliminatedPayoutAmount(), pmr.getType(), pmr.getCategory()));
                }
            }
        }

        if (amountLeftAfterPayouts > 0) {
            //TOxDO: do something with the leftover amount ? bruce is working with darl on deciding what to do
            _logger.info(MessageFormat.format("bots won some of the winnings. amount: ${0,number}", amountLeftAfterPayouts));
        }

        //refund any remaining sponsor players by the netProfitPerPlayer
        List<Sponsor> remainingSponsors = _dao.getSponsorsForGame(game.getId());
//_logger.info(" >>> there are " + remainingSponsors.size() + " sponsor players that didn't win anything.");
        for (Sponsor sponsor : remainingSponsors) {
            long sponsorSubscriberId = sponsor.getSubscriberId();
            SponsorCashPool pool = sponsorCashPoolMap.get(sponsor.getSponsorCashPoolId());
            if (pool == null) {
                pool = _dao.getSponsorCashPoolBySponsorPlayerSubscriberId(sponsorSubscriberId);
                sponsorCashPoolMap.put(sponsor.getSponsorCashPoolId(), pool);
            }

_logger.info(">>> returning " + netProfitPerPlayer + " to sponsor " + sponsor.getSubscriberId() + " for pool " + pool.getSponsorCashPoolId());
            pool.setAmount(pool.getAmount() + netProfitPerPlayer);
            _dao.updateSponsorCashPool(pool);
            _dao.addSponsorCashPoolTransaction(pool.getSponsorCashPoolId(), netProfitPerPlayer, "REFUND_END_OF_GAME");
        }

        //release the remaining sponsor players
//_logger.info(">>> releasing all sponsors for game: " + game.getId());
        _dao.releaseSponsorPlayersForGame(game.getId());

        doAffiliatePayout(players, botLookup, testSubscriberLookup, game, gamePayout, payoutModel, numActualPlayersWhoPaidMoney, netProfitPerPlayer);
    }

    private static class ReferralStruct
    {
        public double amount;
        public String nickname;

        public ReferralStruct(double amount, String nickname)
        {
            this.amount = amount;
            this.nickname = nickname;
        }
    }

    //wrapped in transaction
    private void doAffiliatePayout(
        List<RoundPlayer> players, Set<Long> botLookup, Set<Long> testSubscriberLookup,
        Game game, GamePayout gamePayout, PayoutModel payoutModel, int numActualPlayersWhoPaidMoney, double netProfitPerPlayer)
    throws PayoutManagerException
    {
        if (!game.isProductionGame()) return;

        if (_logger.isDebugEnabled()) {
            _logger.info(MessageFormat.format(
                    "affiliatePayout::netProfitPerPlayer: {0}, numActualPlayersWhoPaidMoney: {1}",
                    netProfitPerPlayer, numActualPlayersWhoPaidMoney));
        }

        Map<Long, List<ReferralStruct>> affiliatePayoutAmounts = new HashMap<>();

        for (int i=0; i<players.size(); i++) {
            long subscriberId = players.get(i).getSubscriberId();

            //ignore bots and test subscribers
            if (botLookup.contains(subscriberId)) continue;
            if (testSubscriberLookup.contains(subscriberId)) continue;

            Subscriber s = _identityService.getSubscriberById(subscriberId);
_logger.info("affiliatePayout::examining player: " + s.getNickname());
            SubscriberStats ss = _dao.getSubscriberStats(s.getSubscriberId());

            //if there was no affiliate plan active when this subscriber signed up, there's nothing to do
            if (ss == null || ss.getAffiliatePlanId() == 0) {
_logger.info("no subscriber stats OR affiliatePayout::player " + s.getNickname() + " has no affiliate plan. skipping");
                continue;
            }
            int affiliatePlanId = ss.getAffiliatePlanId();
            boolean isFirstGame = ss.getGamesPlayed() == 1;

            Long directMintParentId;
            Long secondaryMintParentId = null;
            Long tertiaryMintParentId = null;
            boolean isDirectMintParentAnAffiliate = false;
            boolean isSecondaryMintParentAnAffiliate = false;
            boolean isTertiaryMintParentAnAffiliate = false;

            directMintParentId = s.getMintParentSubscriberId();
            if (directMintParentId != null) {
                Subscriber sDirectMintParent = _identityService.getSubscriberById(directMintParentId);
                if (sDirectMintParent == null) {
                    _logger.error(MessageFormat.format("CORRUPT DATA. subscriber {0,number,#} has invalid mintParentSubscriberId of {1,number,#}", s.getSubscriberId(), directMintParentId));
                    continue;
                }
                isDirectMintParentAnAffiliate = _identityService.hasRole(directMintParentId, new HashSet<>(Arrays.asList(BaseSmMessageHandler.AFFILIATE)), true);

                secondaryMintParentId = sDirectMintParent.getMintParentSubscriberId();
                if (secondaryMintParentId != null) {
                    Subscriber sSecondaryMintParent = _identityService.getSubscriberById(secondaryMintParentId);
                    if (sSecondaryMintParent == null) {
                        _logger.error(MessageFormat.format("CORRUPT DATA. subscriber {0,number,#} has invalid mintParentSubscriberId of {1,number,#}", sDirectMintParent.getSubscriberId(), secondaryMintParentId));
                        continue;
                    }
                    isSecondaryMintParentAnAffiliate = _identityService.hasRole(secondaryMintParentId, new HashSet<>(Arrays.asList(BaseSmMessageHandler.AFFILIATE)), true);

                    tertiaryMintParentId = sSecondaryMintParent.getMintParentSubscriberId();
                    if (tertiaryMintParentId != null) {
                        isTertiaryMintParentAnAffiliate = _identityService.hasRole(tertiaryMintParentId, new HashSet<>(Arrays.asList(BaseSmMessageHandler.AFFILIATE)), true);
                    }
                }
            }

            double percentToGiveToAffiliate;
            if (directMintParentId != null) {
                if (isDirectMintParentAnAffiliate) {
                    percentToGiveToAffiliate = getAffiliatePlan(affiliatePlanId).getAffiliateDirectPayoutPct();
                    if (_logger.isDebugEnabled()) {
                        _logger.debug(MessageFormat.format(
                            "affiliatePayout::source subscriber: {0,number,#}, affiliate subscriber: {1,number,#}, via affiliatePlanId {2,number,#}, getting affiliateDirectPayoutPct of: {3}",
                            subscriberId, directMintParentId, affiliatePlanId, percentToGiveToAffiliate
                        ));
                        addAffiliateAmount(affiliatePayoutAmounts, netProfitPerPlayer, percentToGiveToAffiliate, directMintParentId, s);
                    }
                } else {
                    if (isFirstGame) {
                        percentToGiveToAffiliate = getAffiliatePlan(affiliatePlanId).getPlayerInitialPayoutPct();
                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format(
                                "affiliatePayout::source subscriber: {0,number,#}, affiliate subscriber: {1,number,#}, via affiliatePlanId {2,number,#}, getting playerInitialPayoutPct of: {3}",
                                subscriberId, directMintParentId, affiliatePlanId, percentToGiveToAffiliate
                            ));
                            addAffiliateAmount(affiliatePayoutAmounts, netProfitPerPlayer, percentToGiveToAffiliate, directMintParentId, s);
                        }
                    }

                    if (secondaryMintParentId != null) {
                        if (isSecondaryMintParentAnAffiliate) {
                            percentToGiveToAffiliate = getAffiliatePlan(affiliatePlanId).getAffiliateSecondaryPayoutPct();
                            if (_logger.isDebugEnabled()) {
                                _logger.debug(MessageFormat.format(
                                    "affiliatePayout::source subscriber: {0,number,#}, affiliate subscriber: {1,number,#}, via affiliatePlanId {2,number,#}, getting affiliateSecondaryPayoutPct of: {3}",
                                    subscriberId, secondaryMintParentId, affiliatePlanId, percentToGiveToAffiliate
                                ));
                                addAffiliateAmount(affiliatePayoutAmounts, netProfitPerPlayer, percentToGiveToAffiliate, secondaryMintParentId, s);
                            }
                        } else {

                            if (tertiaryMintParentId != null) {
                                if (isTertiaryMintParentAnAffiliate) {
                                    percentToGiveToAffiliate = getAffiliatePlan(affiliatePlanId).getAffiliateTertiaryPayoutPct();
                                    if (_logger.isDebugEnabled()) {
                                        _logger.debug(MessageFormat.format(
                                            "affiliatePayout::source subscriber: {0,number,#}, affiliate subscriber: {1,number,#}, via affiliatePlanId {2,number,#}, getting affiliateTertiaryPayoutPct of: {3}",
                                            subscriberId, tertiaryMintParentId, affiliatePlanId, percentToGiveToAffiliate
                                        ));
                                        addAffiliateAmount(affiliatePayoutAmounts, netProfitPerPlayer, percentToGiveToAffiliate, tertiaryMintParentId, s);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //if no payouts need to occur, break early
        if (affiliatePayoutAmounts.size() == 0) return;

        //gather and build up the messages that will be used for affiliate payouts
        List<Tuple<String>> subjectList = _dao.getMultiLocalizationValues(UUID_REFERRAL_PAYOUT_SUBJECT, "systemMessage");
        Map<String, String> subjectMap = BaseSmMessageHandler.tupleListToMap(subjectList);

        List<Tuple<String>> messageForm1List = _dao.getMultiLocalizationValues(UUID_REFERRAL_MESSAGE_FORM1, "systemMessage");
        Map<String, String> messageForm1Map = BaseSmMessageHandler.tupleListToMap(messageForm1List);

        List<Tuple<String>> messageForm2List = _dao.getMultiLocalizationValues(UUID_REFERRAL_MESSAGE_FORM2, "systemMessage");
        Map<String, String> messageForm2Map = BaseSmMessageHandler.tupleListToMap(messageForm2List);

        List<Tuple<String>> messageForm3List = _dao.getMultiLocalizationValues(UUID_REFERRAL_MESSAGE_FORM3, "systemMessage");
        Map<String, String> messageForm3Map = BaseSmMessageHandler.tupleListToMap(messageForm3List);

        //for now, piggy backing on this pref. will probably change this in a future iteration so this notification can be set independent of the round start pref
        int prefType = ISnowyowlService.NOTIFICATION_PREF_TYPE_ROUND_START;

        //generate the short url for the referral page
        String referralShortUrl;
        try {
            referralShortUrl = getShortUrl(_urlShortenerService, _shortUrlDomain, _shortUrlPrefix, "https://millionize.com/play/home/referalls");
        } catch (IOException | UrlShortenerException e) {
            _logger.error("unable to generate short url for https://millionize.com/play/home/referalls", e);
            referralShortUrl = "";
        }

        //generate the from email to use
        Map<Integer, EmailAddress> fromEmailMap = new HashMap<>();
        String[] addrsByAppId = _emailFromAddrs.split(",");
        String[] namesByAppId = _emailFromNames.split(",");

        for (int i=0; i<addrsByAppId.length; i++) {
            String[] ss = addrsByAppId[i].split(":");
            int appId = Integer.parseInt(ss[0]);
            String emailAddr = ss[1];
            String emailName = namesByAppId[i].split(":")[1];

            fromEmailMap.put(appId, new EmailAddress(emailAddr, emailName));
        }

        //do the affiliate payouts
        for (long affiliateSubscriberId: affiliatePayoutAmounts.keySet()) {

            //determine which type of notification to send this subscriber for this notification type
            List<NotificationPref> prefs = _notificationService.getPrefsForSubscriber(affiliateSubscriberId);

            NotificationPref pref = prefs.stream()
                .filter(p -> p.getPrefType() == prefType)
                .findFirst()
                .orElseGet(() -> {
                    NotificationPref p = new NotificationPref();
                    p.setValue("NONE");
                    return p; }
                );

            Subscriber s = _identityService.getSubscriberById(affiliateSubscriberId);

            List<ReferralStruct> referralsForAffiliate = affiliatePayoutAmounts.get(affiliateSubscriberId);

            //for each referral, add a cash transaction and send a notification
            for (ReferralStruct rs : referralsForAffiliate) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug(MessageFormat.format(
                        "affiliatePayout::adding affiliate payout amount of {0} to affiliate {1,number,#} due to subscriber {2}",
                        rs.amount, affiliateSubscriberId, rs.nickname));
                }
                _shoutContestService.addCashPoolTransaction(
                        affiliateSubscriberId, rs.amount, CashPoolTransaction2.TYPE.PAYOUT_REFERRAL, rs.nickname, null, game.getId());

                //send a notification to the affiliate
                switch (pref.getValue())
                {
                    case "SMS": {
                        String smsLocalizedBody = MessageFormat.format(LocalizationHelper.getLocalizedString(messageForm1Map, s.getLanguageCode()),
                                rs.amount, _appName, rs.nickname, game.getGameName(s.getLanguageCode()), referralShortUrl);

                        if (smsLocalizedBody.length() > MAX_SMS_LENGTH) {
                            smsLocalizedBody = MessageFormat.format(LocalizationHelper.getLocalizedString(messageForm2Map, s.getLanguageCode()),
                                rs.amount, _appName, rs.nickname, referralShortUrl);
                        }

                        if (smsLocalizedBody.length() > MAX_SMS_LENGTH) {
                            smsLocalizedBody = MessageFormat.format(LocalizationHelper.getLocalizedString(messageForm3Map, s.getLanguageCode()),
                                rs.amount, _appName, referralShortUrl);
                        }

                        sendSms(
                            _twilioFromNumber, s.getPhone(), smsLocalizedBody, _twilioAccountSid, _twilioAuthToken,
                            _logger, affiliateSubscriberId, _identityService, true);
                    }
                    break;

                    case "APP_PUSH": {
                        String pushLocalizedTitle = LocalizationHelper.getLocalizedString(subjectMap, s.getLanguageCode());

                        String pushLocalizedMessage = MessageFormat.format(LocalizationHelper.getLocalizedString(messageForm1Map, s.getLanguageCode()),
                                rs.amount, _appName, rs.nickname, game.getGameName(s.getLanguageCode()), referralShortUrl);

                        sendCustomPush(
                                _transactionManager, _dao, _pushService, _logger,
                                affiliateSubscriberId, s.getLanguageCode(), s.getContextId(), "TODO:apsCategory", pushLocalizedTitle, pushLocalizedMessage, "REFERRAL_PAYOUT", null);
                    }
                    break;

                    case "EMAIL": {
                        String emailLocalizedSubject = LocalizationHelper.getLocalizedString(subjectMap, s.getLanguageCode());

                        String emailLocalizedBody = MessageFormat.format(LocalizationHelper.getLocalizedString(messageForm1Map, s.getLanguageCode()),
                                rs.amount, _appName, rs.nickname, game.getGameName(s.getLanguageCode()), referralShortUrl);

                        sendEmail(_logger, affiliateSubscriberId, _identityService, _postOfficeService, fromEmailMap.get(s.getContextId()), emailLocalizedSubject, emailLocalizedBody, null, null);
                    }
                    break;
                }
            }
        }
    }

    //wrapped in a transaction
    private Map<Integer, AffiliatePlan> _affiliateMap = new MaxSizeHashMap<Integer, AffiliatePlan>().withMaxSize(10);
    private AffiliatePlan getAffiliatePlan(int affiliatePlanId)
    {
        AffiliatePlan plan = _affiliateMap.get(affiliatePlanId);
        if (plan == null) {
            plan = _dao.getAffiliatePlan(affiliatePlanId);

            if (plan == null) {
                _logger.warn("unable to locate affiliate plan for affiliatePlanId: " + affiliatePlanId);
                plan = new AffiliatePlan();
            }

            _affiliateMap.put(affiliatePlanId, plan);
        }
        return plan;
    }

    private void addAffiliateAmount(Map<Long, List<ReferralStruct>> affiliatePayoutAmounts, double netProfitPerPlayer, double percentToGiveToAffiliate, long affiliateId, Subscriber player)
    {
        //now that a percentage has been found for the affiliate, add to their tracked amount by that much
        if (percentToGiveToAffiliate > 0D) {
            double amountToGive = percentToGiveToAffiliate * netProfitPerPlayer;

            List<ReferralStruct> referralsSoFar = affiliatePayoutAmounts.get(affiliateId);
            if (referralsSoFar == null) {
                referralsSoFar = new ArrayList<>();
                affiliatePayoutAmounts.put(affiliateId, referralsSoFar);
            }
            referralsSoFar.add(new ReferralStruct(amountToGive, player.getNickname()));
        }
    }

}
