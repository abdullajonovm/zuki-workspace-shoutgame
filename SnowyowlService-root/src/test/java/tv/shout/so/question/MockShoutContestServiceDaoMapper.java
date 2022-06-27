package tv.shout.so.question;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.meinc.gameplay.domain.Tuple;

import tv.shout.sc.domain.Round;
import tv.shout.sm.db.BaseDbSupport;
import tv.shout.sm.db.BaseDbSupport.SqlMapper;
import tv.shout.sm.db.DbProvider;

public class MockShoutContestServiceDaoMapper
{
    private DbProvider _db;

    public MockShoutContestServiceDaoMapper(DbProvider.DB which)
    {
        _db = new DbProvider(which);
    }



//    Game getGame(String gameId)
//    {
//        String sql = "SELECT * FROM contest.game WHERE id = ?";
//
//        SqlMapper<Game> sqlMapper = new SqlMapper<Game>() {
//            @Override
//            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
//            {
//                ps.setString(1, gameId);
//            }
//
//            @Override
//            public Game mapRowToType(ResultSet rs) throws SQLException
//            {
//                Game game = new Game();
//
//                game.setId(rs.getString("id"));
//                game.setGameEngine(rs.getString("game_engine"));
//                game.setEngineType(Game.ENGINE_TYPE.valueOf(rs.getString("engine_type")));
//                game.setProducer(rs.getString("producer"));
//                game.setGamePhotoUrl(rs.getString("game_photo_url"));
//                game.setGameStatus(Game.GAME_STATUS.valueOf(rs.getString("game_status")));
//                game.setBracketEliminationCount(BaseDbSupport.getNullableInt(rs, "bracket_elimination_count"));
//                game.setAllowBots(rs.getBoolean("allow_bots"));
//                game.setPayoutCalculationMethod(Game.PAYOUT_CALCULATION_METHOD.valueOf("payout_calculation_method"));
//                game.setPayoutHouseTakePercentage(rs.getFloat("payout_house_take_percentage"));
//                game.setPayoutPercentageOfUsersToAward(rs.getFloat("payout_percentage_of_users_to_award"));
//                game.setIncludeActivityAnswersBeforeScoring(rs.getBoolean("include_activity_answers_before_scoring"));
//                game.setGuideUrl(rs.getString("guide_url"));
//                game.setPendingDate(rs.getTimestamp("pending_date"));
//                game.setCancelledDate(rs.getTimestamp("cancelled_date"));
//                game.setOpenDate(rs.getTimestamp("open_date"));
//                game.setInplayDate(rs.getTimestamp("inplay_date"));
//                game.setClosedDate(rs.getTimestamp("closed_date"));
//
//                return game;
//            }
//
//            @Override
//            public Collection<Game> getCollectionObject()
//            {
//                return new ArrayList<>();
//            }
//        };
//
//        try {
//            return ((List<Game>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper)).get(0);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    List<Tuple<String>> getMultiLocalizationValues(String uuid, String type)
    {
        String sql = "SELECT language_code, `value` FROM contest.multi_localization WHERE `uuid` = ? AND `type` = ?";

        SqlMapper<Tuple<String>> sqlMapper = new SqlMapper<Tuple<String>>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, uuid);
                ps.setString(2, type);
            }

            @Override
            public Tuple<String> mapRowToType(ResultSet rs) throws SQLException
            {
                Tuple<String> t = new Tuple<>();

                t.setKey(rs.getString(1));
                t.setVal(rs.getString(2));

                return t;
            }

            @Override
            public Collection<Tuple<String>> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            return (List<Tuple<String>>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

//    Set<Integer> getGameAllowableAppIds(String gameId)
//    {
//        String sql = "SELECT app_id FROM contest.game_app_ids WHERE game_id = ?";
//
//        SqlMapper<Integer> sqlMapper = new SqlMapper<Integer>() {
//            @Override
//            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
//            {
//                ps.setString(1, gameId);
//            }
//
//            @Override
//            public Integer mapRowToType(ResultSet rs) throws SQLException
//            {
//                return rs.getInt(1);
//            }
//
//            @Override
//            public Collection<Integer> getCollectionObject()
//            {
//                return new HashSet<>();
//            }
//        };
//
//        try {
//            return (Set<Integer>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    Set<String> getGameAllowableLanguageCodes(String gameId)
//    {
//        String sql = "SELECT language_code FROM contest.game_language_codes WHERE game_id = ?";
//
//        SqlMapper<String> sqlMapper = new SqlMapper<String>() {
//            @Override
//            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
//            {
//                ps.setString(1, gameId);
//            }
//
//            @Override
//            public String mapRowToType(ResultSet rs) throws SQLException
//            {
//                return rs.getString(1);
//            }
//
//            @Override
//            public Collection<String> getCollectionObject()
//            {
//                return new HashSet<>();
//            }
//        };
//
//        try {
//            return (Set<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    Set<String> getGameForbiddenCountryCodes(String gameId)
//    {
//        String sql = "SELECT country_code FROM contest.game_forbidden_country_codes WHERE game_id = ?";
//
//        SqlMapper<String> sqlMapper = new SqlMapper<String>() {
//            @Override
//            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
//            {
//                ps.setString(1, gameId);
//            }
//
//            @Override
//            public String mapRowToType(ResultSet rs) throws SQLException
//            {
//                return rs.getString(1);
//            }
//
//            @Override
//            public Collection<String> getCollectionObject()
//            {
//                return new HashSet<>();
//            }
//        };
//
//        try {
//            return (Set<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    List<Round> getRoundsForGame(String gameId)
    {
        String sql = "SELECT * FROM contest.round WHERE game_id = ? order by round_sequence";

        SqlMapper<Round> sqlMapper = new SqlMapper<Round>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, gameId);
            }

            @Override
            public Round mapRowToType(ResultSet rs) throws SQLException
            {
                Round r = new Round();

                r.setId(rs.getString("id"));
                r.setGameId(rs.getString("game_id"));
                r.setRoundType(Round.ROUND_TYPE.valueOf(rs.getString("round_type")));
                r.setRoundStatus(Round.ROUND_STATUS.valueOf(rs.getString("round_status")));
                r.setRoundSequence(rs.getInt("round_sequence"));
                r.setFinalRound(rs.getBoolean("final_round"));
                r.setRoundPurse(BaseDbSupport.getNullableDouble(rs, "round_purse"));
                r.setCurrentPlayerCount(rs.getInt("current_player_count"));
                r.setMaximumPlayerCount(rs.getInt("maximum_player_count"));
                r.setMinimumMatchCount(rs.getInt("minimum_match_count"));
                r.setMaximumMatchCount(BaseDbSupport.getNullableInt(rs, "minimum_match_count"));
                r.setCostPerPlayer(BaseDbSupport.getNullableDouble(rs, "cost_per_player"));
                r.setRoundActivityType(rs.getString("round_activity_type"));
                r.setRoundActivityValue(rs.getString("round_activity_value"));
                r.setMinimumActivityToWinCount(rs.getInt("minimum_activity_to_win_count"));
                r.setMaximumActivityCount(BaseDbSupport.getNullableInt(rs, "maximum_activity_count"));
                r.setActivityMinimumDifficulty(BaseDbSupport.getNullableInt(rs, "activity_minimum_difficulty"));
                r.setActivityMaximumDifficulty(BaseDbSupport.getNullableInt(rs, "activity_maximum_difficulty"));
                r.setActivityMaximumDurationSeconds(rs.getInt("activity_maximum_duration_seconds"));
                r.setPlayerMaximumDurationSeconds(rs.getInt("player_maximum_duration_seconds"));
                r.setDurationBetweenActivitiesSeconds(rs.getInt("duration_between_activities_seconds"));
                r.setMatchGlobal(rs.getBoolean("match_global"));
                r.setMaximumDurationMinutes(BaseDbSupport.getNullableInt(rs, "maximum_duration_minutes"));
                r.setMatchPlayerCount(rs.getInt("match_player_count"));
                r.setPendingDate(rs.getTimestamp("pending_date"));
                r.setCancelledDate(rs.getTimestamp("cancelled_date"));
                r.setVisibleDate(rs.getTimestamp("visible_date"));
                r.setExpectedOpenDate(rs.getTimestamp("expected_open_date"));
                r.setOpenDate(rs.getTimestamp("open_date"));
                r.setInplayDate(rs.getTimestamp("inplay_date"));
                r.setClosedDate(rs.getTimestamp("closed_date"));

                return r;
            }

            @Override
            public Collection<Round> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            return (List<Round>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Set<String> getRoundCategories(String roundId)
    {
        switch (roundId)
        {
            //pool 1
            case "b93286c1-c745-46b5-9494-65e6a269c756": {
                return new HashSet<>(Arrays.asList("3d7e0fab-3f86-41c9-803f-99aacb8d5ab6"));
            }

            //pool 2
            case "2b79258f-2d5a-4135-b49a-0d153d812885": {
                return new HashSet<>(Arrays.asList("f54ecca9-4026-4d14-b4e6-ea8e7c9b9fa1"));
            }

            //bracket
            case "054b7002-df80-419c-be19-f8dfea27037f": {
                return new HashSet<>(Arrays.asList("3d7e0fab-3f86-41c9-803f-99aacb8d5ab6","f54ecca9-4026-4d14-b4e6-ea8e7c9b9fa1"));
            }

            default: {
                String sql = "SELECT category FROM contest.round_categories WHERE round_id = ?";

                SqlMapper<String> sqlMapper = new SqlMapper<String>() {
                    @Override
                    public void populatePreparedStatement(PreparedStatement ps) throws SQLException
                    {
                        ps.setString(1, roundId);
                    }

                    @Override
                    public String mapRowToType(ResultSet rs) throws SQLException
                    {
                        return rs.getString(1);
                    }

                    @Override
                    public Collection<String> getCollectionObject()
                    {
                        return new HashSet<>();
                    }
                };

                try {
                    return (Set<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
