import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class of the thread responsible logging the requests' information to a file.
 */
public class LogRequestsInformationThread extends Thread {

    /**
     * The path of the file to save the requests' information to.
     */
    private final String logFilePath;

    /**
     * The lock responsible for the requests' information array,
     * which contains a list of requests information to save to the log.
     */
    private final ReentrantLock requestsInformationLock;
    /**
     * Contains a list of the requests' information not yet saved to the log.
     */
    private final Queue<String> requestsInformation;
    /**
     * The lock responsible for the log document, which contains a list of requests information.
     */
    private final ReentrantLock logLock;

    /**
     * The constructor of the thread that logs the requests' information.
     *
     * @param logFilePath             The path of the file to save the requests' information to.
     * @param requestsInformationLock The lock responsible for the requests' information array.
     * @param requestsInformation     Contains a list of the requests' information not yet saved to the log.
     * @param logLock                 * The lock responsible for the log document.
     */
    public LogRequestsInformationThread(String logFilePath, ReentrantLock requestsInformationLock, Queue<String> requestsInformation, ReentrantLock logLock) {
        this.logFilePath = logFilePath;
        this.requestsInformationLock = requestsInformationLock;
        this.requestsInformation = requestsInformation;
        this.logLock = logLock;
    }

    /**
     * Logs the first request in the requests' information queue.
     *
     * @throws IOException Upon failed or interrupted I/O operations.
     */
    private void logFirstRequestsInformation() throws IOException, InterruptedException {

        String firstRequestInformation;

        if (requestsInformation.size() != 0) {

            requestsInformationLock.lock();
            firstRequestInformation = requestsInformation.poll();
            requestsInformationLock.unlock();

            logLock.lock();
            FileWriter logFileWriter = new FileWriter(logFilePath, true);
            BufferedWriter logBufferedWriter = new BufferedWriter(logFileWriter);
            logBufferedWriter.append(firstRequestInformation).append("\n");
            logBufferedWriter.flush();
            logLock.unlock();
        } else {
            Thread.sleep(100);
        }
    }

    @Override
    public void run() {

        //noinspection InfiniteLoopStatement
        while (true) {

            try {
                logFirstRequestsInformation();
            } catch (IOException | InterruptedException exception) {
                System.out.println(exception.getMessage());
            }
        }

    }

}
