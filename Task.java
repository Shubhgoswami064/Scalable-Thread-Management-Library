public abstract class Task implements Runnable {
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    protected final int id;
    protected final Priority priority;
    protected long executionTime = 0;
    protected String result = ""; 

    public Task(int id, Priority priority) {
        this.id = id;
        this.priority = priority;
    }

    public abstract void execute() throws Exception;

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        try {
            execute();
        } catch (Exception e) {
            this.result = "Error: " + e.getMessage();
        } finally {
            this.executionTime = System.currentTimeMillis() - start;
        }
    }

    public String getResult() { return result; }
    public int getId() { return id; }
    public Priority getPriority() { return priority; }
    public long getExecutionTime() { return executionTime; }
}