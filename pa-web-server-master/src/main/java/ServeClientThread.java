import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class of the thread responsible for serving each client accepted by the server.
 */
public class ServeClientThread extends Thread {

    /**
     * The client's socket, created when the client requested some route.
     */
    private final Socket clientSocket;
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
     * Constructor for the various threads that serve a single accepted request.
     * @param serverConfig The server's configuration, imported from the configuration file when the server started.
     * @param clientSocket The client's socket, created when the client requested some route.
     * @param clientSocketsLock The lock responsible for the client sockets array.
     * @param clientSockets Array that contains the sockets of each request being made at a given point in time.
     * @param currentlyOpenedDocumentsLock The lock responsible for the opened documents array.
     * @param currentlyOpenedDocuments Contains a list of the documents being requested at a given point in time.
     **/
    public ServeClientThread(Properties serverConfig, Socket clientSocket, ReentrantLock clientSocketsLock, ArrayList<Socket> clientSockets, ReentrantLock currentlyOpenedDocumentsLock, Set<String> currentlyOpenedDocuments) {
        this.serverConfig = serverConfig;
        this.clientSocket = clientSocket;

        this.clientSocketsLock = clientSocketsLock;
        this.clientSockets = clientSockets;

        this.currentlyOpenedDocumentsLock = currentlyOpenedDocumentsLock;
        this.currentlyOpenedDocuments = currentlyOpenedDocuments;
    }

    /**
     * Reads a document and returns it as an array of bytes.
     * @param filePath Path of the file to return as an array of bytes.
     * @return <code>byte[]</code> - Array of bytes that constitute a given file.
     */
    private byte[] readBinaryFile(String filePath) {
        byte[] content = new byte[0];
        File f = new File(filePath);
        try {
            FileInputStream fileInputStream = new FileInputStream(f);
            content = fileInputStream.readAllBytes();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return content;
        }
    }

    /**
     * Checks if another thread is already serving a document.
     * @param documentPath The document's path to check.
     * @return <code>boolean</code>
     * <ul>
     * <li> <strong>true - </strong>the document is already being served by another thread. </li>
     * <li> <strong>false - </strong>the document isn't already being served by another thread. </li>
     * </ul>
     */
    private boolean documentAlreadyBeingServed(String documentPath) {

        if (currentlyOpenedDocumentsLock.isLocked()) {
            System.out.println("Another thread is currently accessing the opened documents lock.");
        }

        currentlyOpenedDocumentsLock.lock();
        System.out.println("Acquired opened documents lock.");
        System.out.println("Opened documents: " + currentlyOpenedDocuments.toString());

        if (currentlyOpenedDocuments.contains(documentPath)) {

            System.out.println("Another thread has the document currently opened.");
            currentlyOpenedDocumentsLock.unlock();
            System.out.println("Unlocked documents opened lock.\n");
            return true;

        } else {

            System.out.println("No thread has the document currently opened.");
            currentlyOpenedDocumentsLock.unlock();
            System.out.println("Unlocked documents opened lock.\n");
            return false;

        }

    }

    /**
     * Checks if the HTML error page is properly configured in the server configurations file.
     * @return <code>boolean</code>
     * <ul>
     *     <li> <strong>true -</strong> if the settings error page is properly configured.</li>
     *     <li> <strong>false -</strong> if the settings error page isn't properly configured.</li>
     * </ul>
     */
    private boolean htmlErrorPageExists() {

        String pageNotFoundRootPath = serverConfig.getProperty("server.404.root");
        String pageNotFoundFilename = serverConfig.getProperty("server.404.page");
        String pageNotFoundFileExtension = serverConfig.getProperty("server.404.page.extension");
        String pageNotFoundFile = pageNotFoundFilename + "." + pageNotFoundFileExtension;

        File f = new File(pageNotFoundRootPath + "/" + pageNotFoundFile);

        return f.exists() && f.isFile();

    }

    /**
     * Parses the route the client is requesting from the socket of the request.
     * @return <code>String</code> - the route the client is requesting.
     * @throws IOException if an I/O error occurs when creating the output stream or if the socket is not connected.
     */
    private String parseRequest() throws IOException {

        //! Get and parse the client request
        BufferedReader clientBufferedRead = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        StringBuilder requestBuilder = new StringBuilder();
        String line;

        while (!(line = clientBufferedRead.readLine()).isBlank()) {
            requestBuilder.append(line).append("\r\n");
        }

//        while (true) {
//
//            line = clientBufferedRead.readLine();
//
//            // For some unknown reason, sometimes the line read from the buffer is null
//            if (Objects.isNull(line)) {
//                break;
//            }
//
//            // If the line isn't null, check if is blank, and append it if it isn't
//            boolean lineIsBlank = line.isBlank();
//            if (!lineIsBlank) {
//                requestBuilder.append(line).append("\r\n");
//            }
//
//        }

            /*
            Quite simple parsing, to be expanded by each group
             */
        String request = requestBuilder.toString();
        String[] tokens = request.split(" ");
        System.out.println(request);

        return tokens[1];
    }

    /**
     * Serves a file's content to the client, if that file is not already being served by another thread.
     *
     * @param filePath the path of the file that is going to be served to the client.
     * @throws IOException if an I/O error occurs when creating the output stream or if the socket is not connected.
     */
    private void serveFileContent(String filePath) throws IOException {

        boolean servedDocument = false;

        // Continuously check if the document is already being served, and serve it if it isn't
        while (!servedDocument) {

            // If document is already being served, wait till it isn't
            if (documentAlreadyBeingServed(filePath)) {
                try {
                    Thread.sleep(2000);
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                }
            }

            // If the document isn't being served, serve it
            else {

                currentlyOpenedDocumentsLock.lock();
                System.out.println("Currently serving: " + filePath);
                currentlyOpenedDocuments.add(filePath);
                currentlyOpenedDocumentsLock.unlock();

                //* Sleep 10 seconds
                try {
                    Thread.sleep(10000);
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                }

                byte[] fileContent = readBinaryFile(filePath);
                OutputStream clientOutput = clientSocket.getOutputStream();
                clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
                clientOutput.write(("ContentType: text/html\r\n").getBytes());
                clientOutput.write("\r\n".getBytes());
                clientOutput.write(fileContent);
                clientOutput.write("\r\n\r\n".getBytes());
                clientOutput.flush();
                clientSocket.close();
                servedDocument = true;

                currentlyOpenedDocumentsLock.lock();
                System.out.println("Stopped serving: " + filePath + "\n");
                currentlyOpenedDocuments.remove(filePath);
                currentlyOpenedDocumentsLock.unlock();

            }

        }

    }


    /**
     * Parses the request, and serves the corresponding file to the client.
     */
    @Override
    public void run() {

        try {

            String route = parseRequest(); // Gets the route the client is requesting
            System.out.println("Route to serve: " + route);
            //! Send the appropriate response to the client

            String serverRootPath = serverConfig.getProperty("server.root");
            String pageNotFoundRootPath = serverConfig.getProperty("server.404.root");
            String pageNotFoundFilename = serverConfig.getProperty("server.404.page");
            String pageNotFoundFileExtension = serverConfig.getProperty("server.404.page.extension");
            String pageNotFoundFile = pageNotFoundFilename + "." + pageNotFoundFileExtension;

            //* Serve default page when client requests the root route
            if (Objects.equals(route, "/")) {

                String defaultFilename = serverConfig.getProperty("server.default.page");
                String defaultFileExtension = serverConfig.getProperty("server.default.page.extension");
                String defaultFile = defaultFilename + "." + defaultFileExtension;
                File defaultPage = new File(serverRootPath + "/" + defaultFile);

                if (defaultPage.exists() && defaultPage.isFile()) {

                    System.out.println("Started trying to serve default route.");
                    serveFileContent(serverRootPath + "/" + defaultFile);

                } else {

                    // TODO: Serve predefined file if the error page isn't found
                    System.out.println("Started serving error route. (default route)");
                    serveFileContent(pageNotFoundRootPath + "/" + pageNotFoundFile);

                }

            }

            //* Serve non default page when client requests the root route
            else {

                File nonDefaultPage = new File(serverRootPath + route);

                if (nonDefaultPage.exists() && nonDefaultPage.isFile()) {
                    System.out.println("Started trying to serve non default route.");
                    serveFileContent(serverRootPath + route);
                } else {

                    // TODO: Serve predefined file if the error page isn't found
                    System.out.println("Serving error route. (non default route)");
                    serveFileContent(pageNotFoundRootPath + "/" + pageNotFoundFile);
                }
            }

        } catch (IOException exception) {

            exception.printStackTrace();

        } finally {

            clientSocketsLock.lock();
            clientSockets.remove(clientSocket);
            clientSocketsLock.unlock();

        }

    }
}
