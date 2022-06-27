package meinc.zztasks;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import tv.shout.sm.db.BaseDbSupport;

public class MoveLocalCsvToErgo
{
    private static final String SQL_DRIVER = "com.mysql.jdbc.Driver";
    private static final int POOL_SIZE = 16;

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    private ComboPooledDataSource _cpds;

    private MoveLocalCsvToErgo()
    {
        String dbUrl = "jdbc:mysql://fctasks-production.cik3cnlqzqyo.us-east-1.rds.amazonaws.com:3306/";
        String un = "root";
        String pw = "FLV2auo5CEsO";

        _cpds = new ComboPooledDataSource();
        try {
            _cpds.setDriverClass(SQL_DRIVER);
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
        _cpds.setJdbcUrl(dbUrl);
        _cpds.setUser(un);
        _cpds.setPassword(pw);
        _cpds.setMinPoolSize(POOL_SIZE);
        _cpds.setAcquireIncrement(POOL_SIZE);
        _cpds.setMaxPoolSize(POOL_SIZE);
    }

    /**
     * Insert each of the roles from the csv into the database.
     *
     * @param csvFileHandle containing the role data
     * @return a mapping of role_uuid to role_id
     */
    @SuppressWarnings("unused")
    private Map<String, Integer> insertRoles(Connection con, int subscriberId, String csvFileHandle, boolean validateCsvIntegrityOnly)
    {
        Map<String, Integer> map = new HashMap<>();
        List<String> tableRowData = getTableRowData(csvFileHandle, "roles");

        if (validateCsvIntegrityOnly) {
            for (String csvLine : tableRowData) {
                List<String> rowData = parseLine(csvLine);
                try {
                    String roleUuid = rowData.get(1);
                    String roleName = rowData.get(2);
                    Date createDate = getDateFromString(rowData.get(7));
                    Date lastUpdate = getDateFromString(rowData.get(8));

                    map.put(roleUuid, -1);

                } catch (IndexOutOfBoundsException e) {
                    System.out.println(csvLine);
                    throw e;
                }
            }

        } else {
            String sql = "INSERT INTO ergo.role (role_uuid, subscriber_id, `name`, create_date, update_date) VALUES (?, ?, ?, ?, ?)";
            String sqlGetLastInsertId = "SELECT LAST_INSERT_ID()";

            PreparedStatement ps = null;
            ResultSet rs = null;
            Statement s = null;

            int count = tableRowData.size();
            int idx = 0;

            try {
                s = con.createStatement();
                ps = con.prepareStatement(sql);
                ps.setInt(2, subscriberId);

                for (String csvLine : tableRowData) {
                    idx++;
                    System.out.println(MessageFormat.format("inserting role {0} of {1}", idx, count));

                    List<String> rowData = parseLine(csvLine);
                    String roleUuid = rowData.get(1);
                    String roleName = convertStringUsingNewlines(rowData.get(2));
                    Date createDate = getDateFromString(rowData.get(7));
                    Date lastUpdate = getDateFromString(rowData.get(8));

                    ps.setString(1, roleUuid);
                    ps.setString(3, roleName);
                    BaseDbSupport.setNullableDate(ps, 4, createDate);
                    BaseDbSupport.setNullableDate(ps, 5, lastUpdate);

                    ps.executeUpdate();

                    //grab the newly inserted id and store in the mapping
                    rs = s.executeQuery(sqlGetLastInsertId);
                    rs.next();
                    int newRoleId = rs.getInt(1);
                    rs.close();

                    map.put(roleUuid, newRoleId);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    rs = null;
                }
                if (s != null) {
                    try {
                        s.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    s = null;
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    ps = null;
                }
            }
        }

        return map;
    }

    /**
     * Insert each of the notes from the csv into the database.
     *
     * @param csvFileHandle containing the note data
     * @param roleUuidToRoleIdMap a mapping of role_uuid to role_id
     */
    @SuppressWarnings("unused")
    private void insertNotes(Connection con, int subscriberId, String csvFileHandle, Map<String, Integer> roleUuidToRoleIdMap, boolean validateCsvIntegrityOnly)
    {
        List<String> tableRowData = getTableRowData(csvFileHandle, "notes");

        if (validateCsvIntegrityOnly) {
            for (String csvLine : tableRowData) {
                List<String> rowData = parseLine(csvLine);
                try {
                    String noteUuid = rowData.get(1);
                    String noteText = rowData.get(2);
                    boolean privateFlag = Boolean.parseBoolean(rowData.get(3));
                    String roleUuid = rowData.get(4);

                    Integer roleId = roleUuidToRoleIdMap.get(roleUuid);
                    if (roleId == null && roleUuid != null) {
                        System.out.println(MessageFormat.format("found note that references non-existant role. noteUuid: {0}, roleUuid: {1}", noteUuid, roleUuid));
                    }

                    Date createDate = getDateFromString(rowData.get(7));
                    Date lastUpdate = getDateFromString(rowData.get(8));
                } catch (IndexOutOfBoundsException e) {
                    System.out.println(csvLine);
                    throw e;
                }
            }
        } else {
            String sql  = "INSERT INTO ergo.note (note_uuid, subscriber_id, `note`, private_flag, role_id, role_uuid, create_date, update_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = null;
            ResultSet rs = null;

            int count = tableRowData.size();
            int idx = 0;

            try {
                ps = con.prepareStatement(sql);
                ps.setInt(2, subscriberId);

                for (String csvLine : tableRowData) {
                    idx++;
                    System.out.println(MessageFormat.format("inserting note {0} of {1}", idx, count));

                    List<String> rowData = parseLine(csvLine);
                    String noteUuid = rowData.get(1);
                    String noteText = convertStringUsingNewlines(rowData.get(2));
                    boolean privateFlag = Boolean.parseBoolean(rowData.get(3));
                    String roleUuid = getNullableString(rowData.get(4));
                    Date createDate = getDateFromString(rowData.get(7));
                    Date lastUpdate = getDateFromString(rowData.get(8));

                    Integer roleId = roleUuidToRoleIdMap.get(roleUuid);
                    if (roleId == null && roleUuid != null) {
                        roleUuid = null;
                    }

                    ps.setString(1, noteUuid);
                    ps.setString(3, noteText);
                    ps.setBoolean(4, privateFlag);
                    BaseDbSupport.setNullableInt(ps, 5, roleId);
                    ps.setString(6, roleUuid);
                    BaseDbSupport.setNullableDate(ps, 7, createDate);
                    BaseDbSupport.setNullableDate(ps, 8, lastUpdate);

                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    rs = null;
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    ps = null;
                }
            }
        }
    }

    /**
     * Insert each of the tasks from the csv into the database.
     *
     * @param csvFileHandle containing the task data
     * @param roleUuidToRoleIdMap a mapping of role_uuid to role_id
     */
    @SuppressWarnings("unused")
    private void insertTasks(Connection con, int subscriberId, String csvFileHandle, Map<String, Integer> roleUuidToRoleIdMap, boolean validateCsvIntegrityOnly)
    {
        List<String> tableRowData = getTableRowData(csvFileHandle, "tasks");

        if (validateCsvIntegrityOnly) {
            for (String csvLine : tableRowData) {
                List<String> rowData = parseLine(csvLine);
                try {
                    String taskUuid = rowData.get(1);
                    String description = rowData.get(2);
                    Date dueDate = getDateFromString(rowData.get(3));
                    String priority = rowData.get(4);
                    String roleUuid = getNullableString(rowData.get(5));

                    int reminderMinsBefore=0;
                    try { reminderMinsBefore = Integer.parseInt(rowData.get(6)); } catch (NumberFormatException ignored) {}

                    boolean privateFlag = Boolean.parseBoolean(rowData.get(7));
                    String note = rowData.get(8);
                    String status = rowData.get(9);
                    Date completedDate = getDateFromString(rowData.get(11));
                    int order = Integer.parseInt(rowData.get(12));
                    int roleOrder = Integer.parseInt(rowData.get(13));
                    int priorityOrder = Integer.parseInt(rowData.get(14));
                    Date createDate = getDateFromString(rowData.get(16));
                    Date updateDate = getDateFromString(rowData.get(17));
                    Date recurringStartDate = getDateFromString(rowData.get(18));
                    boolean recurringRegenerativeFlag = getBooleanFromString(rowData.get(19), false);
                    String recurringRRule = rowData.get(20);

                    Integer roleId = roleUuidToRoleIdMap.get(roleUuid);
                    if (roleId == null && roleUuid != null) {
                        System.out.println(MessageFormat.format("found task that references non-existant role. taskUuid: {0}, roleUuid: {1}", taskUuid, roleUuid));
                    }

                    /*System.out.println(MessageFormat.format(
                        "dates for {0}. dueDate: {1}, completedDate: {2}, createDate: {3}, updateDate: {4}, recurringStartDate: {5}",
                        taskUuid, dueDate, completedDate, createDate, updateDate, recurringStartDate));*/

                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    System.out.println(csvLine);
                    throw e;
                }
            }

        } else {
            String sql =
                "INSERT INTO ergo.task (" +
                "   task_uuid, subscriber_id, `description`, due_date, `priority`, role_id, role_uuid, " +
                "   reminder_minutes_before, private_flag, `note`, `status`, completed_date, " +
                "   recurring_start_date, recurring_regenerative_flag, recurring_rrule, " +
                "   `order`, priority_order, role_order, create_date, update_date" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = null;
            ResultSet rs = null;

            int count = tableRowData.size();
            int idx = 0;

            String taskUuid = null;

            try {
                ps = con.prepareStatement(sql);
                ps.setInt(2, subscriberId);

                for (String csvLine : tableRowData) {
                    idx++;
                    System.out.println(MessageFormat.format("inserting task {0} of {1}", idx, count));

                    List<String> rowData = parseLine(csvLine);
                    taskUuid = rowData.get(1);
                    String description = convertStringUsingNewlines(rowData.get(2));
                    Date dueDate = getDateFromString(rowData.get(3));
                    String priority = rowData.get(4);
                    String roleUuid = getNullableString(rowData.get(5));

                    int reminderMinsBefore=0;
                    try { reminderMinsBefore = Integer.parseInt(rowData.get(6)); } catch (NumberFormatException ignored) {}

                    boolean privateFlag = Boolean.parseBoolean(rowData.get(7));
                    String note = convertStringUsingNewlines(rowData.get(8));
                    String status = rowData.get(9);
                    Date completedDate = getDateFromString(rowData.get(11));
                    int order = Integer.parseInt(rowData.get(12));
                    int roleOrder = Integer.parseInt(rowData.get(13));
                    int priorityOrder = Integer.parseInt(rowData.get(14));
                    Date createDate = getDateFromString(rowData.get(16));
                    Date updateDate = getDateFromString(rowData.get(17));
                    Date recurringStartDate = getDateFromString(rowData.get(18));
                    boolean recurringRegenerativeFlag = getBooleanFromString(rowData.get(19), false);
                    String recurringRRule = rowData.get(20);

                    Integer roleId = roleUuidToRoleIdMap.get(roleUuid);
                    if (roleId == null && roleUuid != null) {
                        roleUuid = null;
                    }

                    ps.setString(1, taskUuid);
                    ps.setString(3, description);
                    BaseDbSupport.setNullableDate(ps, 4, dueDate);
                    ps.setString(5, priority);
                    BaseDbSupport.setNullableInt(ps, 6, roleId);
                    ps.setString(7, roleUuid);
                    ps.setInt(8, reminderMinsBefore);
                    ps.setBoolean(9, privateFlag);
                    ps.setString(10, note);
                    ps.setString(11, status);
                    BaseDbSupport.setNullableDate(ps, 12, completedDate);
                    BaseDbSupport.setNullableDate(ps, 13, recurringStartDate);
                    ps.setBoolean(14, recurringRegenerativeFlag);
                    ps.setString(15, recurringRRule);
                    ps.setInt(16, order);
                    ps.setInt(17, priorityOrder);
                    ps.setInt(18, roleOrder);
                    BaseDbSupport.setNullableDate(ps, 19, createDate);
                    BaseDbSupport.setNullableDate(ps, 20, updateDate);

                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println("Task that blew up: " + taskUuid);

                throw new RuntimeException(e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    rs = null;
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    ps = null;
                }
            }
        }
    }

    private void deleteErgoData(Connection con, int subscriberId, boolean validateCsvIntegrityOnly)
    {
        if (validateCsvIntegrityOnly) return;

        PreparedStatement ps = null;
        Statement s = null;
        ResultSet rs = null;
        try {
            //delete tasks
            ps = con.prepareStatement("DELETE FROM ergo.task WHERE subscriber_id = ?");
            ps.setInt(1, subscriberId);
            ps.executeUpdate();
            ps.close();

//            //get all note ids
//            ps = con.prepareStatement("SELECT note.note_id FROM ergo.note WHERE subscriber_id = ?");
//            ps.setInt(1, subscriberId);
//            rs = ps.executeQuery();
//            List<Integer> noteIds = new ArrayList<>();
//            while (rs.next()) {
//                noteIds.add(rs.getInt(1));
//            }
//            rs.close();
//            ps.close();
//            String noteIdsAsCommaDelimitedStr = noteIds.stream().map(i -> i+"").collect(Collectors.joining(","));
//
//            //delete note tags
//            s = con.createStatement();
//            s.execute("DELETE FROM ergo.note_tag WHERE note_id IN ("+noteIdsAsCommaDelimitedStr+")");
//            s.close();

            //delete notes
            ps = con.prepareStatement("DELETE FROM ergo.note WHERE subscriber_id = ?");
            ps.setInt(1, subscriberId);
            ps.executeUpdate();
            ps.close();

            //delete roles
            ps = con.prepareStatement("DELETE FROM ergo.role WHERE subscriber_id = ?");
            ps.setInt(1, subscriberId);
            ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                rs = null;
            }
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                s = null;
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                ps = null;
            }
        }
    }

    private String getNullableString(String val)
    {
        if (val == null || val.trim().length() == 0) {
            return null;
        } else {
            return val;
        }
    }

    private boolean getBooleanFromString(String val, boolean defaultVal)
    {
        if (val == null || val.trim().length() == 0) {
            return defaultVal;
        } else {
            return Boolean.parseBoolean(val);
        }
    }

    private Date getDateFromString(String val)
    {
        if (val == null || val.trim().length() == 0 || "0".equals(val)) {
            return null;

        } else {
            return new Date(Long.parseLong(val));
        }
    }

    //https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
    private List<String> parseLine(String cvsLine)
    {
        char separators = DEFAULT_SEPARATOR;
        char customQuote = DEFAULT_QUOTE;

        List<String> result = new ArrayList<>();

        if (cvsLine == null || cvsLine.isEmpty()) {
            return result;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

    private List<String> getTableRowData(String csvFileHandle, String tableName)
    {
        File file = new File(csvFileHandle);
        String beginDelimiter = "table=" + tableName;
        List<String> tableRowData = new ArrayList<>();

        final AtomicReference<Boolean> foundBeginRef = new AtomicReference<>(false);
        final AtomicReference<Boolean> skipNextLineRef = new AtomicReference<>(true);
        final AtomicReference<Boolean> reachedEndRef = new AtomicReference<>(false);

        try (Stream<String> stream = Files.lines(Paths.get(file.toString()))) {
            stream.forEach(line -> {
                if (reachedEndRef.get()) {
                    //ignore everything else
                    ;
                } else if (foundBeginRef.get()) {
                    if (skipNextLineRef.get()) {
                        //don't store this - it's the row of column names
                        skipNextLineRef.set(false);
                    } else {
                        //see if we're past the end of the data (another table definition follows; if this is at the end, the loop will naturally end)
                        if (line.contains("table=")) {
                            reachedEndRef.set(true);
                        } else {
                            //this is a line that needs to be stored
                            tableRowData.add(line);
                        }
                    }

                } else {
                    //still looking for the beginning
                    foundBeginRef.set(line.contains(beginDelimiter));
                }
            });
        } catch (IOException e) {
            new IllegalStateException("unable to read file");
        }

        return tableRowData;
    }

    private Connection getConnection()
    {
        try {
            return _cpds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void close()
    {
        if (_cpds != null) {
            try {
                DataSources.destroy(_cpds);
            } catch (SQLException ignored) {
            }
        }
    }

    private String convertStringUsingNewlines(String input)
    {
        return input.replace("\\n", "\n");
    }

    public static void main(String[] args)
    {
        boolean validateCsvIntegrityOnly = false;

        int SUB_ID_DARL = 372;
        int subscriberId = SUB_ID_DARL;

        String csvFileHandle = "/Users/shawker/Downloads/darl_massaged2.csv";

        MoveLocalCsvToErgo mover = new MoveLocalCsvToErgo();
        Connection con = mover.getConnection();
        try {
//            //START TRANSACTION
//            System.out.println("transaction BEGIN");
//            con.setAutoCommit(false);

            //clear out old data
            System.out.println("removing old data...");
            mover.deleteErgoData(con, subscriberId, validateCsvIntegrityOnly);

            System.out.println("inserting data from csv file...");
            Map<String, Integer> roleUuidToRoleIdMap = mover.insertRoles(con, subscriberId, csvFileHandle, validateCsvIntegrityOnly);
            mover.insertNotes(con, subscriberId, csvFileHandle, roleUuidToRoleIdMap, validateCsvIntegrityOnly);
            mover.insertTasks(con, subscriberId, csvFileHandle, roleUuidToRoleIdMap, validateCsvIntegrityOnly);

//            //COMMIT TRANSACTION
//            System.out.println("transaction COMMIT");
//            con.commit();

//        } catch (Throwable e) {
//            try {
//                System.out.println("transaction ROLLBACK");
//                con.rollback();
//            } catch (SQLException e1) {
//                throw new RuntimeException(e1);
//            }
//            throw new RuntimeException(e);

        } finally {
            try {
//                con.setAutoCommit(true);
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            mover.close();
        }
    }

    public static void main1(String[] args) throws HeadlessException, UnsupportedFlavorException, IOException
    {
        String s= (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        s = s.replaceAll("\n", "\\\\n");

        StringSelection stringSelection = new StringSelection(s);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        System.out.println(s);
    }

    public static void mai3(String[] args)
    throws Exception
    {
        //String csvFileHandle = "/Users/shawker/Downloads/db_backup_2018-06-27_1642.csv";
        String csvFileHandle = "/Users/shawker/Downloads/darl_massaged.csv";
        File file = new File(csvFileHandle);

        List<String> finishedLines = new ArrayList<>();
        final AtomicReference<String> previousLineRef = new AtomicReference<>("");

        try (Stream<String> stream = Files.lines(Paths.get(file.toString()))) {
            stream.forEach(line -> {
                if (line.endsWith("\"\"") || !line.endsWith("\"")) {
                    String prev =  previousLineRef.get();
                    if (prev.length() == 0) {
                        previousLineRef.set(line);
                    } else {
                        previousLineRef.set(previousLineRef.get() + "\\n" + line);
                    }
                } else {
                    //line ends with a quote. combine with previous line and store
                    finishedLines.add(previousLineRef.get() + line);
                    previousLineRef.set("");
                }
            });
        } catch (IOException e) {
            new IllegalStateException("unable to read file");
        }

        Files.write(Paths.get("/Users/shawker/Downloads/darl_massaged2.csv"), (Iterable<String>)finishedLines.stream()::iterator);
    }

    public static void main4(String[] args)
    {
        String x = "1379617200000";
        long ms = Long.parseLong(x);
        int s = (int) (ms / 1_000L);
        int m = s/60;
        int h = m/60;
        int d = h/24;
        System.out.println(MessageFormat.format("ms: {0}, s: {1}, m: {2}, h: {3}, d: {4}", ms, s, m, h, d));
    }

}
