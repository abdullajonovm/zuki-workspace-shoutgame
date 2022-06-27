package meinc.zztasks;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.lambdaworks.codec.Base64;
import com.lambdaworks.crypto.SCrypt;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import tv.shout.sm.db.BaseDbSupport;
import tv.shout.sm.db.BaseDbSupport.SqlMapper;
import tv.shout.snowyowl.common.FileHandler;

public class MoveGoogleToZzTasks
implements FileHandler
{
    private static final int POOL_SIZE = 16;
    private static Pattern SENTENCE_CAP = Pattern.compile("([\\?!\\.]\\s*)([a-z])");
    private static final String ALGORITHM = "AES";
    private static final String SQL_DRIVER = "com.mysql.jdbc.Driver";
    private static String passKey = "wh@tchamac0llit!";
    //private static String passKey = "ph8z#vjs/KVW7nzq";

    private static final String GOOGLE_CLIENT_ID_1 = "332299754018.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET_1 = "ph9AR7GnwayAdlnpd6IYQh_u";

    private static final String GOOGLE_CLIENT_ID_2 = "332299754018-4q27hgvglgnrss5nv251g17d391k9biq.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET_2 = "9ktS7VCI0InosdCBPpbAjKSQ";

    private static final String GOOGLE_CLIENT_ID_3 = "49723205841-o46k7ea04g8jsjj1hql88embbpgdv2q6.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET_3 = "M_bGqkCq-ECTB5gZagO3ntZN";

    private static List<String> GOOGLE_CLIENT_IDS;
    private static List<String> GOOGLE_CLIENT_SECRETS;

    private HashMap<Integer, Task> _processedTasks;
    private HashSet<Integer> _processedTaskIds;

    private ComboPooledDataSource _cpds;

    private MoveGoogleToZzTasks()
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

        GOOGLE_CLIENT_IDS = Arrays.asList(GOOGLE_CLIENT_ID_1, GOOGLE_CLIENT_ID_2, GOOGLE_CLIENT_ID_3);
        GOOGLE_CLIENT_SECRETS = Arrays.asList(GOOGLE_CLIENT_SECRET_1, GOOGLE_CLIENT_SECRET_2, GOOGLE_CLIENT_SECRET_3);
    }

    private List<Role> getGoogleRoles(int subscriberId)
    {
        String sql = "SELECT * FROM ergo.google_role WHERE subscriber_id = ? ORDER BY create_date ASC";

        SqlMapper<Role> sqlMapper = new SqlMapper<Role>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setInt(1, subscriberId);
            }

            @Override
            public Role mapRowToType(ResultSet rs) throws SQLException
            {
                Role r = new Role();

                r.roleId = rs.getInt("role_id");
                r.roleUuid = rs.getString("role_uuid");
                r.subscriberId = subscriberId;
                r.name = rs.getString("name");
                r.icon = rs.getString("icon");
                r.color = rs.getString("color");
                r.order = rs.getInt("order");
                r.createDate = rs.getTimestamp("create_date");
                r.updateDate = rs.getTimestamp("update_date");
                r.deleteDate = rs.getTimestamp("delete_date");

                r.googleServerId = rs.getString("server_id");

                return r;
            }

            @Override
            public Collection<Role> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            return (List<Role>) BaseDbSupport.executeSqlForList(getConnection(), sql, sqlMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Note> getGoogleNotes(int subscriberId)
    {
        String sql = "SELECT * FROM ergo.google_note WHERE subscriber_id = ? ORDER BY create_date ASC";

        SqlMapper<Note> sqlMapper = new SqlMapper<Note>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setInt(1, subscriberId);
            }

            @Override
            public Note mapRowToType(ResultSet rs) throws SQLException
            {
                Note n = new Note();

                n.noteId = rs.getInt("note_id");
                n.noteUuid = rs.getString("note_uuid");
                n.subscriberId = subscriberId;
                n.note = rs.getString("note");
                n.prvate = rs.getBoolean("private_flag");
                n.roleId = rs.getInt("role_id");
                n.roleUuid = rs.getString("role_uuid");
                n.order = rs.getInt("order");
                n.createDate = rs.getTimestamp("create_date");
                n.updateDate = rs.getTimestamp("update_date");
                n.deleteDate = rs.getTimestamp("delete_date");

                return n;
            }

            @Override
            public Collection<Note> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        List<Note> notes;
        try {
            notes =  (List<Note>) BaseDbSupport.executeSqlForList(getConnection(), sql, sqlMapper);
            notes.forEach(this::addTagsToGoogleNote);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return notes;
    }

    private List<Task> getGoogleTasks(int subscriberId, Map<String, Role> roleMap)
    {
        //first see what (if any) tasks have already been loaded
        File fProcessedTaskIds = new File("/Users/shawker/temp/zztasks_" + subscriberId + "_processedTaskIds.dat");
        File fProcessedTasks = new File("/Users/shawker/temp/zztasks_" + subscriberId + "_processedTasks.dat");
        if (fProcessedTaskIds.exists()) {
            try {
                _processedTaskIds = (HashSet<Integer>) readFromFile(fProcessedTaskIds);
                _processedTasks = (HashMap<Integer, Task>) readFromFile(fProcessedTasks);
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            _processedTaskIds = new HashSet<Integer>();
            _processedTasks = new HashMap<>();
        }

        String sql = "SELECT * FROM ergo.google_task WHERE subscriber_id = ? ORDER BY create_date ASC";

        SqlMapper<Task> sqlMapper = new SqlMapper<Task>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setInt(1, subscriberId);
            }

            @Override
            public Task mapRowToType(ResultSet rs) throws SQLException
            {
                Task t = new Task();

                t.taskId = rs.getInt("task_id");
                t.taskUuid = rs.getString("task_uuid");
                t.subscriberId = subscriberId;
                t.prvate = rs.getBoolean("private_flag");
                t.description = rs.getString("private_description");
                t.note = rs.getString("private_note");
                t.dueDate = rs.getTimestamp("private_due_date");
                t.roleId = BaseDbSupport.getNullableInt(rs, "role_id");
                t.roleUuid = rs.getString("role_uuid");
                t.reminderMinutesBefore = BaseDbSupport.getNullableInt(rs, "reminder_minutes_before");
                t.reminded = rs.getBoolean("reminded");
                t.reminder = rs.getTime("reminder");
                t.completedDate = rs.getTimestamp("completed_date");
                t.delegateEmail = rs.getString("delegate_email");
                t.recurringStartDate = rs.getTimestamp("recurring_start_date");
                t.recurringRegenerativeFlag = rs.getBoolean("recurring_regenerative_flag");
                t.recurringRRule = rs.getString("recurring_rrule");
                t.nextTaskCreated = rs.getBoolean("next_task_created");
                t.order = rs.getInt("order");
                t.priorityOrder = rs.getInt("priority_order");
                t.roleOrder = rs.getInt("role_order");
                t.timezone = rs.getString("timezone");
                t.createDate = rs.getTimestamp("create_date");
                t.updateDate = rs.getTimestamp("update_date");
                t.deleteDate = rs.getTimestamp("delete_date");

                t.googleServerId = rs.getString("server_id");
                t.unconvertedErgoStatus = rs.getString("status");
                t.unconvertedErgoPriority = rs.getString("priority");


                return t;
            }

            @Override
            public Collection<Task> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            List<Task> tasks = (List<Task>) BaseDbSupport.executeSqlForList(getConnection(), sql, sqlMapper);

            //if the # of tasks is the same as the # of processed id's, then all tasks are done, move on.
            if (tasks.size() == _processedTaskIds.size()) {
                return _processedTasks.values().stream().collect(Collectors.toList());
            }

            //initialize the Google oauth tasks service
            String googleOauthToken = getGoogleOauthToken(subscriberId);
            GoogleTokenResponse response;
            Tasks googleOauthService = null;
            for (int i=0; i<GOOGLE_CLIENT_IDS.size(); i++) {
                String googleClientId = GOOGLE_CLIENT_IDS.get(i);
                String googleClientSecret = GOOGLE_CLIENT_SECRETS.get(i);
                try {
                    response = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(), googleOauthToken, googleClientId, googleClientSecret).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (NullPointerException e) {
                    //ignore; this will move on to the next clientId/clientSecret combo
                    continue;
                }
                String accessToken = response.getAccessToken();
                GoogleCredential cred = new GoogleCredential().setAccessToken(accessToken);
                googleOauthService = new Tasks(new NetHttpTransport(), new JacksonFactory(), cred);
            }
            if (googleOauthService == null) {
                throw new RuntimeException("unable to find a workable clientId/clientSecret combo");
            }

            int i=0;
            for (Task task : tasks) {
                i++;

                //see if the task has already been processed. if so, skip it
                if (_processedTaskIds.contains(task.taskId)) continue;


                System.out.println("processing task "+i+" of "+tasks.size()+"...");
                if (task.prvate) {
                    task.status = Task.STATUS.valueOf(task.unconvertedErgoStatus);
                    task.priority = Task.PRIORITY.valueOf(task.unconvertedErgoPriority);

                    //mark as having been processed
                    _processedTaskIds.add(task.taskId);
                    _processedTasks.put(task.taskId, task);
                    try {
                        writeToFile(fProcessedTaskIds, _processedTaskIds);
                        writeToFile(fProcessedTasks, _processedTasks);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    continue;
                }

                //since the task is not marked as private, some of the data is only stored on the google servers and must be retrieved
                String googleTasklistId = getTasklistId(task, roleMap);
                String googleServerId = task.googleServerId;

                if (googleServerId == null) {
                    //mark as having been processed
                    _processedTaskIds.add(task.taskId);
                    try {
                        writeToFile(fProcessedTaskIds, _processedTaskIds);
                    } catch (IOException ee) {
                        throw new RuntimeException(ee);
                    }

                    //this means there is a data sync error and the local data has something that google does not. just remove the task
                    System.out.println("WARNING: removing task. no google server id in db!");
                    task = null;

                    continue;
                }

                try {
                    //get the task from google
                    com.google.api.services.tasks.model.Task googleTask = googleOauthService.tasks().get(googleTasklistId, googleServerId).execute();

                    //add/convert the relevant data

                    task.updateDate = new Date(googleTask.getUpdated().getValue());

                    if (googleTask.getDeleted() != null && googleTask.getDeleted()) {
                        task.deleteDate = new Date(googleTask.getUpdated().getValue());
                    }

                    if (googleTask.getCompleted() != null) {
                        task.completedDate = task.updateDate;
                        task.status = Task.STATUS.COMPLETE;
                    } else {
                        task.status = convertGoogleStatusToErgoStatus(googleTask.getStatus(), task.unconvertedErgoStatus);
                    }

                    if (googleTask.getDue() != null) {
                        Date date = new Date(googleTask.getDue().getValue());
                        task.dueDate = date;
                    }

                    task.note = googleTask.getNotes();

                    //all uppercase from server means "big rock" priority
                    if (googleTask.getTitle().equals(googleTask.getTitle().toUpperCase())) {
                        task.priority = Task.PRIORITY.BIGROCK;
                    } else {
                        task.priority = Task.PRIORITY.valueOf(task.unconvertedErgoPriority);
                    }

                    if (task.priority == Task.PRIORITY.BIGROCK) {
                        task.description = capitalizeFirstLetterInEverySentence(googleTask.getTitle().toLowerCase());
                    } else {
                        task.description = googleTask.getTitle();
                    }

                    //mark as having been processed
                    _processedTaskIds.add(task.taskId);
                    _processedTasks.put(task.taskId, task);
                    try {
                        writeToFile(fProcessedTaskIds, _processedTaskIds);
                        writeToFile(fProcessedTasks, _processedTasks);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } catch (GoogleJsonResponseException e) {
                    if (e.getStatusCode() == 404) {
                        //mark as having been processed
                        _processedTaskIds.add(task.taskId);
                        try {
                            writeToFile(fProcessedTaskIds, _processedTaskIds);
                        } catch (IOException ee) {
                            throw new RuntimeException(ee);
                        }

                        //this means there is a data sync error and the local data has something that google does not. just remove the task
                        System.out.println("WARNING: 404 - removing task. not found in Google");
                        task = null;

                    } else {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

//            //remove any null tasks before returning the final list
//            return tasks.stream().filter(t -> t != null).collect(Collectors.toList());

            return _processedTasks.values().stream().collect(Collectors.toList());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String decrypt(String encryptedString)
    {
        byte[] keyBytes = null;
        byte[] encryptedBytes = null;

        String[] encryptedStringParts = encryptedString.split("\\$");
        if (encryptedStringParts.length == 1) {
            // Old weak cipher
            keyBytes = passKey.getBytes();
            try {
                encryptedBytes = new sun.misc.BASE64Decoder().decodeBuffer(encryptedString);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else if (encryptedStringParts.length == 5) {
            // New strong cipher
            if (!"s0a".equals(encryptedStringParts[1]))
                throw new IllegalArgumentException("Unknown encryption of type '"+encryptedStringParts[1]+"'");

            long scryptParams = Long.parseLong(encryptedStringParts[2], 16);
            byte[] saltBytes = Base64.decode(encryptedStringParts[3].toCharArray());
            encryptedBytes = Base64.decode(encryptedStringParts[4].toCharArray());

            int N = (int) Math.pow(2, scryptParams >> 16 & 0xffff);
            int r = (int) scryptParams >> 8 & 0xff;
            int p = (int) scryptParams & 0xff;

            System.out.println("N: " + N + ", r: " + r + ", p: " + p);

            try {
                keyBytes = SCrypt.scrypt(passKey.getBytes("UTF-8"), saltBytes, N, r, p, 32);
            } catch (UnsupportedEncodingException e) {
                // Shouldn't ever happen
                System.out.println("Internal error: " + e.getMessage());
                throw new IllegalStateException(e);
            } catch (GeneralSecurityException e) {
                // Shouldn't ever happen
                System.out.println("Internal error: " + e.getMessage());
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalArgumentException("Unknown encryption of type '"+encryptedStringParts[1]+"'");
        }

        SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // Shouldn't ever happen
            System.out.println("Internal error: " + e.getMessage());
            throw new IllegalStateException(e);
        } catch (NoSuchPaddingException e) {
            // Shouldn't ever happen
            System.out.println("Internal error: " + e.getMessage());
            throw new IllegalStateException(e);
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e);
        }

        byte[] decValue;
        try {
            decValue = cipher.doFinal(encryptedBytes);
        } catch (IllegalBlockSizeException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (BadPaddingException e) {
            throw new IllegalStateException(e);
        }
        String decryptedValue;
        try {
            decryptedValue = new String(decValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Shouldn't ever happen
            System.out.println("Internal error: " + e.getMessage());
            throw new IllegalStateException(e);
        }
        return decryptedValue;
    }

    //http://tech.chitgoks.com/2010/09/24/capitalize-first-letter-of-every-sentence-in-java/
    private String capitalizeFirstLetterInEverySentence(String content)
    {
        if (content == null || content.length() < 2) return content;

        Matcher m = SENTENCE_CAP.matcher(content);
        while (m.find()) {
            content = m.replaceFirst(m.group(1) + m.group(2).toUpperCase());
            m = SENTENCE_CAP.matcher(content);
        }

        // Capitalize the first letter of the string.
        content = String.format("%s%s", Character.toUpperCase(content.charAt(0)), content.substring(1));

        return content;
    }

    private Task.STATUS convertGoogleStatusToErgoStatus(String googleStatus, String unconvertedErgoStatus)
    {
        if ("completed".equals(googleStatus))
            return Task.STATUS.COMPLETE;
        else
            return unconvertedErgoStatus == "COMPLETE" ? Task.STATUS.NORMAL : Task.STATUS.valueOf(unconvertedErgoStatus);
    }

    /**
     * Take the tasksroleUuid and convert to a Google taskListId
     */
    private String getTasklistId(Task task, Map<String, Role> roleMap)
    {
        String tasklistId;
        if (task.roleUuid == null) {
            tasklistId = "@default";
        } else {
            //convert local role uuid to google server's tasklist id
            Role role = roleMap.get(task.roleUuid);
            tasklistId = role.googleServerId;
        }

        return tasklistId;
    }

    private String getGoogleOauthToken(int subscriberId)
    {
        String sql = "SELECT oauth_auth_token FROM ergo.provider WHERE subscriber_id = ? AND provider_type = 'GOOGLE'";

        Connection con = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, subscriberId);
            rs = ps.executeQuery();
            rs.next();

            String encryptedToken = rs.getString(1);

            String unencryptedToken = decrypt(encryptedToken);
            return unencryptedToken;

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
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void addTagsToGoogleNote(Note note)
    {
        String sql = "SELECT tag_id FROM ergo.google_note_tag WHERE note_id = ?";

        SqlMapper<Integer> sqlMapper = new SqlMapper<Integer>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setInt(1, note.noteId);
            }

            @Override
            public Integer mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getInt(1);
            }

            @Override
            public Collection<Integer> getCollectionObject()
            {
                return new ArrayList<>();
            }
        };

        try {
            List<Integer> tagIds =  (List<Integer>) BaseDbSupport.executeSqlForList(getConnection(), sql, sqlMapper);
            note.tagIds = tagIds;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteErgoData(Connection con, int subscriberId)
    {
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

    private void deleteGoogleData(Connection con, int subscriberId)
    {
        PreparedStatement ps = null;
        Statement s = null;
        ResultSet rs = null;
        try {
            //delete tasks
            ps = con.prepareStatement("DELETE FROM ergo.google_task WHERE subscriber_id = ?");
            ps.setInt(1, subscriberId);
            ps.executeUpdate();
            ps.close();

//            //get all note ids
//            ps = con.prepareStatement("SELECT note_id FROM ergo.google_note WHERE subscriber_id = ?");
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
//            s.execute("DELETE FROM ergo.google_note_tag WHERE note_id IN ("+noteIdsAsCommaDelimitedStr+")");
//            s.close();

            //delete notes
            ps = con.prepareStatement("DELETE FROM ergo.google_note WHERE subscriber_id = ?");
            ps.setInt(1, subscriberId);
            ps.executeUpdate();
            ps.close();

            //delete roles
            ps = con.prepareStatement("DELETE FROM ergo.google_role WHERE subscriber_id = ?");
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

    private void deleteGoogleProvider(Connection con, int subscriberId)
    {
        String sql = "DELETE FROM ergo.provider WHERE subscriber_id = ? AND provider_type = 'GOOGLE'";
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, subscriberId);
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
        }
    }

    private void deleteSessions(Connection con, int subscriberId)
    {
        String sql = "DELETE FROM ergo.subscriber_device_app WHERE subscriber_id = ?";
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, subscriberId);
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
        }
    }

    private Map<Integer, Integer> insertErgoRoles(Connection con, List<Role> roles)
    {
        Map<Integer, Integer> idMapping = new HashMap<>();
        if (roles == null || roles.size() == 0) return idMapping;

        String sql =
            "INSERT INTO ergo.role (" +
            "   role_uuid, subscriber_id, name, color, icon, `order`, create_date, update_date, delete_date " +
            ") VALUES (" +
            "   ?, ?, ?, ?, ?, ?, ?, ?, ?" +
            ")";
        String sqlGetKey = "select last_insert_id()";
        PreparedStatement ps = null;
        ResultSet rs = null;
        Statement s = null;

        try {
            s = con.createStatement();
            ps = con.prepareStatement(sql);

            //for (Role role : roles) {
            for (int i=0; i<roles.size(); i++) {
                Role role = roles.get(i);
                System.out.println(MessageFormat.format("adding role {0} of {1}", i, roles.size()));

                ps.setString(1, role.roleUuid);
                ps.setInt(2, role.subscriberId);
                ps.setString(3, role.name);
                ps.setString(4, role.color);
                ps.setString(5, role.icon);
                ps.setInt(6, role.order);
                ps.setTimestamp(7, new Timestamp(role.createDate.getTime()));
                BaseDbSupport.setNullableDate(ps, 8, role.updateDate);
                BaseDbSupport.setNullableDate(ps, 9, role.deleteDate);

                ps.executeUpdate();

                //grab the newly inserted id and store in the mapping
                rs = s.executeQuery(sqlGetKey);
                rs.next();
                int newRoleId = rs.getInt(1);
                int oldRoleId = role.roleId;
                rs.close();
                idMapping.put(oldRoleId, newRoleId);
            };

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

        return idMapping;
    }

    private void insertErgoNotes(Connection con, List<Note> notes, Map<Integer, Integer> roleIdMappings)
    {
        //there are no note_tag rows in the entire database. obviously, an uncompleted feature (or at least not exposed to the clients),
        // so for the sake of simplification, i am ignoring note_tag

        if (notes == null || notes.size() == 0) return;

        String sql =
            "INSERT INTO ergo.`note` (" +
            "   note_uuid, subscriber_id, `note`, private_flag, role_id, role_uuid, `order`, create_date, update_date, delete_date " +
            ") VALUES (" +
            "   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
            ")";

        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);

            //for (Note note : notes) {
            for (int i=0; i<notes.size(); i++) {
                Note note = notes.get(i);
                System.out.println(MessageFormat.format("adding note {0} of {1}", i, notes.size()));

                ps.setString(1, note.noteUuid);
                ps.setInt(2, note.subscriberId);
                ps.setString(3, note.note);
                ps.setBoolean(4, note.prvate);
                BaseDbSupport.setNullableInt(ps, 5, roleIdMappings.get(note.roleId));
                ps.setString(6, note.roleUuid);
                ps.setInt(7, note.order);
                ps.setTimestamp(8, new Timestamp(note.createDate.getTime()));
                BaseDbSupport.setNullableDate(ps, 9, note.updateDate);
                BaseDbSupport.setNullableDate(ps, 10, note.deleteDate);
                ps.executeUpdate();
            };

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
        }
    }

    private void insertErgoTasks(Connection con, List<Task> tasks, Map<Integer, Integer> roleIdMappings)
    {
        if (tasks == null || tasks.size() == 0) return;
        Map<Integer, Integer> idMapping = new HashMap<>();

        String sql =
            "INSERT INTO ergo.task (" +
            "   task_uuid, subscriber_id, description, due_date, priority, role_id, role_uuid, " +
            "   reminder_minutes_before, reminded, reminder, private_flag, `note`, `status`, completed_date, delegate_email, " +
            "   recurring_start_date, recurring_regenerative_flag, recurring_rrule, next_task_created, " +
            "   `order`, priority_order, role_order, `timezone`, create_date, update_date, delete_date " +
            ") VALUES (" +
            "   ?, ?, ?, ?, ?, ?, ?, " +
            "   ?, ?, ?, ?, ?, ?, ?, ?, " +
            "   ?, ?, ?, ?, " +
            "   ?, ?, ?, ?, ?, ?, ?" +
            ")";
        String sqlGetKey = "select last_insert_id()";
        PreparedStatement ps = null;
        ResultSet rs = null;
        Statement s = null;

        //sort the tasks in create_date desc order so newest tasks are first
        // this is necessary because recurring tasks are created after the task that spawned them, but the task that spawned them
        // refers to the new task, and since id's are changing they need to be mapped, so the newer tasks have to come first
        tasks = tasks.stream().sorted( (ta, tb) -> Long.compare(tb.createDate.getTime(), ta.createDate.getTime()) ).collect(Collectors.toList());

        try {
            s = con.createStatement();
            ps = con.prepareStatement(sql);

            //for (Task task : tasks) {
            for (int i=0; i<tasks.size(); i++) {
                Task task = tasks.get(i);
                System.out.println(MessageFormat.format("adding task {0} of {1}", i, tasks.size()));

                ps.setString(1, task.taskUuid);
                ps.setInt(2, task.subscriberId);
                ps.setString(3, task.description);
                BaseDbSupport.setNullableDate(ps, 4, task.dueDate);
                ps.setString(5, task.priority.toString());
                BaseDbSupport.setNullableInt(ps, 6, roleIdMappings.get(task.roleId));
                ps.setString(7, task.roleUuid);
                BaseDbSupport.setNullableInt(ps, 8, task.reminderMinutesBefore);
                ps.setBoolean(9, task.reminded);
                BaseDbSupport.setNullableDate(ps, 10, task.reminder);
                ps.setBoolean(11, task.prvate);
                ps.setString(12, task.note);
                ps.setString(13, task.status.toString());
                BaseDbSupport.setNullableDate(ps, 14, task.completedDate);
                ps.setString(15, task.delegateEmail);
                BaseDbSupport.setNullableDate(ps, 16, task.recurringStartDate);
                ps.setBoolean(17, task.recurringRegenerativeFlag);
                ps.setString(18, task.recurringRRule);
                BaseDbSupport.setNullableInt(ps, 19, idMapping.get(task.taskId));
                ps.setInt(20, task.order);
                ps.setInt(21, task.priorityOrder);
                ps.setInt(22, task.roleOrder);
                ps.setString(23, task.timezone);
                ps.setTimestamp(24, new Timestamp(task.createDate.getTime()));
                BaseDbSupport.setNullableDate(ps, 25, task.updateDate);
                BaseDbSupport.setNullableDate(ps, 26, task.deleteDate);

                ps.executeUpdate();

                //grab the newly inserted id and store in the mapping
                rs = s.executeQuery(sqlGetKey);
                rs.next();
                int newTaskId = rs.getInt(1);
                int oldTaskId = task.taskId;
                rs.close();
                idMapping.put(oldTaskId, newTaskId);
            };

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

    private void testConnection()
    {
        String sql = "SELECT COUNT(*) FROM ergo.subscriber";
        Connection con = getConnection();
        Statement s = null;
        ResultSet rs = null;
        try {
            s = con.createStatement();
            rs = s.executeQuery(sql);
            rs.next();
            System.out.println("# of subscribers: " + rs.getInt(1));

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
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
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

    public void moveSubscriberFromGoogleToErgo(int subscriberId, boolean testOnly)
    {
        //get the google roles
        List<Role> googleRoles = getGoogleRoles(subscriberId);

        //convert roles to quick lookup map (via roleUuid)
        Map<String, Role> roleMap = new HashMap<>(googleRoles.size());
        googleRoles.forEach(r -> roleMap.put(r.roleUuid, r));

        //grab the google notes
        List<Note> googleNotes = getGoogleNotes(subscriberId);

        //grab the google tasks
        List<Task> googleTasks = getGoogleTasks(subscriberId, roleMap);

        if (testOnly) return;

        Connection con = getConnection();
        try {
            //START TRANSACTION
            System.out.println("transaction BEGIN");
            con.setAutoCommit(false);

            //clear out old data
            System.out.println("removing old data...");
            deleteErgoData(con, subscriberId);
            deleteGoogleData(con, subscriberId);
            deleteGoogleProvider(con, subscriberId);
            deleteSessions(con, subscriberId);

            //add the google data into ergo as ergo data
            Map<Integer, Integer> roleIdMappings = insertErgoRoles(con, googleRoles);
            insertErgoNotes(con, googleNotes, roleIdMappings);
            insertErgoTasks(con, googleTasks, roleIdMappings);

            //COMMIT TRANSACTION
            System.out.println("transaction COMMIT");
            con.commit();

        } catch (Throwable e) {
            try {
                System.out.println("transaction ROLLBACK");
                con.rollback();
            } catch (SQLException e1) {
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        } finally {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args)
    {
        boolean testOnly = false;

        int SEAN_COVEY = 371;
        int SHAWKER = 2540;
        int SARA_LENTZ = 21437;

        MoveGoogleToZzTasks migrator = new MoveGoogleToZzTasks();
        try {
            migrator.testConnection();
            migrator.moveSubscriberFromGoogleToErgo(SARA_LENTZ, testOnly);

            System.out.println("DONE");
        } finally {
            migrator.close();
        }
    }

}
