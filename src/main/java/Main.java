import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * @author li1345825138
 * @date 2023/7/18
 */
public class Main {
    public static void main(String[] args) {
        /*System.out.printf("-hash: %d\n-w: %d\n-p: %d\n-h: %d\n-m: %d\n-mi: %d\n", "-hash".hashCode(), "-w".hashCode(),
                "-p".hashCode(), "-h".hashCode(), "-m".hashCode(), "-mi".hashCode());*/
        try {
            CommandProcessor commandProcessor = new CommandProcessor(args[0]);
            commandProcessor.process(args);
        } catch (Exception e) {
            holdException(e);
            System.err.println(e.getLocalizedMessage());
            CommandProcessor.printHelp();
        }
    }

    /**
     * Hold Exception and print full stack trace to the log file
     * @param e any exception that will be hold
     */
    private static void holdException(Exception e) {
        try (PrintWriter printWriter = new PrintWriter("error_log.log")) {
            e.printStackTrace(printWriter);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
    }
}
