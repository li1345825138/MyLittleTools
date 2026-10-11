import java.util.Arrays;
import java.util.Optional;

/**
 * A command-line option: the name a user types to select a Tool, paired with
 * the number of arguments that Tool expects.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
enum Option {

    // -hash: compare two file hash value
    HASH_COMPARE("-hash", 2),

    // -w: convert WEBP image to JPG format
    WEBP_TO_JPG("-w", 1),

    // -p: Convert JPG files into one single PDF file
    JPGS_TO_PDF("-p", 2),

    // -h: help message
    HELP_MSG("-h", 0),

    // -m: Merge Multi PDFs into single PDF
    MERGE_PDFS("-m", 2),

    // -mi: Merge multi JPGs into single JPG
    MERGE_MULTI_JPGS("-mi", 2),

    // -extractImages: extract images from pdf
    EXTRACT_IMAGES("-extractImages", 2),

    // -randpass: generate a random password
    RAND_PASS("-randpass", 1),

    // -viewsql: view sqlite database
    VIEW_SQL("-viewsql", 1);

    /** the name a user types on the command line */
    private final String cliName;

    /** how many arguments this option expects */
    private final int argCount;

    Option(String cliName, int argCount) {
        this.cliName = cliName;
        this.argCount = argCount;
    }

    /**
     * @return the name a user types on the command line
     */
    public String cliName() {
        return cliName;
    }

    /**
     * @return how many arguments this option expects
     */
    public int argCount() {
        return argCount;
    }

    /**
     * Find the option that owns the given name. Matching is case-sensitive, as
     * it was before this enum existed.
     *
     * @param name the first command-line argument
     * @return the matching option, or empty when no option owns that name
     */
    public static Optional<Option> resolve(String name) {
        if (name == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(option -> option.cliName.equals(name))
                .findFirst();
    }
}
