import java.io.IOException;

public class Main {

    private static MainHTTPServerThread serverThread;


    /**
     * Main class of the program, only minimal changes should be added to this method
     * @param args
     */
    public static void main(String[] args){

        try {
            serverThread = new MainHTTPServerThread( "/media/shared/PA/JavaWebServer/pa-web-server-master/server/server.config");
        } catch (IOException configFileException) {
            System.out.println(configFileException.getMessage());
            return;
        }
        serverThread.start();
        try {
            serverThread.join();
        } catch (InterruptedException exception) {
            System.out.println(exception.getMessage());
            return;
        }
    }
}
