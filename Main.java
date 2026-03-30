import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.*;

public class Main {
    public static ThreadPool pool;
    public static StatsMonitor stats;

    public static void main(String[] args) throws Exception {
        stats = new StatsMonitor();
        pool = new ThreadPool(stats);
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/start", ex -> {
            stats.resume();
            new Thread(() -> {
                for (int i = 1; i <= 1000; i++) {
                    Task t;
                    int seq = i % 5;
                    if (seq == 0) t = new MathTask(i, Task.Priority.MEDIUM);
                    else if (seq == 1) t = new PrimeTask(i, Task.Priority.MEDIUM);
                    else if (seq == 2) t = new StringTask(i, Task.Priority.MEDIUM);
                    else if (seq == 3) t = new FiboTask(i, Task.Priority.MEDIUM);
                    else t = new FileIOTask(i, Task.Priority.MEDIUM);
                    pool.submit(t);
                }
            }).start();
            send(ex, "Started".getBytes(), "text/plain");
        });

        server.createContext("/addTask", ex -> {
            pool.submit(new FiboTask(999, Task.Priority.CRITICAL));
            stats.addLog("!!! PRIORITY INJECTED !!!");
            send(ex, "OK".getBytes(), "text/plain");
        });

        server.createContext("/setSpeed", ex -> {
            String q = ex.getRequestURI().getQuery();
            if (q != null && q.contains("ms=")) {
                stats.setDelay(Integer.parseInt(q.split("ms=")[1]));
            }
            send(ex, "OK".getBytes(), "text/plain");
        });

        server.createContext("/stats", ex -> {
            String json = String.format("{\"total\":%d,\"completed\":%d,\"active\":%d,\"queue\":%d,\"latency\":%d,\"logs\":%s,\"paused\":%b}",
                stats.getTotal(), stats.getCompleted(), stats.getActive(), pool.getQueueSize(), stats.getAvgLatency(), stats.getLogsJson(), stats.isPaused());
            send(ex, json.getBytes(), "application/json");
        });

        server.createContext("/pause", ex -> { stats.pause(); send(ex, "P".getBytes(), "text/plain"); });
        server.createContext("/resume", ex -> { stats.resume(); send(ex, "R".getBytes(), "text/plain"); });

        server.createContext("/", ex -> {
            InputStream is = Main.class.getResourceAsStream("dashboard.html");
            byte[] res = (is != null) ? is.readAllBytes() : "404".getBytes();
            send(ex, res, "text/html");
        });

        server.start();
        System.out.println("Running: http://localhost:8080");
    }

    private static void send(com.sun.net.httpserver.HttpExchange ex, byte[] d, String t) throws IOException {
        ex.getResponseHeaders().add("Content-Type", t);
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, d.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(d); }
    }
}