import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author li1345825138
 * @date 2023/7/18
 */
public class Main {
    public static void main(String[] args) {
        try {
            CommandProcessor commandProcessor = new CommandProcessor(args[0]);
            commandProcessor.process(args);
        } catch (Exception e) {
            logException(e);
            System.err.println(e.getLocalizedMessage());
            CommandProcessor.printHelp();
        }
    }

    /**
     * Hold Exception and print full stack trace to the log file
     * @param e any exception that will be hold
     */
    private static void logException(Exception e) {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter("error_log.log"))) {
            e.printStackTrace(printWriter);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
