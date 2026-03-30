
import java.util.concurrent.PriorityBlockingQueue;

public class WorkerThread extends Thread {
    private final PriorityBlockingQueue<Task> queue;
    private final StatsMonitor stats;
    private volatile boolean running = true;

    public WorkerThread(PriorityBlockingQueue<Task> queue, StatsMonitor stats, int id) {
        this.queue = queue;
        this.stats = stats;
        this.setName("ScaleThread-Worker-" + id);
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (running && !isInterrupted()) {
            try {
                Task task = queue.take(); // Blocks until task available
                stats.checkWait();
                
                stats.incrementActive();
                task.run();
                stats.incrementCompleted();
                stats.decrementActive();
                
                stats.addLog(getName() + " completed task " + task.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
