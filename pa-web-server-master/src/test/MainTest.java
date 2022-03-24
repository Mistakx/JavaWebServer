import com.sun.tools.javac.Main;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;

import static java.lang.Integer.parseInt;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
class MainTest {

    private static final String serverConfigPath = "server/server.config";

    @BeforeAll
    static void startServer() throws IOException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        new Thread(() -> {
            try {
                Main.main(new String[]{serverConfigPath});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    @DisplayName("Server accepting connections")
    @Test
    void serverAcceptingConnections() throws IOException, InterruptedException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        int serverPort = parseInt(serverConfig.getProperty("server.port"), 10);

        // https://stackoverflow.com/questions/3489543/how-to-call-a-method-with-a-separate-thread-in-java
        final boolean[] serverExists = {false};
        Thread testServerConnectionThread = new Thread(() -> {

            // Sleep 2 seconds to wait for server start
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Try to connect to server
            try (Socket serverSocket = new Socket("127.0.0.1", serverPort)) {
                serverExists[0] = true;
                DataOutputStream serverDataOutputStream = new DataOutputStream(serverSocket.getOutputStream());
                // https://www.javatpoint.com/socket-programming
                serverDataOutputStream.writeUTF("");
                serverDataOutputStream.flush();
                serverDataOutputStream.close();
            } catch (IOException ignored) {
            }

        });
        testServerConnectionThread.start();
        testServerConnectionThread.join();
        assertTrue(serverExists[0]);

    }

    @DisplayName("Server returning page")
    @Test
    void serverSendingPage() throws IOException, InterruptedException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        int serverPort = parseInt(serverConfig.getProperty("server.port"), 10);

        // https://stackoverflow.com/questions/3489543/how-to-call-a-method-with-a-separate-thread-in-java
        final boolean[] serverExists = {false};
        Thread testServerConnectionThread = new Thread(() -> {

            try (Socket serverSocket = new Socket("127.0.0.1", serverPort)) {
                serverExists[0] = true;
                DataOutputStream serverDataOutputStream = new DataOutputStream(serverSocket.getOutputStream());
                // https://www.javatpoint.com/socket-programming
                serverDataOutputStream.writeUTF("");
                serverDataOutputStream.flush();
                serverDataOutputStream.close();
            } catch (IOException ignored) {
            }
        });
        testServerConnectionThread.start();
        testServerConnectionThread.join();
        assertTrue(serverExists[0]);
    }

}