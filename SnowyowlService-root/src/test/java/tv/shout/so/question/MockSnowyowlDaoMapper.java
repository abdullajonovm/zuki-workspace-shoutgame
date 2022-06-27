package tv.shout.so.question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.meinc.gameplay.domain.Tuple;

import tv.shout.sm.db.BaseDbSupport;
import tv.shout.sm.db.BaseDbSupport.SqlMapper;
import tv.shout.sm.db.DbProvider;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.AffiliatePlan;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.GameWinner;
import tv.shout.snowyowl.domain.IneligibleSubscriber;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutTableRow;
import tv.shout.snowyowl.domain.ProhibitedSubscriber;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.Question.STATUS;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.domain.QuestionCategory;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayoutSubscriberInfo;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayoutSubscriberNetworkSize;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayoutSubscriberWinnings;
import tv.shout.snowyowl.domain.Sponsor;
import tv.shout.snowyowl.domain.SponsorCashPool;
import tv.shout.snowyowl.domain.SubscriberFromSearch;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberRoundQuestion;
import tv.shout.snowyowl.domain.SubscriberStats;

public class MockSnowyowlDaoMapper
implements IDaoMapper
{
    private DbProvider _db;

    public MockSnowyowlDaoMapper(DbProvider.DB which)
    {
        _db = new DbProvider(which);
    }

    @Override
    public List<String> getQuestionIdsBasedOnFiltersSansCategory(int minDifficulty, int maxDifficulty, String languageCodesAsCommaDelimitedList)
    {
        String sql =
            "SELECT DISTINCT(q.id) " +
            "  FROM snowyowl.question q, " +
            "       snowyowl.question_language_codes qlc " +
            " WHERE q.id = qlc.question_id " +
            "   AND q.difficulty >= ? AND q.difficulty <= ? " +
            "   AND (q.expiration_date IS NULL OR q.expiration_date > NOW()) " +
            "   AND q.`status` = 'PUBLISHED' " +
            "   AND FIND_IN_SET(qlc.language_code, ? <> 0";

        SqlMapper<String> sqlMapper = new SqlMapper<String>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setInt(1, minDifficulty);
                ps.setInt(2, maxDifficulty);
                ps.setString(3, languageCodesAsCommaDelimitedList);
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
            return (List<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getQuestionIdsBasedOnFilters(int minDifficulty, int maxDifficulty, String languageCodesAsCommaDelimitedList, String categoryUuidsAsCommaDelimiatedList)
    {
        switch (categoryUuidsAsCommaDelimiatedList)
        {
            //utah jazz
            case "3d7e0fab-3f86-41c9-803f-99aacb8d5ab6": {
                return Arrays.asList("0195050f-711f-4b0a-b47e-b377eb1f885f", "21f9090e-b42a-4cc7-ada1-89ce7e26525c", "7eed4b02-84ca-4114-a1db-e73eac1a2807", "ced88f8d-91da-4290-9910-8ac04580c6bf");
            }

            //zions bank
            case "f54ecca9-4026-4d14-b4e6-ea8e7c9b9fa1": {
                return Arrays.asList("0b33f4ea-794b-486b-82dc-221b4dc78740", "5131e23c-57fb-4c23-a3ac-2f29cc4466ab", "e95e933f-4baf-41c3-ab6a-4efe886d28ad", "f6a4900e-312d-4e11-aa76-c2e3b3c6c6af");
            }

            //utah jazz,zions bank
            case "3d7e0fab-3f86-41c9-803f-99aacb8d5ab6,f54ecca9-4026-4d14-b4e6-ea8e7c9b9fa1": {
                return Arrays.asList(
                    "0195050f-711f-4b0a-b47e-b377eb1f885f", "21f9090e-b42a-4cc7-ada1-89ce7e26525c", "7eed4b02-84ca-4114-a1db-e73eac1a2807", "ced88f8d-91da-4290-9910-8ac04580c6bf",
                    "0b33f4ea-794b-486b-82dc-221b4dc78740", "5131e23c-57fb-4c23-a3ac-2f29cc4466ab", "e95e933f-4baf-41c3-ab6a-4efe886d28ad", "f6a4900e-312d-4e11-aa76-c2e3b3c6c6af"
                );
            }

            default: {
                String sql =
                    "SELECT DISTINCT(q.id) " +
                    "  FROM snowyowl.question q, " +
                    "       snowyowl.question_language_codes qlc, " +
                    "       snowyowl.question_categories qc, " +
                    "       snowyowl.question_category_list cat " +
                    " WHERE q.id = qlc.question_id AND q.id = qc.question_id AND qc.category_id = cat.id " +
                    "   AND q.difficulty >= ? AND q.difficulty <= ? " +
                    "   AND (q.expiration_date IS NULL OR q.expiration_date > NOW()) " +
                    "   AND q.`status` = 'PUBLISHED' " +
                    "   AND FIND_IN_SET(qlc.language_code, ?) <> 0 " +
                    "   AND FIND_IN_SET(cat.id, ?) <> 0";

                SqlMapper<String> sqlMapper = new SqlMapper<String>() {
                    @Override
                    public void populatePreparedStatement(PreparedStatement ps) throws SQLException
                    {
                        ps.setInt(1, minDifficulty);
                        ps.setInt(2, maxDifficulty);
                        ps.setString(3, languageCodesAsCommaDelimitedList);
                        ps.setString(4, categoryUuidsAsCommaDelimiatedList);
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
                    return (List<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public List<String> getGameRoundQuestionIds(String gameId, String roundid)
    {
        String sql = "SELECT question_id FROM snowyowl.game_round_questions WHERE game_id = ? AND round_id = ? ORDER BY `order`";

        SqlMapper<String> sqlMapper = new SqlMapper<String>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, gameId);
                ps.setString(2, roundid);
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
            return (List<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addGameRoundQuestion(String gameId, String roundId, String questionId, int order)
    {
        String sql = "INSERT INTO snowyowl.game_round_questions (game_id, round_id, question_id, `order`) VALUES (?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = _db.getConnection();
            ps = con.prepareStatement(sql);

            ps.setString(1, gameId);
            ps.setString(2, roundId);
            ps.setString(3, questionId);
            ps.setInt(4, order);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                ps = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                con = null;
            }
        }
    }

    @Override
    public List<SubscriberRoundQuestion> getSubscriberRoundQuestions(long subscriberId, String roundId)
    {
        String sql = "SELECT subscriber_id, round_id, question_id, `order`, seen FROM snowyowl.subscriber_round_questions WHERE subscriber_id = ? AND round_id = ? ORDER BY `order`";

        SqlMapper<SubscriberRoundQuestion> sqlMapper = new SqlMapper<SubscriberRoundQuestion>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setLong(1, subscriberId);
                ps.setString(2, roundId);
            }

            @Override
            public SubscriberRoundQuestion mapRowToType(ResultSet rs) throws SQLException
            {
                SubscriberRoundQuestion srq = new SubscriberRoundQuestion();

                srq.setSubscriberId(rs.getInt("subscriber_id"));
                srq.setRoundId(rs.getString("round_id"));
                srq.setQuestionId(rs.getString("question_id"));
                srq.setOrder(rs.getInt("order"));
                srq.setSeen(rs.getBoolean("seen"));

                return srq;
            }

            @Override
            public Collection<SubscriberRoundQuestion> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            return (List<SubscriberRoundQuestion>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addSubscriberRoundQuestion(long subscriberId, String roundId, String questionId, int order)
    {
        String sql = "INSERT INTO snowyowl.subscriber_round_questions (subscriber_id, round_id, question_id, `order`, seen) VALUES (?, ?, ?, ?, 0)";

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = _db.getConnection();
            ps = con.prepareStatement(sql);

            ps.setLong(1, subscriberId);
            ps.setString(2, roundId);
            ps.setString(3, questionId);
            ps.setInt(4, order);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                ps = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                con = null;
            }
        }
    }

    @Override
    public void updateSubscriberRoundQuestion(SubscriberRoundQuestion srq)
    {
        String sql = "UPDATE snowyowl.subscriber_round_questions SET seen = ? WHERE subscriber_id = ? AND round_id = ? AND question_id = ?";

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = _db.getConnection();
            ps = con.prepareStatement(sql);

            ps.setBoolean(1, srq.isSeen());
            ps.setLong(2, srq.getSubscriberId());
            ps.setString(3, srq.getRoundId());
            ps.setString(4, srq.getQuestionId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                ps = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                con = null;
            }
        }
    }

    @Override
    public List<QuestionAnswer> getQuestionAnswersForQuestion(String questionId)
    {
        String sql = "SELECT * FROM snowyowl.question_answer WHERE question_id = ?";

        SqlMapper<QuestionAnswer> sqlMapper = new SqlMapper<QuestionAnswer>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, questionId);
            }

            @Override
            public QuestionAnswer mapRowToType(ResultSet rs) throws SQLException
            {
                QuestionAnswer qa = new QuestionAnswer();

                qa.setId(rs.getString("id"));
                qa.setQuestionId(rs.getString("question_id"));
                qa.setMediaUrl(rs.getString("media_url"));
                qa.setMediaType(rs.getString("media_type"));
                qa.setCorrect(BaseDbSupport.getNullableBoolean(rs, "correct"));
                qa.setSurveyPercent(BaseDbSupport.getNullableInt(rs, "survey_percent"));
                qa.setCreateDate(rs.getTimestamp("create_date"));

                return qa;
            }

            @Override
            public Collection<QuestionAnswer> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            return (List<QuestionAnswer>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void incrementQuestionUsageCount(String questionId)
    {
        //no-op for mock purposes
    }

    @Override
    public Question getQuestion(String questionId)
    {
        String sql = "SELECT * FROM snowyowl.question WHERE id = ?";

        SqlMapper<Question> sqlMapper = new SqlMapper<Question>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, questionId);
            }

            @Override
            public Question mapRowToType(ResultSet rs) throws SQLException
            {
                Question q = new Question();

                q.setId(rs.getString("id"));
                q.setDifficulty(rs.getInt("difficulty"));
                q.setSource(rs.getString("source"));
                q.setMediaUrl(rs.getString("media_url"));
                q.setMediaType(rs.getString("media_type"));
                q.setCreateDate(rs.getTimestamp("create_date"));
                q.setExpirationDate(rs.getTimestamp("expiration_date"));
                q.setUsageCount(rs.getInt("usage_count"));
                q.setStatus(Question.STATUS.valueOf(rs.getString("status")));

                return q;
            }

            @Override
            public Collection<Question> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            return ((List<Question>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper)).get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> getQuestionLanguageCodes(String questionId)
    {
        String sql = "SELECT language_code FROM snowyowl.question_language_codes WHERE question_id = ?";

        SqlMapper<String> sqlMapper = new SqlMapper<String>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, questionId);
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

    @Override
    public Set<String> getQuestionForbiddenCountryCodes(String questionId)
    {
        String sql = "SELECT country_code FROM snowyowl.question_forbidden_country_codes WHERE question_id = ?";

        SqlMapper<String> sqlMapper = new SqlMapper<String>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, questionId);
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

    @Override
    public Set<String> getQuestionCategoryUuids(String questionId)
    {
        String sql = "SELECT category_id FROM snowyowl.question_categories WHERE question_id = ?";

        SqlMapper<String> sqlMapper = new SqlMapper<String>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, questionId);
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

    @Override
    public List<Tuple<String>> getMultiLocalizationValues(String uuid, String type)
    {
        String sql = "SELECT language_code, `value` FROM snowyowl.multi_localization WHERE `uuid` = ? AND `type` = ?";

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
























    @Override
    public void insertOrReplaceMultiLocalizationValue(String uuid, String type, String languageCode, String value)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeMutliLocalizationValues(String uuid, String type)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Long> getIdleBotIds()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getBotCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void markBotIdle(long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Long> getBotsForGame(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void releaseBotsForGame(String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getBundleIdsForApp(int appId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearPhoneVerificationCodeForSubscriber(long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPhoneVerificationCodeForSubscriber(long subscriberId, String phone, String code)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isPhoneVerificationCodeValidForSubscriber(long subscriberId, String phone, String code,
            Date cutoffDate)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<SubscriberFromSearch> getSubscribersInSignupDateRange(Date from, Date to)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertQuestionCategory(QuestionCategory category)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Tuple<String>> getQuestionCategoryIdToKey()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<QuestionCategory> getAllQuestionCategories()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QuestionCategory getQuestionCategoryByKey(String key)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QuestionCategory getQuestionCategoryById(String id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getQuestionIdsForCategory(String categoryId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeQuestionCategoryNames(String categoryId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionCategory(String categoryId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public SubscriberQuestionAnswer getSubscriberQuestionAnswer(String subscriberQuestionAnswerId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAnswerOnSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setQuestionViewedTimestampOnSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<MatchQuestion> getMatchQuestionsForMatch(String matchId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SubscriberQuestionAnswer> getSubscriberQuestionAnswersViaMatchQuestion(String matchQuestionId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertMatchQuestion(MatchQuestion matchQuestion)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMatchQuestion(MatchQuestion matchQuestion)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void insertSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<SubscriberQuestionAnswer> getSubscriberQuestionAnswersForMatch(String matchId, long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createQuestion(Question question)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addQuestionLanguageCode(String questionId, String languageCode)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addQuestionForbiddenCountryCode(String questionId, String countryCode)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addQuestionCategory(String questionId, String categoryUuid)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addQuestionAnswer(QuestionAnswer answer)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getQuestionIdsByState(STATUS status)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateQuestionStatus(String questionId, STATUS newStatus)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionAnswers(String questionid)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionCategories(String questionId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionForbiddenCountryCodes(String questionId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionLanguageCodes(String questionId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestion(String questionId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<GameWinner> getGameWinners(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setGameStats(GameStats gameStats)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public GameStats getGameStats(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addPayoutTableRow(String gameId, String rowId, int rankFrom, int rankTo, float amount)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<PayoutTableRow> getPayoutTableRows(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addTieBreakerQuestion(String gameId, String matchId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isTieBreakerQuestion(String gameId, String matchId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getTieBreakerWinnerSubscriberId(String gameId, String matchId)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void addTieBreakerWinnerSubscriberId(String gameId, String matchId, long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSocketIoLog(Integer subscriberId, String messageType, String message, String status, Date sentDate)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePayoutTableRows(String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public GamePayout getGamePayout(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addGamePayout(GamePayout gamePayout)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public PayoutModel getPayoutModel(int payoutModelId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PayoutModelRound> getPayoutModelRounds(int payoutModelId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PayoutModel> getPayoutModelsByEntranceFee(float entranceFee)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PayoutModel> getAllPayoutModels()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertPayoutModel(PayoutModel payoutModel)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void insertPayoutModelRound(PayoutModelRound pmr)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isPayoutModelInUse(int payoutModelId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updatePayoutModel(PayoutModel pm)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePayoutModel(int payoutModelId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePayoutModelRounds(int payoutModelId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deactivatePayoutModel(int payoutModelId, long deactivatorId, String reason)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteIneligibleSubscriber(long isId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<IneligibleSubscriber> getIneligibleSubscribers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertIneligibleSubscriber(IneligibleSubscriber is)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<ProhibitedSubscriber> getProhibitedSubscribers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertProhibitedSubscriber(ProhibitedSubscriber ps)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteProhibitedSubscriber(long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSubscriberIneligible(long subscriberId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSubscriberProhibited(long subscriberId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addSubscriberActionLog(long subscriberId, String action, String reason, String note)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public SubscriberStats getSubscriberStats(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertSubscriberStats(SubscriberStats stats)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateSubscriberStats(SubscriberStats stats)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<SubscriberStats> getAllSubscriberStats()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addSubscriberQuestion(String gameId, long subscriberId, String questionId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearSubscriberQuestions(String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getCombinedSubscriberQuestions(String gameId, String subscriberIdsAsCommaDelimitedList)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addAffiliatePlan(AffiliatePlan plan)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearCurrentAffiliatePlan()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public AffiliatePlan getCurrentAffiliatePlan()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AffiliatePlan getAffiliatePlan(int affiliatePlanId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ReportStructAffiliatePayoutSubscriberWinnings> getReportStructAffiliatePayoutSubscriberWinnings(
            Date since)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ReportStructAffiliatePayoutSubscriberNetworkSize> getReportStructAffiliatePayoutSubscriberNetworkSize(
            String subscriberIdsAsCommaDelimitedList)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ReportStructAffiliatePayoutSubscriberInfo> getReportStructAffiliatePayoutSubscriberInfo(
            String subscriberIdsAsCommaDelimitedList)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertSponsorCashPool(SponsorCashPool pool)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateSponsorCashPool(SponsorCashPool pool)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSponsorCashPoolTransaction(int sponsorCashPoolId, double amount, String reason)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public SponsorCashPool getSponsorCashPoolById(int sponsorCashPoolId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getNumberOfAvailableSponsors()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Long> getAvailableSponsorIds()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addSponsorToGame(int sponsorCashPoolId, String gameId, long sponsorSubscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Sponsor> getSponsorsForGame(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void releaseSponsorPlayersForGame(String gameId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Long> getSponsorIdsGame(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Sponsor getSingleSponsorForGame(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Sponsor getSponsorById(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void releaseSponsorPlayerForGame(long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public SponsorCashPool getSponsorCashPoolByPoolOwnerSubscriberId(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SponsorCashPool getSponsorCashPoolBySponsorPlayerSubscriberId(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
