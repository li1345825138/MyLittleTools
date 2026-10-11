package org.mytool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Finds the files of one kind inside a directory.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
final class FileFinder {

    private FileFinder() {
    }

    /**
     * @param dir       the directory to search
     * @param extension the extension to keep, matched case-insensitively
     * @return the matching files sorted by name, or an empty list when the
     *         directory holds none
     * @throws UsageException when the path is not a directory
     */
    static List<Path> find(Path dir, String extension) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new UsageException(dir + " is not a directory");
        }
        String suffix = extension.toLowerCase(Locale.ROOT);
        try (Stream<Path> entries = Files.list(dir)) {
            return entries
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(suffix))
                    .sorted((left, right) -> left.getFileName().toString()
                            .compareToIgnoreCase(right.getFileName().toString()))
                    .toList();
        }
    }
}
