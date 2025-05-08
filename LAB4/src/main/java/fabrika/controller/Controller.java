package fabrika.controller;

import fabrika.workers.Worker;
import fabrika.CarPart.*;
import fabrika.car.CarModel;
import fabrika.storage.Storage;
import fabrika.workers.ThreadPool;

import java.io.Serializable;

public class Controller implements Runnable, Serializable {
    private final Storage<CarModel> carStorage;
    private final Storage<BodyPart> bodyStorage;
    private final Storage<Engine> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final ThreadPool threadPool;

    public Controller(Storage<CarModel> carStorage, Storage<BodyPart> bodyStorage, Storage<Engine> motorStorage, Storage<Accessory> accessoryStorage, ThreadPool threadPool) {
        this.carStorage = carStorage;
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        int target = carStorage.getCapacity()/2;
        try {
            for (int i = 0; i < carStorage.getCapacity(); i++)
                threadPool.submitTask(new Worker(bodyStorage, motorStorage, accessoryStorage, carStorage));

            while (!Thread.currentThread().isInterrupted()) {
                synchronized (this) {wait(); }
                synchronized (carStorage) {
                    if (carStorage.getSize() <= target)
                        threadPool.submitTask(new Worker(bodyStorage, motorStorage, accessoryStorage, carStorage));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        System.out.println("Stopping the factory...");
        threadPool.shutdown();
        System.out.println("Factory stopped.");
    }

    public synchronized void notifySale() {notify(); }
}
