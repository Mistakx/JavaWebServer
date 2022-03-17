import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Integer.parseInt;

/**
 * The main HTTP server thread class.
 * The main HTTP thread is simply responsible for accepting the clients.
 * After accepting the clients, the main HTTP creates a new thread to serve the client.
 */
public class MainHTTPServerThread extends Thread {

    /**
     * The port the HTTP server is going to run in.
     */
    private final int port; //
    /**
     * The server configurations, imported from the config file.
     */
    private final Properties serverConfig;

    /**
     * Constructor for the main HTTP server thread
     * @param configPath path of the configuration file used by the HTTP server
     **/
    public MainHTTPServerThread(String configPath) throws IOException {
        serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(configPath);
        serverConfig.load(configPathInputStream);
        port = parseInt(serverConfig.getProperty("server.port"), 10);
    }


    /**
     * <b>Completar pelos alunos..</b>
     * <p>
     * Main cycle of the server, it creates the {@link ServerSocket} at the specified port, and then it creates a new {@link Socket}
     * for each new request
     * <p>
     * To refactor with:
     * <ul>
     *     <li>loading the server configurations from the server.config file</li>
     *     <li>Introduce parallelism to handle the requests</li>
     *     <li>Introduce parallelism to handle the documents</li>
     *     <li>Parse the request according as necessary for the implementation</li>
     *     <li>...</li>
     * </ul>
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

                Socket newClient = server.accept();
                System.out.println("\nDebug: got new client " + newClient.toString());
                Runnable r1 = new serveClientThread(newClient, serverConfig);
                clientPool.execute(r1);

            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


}
