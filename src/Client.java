import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client {

    private record Pair<T, T1>(T first, T1 second) {
    }

    // https://www.geeksforgeeks.org/java/callable-future-java/
    private record Task(String IP, int PORT, String message) implements Callable<Pair<String, Long>> {
        @Override
        public Pair<String, Long> call() throws IOException {
            try (final Socket socket = new Socket(this.IP, this.PORT); final BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); final PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
                final long start = System.nanoTime();
                output.println(this.message);

                StringBuilder outputMessage = new StringBuilder();

                String line;

                while ((line = input.readLine()) != null) outputMessage.append(line).append("\n");

                final long time = System.nanoTime() - start;
                return new Pair<>(outputMessage.toString(), time);
            }
        }
    }

    private static void prompt() {
        System.out.println("Enter option to send to server (1-7)");
        System.out.println("1. Data and Time");
        System.out.println("2. Uptime");
        System.out.println("3. Memory Use");
        System.out.println("4. Netstat");
        System.out.println("5. Current Users");
        System.out.println("6. Running Processes");
        System.out.println("7. Quit");
    }

    private static void writeToFile(final String fileName, final Long timestamp, final int messageId, final int numThreads, final double averageTime) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true))) {
            //timestamp, command, numThreads, averageTime
            out.write(timestamp + "," + messageId + "," + numThreads + "," + averageTime + "\n");
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java Client <IP> <port>");
            System.exit(1);
        }

        //TODO: Handle errors for invalid ports and ip.
        final String ip = args[0];
        final int port = Integer.parseInt(args[1]);

        Scanner scanner = new Scanner(System.in);
        System.out.println("How many clients do you want to generate? (1-100)");
        int numThreads = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (numThreads < 1 || numThreads > 100) {
            System.err.println("Invalid number of clients. Please try again.");
            System.exit(1);
        }

        //            https://www.geeksforgeeks.org/java/thread-pools-java/
        //            https://www.baeldung.com/thread-pool-java-and-guava
        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        ArrayList<Future<Pair<String, Long>>> results = new ArrayList<>();

        prompt();
        String msg = scanner.nextLine();
        int command = Integer.parseInt(msg);

        while (true) {
            if (command < 1 || command > 7) {
                System.err.println("Invalid command. Please try again.");
                msg = scanner.nextLine();
                command = Integer.parseInt(msg);
                continue;
            }

            if (command == 7) {
                System.out.println("Closing server and client");
                threadPool.submit(new Task(ip, port, msg));
                break;
            }

            final Instant time = Instant.now();

            for (int i = 0; i < numThreads; ++i) {
                Task task = new Task(ip, port, msg);
                Future<Pair<String, Long>> output = threadPool.submit(task);
                results.add(output);
            }

            double totalTime = 0.0;

            for (Future<Pair<String, Long>> result : results) {
                try {
                    Pair<String, Long> res = result.get();
                    totalTime += res.second();
                    System.out.println(res.first + " Runtime: " + res.second / 1e9);
                } catch (Exception e) {
                    System.err.println("Error getting result: " + e);
                }
            }

            //timestamp, command, numThreads, averageTime
            writeToFile("data/res.csv", time.toEpochMilli(), command, numThreads, (totalTime / numThreads) / 1e9);

            results.clear();
            prompt();
            msg = scanner.nextLine();
            command = Integer.parseInt(msg);
        }

        threadPool.shutdown();
        scanner.close();
        System.exit(0);
    }
}
