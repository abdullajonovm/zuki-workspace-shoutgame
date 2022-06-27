package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Game.GAME_STATUS;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.Match.MATCH_STATUS;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_STATUS;
import tv.shout.sc.domain.Round.ROUND_TYPE;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.domain.RoundPlayer.ROUND_PLAYER_DETERMINATION;

public class MockContestDao
{
    private List<Game> _games = new ArrayList<>();
    private List<Round> _rounds = new ArrayList<>();
    private List<Match> _matches = new ArrayList<>();

    private List<GamePlayer> _gamePlayers = new ArrayList<>();
    private List<RoundPlayer> _roundPlayers = new ArrayList<>();
    private List<MatchPlayer> _matchPlayers = new ArrayList<>();

    void addMatch(Match m)
    {
        _matches.add(m);
    }

    Match getMatch(String matchId)
    {
        return _matches.stream()
                .filter(m -> m.getId().equals(matchId))
                .findFirst()
                .orElse(null);
    }

    RoundPlayer getRoundPlayer2(String roundId, long subscriberId)
    {
        return _roundPlayers.stream()
                .filter(rp -> rp.getRoundId().equals(roundId))
                .filter(rp -> rp.getSubscriberId() == subscriberId)
                .findFirst()
                .orElse(null);
    }

    void addMatchPlayer(MatchPlayer mp)
    {
        _matchPlayers.add(mp);
    }

    Game getGame(String gameId)
    {
        return _games.stream()
                .filter(g -> g.getId().equals(gameId))
                .findFirst()
                .orElse(null);
    }

    List<Round> getRoundsForGameForStatus(String gameId, ROUND_STATUS[] roundStatuses)
    {
        List<ROUND_STATUS> statuses = Arrays.asList(roundStatuses);

        List<Round> roundsForGame = _rounds.stream()
                .filter(r -> r.getGameId().equals(gameId))
                .collect(Collectors.toList());


        final List<Round> matchingRounds = new ArrayList<>();
        for (Round round : roundsForGame) {
            if (statuses.contains(round.getRoundStatus())) {
                matchingRounds.add(round);
            }
        }

        return matchingRounds;
    }

    void addGamePlayer(GamePlayer gamePlayer)
    {
        _gamePlayers.add(gamePlayer);
    }

    void addGame(Game game)
    {
        _games.add(game);
    }

    void addRound(Round round)
    {
        _rounds.add(round);
    }

    void setGameStatusOpen(String gameId)
    {
        Optional<Game> game = _games.stream()
                .filter(g -> g.getId().equals(gameId))
                .findFirst();

        if (game.isPresent()) {
            game.get().setGameStatus(Game.GAME_STATUS.OPEN);
            game.get().setOpenDate(new Date());
        }
    }

    void setRoundStatusesVisibleForNewlyOpenedGame(String gameId)
    {
        _rounds.stream()
                .filter(r -> r.getGameId().equals(gameId))
                .filter(r -> r.getRoundStatus() == Round.ROUND_STATUS.PENDING)
                .forEach(r -> {
                    r.setRoundStatus(Round.ROUND_STATUS.VISIBLE);
                });
    }

    List<Round> getRoundsForGame(String gameId)
    {
        return _rounds.stream()
            .filter(r -> r.getGameId().equals(gameId))
            .sorted(Comparator.comparing(Round::getRoundSequence))
            .collect(Collectors.toList());
    }

    void setRoundStatusVisible(String roundId, boolean finalRound)
    {
        Optional<Round> round = _rounds.stream()
                .filter(r -> r.getId().equals(roundId))
                .findFirst();

        if (round.isPresent()) {
            round.get().setRoundStatus(Round.ROUND_STATUS.VISIBLE);
            round.get().setFinalRound(finalRound);
            round.get().setVisibleDate(new Date());
        }
    }

    void setRoundStatusOpen(String roundId, boolean finalRound)
    {
        Optional<Round> round = _rounds.stream()
                .filter(r -> r.getId().equals(roundId))
                .findFirst();

        if (round.isPresent()) {
            round.get().setRoundStatus(Round.ROUND_STATUS.OPEN);
            round.get().setFinalRound(finalRound);
            round.get().setOpenDate(new Date());
        }
    }

    void setRoundStatusFull(String roundId, boolean finalRound)
    {
        Optional<Round> round = _rounds.stream()
                .filter(r -> r.getId().equals(roundId))
                .findFirst();

        if (round.isPresent()) {
            round.get().setRoundStatus(Round.ROUND_STATUS.FULL);
            round.get().setFinalRound(finalRound);
        }
    }

    void setRoundStatusInplay(String roundId, boolean finalRound)
    {
        Optional<Round> round = _rounds.stream()
                .filter(r -> r.getId().equals(roundId))
                .findFirst();

        if (round.isPresent()) {
            round.get().setRoundStatus(Round.ROUND_STATUS.INPLAY);
            round.get().setFinalRound(finalRound);
            round.get().setInplayDate(new Date());
        }
    }

    void setRoundStatusClosed(String roundId, boolean finalRound)
    {
        Optional<Round> round = _rounds.stream()
                .filter(r -> r.getId().equals(roundId))
                .findFirst();

        if (round.isPresent()) {
            round.get().setRoundStatus(Round.ROUND_STATUS.CLOSED);
            round.get().setFinalRound(finalRound);
            round.get().setClosedDate(new Date());
        }
    }

    void updateRoundStatusAndPlayerCount(String roundId, ROUND_STATUS newStatus, int newPlayerCount)
    {
        Optional<Round> round = _rounds.stream()
                .filter(r -> r.getId().equals(roundId))
                .findFirst();

        if (round.isPresent()) {
            round.get().setRoundStatus(newStatus);
            round.get().setCurrentPlayerCount(newPlayerCount);
        }
    }

    RoundPlayer getMostRecentRoundPlayer(String gameId, long subscriberId)
    {
        return
            _roundPlayers.stream()
                .filter(rp -> rp.getSubscriberId() == subscriberId)
                .filter(rp -> rp.getGameId().equals(gameId))
                .filter(rp -> rp.getDetermination() != RoundPlayer.ROUND_PLAYER_DETERMINATION.ABANDONED)
                .sorted(Comparator.comparing(RoundPlayer::getCreateDate).reversed())
                .findFirst()
                .orElse(null);
    }

    void addRoundPlayer(RoundPlayer roundPlayer)
    {
        _roundPlayers.add(roundPlayer);
    }

    GamePlayer getGamePlayer(String gameId, long subscriberId)
    {
        return _gamePlayers.stream()
                .filter(gp -> gp.getGameId().equals(gameId))
                .filter(gp -> gp.getSubscriberId() == subscriberId)
                .filter(gp -> gp.getDetermination() != GamePlayer.GAME_PLAYER_DETERMINATION.REMOVED)
                .findFirst()
                .orElse(null);
    }

    Round getRound(String roundId)
    {
        return _rounds.stream()
                .filter(r -> r.getId().equals(roundId))
                .findFirst()
                .orElse(null);
    }

    void updateMatch(Match match)
    {
        Optional<Match> oMatch = _matches.stream()
                .filter(m -> m.getId().equals(match.getId()))
                .findFirst();

        if (oMatch.isPresent()) {
            _matches.remove(oMatch.get());
        }

        _matches.add(match);
    }

    List<MatchPlayer> getMatchPlayersForMatch(String matchId)
    {
        return _matchPlayers.stream()
                .filter(mp -> mp.getMatchId().equals(matchId))
                .collect(Collectors.toList());
    }

    List<RoundPlayer> getAllRoundPlayersForGame(String gameId)
    {
        return _roundPlayers.stream()
                .filter(rp -> rp.getGameId().equals(gameId))
                .filter(rp -> rp.getDetermination() == ROUND_PLAYER_DETERMINATION.WON ||
                              rp.getDetermination() == ROUND_PLAYER_DETERMINATION.LOST ||
                              rp.getDetermination() == ROUND_PLAYER_DETERMINATION.TIMEDOUT ||
                              rp.getDetermination() == ROUND_PLAYER_DETERMINATION.SAVED)
                .sorted(Comparator.comparing(RoundPlayer::getCreateDate))
                .collect(Collectors.toList());
    }

    void updateRoundPlayerDetermination(String roundPlayerId, RoundPlayer.ROUND_PLAYER_DETERMINATION determination)
    {
        Optional<RoundPlayer> oRp = _roundPlayers.stream()
                .filter(rp -> rp.getId().equals(roundPlayerId))
                .findFirst();

        if (oRp.isPresent()) {
            oRp.get().setDetermination(determination);
        }
    }

    void updateGamePlayer(GamePlayer gamePlayer)
    {
        Optional<GamePlayer> oGp = _gamePlayers.stream()
                .filter(gp -> gp.getId().equals(gamePlayer.getId()))
                .findFirst();

        if (oGp.isPresent()) {
            oGp.get().setFreeplay(gamePlayer.isFreeplay());
            oGp.get().setRank(gamePlayer.getRank());
            oGp.get().setPayoutPaymentId(gamePlayer.getPayoutPaymentId());
            oGp.get().setPayoutAwardedAmount(gamePlayer.getPayoutAwardedAmount());
            oGp.get().setPayoutVenue(gamePlayer.getPayoutVenue());
            oGp.get().setPayoutCompleted(gamePlayer.isPayoutCompleted());
            oGp.get().setDetermination(gamePlayer.getDetermination());
            oGp.get().setCountdownToElimination(gamePlayer.getCountdownToElimination());
            oGp.get().setNextRoundId(gamePlayer.getNextRoundId());
            oGp.get().setLastRoundId(gamePlayer.getLastRoundId());
        }
    }

    void updateMatchPlayer(MatchPlayer matchPlayer)
    {
        Optional<MatchPlayer> oMp = _matchPlayers.stream()
                .filter(mp -> mp.getId().equals(matchPlayer.getId()))
                .findFirst();

        if (oMp.isPresent()) {
            oMp.get().setDetermination(matchPlayer.getDetermination());
            oMp.get().setScore(matchPlayer.getScore());
        }
    }

    String getRoundIdForGameAndSequence(String gameId, int sequence)
    {
        return _rounds.stream()
                .filter(r -> r.getGameId().equals(gameId))
                .filter(r -> r.getRoundSequence() == sequence)
                .map(r -> r.getId())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format("no round found for gameId: {0}, sequence: {1}", gameId, sequence)));
    }

    void updateRoundPlayer(RoundPlayer roundPlayer)
    {
        Optional<RoundPlayer> oRp = _roundPlayers.stream()
                .filter(rp -> rp.getId().equals(roundPlayer.getId()))
                .findFirst();

        if (oRp.isPresent()) {
            oRp.get().setPlayedMatchCount(roundPlayer.getPlayedMatchCount());
            oRp.get().setDetermination(roundPlayer.getDetermination());
            oRp.get().setReceiptId(roundPlayer.getReceiptId());
            oRp.get().setAmountPaid(roundPlayer.getAmountPaid());
            oRp.get().setRefunded(roundPlayer.isRefunded());
            oRp.get().setRank(roundPlayer.getRank());
            oRp.get().setSkillAnswerCorrectPct(roundPlayer.getSkillAnswerCorrectPct());
            oRp.get().setSkillAverageAnswerMs(roundPlayer.getSkillAverageAnswerMs());
            oRp.get().setSkill(roundPlayer.getSkill());
        }
    }

    List<Match> getMatchesByRoundAndStatus(String roundId, List<MATCH_STATUS> statuses)
    {
        List<Match> matches = new ArrayList<>();

        for (Match m : _matches) {
            if (m.getRoundId().equals(roundId) && statuses.contains(m.getMatchStatus())) {
                matches.add(m);
            }
        }

        return matches;
    }

    RoundPlayer getRoundPlayer(String roundPlayerId)
    {
        return _roundPlayers.stream()
                .filter(rp -> rp.getId().equals(roundPlayerId))
                .findFirst()
                .orElse(null);
    }

    void cancelGame(String gameId)
    {
        Optional<Game> game = _games.stream()
                .filter(g -> g.getId().equals(gameId))
                .findFirst();

        if (game.isPresent()) {
            game.get().setGameStatus(Game.GAME_STATUS.CANCELLED);
            game.get().setCancelledDate(new Date());
        }
    }

    void setGameStatusClosed(String gameId)
    {
        Optional<Game> game = _games.stream()
                .filter(g -> g.getId().equals(gameId))
                .findFirst();

        if (game.isPresent()) {
            game.get().setGameStatus(Game.GAME_STATUS.CLOSED);
            game.get().setClosedDate(new Date());
        }
    }

    void setGameStatusInplay(String gameId)
    {
        Optional<Game> game = _games.stream()
                .filter(g -> g.getId().equals(gameId))
                .findFirst();

        if (game.isPresent()) {
            game.get().setGameStatus(Game.GAME_STATUS.INPLAY);
            game.get().setInplayDate(new Date());
        }
    }

    void removeGamePlayer(long subscriberId, String gameId)
    {
        _gamePlayers.remove(_gamePlayers.stream()
                .filter(gp -> gp.getGameId().equals(gameId))
                .filter(gp -> gp.getSubscriberId() == subscriberId)
                .findFirst().get());
    }

    List<GamePlayer> getGamePlayersForGame(String gameId)
    {
        return _gamePlayers.stream()
                .filter(gp -> gp.getGameId().equals(gameId))
                .filter(gp -> gp.getDetermination() != GamePlayer.GAME_PLAYER_DETERMINATION.REMOVED && gp.getDetermination() != GamePlayer.GAME_PLAYER_DETERMINATION.CANCELLED)
                .collect(Collectors.toList());
    }

    List<GamePlayer> getCurrentGamePlayerCount(String gameId, List<Long> botIds)
    {
        List<GamePlayer> filteredGamePlayers = new ArrayList<>();
        for (GamePlayer gp : _gamePlayers) {
            if (
                    gp.getGameId().equals(gameId) &&
                    (gp.getDetermination() != GamePlayer.GAME_PLAYER_DETERMINATION.CANCELLED && gp.getDetermination() != GamePlayer.GAME_PLAYER_DETERMINATION.REMOVED) &&
                    !botIds.contains(gp.getSubscriberId())
               ) {
                filteredGamePlayers.add(gp);
            }
        }

        return filteredGamePlayers;
    }

    List<Long> getSubscriberIdsForRound(String roundId)
    {
        //get the relevant subscriberIds
        List<Long> subscriberIds = _roundPlayers.stream()
                .filter(rp -> rp.getRoundId().equals(roundId))
                .filter(rp -> rp.getDetermination() != RoundPlayer.ROUND_PLAYER_DETERMINATION.ABANDONED && rp.getDetermination() != RoundPlayer.ROUND_PLAYER_DETERMINATION.CANCELLED)
                .map(rp -> rp.getSubscriberId())
                .collect(Collectors.toList());

        //there may be duplicates. remove them
        Set<Long> set = new LinkedHashSet<>();
        set.addAll(subscriberIds);
        subscriberIds.clear();
        subscriberIds.addAll(set);

        return subscriberIds;
    }

    List<String> getGameIdsByEngineAndStatus(String gameEngine, GAME_STATUS[] statuses)
    {
        List<GAME_STATUS> s = Arrays.asList(statuses);

        return _games.stream()
                .filter(g -> g.getGameEngine().equals(gameEngine))
                .filter(g -> s.contains(g.getGameStatus()))
                .map(g -> g.getId())
                .collect(Collectors.toList());
    }

    List<RoundPlayer> getMostRecentRoundPlayersForGame(String gameId, List<ROUND_TYPE> roundTypes)
    {
        //get a list of all the roundIds for the game that match one of the given roundTypes (for this game)
        List<String> roundIds = _rounds.stream()
                .filter(r -> r.getGameId().equals(gameId))
                .filter(r -> roundTypes.contains(r.getRoundType()))
                .map(r -> r.getId())
                .collect(Collectors.toList());

        //get a list of subscriber ids for anyone that played one of the given rounds (and that didn't abandon the round)
        List<Long> subscriberIds = _roundPlayers.stream()
                .filter(rp -> rp.getGameId().equals(gameId))
                .filter(rp -> roundIds.contains(rp.getRoundId()))
                .filter(rp -> rp.getDetermination() != RoundPlayer.ROUND_PLAYER_DETERMINATION.ABANDONED)
                .map(rp -> rp.getSubscriberId())
                .collect(Collectors.toList());

        //remove duplicates from the list of subscriber ids
        Set<Long> set = new LinkedHashSet<>();
        set.addAll(subscriberIds);
        subscriberIds.clear();
        subscriberIds.addAll(set);

        //for each subscriber in the list, get their most recent RoundPlayer object for this game
        List<RoundPlayer> rpList = new ArrayList<>(subscriberIds.size());
        for (long subscriberId : subscriberIds) {
            rpList.add(
                _roundPlayers.stream()
                    .filter(rp -> rp.getGameId().equals(gameId))
                    .filter(rp -> rp.getSubscriberId() == subscriberId)
                    .sorted(Comparator.comparing(RoundPlayer::getCreateDate).reversed())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("no most recent roundPlayer found"))
            );
        }

        return rpList;
    }

    List<RoundPlayer> getRoundPlayersForGame(String gameId, long subscriberId)
    {
        return _roundPlayers.stream()
                .filter(rp -> rp.getGameId().equals(gameId))
                .filter(rp -> rp.getSubscriberId() == subscriberId)
                .sorted(Comparator.comparing(RoundPlayer::getCreateDate))
                .collect(Collectors.toList());
    }
}