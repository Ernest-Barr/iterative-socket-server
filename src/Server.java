import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

public class Server {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        int port = (args.length >= 1) ? Integer.parseInt(args[0]) : 1337;
        Instant serverStart = Instant.now();
        System.out.println("Server (iterative) listening on port " + port);
        System.out.println("Commands: DATE_AND_TIME | UPTIME | MEMORY_USE | NETSTAT | CURRENT_USERS | RUNNING_PROCESSES");

        try (ServerSocket ss = new ServerSocket(port)) {
            while (true) {
                try (Socket s = ss.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

                    String line = in.readLine();
//                    String command = normalize(line);
                    int command = Integer.parseInt(line);
                    String reply = switch (command) {
                        case 1 -> dateTime();
                        case 2 -> uptime(serverStart);
                        case 3 -> memoryUse();
                        case 4 -> netstat();
                        case 5 -> currentUsers();
                        case 6 -> runningProcesses();
                        case 7 -> "Closing Server";
                        default ->
                                "ERROR: Use one of: DATE_AND_TIME, UPTIME, MEMORY_USE, NETSTAT, CURRENT_USERS, RUNNING_PROCESSES";
                    };
//                    switch (command) {
//                        case "DATE_AND_TIME":
//                            reply = dateTime();
//                            break;
//                        case "UPTIME":
//                            reply = uptime(serverStart);
//                            break;
//                        case "MEMORY_USE":
//                            reply = memoryUse();
//                            break;
//                        case "NETSTAT":
//                            reply = netstat();
//                            break;
//                        case "CURRENT_USERS":
//                            reply = currentUsers();
//                            break;
//                        case "RUNNING_PROCESSES":
//                            reply = runningProcesses();
//                            break;
//                        default:
//                            reply = "ERROR: Use one of: DATE_AND_TIME, UPTIME, MEMORY_USE, NETSTAT, CURRENT_USERS, RUNNING_PROCESSES";
//                    }

                    out.println(reply);
                    if (command == 7) System.exit(0);

                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Bind failed: " + e.getMessage());
        }
    }

    private static String dateTime() {
        return "Date and Time: " + LocalDateTime.now().format(FMT);
    }

    private static String uptime(Instant start) {
        Duration d = Duration.between(start, Instant.now());
        long days = d.toDays();
        long hrs = d.minusDays(days).toHours();
        long mins = d.minusDays(days).minusHours(hrs).toMinutes();
        long secs = d.getSeconds() % 60;
        return String.format("%s %dd %dh %dm %ds", "Uptime:", days, hrs, mins, secs);
    }

    private static String memoryUse() {
        Runtime rt = Runtime.getRuntime();
        long mb = 1024L * 1024L;
        long used = (rt.totalMemory() - rt.freeMemory()) / mb;
        long total = rt.totalMemory() / mb;
        long max = rt.maxMemory() / mb;
        return String.format(Locale.ROOT, "Memory Use: used=%dMB total=%dMB max=%dMB", used, total, max);
    }

    private static String netstat() {
        String out = runFirstLine(new String[]{"netstat", "-an"});
        if (isBlank(out)) out = runFirstLine(new String[]{"ss", "-tuan"});
        return !isBlank(out) ? "Netstat: " + out : "Netstat: unavailable";
    }

    private static String currentUsers() {
        String out = runFirstLine(new String[]{"who"});
        return !isBlank(out) ? "Current Users: " + out : "Current Users: none";
    }

    private static String runningProcesses() {
        String cnt = runFirstLine(new String[]{"sh", "-c", "ps -e | wc -l"});
        return "Running Processes: " + (isBlank(cnt) ? "unknown" : cnt.trim());
    }

    private static String normalize(String s) {
        if (s == null) return "";
        s = s.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if (s.equals("DATETIME") || s.equals("DATE_TIME")) s = "DATE_AND_TIME";
        if (s.equals("MEMORY")) s = "MEMORY_USE";
        if (s.equals("USERS")) s = "CURRENT_USERS";
        if (s.equals("PROCESSES") || s.equals("PS")) s = "RUNNING_PROCESSES";
        return s;
    }

    private static String runFirstLine(String[] cmd) {
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) return line;
                }
            }
            p.waitFor();
        } catch (Exception ignored) {
        }
        return "";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
