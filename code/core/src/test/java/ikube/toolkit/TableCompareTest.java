package ikube.toolkit;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

/**
 * NOTE: The primary key column(the first column) is excluded from the comparison.
 * 
 * This class, although written as a test(for convenience) will test the equality of two database tables.
 * 
 * @author Michael Couck
 * @since 17.07.13
 * @version 01.00
 */
public final class TableCompareTest {

	@Test
	@Ignore
	public void compareTables() throws Exception {
		String url = "jdbc:oracle:thin:@ldap://OID.NETPOST:389/HR1T1,cn=OracleContext,dc=pr,dc=netpost,dc=be";
		String user = "EHR_PLA_USER";
		String password = "TOGETHER2";
		Connection connection = getConnection(url, user, password, "oracle.jdbc.driver.OracleDriver");

		ResultSet sourceResultSet = getResultSet(connection, "rep_assignment_o");
		ResultSet targetResultSet = getResultSet(connection, "rep_assignment_fixed");
		Map<Long, String> sourceIdHash = new HashMap<Long, String>();
		Map<Long, String> targetIdHash = new HashMap<Long, String>();

		try {
			long rows = 0;
			do {
				if (sourceResultSet.next()) {
					if (targetResultSet.next()) {
						// Compare the lines
						long sourceHash = hash(getRowValues(sourceResultSet, sourceResultSet.getMetaData()));
						long targetHash = hash(getRowValues(targetResultSet, targetResultSet.getMetaData()));

						sourceIdHash.put(sourceHash, sourceResultSet.getString(1));
						targetIdHash.put(targetHash, targetResultSet.getString(1));

						if (targetIdHash.containsKey(sourceHash)) {
							targetIdHash.remove(sourceHash);
							sourceIdHash.remove(sourceHash);
						}
						if (sourceIdHash.containsKey(targetHash)) {
							sourceIdHash.remove(targetHash);
							targetIdHash.remove(targetHash);
						}
					} else {
						// Add the source row
						long sourceHash = hash(getRowValues(sourceResultSet, sourceResultSet.getMetaData()));
						sourceIdHash.put(sourceHash, sourceResultSet.getString(1));
					}
				} else {
					if (targetResultSet.next()) {
						// Add the target row
						long targetHash = hash(getRowValues(targetResultSet, targetResultSet.getMetaData()));
						targetIdHash.put(targetHash, targetResultSet.getString(1));
					} else {
						break;
					}
				}
				if (rows++ % 10000 == 0) {
					System.out.println("Rows : " + rows);
				}
			} while (true);
		} finally {
			closeAll(sourceResultSet);
			closeAll(targetResultSet);
		}

		for (final Map.Entry<Long, String> mapEntry : sourceIdHash.entrySet()) {
			if (targetIdHash.containsKey(mapEntry.getKey())) {
				targetIdHash.remove(mapEntry.getKey());
				continue;
			}
			System.out.println("Not in target : " + mapEntry.getValue());
		}
		for (final Map.Entry<Long, String> mapEntry : targetIdHash.entrySet()) {
			if (sourceIdHash.containsKey(mapEntry.getKey())) {
				sourceIdHash.remove(mapEntry.getKey());
				continue;
			}
			System.out.println("Not in source : " + mapEntry.getValue());
		}

		System.out.println("In source and not target : " + sourceIdHash.size());
		System.out.println("In target and not source : " + targetIdHash.size());
	}

	private ResultSet getResultSet(final Connection connection, final String tableName) {
		String query = "select * from " + tableName + " order by pdb_key, organization_code, service_littera, day, resource_category";
		return executeQuery(connection, query);
	}

	private Object[] getRowValues(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData) throws SQLException {
		List<Object> rowValues = new ArrayList<Object>();
		for (int i = 2; i < resultSetMetaData.getColumnCount(); i++) {
			rowValues.add(resultSet.getObject(i));
		}
		return rowValues.toArray(new Object[rowValues.size()]);
	}

	private final Connection getConnection(final String url, final String user, final String password, final String driverClass) {
		try {
			Driver driver = (Driver) Class.forName(driverClass).newInstance();
			DriverManager.registerDriver(driver);
			return DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final ResultSet executeQuery(final Connection connection, final String query) {
		try {
			return connection.createStatement().executeQuery(query);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private final Long hash(final Object... objects) {
		StringBuilder builder = new StringBuilder();
		for (Object object : objects) {
			builder.append(object);
		}
		return hash(builder.toString());
	}

	public Long hash(final String string) {
		// Must be prime of course
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			hash = (hash * seed) + chars[i];
		}
		return Long.valueOf(Math.abs(hash));
	}

	private void closeAll(final ResultSet resultSet) {
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
			e.printStackTrace();
		}
		close(resultSet);
		close(statement);
		close(connection);
	}

	private void close(final Statement statement) {
		if (statement == null) {
			return;
		}
		try {
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void close(final Connection connection) {
		if (connection == null) {
			return;
		}
		try {
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void close(final ResultSet resultSet) {
		if (resultSet == null) {
			return;
		}
		try {
			resultSet.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
