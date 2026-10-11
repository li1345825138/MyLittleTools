package org.mytool;

import java.nio.file.Path;
import java.util.List;

/**
 * Runs the Tool that the given option selects.
 *
 * @author li1345825138
 * @date 2023/7/18
 */
record CommandProcessor(Option option, Progress progress) {

    /** Where -randpass keeps the passwords it has generated before. */
    private static final Path DEFAULT_DATABASE = Path.of("./database.sql");

    /**
     * Process the command send in
     *
     * @param arguments the arguments this option expects, excluding the option name itself
     */
    void process(String[] arguments) throws Exception {
        switch (option) {
            case HASH_COMPARE -> compareHash(arguments);
            case WEBP_TO_JPG -> convertWebp(arguments);
            case JPGS_TO_PDF -> convertJpgsToPdf(arguments);
            case HELP_MSG -> Main.printHelp();
            case MERGE_PDFS -> mergePdfs(arguments);
            case MERGE_MULTI_JPGS -> mergeImages(arguments);
            case RAND_PASS -> generatePassword(arguments);
            case VIEW_SQL -> viewSqlite(arguments);
            // Guards against a new Option constant silently doing nothing:
            // a statement switch over an enum is not checked for exhaustiveness.
            default -> throw new IllegalStateException(option + " has no handler");
        }
    }

    private void compareHash(String[] arguments) throws Exception {
        HashComparer.Comparison comparison =
                new HashComparer().compare(Path.of(arguments[0]), Path.of(arguments[1]));
        System.out.printf("FILE 1: %s  Hash: %s%n", arguments[0], comparison.file1Hash());
        System.out.printf("FILE 2: %s  Hash: %s%n", arguments[1], comparison.file2Hash());
        System.out.printf("Hash Result: %s%n", comparison.equal() ? "Hash Correct" : "File has been modify");
    }

    private void convertWebp(String[] arguments) throws Exception {
        List<Path> webps = imagesIn(arguments[0], ".webp");
        WebpConverter.Result result = new WebpConverter().convert(webps, progress);
        System.out.printf("Converted %d WEBP file(s) to JPG%n", result.outputs().size());
    }

    private void convertJpgsToPdf(String[] arguments) throws Exception {
        List<Path> images = imagesIn(arguments[0], ".jpg");
        JpgToPdf.Result result = new JpgToPdf().convert(images, outputPath(arguments[1]), progress);
        System.out.printf("Wrote %d page(s) to %s%n", result.pageCount(), result.output());
    }

    private void mergePdfs(String[] arguments) throws Exception {
        List<Path> pdfs = imagesIn(arguments[0], ".pdf");
        PdfMerger.Result result = new PdfMerger().merge(pdfs, outputPath(arguments[1]), progress);
        System.out.printf("Merged %d PDF(s) into %s%n", result.sourceCount(), result.output());
    }

    private void mergeImages(String[] arguments) throws Exception {
        List<Path> images = imagesIn(arguments[0], ".jpg");
        ImageStacker.Result result = new ImageStacker().stack(images, outputPath(arguments[1]), progress);
        System.out.printf("Merged %d image(s) into %s (%dx%d)%n",
                result.imageCount(), result.output(), result.width(), result.height());
    }

    private void generatePassword(String[] arguments) throws Exception {
        int length = parseLength(arguments[0]);
        try (PasswordStore store = PasswordStore.create(DEFAULT_DATABASE)) {
            String password = new RandomPasswordGenerator(store).generate(length);
            System.out.printf("Random Password: %s\n", password);
        }
    }

    private void viewSqlite(String[] arguments) throws Exception {
        try (PasswordStore store = PasswordStore.open(Path.of(arguments[0]))) {
            List<String> passwords = store.all();
            passwords.forEach(System.out::println);
            System.out.printf("Total Records: %d%n", passwords.size());
        }
    }

    /**
     * @throws UsageException when the directory holds no files of that kind
     */
    private static List<Path> imagesIn(String directory, String extension) throws Exception {
        List<Path> files = FileFinder.find(Path.of(directory), extension);
        if (files.isEmpty()) {
            throw new UsageException("No " + extension + " files in " + directory);
        }
        return files;
    }

    /**
     * @throws UsageException when the caller left the output name blank
     */
    private static Path outputPath(String name) {
        if (name == null || name.isBlank()) {
            throw new UsageException("An output file name is required");
        }
        return Path.of(name);
    }

    /**
     * @throws UsageException when the argument is not a number
     */
    private static int parseLength(String argument) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new UsageException("Not a number: " + argument);
        }
    }
}
