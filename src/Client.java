import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.time.Instant;


public class Client {
    private record Pair<T, T1>(T first, T1 second) {
    }

    // https://www.geeksforgeeks.org/java/callable-future-java/
    private static class Task implements Callable<Pair<String, Long>> {
        private final String IP;
        private final int PORT;
        private final String message;

        Task(String IP, int PORT, String message) {
            this.PORT = PORT;
            this.IP = IP;
            this.message = message;
        }

        @Override
        public Pair<String, Long> call() throws IOException {
            try (Socket socket = new Socket(this.IP, this.PORT); BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
                long start = System.nanoTime();
                output.println(this.message);
                String outputMessage = input.readLine();
                long time = System.nanoTime() - start;
                return new Pair<>(outputMessage, time);
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

    //TODO: Fix parameter order
//    private static void writeToFile(String fileName, Long timestamp,  Pair<String, Long> result, String message, int numThreads, double averageTime) throws IOException {
//        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName))) {
//            //Timestamp, MessageID, OutputMessage, Runtime, numThreads, averageTime
//            String line = timestamp + "," + message + "," + result.first + "," + result.second + "," + numThreads + "," + averageTime + "\n";
//            out.write(line);
//        }
//    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Client <IP> <port>");
            System.exit(1);
        }

        //TODO: Handle errors for invalid ports and ip.
        final String ip = args[0];
        final int port = Integer.parseInt(args[1]);

        Scanner scanner = new Scanner(System.in);
        System.out.println("How many clients do you want to generate? (1-25)");
        int numThreads = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (numThreads < 1 || numThreads > 25) {
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

            for (int i = 0; i < numThreads; ++i) {
                Task task = new Task(ip, port, msg);
                Future<Pair<String, Long>> output = threadPool.submit(task);
                results.add(output);
            }

            for (Future<Pair<String, Long>> result : results) {
                try {
                    Pair<String, Long> res = result.get();
                    //TODO: Write to CSV file for graphing.
                    System.out.println(res.first + " " + res.second / 1e9);
                } catch (Exception e) {
                    System.err.println("Error getting result: " + e);
                }
            }

            results.clear();
            prompt();
            msg = scanner.nextLine();
            command = Integer.parseInt(msg);
        }

        threadPool.shutdown();
        scanner.close();
    }
}