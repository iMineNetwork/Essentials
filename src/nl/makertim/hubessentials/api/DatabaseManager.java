package nl.makertim.hubessentials.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

	private static final int PORT = 3306;

	private Connection connection;
	private String username, password, database;

	public DatabaseManager(String database, String username, String password) {
		this.database = database;
		this.username = username;
		this.password = password;
	}

	public boolean openConnection() {
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:" + PORT + "/" + database, username,
					password);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
		return true;
	}

	public boolean closeConnection() {
		try {
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			connection = null;
		}
		return true;

	}

	public static String prepareString(Object raw) {
		return raw.toString().replaceAll(
				"\\{ | \\} | \\\\ | \\, | \\& | \\? | \\( | \\) | \\[ | \\] | \\- | \\; | \\~ | \\| | \\ $ |"
						+ " \\! | \\< | \\> | \\* | \\% | \\_ | \\' | \\\"",
				"\\\\$0");
	}

	public boolean executeQuery(String query) {
		try {
			if (connection.isClosed()) {
				this.openConnection();
			}
			Statement statement = connection.createStatement();
			statement.executeQuery(query);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
		return true;
	}

	public ResultSet doQuery(String query) {
		ResultSet result = null;
		try {
			openIfNotClosed();
			Statement statement = connection.createStatement();
			result = statement.executeQuery(query);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return result;
	}

	public ResultSet insertQuery(String query) {
		ResultSet result = null;
		try {
			openIfNotClosed();
			Statement statement = connection.createStatement();
			statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			result = statement.getGeneratedKeys();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return result;
	}

	public ResultSet updateQuery(String query) {
		ResultSet result = null;
		try {
			openIfNotClosed();
			Statement statement = connection.createStatement();
			statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			result = statement.getGeneratedKeys();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return result;
	}

	public boolean deleteQuery(String query) {
		try {
			openIfNotClosed();
			Statement statement = connection.createStatement();
			statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
		return true;
	}

	private boolean openIfNotClosed() throws SQLException {
		if (connection == null || connection.isClosed()) {
			return this.openConnection();
		}
		return true;
	}
}