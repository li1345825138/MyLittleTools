import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author li1345825138
 * @date 2023/7/18
 */
public class Main {
    public static void main(String[] args) {
        // No arguments at all is a request for help, not a usage error.
        if (args.length == 0) {
            printHelp();
            return;
        }

        int givenArgs = args.length - 1;
        Option option = Option.resolve(args[0]).orElse(null);
        if (option == null || givenArgs < option.argCount()) {
            printHelp();
            System.exit(2);
        }

        try {
            new CommandProcessor(option).process(Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            logException(e);
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
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

    public static void printHelp() {
        Properties properties = new Properties();
        try (
                InputStream input = CommandProcessor.class.getClassLoader().getResourceAsStream("message.properties");
                BufferedInputStream bInput = (input != null) ? new BufferedInputStream(input) : null
        ) {
            properties.load(bInput);
            System.out.println(properties.getProperty("HELP_MESSAGE"));
        } catch (Exception e) {
            System.err.println("Can't Read message properties file from resource stream: " + e.getMessage());
            System.exit(1);
        }
    }
}
