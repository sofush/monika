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
                    Kunde INTEGER NOT NULL,
                    Medarbejder INTEGER NOT NULL,
                    Fase TEXT NOT NULL,
                    FOREIGN KEY (Kunde) REFERENCES Kunde(Id),
                    FOREIGN KEY (Medarbejder) REFERENCES Medarbejder(Id)
                );""");

        this.conn.commit();
    }

    public void indsaetAftale(Aftale aftale) throws SQLException {
        int kundeId = this.indsaetKunde(aftale.kunde);
        int medarbejderId = this.indsaetMedarbejder(aftale.medarbejder);

        PreparedStatement st = this.conn.prepareStatement("""
                INSERT INTO Aftale(Id, Start, Stop, Kunde, Medarbejder, Fase)
                VALUES (?, ?, ?, ?, ?);
                """);

        st.setString(1, aftale.id.toString());
        st.setTimestamp(2, Timestamp.valueOf(aftale.start));
        st.setTimestamp(3, Timestamp.valueOf(aftale.stop));
        st.setInt(4, kundeId);
        st.setInt(5, medarbejderId);
        st.setString(6, aftale.fase.toString());
        st.executeUpdate();
        this.conn.commit();
    }
}
