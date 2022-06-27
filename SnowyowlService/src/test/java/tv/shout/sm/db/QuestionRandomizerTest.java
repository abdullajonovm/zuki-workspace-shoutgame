package tv.shout.sm.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import tv.shout.sm.db.DbProvider.DB;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;

public class QuestionRandomizerTest
extends BaseDbSupport
{
    private static Logger _logger = Logger.getLogger(QuestionRandomizerTest.class);

    public QuestionRandomizerTest(DB which)
    throws Exception
    {
        super(which);
    }

    @Override
    public void init(DB which)
    throws Exception
    {
    }

    @Override
    public void run()
    throws Exception
    {
        Set<String> categories = new HashSet<>();
        categories.add("*");

        Set<String> languages = new HashSet<>();
        languages.add("en");

        int minDifficulty = 0;
        int maxDifficulty = 10;

        for (int j=0; j<3; j++) {
            _logger.info("");
            for (int i=0; i<20; i++) {
                Question q = getQuestion2(categories, languages, minDifficulty, maxDifficulty);
                _logger.info(q.getQuestionText().get("en"));
            }
        }
    }

//TODO: fix this. it doesn't appear to be very random
    private Question getQuestion(Set<String> roundCategoryUuids, Set<String> allowedLanguageCodes, int minDifficulty, int maxDifficulty)
    throws SQLException
    {
        String languageCodesAsCommaDelimitedList = allowedLanguageCodes.stream().collect(Collectors.joining(","));

        List<String> questionUuids;
        if (roundCategoryUuids.contains("*")) {
            questionUuids = getQuestionIdsBasedOnFiltersSansCategory(minDifficulty, maxDifficulty, languageCodesAsCommaDelimitedList);
        } else {
            String categoryUuidsAsCommaDelimiatedList = roundCategoryUuids.stream().collect(Collectors.joining(","));
            questionUuids = getQuestionIdsBasedOnFilters(minDifficulty, maxDifficulty, languageCodesAsCommaDelimitedList, categoryUuidsAsCommaDelimiatedList);
        }

        if (questionUuids.size() == 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("there are no questions that match the given criteria!");
            buf.append("\nminDifficulty: ").append(minDifficulty);
            buf.append("\nmaxDifficulty: ").append(maxDifficulty);
            buf.append("\nlanguageCodes: ").append(allowedLanguageCodes);
            buf.append("\ncategories: ").append(roundCategoryUuids);
            _logger.error(buf.toString());
            throw new IllegalStateException("there are no questions that match the given criteria!");
        }

        //randomize the list and pick the first one
        Collections.shuffle(questionUuids, new Random(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE));
        String questionUuid = questionUuids.get(0);

        //get the question object
//        Question q = QuestionHelper.getQuestion(questionUuid, _dao);
        Question q = fattenQuestion(questionUuid);

//        //increment the usage count
//        _dao.incrementQuestionUsageCount(questionUuid);

        return q;
    }

    private Map<Integer, List<String>> _questionQueryCriteriaHashToQuestionUuidList = new HashMap<>();
    private Map<Integer, Integer> _queryQuestionCriteriaHashToCurrentIndex = new HashMap<>();
    private Lock _questionListLock = new ReentrantLock();
    private Question getQuestion2(Set<String> roundCategoryUuids, Set<String> allowedLanguageCodes, int minDifficulty, int maxDifficulty)
    throws SQLException
    {
if (_questionListLock == null) {
_questionQueryCriteriaHashToQuestionUuidList = new HashMap<>();
_queryQuestionCriteriaHashToCurrentIndex = new HashMap<>();
_questionListLock = new ReentrantLock();
}
        //FUTURE: take usage count into account when selecting the question
        //FUTURE: scalability issue if there are tens of thousands or hundreds of thousands or more questions - holding their id's in memory for each hash combo

        String languageCodesAsCommaDelimitedList = allowedLanguageCodes.stream().collect(Collectors.joining(","));

        int hash;
        List<String> questionUuids;
        int curIndex;
        String categoryUuidsAsCommaDelimiatedList="";

        if (roundCategoryUuids.contains("*")) {
            hash = ("" + minDifficulty + maxDifficulty + languageCodesAsCommaDelimitedList + roundCategoryUuids).hashCode();
        } else {
            categoryUuidsAsCommaDelimiatedList = roundCategoryUuids.stream().collect(Collectors.joining(","));
            hash = ("" + minDifficulty + maxDifficulty + languageCodesAsCommaDelimitedList + categoryUuidsAsCommaDelimiatedList).hashCode();
        }

        _questionListLock.lock();
        try {
            questionUuids = _questionQueryCriteriaHashToQuestionUuidList.get(hash);
            if (questionUuids == null) {
                if (roundCategoryUuids.contains("*")) {
                    questionUuids = getQuestionIdsBasedOnFiltersSansCategory(minDifficulty, maxDifficulty, languageCodesAsCommaDelimitedList);
                } else {
                    questionUuids = getQuestionIdsBasedOnFilters(minDifficulty, maxDifficulty, languageCodesAsCommaDelimitedList, categoryUuidsAsCommaDelimiatedList);
                }
                Collections.shuffle(questionUuids, new Random(UUID.randomUUID().getMostSignificantBits())); //randomize the list

                curIndex = 0;
                _queryQuestionCriteriaHashToCurrentIndex.put(hash, curIndex);
                _questionQueryCriteriaHashToQuestionUuidList.put(hash, questionUuids);

            } else {
                curIndex = _queryQuestionCriteriaHashToCurrentIndex.get(hash);
            }

if (curIndex == 0) {
    _logger.info("--------------------------------------");
}
            if (questionUuids.size() == 0) {
                StringBuilder buf = new StringBuilder();
                buf.append("there are no questions that match the given criteria!");
                buf.append("\nminDifficulty: ").append(minDifficulty);
                buf.append("\nmaxDifficulty: ").append(maxDifficulty);
                buf.append("\nlanguageCodes: ").append(allowedLanguageCodes);
                buf.append("\ncategories: ").append(roundCategoryUuids);
                _logger.error(buf.toString());
                throw new IllegalStateException("there are no questions that match the given criteria!");
            }

            //grab the question and increment the current index
            String questionUuid = questionUuids.get(curIndex);
            curIndex++;
            _queryQuestionCriteriaHashToCurrentIndex.put(hash, curIndex);

            //get the question object
            //Question q = QuestionHelper.getQuestion(questionUuid, _dao);
            Question q = fattenQuestion(questionUuid);

//            //increment the usage count
//            _dao.incrementQuestionUsageCount(questionUuid);

            //if the index has wrapped around - re-randomize and start again
            if (curIndex == questionUuids.size()) {
                curIndex = 0;
                Collections.shuffle(questionUuids, new Random(UUID.randomUUID().getMostSignificantBits())); //randomize the list
                _questionQueryCriteriaHashToQuestionUuidList.put(hash, questionUuids);
                _queryQuestionCriteriaHashToCurrentIndex.put(hash, curIndex);
            }

            return q;

        } finally {
            _questionListLock.unlock();
        }
    }

    private Question fattenQuestion(String questionId)
    throws SQLException
    {
        Question q = getQuestionFromDb(questionId);
        if (q == null) return null;

        q.setLanguageCodes(getQuestionLanguageCodes(questionId));
        q.setForbiddenCountryCodes(getQuestionForbiddenCountryCodes(questionId));
        q.setQuestionCategoryUuids(getQuestionCategoryUuids(questionId));
        q.setQuestionText(tupleListToMap(getMultiLocalizationValues("shoutmillionaire", questionId, "questionText")));

        //flesh out the answers
        List<QuestionAnswer> answers = getQuestionAnswersForQuestion(questionId);
        for (QuestionAnswer answer : answers) {
            answer.setAnswerText(tupleListToMap(getMultiLocalizationValues("shoutmillionaire", answer.getId(), "answerText")));
        }
        q.setAnswers(answers);

        return q;
    }

    private List<String> getQuestionIdsBasedOnFiltersSansCategory(int minDifficulty, int maxDifficulty, String languageCodesAsCommaDelimitedList)
    throws SQLException
    {
        String sql =
            "SELECT DISTINCT(q.id) " +
            "  FROM shoutmillionaire.question q, " +
            "       shoutmillionaire.question_language_codes qlc " +
            " WHERE q.id = qlc.question_id " +
            "   AND q.difficulty >= ? AND q.difficulty <= ? " +
            "   AND (q.expiration_date IS NULL OR q.expiration_date > NOW()) " +
            "   AND q.`status` = 'PUBLISHED' " +
            "   AND FIND_IN_SET(qlc.language_code, ?) <> 0";

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

        return (List<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
    }

    private List<String> getQuestionIdsBasedOnFilters(int minDifficulty, int maxDifficulty, String languageCodesAsCommaDelimitedList, String categoryUuidsAsCommaDelimiatedList)
    throws SQLException
    {
        String sql =
            "SELECT DISTINCT(q.id) " +
            "  FROM shoutmillionaire.question q, " +
            "       shoutmillionaire.question_language_codes qlc, " +
            "       shoutmillionaire.question_categories qc, " +
            "       shoutmillionaire.question_category_list cat " +
            " WHERE q.id = qlc.question_id AND q.id = qc.question_id AND qc.category_id = cat.id " +
            "   AND q.difficulty >= ? AND q.difficulty <= ? " +
            "   AND (q.expiration_date IS NULL OR q.expiration_date > NOW()) " +
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

        return (List<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
    }

    private Question getQuestionFromDb(String questionId)
    throws SQLException
    {
        String sql = "SELECT * FROM shoutmillionaire.question WHERE id = ?";

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

        List<Question> questions = (List<Question>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
        return questions == null || questions.isEmpty() ? null : questions.get(0);
    }

    private Set<String> getQuestionLanguageCodes(String questionId)
    throws SQLException
    {
        String sql = "SELECT language_code FROM shoutmillionaire.question_language_codes WHERE question_id = ?";

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

        return (Set<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
    }

    private Set<String> getQuestionForbiddenCountryCodes(String questionId)
    throws SQLException
    {
        String sql = "SELECT country_code FROM shoutmillionaire.question_forbidden_country_codes WHERE question_id = ?";

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

        return (Set<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
    }

    private Set<String> getQuestionCategoryUuids(String questionId)
    throws SQLException
    {
        String sql = "SELECT category_id FROM shoutmillionaire.question_categories WHERE question_id = ?";

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

        return (Set<String>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
    }

    private List<QuestionAnswer> getQuestionAnswersForQuestion(String questionId)
    throws SQLException
    {
        String sql = "SELECT * FROM shoutmillionaire.question_answer WHERE question_id = ?";

        SqlMapper<QuestionAnswer> sqlMapper = new SqlMapper<QuestionAnswer>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setString(1, questionId);
            }

            @Override
            public QuestionAnswer mapRowToType(ResultSet rs) throws SQLException
            {
                QuestionAnswer a = new QuestionAnswer();
                a.setId(rs.getString("id"));
                a.setQuestionId(questionId);
                a.setMediaUrl(rs.getString("media_url"));
                a.setMediaType(rs.getString("media_type"));

                boolean readPossibleNullBooleanValue = rs.getBoolean("correct");
                a.setCorrect(rs.wasNull() ? null : readPossibleNullBooleanValue);

                int readPossibleNullIntValue = rs.getInt("survey_percent");
                a.setSurveyPercent(rs.wasNull() ? null : readPossibleNullIntValue);

                a.setCreateDate(rs.getTimestamp("create_date"));
                return a;
            }

            @Override
            public Collection<QuestionAnswer> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        return (List<QuestionAnswer>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapper);
    }

    public static void main(String[] args)
    throws Exception
    {
        new QuestionRandomizerTest(DbProvider.DB.LOCAL);
    }

}
