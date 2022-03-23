import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Integer.parseInt;

/**
 * The main class of the HTTP server.
 */
public class Main {

    /**
     * The port the HTTP server is going to run in.
     */
    private static int port;
    /**
     * The server's configuration, imported from the configuration file when the server started.
     */
    private static Properties serverConfig;

    /**
     * The lock responsible for the client sockets array,
     * which contains all the clients that are making requests at a given point in time.
     */
    private static final ReentrantLock clientSocketsLock = new ReentrantLock();
    /**
     * The server's array that contains the sockets of each request being made at a given point in time.
     */
    private static final ArrayList<Socket> clientSockets = new ArrayList<>();

    /**
     * The lock responsible for the opened documents array,
     * which contains a list of the documents the clients are requesting at a given point in time.
     */
    private static final ReentrantLock currentlyOpenedDocumentsLock = new ReentrantLock();
    /**
     * Contains a list of the documents being requested at a given point in time.
     */
    private static final Set<String> currentlyOpenedDocuments = new HashSet<>();

    /**
     * The lock responsible for the requests' information array,
     * which contains a list of requests information to save to the log.
     */
    private static final ReentrantLock requestsInformationLock = new ReentrantLock();
    /**
     * Contains a list of the requests' information not yet saved to the log.
     */
    private static final Queue<String> requestsInformation = new PriorityQueue<>();
    /**
     * The lock responsible for the log document, which contains a list of requests information.
     */
    private static final ReentrantLock logLock = new ReentrantLock();

    /**
     * Constructor for the thread responsible for accepting the clients.
     *
     * @param configPath path of the configuration file used by the HTTP server.
     * @throws IOException if an I/O error occurs when creating the output stream or if the socket is not connected.
     **/
    private static void initializeSettings(String configPath) throws IOException {
        serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(configPath);
        serverConfig.load(configPathInputStream);
        port = parseInt(serverConfig.getProperty("server.port"), 10);
    }

    /**
     * The Java main method is the entry point of any java program.
     * @param args Command line arguments in the form of string values.
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Settings config path not passed as argument.");

        } else {

            // Initialize server settings
            try {
                initializeSettings(args[0]);
            } catch (Exception exception) {
                System.out.println("Settings config path not found.");
            }

            //* Create and start one accept clients thread, responsible for accepting the clients
            AcceptClientsThread acceptClientsThread; // The HTTP Server thread responsible for accepting the clients.
            acceptClientsThread = new AcceptClientsThread(port, serverConfig, clientSocketsLock, clientSockets, currentlyOpenedDocumentsLock, currentlyOpenedDocuments, requestsInformationLock, requestsInformation);
            acceptClientsThread.start();

            //* Create and start one log requests thread, responsible for logging the clients' requests
            LogRequestsInformationThread logRequestsInformationThread;
            logRequestsInformationThread = new LogRequestsInformationThread("./logFile.txt", requestsInformationLock, requestsInformation, logLock);
            logRequestsInformationThread.start();

            //* Join the started threads
            try {
                acceptClientsThread.join();
                logRequestsInformationThread.join();
            } catch (InterruptedException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }
}
