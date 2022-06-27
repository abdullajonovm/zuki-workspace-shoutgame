package tv.shout.tools.stats;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QuestionInputter
{
    ShoutDbProvider _db = new ShoutDbProvider(ShoutDbProvider.DB.DC4);
    
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
    
    public void shutdown()
    {
        _db.close();
    }
    
    public static void main(String[] args)
    throws Exception
    {
        QuestionInputter q = new QuestionInputter();
        
        System.out.println("# questions: " + q.getQuestionCount());
        
        q.shutdown();
    }

}
