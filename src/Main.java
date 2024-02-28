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

    public static void main(String[] args) throws SQLException {
        String brugernavn = prompt("Indtast brugernavn:");
        String kodeord = prompt("Indtast kodeord:");
        final Database db = new Database(brugernavn, kodeord);

        List<Valgmulighed<Void>> valgmuligheder = new ArrayList<>();
        valgmuligheder.add(new Valgmulighed<>("Opret en aftale", () -> {
            String kunde = prompt("Indtast kundens navn:");
            LocalDateTime start = LocalDateTime.parse(prompt("Indtast starttidspunkt:"));
            LocalDateTime stop;

            while (true) {
                stop = LocalDateTime.parse(prompt("Indtast stoptidspunkt:"));

                if (stop.isAfter(start)) {
                    break;
                }

                System.out.println("Fejl: stoptidspunkt må ikke være tidligere end starttidspunktet.");
            }

            List<Valgmulighed<Fase>> faseValgmuligheder = Arrays.stream(Fase.values()).map((fase) ->
                    new Valgmulighed<>(fase.toString(), () -> fase)).toList();
            Menu<Fase> faseMenu = new Menu<>(faseValgmuligheder);
            Fase fase = faseMenu.aktiver();

            UseCase.opretAftale(db, start, stop, kunde, fase);
            return null;
        }));
        valgmuligheder.add(new Valgmulighed<>("Indlæs aftaler", () -> {
            System.out.println("Ikke implementeret.");
            System.exit(1);
            return null;
        }));
        valgmuligheder.add(new Valgmulighed<>("Ret en aftale", () -> {
            System.out.println("Ikke implementeret.");
            System.exit(1);
            return null;
        }));
        valgmuligheder.add(new Valgmulighed<>("Tilføj ny medarbejder", () -> {
            System.out.println("Ikke implementeret.");
            System.exit(1);
            return null;
        }));
        valgmuligheder.add(new Valgmulighed<>("Slet en medarbejder", () -> {
            System.out.println("Ikke implementeret.");
            System.exit(1);
            return null;
        }));

        Menu<Void> menu = new Menu<>(valgmuligheder);
        menu.aktiver();
    }
}