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
            System.err.println(e.getLocalizedMessage());
            System.out.println("""
                    Usage: java -jar MyLittleTools option [argument1] [argument2] ...
                    options:
                        -hash: compare two file hash.
                            -hash file1 file2
                        -w: convert WEBP image into JPG.
                            -w imageFolderPath
                        -p: convert multiple jpg images into single pdf
                            -p imageFolderPath finalSaveName
                    """);
        }
    }
}
