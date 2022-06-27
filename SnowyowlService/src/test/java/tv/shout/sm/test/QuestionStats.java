package tv.shout.sm.test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import tv.shout.sm.db.BaseDbSupport;
import tv.shout.sm.db.BaseDbSupport.SqlMapper;
import tv.shout.sm.db.DbProvider;

public class QuestionStats
{
    private DbProvider _db;

    public QuestionStats(DbProvider.DB which)
    {
        _db = new DbProvider(which);
    }

    public void calculateQuestionStats() throws SQLException
    {
        String sql =
            "select ml.`value` " +
            "  from snowyowl.question q, snowyowl.multi_localization ml " +
            " where q.id = ml.uuid and ml.language_code = 'en' and ml.`type` = 'questionText'";

        Collection<String> list = getItemsFromSql(sql);
        outputMinMaxAverage("QUESTIONS", list);
    }

    public void calculateAnswerStats() throws SQLException
    {
        String sql =
            "select ml.`value` " +
            "  from snowyowl.multi_localization ml " +
            " where ml.language_code = 'en' and ml.`type` = 'answerText'";

        Collection<String> list = getItemsFromSql(sql);
        outputMinMaxAverage("ANSWERS", list);
    }

    private Collection<String> getItemsFromSql(String sql) throws SQLException
    {
        Collection<String> list = BaseDbSupport.executeSqlForList(_db, sql, new SqlMapper<String>() {
            @Override
            public Collection<String> getCollectionObject()
            {
                return new ArrayList<>();
            }

            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
            }

            @Override
            public String mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getString(1);
            }
        });

        return list;
    }

    private void outputMinMaxAverage(String tag, Collection<String> list)
    {
        //compute min/max/average
        int minLength = Integer.MAX_VALUE;
        int maxLength = Integer.MIN_VALUE;
        long totalLength = 0;
        long averageLength;

        for (String questionText : list) {
            int length = questionText.length();
            totalLength += length;

            if (length < minLength) {
                minLength = length;
            }

            if (length > maxLength) {
                maxLength = length;
            }
        }

        averageLength = totalLength / list.size();

        System.out.println(tag);
        System.out.println(MessageFormat.format("total items: {0}, min: {1}, max: {2}, average: {3}", list.size(), minLength, maxLength, averageLength));
    }

    public static void main(String[] args) throws SQLException
    {
        QuestionStats stats = new QuestionStats(DbProvider.DB.NC11_1);

        stats.calculateQuestionStats();
        stats.calculateAnswerStats();
    }
}
