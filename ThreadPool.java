import java.util.concurrent.*;

public class ThreadPool {
    private final PriorityBlockingQueue<Task> queue;
    private final StatsMonitor stats;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ThreadPool(StatsMonitor stats) {
        this.stats = stats;
        this.queue = new PriorityBlockingQueue<>(100, (a, b) -> b.getPriority().compareTo(a.getPriority()));
        startDispatcher();
    }

    private void startDispatcher() {
        Thread d = new Thread(() -> {
            while (true) {
                try {
                    stats.checkWait(); 
                    Task t = queue.take();
                    stats.checkWait(); 

                    executor.submit(() -> {
                        try {
                            stats.incrementActive();
                            t.run();
                            stats.writeToBackend(t.getId(), t.getClass().getSimpleName(), t.getResult());
                        } finally {
                            stats.recordTime(t.getExecutionTime());
                            stats.incrementCompleted();
                            stats.decrementActive();
                            stats.addLog("[" + t.getClass().getSimpleName() + " #" + t.getId() + "] Done");
                        }
                    }).get(); 

                } catch (Exception e) { break; }
            }
        });
        d.setDaemon(true);
        d.start();
    }

    public void submit(Task t) { stats.incrementTotal(); queue.offer(t); }
    public int getQueueSize() { return queue.size(); }
}