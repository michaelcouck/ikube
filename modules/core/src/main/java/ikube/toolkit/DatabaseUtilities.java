package ikube.toolkit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class DatabaseUtilities {

	private static Logger LOGGER = Logger.getLogger(DatabaseUtilities.class);

	public static void closeAll(ResultSet resultSet) {
		Statement statement = null;
		Connection connection = null;
		try {
			if (resultSet != null) {
				statement = resultSet.getStatement();
			}
			if (statement != null) {
				connection = statement.getConnection();
			}
		} catch (Exception e) {
			LOGGER.error("Exception getting the statement and connection from the result set : ", e);
		}
		close(resultSet);
		close(statement);
		close(connection);
	}

	public static void close(Statement statement) {
		if (statement == null) {
			return;
		}
		try {
			statement.close();
		} catch (Exception e) {
			LOGGER.error("Exception closing the statement : ", e);
		}
	}

	public static void close(Connection connection) {
		if (connection == null) {
			return;
		}
		try {
			connection.close();
		} catch (Exception e) {
			LOGGER.error("Exception closing the connection : ", e);
		}
	}

	public static void close(ResultSet resultSet) {
		if (resultSet == null) {
			return;
		}
		try {
			resultSet.close();
		} catch (Exception e) {
			LOGGER.error("Exception closing the result set : ", e);
		}

	}

}
