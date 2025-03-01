import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * View sqlite database
 * @author li1345825138
 * @date 2025/2/28
 */
public class SqliteViewer implements AutoCloseable {
	private Connection sqliteConnection;
	private Statement statement;

	/**
	 * Get sqlite connection
	 * @param location location of sqlite database
	 * @return sqlite database connection
	 * @throws ClassNotFoundException throw exception if can't get database location
	 * @throws SQLException throw if any sql exception cause
	 */
	private Connection getSQLiteConnection(String location) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		String formatURL = String.format("jdbc:sqlite:%s", location);
		return DriverManager.getConnection(formatURL);
	}

	/**
	 * View sqlite database content in console
	 * @throws SQLException throw exception if any exception cause
	 */
	public void viewSqliteDatabase() throws SQLException {
		String sql = "SELECT * FROM prevPass";
		try (ResultSet rs = statement.executeQuery(sql)) {
			while (rs.next()) {
				System.out.println(rs.getString("password"));
			}
		}

		sql = "SELECT COUNT(*) AS total FROM prevPass";
		try (ResultSet countRs = statement.executeQuery(sql)) {
			if (countRs.next()) {
				System.out.println("Total Records: " + countRs.getInt("total"));
			}
		}
	}

	/**
	 * Constructor
	 * @param location location of sqlite database
	 * @throws SQLException throw exception if can't get database location
	 * @throws ClassNotFoundException throw exception if can't get database location
	 */
	public SqliteViewer(String location) throws SQLException, ClassNotFoundException {
		File file = new File(location);
		if (file.exists()) {
			sqliteConnection = getSQLiteConnection(location);
			statement = sqliteConnection.createStatement();
		}
	}

	/**
	 * Close connection
	 * @throws Exception throw exception if any exception cause
	 */
	@Override
	public void close() throws Exception {
		if (statement != null) {
			statement.close();
		}
		if (sqliteConnection != null) {
			sqliteConnection.close();
		}
	}
}
