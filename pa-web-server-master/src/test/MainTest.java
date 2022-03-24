import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
            WebServer.main(new String[]{serverConfigPath});
        }).start();

    }

    @DisplayName("Server sends index page.")
    @Test
    void serverSendsIndexPage() throws IOException, InterruptedException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        int serverPort = parseInt(serverConfig.getProperty("server.port"), 10);

        // https://stackoverflow.com/questions/3489543/how-to-call-a-method-with-a-separate-thread-in-java
        AtomicInteger responseCode = new AtomicInteger();
        Thread testServerConnectionThread = new Thread(() -> {

            try {
                URL url = new URL("http://127.0.0.1:" + serverPort);
                HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
                serverConnection.setRequestMethod("GET");
                responseCode.set(serverConnection.getResponseCode());
                responseCode.set(serverConnection.getResponseCode());

            } catch (Exception exception) {
                exception.printStackTrace();
            }

        });
        testServerConnectionThread.start();
        testServerConnectionThread.join();
        assertEquals(200, responseCode.get());
    }

    @DisplayName("Server sends error page.")
    @Test
    void serverSendsErrorPage() throws IOException, InterruptedException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        int serverPort = parseInt(serverConfig.getProperty("server.port"), 10);

        // https://stackoverflow.com/questions/3489543/how-to-call-a-method-with-a-separate-thread-in-java
        AtomicInteger responseCode = new AtomicInteger();
        Thread testServerConnectionThread = new Thread(() -> {

            try {
                URL url = new URL("http://127.0.0.1:" + serverPort + "/invalidPath");
                HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
                serverConnection.setRequestMethod("GET");
                responseCode.set(serverConnection.getResponseCode());

            } catch (Exception exception) {
                exception.printStackTrace();
            }

        });
        testServerConnectionThread.start();
        testServerConnectionThread.join();
        assertEquals(404, responseCode.get());
    }


}