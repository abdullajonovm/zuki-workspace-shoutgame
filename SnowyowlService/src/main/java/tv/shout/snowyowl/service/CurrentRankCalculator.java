package tv.shout.snowyowl.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.service.IShoutContestService;

public class CurrentRankCalculator
{
//    private static Logger _logger = Logger.getLogger(CurrentRankCalculator.class);

    @Autowired
    protected IShoutContestService _shoutContestService;

    private Lock _lock = new ReentrantLock();

    /**
     * if a clear call comes in for a game that is currently being calculated, this will be set so that after the calculation is complete
     * and interested parties are notified, the game rankings will be cleared
     */
    private Set<String> _clearAfterCalculating = new HashSet<>();

    /**
     * Contains the current rank mapping for a game.
     * gameId -> subscriberId -> rank
     */
    private Map<String, Map<Long, Double>> _gameToSubscriberToRankMap = new HashMap<>();

    /**
     * If a game calculation is in progress, this contains everyone who wants to be notified of the results. It is cleared out after everyone in that game is notified.
     * gameId -> subscriberId -> callback
     */
    private Map<String, Map<Long, CurrentRankCalculatorCallback>> _interestedParties = new HashMap<>();

    /**
     * the list of games that are currently being calculated
     */
    private Set<String> _gamesCurrentlyBeingCalculated = new HashSet<>();

    public interface CurrentRankCalculatorCallback
    {
        void currentRank(String gameId, long subscriberId, Double rank);
    }

    /**
     * Clear out the current rankings (if any) for the given game. If there is a calculation in progress for this game,
     * the calculation will complete and any interested parties will still be notified.
     *
     * @param gameId which game to clear
     */
    public void clear(String gameId)
    {
        _lock.lock();
        try {
            if (_gamesCurrentlyBeingCalculated.contains(gameId)) {
                //calculation ongoing for game. add a notice that it will be stale once the calculation completes
                _clearAfterCalculating.add(gameId);
            } else {
                //not currently being calculated. clear it out
                _gameToSubscriberToRankMap.remove(gameId);
            }
        } finally {
            _lock.unlock();
        }
    }

    /**
     * Retrieve (calculate first if necessary) the current rank of the given subscriber for the given game.
     *
     * @param gameId which game
     * @param subscriberId which subscriber
     * @param callback callback function with the current ranking
     */
    public void getCurrentRank(String gameId, long subscriberId, CurrentRankCalculatorCallback callback)
    {
        _lock.lock();
        try {
            if (_gamesCurrentlyBeingCalculated.contains(gameId)) {
//_logger.info("getCurrentRank for game: " + gameId + ", but calculation ongoing");
                //calculation currently ongoing. add to interested parties callback
                Map<Long, CurrentRankCalculatorCallback> interestedParties = _interestedParties.get(gameId);
                if (interestedParties == null) {
                    interestedParties = new HashMap<>();
                }
                interestedParties.put(subscriberId, callback);
//_logger.info("getCurrentRank, interestedParties, subscriberId: " + subscriberId + ", callback: " + callback);
                _interestedParties.put(gameId, interestedParties);

            } else {
                //no calculation ongoing. see if the results are cached
                Map<Long, Double> rankMap = _gameToSubscriberToRankMap.get(gameId);
                if (rankMap != null) {
                    //results are cached. return them
                    Double rank = rankMap.get(subscriberId);
//_logger.info("getCurrentRank, no ongoing calculation. results are cached. gameId: " + gameId + ", rank: " + rank + ", sId: " + subscriberId + ", callback: " + callback);
                    callback.currentRank(gameId, subscriberId, rank);

                } else {
//_logger.info("getCurrentRank, no ongoing calculation. results are not cached. gameId: " + gameId + ", sId: " + subscriberId);
                    //add to interested parties callback and begin the calculation
                    // (after first clearing out any old data that might be there from a previous calculation)
                    Map<Long, CurrentRankCalculatorCallback> interestedParties = new HashMap<>();
                    interestedParties.put(subscriberId, callback);
//_logger.info("getCurrentRank, interestedParties, subscriberId: " + subscriberId + ", callback: " + callback);
                    _interestedParties.put(gameId, interestedParties);

                    _gamesCurrentlyBeingCalculated.add(gameId);

                    _clearAfterCalculating.remove(gameId);

                    calculateRanksForGame(gameId);
                }
            }
        } finally {
            _lock.unlock();
        }
    }

    private void calculateRanksForGame(String gameId)
    {
        //get them, sort them
        List<RoundPlayer> roundPlayers = _shoutContestService.getMostRecentRoundPlayerForEachPlayerForGame(gameId);
        List<RoundPlayer> sortedRoundPlayers = roundPlayers.stream()
            .sorted( Comparator.comparing(RoundPlayer::getRank, Comparator.nullsLast(Comparator.reverseOrder())) )
            .collect(Collectors.toList());

        _lock.lock();
        try {
            //Map<Integer, Double> rankMap = _gameToSubscriberToRankMap.get(gameId);
            Map<Long, Double> rankMap = new HashMap<>();
            _gameToSubscriberToRankMap.put(gameId, rankMap);

            //apply the rank
            for (int i=0; i<sortedRoundPlayers.size(); i++) {
                rankMap.put(sortedRoundPlayers.get(i).getSubscriberId(), (double)i);
            }

            //notify everyone who wanted to know
            Map<Long, CurrentRankCalculatorCallback> interestedParties = _interestedParties.get(gameId);
            for (long subscriberId : interestedParties.keySet()) {
                CurrentRankCalculatorCallback callback = interestedParties.get(subscriberId);

                Double rank = rankMap.get(subscriberId);
//_logger.info("ranks have been calculated. notifying interested parties. gameId: " + gameId + ", sId: " + subscriberId + ", rank: " + rank + ", callback: " + callback);
                callback.currentRank(gameId, subscriberId, rank);
            }

            //clear out interested parties and mark game as no longer being calculated
            _interestedParties.remove(gameId);
            _gamesCurrentlyBeingCalculated.remove(gameId);

            //if the game calculation is now stale, clear out the calculation
            if (_clearAfterCalculating.contains(gameId)) {
                _gameToSubscriberToRankMap.remove(gameId);
                _clearAfterCalculating.remove(gameId);
            }
        } finally {
            _lock.unlock();
        }
    }
}
