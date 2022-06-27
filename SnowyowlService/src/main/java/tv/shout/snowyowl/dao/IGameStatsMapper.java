package tv.shout.snowyowl.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import tv.shout.snowyowl.domain.GameStats;

public interface IGameStatsMapper
{
    //
    // game stats
    //

    @Insert(
        "<script>" +
        "   INSERT INTO snowyowl.game_stats (game_id, remaining_players, freeplay_notification_sent, remaining_save_player_count, twitch_console_followed_subscriber_id) " +
        "   VALUES (#{gameId}, #{remainingPlayers}, #{freeplayNotificationSent}, #{remainingSavePlayerCount}, #{twitchConsoleFollowedSubscriberId}) " +
        "   <trim prefix=\"ON DUPLICATE KEY UPDATE\" prefixOverrides=\", \"> " +
        "       <if test=\"remainingPlayers != null\">remaining_players=#{remainingPlayers}</if> " +
        "       <if test=\"freeplayNotificationSent != null\">, freeplay_notification_sent=#{freeplayNotificationSent}</if> " +
        "       <if test=\"remainingSavePlayerCount != null\">, remaining_save_player_count=#{remainingSavePlayerCount}</if> " +
        "       <if test=\"twitchConsoleFollowedSubscriberId != null\">, twitch_console_followed_subscriber_id=#{twitchConsoleFollowedSubscriberId}</if> " +
        "   </trim>" +
        "</script>"
    )
    void setGameStats(GameStats gameStats);

    @Select("SELECT * FROM snowyowl.game_stats WHERE game_id = #{0}")
    GameStats getGameStats(String gameId);


}
