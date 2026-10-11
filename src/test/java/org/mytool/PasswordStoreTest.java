package org.mytool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordStoreTest {

    @TempDir
    Path dir;

    private Path database() {
        return dir.resolve("database.sql");
    }

    @Test
    void createMakesTheDatabaseFile() throws Exception {
        assertFalse(Files.exists(database()));

        try (PasswordStore ignored = PasswordStore.create(database())) {
            assertTrue(Files.exists(database()));
        }
    }

    @Test
    void aFreshDatabaseIsEmpty() throws Exception {
        try (PasswordStore store = PasswordStore.create(database())) {
            assertTrue(store.all().isEmpty());
        }
    }

    @Test
    void openRejectsAPathThatDoesNotExist() {
        UsageException failure =
                assertThrows(UsageException.class, () -> PasswordStore.open(database()));

        assertTrue(failure.getMessage().contains("does not exist"), failure.getMessage());
    }

    @Test
    void openRejectsADatabaseWithNoPasswordTable() throws Exception {
        Path other = dir.resolve("other.sql");
        execute(other, "CREATE TABLE unrelated (value TEXT)");

        UsageException failure =
                assertThrows(UsageException.class, () -> PasswordStore.open(other));

        assertTrue(failure.getMessage().contains("no password table"), failure.getMessage());
    }

    /** Opening a store for reading must not write to the file it is handed. */
    @Test
    void openLeavesAForeignDatabaseExactlyAsItWas() throws Exception {
        Path foreign = dir.resolve("foreign.sql");
        execute(foreign,
                "CREATE TABLE unrelated (value TEXT)",
                "CREATE INDEX idx_password ON unrelated (value)",
                "INSERT INTO unrelated (value) VALUES ('kept')");

        assertThrows(UsageException.class, () -> PasswordStore.open(foreign));

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + foreign);
             Statement statement = connection.createStatement()) {
            try (ResultSet rows = statement.executeQuery("SELECT value FROM unrelated")) {
                assertTrue(rows.next(), "the foreign table is still there");
                assertEquals("kept", rows.getString("value"), "its rows are untouched");
            }
            try (ResultSet indexes = statement.executeQuery("PRAGMA index_list('unrelated')")) {
                assertTrue(indexes.next(), "the foreign index was not dropped");
                assertEquals("idx_password", indexes.getString("name"));
            }
        }
    }

    @Test
    void openAcceptsADatabaseThatExists() throws Exception {
        try (PasswordStore created = PasswordStore.create(database())) {
            created.storeIfAbsent("kept-across-reopen");
        }

        try (PasswordStore reopened = PasswordStore.open(database())) {
            assertEquals(List.of("kept-across-reopen"), reopened.all());
        }
    }

    /** An earlier version's schema, plus a password it allowed to repeat. */
    @Test
    void createRepairsADatabaseThatHoldsAPasswordTwice() throws Exception {
        execute(database(),
                "CREATE TABLE prevPass (id INTEGER PRIMARY KEY AUTOINCREMENT, password TEXT)",
                "CREATE INDEX idx_password ON prevPass (password)",
                "INSERT INTO prevPass (password) VALUES ('same'), ('same'), ('other')");

        try (PasswordStore store = PasswordStore.create(database())) {
            assertEquals(List.of("same", "other"), store.all(), "the duplicate row was dropped");
            assertFalse(store.storeIfAbsent("same"), "the surviving password is still recognised");
            assertTrue(store.storeIfAbsent("third"));
        }
    }

    @Test
    void storeIfAbsentReportsWhetherThePasswordWasNew() throws Exception {
        try (PasswordStore store = PasswordStore.create(database())) {
            assertTrue(store.storeIfAbsent("first"));
            assertFalse(store.storeIfAbsent("first"), "the second insert is not new");
            assertTrue(store.storeIfAbsent("second"));

            assertEquals(List.of("first", "second"), store.all(), "a rejected insert adds no row");
        }
    }

    @Test
    void allReturnsPasswordsOldestFirst() throws Exception {
        try (PasswordStore store = PasswordStore.create(database())) {
            store.storeIfAbsent("one");
            store.storeIfAbsent("two");
            store.storeIfAbsent("three");

            assertEquals(List.of("one", "two", "three"), store.all());
        }
    }

    private static void execute(Path dbFile, String... statements)
            throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
             Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.executeUpdate(sql);
            }
        }
    }
}
