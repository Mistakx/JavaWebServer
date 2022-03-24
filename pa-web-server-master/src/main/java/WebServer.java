import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Integer.parseInt;

/**
 * The main class of the HTTP server.
 */
public class WebServer {

    /**
     * The port the HTTP server is going to run in.
     */
    private static int port;
    /**
     * The server's configuration, imported from the configuration file when the server started.
     */
    private static Properties serverConfig;
    /**
     * The semaphore responsible for the number of requests that can be served simultaneously.
     */
    private static Semaphore numberOfConcurrentRequests;

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
        numberOfConcurrentRequests = new Semaphore( parseInt(serverConfig.getProperty("server.maximum.requests"), 10));
    }

    /**
     * Checks if the HTML error page is properly configured in the server configurations file.
     *
     * @return <code>boolean</code>
     * <ul>
     *     <li> <strong>true -</strong> if the settings error page is properly configured.</li>
     *     <li> <strong>false -</strong> if the settings error page isn't properly configured.</li>
     * </ul>
     */
    private static boolean htmlErrorPageExists() {

        String pageNotFoundRootPath = serverConfig.getProperty("server.404.root");
        String pageNotFoundFilename = serverConfig.getProperty("server.404.page");
        String pageNotFoundFileExtension = serverConfig.getProperty("server.404.page.extension");
        String pageNotFoundFile = pageNotFoundFilename + "." + pageNotFoundFileExtension;

        File f = new File(pageNotFoundRootPath + "/" + pageNotFoundFile);

        return f.exists() && f.isFile();

    }

    /**
     * Checks if the server roots folder is properly configured in the server configurations file.
     *
     * @return <code>boolean</code>
     * <ul>
     *     <li> <strong>true -</strong> if the root folder is properly configured.</li>
     *     <li> <strong>false -</strong> if the root folder isn't properly configured.</li>
     * </ul>
     */
    private static boolean rootFolderExists() {

        String rootPath = serverConfig.getProperty("server.root");

        File f = new File(rootPath);

        return f.exists() && !f.isFile();

    }

    /**
     * The Java main method is the entry point of any java program.
     *
     * @param args Command line arguments in the form of string values.
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Settings config path not passed as argument.");
        } else {

            // Initialize serverSocket settings
            try {
                initializeSettings(args[0]);
            } catch (Exception exception) {
                System.out.println("Settings config path not found.");
            }

            if (!htmlErrorPageExists()) {
                System.out.println("Error page path is not properly configured.");
                return;
            }

            if (!rootFolderExists()) {
                System.out.println("Server root path is not properly configured.");
                return;
            }

            // Start the server
            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Started server on port: " + port);
                System.out.println("Working directory: " + System.getProperty("user.dir") + "\n");
            } catch (IOException exception) {
                System.out.println(exception.getMessage());
                return;
            }

            //* Create and start one accept clients thread, responsible for accepting the clients
            AcceptClientsThread acceptClientsThread = new AcceptClientsThread(serverSocket, serverConfig, numberOfConcurrentRequests, clientSocketsLock, clientSockets, currentlyOpenedDocumentsLock, currentlyOpenedDocuments, requestsInformationLock, requestsInformation);
            acceptClientsThread.start();
//            AcceptClientsThread acceptClientsThread1 = new AcceptClientsThread(serverSocket, serverConfig, clientSocketsLock, clientSockets, currentlyOpenedDocumentsLock, currentlyOpenedDocuments, requestsInformationLock, requestsInformation);
//            acceptClientsThread1.start();
//            AcceptClientsThread acceptClientsThread2 = new AcceptClientsThread(serverSocket, serverConfig, clientSocketsLock, clientSockets, currentlyOpenedDocumentsLock, currentlyOpenedDocuments, requestsInformationLock, requestsInformation);
//            acceptClientsThread2.start();
//            AcceptClientsThread acceptClientsThread3 = new AcceptClientsThread(serverSocket, serverConfig, clientSocketsLock, clientSockets, currentlyOpenedDocumentsLock, currentlyOpenedDocuments, requestsInformationLock, requestsInformation);
//            acceptClientsThread3.start();
//            AcceptClientsThread acceptClientsThread4 = new AcceptClientsThread(serverSocket, serverConfig, clientSocketsLock, clientSockets, currentlyOpenedDocumentsLock, currentlyOpenedDocuments, requestsInformationLock, requestsInformation);
//            acceptClientsThread4.start();

            //* Create and start one log requests thread, responsible for logging the clients' requests
            LogRequestsInformationThread logRequestsInformationThread = new LogRequestsInformationThread("./logFile.txt", requestsInformationLock, requestsInformation, logLock, 0);
            logRequestsInformationThread.start();
//            LogRequestsInformationThread logRequestsInformationThread1 = new LogRequestsInformationThread("./logFile.txt", requestsInformationLock, requestsInformation, logLock, 0);
//            logRequestsInformationThread1.start();
//            LogRequestsInformationThread logRequestsInformationThread2 = new LogRequestsInformationThread("./logFile.txt", requestsInformationLock, requestsInformation, logLock, 0);
//            logRequestsInformationThread2.start();
//            LogRequestsInformationThread logRequestsInformationThread3 = new LogRequestsInformationThread("./logFile.txt", requestsInformationLock, requestsInformation, logLock, 0);
//            logRequestsInformationThread3.start();
//            LogRequestsInformationThread logRequestsInformationThread4 = new LogRequestsInformationThread("./logFile.txt", requestsInformationLock, requestsInformation, logLock, 0);
//            logRequestsInformationThread4.start();

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
