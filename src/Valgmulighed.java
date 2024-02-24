public class Valgmulighed {
    private final String beskrivelse;
    private final Runnable funktion;

    public Valgmulighed(String beskrivelse, Runnable funktion) {
        this.beskrivelse = beskrivelse;
        this.funktion = funktion;
    }

    public String beskrivelse() {
        return this.beskrivelse;
    }

    public void aktiver() {
        this.funktion.run();
    }
}
