package fabrika;

import fabrika.CarPart.Accessory;
import fabrika.CarPart.BodyPart;
import fabrika.CarPart.Engine;
import fabrika.car.CarModel;
import fabrika.config.ConfigReader;
import fabrika.controller.Controller;
import fabrika.dealer.Dealer;
import fabrika.factoryGui.FactoryView;
import fabrika.storage.Storage;
import fabrika.supplier.Supplier;
import fabrika.workers.ThreadPool;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties config = ConfigReader.loadConfig();

        int bodyStorageCapacity = Integer.parseInt(config.getProperty("StorageBodySize"));
        int motorStorageCapacity = Integer.parseInt(config.getProperty("StorageMotorSize"));
        int accessoryStorageCapacity = Integer.parseInt(config.getProperty("StorageAccessorySize"));
        int carStorageCapacity = Integer.parseInt(config.getProperty("StorageAutoSize"));
        int supplierBodyDelay = Integer.parseInt(config.getProperty("SupplierBodyDelay"));
        int supplierMotorDelay = Integer.parseInt(config.getProperty("SupplierMotorDelay"));
        int supplierAccessoryDelay = Integer.parseInt(config.getProperty("SupplierAccessoryDelay"));
        int dealerDelay = Integer.parseInt(config.getProperty("DealerDelay"));
        int threadPoolSize = Integer.parseInt(config.getProperty("Workers"));
        int dealerCount = Integer.parseInt(config.getProperty("Dealers"));
//FIXME если константные значения много то возвращать распаршенный класс not necessary

        Storage<BodyPart> bodyStorage = new Storage<>(bodyStorageCapacity);
        Storage<Engine> motorStorage = new Storage<>(motorStorageCapacity);
        Storage<Accessory> accessoryStorage = new Storage<>(accessoryStorageCapacity);
        Storage<CarModel> carStorage = new Storage<>(carStorageCapacity);

        Supplier<BodyPart> bodySupplier = new Supplier<>(bodyStorage, BodyPart.class, supplierBodyDelay);
        Supplier<Engine> motorSupplier = new Supplier<>(motorStorage, Engine.class, supplierMotorDelay);
        Supplier<Accessory> accessorySupplier = new Supplier<>(accessoryStorage, Accessory.class, supplierAccessoryDelay);

        Thread bodySupplierThread = new Thread(bodySupplier);
        Thread motorSupplierThread = new Thread(motorSupplier);
        Thread accessorySupplierThread = new Thread(accessorySupplier);

        bodySupplierThread.start();
        motorSupplierThread.start();
        accessorySupplierThread.start();

        ThreadPool threadPool = new ThreadPool(threadPoolSize);

        Controller factoryController = new Controller(carStorage, bodyStorage, motorStorage, accessoryStorage, threadPool);
        Thread factoryControllerThread = new Thread(factoryController);
        factoryControllerThread.start();

        List<Dealer> dealers = new ArrayList<>();
        for (int i = 0; i < dealerCount; i++)
            dealers.add(new Dealer(carStorage, i + 1, dealerDelay, true, factoryController));

        for (Dealer dealer : dealers) {
            Thread dealerThread = new Thread(dealer);
            dealerThread.start();
        }

        SwingUtilities.invokeLater(() -> new FactoryView(bodyStorage, motorStorage, accessoryStorage, carStorage, bodySupplier, motorSupplier, accessorySupplier, dealers, threadPool, factoryController, bodySupplierThread, motorSupplierThread, accessorySupplierThread).setVisible(true));

    }
}
