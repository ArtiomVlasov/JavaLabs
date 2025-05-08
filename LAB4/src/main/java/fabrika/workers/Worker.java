package fabrika.workers;

import fabrika.CarPart.*;
import fabrika.car.CarModel;
import fabrika.storage.Storage;

import java.io.Serializable;

public class Worker implements Runnable, Serializable {
    private final Storage<BodyPart> bodyStorage;
    private final Storage<Engine> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<CarModel> carStorage;

    public Worker(Storage<BodyPart> bodyStorage, Storage<Engine> motorStorage, Storage<Accessory> accessoryStorage, Storage<CarModel> carStorage) {
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                BodyPart body = bodyStorage.take();
                Engine engine = motorStorage.take();
                Accessory accessory = accessoryStorage.take();

                CarModel car = new CarModel(engine, body, accessory);

                carStorage.addItem(car);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
