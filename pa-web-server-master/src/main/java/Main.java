import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The main class of the HTTP server.
 */
public class Main {

    /**
     * The Java main method is the entry point of any java program.
     *
     * @param args command line arguments in the form of string values.
     */
    public static void main(String[] args) {

        AcceptClientsThread acceptClientsThread; // The HTTP Server thread responsible for accepting the clients.

        //* Create the main server thread, responsible for accepting the clients
        try {
            acceptClientsThread = new AcceptClientsThread("/media/shared/PA/JavaWebServer/pa-web-server-master/server/server.config");
        } catch (IOException configFileException) {
            System.out.println(configFileException.getMessage());
            return;
        }

        //* Start the main server thread
        acceptClientsThread.start();

        //* Join the main server thread
        try {
            acceptClientsThread.join();
        } catch (InterruptedException exception) {
            System.out.println(exception.getMessage());
        }
    }

}
