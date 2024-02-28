import java.util.concurrent.Callable;

public class Valgmulighed<T> {
    private final String beskrivelse;
    private final Callable<T> funktion;

    public Valgmulighed(String beskrivelse, Callable<T> funktion) {
        this.beskrivelse = beskrivelse;
        this.funktion = funktion;
    }

    public String beskrivelse() {
        return this.beskrivelse;
    }

    public T aktiver() {
        try {
            return this.funktion.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
