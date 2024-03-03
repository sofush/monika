public class Kunde {
    public String navn;

    Kunde(String navn) {
        this.navn = navn;
    }

    @Override
    public String toString() {
        return this.navn;
    }
}