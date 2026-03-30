abstract class Task implements Runnable {
    protected int id;
    protected String result;
    protected Priority priority;
    protected long startTime;
    protected long executionTime;

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public Task(int id, Priority p) { this.id = id; this.priority = p; }
    public int getId() { return id; }
    public String getResult() { return result; }
    public Priority getPriority() { return priority; }
    public long getExecutionTime() { return executionTime; }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        try { execute(); } catch (Exception e) { result = "Error"; }
        executionTime = System.currentTimeMillis() - startTime;
    }
    public abstract void execute() throws Exception;
}

class MathTask extends Task {
    public MathTask(int i, Priority p) { super(i, p); }
    @Override public void execute() throws Exception { this.result = id + "^2 = " + ((long)id*id); Thread.sleep(Main.stats.getDelay()); }
}
class PrimeTask extends Task {
    public PrimeTask(int i, Priority p) { super(i, p); }
    @Override public void execute() throws Exception { this.result = "Prime check: " + (id+7); Thread.sleep(Main.stats.getDelay()); }
}
class StringTask extends Task {
    public StringTask(int i, Priority p) { super(i, p); }
    @Override public void execute() throws Exception { this.result = "String_" + id + " Rev"; Thread.sleep(Main.stats.getDelay()); }
}
class FiboTask extends Task {
    public FiboTask(int i, Priority p) { super(i, p); }
    @Override public void execute() throws Exception { this.result = "Fibonacci sequence " + id; Thread.sleep(Main.stats.getDelay()); }
}
class FileIOTask extends Task {
    public FileIOTask(int i, Priority p) { super(i, p); }
    @Override public void execute() throws Exception { this.result = "Sector " + id + " Verified"; Thread.sleep(Main.stats.getDelay()); }
}