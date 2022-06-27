package tv.shout.snowyowl.loadtest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.SubscriberSession;

import tv.shout.sc.domain.Round;
import tv.shout.sm.db.BaseDbSupport;
import tv.shout.sm.db.BaseDbSupport.SqlMapper;
import tv.shout.sm.db.DbProvider;
import tv.shout.snowyowl.domain.QuestionAnswer;

class BackdoorSqlGateway
{
//    private static Logger _logger = Logger.getLogger(BackdoorSqlGateway.class);

    private DbProvider _db;

    BackdoorSqlGateway(DbProvider.DB which)
    throws Exception
    {
        _db = new DbProvider(which);
    }

    void stop()
    {
        _db.close();
    }

    /**
     * Grab all possible questions with their answers and convert into lookup maps with correct and incorrect answers. Allows clients to quickly
     * choose a correct or incorrect answer for a question.
     *
     * @param correctAnswerMap will be populated with the correct answers. questionId -> answerId(correct)
     * @param incorrectAnswerMap will be populated with incorrect answers. questionId -> answerId(incorrect)
     */
    void prefetchQuestionAnswers(final Map<String, String> correctAnswerMap, final Map<String, String> incorrectAnswerMap)
    {
        if (correctAnswerMap == null) throw new IllegalArgumentException("correctAnswerMap is null");
        if (incorrectAnswerMap == null) throw new IllegalArgumentException("incorrectAnswerMap is null");

        try {
            //step 1: get all of the questionIds that could be asked
            String questionIdSql =
                "SELECT id " +
                "  FROM snowyowl.question " +
                " WHERE " +
                "       (`expiration_date` IS NULL or `expiration_date` > NOW()) " +
                "       AND `status` = 'PUBLISHED' ";

            SqlMapper<String> qIdMapper = new SqlMapper<String>() {
                @Override
                public void populatePreparedStatement(PreparedStatement ps) throws SQLException
                {
                    //no-op
                }

                @Override
                public String mapRowToType(ResultSet rs) throws SQLException
                {
                    return rs.getString(1);
                }

                @Override
                public Collection<String> getCollectionObject()
                {
                    return new ArrayList<>();
                }
            };

            List<String> questionIds = (List<String>) BaseDbSupport.executeSqlForList(_db, questionIdSql, qIdMapper);

            //step 2: for each question, grab the answer ids and then sort into the appropriate map
            questionIds.forEach(qId -> {
                getAnswersForQuestionAndSortIntoMaps(qId, correctAnswerMap, incorrectAnswerMap);
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getAnswersForQuestionAndSortIntoMaps(final String questionId, final Map<String, String> correctAnswerMap, final Map<String, String> incorrectAnswerMap)
    {
        String answerIdSql = "SELECT id, `correct` FROM snowyowl.question_answer WHERE question_id = ?";

        SqlMapper<QuestionAnswer> aIdMapper = new SqlMapper<QuestionAnswer>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, questionId);
            }

            @Override
            public QuestionAnswer mapRowToType(ResultSet rs) throws SQLException
            {
                QuestionAnswer a = new QuestionAnswer();
                a.setId(rs.getString(1));
                boolean correct = rs.getBoolean(2);
                a.setCorrect(rs.wasNull() ? false : correct);
                return a;
            }

            @Override
            public Collection<QuestionAnswer> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            List<QuestionAnswer> answers = (List<QuestionAnswer>) BaseDbSupport.executeSqlForList(_db, answerIdSql, aIdMapper);

            String correctAnswerId = answers.stream()
                .filter(answer -> answer.getCorrect() != null && answer.getCorrect() == true).findFirst()
                .orElseThrow(() -> new IllegalStateException("no correct answer for question: " + questionId))
                .getId();

            String anIncorrectAnswerId = answers.stream()
                    .filter(answer -> answer.getCorrect() == null || answer.getCorrect() == false).findAny()
                    .orElseThrow(() -> new IllegalStateException("no incorrect answer for question: " + questionId))
                    .getId();

            correctAnswerMap.put(questionId, correctAnswerId);
            incorrectAnswerMap.put(questionId, anIncorrectAnswerId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Grab the basic info needed for each round of a game.
     * @param gameId
     * @return
     */
    List<Round> getRoundsForGame(final String gameId)
    {
        String sql = "SELECT id, round_type, player_maximum_duration_seconds FROM contest.round WHERE game_id = ?";

        SqlMapper<Round> mapper = new SqlMapper<Round>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, gameId);
            }

            @Override
            public Round mapRowToType(ResultSet rs) throws SQLException
            {
                Round round = new Round();

                round.setId(rs.getString(1));
                round.setRoundType(Round.ROUND_TYPE.valueOf(rs.getString(2)));
                round.setPlayerMaximumDurationSeconds(rs.getInt(3));

                return round;
            }

            @Override
            public Collection<Round> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        List<Round> rounds;
        try {
            rounds = (List<Round>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rounds;
    }

    /**
     * Get all of the subscribers in the system that are marked as players
     *
     * @param maxCount return up to this number of subscribers
     * @return
     */
    List<Subscriber> getSubscriberPlayers(int maxCount)
    {
        String nicknamePrefix = "__player_%";

        String sql =
            "select s.subscriber_id, s.nickname, s.email, s.language_code, s.email_sha256_hash, s.encrypt_key " +
            "  from gameplay.s_subscriber s " +
            " where s.nickname like ? limit ?";

        SqlMapper<Subscriber> mapper = new SqlMapper<Subscriber>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, nicknamePrefix);
                ps.setInt(2, maxCount);
            }

            @Override
            public Subscriber mapRowToType(ResultSet rs) throws SQLException
            {
                Subscriber s = new Subscriber();

                s.setSubscriberId(rs.getInt(1));
                s.setNickname(rs.getString(2));
                s.setEmail(rs.getString(3));
                s.setLanguageCode(rs.getString(4));
                s.setEmailSha256Hash(rs.getString(5));
                s.setEncryptKey(rs.getString(6));

                return s;
            }

            @Override
            public Collection<Subscriber> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        //grab all matching subscribers
        List<Subscriber> subscribers;
        try {
            subscribers = (List<Subscriber>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //add in the seesion info for each
        subscribers.forEach(s -> addSessionToSubscriber(s));

        return subscribers;
    }

    private void addSessionToSubscriber(Subscriber s)
    {
        String sql = "SELECT ss.device_id, ss.session_key FROM gameplay.s_subscriber_session ss WHERE ss.subscriber_id = ? order by ss.last_authenticated_date desc limit 1";

        SqlMapper<SubscriberSession> mapper = new SqlMapper<SubscriberSession>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setLong(1, s.getSubscriberId());
            }

            @Override
            public SubscriberSession mapRowToType(ResultSet rs) throws SQLException
            {
                SubscriberSession ss = new SubscriberSession();
                ss.setDeviceId(rs.getString(1));
                ss.setSessionKey(rs.getString(2));
                return ss;
            }

            @Override
            public Collection<SubscriberSession> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            List<SubscriberSession> sessions = (List<SubscriberSession>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
            s.setSubscriberSession(sessions.get(0));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    String getDeviceIdForSubscriber(String email)
    {
        String sql =
            "select ss.device_id " +
            "  from gameplay.s_subscriber s, gameplay.s_subscriber_session ss " +
            " where s.subscriber_id = ss.subscriber_id " +
            "   and s.email = ? " +
            " order by ss.last_authenticated_date desc " +
            " limit 1";

        SqlMapper<String> mapper = new SqlMapper<String>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, email);
            }

            @Override
            public String mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getString(1);
            }

            @Override
            public Collection<String> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            List<String> deviceIds = (List<String>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
            return deviceIds.get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    double getCostToJoinGame(String gameId)
    {
        String sql = "select cost_per_player from contest.round where game_id = ? and round_sequence = 1";

        SqlMapper<Double> mapper = new SqlMapper<Double>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, gameId);
            }

            @Override
            public Double mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getDouble(1); //in this case, a null=0 is just what we want, so no null check isn't necessary
            }

            @Override
            public Collection<Double> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            List<Double> doubles = (List<Double>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
            return doubles.get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    double getSubscriberBalance(long subscriberId)
    {
        String sql = "select amount from contest.cash_pool where subscriber_id = ?";

        SqlMapper<Double> mapper = new SqlMapper<Double>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setLong(1, subscriberId);
            }

            @Override
            public Double mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getDouble(1); //in this case, a null=0 is just what we want, so no null check isn't necessary
            }

            @Override
            public Collection<Double> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            List<Double> doubles = (List<Double>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
            if (doubles == null || doubles.size() < 1) return 0D; //if they've never purchased, there won't be a row
            return doubles.get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    String getIdOfMostRecentOpenGame()
    {
        String sql = "select id from contest.game where game_status = 'OPEN' order by open_date desc limit 1";

        SqlMapper<String> mapper = new SqlMapper<String>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
            }

            @Override
            public String mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getString(1);
            }

            @Override
            public Collection<String> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            List<String> gameIds = (List<String>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
            return gameIds != null && gameIds.size() > 0 ? gameIds.get(0) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
