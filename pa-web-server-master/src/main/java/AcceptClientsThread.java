import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class of the thread responsible for accepting the HTTP server's clients.
 * After accepting a client, a new thread is created to serve that individual client.
 */
public class AcceptClientsThread extends Thread {

    /**
     * The port the HTTP server is going to run in.
     */
    private final int port;
    /**
     * The server's configuration, imported from the configuration file when the server started.
     */
    private final Properties serverConfig;

    /**
     * The lock responsible for the client sockets array,
     * which contains all the clients that are making requests at a given point in time.
     */
    private final ReentrantLock clientSocketsLock;
    /**
     * The server's array that contains the sockets of each request being made at a given point in time.
     */
    private final ArrayList<Socket> clientSockets;

    /**
     * The lock responsible for the opened documents array,
     * which contains a list of the documents the clients are requesting at a given point in time.
     */
    private final ReentrantLock currentlyOpenedDocumentsLock;
    /**
     * Contains a list of the documents being requested at a given point in time.
     */
    private final Set<String> currentlyOpenedDocuments;

    /**
     * The lock responsible for the requests' information array,
     * which contains a list of requests information to save to the log.
     */
    private final ReentrantLock requestsInformationLock;
    /**
     * Contains a list of the requests' information not yet saved to the log.
     */
    private final Queue<String> requestsInformation;

    /**
     * Constructor for the thread responsible for accepting the clients.
     * @param port                         The port the HTTP server is going to run in.
     * @param serverConfig                 The server's configuration, imported from the configuration file when the server started.
     * @param clientSocketsLock            The lock responsible for the client sockets array.
     * @param clientSockets                The server's array that contains the sockets of each request being made at a given point in time.
     * @param currentlyOpenedDocumentsLock The lock responsible for the opened documents array.
     * @param currentlyOpenedDocuments     Contains a list of the documents being requested at a given point in time.
     * @param requestsInformationLock      The lock responsible for the requests' information array.
     * @param requestsInformation          Contains a list of the requests' information not yet saved to the log.
     */
    public AcceptClientsThread(int port, Properties serverConfig, ReentrantLock clientSocketsLock, ArrayList<Socket> clientSockets, ReentrantLock currentlyOpenedDocumentsLock, Set<String> currentlyOpenedDocuments, ReentrantLock requestsInformationLock, Queue<String> requestsInformation){
        this.port = port;
        this.serverConfig = serverConfig;

        this.clientSocketsLock = clientSocketsLock;
        this.clientSockets = clientSockets;

        this.currentlyOpenedDocumentsLock = currentlyOpenedDocumentsLock;
        this.currentlyOpenedDocuments = currentlyOpenedDocuments;

        this.requestsInformationLock = requestsInformationLock;
        this.requestsInformation = requestsInformation;
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
            ExecutorService clientPool = Executors.newFixedThreadPool(10);
            System.out.println("Started server socket on port: " + port);
            System.out.println("Working directory: " + System.getProperty("user.dir") + "\n");

            //* Continuously accept clients, and spawn a thread to serve them
            //noinspection InfiniteLoopStatement
            while (true) {

                Socket newClientSocket = server.accept(); // Accept a client and create a socket

                clientSocketsLock.lock();
                clientSockets.add(newClientSocket); // Adds the accepted client to the clients array
                clientSocketsLock.unlock();
                System.out.println("New client accepted: " + newClientSocket.toString());
                System.out.println("Clients connected: " + clientSockets + "\n");
                Socket clientAdded = clientSockets.get(clientSockets.size() - 1);
                Runnable newClientThread = new ServeClientThread(serverConfig, clientAdded, clientSocketsLock, clientSockets, currentlyOpenedDocumentsLock, currentlyOpenedDocuments, requestsInformationLock, requestsInformation); // Create a new thread to serve the accepted client
                clientPool.execute(newClientThread); // Add the thread to serve the client to the thread pool, and execute it

            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

}
