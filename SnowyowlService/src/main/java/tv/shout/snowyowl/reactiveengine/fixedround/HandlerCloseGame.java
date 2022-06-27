package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.snowyowl.engine.PayoutManagerException;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class HandlerCloseGame
extends BaseHandler
implements SyncMessageSender
{
    private static Logger _logger = Logger.getLogger(HandlerCloseGame.class);

    private static final String CLOSE_GAME = "CLOSE_GAME";

    @Autowired
    protected EngineCoordinator _engineCoordinator;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected IIdentityService _identityService;

    @Autowired
    private ISyncService _syncService;

    @Autowired
    private ITriggerService _triggerService;

    public static Message getCloseGameMessage(
            Game game, Map<Long, RoundPlayer> subscriberIdToRoundPlayerMap, Map<Long, GamePlayer> subscriberIdToGamePlayerMap,
            List<Long> botIds, List<Long> sponsorIds)
    {
        Map<String, Object> payload = new FastMap<>(
            "game", game,
            "subscriberIdToRoundPlayerMap", subscriberIdToRoundPlayerMap,
            "subscriberIdToGamePlayerMap", subscriberIdToGamePlayerMap,
            "botIds", botIds,
            "sponsorIds", sponsorIds
        );

        return new Message(CLOSE_GAME, payload);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case CLOSE_GAME:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "game", ((Game)payload.get("game")).getId()
                    );

                    _busLogger.debug(MessageFormat.format("HandlerCloseGame received CLOSE_GAME\n{0}", JsonUtil.print(map)));
                }
                closeGame(message);
                break;

            //else ignore
        }
    }

    @SuppressWarnings("unchecked")
    protected void closeGame(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Game game = (Game) payload.get("game");
        Map<Long, RoundPlayer> subscriberIdToRoundPlayerMap = (Map<Long, RoundPlayer>) payload.get("subscriberIdToRoundPlayerMap");
        Map<Long, GamePlayer> subscriberIdToGamePlayerMap = (Map<Long, GamePlayer>) payload.get("subscriberIdToGamePlayerMap");
        List<Long> botIds = (List<Long>) payload.get("botIds");
        List<Long> sponsorIds = (List<Long>) payload.get("sponsorIds");

if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("setting game status to CLOSED for game: {0}, id: {1}", game.getGameNames().get("en"), game.getId()));
}
        wrapInTransaction((x) -> {
            //close the game
            game.setGameStatus(Game.GAME_STATUS.CLOSED);
            _shoutContestService.updateGameStatus(game.getId(), Game.GAME_STATUS.CLOSED);

            //write out the final game stats
            _gameStatsHandler.saveGameStats(game.getId());

            //update all the GamePlayer objects with final rankings
            subscriberIdToRoundPlayerMap.values().stream().forEach(rp -> {
                GamePlayer gamePlayer = subscriberIdToGamePlayerMap.get(rp.getSubscriberId());
                gamePlayer.setRank(rp.getRank());
                _shoutContestService.updateGamePlayer(gamePlayer);
            });

            List<RoundPlayer> rpList = _shoutContestService.getMostRecentRoundPlayersForGame(game.getId(), Arrays.asList(Round.ROUND_TYPE.BRACKET));
if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("all bracket subscribers being considered for end of game: {0}",
            rpList.stream().map(rp -> rp.getSubscriberId()).collect(Collectors.toList())
        ));
}

            //sanity checking (these are all things i've seen occur before that shouldn't be possible)
            boolean passedSanityCheck = true;
            Set<Long> rpListAlreadyExaminedSubscriberIds = new HashSet<>();
            for (RoundPlayer rp : rpList) {
                //sanity check 1: for every entry in the rpList, there MUST be a matching GamePlayer in the subscriberIdToGamePlayerMap
                //(except for bot players)
                if (subscriberIdToGamePlayerMap.get(rp.getSubscriberId()) == null && !botIds.contains(rp.getSubscriberId())) {
                    //it's perfectly legit for there to be no GamePlayer record in the map at this point. The map is populated up top
                    // from all the players in the final round, but if a player didn't make it to the final round, they won't have an
                    // object and thus must be manually added here.

                    //FUTURE: performance enhancement: rather than retrieve these one by one, do a multi select once the rpList is retrieved

                    GamePlayer gamePlayer =  _shoutContestService.getGamePlayer(game.getId(), rp.getSubscriberId());
                    if (gamePlayer != null) {
                        subscriberIdToGamePlayerMap.put(gamePlayer.getSubscriberId(), gamePlayer);
                    } else {
                        passedSanityCheck = false;
                        _logger.error(MessageFormat.format(
                            "RME: sanity check FAIL: NO GAMEPLAYER FOUND: sId: {0,number,#}, gameId: {1}, roundId: {2}",
                            rp.getSubscriberId(), rp.getGameId(), rp.getRoundId()));
                    }
                }

                //sanity check 2: there MAY NOT be any duplicate subscriberId's in the rpList (i.e. you can't play twice in the same game)
                if (rpListAlreadyExaminedSubscriberIds.contains(rp.getSubscriberId())) {
                    passedSanityCheck = false;
                    _logger.error(MessageFormat.format(
                        "RME: sanity check FAIL: DUPLICATE ROUNDPLAYER FOUND: sId: {0,number,#}, gameId: {1}",
                        rp.getSubscriberId(), rp.getGameId()));
                } else {
                    rpListAlreadyExaminedSubscriberIds.add(rp.getSubscriberId());
                }

                //sanity check 3: EVERY entry in the rpList MUST have a determination
                if (rp.getDetermination() == null || rp.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.UNKNOWN) {
                    passedSanityCheck = false;
                    _logger.error(MessageFormat.format(
                        "RME: sanity check FAIL: ROUNDPLAYER HAS NO DETERMINATION: sId: {0,number,#}, gameId: {1}, roundPlayerId: {2}",
                        rp.getSubscriberId(), rp.getGameId(), rp.getId()));
                }
            }

            if (passedSanityCheck) {
//_logger.info(">>> sanity checks passed. this is where payouts will be assigned...");
                //assign payouts based on rank
                try {
                    _engineCoordinator.assignPayouts(
                        game,
                        _shoutContestService.getRoundsForGame(game.getId()),
                        subscriberIdToGamePlayerMap,
                        rpList,
                        new HashSet<>(botIds),
                        new HashSet<>(sponsorIds),
                        new HashSet<>(_identityService.getSubscriberIdsForRole(Subscriber.ROLE.TESTER)),
                        _dao.getGamePayout(game.getId())
                    );
                } catch (PayoutManagerException e) {
                    //this shouldn't happen - any errors should have been caught before this
                    _logger.error(MessageFormat.format("RME: UNABLE TO ASSIGN PAYOUTS: {0}\n\t{1}", e.getErrorTypeCode(), e.getErrorDetails()), e);
                }

            } else {
                _logger.error("RME: NOT ASSIGNING PAYOUTS. SANITY CHECK FAILED!!!");
            }

            //any GamePlayer objects that didn't get awarded (via the payout engine) need their determination set (otherwise it will stay as INPLAY even though the game is complete)
            subscriberIdToGamePlayerMap.forEach( (subscriberId,gamePlayer) -> {
                if (gamePlayer.getDetermination() == GamePlayer.GAME_PLAYER_DETERMINATION.INPLAY) {
                    gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.AWARDED);
                    gamePlayer.setPayoutAwardedAmount(0D);
                    _shoutContestService.updateGamePlayer(gamePlayer);
                }
            });

            //send out the "game_result" sync message to everyone
            subscriberIdToGamePlayerMap.forEach( (subscriberId, gamePlayer) -> {
                if (!botIds.contains(subscriberId) && !sponsorIds.contains(subscriberId)) {
                    Subscriber s = _identityService.getSubscriberById(subscriberId);
                    List<RoundPlayer> roundPlayers = _shoutContestService.getRoundPlayersForGame(gamePlayer.getGameId(), subscriberId);
                    enqueueSyncMessage(
                            JsonUtil.getObjectMapper(), _syncService, _logger,
                            gamePlayer.getGameId(), ISnowyowlService.SYNC_MESSAGE_GAME_RESULT,
                            new FastMap<> ("roundPlayers", roundPlayers, "gamePlayer", gamePlayer),
                            s, _socket, _triggerService);
                }
            });

            //clear out the bots/subscriber question list
            _dao.releaseBotsForGame(game.getId());
            _dao.clearSubscriberQuestions(game.getId());

            //if there are any non-dequeued entries in the match_queue table for this game, they should be cleared out
            List<Long> subscriberIdsThatWereNeverMatchedForGame = _shoutContestService.getSubscriberIdsThatWereNeverMatchedForGame(game.getId());
            if (subscriberIdsThatWereNeverMatchedForGame.size() > 0) {
                _shoutContestService.removeSubscribersThatWereNeverMatchedForGame(game.getId());
            }

            return null;
        }, null);

        //republish all the (potentially) changed games
        Message msg1 = HandlerDocPublisher.getRepublishOpenInplayGames();
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("HandlerSendQuestion sending message: " + msg1.type);
        }
        _messageBus.sendMessage(msg1);

        Message msg2 = HandlerDocPublisher.getGameWithExtrasMessage(game.getId(), null, null, null);
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("HandlerSendQuestion sending message: " + msg2.type);
        }
        _messageBus.sendMessage(msg2); //this game is now closed so won't be in the open/inplay list
    }

}
