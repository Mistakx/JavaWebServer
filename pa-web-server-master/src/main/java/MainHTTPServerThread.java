import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class MainHTTPServerThread extends Thread {

    private DataInputStream in;
    private ServerSocket server;
    private Socket client;
    private int port;
    private Properties serverConfig;

    /**
     * Constructor for the main HTTP server thread
     *
     * @param configPath path of the configuration file used by the HTTP server
     **/
    public MainHTTPServerThread(String configPath) throws IOException {
        serverConfig = new Properties();
        InputStream configPathInputStream = new FileInputStream(configPath);
        serverConfig.load(configPathInputStream);
        port = parseInt(serverConfig.getProperty("server.port"), 10);
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
     * Reads an HTML documents and returns it as string
     *
     * @param path path of the file
     * @return String with the html document at <code>path</code>
     */
    private String readFile(String path) {
        System.out.println(">>> Reading the file");
        File original = new File(path);
        Scanner reader = null;
        String content = "";
        try {
            reader = new Scanner(original);
            while (reader.hasNextLine()) {
                String input = reader.nextLine();
                if (content.isEmpty()) {
                    content = input;
                } else {
                    content = content + "\n" + input;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        System.out.println(">>> Done reading the file");
        return content;
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
            server = new ServerSocket(port);
            System.out.println("Started server socket on port: " + port);
            System.out.println("Working directory: " + System.getProperty("user.dir"));

            while (true) {
                client = server.accept();

                System.out.println();
                System.out.println("Debug: got new client " + client.toString());
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

                StringBuilder requestBuilder = new StringBuilder();
                String line;

                while (!(line = br.readLine()).isBlank()) {
                    requestBuilder.append(line + "\r\n");
                }

                /*
                Quite simple parsing, to be expanded by each group
                 */
                String request = requestBuilder.toString();
                String[] tokens = request.split(" ");
                String route = tokens[1];
                System.out.println(request);

                //* Serve default page when client requests the root route
                String serverRootPath = serverConfig.getProperty("server.root");
                File f = new File(serverRootPath + route);
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

            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Serves a file's content to the client.
     * @param fileContent the root folder path to be served by the server.
     * @throws IOException if an I/O error occurs when creating the output stream or if the socket is not connected.
     */
    private void serveFileContent(byte[] fileContent) throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
        clientOutput.write(("ContentType: text/html\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(fileContent);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }

    /**
     * Serves the error page to the client.
     * @param serverRootRoute the root folder path to be served by the server.
     * @throws IOException if an I/O error occurs when creating the output stream or if the socket is not connected.
     */
    private void serveErrorPage(String serverRootRoute) throws IOException {
        String pageNotFoundFile = serverConfig.getProperty("server.page.404");
        byte[] content = readBinaryFile(serverRootRoute + "../server/" + pageNotFoundFile);
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write("HTTP/1.1 404 Not Found\r\n".getBytes());
        clientOutput.write(("ContentType: text/html\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }
}
