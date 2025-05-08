package fabrika.factoryGui;

import fabrika.CarPart.Accessory;
import fabrika.CarPart.BodyPart;
import fabrika.CarPart.Engine;
import fabrika.car.CarModel;
import fabrika.controller.Controller;
import fabrika.dealer.Dealer;
import fabrika.storage.Storage;
import fabrika.supplier.Supplier;
import fabrika.workers.ThreadPool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class FactoryView extends JFrame {
    private Storage<BodyPart> bodyStorage;
    private Storage<Engine> motorStorage;
    private Storage<Accessory> accessoryStorage;
    private Storage<CarModel> carStorage;
    private Supplier<BodyPart> bodySupplier;
    private Supplier<Engine> motorSupplier;
    private Supplier<Accessory> accessorySupplier;
    private transient List<Dealer> dealers;
    private transient ThreadPool threadPool;
    private transient Thread bodySupplierThread;
    private transient Thread motorSupplierThread;
    private transient Thread accessorySupplierThread;


    private final JLabel bodyCountLabel;
    private final JLabel motorCountLabel;
    private final JLabel accessoryCountLabel;
    private final JLabel carCountLabel;
    private final JLabel soldCarsLabel;
    private final JLabel queueSizeLabel;
    private final JLabel bodySuppliedLabel;
    private final JLabel motorSuppliedLabel;
    private final JLabel accessorySuppliedLabel;

    private final JSlider bodySpeedSlider;
    private final JSlider motorSpeedSlider;
    private final JSlider accessorySpeedSlider;
    private final JSlider dealerSpeedSlider;
    private final JTextField workerCountField;

    public FactoryView(Storage<BodyPart> bodyStorage, Storage<Engine> motorStorage, Storage<Accessory> accessoryStorage,
                       Storage<CarModel> carStorage, Supplier<BodyPart> bodySupplier, Supplier<Engine> motorSupplier,
                       Supplier<Accessory> accessorySupplier, List<Dealer> dealers, ThreadPool threadPool,
                       Controller factoryController, Thread bodyThread, Thread motorThread, Thread accessoryThread) {
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
        this.bodySupplier = bodySupplier;
        this.motorSupplier = motorSupplier;
        this.accessorySupplier = accessorySupplier;
        this.dealers = dealers;
        this.threadPool = threadPool;
        this.bodySupplierThread = bodyThread;
        this.motorSupplierThread = motorThread;
        this.accessorySupplierThread = accessoryThread;

        setTitle("Factory Simulator");
        setSize(700, 450);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel suppliersPanel = new JPanel(new GridLayout(3, 3));
        suppliersPanel.setBorder(BorderFactory.createTitledBorder("Suppliers"));

        bodySpeedSlider = createSlider(bodySupplier.getDelay());
        motorSpeedSlider = createSlider(motorSupplier.getDelay());
        accessorySpeedSlider = createSlider(accessorySupplier.getDelay());

        bodySpeedSlider.addChangeListener(e -> bodySupplier.setDelay(bodySpeedSlider.getValue()));
        motorSpeedSlider.addChangeListener(e -> motorSupplier.setDelay(motorSpeedSlider.getValue()));
        accessorySpeedSlider.addChangeListener(e -> accessorySupplier.setDelay(accessorySpeedSlider.getValue()));

        bodySuppliedLabel = new JLabel("Supplied: 0");
        motorSuppliedLabel = new JLabel("Supplied: 0");
        accessorySuppliedLabel = new JLabel("Supplied: 0");

        suppliersPanel.add(new JLabel("Body Supplier Speed (ms):"));
        suppliersPanel.add(bodySpeedSlider);
        suppliersPanel.add(bodySuppliedLabel);

        suppliersPanel.add(new JLabel("Motor Supplier Speed (ms):"));
        suppliersPanel.add(motorSpeedSlider);
        suppliersPanel.add(motorSuppliedLabel);

        suppliersPanel.add(new JLabel("Accessory Supplier Speed (ms):"));
        suppliersPanel.add(accessorySpeedSlider);
        suppliersPanel.add(accessorySuppliedLabel);

        JPanel storagesPanel = new JPanel(new GridLayout(3, 5));
        storagesPanel.setBorder(BorderFactory.createTitledBorder("Storages"));

        storagesPanel.add(new JLabel(""));
        storagesPanel.add(new JLabel("Bodies"));
        storagesPanel.add(new JLabel("Motors"));
        storagesPanel.add(new JLabel("Accessories"));
        storagesPanel.add(new JLabel("Cars"));

        storagesPanel.add(new JLabel("Capacity:"));
        JTextField bodyCapacityField = createCapacityField(bodyStorage);
        JTextField motorCapacityField = createCapacityField(motorStorage);
        JTextField accessoryCapacityField = createCapacityField(accessoryStorage);
        JTextField carCapacityField = createCapacityField(carStorage);

        storagesPanel.add(bodyCapacityField);
        storagesPanel.add(motorCapacityField);
        storagesPanel.add(accessoryCapacityField);
        storagesPanel.add(carCapacityField);

        storagesPanel.add(new JLabel("Stored:"));
        bodyCountLabel = new JLabel();
        motorCountLabel = new JLabel();
        accessoryCountLabel = new JLabel();
        carCountLabel = new JLabel();

        storagesPanel.add(bodyCountLabel);
        storagesPanel.add(motorCountLabel);
        storagesPanel.add(accessoryCountLabel);
        storagesPanel.add(carCountLabel);

        JPanel workersPanel = new JPanel();
        workersPanel.setBorder(BorderFactory.createTitledBorder("Workers"));

        workerCountField = new JTextField(String.valueOf(threadPool.getWorkerCount()), 5);
        workerCountField.addActionListener(e -> threadPool.setWorkerCount(Integer.parseInt(workerCountField.getText())));

        workersPanel.add(new JLabel("Number of Workers:"));
        workersPanel.add(workerCountField);

        JPanel dealersPanel = new JPanel();
        dealersPanel.setLayout(new BoxLayout(dealersPanel, BoxLayout.X_AXIS));
        dealersPanel.setBorder(BorderFactory.createTitledBorder("Dealers"));

        soldCarsLabel = new JLabel();
        queueSizeLabel = new JLabel();

        dealerSpeedSlider = createSlider(dealers.get(0).getDelay());
        dealerSpeedSlider.addChangeListener(e -> {
            for (Dealer dealer : dealers) {
                dealer.setDelay(dealerSpeedSlider.getValue());
            }
        });

        JPanel queuePanel = new JPanel(new GridLayout(1, 2));
        queuePanel.add(new JLabel("Sold Cars:"));
        queuePanel.add(soldCarsLabel);

        dealersPanel.add(queuePanel);

        dealersPanel.add(new JLabel("Dealer Speed (ms):"));
        dealersPanel.add(dealerSpeedSlider);

        add(suppliersPanel, BorderLayout.NORTH);
        add(storagesPanel, BorderLayout.EAST);
        add(workersPanel, BorderLayout.WEST);
        add(dealersPanel, BorderLayout.SOUTH);

        JPanel serializationPanel = new JPanel(new FlowLayout());
        serializationPanel.setBorder(BorderFactory.createTitledBorder("Serialization Controls"));

        JButton serializeButton = new JButton("Serialize");
        JButton deserializeButton = new JButton("Deserialize");

        serializationPanel.add(serializeButton);
        serializationPanel.add(deserializeButton);

        serializeButton.addActionListener(e -> serializeToFile("factory_gui.ser"));
        deserializeButton.addActionListener(e -> deserializeFromFile("factory_gui.ser"));

        add(serializationPanel, BorderLayout.CENTER);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                factoryController.stop();
                System.out.println("Exiting...");
                System.exit(0);
            }
        });

        new Timer(500, e -> updateLabels()).start();
    }

    private JSlider createSlider(int initialValue) {
        JSlider slider = new JSlider(0, 5000, initialValue);
        slider.setMajorTickSpacing(1000);
        slider.setMinorTickSpacing(500);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = 0; i <= 5000; i += 1000)
            labelTable.put(i, new JLabel(String.valueOf(i)));

        slider.setLabelTable(labelTable);

        return slider;
    }

    private JTextField createCapacityField(Storage<?> storage) {
        JTextField field = new JTextField(String.valueOf(storage.getCapacity()), 5);
        field.addActionListener(e -> storage.setCapacity(Integer.parseInt(field.getText())));
        return field;
    }

    private void updateLabels() {
        bodyCountLabel.setText(String.valueOf(bodyStorage.getSize()));
        motorCountLabel.setText(String.valueOf(motorStorage.getSize()));
        accessoryCountLabel.setText(String.valueOf(accessoryStorage.getSize()));
        carCountLabel.setText(String.valueOf(carStorage.getSize()));

        int totalSoldCars = dealers.stream().mapToInt(Dealer::getSoldCarsCount).sum();
        soldCarsLabel.setText(String.valueOf(totalSoldCars));
        queueSizeLabel.setText(String.valueOf(threadPool.getQueueSize()));

        bodySuppliedLabel.setText("Supplied: " + bodySupplier.getSuppliedCount());
        motorSuppliedLabel.setText("Supplied: " + motorSupplier.getSuppliedCount());
        accessorySuppliedLabel.setText("Supplied: " + accessorySupplier.getSuppliedCount());
    }

    public void serializeToFile(String filePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(this);
            System.out.println("GUI serialized successfully!");
        } catch (IOException e) {
            System.err.println("Serialization failed: " + e.getMessage());
        }
    }

    public void deserializeFromFile(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            FactoryView restoredView = (FactoryView) in.readObject();
            this.bodyStorage.setCapacity(restoredView.bodyStorage.getCapacity());  // Обновляем только capacity
            this.bodyStorage.setItems(new LinkedList<>(restoredView.bodyStorage.getItems()));  // Обновляем items

            this.motorStorage.setCapacity(restoredView.motorStorage.getCapacity());
            this.motorStorage.setItems(new LinkedList<>(restoredView.motorStorage.getItems()));

            this.accessoryStorage.setCapacity(restoredView.accessoryStorage.getCapacity());
            this.accessoryStorage.setItems(new LinkedList<>(restoredView.accessoryStorage.getItems()));

            this.carStorage.setCapacity(restoredView.carStorage.getCapacity());
            this.carStorage.setItems(new LinkedList<>(restoredView.carStorage.getItems()));

            this.bodySupplier.setDelay(restoredView.bodySupplier.getDelay());
            this.bodySupplier.setStorage(restoredView.bodySupplier.getStorage());
            this.bodySupplier.setSuppliedCount(restoredView.bodySupplier.getSuppliedCount());

            this.motorSupplier.setDelay(restoredView.motorSupplier.getDelay());
            this.motorSupplier.setStorage(restoredView.motorSupplier.getStorage());
            this.motorSupplier.setSuppliedCount(restoredView.motorSupplier.getSuppliedCount());

            this.accessorySupplier.setDelay(restoredView.accessorySupplier.getDelay());
            this.accessorySupplier.setStorage(restoredView.accessorySupplier.getStorage());
            this.accessorySupplier.setSuppliedCount(restoredView.accessorySupplier.getSuppliedCount());

            System.out.println("GUI deserialized successfully!");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Deserialization failed: " + e.getMessage());
        }
    }

    public void stopSuppliers() {
        if (bodySupplierThread != null && bodySupplierThread.isAlive()) {
            bodySupplierThread.interrupt();
        }
        if (motorSupplierThread != null && motorSupplierThread.isAlive()) {
            motorSupplierThread.interrupt();
        }
        if (accessorySupplierThread != null && accessorySupplierThread.isAlive()) {
            accessorySupplierThread.interrupt();
        }
    }
    public void recreateSupplierThreads() {
        stopSuppliers();

        bodySupplierThread = new Thread(bodySupplier);
        motorSupplierThread = new Thread(motorSupplier);
        accessorySupplierThread = new Thread(accessorySupplier);

        bodySupplierThread.start();
        motorSupplierThread.start();
        accessorySupplierThread.start();
    }
}
