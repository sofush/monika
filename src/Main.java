public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
        String brugernavn = prompt("Indtast brugernavn:");
        String kodeord = prompt("Indtast kodeord:");
        final Database db = new Database(brugernavn, kodeord);
    }
}