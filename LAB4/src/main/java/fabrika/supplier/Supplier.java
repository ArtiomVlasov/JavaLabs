package fabrika.supplier;

import fabrika.CarPart.CarParts;
import fabrika.storage.Storage;

import java.io.Serializable;

public class Supplier<T extends CarParts> implements Runnable, Serializable {
    private  Storage<T> storage; //FIXME trancient серилизацию сделать и убрать тогда геттер и сеттер
    private final Class<T> partType;
    private int delay;
    private int suppliedCount;

    public void setDelay(int delay) {this.delay = delay; }
    public int getDelay() {return delay; }
    public int getSuppliedCount() {return suppliedCount; }

    public Supplier(Storage<T> storage, Class<T> partType, int delay) {
        this.storage = storage;
        this.partType = partType;
        this.delay = delay;
        this.suppliedCount = 0;
    }

    public void setStorage(Storage<T> storage){
        this.storage = storage;
    }

    public Storage<T> getStorage(){
        return storage;
    }

    public void setSuppliedCount(int suppliedCount){
        this.suppliedCount = suppliedCount;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                T part = partType.getDeclaredConstructor().newInstance();
                storage.addItem(part);
                suppliedCount++;
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
