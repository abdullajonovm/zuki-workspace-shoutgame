package tv.shout.snowyowl.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.domain.Trigger;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webdatastore.service.IWebDataStoreService;

import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.util.DateUtil;

public class SubscriberStatsHandler
implements WdsPublisher
{
    private static Logger _logger = Logger.getLogger(SubscriberStatsHandler.class);
    private static final int PAGE_SIZE = 100;

    @Autowired
    private ITriggerService _triggerService;

    @Autowired
    private IIdentityService _identityService;

    @Resource(name="webDataStoreService")
    private IWebDataStoreService _wdsService;

    @Autowired
    private IDaoMapper _dao;

    @Autowired
    private PlatformTransactionManager _transactionManager;

    //fire off a subscriber stats trigger message which will increment the given stat type by the given value
    public void incrementSubscriberStat(long subscriberId, SubscriberStats.STATS_TYPE statsType, int incrementBy)
    {
        //these values don't matter in our case
        String source = null;
        Set<String> bundleIds = null;
        int contextId = -1;

        //build up the payload
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("subscriberId", subscriberId);
        payload.put("statsType", statsType.toString());
        payload.put("incrementBy", incrementBy);

        //fire off the message
        _triggerService.enqueue(ISnowyowlService.TRIGGER_KEY_SUBSCRIBER_STATS_MESSAGE, payload, ISnowyowlService.TRIGGER_SERVICE_ROUTE, source, bundleIds, contextId);
    }

    //this method is inside of a transaction. it is called by the service whenever a subscriber stats trigger message is received
    public void handleTriggerMessage(Trigger trigger)
    {
        //pull data from the payload
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) trigger.getPayload();
        int subscriberId = (Integer) payload.get("subscriberId");
        SubscriberStats.STATS_TYPE statsType = SubscriberStats.STATS_TYPE.valueOf((String)payload.get("statsType"));
        int incrementBy = (Integer) payload.get("incrementBy");

        //grab (or create) the subscriber stats object
        SubscriberStats stats = _dao.getSubscriberStats(subscriberId);
        boolean doInsert;
        if (stats != null) {
            doInsert = false;

        } else {
            doInsert = true;
            stats = new SubscriberStats();
            stats.setSubscriberId(subscriberId);
        }

        //increment the given value
        switch (statsType)
        {
            case BRACKET_ROUNDS_PLAYED:
                stats.setBracketRoundsPlayed(stats.getBracketRoundsPlayed() + incrementBy);
                break;

            case CUMULATIVE_QUESTION_SCORE:
                stats.setCumulativeQuestionScore(stats.getCumulativeQuestionScore() + incrementBy);
                break;

            case GAMES_PLAYED:
                stats.setGamesPlayed(stats.getGamesPlayed() + incrementBy);
                break;

            case POOL_ROUNDS_PLAYED:
                stats.setPoolRoundsPlayed(stats.getPoolRoundsPlayed() + incrementBy);
                break;

            case QUESTIONS_ANSWERED:
                stats.setQuestionsAnswered(stats.getQuestionsAnswered() + incrementBy);
                break;

            case QUESTIONS_CORRECT:
                stats.setQuestionsCorrect(stats.getQuestionsCorrect() + incrementBy);
                break;

            case AFFILIATE_PLAN_ID:
                stats.setAffiliatePlanId(incrementBy);
                break;
        }

        //update the database
        if (doInsert) {
            _dao.insertSubscriberStats(stats);
        } else {
            _dao.updateSubscriberStats(stats);
        }
    }

    //this method will start a separate thread to do this work, as it will be quite expensive
    //it will publish each of the subscriber stats documents using the current data from the database
    /**
     * @param gameId optional. if provided, it will also publish a filtered CUMULATIVE_QUESTION_SCORE subscriber stats document with players in the given game
     * @param subscriberIds optional. if provided, a list of subscriber ids to filter by for the current gameId (i.e. who's still in the game when this is called)
     */
    public void publishSubscriberStatsDocuments(String gameId, List<Long> subscriberIds)
    {
        new Thread() {
            @Override
            public void run() {
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                try {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("about to publish subscriber stats documents");
                    }

                    long b = System.currentTimeMillis();
                    publishSubscriberStatsDocumentsRefactor(gameId, subscriberIds);
                    long e = System.currentTimeMillis();
                    if (_logger.isDebugEnabled()) {
                        _logger.debug(MessageFormat.format("publishing subscriber stats documents took {0} ms", (e-b)));
                    }

                    _transactionManager.commit(txStatus);
                    txStatus = null;

                } catch (RuntimeException e) {
                    _logger.error("uncaught runtime exception", e);
                    throw e;

                } finally {
                    if (txStatus != null) {
                        _transactionManager.rollback(txStatus);
                        txStatus = null;
                        throw new IllegalStateException("transaction failed");
                    }
                }
            }
        }.start();
    }

    //this is inside of a thread and a transaction
    private void publishSubscriberStatsDocumentsRefactor(String gameId, List<Long> subscriberIds)
    {
        Date publishDate = new Date();
        List<Integer> ranks;

        //grab the current data from the db
        List<SubscriberStats> stats = _dao.getAllSubscriberStats();

        //GAMES_PLAYED
        stats = stats.stream()
            .sorted(Comparator.comparing(SubscriberStats::getGamesPlayed, Comparator.reverseOrder()))
            .collect(Collectors.toList());
        ranks = getRanksFromStats(stats, SubscriberStats.STATS_TYPE.GAMES_PLAYED);
        publishDocument(stats, SubscriberStats.STATS_TYPE.GAMES_PLAYED, ranks, publishDate);

        //BRACKET_ROUNDS_PLAYED
        stats = stats.stream()
            .sorted(Comparator.comparing(SubscriberStats::getBracketRoundsPlayed, Comparator.reverseOrder()))
            .collect(Collectors.toList());
        ranks = getRanksFromStats(stats, SubscriberStats.STATS_TYPE.BRACKET_ROUNDS_PLAYED);
        publishDocument(stats, SubscriberStats.STATS_TYPE.BRACKET_ROUNDS_PLAYED, ranks, publishDate);

        //POOL_ROUNDS_PLAYED
        stats = stats.stream()
            .sorted(Comparator.comparing(SubscriberStats::getPoolRoundsPlayed, Comparator.reverseOrder()))
            .collect(Collectors.toList());
        ranks = getRanksFromStats(stats, SubscriberStats.STATS_TYPE.POOL_ROUNDS_PLAYED);
        publishDocument(stats, SubscriberStats.STATS_TYPE.POOL_ROUNDS_PLAYED, ranks, publishDate);

        //QUESTIONS_ANSWERED
        stats = stats.stream()
            .sorted(Comparator.comparing(SubscriberStats::getQuestionsAnswered, Comparator.reverseOrder()))
            .collect(Collectors.toList());
        ranks = getRanksFromStats(stats, SubscriberStats.STATS_TYPE.QUESTIONS_ANSWERED);
        publishDocument(stats, SubscriberStats.STATS_TYPE.QUESTIONS_ANSWERED, ranks, publishDate);

        //QUESTIONS_CORRECT
        stats = stats.stream()
            .sorted(Comparator.comparing(SubscriberStats::getQuestionsCorrect, Comparator.reverseOrder()))
            .collect(Collectors.toList());
        ranks = getRanksFromStats(stats, SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT);
        publishDocument(stats, SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, ranks, publishDate);

        //CUMULATIVE_QUESTION_SCORE
        stats = stats.stream()
            .sorted(Comparator.comparing(SubscriberStats::getCumulativeQuestionScore, Comparator.reverseOrder()))
            .collect(Collectors.toList());
        ranks = getRanksFromStats(stats, SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE);
        publishDocument(stats, SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, ranks, publishDate);

        if (gameId != null) {
            publishSizeLimitedCumulativeQuestionScoreDocument(gameId, subscriberIds, stats, ranks, publishDate);
        }
    }

    private static final int TOP_PLAYERS_SIZE_LIMIT = 20;

    private void publishSizeLimitedCumulativeQuestionScoreDocument(String gameId, List<Long> subscriberIds, List<SubscriberStats> stats, List<Integer> ranks, Date publishDate)
    {
        List<SubscriberStats> filteredStats = new ArrayList<>();
        List<Integer> filteredRanks = new ArrayList<>();

//_logger.info(MessageFormat.format(">>> unfiltered stats size: {0}, stats: {1}", stats.size(), stats) );
//_logger.info(MessageFormat.format(">>> unfiltered ranks size: {0}, ranks: {1}", ranks.size(), ranks) );
//_logger.info(MessageFormat.format(">>> game subscribers: {0}", subscriberIds));
        //find (up to) the top TOP_PLAYERS_SIZE_LIMIT players in the current list
        for (int i=0; i<stats.size() && filteredStats.size() < TOP_PLAYERS_SIZE_LIMIT; i++) {
            SubscriberStats stat = stats.get(i);
            int rank = ranks.get(i);

            //if this subscriber stat is in the list of acceptable subscribers, add it to the filtered list
            if (subscriberIds.contains(stat.getSubscriberId())) {
                filteredStats.add(stat);
                filteredRanks.add(rank);
            }
        }
//_logger.info(">>> filtered stats size: " + filteredStats.size());
//_logger.info(">>> filtered ranks size: " + filteredRanks.size());

        //build up the object to publish
        List<Map<String, Object>> leaderboardList = new ArrayList<>(filteredStats.size());

        for (int idx=0; idx<filteredStats.size(); idx++) {
            //build up the entries in the current page
            Map<String, Object> entry = new HashMap<>();
            Subscriber s = _identityService.getSubscriberById(filteredStats.get(idx).getSubscriberId());

            entry.put("subscriberId", s.getSubscriberId());
            entry.put("nickname", s.getNickname());
            entry.put("profilePhoto", s.getPhotoUrl());
            entry.put("profilePhotoSmall", s.getPhotoUrlSmall());
            entry.put("profilePhotoLarge", s.getPhotoUrlLarge());
            entry.put("rank", filteredRanks.get(idx));
            entry.put("value", filteredStats.get(idx).getCumulativeQuestionScore());

            leaderboardList.add(entry);
        }

        //publish
        String path = MessageFormat.format("/stats/leaderboard/{0}/CUMULATIVE_QUESTION_SCORE.json", gameId);
//_logger.info(">>> publishing doc: " + path);
        publishJsonWdsDoc(_logger, _wdsService, null, path, leaderboardList);
    }

    private void publishDocument(List<SubscriberStats> stats, SubscriberStats.STATS_TYPE statsType, List<Integer> ranks, Date publishDate)
    {
        //determine how many pages are needed
        int numPages = (ranks.size() / PAGE_SIZE) + (ranks.size() % PAGE_SIZE != 0 ? 1 : 0);

        int curPage = 0;
        Map<String, Object> map = new HashMap<>();

        for (int i=0; i<numPages; i++) {
            map.clear();

            map.put("page", curPage+1);
            map.put("totalPages", numPages);
            map.put("generatedAt", DateUtil.dateToIso8601(publishDate));
            map.put("totalEntries", ranks.size());

            List<Map<String, Object>> leaderboardList = new ArrayList<>();
            map.put("leaderboard", leaderboardList);

            for (int j=0; j<PAGE_SIZE && i*PAGE_SIZE+j < ranks.size(); j++) {
                int idx = i*PAGE_SIZE+j;

                int value;
                switch (statsType)
                {
                    case BRACKET_ROUNDS_PLAYED:
                        value = stats.get(idx).getBracketRoundsPlayed();
                        break;

                    case CUMULATIVE_QUESTION_SCORE:
                        value = stats.get(idx).getCumulativeQuestionScore();
                        break;

                    case GAMES_PLAYED:
                        value = stats.get(idx).getGamesPlayed();
                        break;

                    case POOL_ROUNDS_PLAYED:
                        value = stats.get(idx).getPoolRoundsPlayed();
                        break;

                    case QUESTIONS_ANSWERED:
                        value = stats.get(idx).getQuestionsAnswered();
                        break;

                    case QUESTIONS_CORRECT:
                        value = stats.get(idx).getQuestionsCorrect();
                        break;

                    default:
                        //won't happen
                        value = 0;
                        break;
                }

                //build up the entries in the current page
                Map<String, Object> entry = new HashMap<>();
                Subscriber s = _identityService.getSubscriberById(stats.get(idx).getSubscriberId());

                entry.put("subscriberId", s.getSubscriberId());
                entry.put("nickname", s.getNickname());
                entry.put("profilePhoto", s.getPhotoUrl());
                entry.put("profilePhotoSmall", s.getPhotoUrlSmall());
                entry.put("profilePhotoLarge", s.getPhotoUrlLarge());
                entry.put("rank", ranks.get(idx));
                entry.put("value", value);

                leaderboardList.add(entry);
            }

            //publish the current page
            String path = MessageFormat.format("/stats/leaderboard/{0}_{1,number,#}.json", statsType, curPage+1);
            publishJsonWdsDoc(_logger, _wdsService, null, path, map);

            curPage++;
        }
    }

    /**
     * This will use olympic style ranking (where if two tie, they both get the same place, and a spot is skipped).
     * For example, if the scores are 10,8,8,7, the rankings would be: 1, 2, 2, 4.
     *
     * @param stats the subscriber stats objects. this must be sorted (high to low) prior to calling the method
     * @param statsType the type of stat being ranked
     * @return the rankings the list of rankings (high to low) - index position matches the stats list
     */
    private List<Integer> getRanksFromStats(List<SubscriberStats> stats, SubscriberStats.STATS_TYPE statsType)
    {
        List<Integer> ranks = new ArrayList<>(stats.size());

        int currentRank = 1;
        int lastScore = -1;
        int numToSkip = 0;

        for (SubscriberStats stat : stats) {

            //get the current score for the given stat type
            int currentScore;
            switch (statsType)
            {
                case BRACKET_ROUNDS_PLAYED:
                    currentScore = stat.getBracketRoundsPlayed();
                    break;

                case CUMULATIVE_QUESTION_SCORE:
                    currentScore = stat.getCumulativeQuestionScore();
                    break;

                case GAMES_PLAYED:
                    currentScore = stat.getGamesPlayed();
                    break;

                case POOL_ROUNDS_PLAYED:
                    currentScore = stat.getPoolRoundsPlayed();
                    break;

                case QUESTIONS_ANSWERED:
                    currentScore = stat.getQuestionsAnswered();
                    break;

                case QUESTIONS_CORRECT:
                    currentScore = stat.getQuestionsCorrect();
                    break;

                default:
                    //won't happen
                    currentScore = 0;
                    break;
            }

            //assign the ranking
            if (currentScore != lastScore) {
                currentRank += numToSkip;
                ranks.add(currentRank);
                currentRank++;
                lastScore = currentScore;
                numToSkip = 0;
            } else {
                ranks.add(currentRank-1);
                numToSkip++;
            }
        }

        return ranks;
    }

}
