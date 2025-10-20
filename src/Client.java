import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java Client <IP> <port>");
            System.exit(1);
        }

        final String ip = args[0];
        final int port = Integer.parseInt(args[1]);


        try {
            Socket socket = new Socket(ip, port);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            Scanner scanner = new Scanner(System.in);
            int numThreads = scanner.nextInt();


//            https://www.geeksforgeeks.org/java/thread-pools-java/
//            https://www.baeldung.com/thread-pool-java-and-guava
            input.close();
            output.close();
            socket.close();
        } catch (UnknownHostException e) {
            System.err.println("Can't connect to host " + ip);
            System.exit(1);
        }





    }
}
