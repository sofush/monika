import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

class IndeksGenerator {
    private int taeller = 0;
    private int laengde = 1;

    public String naeste() {
        final int BASE = 26;
        StringBuilder sb = new StringBuilder();
        int tal = this.taeller;

        // Ekstraherer et enkelt base-26 ciffer fra tæller-decimaltallet, som så
        // derefter omdannes til et bogstav fra alfabetet (kun bogstaverne a til z)
        for (int i = 0; i < this.laengde; i++) {
            int n = tal % BASE;
            tal /= BASE;
            sb.appendCodePoint('a' + n);
        }

        this.taeller += 1;

        if (this.taeller == Math.pow(BASE, this.laengde)) {
            this.laengde += 1;
        }

        return sb.reverse().toString();
    }
}

public class Menu {
    private final List<Valgmulighed> valgmuligheder;

    public Menu(List<Valgmulighed> valgmuligheder) {
        this.valgmuligheder = valgmuligheder;
    }

    public void aktiver() {
        System.out.println("Vælg en af valgmulighederne:");
        IndeksGenerator gen = new IndeksGenerator();
        List<String> indekser = new ArrayList<>();

        for (Valgmulighed valgmulighed : this.valgmuligheder) {
            String indeks = gen.naeste();
            indekser.add(indeks);
            System.out.println(indeks + ") " + valgmulighed.beskrivelse());
        }

        System.out.print("> ");
        Scanner scanner = new Scanner(System.in);
        String indtastning = scanner.nextLine().toLowerCase();
        int i = indekser.indexOf(indtastning);

        if (i == -1) {
            Optional<Valgmulighed> valgmulighed = this.valgmuligheder.stream().filter((v) ->
                    v.beskrivelse().equalsIgnoreCase(indtastning)).findFirst();

            if (valgmulighed.isPresent()) {
                i = this.valgmuligheder.indexOf(valgmulighed.get());
            } else {
                // Prøv igen.
                aktiver();
            }
        }

        this.valgmuligheder.get(i).aktiver();
    }
}
