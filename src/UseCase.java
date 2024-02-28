import java.sql.SQLException;
import java.time.LocalDateTime;

public class UseCase {
    public static void opretAftale(Database db, LocalDateTime start, LocalDateTime stop, String kunde, Fase fase) {
        Aftale aftale = new Aftale(start, stop, kunde, fase);
        try {
            db.indsaetAftale(aftale);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
