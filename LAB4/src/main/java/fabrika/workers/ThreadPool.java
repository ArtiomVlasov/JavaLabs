package fabrika.workers;
import fabrika.exeptions.InvalidThreadPoolSizeException;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static java.lang.Thread.sleep;

public class ThreadPool
{
    private List<Thread> workers;
    private final Queue<Runnable> taskQueue = new LinkedList<>();
    private boolean isRunning = true;
    private int workerCount;

    public ThreadPool(int workerCount) {setWorkerCount(workerCount); }
    public synchronized int getQueueSize() {return taskQueue.size(); }
    public int getWorkerCount() {return workerCount; }

    public void submitTask(Runnable task) {
        synchronized (this) {
            taskQueue.add(task);
            notify();
        }
    }

    public void setWorkerCount(int newCount) {
        if (newCount <= 0) throw new InvalidThreadPoolSizeException(newCount);

        synchronized (this) {
            if (workers != null) {
                for (Thread worker : workers)
                    worker.interrupt();
            }
            this.workerCount = newCount;
            workers = new LinkedList<>();

            for (int i = 0; i < workerCount; i++) {
                Thread workerThread = new Thread(() -> {
                    synchronized (ThreadPool.this) {
                        Runnable task = null;
                        while (isRunning) {
                            task = null;

                            while (taskQueue.isEmpty() && isRunning) {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                            }

                            if (!isRunning) {
                                break;
                            }

                            task = taskQueue.poll();
                        }

                        if (task != null) {
                            try {
                                task.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                workerThread.start();
                workers.add(workerThread);
            }
        }
    }

    public void shutdown() {

        synchronized (this) {
            isRunning = false;
            notify();            }
        for (Thread worker : workers)
            worker.interrupt();
    }
}