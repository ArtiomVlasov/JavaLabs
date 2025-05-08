package fabrika.storage;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class Storage<T> implements Serializable {
    private int capacity;
    private Queue<T> items = new LinkedList<>();

    public Storage(int capacity) {this.capacity = capacity; }
    public int getCapacity() {return capacity; }
    public synchronized int getSize() {return items.size(); }

    public synchronized void addItem(T item) throws InterruptedException {
        while(items.size() >= capacity){
            wait();
        }
        items.add(item);
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while (items.isEmpty())
            wait();

        T item = items.poll();
        notifyAll();
        return item;
    }

    public synchronized void setCapacity(int newCapacity) {
        this.capacity = newCapacity;
        notifyAll();
    }

    public synchronized void setItems(Queue<T> items){
        this.items = items;
    }

    public synchronized Queue<T> getItems(){
        return items;
    }
}
