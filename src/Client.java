import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
            try (Socket socket = new Socket(this.IP, this.PORT); DataInputStream input = new DataInputStream(socket.getInputStream()); DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
                long start = System.nanoTime();
                output.writeUTF(this.message);
                String outputMessage = input.readUTF();
                long time = System.nanoTime() - start;
                return new Pair<>(outputMessage, time);
            }
        }
    }

    static void prompt() {
        System.out.println("Enter option to send to server (1-7)");
        System.out.println("1. Data and Time");
        System.out.println("2. Uptime");
        System.out.println("3. Memory Use");
        System.out.println("4. Netstat");
        System.out.println("5. Current Users");
        System.out.println("6. Running Processes");
        System.out.println("7. Quit");
    }

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

        while (!msg.equals("quit")) {
            long batchStart = System.nanoTime();
            for (int i = 0; i < numThreads; ++i) {
                Task task = new Task(ip, port, msg);
                Future<Pair<String, Long>> output = threadPool.submit(task);
                results.add(output);
            }
            long batchDur = System.nanoTime() - batchStart;

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
        }

        threadPool.shutdown();
        scanner.close();
    }
}