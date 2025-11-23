import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java Server <port>");
            System.exit(1);
        }

        final int port = Integer.parseInt(args[0]);

        System.out.println("Server listening on port " + port);
        System.out.println("Commands: DATE_AND_TIME (1) | UPTIME (2) | MEMORY_USE (3) | NETSTAT (4) | CURRENT_USERS (5) | RUNNING_PROCESSES (6)");

        try (final ServerSocket ss = new ServerSocket(port); final ExecutorService threadpool = Executors.newCachedThreadPool()) {
            while (true) threadpool.submit(new ServerTask(ss.accept()));
        }
    }

    private record ServerTask(Socket socket) implements Runnable {
        private String handleCmd(final String command) throws IOException, InterruptedException {
            final Process p = new ProcessBuilder("bash", "-c", command).redirectErrorStream(true).start();

            final StringBuilder output = new StringBuilder();

            try (final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) output.append(line).append("\n");
            }

            p.waitFor();
            return output.toString();
        }

        @Override
        public void run() {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); final PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
                final int cmd = Integer.parseInt(in.readLine());
                //TODO: Uncomment when done testing
//                    if (cmd == 7) {
//                        System.out.println("Shutting Down");
//                        System.exit(0);
//                    }

                out.println(handleCmd(switch (cmd) {
                    case 1 -> "date";
                    case 2 -> "uptime -p";
                    case 3 -> "free -h";
                    case 4 -> "netstat";
                    case 5 -> "who";
                    case 6 -> "ps aux";
                    default -> "echo ERROR: Input Integer 1-7";
                }));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
