import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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

    static LocalDateTime indtastTidspunkt(String spg) {
        String indtastning;

        while (true) {
            indtastning = prompt(spg);

            try {
                return LocalDateTime.parse(indtastning);
            } catch (DateTimeParseException e) {
                System.out.println("Kunne ikke parse input. Prøv igen.");
            }
        }
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
                LocalDateTime start = indtastTidspunkt("Indtast starttidspunkt:");
                LocalDateTime stop;

                while (true) {
                    stop = indtastTidspunkt("Indtast stoptidspunkt:");

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
                Medarbejder medarbejder = vaelgMedarbejder(db);

                if (medarbejder == null) return null;

                LocalDateTime start = indtastTidspunkt("Indtast starttidspunkt for søgning:");
                LocalDateTime stop = indtastTidspunkt("Indtast stoptidspunkt for søgning:");
                List<Aftale> aftaler = db.indlaesAftaler(medarbejder, start, stop);

                for (int i = 0; i < aftaler.size(); i++) {
                    System.out.println("Aftale " + (i + 1) + ")");
                    aftaler.get(i).toString()
                            .lines()
                            .forEach((linje) -> System.out.println("\t" + linje));
                }

                return null;
            }));
            valgmuligheder.add(new Valgmulighed<>("Ret en aftale", () -> {
                String uuid = prompt("Indtast UUID for aftalen:");
                Aftale aftale = db.indlaesAftale(uuid);

                if (aftale == null) {
                    System.out.println("Fejl: Aftalen med UUID `" + uuid + "` findes ikke i databasen.");
                    return null;
                }

                List<Valgmulighed<Void>> subvalgmuligheder = new ArrayList<>();
                subvalgmuligheder.add(new Valgmulighed<>("Starttidspunkt", () -> {
                    aftale.start = indtastTidspunkt("Indtast nyt starttidspunkt:");
                    return null;
                }));
                subvalgmuligheder.add(new Valgmulighed<>("Stoptidspunkt", () -> {
                    aftale.stop = indtastTidspunkt("Indtast nyt stoptidspunkt:");
                    return null;
                }));
                subvalgmuligheder.add(new Valgmulighed<>("Kunde", () -> {
                    aftale.kunde = new Kunde(prompt("Indtast nyt kundenavn:"));
                    return null;
                }));
                subvalgmuligheder.add(new Valgmulighed<>("Medarbejder", () -> {
                    Medarbejder medarbejder = vaelgMedarbejder(db);

                    if (medarbejder != null) {
                        aftale.medarbejder = medarbejder;
                    }

                    return null;
                }));
                subvalgmuligheder.add(new Valgmulighed<>("Fase", () -> {
                    aftale.fase = vaelgFase();
                    return null;
                }));
                Menu<Void> menu = new Menu<>(subvalgmuligheder);
                menu.aktiver();

                db.indsaetAftale(aftale);
                return null;
            }));
            valgmuligheder.add(new Valgmulighed<>("Tilføj ny medarbejder", () -> {
                String medarbejderNavn = prompt("Indtast medarbejders brugernavn:");
                String medarbejderKodeord = prompt("Indtast medarbejders kodeord:");
                db.indsaetMedarbejder(new Medarbejder(medarbejderNavn), medarbejderKodeord);
                return null;
            }));
            valgmuligheder.add(new Valgmulighed<>("Slet en medarbejder", () -> {
                Medarbejder medarbejder = vaelgMedarbejder(db);

                if (medarbejder == null) return null;

                db.sletMedarbejder(medarbejder);
                return null;
            }));
            valgmuligheder.add(new Valgmulighed<>("Log ud", () -> {
                System.exit(0);
                return null;
            }));

            Menu<Void> menu = new Menu<>(valgmuligheder);
            menu.aktiver();
        }
    }
}