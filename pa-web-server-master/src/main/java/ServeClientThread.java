import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Properties;

/**
 * The class of the thread responsible for serving each client accepted by the server.
 */
public class ServeClientThread extends Thread {

    /**
     * The client's socket, created when the client requested some route.
     */
    private final Socket clientSocket;
    /**
     * The server configuration, created when the "AcceptClients" thread was instantiated.
     */
    private final Properties serverConfig;

    /**
     * Constructor for the various threads that serve a single accepted client.
     *
     * @param clientSocket socket of the client that is going to get served by this thread.
     **/
    public ServeClientThread(Socket clientSocket, Properties serverConfig) {
        this.clientSocket = clientSocket;
        this.serverConfig = serverConfig;
    }

    /**
     * Reads a server document and returns it as an array of bytes
     *
     * @param path path of the file
     * @return <code>byte[]</code> with the html document at <code>path</code>
     */
    private byte[] readBinaryFile(String path) {
        byte[] content = new byte[0];
        File f = new File(path);
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
     * Serves a file's content to the client.
     *
     * @param fileContent the file's content in bytes to be served by the server.
     * @throws IOException if an I/O error occurs when creating the output stream or if the socket is not connected.
     */
    private void serveFileContent(byte[] fileContent) throws IOException {
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
        clientOutput.write(("ContentType: text/html\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(fileContent);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        clientSocket.close();
    }

    /**
     * Serves the error page to the client.
     * @param serverRootRoute the root folder path to be served by the server.
     * @throws IOException if an I/O error occurs when creating the output stream or if the socket is not connected.
     */
    private void serveErrorPage(String serverRootRoute) throws IOException {
        String pageNotFoundFile = serverConfig.getProperty("server.page.404");
        byte[] content = readBinaryFile(serverRootRoute + "../server/" + pageNotFoundFile);
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write("HTTP/1.1 404 Not Found\r\n".getBytes());
        clientOutput.write(("ContentType: text/html\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        clientSocket.close();
    }

    @Override
    public void run() {

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            StringBuilder requestBuilder = new StringBuilder();
            String line;

            while (!(line = br.readLine()).isBlank()) {
                requestBuilder.append(line).append("\r\n");
            }

            /*
            Quite simple parsing, to be expanded by each group
             */
            String request = requestBuilder.toString();
            String[] tokens = request.split(" ");
            String route = tokens[1];
            System.out.println(request);

            String serverRootPath = serverConfig.getProperty("server.root");
            File f = new File(serverRootPath + route);

            //* Serve default page when client requests the root route
            if (Objects.equals(route, "/")) {

                // If route file exists, serve requested route
                if (f.exists() && f.isFile()) {
                    String defaultFilename = serverConfig.getProperty("server.default.page");
                    String defaultFileExtension = serverConfig.getProperty("server.default.page.extension");
                    String defaultFile = defaultFilename + "." + defaultFileExtension;
                    byte[] fileContent = readBinaryFile(serverRootPath + "/" + defaultFile);
                    serveFileContent(fileContent);
                }

                // If the requested route doesn't have a file, serve the error page
                else {
                    serveErrorPage(serverRootPath);
                }

            }

            //* Serve non default page when client requests non root route
            else {

                // If route file exists, serve requested route
                if (f.exists() && f.isFile()) {
                    byte[] content = readBinaryFile(serverRootPath + route);
                    serveFileContent(content);
                }

                // If the requested route doesn't have a file, serve the error page
                else {
                    serveErrorPage(serverRootPath);
                }
            }

            //* Sleep 10 seconds
            try {
                Thread.sleep(10000);
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
