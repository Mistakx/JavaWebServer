import java.io.IOException;

public class Main {

    private static MainHTTPServerThread serverThread;


    /**
     * Main class of the program, only minimal changes should be added to this method
     * @param args
     */
    public static void main(String[] args){

        try {
            serverThread = new MainHTTPServerThread( "/media/shared/JavaWebServer/pa-web-server-master/server/server.config");
        } catch (IOException configFileException) {
            System.out.println("Error when opening configuration file.");
            System.out.println(configFileException.getMessage());
        }
        serverThread.start();
        try {
            serverThread.join();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}
