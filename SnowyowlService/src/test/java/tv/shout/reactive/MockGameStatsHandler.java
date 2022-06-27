package tv.shout.reactive;

import tv.shout.snowyowl.common.GameStatsHandler;
import tv.shout.snowyowl.domain.GameStats;

public class MockGameStatsHandler
extends GameStatsHandler
{
    private MockGameStatsDao _mockDao;

    public MockGameStatsHandler(MockGameStatsDao dao)
    {
        _mockDao = dao;
    }

    @Override
    public void setGameStats(GameStats gameStats)
    {
        _mockDao.setGameStats(gameStats);
    }

    @Override
    public GameStats getGameStats(String gameId)
    {
        return _mockDao.getGameStats(gameId);
    }
}
