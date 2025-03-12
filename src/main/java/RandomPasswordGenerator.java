import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * Generate random password class
 * @author li1345825138
 * @date 11/7/2024
 */
public class RandomPasswordGenerator implements AutoCloseable {
    // password symbols
    private static final String symbols = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz0123456789!#$@";

    // password length
    private int length;

    // sql connection
    private Connection sqlConnection;

    // sql execute statement
    private Statement statement;

    /**
     * Get SQLite Driver connection
     * @param location the location of sqlite database
     * @throws ClassNotFoundException throw exception if JDBC is not found
     * @throws SQLException throw exception if can't get database location
     */
    private Connection getSQLiteConnection(String location) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String formatURL = String.format("jdbc:sqlite:%s", location);
        return DriverManager.getConnection(formatURL);
    }

    /**
     * Store new random password into database
     * @param randPassword new random password
     */
    private void updateDatabase(String randPassword) throws SQLException {
        String insertStatement = "INSERT INTO prevPass (password) VALUES (?)";
        PreparedStatement preparedStatement = this.sqlConnection.prepareStatement(insertStatement);
        preparedStatement.setString(1, randPassword);
        preparedStatement.executeUpdate();
    }

    /**
     * Check if current random password is already on database
     * @param randPassword new random password
     * @return true if already exists on database, otherwise false
     * @throws SQLException throw if any sql exception cause
     */
    private boolean isOnDatabase(String randPassword) throws SQLException {
        if (randPassword == null || randPassword.isEmpty()) return true;
        String existsQuery = "SELECT EXISTS (SELECT 1 FROM prevPass WHERE password=?)";
        PreparedStatement preparedStatement = this.sqlConnection.prepareStatement(existsQuery);
        preparedStatement.setString(1, randPassword);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) == 1;
    }

    /**
     * Create database if not exists
     * @throws SQLException
     */
    private void createTableIfNotExists() throws SQLException {
        this.statement.executeUpdate("CREATE TABLE IF NOT EXISTS prevPass (id INTEGER PRIMARY KEY AUTOINCREMENT, password TEXT)");
        this.statement.executeUpdate("CREATE INDEX idx_password ON prevPass (password)");
    }

    /**
     * Constructor the random pass class
     * @param length The length of random password
     */
    public RandomPasswordGenerator(int length) throws SQLException, ClassNotFoundException {
        this.length = (Math.max(length, 8) >= 1024) ? 1024 : length;
        File file = new File("./database.sql");
        if (!file.exists()) {
            this.sqlConnection = getSQLiteConnection("./database.sql");
            this.statement = this.sqlConnection.createStatement();
            createTableIfNotExists();
        } else {
            this.sqlConnection = getSQLiteConnection("./database.sql");
            this.statement = this.sqlConnection.createStatement();
        }
    }

    /**
     * Generate random password
     * @return random password
     */
    public String generateRandomPassword() throws SQLException {
        StringBuilder sb = new StringBuilder();
        Random random = new Random(System.currentTimeMillis());
        int symbolLength = symbols.length();
        char currChar;
        String randomPass;
        int index;
        do {
            for (int i = 0; i < this.length; i++) {
                index = random.nextInt(symbolLength);
                currChar = symbols.charAt(index);
                sb.append(currChar);
            }
            randomPass = sb.toString();
        } while (isOnDatabase(randomPass));
        updateDatabase(randomPass);
        return randomPass;
    }

    /**
     * Close sql resources
     * @throws SQLException
     */
    public void close() throws SQLException {
        if (this.statement != null) {
            this.statement.close();
        }
        if (this.sqlConnection != null) {
            this.sqlConnection.close();
        }
    }
}
