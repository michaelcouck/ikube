package ikube.toolkit;

import ikube.logging.Logging;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Id;

import org.apache.log4j.Logger;

public class DatabaseUtilities {

	private static Logger LOGGER = Logger.getLogger(DatabaseUtilities.class);
	private static Map<Class<?>, Field> idFields = new HashMap<Class<?>, Field>();

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

	public static <T> void setIdField(T object, long id) {
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

	public static Field getIdField(Class<?> klass, Class<?> superKlass) {
		Field idField = idFields.get(klass);
		if (idField != null) {
			return idField;
		}
		Field[] fields = superKlass != null ? superKlass.getDeclaredFields() : klass.getDeclaredFields();
		for (Field field : fields) {
			Id idAnnotation = field.getAnnotation(Id.class);
			if (idAnnotation != null) {
				idFields.put(klass, field);
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

	public static <T> Object getIdFieldValue(T object) {
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

	public static String getIdFieldName(Class<?> klass) {
		Field field = getIdField(klass, null);
		return field != null ? field.getName() : null;
	}

}
