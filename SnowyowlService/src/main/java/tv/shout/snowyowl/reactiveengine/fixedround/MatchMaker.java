package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class MatchMaker
extends BaseHandler
implements SyncMessageSender
{
    private static Logger _logger = Logger.getLogger(MatchMaker.class);

    @Autowired
    protected BotEngine _botEngine;

    @Autowired
    protected SponsorEngine _sponsorEngine;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected IIdentityService _identityService;

    @Autowired
    private ISyncService _syncService;

    @Autowired
    private ITriggerService _triggerService;

    private Map<String, Map<Long, String>> _gameIdToPlayerIdToRoundPlayerId = new HashMap<>();
    private Map<String, List<RoundPlayer>> _roundIdToRoundPlayerMap = new HashMap<>();

    @Override
    //MessageHandler
    public void handleMessage(Message message)
    {
        //no-op
    }

    //call this for pool play
    public void addPlayerForMatching(String gameId, long subscriberId, String roundPlayerId)
    {
        Map<Long, String> playerIdToRoundPlayerId = _gameIdToPlayerIdToRoundPlayerId.get(gameId);
        if (playerIdToRoundPlayerId == null) {
            playerIdToRoundPlayerId = new HashMap<>();
            _gameIdToPlayerIdToRoundPlayerId.put(gameId, playerIdToRoundPlayerId);
        }
        playerIdToRoundPlayerId.put(subscriberId, roundPlayerId);
    }

    //call this for bracket play
    public void addPlayerForMatching(RoundPlayer roundPlayer)
    {
if (_logger.isDebugEnabled()) {
    _logger.debug("adding bracket play round player for next round: " + roundPlayer.getSubscriberId());
}
        Map<Long, String> playerIdToRoundPlayerId = _gameIdToPlayerIdToRoundPlayerId.get(roundPlayer.getGameId());
        if (playerIdToRoundPlayerId == null) {
            playerIdToRoundPlayerId = new HashMap<>();
            _gameIdToPlayerIdToRoundPlayerId.put(roundPlayer.getGameId(), playerIdToRoundPlayerId);
        }
        playerIdToRoundPlayerId.put(roundPlayer.getSubscriberId(), roundPlayer.getId());

        //also store the list of RoundPlayer objects
        List<RoundPlayer> list = _roundIdToRoundPlayerMap.get(roundPlayer.getRoundId());
        if (list == null) {
            list = new ArrayList<>();
            _roundIdToRoundPlayerMap.put(roundPlayer.getRoundId(), list);
        }
        list.add(roundPlayer);
    }

    public void createPoolMatch(Game game, Round round, long subscriberId, long opponentId)
    {
        createMatch(game, round, subscriberId, opponentId, null, null);
    }

    @SuppressWarnings("unchecked")
    public void createBracketMatches(Game game, Round round, boolean isFirstBracketRound)
    {
        Map<Long, String> playerIdToRoundPlayerId = _gameIdToPlayerIdToRoundPlayerId.get(game.getId());
        if (playerIdToRoundPlayerId == null) {
            return;
        }

        //grab the RoundPlayer objects
        List<RoundPlayer> roundPlayers = _roundIdToRoundPlayerMap.get(round.getId());
        if (roundPlayers == null || roundPlayers.size() == 0) {
            _logger.warn("no round players found waiting to pair for bracket round: " + round.getId());
            return;
        }

_logger.info("creating matches for bracket round: " + round.getId() + ", name: " + round.getRoundNames().get("en") + ", numPlayers: " + roundPlayers.size());

        //grab the bot and sponsor ids
        Object[] obj = (Object[]) wrapInTransaction((x) -> {
            List<Long> bIds = _botEngine.getBotsForGame(game.getId());
            List<Long> sIds = _sponsorEngine.getSponsorsForGame(game.getId());
            return new Object[] {bIds, sIds};
        }, null);
        List<Long> botIds = (List<Long>) obj[0];
        List<Long> sponsorIds = (List<Long>) obj[1];

        //for the purposes of sorting/pairing, any bots added need a temporary RoundPlayer created (with a skill of 0 so they sort to the bottom)
        if (isFirstBracketRound) {
            for (long botSubscriberId : botIds) {
                RoundPlayer botRp = _shoutContestService.getRoundPlayer2(round.getId(), botSubscriberId);
                if (botRp == null) {
_logger.info(">>> roundPlayer not found for bot. creating new roundPlayer...");
                    botRp = new RoundPlayer(game.getId(), round.getId(), botSubscriberId);
                    botRp.setSkill(0.0D);

                    //add to the database
_logger.info(MessageFormat.format(">>> MatchMaker::createBracketMatches, addRoundPlayer. gameId: {0}, roundId: {1}, subscriberId: {2}", botRp.getGameId(), botRp.getRoundId(), botRp.getSubscriberId()));
                    _shoutContestService.addRoundPlayer(botRp, round.getRoundType());
                }
//else _logger.info("using preexisting bot roundPlayer");

                //add to the local maps
//_logger.info("adding bot player to roundPlayer maps");
                roundPlayers.add(botRp);
                playerIdToRoundPlayerId.put(botSubscriberId, botRp.getId());
            }
        }

        if (roundPlayers.size() % 2 != 0) {
            throw new IllegalStateException("there are an odd number of players being paired for bracket play!");
        }

        //sort the roundplayers by skill, high to low
        Collections.sort(roundPlayers, new Comparator<RoundPlayer>() {
            @Override
            public int compare(RoundPlayer rp1, RoundPlayer rp2)
            {
                double r1 = rp1.getSkill() == null ? 0 : rp1.getSkill();
                double r2 = rp2.getSkill() == null ? 0 : rp2.getSkill();

                return r1 == r2 ? 0 : r1 < r2 ? 1 : -1;
            }
        });

        //do the bracket sorting: first paired with last, second paired with next to last, etc.
        int idx = 0;
        while (idx < roundPlayers.size()-2) {
            roundPlayers.add(idx+1, roundPlayers.remove(roundPlayers.size()-1));
            idx += 2;
        }

        //for each pair, create a match
        for (int i=0; i<roundPlayers.size(); i+=2) {
            createMatch(game, round, roundPlayers.get(i).getSubscriberId(), roundPlayers.get(i+1).getSubscriberId(), botIds, sponsorIds);
//            _logger.info(MessageFormat.format(
//                    ">>> THIS IS WHERE PAIRING WILL OCCUR FOR {0,number,#} ({1}) AND {2,number,#} ({3})",
//                    roundPlayers.get(i).getSubscriberId(), roundPlayers.get(i).getSkill(), roundPlayers.get(i+1).getSubscriberId(), roundPlayers.get(i+1).getSkill() ));
        }
    }

    @SuppressWarnings("unchecked")
    private void createMatch(Game game, Round round, long subscriberId, long opponentId, List<Long> botIds, List<Long> sponsorIds)
    {
if (_logger.isDebugEnabled()) _logger.debug("createMatch");
        Long twitchSubscriberId = getTwitchSubscriberId(game.getId());
//if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("\ttwitchSubscriberId: {0,number,#}", twitchSubscriberId));
        boolean doesMatchContainTwitchSubscriber = twitchSubscriberId != null && (twitchSubscriberId == subscriberId || twitchSubscriberId == opponentId);
//if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("\tdoesMatchContainTwitchSubscriber: {0}", doesMatchContainTwitchSubscriber));

        Match match = new Match(game.getId(), round.getId(), ISnowyowlService.GAME_ENGINE, ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife, round.getMinimumActivityToWinCount(), round.getMaximumActivityCount());
        List<MatchPlayer> players = new ArrayList<MatchPlayer>(2);

        //if the bot and sponsor ids weren't passed in (pool play), then retrieve them, otherwise (bracket play) use what was passed
        if (botIds == null || sponsorIds == null) {
            Object[] obj = (Object[]) wrapInTransaction((x) -> {
                List<Long> bIds = _botEngine.getBotsForGame(game.getId());
                List<Long> sIds = _sponsorEngine.getSponsorsForGame(game.getId());
                return new Object[] {bIds, sIds};
            }, null);
            botIds = (List<Long>) obj[0];
            sponsorIds = (List<Long>) obj[1];
        }

        boolean opponentIsBot = botIds.contains(opponentId);
        //subscriberId can also be a bot... but that doesn't matter. it'll just be null from the _gameIdToPlayerIdToRoundPlayerId structure, and that's ok

        //get the subscriber round player id
        String subscriberRoundPlayerId = _gameIdToPlayerIdToRoundPlayerId.get(game.getId()).get(subscriberId);
        _gameIdToPlayerIdToRoundPlayerId.get(game.getId()).remove(subscriberId);
if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("\tsubscriberRoundPlayerId: {0}", subscriberRoundPlayerId));
        if (subscriberRoundPlayerId == null) {
            throw new IllegalStateException(MessageFormat.format("subscriberRoundPlayerId is null for game: {0}, subscriberId: {1}", game.getId(), subscriberId));
        }

        Map<Long, Subscriber> subscribers = new HashMap<>();

        wrapInTransaction((x) -> {
            //create the match
            _shoutContestService.addMatch(match);

            //get the opponent round player id
            String opponentRoundPlayerId;
            if (opponentIsBot) {
                RoundPlayer rp = _shoutContestService.getRoundPlayer2(round.getId(), opponentId);
                opponentRoundPlayerId = rp == null ? null : rp.getId();

            } else {
                opponentRoundPlayerId = _gameIdToPlayerIdToRoundPlayerId.get(game.getId()).get(opponentId);
                _gameIdToPlayerIdToRoundPlayerId.get(game.getId()).remove(opponentId);
            }
//if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("\topponentRoundPlayerId: {0}", opponentRoundPlayerId));

            //create the match players
            players.add(_shoutContestService.addMatchPlayer(game.getId(), round.getId(), match.getId(), subscriberRoundPlayerId, subscriberId));
            players.add(_shoutContestService.addMatchPlayer(game.getId(), round.getId(), match.getId(), opponentRoundPlayerId, opponentId));

            //grab the relevant data about the total/remaining lives for each player
            List<Map<String, Object>> lives = new ArrayList<>();
            GamePlayer gp1 = _shoutContestService.getGamePlayer(game.getId(), subscriberId);
            GamePlayer gp2 = _shoutContestService.getGamePlayer(game.getId(), opponentId);
            lives.add(new FastMap<>("subscriberId", subscriberId, "totalLives", gp1.getTotalLives(), "remainingLives", gp1.getCountdownToElimination()));
            lives.add(new FastMap<>("subscriberId",   opponentId, "totalLives", gp2.getTotalLives(), "remainingLives", gp2.getCountdownToElimination()));

            //send a "user_matched" sync message for each player that was just matched
            Subscriber s = _identityService.getSubscriberById(subscriberId);
            subscribers.put(s.getSubscriberId(), s);
//if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("\tsending user_matched to: {0,number,#}", subscriberId));
            enqueueSyncMessage(
                    JsonUtil.getObjectMapper(), _syncService, _logger,
                    game.getId(), ISnowyowlService.SYNC_MESSAGE_USER_MATCHED,
                    new FastMap<>("players", players, "lives", lives), s, _socket, _triggerService);

            s = _identityService.getSubscriberById(opponentId);
            subscribers.put(s.getSubscriberId(), s);
            if (!opponentIsBot) {
//if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("\tsending user_matched to: {0,number,#}", opponentId));
                enqueueSyncMessage(
                        JsonUtil.getObjectMapper(), _syncService, _logger,
                        game.getId(), ISnowyowlService.SYNC_MESSAGE_USER_MATCHED,
                        new FastMap<>("players", players, "lives", lives), s, _socket, _triggerService);
            }
//else if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("\tnot sending user_matched to: {0,number,#}, player is a bot", opponentId));

            return null;
        }, null);

        //notify the twitch console via socket.io
        if (doesMatchContainTwitchSubscriber) {
//if (_logger.isDebugEnabled()) _logger.debug("\tsending twitch console update...");
            sendTwitchUpdate(game, round, match, players, twitchSubscriberId);
        }

        //add new delayed message: BEGIN_MATCH_AT
//if (_logger.isDebugEnabled()) _logger.debug("\tsending BEGIN_MATCH message...");
        Message msg = HandlerBeginMatch.createMessage(game, round, match, players, subscribers,botIds, sponsorIds, false);
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("MatchMaker sending message: " + msg.type);
        }
        _messageBus.sendMessage(msg);
    }

    protected void sendTwitchUpdate(Game game, Round round, Match match, List<MatchPlayer> players, Long twitchSubscriberId)
    {
        if (_socket != null) {
            long opId = 0;
            for (MatchPlayer mp : players) {
                if (mp.getSubscriberId() != twitchSubscriberId) {
                    opId = mp.getSubscriberId();
                    break;
                }
            }

            Map<String, Object> twitchMap = new FastMap<>(
                "type", "TWITCH_PAIRED",
                "gameid", game.getId(),
                "roundId", round.getId(),
                "matchId", match.getId(),
                "subscriberId", twitchSubscriberId,
                "opponentId", opId
            );

            try {
                _socket.emit("send_twitch_message", JsonUtil.getObjectMapper().writeValueAsString(twitchMap));
            } catch (JsonProcessingException e) {
                _logger.error("unable to emit send_twitch_message", e);
            }
        }
    }

}

