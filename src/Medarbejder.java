public class Medarbejder {
    public String navn;

    Medarbejder(String navn) {
        this.navn = navn;
    }

    @Override
    public String toString() {
        return this.navn;
    }
}