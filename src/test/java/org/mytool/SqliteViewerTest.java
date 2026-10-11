package org.mytool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteViewerTest {

    @TempDir
    Path dir;

    @Test
    void aMissingDatabaseFileIsAUsageErrorRatherThanANullPointer() {
        String missing = dir.resolve("missing.db").toString();

        UsageException failure =
                assertThrows(UsageException.class, () -> new SqliteViewer(missing));

        assertTrue(failure.getMessage().contains("does not exist"), failure.getMessage());
    }
}
