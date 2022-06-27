package tv.shout.sm.test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.meinc.gameplay.domain.Tuple;
import com.meinc.identity.domain.Subscriber;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Game.GAME_STATUS;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.LocalizationHelper;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.collector.BaseSmMessageHandler;

public class SmsNotifier
{
    public static final String SMS_BRACKET_STARTING_NOTIFICATION_UUID = "aa1510af-12b1-411d-ae9a-205a42abc378";
    private static Logger _logger = Logger.getLogger(SmsNotifier.class);

    private boolean _docProducer = true;
    private long _pregameSmsNotificationTimeMins = 5;
    private String _twilioFromNumber;
    private String _twilioAccountSid;
    private String _twilioAuthToken;
    private MockIdentityService _identityService = new MockIdentityService();
    private MockDao _dao = new MockDao();
    private MockShoutContestService _shoutContestService = new MockShoutContestService();

    private static class GameIdAndExpectedBracketStartDate
    {
        public String gameId;
        public Date expectedBracketStartDate;

        public GameIdAndExpectedBracketStartDate(String gameId, Date expectedBracketStartDate)
        {
            this.gameId = gameId;
            this.expectedBracketStartDate = expectedBracketStartDate;
        }
    }

    private static class MockDao
    {
        public List<Tuple<String>> getMultiLocalizationValues(String uuid, String type)
        {
            Tuple<String> v = new Tuple<>();
            v.setKey("en");
            v.setVal("Your Daily Millionaire game is about to start! Jump in and get ready to play so you can win: https://millionize.net/play/game/{0}");

            List<Tuple<String>> result = new ArrayList<>();
            result.add(v);

            return result;
        }
    }

    private static class MockShoutContestService
    {
        public List<Game> getGamesByStatus(GAME_STATUS open)
        {
            Game g = new Game();
            g.setId("foobar");

            return Arrays.asList(g);
        }

        public List<Round> getRoundsForGame(String id)
        {
            Date tPlus3Mins = new Date(System.currentTimeMillis() + 180_000L);

            Round r1 = new Round();
            r1.setRoundType(Round.ROUND_TYPE.BRACKET);
            r1.setGameId("foobar");
            r1.setRoundSequence(1);
            r1.setExpectedOpenDate(tPlus3Mins);

            return Arrays.asList(r1);
        }

        public List<GamePlayer> getGamePlayersForGame(String gameId)
        {
            //return a list of gameplayers, with the subscriberId set for each
            GamePlayer gp1 = new GamePlayer();
            gp1.setSubscriberId(8);
            GamePlayer gp2 = new GamePlayer();
            gp2.setSubscriberId(12);

            return Arrays.asList(gp1, gp2);
        }

        public void setGameSmsSent(String gameId)
        {
            //no-op
        }
    }

    private static class MockIdentityService
    {
        public Subscriber getSubscriberById(long subscriberId)
        {
            Subscriber s = new Subscriber();
            s.setSubscriberId(subscriberId);
            s.setPhone("1-800-what-ever");
            s.setPhoneVerified(true);
            return s;
        }
    }

    private void sendSms(String twilioFromNumber, String phone, String message, String twilioAccountSid,
            String twilioAuthToken, Logger _logger2, long subscriberId, MockIdentityService identityService, boolean b)
    {
        System.out.println(MessageFormat.format("sending sms to: {0,number,#}. msg: {1}", subscriberId, message));
    }

    public void run()
    {
        if (!_docProducer) return;

        //get all games in the OPEN status
        List<Game> openGames = _shoutContestService.getGamesByStatus(Game.GAME_STATUS.OPEN);

        //filter to only those that haven't been notified
        List<Game> notNotifiedGames = openGames.stream().filter(g -> g.getStartingSmsSentDate() == null).collect(Collectors.toList());

        //filter to only those games that start in the next X minutes
        long startsInInterval = 1_000L * 60L * _pregameSmsNotificationTimeMins;
        List<String> gameIdsNeedingToBeNotified = new ArrayList<>();
        notNotifiedGames.forEach(g -> {
            Optional<GameIdAndExpectedBracketStartDate> xxx = _shoutContestService.getRoundsForGame(g.getId()).stream()
                .filter(r -> r.getRoundType() == Round.ROUND_TYPE.BRACKET)
                .sorted( (r1, r2) -> r1.getRoundSequence() < r2.getRoundSequence() ? - 1 : 1)
                .map(r -> new GameIdAndExpectedBracketStartDate(r.getGameId(), r.getExpectedOpenDate()))
                .filter(x -> System.currentTimeMillis() + startsInInterval >= x.expectedBracketStartDate.getTime())
                .findFirst();

            if (xxx.isPresent()) {
                gameIdsNeedingToBeNotified.add(xxx.get().gameId);
            }
        });

        if (gameIdsNeedingToBeNotified.isEmpty()) {
            return;
        }

        List<Tuple<String>> bracketPlayBeginsMsgList = null;
        //DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        //TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        //try {
            bracketPlayBeginsMsgList = _dao.getMultiLocalizationValues(SMS_BRACKET_STARTING_NOTIFICATION_UUID, "systemMessage");

        //    _transactionManager.commit(txStatus);
        //    txStatus = null;
        //} finally {
        //    if (txStatus != null) {
        //        _transactionManager.rollback(txStatus);
        //        txStatus = null;
        //        return;
        //    }
        //}
        Map<String, String> bracketPlayBeginsMsgMap = BaseSmMessageHandler.tupleListToMap(bracketPlayBeginsMsgList);

        //loop each game
        gameIdsNeedingToBeNotified.forEach(gameId -> {
            List<Subscriber> smsRecipients = new ArrayList<>();
            //grab all the subscribers in the game and filter to ones that have a phone #
            _shoutContestService.getGamePlayersForGame(gameId).forEach(gp -> {
                Subscriber s = _identityService.getSubscriberById(gp.getSubscriberId());
                if (s.getPhone() != null && s.isPhoneVerified()) {
                    smsRecipients.add(s);
                }
            });

            //send an sms to each of those subscribers
            smsRecipients.forEach(s -> {
                String message = MessageFormat.format(LocalizationHelper.getLocalizedString(
                        bracketPlayBeginsMsgMap, s.getLanguageCode()), gameId);

                sendSms(_twilioFromNumber, s.getPhone(), message, _twilioAccountSid, _twilioAuthToken, _logger, s.getSubscriberId(), _identityService, false);
            });

            //mark game as having been notified
            _shoutContestService.setGameSmsSent(gameId);
        });
    }

    public static void main(String[] args)
    throws Exception
    {
        SmsNotifier notifier = new SmsNotifier();
        notifier.run();
    }
}
