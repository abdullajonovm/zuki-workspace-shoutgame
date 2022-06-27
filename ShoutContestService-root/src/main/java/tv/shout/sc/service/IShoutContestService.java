package tv.shout.sc.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.meinc.gameplay.domain.App;
import com.meinc.webcollector.message.handler.IMessageTypeHandler;

import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.CouponBatch;
import tv.shout.sc.domain.CouponCode;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Game.GAME_STATUS;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.ManualRedeemRequest;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.Match.MATCH_STATUS;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.MatchQueue;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;

public interface IShoutContestService
extends IMessageTypeHandler
{
    enum MATCH_QUEUE_ALGORITHM {
         NATURAL_ORDER      //return records in the order in which they naturally occur in the database result set (fastest method)
        ,RANDOM             //randomize the list before pairing
        ,BY_OPPOSITE_SKILL  //sort based on the RoundPlayer skill, with 1st and last being paired, then 2nd, next to last, etc
        //,BY_RANK
    }

    public static final String SERVICE_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "ShoutContestService";
    public static final String SERVICE_INTERFACE = "IShoutContestService";
    public static final String SERVICE_VERSION = "1.0";

    static final String COUNTRY_UUID = "f983f72c-e72c-4a69-be9a-1ab53276ecc5";

    void start();
    void stop();

    //app
    Integer getContextId(Map<String, String> props); //null = no contextId found for given values (appId must be provided and map to an appName)
    App getAppById(int appId);
    Set<String> getAppBundleIds(int appId);

    //cash pool
    List<CashPoolTransaction2> getCashPoolTransactionsForSubscriberForTypes(long subscriberId, List<String> transactionTypes);
    Double getTotalBalance(long subscriberId);
    Double getAvailableBalance(long subscriberId);
    void addCashPoolTransaction(long subscriberId, double amount, CashPoolTransaction2.TYPE type, String desc, Integer receiptId, String contextUuid);
    ManualRedeemRequest getManualRedeemRequest(int manualRedeemRequestId);
    void markManualRedeemRequestFulfilled(int manualRedeemRequestId);
    List<ManualRedeemRequest> getOutstandingManualRedeemRequests();
    List<CashPoolTransaction2> getJoinedAndAbandonedForContext(String contextId);

    // Game
    List<Game> getGamesByStatus(Game.GAME_STATUS status);
    List<Game> getGamesByStatusAndAllowableAppId(Game.GAME_STATUS status, int appId);
    List<Game> getGamesByStatusAndEngine(String gameEngine, Game.GAME_STATUS... statuses);
    void addGame(Game game);
    void updateGame(Game game);
    void updateGameThin(Game game);
    void updateGameStatus(String gameId, Game.GAME_STATUS newStatus);
    void setGameSmsSent(String gameId);
    Game getGame(String gameId);
    Game getGameNoFat(String gameId);
    Game getGameViaInviteCode(String inviteCode);
    List<Game> getSubscriberGamesByStatus(Game.GAME_STATUS status, int appId, long subscriberId);
    List<String> getSubscriberGameIdsByStatuses(int appId, long subscriberId, Game.GAME_STATUS... statuses);
    List<Game> getGamesByStatusByGameIds(int appId, Game.GAME_STATUS status, List<String> gameIds);
//    void resetGame(String gameId);
    void cancelGame(String gameId);
    void openGame(String gameId);
    void cloneGame(String gameId, String inviteCode, List<Date> expectedStartDatesForEachRoundBeingCloned, Map<String, String> gameNames, List<String> roundIdsToClone);
    String generateRandomString(int len);
    List<String> getPrivateGameIdsForSubscriberByStatusAndEngine(long subscriberId, String gameEngine, Game.GAME_STATUS... statuses);

    // GamePlayer
    GamePlayer getGamePlayer(String gameId, long subscriberId);
    void removeGamePlayer(long subscriberId, String gameId);
    List<GamePlayer> getGamePlayers(long subscriberId);
    List<GamePlayer> getGamePlayersForGame(String gameId);
    List<GamePlayer> getGamePlayersWithoutBots(String gameId, List<Long> botIds);

    /**
     * @param gamePlayer
     * @param nextRoundId if null, the GamePlayer.nextRoundId will set to the first available round, otherwise it will use the provided round
     * @param roundStatuses only consider rounds that have these statuses
     * @return
     */
    boolean addGamePlayer(GamePlayer gamePlayer, String nextRoundId, Round.ROUND_STATUS... roundStatuses);

    void removeGamePlayer(GamePlayer existingGamePlayerRecord);

    // Game / GamePlayer common collector methods (to be passed through from the specific game engine collector)
//    Map<String, Object> joinGame(Map<String, String> props, String messageId, Subscriber subscriber) throws PublishResponseError;
//    Map<String, Object> ensureGamePlayer(Map<String, String> props, String messageId, Subscriber subscriber) throws PublishResponseError;

    // Round / RoundPlayer
    List<Round> getRoundsForGameForStatus(String gameId, Round.ROUND_STATUS... statuses);
    void addRound(Round round);
    void updateRound(Round round);
    void updateRoundStatus(String roundId, boolean finalRound, Round.ROUND_STATUS newStatus);
    void updateRoundStatusAndPlayerCount(String roundId, Round.ROUND_STATUS newStatus, int newPlayerCount);
    void addRoundPlayer(RoundPlayer roundPlayer, Round.ROUND_TYPE roundType);
    Round getRound(String roundId);
    Round getRoundForGameAndSequence(String gameId, int sequence);
    List<Round> getRoundsForGame(String gameId);
    List<RoundPlayer> getRoundPlayersForGame(String gameId, long subscriberId);
    List<RoundPlayer> getMostRecentRoundPlayerForEachPlayerForGame(String gameId);
    List<Long> getSubscriberIdsForRound(String roundId);
    RoundPlayer getRoundPlayerByDetermination(String gameId, String roundId, long subscriberId, RoundPlayer.ROUND_PLAYER_DETERMINATION determination);
    RoundPlayer getMostRecentRoundPlayer(String gameId, long subscriberId);
    RoundPlayer getRoundPlayer2(String roundId, long subscriberId);
    List<RoundPlayer> getMostRecentRoundPlayersForGame(String gameId, List<Round.ROUND_TYPE> roundTypes);
    RoundPlayer getRoundPlayer(String roundPlayerId);
    void updateGamePlayer(GamePlayer gamePlayer);
    void updateRoundPlayer(RoundPlayer roundPlayer);
    void updateRoundPlayerDetermination(String roundPlayerId, RoundPlayer.ROUND_PLAYER_DETERMINATION determination);
    int getMatchCountForRound(String roundId);

    // Match / MatchPlayer / MatchQueue
    Match addMatch(Match match);
    MatchPlayer addMatchPlayer(String gameId, String roundId, String matchId, String roundPlayerId, long subscriberId);
    List<MatchPlayer> getMatchPlayersForMatch(String matchId);
    void updateMatch(Match match);
    void updateMatchPlayer(MatchPlayer matchPlayer);

    void enqueueMatchQueue(String gameId, String roundId, String roundPlayerId, long subscriberId/*, boolean demoStore*/);
    List<MatchQueue> getMatchPlayersNotQueuedOlderThan(String gameId, String roundId, long ageMs);
    List<Long> getSubscriberIdsThatWereNeverMatchedForGame(String gameId);
    void removeSubscribersThatWereNeverMatchedForGame(String gameId);
    MatchQueue cancelMatchQueue(String gameId, String roundId, long subscriberId);
    MatchQueue getPlayerAvailableMatchQueue(String gameId, String roundId, long subscriberId);
    List<List<MatchQueue>> getMatchQueues(String gameId, String roundId, int matchPlayerCount, MATCH_QUEUE_ALGORITHM algorithm);
    List<MatchQueue> getOpenMatchQueues(String gameId, String roundId, int matchPlayerCount);
    List<Match> getMatchesByEngineAndStatus(String gameEngine, String engineType, MATCH_STATUS... statuses);
    List<Match> getMatchesByEngineAndStatusAndGame(String gameEngine, String engineType, String gameId, MATCH_STATUS... statuses);
    List<Match> getMatchesByRoundAndStatus(String roundId, MATCH_STATUS... statuses);

    //coupon
    CouponBatch createCouponBatch(CouponBatch couponBatch, int quantity);
    void cancelCouponBatch(int batchId);
    CouponCode getCouponCode(String couponCode);
    void cancelCouponCode(String couponCode);
    List<CouponBatch> getCouponBatches();
    List<CouponCode> getCouponsForBatch(int batchId);
    List<CouponCode> getUnusedCoupons();
    List<CouponCode> getCouponsRedeemedSince(Date since);
    void assignCouponsToSubscribersFromBatch(List<Long> subscriberIds, int batchId);
    void markCouponAsRedeemed(String couponCode, long subscriberId);

    // Misc
    List<String> publishWebDataStoreGameListDoc(String gameEngineName, Game.GAME_TYPE gameType, String documentName, GAME_STATUS... statuses);
    void publishGameToWds(String gameId, Map<String, Object> extras);
    Map<String, Integer> publishGamePlayerCountToWds(String gameId, List<Long> botIds);
    String aesEncrypt(String key, String initVector, String plainTextMessage);

}
