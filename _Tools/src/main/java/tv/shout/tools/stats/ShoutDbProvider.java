package tv.shout.tools.stats;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

public class ShoutDbProvider
{
    private static final String SQL_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL_PRODUCTION = "jdbc:mysql://ms1-db2.shoutgameplay.com/gameplay?autoReconnect=true"; //production
    private static final String DB_URL_STAGE = "jdbc:mysql://stage-db-1.crpubohacrga.us-east-1.rds.amazonaws.com/gameplay?autoReconnect=true"; //stage
    private static final String DB_URL_LOCAL = "jdbc:mysql://foo.meinc?autoReconnect=true"; //local
    private static final String DB_URL_DC4 = "jdbc:mysql://dc4-db1.shoutgameplay.com:49331/";
    private static final String USERNAME_1 = "mroot";
    private static final String PASSWORD_1 = "Tre2quod3";
    private static final String USERNAME_2 = "root";
    private static final String PASSWORD_2 = "root";
    private static final String USERNAME_DC4 = "root";
    private static final String PASSWORD_DC4 = "rootroot";
    
    public static enum DB {PRODUCTION, STAGE, LOCAL, DC4};
    
    private static final int POOL_SIZE = 16;
    
    private ComboPooledDataSource _cpds;

    public ShoutDbProvider(DB which)
    {
        try {
            String dbUrl = null;
            String un = null, pw = null;
            switch (which)
            {
                case PRODUCTION:
                    dbUrl = DB_URL_PRODUCTION;
                    un = USERNAME_1;
                    pw = PASSWORD_1;
                    break;
                case STAGE:
                    dbUrl = DB_URL_STAGE;
                    un = USERNAME_1;
                    pw = PASSWORD_1;
                    break;
                case LOCAL:
                    dbUrl = DB_URL_LOCAL;
                    un = USERNAME_2;
                    pw = PASSWORD_2;
                case DC4:
                    dbUrl = DB_URL_DC4;
                    un = USERNAME_DC4;
                    pw = PASSWORD_DC4;
            }
            System.out.println("using db: " + dbUrl);
            
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
