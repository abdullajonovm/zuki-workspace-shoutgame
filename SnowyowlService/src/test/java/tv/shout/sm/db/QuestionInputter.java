package tv.shout.sm.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import tv.shout.sm.db.DbProvider.DB;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;

public class QuestionInputter
extends BaseDbSupport
{
    private static Logger _logger = Logger.getLogger(QuestionInputter.class);

    public QuestionInputter(DB which) throws Exception
    {
        super(which);
    }

    @Override
    public void init(DB which) throws Exception
    {
    }

    public int getQuestionCount()
    throws SQLException
    {
        String sql = "SELECT COUNT(*) FROM shoutmillionaire.question";
        Statement s = null;
        ResultSet rs = null;
        Connection con = _db.getConnection();

        try {
            s = con.createStatement();
            rs = s.executeQuery(sql);
            rs.next();

            return rs.getInt(1);

        } finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (s != null) {
                s.close();
                s = null;
            }
            if (con != null) {
                con.close();
                con = null;
            }
        }
    }

    public Question getQuestion()
    throws IOException, SQLException
    {
        Map<String, String> questionCategoriesReverseMap = getQuestionCategoriesReverseMap();

        Question q = new Question();
        q.setId(UUID.randomUUID().toString());
        q.setDifficulty(0);
        //q.setSource(source);
        //q.setMediaUrl(mediaUrl);
        //q.setMediaType(mediaType);
        q.setCreateDate(new Date());
        //q.setExpirationDate(expirationDate);
        q.setUsageCount(0);

        Set<String> languageCodes = new HashSet<>();
        //Set<String> forbiddenCountryCodes = new HashSet<>();
        Map<String, String> questionTextMap = new HashMap<>();

        _logger.info("Enter the question text in the form languagecode::questiontext. Empty string moves on to answer entry.");
        String input = getConsoleInput("Question: ");
        while (input.indexOf("::") != -1) {
            String[] vals = input.split("::");
            String languageCode = vals[0];
            String questionText = vals[1];

            languageCodes.add(languageCode);
            questionTextMap.put(languageCode, questionText);

            input = getConsoleInput("Question: ");
        }
        q.setLanguageCodes(languageCodes);
        q.setQuestionText(questionTextMap);

        boolean atLeastOneCategoryFound = false;
        Set<String> questionCategoryUuids = new HashSet<>();
        _logger.info("Categories: " + questionCategoriesReverseMap.entrySet().stream()
            .map(m -> m.getKey())
            .collect(Collectors.joining(",")) );
        input = getConsoleInput("Categories (comma-delimited): ");
        String[] vals = input.split(",");
        for (String key : vals) {
            if (questionCategoriesReverseMap.containsKey(key)) {
                atLeastOneCategoryFound = true;
                questionCategoryUuids.add(questionCategoriesReverseMap.get(key));
            }
        }
        if (!atLeastOneCategoryFound) throw new IllegalStateException("At least one category must be specified");
        q.setQuestionCategoryUuids(questionCategoryUuids);

        List<QuestionAnswer> answers = new ArrayList<>();

        int numberOfAnswers = Integer.parseInt(getConsoleInput("# of answers: "));

        boolean correctSet = false;

        for (int i=0; i<numberOfAnswers; i++) {
            Map<String, String> answerTextMap = new HashMap<>();
            boolean correct = Boolean.parseBoolean(getConsoleInput("is answer # "+(i+1)+" correct (true/false)? "));
            correctSet |= correct;

            //add an entry for each language code
            q.getLanguageCodes().stream().forEach(languageCode -> {
                String answerText = getConsoleInput("Answer ("+languageCode+"): ");
                answerTextMap.put(languageCode, answerText);
            });

            QuestionAnswer answer = new QuestionAnswer(q.getId(), answerTextMap, correct);
            answers.add(answer);
        }

        q.setAnswers(answers);

        if (!correctSet) throw new IllegalStateException("no correct answer set");

        return q;
    }

    public void addQuestionToDb(Question q)
    throws SQLException
    {
        String sqlInsertQuestion =
            "INSERT INTO shoutmillionaire.question (id, difficulty, create_date)" +
            "VALUES (?, ?, NOW())";
        String sqlInsertLanguageCode =
            "INSERT INTO shoutmillionaire.question_language_codes (question_id, language_code) VALUES (?,?)";
        String sqlInsertCategory =
            "INSERT INTO shoutmillionaire.question_categories (question_id, category_id) VALUES (?,?)";
        String sqlInsertAnswer =
            "INSERT INTO shoutmillionaire.question_answer (" +
            "   id, question_id, correct, create_date) VALUES (?, ?, ?, NOW())";

        PreparedStatement ps = null;
        Connection con = _db.getConnection();

        try {
            ps = con.prepareStatement(sqlInsertQuestion);
            ps.setString(1, q.getId());
            ps.setInt(2, q.getDifficulty());
            ps.execute();
            ps.close();

            ps = con.prepareStatement(sqlInsertLanguageCode);
            ps.setString(1, q.getId());
            for (String languageCode : q.getLanguageCodes()) {
                ps.setString(2, languageCode);
                ps.execute();
            }
            ps.close();

            ps = con.prepareStatement(sqlInsertCategory);
            ps.setString(1, q.getId());
            for (String categoryUuid : q.getQuestionCategoryUuids()) {
                ps.setString(2, categoryUuid);
                ps.execute();
            }
            ps.close();

            addMultiLocalizationValuesFromMap("shoutmillionaire", q.getId(), "questionText", q.getQuestionText());

            ps = con.prepareStatement(sqlInsertAnswer);
            ps.setString(2, q.getId());
            for (QuestionAnswer answer : q.getAnswers()) {
                ps.setString(1, answer.getId());
                ps.setBoolean(3, answer.getCorrect());
                ps.execute();

                addMultiLocalizationValuesFromMap("shoutmillionaire", answer.getId(), "answerText", answer.getAnswerText());
            }
            ps.close();

        } finally {
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (con != null) {
                con.close();
                con = null;
            }
        }
    }

    @Override
    public void run() throws Exception
    {
//        System.out.println("# questions: " + getQuestionCount());

        Question question = getQuestion();
        addQuestionToDb(question);

        _logger.info("___DONE___");
    }

    public static void main(String[] args)
    throws Exception
    {
        new QuestionInputter(DbProvider.DB.LOCAL);
    }

}
