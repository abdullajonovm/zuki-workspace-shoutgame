package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

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

public class MockShoutContestService
implements IShoutContestService
{
    private static Logger _logger = Logger.getLogger(MockShoutContestService.class);

    private MockContestDao _dao;

    public MockShoutContestService(MockContestDao dao)
    {
        _dao = dao;
    }

    @Override
    public Match addMatch(Match match)
    {
        _dao.addMatch(match);
        return match;
    }

    @Override
    public Match getMatch(String matchId)
    {
        return _dao.getMatch(matchId);
    }

    @Override
    public RoundPlayer getRoundPlayer2(String roundId, long subscriberId)
    {
        return _dao.getRoundPlayer2(roundId, subscriberId);
    }

    @Override
    public MatchPlayer addMatchPlayer(String gameId, String roundId, String matchId, String roundPlayerId, long subscriberId)
    {
        MatchPlayer mp = new MatchPlayer(gameId, roundId, matchId, roundPlayerId, subscriberId);
        _dao.addMatchPlayer(mp);
        return mp;
    }

    @Override
    public Game getGame(String gameId)
    {
        return _dao.getGame(gameId);
    }

    @Override
    public boolean addGamePlayer(GamePlayer gamePlayer, String nextRoundId, ROUND_STATUS... roundStatuses)
    {
        if (nextRoundId == null) {

            //find the "first" round in the game (don't need the extra stuff; just seeing if it passes muster to use the id)
            List<Round> roundsForGame = _dao.getRoundsForGameForStatus(gamePlayer.getGameId(), roundStatuses);

            Optional<Round> oRound = roundsForGame.stream()
                .sorted( Comparator.comparing(Round::getRoundSequence, Comparator.nullsLast(Comparator.naturalOrder())) )
                .findFirst();
            if (!oRound.isPresent()) {
                _logger.warn(MessageFormat.format("game {0} has no rounds; unable to add player", gamePlayer.getGameId()));
                return false;
            }

            nextRoundId = oRound.get().getId();
        }

        gamePlayer.setNextRoundId(nextRoundId);
        _dao.addGamePlayer(gamePlayer);

        return true;
    }

    @Override
    public void addGame(Game game)
    {
        _dao.addGame(game);
    }

    @Override
    public void addRound(Round round)
    {
        _dao.addRound(round);
    }

    @Override
    public void openGame(String gameId)
    {
        _dao.setGameStatusOpen(gameId);
        _dao.setRoundStatusesVisibleForNewlyOpenedGame(gameId);
    }

    @Override
    public List<Round> getRoundsForGame(String gameId)
    {
        return _dao.getRoundsForGame(gameId);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void updateRoundStatus(String roundId, boolean finalRound, ROUND_STATUS newStatus)
    {
        switch (newStatus)
        {
            case CLOSED:
                _dao.setRoundStatusClosed(roundId, finalRound);
                break;

            case FULL:
                _dao.setRoundStatusFull(roundId, finalRound);
                break;

            case INPLAY:
                _dao.setRoundStatusInplay(roundId, finalRound);
                break;

            case OPEN:
                _dao.setRoundStatusOpen(roundId, finalRound);
                break;

            case VISIBLE:
                _dao.setRoundStatusVisible(roundId, finalRound);
                break;
        }
    }

    @Override
    public void updateRoundStatusAndPlayerCount(String roundId, ROUND_STATUS newStatus, int newPlayerCount)
    {
        _dao.updateRoundStatusAndPlayerCount(roundId, newStatus, newPlayerCount);
    }

    @Override
    public RoundPlayer getMostRecentRoundPlayer(String gameId, long subscriberId)
    {
        return _dao.getMostRecentRoundPlayer(gameId, subscriberId);
    }

    @Override
    public void addRoundPlayer(RoundPlayer roundPlayer, ROUND_TYPE roundType)
    {
        _dao.addRoundPlayer(roundPlayer);
    }

    @Override
    public GamePlayer getGamePlayer(String gameId, long subscriberId)
    {
        return _dao.getGamePlayer(gameId, subscriberId);
    }

    @Override
    public Round getRound(String roundId)
    {
        return _dao.getRound(roundId);
    }

    @Override
    public void updateMatch(Match match)
    {
        _dao.updateMatch(match);
    }

    @Override
    public String aesEncrypt(String key, String initVector, String plainTextMessage)
    {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(plainTextMessage.getBytes());

            return new String(Base64.getEncoder().encode(encrypted), "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public List<MatchPlayer> getMatchPlayersForMatch(String matchId)
    {
        return _dao.getMatchPlayersForMatch(matchId);
    }

    @Override
    public List<RoundPlayer> getMostRecentRoundPlayerForEachPlayerForGame(String gameId)
    {
        //returns all round players, order by create date (asc)
        List<RoundPlayer> allRoundPlayers = _dao.getAllRoundPlayersForGame(gameId);

        //by putting them into a map, each time a round player is overridden, it's a newer one, and thus we're left with the newest ones
        Map<Long, RoundPlayer> map = new HashMap<>();
        for (RoundPlayer rp : allRoundPlayers) {
            map.put(rp.getSubscriberId(), rp);
        }

        //map down to a list
        List<RoundPlayer> result = new ArrayList<>();
        for (Entry<Long, RoundPlayer> x : map.entrySet()) {
            result.add(x.getValue());
        }

        return result;
    }

    @Override
    public void updateRoundPlayerDetermination(String roundPlayerId, ROUND_PLAYER_DETERMINATION determination)
    {
        _dao.updateRoundPlayerDetermination(roundPlayerId, determination);
    }

    @Override
    public void updateGamePlayer(GamePlayer gamePlayer)
    {
        _dao.updateGamePlayer(gamePlayer);
    }

    @Override
    public void updateMatchPlayer(MatchPlayer matchPlayer)
    {
        _dao.updateMatchPlayer(matchPlayer);
    }

    @Override
    public Round getRoundForGameAndSequence(String gameId, int sequence)
    {
        String roundId = _dao.getRoundIdForGameAndSequence(gameId, sequence);
        return _dao.getRound(roundId);
    }

    @Override
    public void updateRoundPlayer(RoundPlayer roundPlayer)
    {
        _dao.updateRoundPlayer(roundPlayer);
    }

    @Override
    public List<Match> getMatchesByRoundAndStatus(String roundId, MATCH_STATUS... statuses)
    {
        return _dao.getMatchesByRoundAndStatus(roundId, Arrays.asList(statuses));
    }

    @Override
    public RoundPlayer getRoundPlayer(String roundPlayerId)
    {
        return _dao.getRoundPlayer(roundPlayerId);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void updateGameStatus(String gameId, GAME_STATUS newStatus)
    {
        switch (newStatus)
        {
            case CANCELLED:
                _dao.cancelGame(gameId);
                break;

            case CLOSED:
                _dao.setGameStatusClosed(gameId);
                break;

            case INPLAY:
                _dao.setGameStatusInplay(gameId);
                break;

            case OPEN:
                _dao.setGameStatusOpen(gameId);
                break;
        }
    }

    @Override
    public void removeGamePlayer(long subscriberId, String gameId)
    {
        _dao.removeGamePlayer(subscriberId, gameId);
    }

    @Override
    public List<GamePlayer> getGamePlayersForGame(String gameId)
    {
        return _dao.getGamePlayersForGame(gameId);
    }

    @Override
    public Map<String, Integer> publishGamePlayerCountToWds(String gameId, List<Long> botIds)
    {
        List<GamePlayer> gamePlayers = _dao.getCurrentGamePlayerCount(gameId, botIds);
        int freePlayerCount = (int) gamePlayers.stream()
            .filter(gp -> gp.isFreeplay())
            .count();
        int payedPlayerCount = (int) gamePlayers.stream()
            .filter(gp -> !gp.isFreeplay())
            .count();

        Map<String, Integer> map = new HashMap<>();
        map.put("payedPlayerCount", payedPlayerCount);
        map.put("freePlayerCount", freePlayerCount);

        return map;
    }

    @Override
    public List<Long> getSubscriberIdsForRound(String roundId)
    {
        return _dao.getSubscriberIdsForRound(roundId);
    }

    @Override
    public List<String> publishWebDataStoreGameListDoc(String gameEngineName, GAME_TYPE gameType, String documentName, GAME_STATUS... statuses)
    {
        //get the list of games matching the status and filter by gameType
        //business rule: do NOT publish private games
        List<Game> filteredGames = getGamesByStatusAndEngine(gameEngineName, statuses).stream()
                .filter(g -> g.getGameType() == gameType)
                .filter(g -> !g.isPrivateGame())
                .collect(Collectors.toList());

        _logger.debug(MessageFormat.format("MOCK PUBLISH: republishing all games for type: {0} with the following statuses: {1}", gameType, statuses));

        return filteredGames.stream().map(g -> g.getId()).collect(Collectors.toList());
    }

    @Override
    public List<Game> getGamesByStatusAndEngine(String gameEngine, GAME_STATUS... statuses)
    {
        List<String> dbGameIds = _dao.getGameIdsByEngineAndStatus(gameEngine, statuses);

        //grab all the game data
        List<Game> games = new ArrayList<>(dbGameIds.size());
        dbGameIds.forEach(gameId -> {
            games.add(_dao.getGame(gameId));
        });

        return games;
    }

    @Override
    public List<RoundPlayer> getMostRecentRoundPlayersForGame(String gameId, List<ROUND_TYPE> roundTypes)
    {
        return _dao.getMostRecentRoundPlayersForGame(gameId, roundTypes);
    }

    @Override
    public List<RoundPlayer> getRoundPlayersForGame(String gameId, long subscriberId)
    {
        return _dao.getRoundPlayersForGame(gameId, subscriberId);
    }

    @Override
    public List<Long> getSubscriberIdsThatWereNeverMatchedForGame(String gameId)
    {
        //no-op
        return new ArrayList<>();
    }

    @Override
    public void removeSubscribersThatWereNeverMatchedForGame(String gameId)
    {
        //no-op
    }

    @Override
    public void addCashPoolTransaction(long subscriberId, double amount, TYPE type, String desc, Integer receiptId, String contextUuid)
    {
        //no-op
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
    public List<CashPoolTransaction2> getJoinedAndAbandonedForContext(String contextId)
    {
        // TODO Auto-generated method stub
        return null;
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
    public void updateGame(Game game)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateGameThin(Game game)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setGameSmsSent(String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Game getGameNoFat(String gameId)
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
    public List<Game> getSubscriberGamesByStatus(GAME_STATUS status, int appId, long subscriberId)
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
    public void cloneGame(String gameId, String inviteCode, List<Date> expectedStartDatesForEachRoundBeingCloned,
            Map<String, String> gameNames, List<String> roundIdsToClone)
    {
        // TODO Auto-generated method stub

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
    public List<GamePlayer> getGamePlayers(long subscriberId)
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
    public void updateRound(Round round)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public RoundPlayer getRoundPlayerByDetermination(String gameId, String roundId, long subscriberId,
            ROUND_PLAYER_DETERMINATION determination)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMatchCountForRound(String roundId)
    {
        // TODO Auto-generated method stub
        return 0;
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
    public void publishGameToWds(String gameId, Map<String, Object> extras)
    {
        // TODO Auto-generated method stub

    }


}
