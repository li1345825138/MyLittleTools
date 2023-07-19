/**
 * @author li1345825138
 * @date 2023/7/18
 */
public class Main {
    public static void main(String[] args) {
        try {
            CommandProcessor commandProcessor = new CommandProcessor(args[0]);
            commandProcessor.process(args);
        }catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.out.println("Usage: java -jar MyLittleTools option [argument1] [argument2] ...");
            System.out.println("options:");
            System.out.println("    -hash: compare two file hash");
        }
    }
}
