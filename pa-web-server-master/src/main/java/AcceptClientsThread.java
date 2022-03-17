import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Integer.parseInt;

/**
 * The main HTTP server thread class.
 * The main HTTP thread is simply responsible for accepting the clients.
 * After accepting the clients, the main HTTP creates a new thread to serve the client.
 */
public class AcceptClientsThread extends Thread {

    /**
     * The port the HTTP server is going to run in.
     */
    private final int port; //
    /**
     * The server configurations, imported from the config file.
     */
    private final Properties serverConfig;
    /**
     * The array that saves the client sockets as they are accepted.
     */
    ArrayList<Socket> clientSockets = new ArrayList<Socket>();

    /**
     * Constructor for the main HTTP server thread
     * @param configPath path of the configuration file used by the HTTP server
     **/
    public AcceptClientsThread(String configPath) throws IOException {
        serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(configPath);
        serverConfig.load(configPathInputStream);
        port = parseInt(serverConfig.getProperty("server.port"), 10);
    }

    /**
     * Main cycle of the server, it creates the {@link ServerSocket} at the specified port. <p>
     * The server then creates a {@link ExecutorService} thread pool to continuously accept clients. <p>
     * After accepting a new client, a {@link Socket} is created,
     * and a new {@link ServeClientThread} is added to the thread pool to serve that client.
     */
    @Override
    public void run() {

        try {

            ServerSocket server = new ServerSocket(port);
            ExecutorService clientPool = Executors.newFixedThreadPool(2);
            System.out.println("Started server socket on port: " + port);
            System.out.println("Working directory: " + System.getProperty("user.dir"));

            // Continuously accept clients, and spawn a thread to serve them
            //noinspection InfiniteLoopStatement
            while (true) {

                Socket newClientSocket = server.accept(); // Accept a client and create a socket
                clientSockets.add(newClientSocket); // Adds the accepted client to the clients array
                System.out.println("\nDebug: got new client " + newClientSocket.toString());
                System.out.println("Array: " + clientSockets.toString());
                Socket clientAdded = clientSockets.get(clientSockets.size() - 1);
                Runnable newClientThread = new ServeClientThread(clientSockets, clientAdded, serverConfig); // Create a new thread to serve the accepted client
                clientPool.execute(newClientThread); // Add the thread to serve the client to the thread pool, and execute it

            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


}
