package tv.shout.reactive;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import tv.shout.snowyowl.domain.GameStats;

public class MockGameStatsDao
{
    private List<GameStats> _gameStats = new ArrayList<>();

    public void setGameStats(GameStats gameStats)
    {
        Optional<GameStats> oStats = _gameStats.stream()
                .filter(gs -> gs.getGameId().equals(gameStats.getGameId()))
                .findFirst();

        if (oStats.isPresent()) {
            GameStats newGameStats = oStats.get();

            if (newGameStats.getRemainingPlayers() != null) {
                gameStats.setRemainingPlayers(newGameStats.getRemainingPlayers());
            }

            if (newGameStats.getFreeplayNotificationSent() != null) {
                gameStats.setFreeplayNotificationSent(newGameStats.getFreeplayNotificationSent());
            }

            if (newGameStats.getRemainingSavePlayerCount() != null) {
                gameStats.setRemainingSavePlayerCount(newGameStats.getRemainingSavePlayerCount());
            }

            if (newGameStats.getTwitchConsoleFollowedSubscriberId() != null) {
                gameStats.setTwitchConsoleFollowedSubscriberId(newGameStats.getTwitchConsoleFollowedSubscriberId());
            }

        } else {
            _gameStats.add(gameStats);
        }
    }

    public GameStats getGameStats(String gameId)
    {
        return _gameStats.stream()
                .filter(gs -> gs.getGameId().equals(gameId))
                .findFirst()
                .orElse(null);

    }
}
