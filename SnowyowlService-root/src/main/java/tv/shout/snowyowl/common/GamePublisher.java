package tv.shout.snowyowl.common;

import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.util.FastMap;

public interface GamePublisher
{
    default void publishGameWithExtrasToWds(
            String gameId,
            IDaoMapper _dao, IShoutContestService _shoutContestService)
    {
        publishGameWithExtrasToWds(gameId, null, null, null, _dao, _shoutContestService);
    }

    default void publishGameWithExtrasToWds(
        String gameId, PayoutModel pm, GamePayout gp, GameStats gameStats,
        IDaoMapper _dao, IShoutContestService _shoutContestService)
    {
        if (pm == null) {
            pm = _dao.getPayoutModel(_dao.getGamePayout(gameId).getPayoutModelId());
        }

        if (gp == null) {
            gp = _dao.getGamePayout(gameId);
        }

        if (gameStats == null) {
            gameStats = _dao.getGameStats(gameId);
            if (gameStats == null) gameStats = new GameStats(gameId);
        }

        double topPayout = pm.getPayoutModelRounds().stream()
            .filter(pmr -> pmr.getSortOrder() == 0)
            .mapToDouble(pmr -> pmr.getEliminatedPayoutAmount())
            .findFirst().orElse(0.0D);

        _shoutContestService.publishGameToWds(gameId, new FastMap<>(
            //"payoutModel", JsonUtil.getObjectMapper().convertValue(pm, new TypeReference<Map<String, Object>>() {}),
            "minimumPayoutAmount", gp.getMinimumPayoutAmount(),
            "freeplayNotificationSent", gameStats.getFreeplayNotificationSent() == null ? false : gameStats.getFreeplayNotificationSent(),
            "costToJoin", pm.getEntranceFeeAmount(),
            "topPayout", (float) topPayout,
            "payoutModelId", pm.getPayoutModelId()
        ));
    }
}
