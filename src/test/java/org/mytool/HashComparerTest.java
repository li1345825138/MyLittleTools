package org.mytool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashComparerTest {

    @TempDir
    Path dir;

    @Test
    void equalFilesCompareEqual() throws IOException {
        Path a = Files.writeString(dir.resolve("a.txt"), "AAA\n");
        Path b = Files.writeString(dir.resolve("b.txt"), "AAA\n");

        HashComparer.Comparison result = new HashComparer().compare(a, b);

        assertTrue(result.equal());
        assertEquals(result.file1Hash(), result.file2Hash());
    }

    @Test
    void differentFilesCompareUnequal() throws IOException {
        Path a = Files.writeString(dir.resolve("a.txt"), "AAA\n");
        Path b = Files.writeString(dir.resolve("b.txt"), "BBB\n");

        assertFalse(new HashComparer().compare(a, b).equal());
    }

    @Test
    void everyHashIsUppercaseHexOfSixtyFourCharacters() throws IOException {
        Path a = Files.writeString(dir.resolve("a.txt"), "AAA\n");
        Path b = Files.writeString(dir.resolve("b.txt"), "AAA\n");

        HashComparer.Comparison result = new HashComparer().compare(a, b);

        assertTrue(result.file1Hash().matches("[0-9A-F]{64}"), result.file1Hash());
        assertTrue(result.file2Hash().matches("[0-9A-F]{64}"), result.file2Hash());
    }

    @Test
    void aMissingFileFails() {
        assertThrows(IOException.class,
                () -> new HashComparer().compare(dir.resolve("nope.txt"), dir.resolve("nope2.txt")));
    }
}
