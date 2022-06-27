package tv.shout.sm.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.gameplay.domain.Tuple;

import tv.shout.util.JsonUtil;

public abstract class BaseDbSupport
{
    static
    {
        //initialize the local logging to go to the console
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache", "debug");

        System.setProperty("log4j.defaultInitOverride", "true");
        Properties log4jProperties = new Properties();
        log4jProperties.setProperty("log4j.rootLogger", "INFO, stdout");
        log4jProperties.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        log4jProperties.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        log4jProperties.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d{MM/dd HH:mm:ss.SSS} %-5p [%c] %m%n");
        log4jProperties.setProperty("log4j.logger.org.apache.http.wire", "DEBUG");
        log4jProperties.setProperty("log4j.logger.org.apache.httpclient", "DEBUG");
        PropertyConfigurator.configure(log4jProperties);
    }

    protected DbProvider _db;

    public abstract void init(DbProvider.DB which) throws Exception;
    public abstract void run() throws Exception;

    public static String getConsoleInput(String message)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static void prettyPrintObjectAsJson(Object o)
    throws IOException
    {
        ObjectMapper mapper = JsonUtil.getObjectMapper();
        JsonNode node = mapper.convertValue(o, JsonNode.class);
        JsonGenerator jsonGenerator = mapper.getFactory().createGenerator(System.out);
        jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
        mapper.writeTree(jsonGenerator, node);
    }

    /**
     * Add a new row into the multi localization table.
     *
     * @param whichDb to support multiple databases, pick which to use: snowyowl, contest
     * @param uuid
     * @param type
     * @param map
     * @throws SQLException
     */
    protected void addMultiLocalizationValuesFromMap(String whichDb, String uuid, String type, Map<String, String> map)
    throws SQLException
    {
        String sql = "INSERT INTO "+whichDb+".multi_localization (" +
            "   uuid, `type`, language_code, `value`) VALUES (?,?,?,?)";

        PreparedStatement ps = null;
        Connection con = _db.getConnection();

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, uuid);
            ps.setString(2, type);

            for (String languageCode : map.keySet()) {
                String value = map.get(languageCode);
                ps.setString(3, languageCode);
                ps.setString(4, value);

                ps.execute();
            }

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

    protected List<Tuple<String>> getMultiLocalizationValues(String whichDb, String uuid, String type)
    throws SQLException
    {
        String sql = "SELECT language_code, `value` FROM " + whichDb + ".multi_localization WHERE `uuid` = ? AND `type` = ?";

        Connection con = _db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<Tuple<String>> result = new ArrayList<>();

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, uuid);
            ps.setString(2, type);

            rs = ps.executeQuery();
            while (rs.next()) {
                Tuple<String> t = new Tuple<>();
                t.setKey(rs.getString(1));
                t.setVal(rs.getString(2));
                result.add(t);
            }

        } finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (con != null) {
                con.close();
                con = null;
            }
        }

        return result;
    }

    public interface SqlMapper<T>
    {
        Collection<T> getCollectionObject();
        void populatePreparedStatement(PreparedStatement ps) throws SQLException;
        T mapRowToType(ResultSet rs) throws SQLException;
    }

    public static <T> Collection<T> executeSqlForList(DbProvider db, String sql, SqlMapper<T> mapper)
    throws SQLException
    {
        return executeSqlForList(db.getConnection(), sql, mapper);
    }

    public static <T> Collection<T> executeSqlForList(Connection con, String sql, SqlMapper<T> mapper)
    throws SQLException
    {
        return executeSqlForList(con, sql, mapper, true);
    }

    public static <T> Collection<T> executeSqlForList(Connection con, String sql, SqlMapper<T> mapper, boolean closeConnectionAfterUse)
    throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Collection<T> result = mapper.getCollectionObject();

        try {
            ps = con.prepareStatement(sql);
            mapper.populatePreparedStatement(ps);

            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapper.mapRowToType(rs));
            }

        } finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (closeConnectionAfterUse) {
                if (con != null) {
                    con.close();
                    con = null;
                }
            }
        }

        return result;
    }

    protected <T> Map<T, T> tupleListToMap(List<Tuple<T>> list)
    {
        if (list == null) return null;

        Map<T, T> map = new HashMap<>(list.size());
        list.forEach(tuple -> {
            map.put(tuple.getKey(), tuple.getVal());
        });

        return map;
    }

    //key = category key, val = uuid
    protected Map<String, String> getQuestionCategoriesReverseMap()
    throws SQLException
    {
        String sql = "SELECT id, category_key FROM shoutmillionaire.question_category_list";
        Statement s = null;
        ResultSet rs = null;
        Connection con = _db.getConnection();

        Map<String, String> resultMap = new HashMap<>();

        try {
            s = con.createStatement();
            rs = s.executeQuery(sql);

            while (rs.next()) {
                resultMap.put(rs.getString(2), rs.getString(1));
            }

            return resultMap;

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

    public static Boolean getNullableBoolean(ResultSet rs, String columnName)
    throws SQLException
    {
        boolean val = rs.getBoolean(columnName);
        return rs.wasNull() ? null : val;
    }

    public static Integer getNullableInt(ResultSet rs, String columnName)
    throws SQLException
    {
        int val = rs.getInt(columnName);
        return rs.wasNull() ? null : val;
    }

    public static Double getNullableDouble(ResultSet rs, String columnName)
    throws SQLException
    {
        double val = rs.getDouble(columnName);
        return rs.wasNull() ? null : val;
    }

    public static void setNullableInt(PreparedStatement ps, int paramIndex, Integer val)
    throws SQLException
    {
        if (val == null) {
            ps.setNull(paramIndex, Types.INTEGER);
        } else {
            ps.setInt(paramIndex, val);
        }
    }

    public static void setNullableDate(PreparedStatement ps, int paramIndex, Date date)
    throws SQLException
    {
        if (date == null) {
            ps.setNull(paramIndex, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(paramIndex, new Timestamp(date.getTime()));
        }
    }

    public BaseDbSupport(DbProvider.DB which) throws Exception
    {
        _db = new DbProvider(which);
        try {
            init(which);
            run();
        } finally {
            _db.close();
        }
        System.exit(0);
    }

}
