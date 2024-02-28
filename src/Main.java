import java.sql.SQLException;
import java.util.ArrayList;
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

        List<Valgmulighed> valgmuligheder = new ArrayList<>();
        valgmuligheder.add(new Valgmulighed("Opret en aftale", () -> {
            System.out.println("Ikke implementeret.");
            System.exit(1);
        }));
        valgmuligheder.add(new Valgmulighed("Indlæs aftaler", () -> {
            System.out.println("Ikke implementeret.");
            System.exit(1);
        }));
        valgmuligheder.add(new Valgmulighed("Ret en aftale", () -> {
            System.out.println("Ikke implementeret.");
            System.exit(1);
        }));
        valgmuligheder.add(new Valgmulighed("Tilføj ny medarbejder", () -> {
            System.out.println("Ikke implementeret.");
            System.exit(1);
        }));
        valgmuligheder.add(new Valgmulighed("Slet en medarbejder", () -> {
            System.out.println("Ikke implementeret.");
            System.exit(1);
        }));

        Menu menu = new Menu(valgmuligheder);
        menu.aktiver();
    }
}