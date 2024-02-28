import java.time.LocalDateTime;
import java.util.UUID;

public class Aftale {
    public final UUID id;
    public LocalDateTime start;
    public LocalDateTime stop;
    public String kunde;
    public Fase fase;

    public Aftale(LocalDateTime start, LocalDateTime stop, String kunde, Fase fase) {
        this.id = UUID.randomUUID();
        this.start = start;
        this.stop = stop;
        this.kunde = kunde;
        this.fase = fase;
    }

    public boolean konflikterMed(Aftale anden) {
        return (anden.start.isBefore(this.stop) && anden.start.isAfter(this.start))
                || (anden.stop.isBefore(this.stop) && anden.stop.isAfter(this.start));
    }
}
