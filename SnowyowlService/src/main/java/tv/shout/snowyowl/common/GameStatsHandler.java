package tv.shout.snowyowl.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import tv.shout.snowyowl.dao.IGameStatsMapper;
import tv.shout.snowyowl.domain.GameStats;

public class GameStatsHandler
{
    private List<GameStats> _gameStats = new ArrayList<>(); //FUTURE: this could potentially grow quite large over a big enough period of time

    @Autowired
    private IGameStatsMapper _dao;

    //caller must wrap this in a transaction
    public GameStats getGameStats(String gameId)
    {
        Optional<GameStats> oStats = _gameStats.stream()
                .filter(gs -> gs.getGameId().equals(gameId))
                .findFirst();

        if (oStats.isPresent()) {
            return oStats.get();
        } else {
            GameStats stats = _dao.getGameStats(gameId);
            if (stats != null) {
                _gameStats.add(stats);
            }
            return stats;
        }
    }

    //caller must wrap this in a transaction
    public void setGameStats(GameStats gameStats)
    {
        Optional<GameStats> oExistingGameStats = _gameStats.stream()
                .filter(gs -> gs.getGameId().equals(gameStats.getGameId()))
                .findFirst();

        if (oExistingGameStats.isPresent()) {
            GameStats existingGameStats = oExistingGameStats.get();

            if (gameStats.getRemainingPlayers() != null) {
                existingGameStats.setRemainingPlayers(gameStats.getRemainingPlayers());
            }

            if (gameStats.getFreeplayNotificationSent() != null) {
                existingGameStats.setFreeplayNotificationSent(gameStats.getFreeplayNotificationSent());
            }

            if (gameStats.getRemainingSavePlayerCount() != null) {
                existingGameStats.setRemainingSavePlayerCount(gameStats.getRemainingSavePlayerCount());
            }

            if (gameStats.getTwitchConsoleFollowedSubscriberId() != null) {
                existingGameStats.setTwitchConsoleFollowedSubscriberId(gameStats.getTwitchConsoleFollowedSubscriberId());
            }

        } else {
            _gameStats.add(gameStats);
        }
    }

    //caller must wrap this in a transaction
    public void saveGameStats(String gameId)
    {
        Optional<GameStats> oStats = _gameStats.stream()
                .filter(gs -> gs.getGameId().equals(gameId))
                .findFirst();

        if (oStats.isPresent()) {
            _dao.setGameStats(oStats.get());
        }
    }

}
