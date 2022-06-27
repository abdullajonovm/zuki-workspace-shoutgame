package tv.shout.so.question;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.meinc.gameplay.domain.App;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.IWebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;

import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.CashPoolTransaction2.TYPE;
import tv.shout.sc.domain.CouponBatch;
import tv.shout.sc.domain.CouponCode;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Game.GAME_STATUS;
import tv.shout.sc.domain.Game.GAME_TYPE;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.ManualRedeemRequest;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.Match.MATCH_STATUS;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.MatchQueue;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_STATUS;
import tv.shout.sc.domain.Round.ROUND_TYPE;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.domain.RoundPlayer.ROUND_PLAYER_DETERMINATION;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.sc.service.ShoutContestService;

public class MockShoutContestService
implements IShoutContestService
{
    private MockShoutContestServiceDaoMapper _dao;

    public MockShoutContestService(MockShoutContestServiceDaoMapper dao)
    {
        _dao = dao;
    }

    @Override
    public Game getGame(String gameId)
    {
//        Game game = _dao.getGame(gameId);
//        fattenGame(game);
//        return game;
        //TODO
        return null;
    }

    @Override
    public List<Round> getRoundsForGame(String gameId)
    {
        List<Round> rounds = _dao.getRoundsForGame(gameId);
        rounds.stream().forEach(r -> fattenRound(r));
        return rounds;
    }

//    private void fattenGame(Game game)
//    {
//        if (game == null) return;
//
//        game.setGameNames(ShoutContestService.tupleListToMap(_dao.getMultiLocalizationValues(game.getId(), "gameName")));
//        game.setGameDescriptions(ShoutContestService.tupleListToMap(_dao.getMultiLocalizationValues(game.getId(), "gameDescription")));
//        game.setFetchingActivityTitles(ShoutContestService.tupleListToMap(_dao.getMultiLocalizationValues(game.getId(), "fetchingActivityTitle")));
//        game.setSubmittingActivityTitles(ShoutContestService.tupleListToMap(_dao.getMultiLocalizationValues(game.getId(), "submittingActivityTitle")));
//        game.setGuideHtmls(ShoutContestService.tupleListToMap(_dao.getMultiLocalizationValues(game.getId(), "guideHtml")));
//        game.setAllowableAppIds(_dao.getGameAllowableAppIds(game.getId()));
//        game.setAllowableLanguageCodes(_dao.getGameAllowableLanguageCodes(game.getId()));
//        game.setForbiddenCountryCodes(_dao.getGameForbiddenCountryCodes(game.getId()));
//    }

    private void fattenRound(Round round)
    {
        if (round == null) return;

        //add the localization rows
        round.setRoundNames(ShoutContestService.tupleListToMap(_dao.getMultiLocalizationValues(round.getId(), "roundName")));

        //add the categories
        round.setCategories(_dao.getRoundCategories(round.getId()));
    }

    @Override
    public CollectorMessageResult createMessage(String requestPath, Map<String, String> requestHeaders,
            Map<String, String> requestParameters) throws BadRequestException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHandlerMessageType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handleMessages(List<CollectorMessage> messages, IWebCollectorMessageBuffer messageBuffer)
            throws BadRequestException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public HttpResponse handleSyncRequest(HttpRequest request) throws BadRequestException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void start()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Integer getContextId(Map<String, String> props)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public App getAppById(int appId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getAppBundleIds(int appId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CashPoolTransaction2> getCashPoolTransactionsForSubscriberForTypes(long subscriberId,
            List<String> transactionTypes)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getTotalBalance(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getAvailableBalance(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addCashPoolTransaction(long subscriberId, double amount, TYPE type, String desc, Integer receiptId,
            String contextUuid)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Game> getGamesByStatus(GAME_STATUS status)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Game> getGamesByStatusAndAllowableAppId(GAME_STATUS status, int appId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Game> getGamesByStatusAndEngine(String gameEngine, GAME_STATUS... statuses)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addGame(Game game)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateGame(Game game)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateGameStatus(String gameId, GAME_STATUS newStatus)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setGameSmsSent(String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Game> getSubscriberGamesByStatus(GAME_STATUS status, int appId, long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Game> getGamesByStatusByGameIds(int appId, GAME_STATUS status, List<String> gameIds)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelGame(String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void openGame(String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void cloneGame(String gameId, String inviteCode, List<Date> expectedStartDatesForEachRoundBeingCloned,
            Map<String, String> gameNames, List<String> roundIdsToClone)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public GamePlayer getGamePlayer(String gameId, long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeGamePlayer(long subscriberId, String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<GamePlayer> getGamePlayers(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GamePlayer> getGamePlayersForGame(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean addGamePlayer(GamePlayer gamePlayer, String nextRoundId, ROUND_STATUS... roundStatuses)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeGamePlayer(GamePlayer existingGamePlayerRecord)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Round> getRoundsForGameForStatus(String gameId, ROUND_STATUS... statuses)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addRound(Round round)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRound(Round round)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRoundStatus(String roundId, boolean finalRound, ROUND_STATUS newStatus)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRoundStatusAndPlayerCount(String roundId, ROUND_STATUS newStatus, int newPlayerCount)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addRoundPlayer(RoundPlayer roundPlayer, ROUND_TYPE roundType)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Round getRound(String roundId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Round getRoundForGameAndSequence(String gameId, int sequence)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RoundPlayer> getRoundPlayersForGame(String gameId, long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RoundPlayer> getMostRecentRoundPlayerForEachPlayerForGame(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Long> getSubscriberIdsForRound(String roundId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RoundPlayer getRoundPlayerByDetermination(String gameId, String roundId, long subscriberId,
            ROUND_PLAYER_DETERMINATION determination)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RoundPlayer getMostRecentRoundPlayer(String gameId, long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RoundPlayer> getMostRecentRoundPlayersForGame(String gameId, List<Round.ROUND_TYPE> roundTypes)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RoundPlayer getRoundPlayer(String roundPlayerId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateGamePlayer(GamePlayer gamePlayer)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRoundPlayer(RoundPlayer roundPlayer)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRoundPlayerDetermination(String roundPlayerId, ROUND_PLAYER_DETERMINATION determination)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int getMatchCountForRound(String roundId)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Match addMatch(Match match)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchPlayer addMatchPlayer(String gameId, String roundId, String matchId, String roundPlayerId,
            long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MatchPlayer> getMatchPlayersForMatch(String matchId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateMatch(Match match)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMatchPlayer(MatchPlayer matchPlayer)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void enqueueMatchQueue(String gameId, String roundId, String roundPlayerId, long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<MatchQueue> getMatchPlayersNotQueuedOlderThan(String gameId, String roundId, long ageMs)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Long> getSubscriberIdsThatWereNeverMatchedForGame(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeSubscribersThatWereNeverMatchedForGame(String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public MatchQueue cancelMatchQueue(String gameId, String roundId, long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MatchQueue getPlayerAvailableMatchQueue(String gameId, String roundId, long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<MatchQueue>> getMatchQueues(String gameId, String roundId, int matchPlayerCount,
            MATCH_QUEUE_ALGORITHM algorithm)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MatchQueue> getOpenMatchQueues(String gameId, String roundId, int matchPlayerCount)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Match> getMatchesByEngineAndStatus(String gameEngine, String engineType, MATCH_STATUS... statuses)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Match> getMatchesByEngineAndStatusAndGame(String gameEngine, String engineType, String gameId,
            MATCH_STATUS... statuses)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Match> getMatchesByRoundAndStatus(String roundId, MATCH_STATUS... statuses)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CouponBatch createCouponBatch(CouponBatch couponBatch, int quantity)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelCouponBatch(int batchId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public CouponCode getCouponCode(String couponCode)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelCouponCode(String couponCode)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<CouponBatch> getCouponBatches()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CouponCode> getCouponsForBatch(int batchId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CouponCode> getUnusedCoupons()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CouponCode> getCouponsRedeemedSince(Date since)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void assignCouponsToSubscribersFromBatch(List<Long> subscriberIds, int batchId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void markCouponAsRedeemed(String couponCode, long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> publishWebDataStoreGameListDoc(String gameEngineName, GAME_TYPE gameType, String documentName,
            GAME_STATUS... statuses)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void publishGameToWds(String gameId, Map<String, Object> extras)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String aesEncrypt(String key, String initVector, String plainTextMessage)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Integer> publishGamePlayerCountToWds(String gameId, List<Long> botIds)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RoundPlayer getRoundPlayer2(String roundId, long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ManualRedeemRequest getManualRedeemRequest(int manualRedeemRequestId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void markManualRedeemRequestFulfilled(int manualRedeemRequestId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<ManualRedeemRequest> getOutstandingManualRedeemRequests()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Game getGameViaInviteCode(String inviteCode)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String generateRandomString(int len)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getPrivateGameIdsForSubscriberByStatusAndEngine(long subscriberId, String gameEngine,
            GAME_STATUS... statuses)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Game getGameNoFat(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getSubscriberGameIdsByStatuses(int appId, long subscriberId, GAME_STATUS... statuses)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CashPoolTransaction2> getJoinedAndAbandonedForContext(String contextId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GamePlayer> getGamePlayersWithoutBots(String gameId, List<Long> botIds)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateGameThin(Game game)
    {
        // TODO Auto-generated method stub

    }



}
