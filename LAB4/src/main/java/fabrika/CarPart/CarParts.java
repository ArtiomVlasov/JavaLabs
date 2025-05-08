package fabrika.CarPart;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class CarParts  implements Serializable {
    private static AtomicInteger counter = new AtomicInteger(0);
    private final int id;

    public CarParts() {
        this.id = counter.incrementAndGet();
    }

    public int getId(){
        return id;
    }
}
