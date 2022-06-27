package tv.shout.sm.db;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

public class DbProvider
{
    private static final String DB_PROP_FILE = "/Volumes/Encrypted2/ShoutMeinc/snowyowl.properties";
    private static final String SQL_DRIVER = "com.mysql.jdbc.Driver";

    private static final String DB_URL_LOCAL = "jdbc:mysql://localhost:3306/";
    private static final String USERNAME_LOCAL = "meinc";
    private static final String PASSWORD_LOCAL = "pass123";

    private static final String DB_URL_NC10_1 = "jdbc:mysql://nc10-1.shoutgameplay.com:3306/";
    private static final String USERNAME_NC10_1_PROPKEY = "nc10-1.db.username";
    private static final String PASSWORD_NC10_1_PROPKEY = "nc10-1.db.password";

    private static final String DB_URL_NC11_1 = "jdbc:mysql://nc11-1.shoutgameplay.com:3306/";
    private static final String USERNAME_NC11_1_PROPKEY = "nc11-1.db.username";
    private static final String PASSWORD_NC11_1_PROPKEY = "nc11-1.db.password";

    private static final String DB_URL_shoutprod = "jdbc:mysql://ms1-db2.shoutgameplay.com:3306/";
    private static final String USERNAME_SHOUTPROD_PROPKEY = "shoutprod.db.username";
    private static final String PASSWORD_SHOUTPROD_PROPKEY = "shoutprod.db.password";

    public enum DB {LOCAL, /*DC4,*/ NC10_1, NC11_1, SHOUTPROD};

    private static final int POOL_SIZE = 16;

    private ComboPooledDataSource _cpds;

    public DbProvider(DB which)
    {
        try {
            String dbUrl, un, pw;
            switch (which)
            {
                case LOCAL: {
                    dbUrl = DB_URL_LOCAL;
                    un = USERNAME_LOCAL;
                    pw = PASSWORD_LOCAL;
                }
                break;

                case NC10_1: {
                    Properties prop = getProperties();
                    dbUrl = DB_URL_NC10_1;
                    un = prop.getProperty(USERNAME_NC10_1_PROPKEY);
                    pw = prop.getProperty(PASSWORD_NC10_1_PROPKEY);
                }
                break;

                case NC11_1: {
                    Properties prop = getProperties();
                    dbUrl = DB_URL_NC11_1;
                    un = prop.getProperty(USERNAME_NC11_1_PROPKEY);
                    pw = prop.getProperty(PASSWORD_NC11_1_PROPKEY);
                }
                break;

                case SHOUTPROD: {
                    Properties prop = getProperties();
                    dbUrl = DB_URL_shoutprod;
                    un = prop.getProperty(USERNAME_SHOUTPROD_PROPKEY);
                    pw = prop.getProperty(PASSWORD_SHOUTPROD_PROPKEY);
                }
                break;

                default:
                    throw new IllegalArgumentException("invalid DB");
            }
            //System.out.println("using db: " + dbUrl);

            _cpds = new ComboPooledDataSource();
            _cpds.setDriverClass(SQL_DRIVER);
            _cpds.setJdbcUrl(dbUrl);
            _cpds.setUser(un);
            _cpds.setPassword(pw);
            _cpds.setMinPoolSize(POOL_SIZE);
            _cpds.setAcquireIncrement(POOL_SIZE);
            _cpds.setMaxPoolSize(POOL_SIZE);

        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties()
    {
        try {
            Properties prop = new Properties();
            try (InputStream is = new FileInputStream(new File(DB_PROP_FILE))) {
                prop.load(is);
            }
            return prop;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection()
    throws SQLException
    {
        return _cpds.getConnection();
    }

    public void close()
    {
        if (_cpds != null) {
            try {
                DataSources.destroy(_cpds);
            } catch (SQLException ignored) {
                //e.printStackTrace();
            }
        }
    }
}
