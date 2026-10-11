package org.mytool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * The passwords generated so far, kept in a SQLite file.
 *
 * <p>Uniqueness is a constraint rather than a lookup: an insert either takes
 * the password or reports that it was already there, so no separate read is
 * needed and there is no window between checking and writing.
 *
 * <p>Only {@link #create} writes. {@link #open} is read-only, so pointing the
 * viewer at a database that belongs to something else cannot alter it.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
final class PasswordStore implements AutoCloseable {

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS prevPass (id INTEGER PRIMARY KEY AUTOINCREMENT, password TEXT)";

    /**
     * Databases written by earlier versions could hold a password more than
     * once, which would make the unique index below unbuildable. Keep the
     * oldest row of each password and drop the rest.
     */
    private static final String DROP_DUPLICATES =
            "DELETE FROM prevPass WHERE id NOT IN (SELECT MIN(id) FROM prevPass GROUP BY password)";

    /** A plain index cannot back ON CONFLICT, so the schema needs a unique one. */
    private static final String CREATE_UNIQUE_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS uq_prevpass_password ON prevPass (password)";

    /** Earlier versions used a plain index; the unique one supersedes it. */
    private static final String DROP_LEGACY_INDEX = "DROP INDEX IF EXISTS idx_password";

    private static final String TABLE_EXISTS =
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'prevPass'";

    private static final String INSERT_IF_ABSENT =
            "INSERT INTO prevPass (password) VALUES (?) ON CONFLICT(password) DO NOTHING";

    private static final String SELECT_ALL = "SELECT password FROM prevPass ORDER BY id";

    private final Connection connection;

    private PasswordStore(Connection connection) {
        this.connection = connection;
    }

    /**
     * Opens the store, creating the database file and its schema when absent.
     *
     * @param dbFile where the database lives
     */
    static PasswordStore create(Path dbFile) throws SQLException, ClassNotFoundException {
        Connection connection = connect(dbFile);
        try {
            initialise(connection);
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return new PasswordStore(connection);
    }

    /**
     * Opens a store that already exists, without writing to it. Creating a
     * database is a write, so a path the user mistyped fails rather than
     * quietly producing an empty file, and a file belonging to some other
     * program is left exactly as it was found.
     *
     * @param dbFile where the database lives
     * @throws UsageException when the file is missing or holds no password table
     */
    static PasswordStore open(Path dbFile) throws SQLException, ClassNotFoundException {
        if (!Files.exists(dbFile)) {
            throw new UsageException(dbFile + " does not exist");
        }
        Connection connection = connect(dbFile);
        if (!holdsPasswordTable(connection)) {
            connection.close();
            throw new UsageException(dbFile + " holds no password table");
        }
        return new PasswordStore(connection);
    }

    /**
     * Stores the password if the store does not already hold it.
     *
     * @return true when this password was newly stored, false when it was
     *         already there
     */
    boolean storeIfAbsent(String password) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(INSERT_IF_ABSENT)) {
            insert.setString(1, password);
            return insert.executeUpdate() == 1;
        }
    }

    /**
     * @return every stored password, oldest first
     */
    List<String> all() throws SQLException {
        List<String> passwords = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(SELECT_ALL)) {
            while (rows.next()) {
                passwords.add(rows.getString("password"));
            }
        }
        return passwords;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    private static void initialise(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_TABLE);
            statement.executeUpdate(DROP_DUPLICATES);
            statement.executeUpdate(CREATE_UNIQUE_INDEX);
            statement.executeUpdate(DROP_LEGACY_INDEX);
        }
    }

    private static boolean holdsPasswordTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(TABLE_EXISTS)) {
            return rows.next();
        }
    }

    private static Connection connect(Path dbFile) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    }
}
