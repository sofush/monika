import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public Connection conn;

    public Database(String brugernavn, String kodeord) {
        try {
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/monika", brugernavn, kodeord);
        } catch (SQLException e) {
            System.out.println("Kunne ikke tilslutte til databasen:\n\t" + e);
            System.exit(1);
        }
    }
}
