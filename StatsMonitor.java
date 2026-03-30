import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;

public class StatsMonitor {
    private final AtomicInteger totalTasks = new AtomicInteger(0);
    private final LongAdder completedTasks = new LongAdder();
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final LongAdder totalTimeMs = new LongAdder();
    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();
    private final AtomicInteger taskDelayMs = new AtomicInteger(1500); 
    private volatile boolean paused = false;

    public void incrementTotal() { totalTasks.incrementAndGet(); }
    public void incrementCompleted() { completedTasks.increment(); }
    public void incrementActive() { activeThreads.incrementAndGet(); }
    public void decrementActive() { activeThreads.decrementAndGet(); }
    public void recordTime(long ms) { totalTimeMs.add(ms); }

    public void setDelay(int ms) { taskDelayMs.set(ms); addLog("SPEED UPDATED: " + ms + "ms"); }
    public int getDelay() { return taskDelayMs.get(); }

    public synchronized void writeToBackend(int id, String type, String result) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("backend_history.txt", true))) {
            bw.write(String.format("ID: %-4d | Type: %-12s | Result: %s\n", id, type, result));
        } catch (IOException e) { e.printStackTrace(); }
    }

    public int getTotal() { return totalTasks.get(); }
    public long getCompleted() { return completedTasks.sum(); }
    public int getActive() { return activeThreads.get(); }
    public long getAvgLatency() {
        long done = completedTasks.sum();
        return (done == 0) ? 0 : totalTimeMs.sum() / done;
    }

    public void addLog(String m) { 
        logs.add(m); 
        if (logs.size() > 10) logs.poll(); 
    }
    public String getLogsJson() { 
        return logs.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]")); 
    }

    public boolean isPaused() { return paused; }
    public void pause() { paused = true; addLog("SYSTEM PAUSED"); }
    public synchronized void resume() { paused = false; addLog("SYSTEM RESUMED"); notifyAll(); }
    public synchronized void checkWait() throws InterruptedException { while (paused) wait(); }
}