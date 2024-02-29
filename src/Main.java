import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    static String prompt(String spg) {
        System.out.println(spg);
        System.out.print("> ");
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }

    static Fase vaelgFase() {
        List<Valgmulighed<Fase>> faseValgmuligheder = Arrays.stream(Fase.values()).map((fase) ->
                new Valgmulighed<>(fase.toString(), () -> fase)).toList();
        Menu<Fase> faseMenu = new Menu<>(faseValgmuligheder);
        return faseMenu.aktiver();
    }

    static Medarbejder vaelgMedarbejder(Database db) throws SQLException {
        List<Medarbejder> medarbejdere = db.indlaesMedarbejdere();

        if (medarbejdere.isEmpty()) {
            System.out.println("Tilføj en medarbejder først.");
            return null;
        }

        List<Valgmulighed<Medarbejder>> valgmuligheder = medarbejdere.stream().map((medarbejder) ->
                new Valgmulighed<>(medarbejder.navn, () -> medarbejder)).toList();
        Menu<Medarbejder> medarbejderMenu = new Menu<>(valgmuligheder);
        return medarbejderMenu.aktiver();
    }

    public static void main(String[] args) throws SQLException {
        String brugernavn = prompt("Indtast brugernavn:");
        String kodeord = prompt("Indtast kodeord:");
        final Database db = new Database(brugernavn, kodeord);

        while (true) {
            List<Valgmulighed<Void>> valgmuligheder = new ArrayList<>();
            valgmuligheder.add(new Valgmulighed<>("Opret en aftale", () -> {
                Medarbejder medarbejder = vaelgMedarbejder(db);

                if (medarbejder == null) return null;

                Kunde kunde = new Kunde(prompt("Indtast kundens navn:"));
                LocalDateTime start = LocalDateTime.parse(prompt("Indtast starttidspunkt:"));
                LocalDateTime stop;

                while (true) {
                    stop = LocalDateTime.parse(prompt("Indtast stoptidspunkt:"));

                    if (stop.isAfter(start)) {
                        break;
                    }

                    System.out.println("Fejl: stoptidspunkt må ikke være tidligere end starttidspunktet.");
                }

                Fase fase = vaelgFase();
                UseCase.opretAftale(db, start, stop, kunde, medarbejder, fase);
                return null;
            }));
            valgmuligheder.add(new Valgmulighed<>("Indlæs aftaler", () -> {
                System.out.println("Ikke implementeret.");
                return null;
            }));
            valgmuligheder.add(new Valgmulighed<>("Ret en aftale", () -> {
                System.out.println("Ikke implementeret.");
                return null;
            }));
            valgmuligheder.add(new Valgmulighed<>("Tilføj ny medarbejder", () -> {
                System.out.println("Ikke implementeret.");
                return null;
            }));
            valgmuligheder.add(new Valgmulighed<>("Slet en medarbejder", () -> {
                System.out.println("Ikke implementeret.");
                return null;
            }));

            Menu<Void> menu = new Menu<>(valgmuligheder);
            menu.aktiver();
        }
    }
}