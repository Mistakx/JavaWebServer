import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private static final String serverConfigPath = "server/server.config";
    private static final String logFilePath = "logFile.txt";
    private static final int requestTimeout = 100000;

    @BeforeAll
    @DisplayName("Server starts.")
    static void startServer() throws IOException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        new Thread(() -> {
            WebServer.main(new String[]{serverConfigPath});
        }).start();

    }

    @BeforeEach
    @DisplayName("Deletes the server log.")
    void deleteServerLog() {
        File file = new File(logFilePath);

        if (file.delete()) {
            System.out.println("Log file deleted successfully.");
        } else {
            System.out.println("Failed to delete the log file.");
        }
    }

    @DisplayName("Server sends index page.")
    @Test
    void serverSendsIndexPage() throws IOException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        int serverPort = parseInt(serverConfig.getProperty("server.port"), 10);

        // https://stackoverflow.com/questions/3489543/how-to-call-a-method-with-a-separate-thread-in-java
        int responseCode = 0;
        try {
            URL url = new URL("http://127.0.0.1:" + serverPort);
            HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
            serverConnection.setRequestMethod("GET");
            serverConnection.setReadTimeout(requestTimeout);
            responseCode = serverConnection.getResponseCode();

        } catch (Exception exception) {
            exception.printStackTrace();
        }


        assertEquals(200, responseCode);
    }

    @DisplayName("Server sends error page.")
    @Test
    void serverSendsErrorPage() throws IOException, InterruptedException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        int serverPort = parseInt(serverConfig.getProperty("server.port"), 10);

        // https://stackoverflow.com/questions/3489543/how-to-call-a-method-with-a-separate-thread-in-java
        int responseCode = 0;

        try {
            URL url = new URL("http://127.0.0.1:" + serverPort + "/invalidPath");
            HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
            serverConnection.setRequestMethod("GET");
            serverConnection.setReadTimeout(requestTimeout);
            responseCode = serverConnection.getResponseCode();

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        assertEquals(404, responseCode);
    }

    @DisplayName("Server sends the same page to two different clients simultaneously, to test the synchronization.")
    @RepeatedTest(5)
    void serverSendsPageToTwoDifferentClients() throws IOException, InterruptedException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        int serverPort = parseInt(serverConfig.getProperty("server.port"), 10);

        // https://stackoverflow.com/questions/3489543/how-to-call-a-method-with-a-separate-thread-in-java
        AtomicInteger responseCode1 = new AtomicInteger();
        Thread testServerConnectionThread1 = new Thread(() -> {

            try {
                URL url = new URL("http://127.0.0.1:" + serverPort);
                HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
                serverConnection.setRequestMethod("GET");
                serverConnection.setReadTimeout(requestTimeout);
                responseCode1.set(serverConnection.getResponseCode());

            } catch (Exception exception) {
                exception.printStackTrace();
            }

        });

        AtomicInteger responseCode2 = new AtomicInteger();
        Thread testServerConnectionThread2 = new Thread(() -> {

            try {
                URL url = new URL("http://127.0.0.1:" + serverPort);
                HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
                serverConnection.setRequestMethod("GET");
                serverConnection.setReadTimeout(requestTimeout);
                responseCode2.set(serverConnection.getResponseCode());

            } catch (Exception exception) {
                exception.printStackTrace();
            }

        });

        testServerConnectionThread1.start();
        testServerConnectionThread2.start();

        testServerConnectionThread1.join();
        testServerConnectionThread2.join();

        assertAll(
                () -> assertEquals(200, responseCode1.get()),
                () -> assertEquals(200, responseCode2.get())
        );
    }

    @DisplayName("Server logs the request for the index page.")
    @Test
    void serverLogsIndexPageRequest() throws IOException {
        Properties serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(serverConfigPath);
        serverConfig.load(configPathInputStream);
        int serverPort = parseInt(serverConfig.getProperty("server.port"), 10);

        // https://stackoverflow.com/questions/3489543/how-to-call-a-method-with-a-separate-thread-in-java

        try {

            URL url = new URL("http://127.0.0.1:" + serverPort);
            HttpURLConnection serverConnection = (HttpURLConnection) url.openConnection();
            serverConnection.setRequestMethod("GET");
            serverConnection.getInputStream();

            File logFile = new File(logFilePath);
            boolean logFileContainsRequest = false;

            try {

                Scanner scanner = new Scanner(logFile);

                int lineNum = 0;

                while (scanner.hasNextLine()) {

                    String line = scanner.nextLine();
                    lineNum++;
                    if (line.contains("Method:GET-Route:/")) {
                        logFileContainsRequest = true;
                    }

                }

            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }

            assertTrue(logFileContainsRequest);

        } catch (
                Exception exception) {
            exception.printStackTrace();
        }

    }

}