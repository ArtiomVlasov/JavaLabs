package src;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class Main {


    public static void main(String[] args) {
        ReentrantLock lock  = new ReentrantLock();
        Condition condA = lock.newCondition();
        Condition condB = lock.newCondition();
        boolean turnThreadA = true;
        Thread a = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    lock.lock();
                    try {
                        while (!turnThreadA) {
                            condA.await(); // Ждем своей очереди
                        }
                        System.out.println("Message1");
                        turnThreadA = false; // Передаем ход потоку B
                        condB.signal(); // Уведомляем поток B
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } finally {
                        lock.unlock();
                    }
                }
            }
        });
        Thread b = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Message2");
            }
        });
    }
}
