import java.sql.*;

public class Database {
    public Connection conn;

    public Database(String brugernavn, String kodeord) throws SQLException {
        try {
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/monika", brugernavn, kodeord);
        } catch (SQLException e) {
            System.out.println("Kunne ikke tilslutte til databasen:\n\t" + e);
            System.exit(1);
        }

        // Vi vil gerne styre commits manuelt da MySQL ellers automatisk laver et
        // commit efter hvert statement. Da statements kan fejle burde programmet
        // udføre statements gennem database transaktioner som kræver manuelle commits.
        conn.setAutoCommit(false);

        Statement st = this.conn.createStatement();

        System.out.println("Opretter `Aftale` tabellen.");
        st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Aftale(
                    Id VARCHAR(36) PRIMARY KEY,
                    Start DATETIME NOT NULL,
                    Stop DATETIME NOT NULL,
                    Kunde TEXT NOT NULL,
                    Fase TEXT NOT NULL
                );""");

        this.conn.commit();
    }

    public void indsaetAftale(Aftale aftale) throws SQLException {
        PreparedStatement st = this.conn.prepareStatement("""
                INSERT INTO Aftale(Id, Start, Stop, Kunde, Fase)
                VALUES (?, ?, ?, ?, ?);
                """);

        st.setString(1, aftale.id.toString());
        st.setTimestamp(2, Timestamp.valueOf(aftale.start));
        st.setTimestamp(3, Timestamp.valueOf(aftale.stop));
        st.setString(4, aftale.kunde);
        st.setString(5, aftale.fase.toString());
        st.executeUpdate();
        this.conn.commit();
    }
}
