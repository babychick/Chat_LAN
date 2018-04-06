package chatlan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author philx
 */
public class Library {
    public static Connection connectTO ()
    {
        Connection con = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String dbUrl = "jdbc:sqlserver://PHI\\SQLEXPRESS:1433;databaseName=Chat;user=sa;password=sa2014";
            con = DriverManager.getConnection(dbUrl);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }
}
