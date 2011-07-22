package ikube.toolkit;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Id;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * General database operations like closing result sets etc.
 * 
 * @author Michael Couck
 * @since 23.12.10
 * @version 01.00
 */
public final class DatabaseUtilities {

	private static final Logger LOGGER = Logger.getLogger(DatabaseUtilities.class);
	private static final Map<Class<?>, Field> ID_FIELDS = new HashMap<Class<?>, Field>();

	private DatabaseUtilities() {
	}

	public static void executeStatement(DataSource dataSource, String sql) {
		// String sql = "create sequence if not exists persistable";
		LOGGER.info("Executing statement : " + sql + ", on data source : " + dataSource);
		Connection connection = null;
		Statement statement = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			int result = statement.executeUpdate(sql);
			LOGGER.info("Result from statement : " + result);
		} catch (Exception e) {
			LOGGER.error("Exception executing statement : " + sql + ", on data source : " + dataSource, e);
		} finally {
			close(statement);
			close(connection);
		}
	}

	/**
	 * This method will close all related resources to the result set object in the parameter list. First getting the statement from the
	 * result set, then the connection from the statement and closing them, result set, statement then connection.
	 * 
	 * @param resultSet
	 *            the result set and related database resources to close
	 */
	public static void closeAll(final ResultSet resultSet) {
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

	/**
	 * This method just closes the statement.
	 * 
	 * @param statement
	 *            the statement to close
	 */
	public static void close(final Statement statement) {
		if (statement == null) {
			return;
		}
		try {
			statement.close();
		} catch (Exception e) {
			LOGGER.error("Exception closing the statement : ", e);
		}
	}

	/**
	 * This method just closes the connection.
	 * 
	 * @param connection
	 *            the connection to close
	 */
	public static void close(final Connection connection) {
		if (connection == null) {
			return;
		}
		try {
			connection.close();
		} catch (Exception e) {
			LOGGER.error("Exception closing the connection : ", e);
		}
	}

	public static void commit(Connection connection) {
		if (connection == null) {
			LOGGER.warn("Connection null : ");
			return;
		}
		try {
			if (connection.getAutoCommit()) {
				LOGGER.warn("Can't commit the connection as it is not user comitted : " + connection);
				return;
			}
			connection.commit();
		} catch (Exception e) {
			LOGGER.error("Exception comitting the connection : " + connection, e);
		}
	}

	/**
	 * This method closes the result set.
	 * 
	 * @param resultSet
	 *            the result set to close
	 */
	public static void close(final ResultSet resultSet) {
		if (resultSet == null) {
			return;
		}
		try {
			resultSet.close();
		} catch (Exception e) {
			LOGGER.error("Exception closing the result set : ", e);
		}

	}

	/**
	 * This method will look into an object and try to find the field that is the id field in the object, then set it with the id specified
	 * in the parameter list.
	 * 
	 * @param <T>
	 *            the type of object to set the id field for
	 * @param object
	 *            the object to set the id field for
	 * @param id
	 *            the id to set in the object
	 */
	public static <T> void setIdField(final T object, final long id) {
		if (object == null) {
			return;
		}
		Field idField = getIdField(object.getClass(), null);
		if (idField != null) {
			try {
				idField.set(object, id);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Can't set the id : " + id + ", in the field : " + idField + ", of object : " + object, e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Field not accessible : " + idField, e);
			}
		} else {
			LOGGER.warn("No id field defined for object : " + object);
		}
	}

	public static Field getIdField(final Class<?> klass, final Class<?> superKlass) {
		Field idField = ID_FIELDS.get(klass);
		if (idField != null) {
			return idField;
		}
		Field[] fields = superKlass != null ? superKlass.getDeclaredFields() : klass.getDeclaredFields();
		for (Field field : fields) {
			Id idAnnotation = field.getAnnotation(Id.class);
			if (idAnnotation != null) {
				ID_FIELDS.put(klass, field);
				field.setAccessible(Boolean.TRUE);
				return field;
			}
		}
		// Try the super classes
		Class<?> superClass = superKlass != null ? superKlass.getSuperclass() : klass.getSuperclass();
		if (superClass != null && !Object.class.getName().equals(superClass.getName())) {
			return getIdField(klass, superClass);
		}
		return null;
	}

	public static <T> Object getIdFieldValue(final T object) {
		if (object == null) {
			return null;
		}
		Field idField = getIdField(object.getClass(), null);
		if (idField != null) {
			try {
				return idField.get(object);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Can't get the id in the field : " + idField + ", of object : " + object, e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Field not accessible : " + idField, e);
			}
		} else {
			LOGGER.info(Logging.getString("Id field not found for object : ", object.getClass().getName()));
		}
		return null;
	}

	public static String getIdFieldName(final Class<?> klass) {
		Field field = getIdField(klass, null);
		return field != null ? field.getName() : null;
	}

}
