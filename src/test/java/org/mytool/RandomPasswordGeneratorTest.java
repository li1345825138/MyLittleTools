package org.mytool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomPasswordGeneratorTest {

    @TempDir
    Path dir;

    private PasswordStore store() throws Exception {
        return PasswordStore.create(dir.resolve("database.sql"));
    }

    @Test
    void clampsTheLengthUpToTheDocumentedMinimum() {
        assertEquals(8, RandomPasswordGenerator.clampLength(0));
        assertEquals(8, RandomPasswordGenerator.clampLength(1));
        assertEquals(8, RandomPasswordGenerator.clampLength(-5));
        assertEquals(8, RandomPasswordGenerator.clampLength(8));
    }

    @Test
    void clampsTheLengthDownToTheDocumentedMaximum() {
        assertEquals(1024, RandomPasswordGenerator.clampLength(1024));
        assertEquals(1024, RandomPasswordGenerator.clampLength(5000));
    }

    @Test
    void leavesAReasonableLengthAlone() {
        assertEquals(12, RandomPasswordGenerator.clampLength(12));
        assertEquals(64, RandomPasswordGenerator.clampLength(64));
    }

    @Test
    void generateHonoursTheRequestedLength() throws Exception {
        try (PasswordStore store = store()) {
            assertEquals(16, new RandomPasswordGenerator(store).generate(16).length());
        }
    }

    @Test
    void generateAppliesTheMinimumBeforeGenerating() throws Exception {
        try (PasswordStore store = store()) {
            assertEquals(8, new RandomPasswordGenerator(store).generate(0).length());
        }
    }

    @Test
    void everyGeneratedPasswordIsStored() throws Exception {
        try (PasswordStore store = store()) {
            RandomPasswordGenerator generator = new RandomPasswordGenerator(store);

            for (int i = 0; i < 20; i++) {
                String password = generator.generate(8);
                assertEquals(8, password.length());
                assertTrue(store.all().contains(password));
            }

            assertEquals(20, store.all().size(), "twenty generations, twenty distinct passwords");
        }
    }

    @Test
    void consecutivePasswordsDiffer() throws Exception {
        try (PasswordStore store = store()) {
            RandomPasswordGenerator generator = new RandomPasswordGenerator(store);

            assertNotEquals(generator.generate(12), generator.generate(12));
        }
    }
}
