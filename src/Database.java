import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        System.out.println("Opretter `Medarbejder` tabellen.");
        st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS Medarbejder(
                Id INTEGER PRIMARY KEY AUTO_INCREMENT,
                Navn TEXT NOT NULL
            );""");

        System.out.println("Opretter `Kunde` tabellen.");
        st.executeUpdate("""
            CREATE TABLE IF NOT EXISTS Kunde(
                Id INTEGER PRIMARY KEY AUTO_INCREMENT,
                Navn TEXT NOT NULL
            );""");

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
        int medarbejderId;

        {
            PreparedStatement st = this.conn.prepareStatement("""
                SELECT (Id) FROM Medarbejder
                WHERE LOWER(Navn) = (?)
                LIMIT 1;
                """);
            st.setString(1, aftale.medarbejder.navn.toLowerCase());
            ResultSet rs = st.executeQuery();

            if (!rs.next()) {
                System.out.println("Kunne ikke finde medarbejder ved navn \"" + aftale.medarbejder.navn + "\". Afbryder.");
                return;
            }

            medarbejderId = rs.getInt(1);
        }

        PreparedStatement st = this.conn.prepareStatement("""
            INSERT INTO Aftale(Id, Start, Stop, Kunde, Medarbejder, Fase)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE Start = ?, Stop = ?, Kunde = ?, Medarbejder = ?, Fase = ?;
            """);

        // INSERT argumenter.
        st.setString(1, aftale.id.toString());
        st.setTimestamp(2, Timestamp.valueOf(aftale.start));
        st.setTimestamp(3, Timestamp.valueOf(aftale.stop));
        st.setInt(4, kundeId);
        st.setInt(5, medarbejderId);
        st.setString(6, aftale.fase.toString());

        // UPDATE argumenter.
        st.setTimestamp(7, Timestamp.valueOf(aftale.start));
        st.setTimestamp(8, Timestamp.valueOf(aftale.stop));
        st.setInt(9, kundeId);
        st.setInt(10, medarbejderId);
        st.setString(11, aftale.fase.toString());

        st.executeUpdate();
        this.conn.commit();
    }

    public void indsaetMedarbejder(Medarbejder medarbejder, String kodeord) throws SQLException {
        {
            PreparedStatement st = this.conn.prepareStatement("""
                SELECT 1 FROM Medarbejder
                WHERE LOWER(Navn) = ?
                LIMIT 1;
                """);
            st.setString(1, medarbejder.navn);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                System.out.println("Medarbejder ved navn '" + medarbejder.navn + "' eksisterer allerede i databasen.");
                return;
            }
        }

        {
            PreparedStatement st = this.conn.prepareStatement("""
                CREATE USER ?@'%' IDENTIFIED BY ?;
                """);
            st.setString(1, medarbejder.navn);
            st.setString(2, kodeord);
            st.executeUpdate();
        }

        {
            PreparedStatement st = this.conn.prepareStatement("""
                GRANT ALL PRIVILEGES ON *.* TO ?@'%';
                """);
            st.setString(1, medarbejder.navn);
            st.executeUpdate();
        }

        {
            PreparedStatement st = this.conn.prepareStatement("""
                FLUSH PRIVILEGES;
                """);
            st.execute();
        }

        PreparedStatement st = this.conn.prepareStatement("""
            INSERT INTO Medarbejder (Navn)
            VALUES (?);
            """, Statement.RETURN_GENERATED_KEYS);

        st.setString(1, medarbejder.navn);
        st.executeUpdate();
        this.conn.commit();

        ResultSet keys = st.getGeneratedKeys();
        keys.next();
        keys.getInt(1);
    }

    public int indsaetKunde(Kunde kunde) throws SQLException {
        PreparedStatement st = this.conn.prepareStatement("""
            INSERT INTO Kunde(Navn)
            VALUES (?);
            """, Statement.RETURN_GENERATED_KEYS);

        st.setString(1, kunde.navn);
        st.executeUpdate();

        ResultSet keys = st.getGeneratedKeys();
        keys.next();
        return keys.getInt(1);
    }

    public List<Medarbejder> indlaesMedarbejdere() throws SQLException {
        Statement st = this.conn.createStatement();
        ResultSet rs = st.executeQuery("""
            SELECT (Navn) FROM Medarbejder;
            """);

        List<Medarbejder> liste = new ArrayList<>();

        while (rs.next()) {
            liste.add(new Medarbejder(rs.getString(1)));
        }

        return liste;
    }

    public void sletMedarbejder(Medarbejder medarbejder) throws SQLException {
        PreparedStatement st = this.conn.prepareStatement("""
            UPDATE Medarbejder
            SET Navn = CONCAT(Navn, ' (slettet)')
            WHERE LOWER(Navn) = ? AND Navn NOT LIKE '% (slettet)'
            LIMIT 1;
            """);
        st.setString(1, medarbejder.navn.toLowerCase());
        st.executeUpdate();
        this.conn.commit();
    }

    public Aftale indlaesAftale(String uuid) throws SQLException {
        PreparedStatement st = this.conn.prepareStatement("""
            SELECT Aftale.Id, Aftale.Start, Aftale.Stop, Aftale.Fase, Kunde.Navn, Medarbejder.Navn
            FROM Aftale
            INNER JOIN Medarbejder
            ON Medarbejder.Id = Aftale.Medarbejder
            INNER JOIN Kunde
            ON Kunde.Id = Aftale.Kunde
            WHERE Aftale.Id = ?
            LIMIT 1;
            """);
        st.setString(1, uuid.toLowerCase());
        ResultSet rs = st.executeQuery();

        if (!rs.next()) return null;

        return new Aftale(
            rs.getString("Aftale.Id"),
            rs.getTimestamp("Aftale.Start").toLocalDateTime(),
            rs.getTimestamp("Aftale.Stop").toLocalDateTime(),
            new Kunde(rs.getString("Kunde.Navn")),
            new Medarbejder(rs.getString("Medarbejder.Navn")),
            Fase.valueOf(rs.getString("Fase"))
        );
    }

    public List<Aftale> indlaesAftaler(Medarbejder medarbejder, LocalDateTime start, LocalDateTime stop) throws SQLException {
        PreparedStatement st =  this.conn.prepareStatement("""
            SELECT Id FROM Medarbejder
            WHERE LOWER(Navn) = ?;
            """);
        st.setString(1, medarbejder.navn);
        ResultSet rs = st.executeQuery();

        if (!rs.next()) {
            System.out.println("Fejl: Kunne ikke finde medarbejder ved navn `" + medarbejder.navn + "`.");
            return new ArrayList<>();
        }

        int medarbejderId = rs.getInt(1);

        PreparedStatement st1 =  this.conn.prepareStatement("""
            SELECT Aftale.Id, Aftale.Start, Aftale.Stop, Kunde.Navn, Aftale.Fase
            FROM Aftale
            INNER JOIN Kunde
            ON Kunde.Id = Aftale.Kunde
            WHERE Medarbejder = ?
            AND Start >= ?
            AND Stop <= ?;
            """);
        st1.setInt(1, medarbejderId);
        st1.setTimestamp(2, Timestamp.valueOf(start));
        st1.setTimestamp(3, Timestamp.valueOf(stop));
        ResultSet rs1 = st1.executeQuery();

        List<Aftale> aftaler = new ArrayList<>();

        while (rs1.next()) {
            aftaler.add(new Aftale(
                rs1.getString("Aftale.Id"),
                rs1.getTimestamp("Aftale.Start").toLocalDateTime(),
                rs1.getTimestamp("Aftale.Stop").toLocalDateTime(),
                new Kunde(rs1.getString("Kunde.Navn")),
                medarbejder,
                Fase.valueOf(rs1.getString("Fase"))
            ));
        }

        return aftaler;
    }
}
