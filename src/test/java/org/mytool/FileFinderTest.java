package org.mytool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileFinderTest {

    @TempDir
    Path dir;

    @Test
    void keepsOnlyTheMatchingExtensionAndSortsByName() throws IOException {
        Files.writeString(dir.resolve("b.jpg"), "x");
        Files.writeString(dir.resolve("a.JPG"), "x");
        Files.writeString(dir.resolve("c.png"), "x");

        List<String> found = FileFinder.find(dir, ".jpg").stream()
                .map(path -> path.getFileName().toString())
                .toList();

        assertEquals(List.of("a.JPG", "b.jpg"), found);
    }

    @Test
    void returnsAnEmptyListRatherThanNull() throws IOException {
        Files.writeString(dir.resolve("notes.txt"), "x");

        assertTrue(FileFinder.find(dir, ".jpg").isEmpty());
    }

    @Test
    void skipsSubdirectoriesThatLookLikeMatches() throws IOException {
        Files.createDirectory(dir.resolve("nested.jpg"));

        assertTrue(FileFinder.find(dir, ".jpg").isEmpty());
    }

    @Test
    void rejectsAPathThatIsNotADirectory() {
        UsageException failure = assertThrows(UsageException.class,
                () -> FileFinder.find(dir.resolve("missing"), ".jpg"));

        assertTrue(failure.getMessage().contains("not a directory"));
    }
}
