package ikube.database;

import ikube.toolkit.Logging;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * General database operations like closing result sets etc.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-12-2010
 */
public final class DatabaseUtilities {

    static {
        Logging.configure();
    }

    private static final Logger LOGGER = Logger.getLogger(DatabaseUtilities.class);

    @SuppressWarnings("UnusedDeclaration")
    public static Connection getConnection(
            final String url,
            final String user,
            final String password,
            final Class<? extends Driver> driverClass) {
        try {
            DriverManager.registerDriver(driverClass.newInstance());
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static ResultSet executeQuery(final Connection connection, final String query) {
        try {
            return connection.createStatement().executeQuery(query);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void searchDatabase(final Connection connection, final String string) {
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tablesResultSet = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tablesResultSet.next()) {
                try {
                    Object tableName = tablesResultSet.getObject("TABLE_NAME");
                    LOGGER.info("Table : " + tableName);
                    String sql = "select * from " + tableName;
                    ResultSet resultSet = connection.createStatement().executeQuery(sql);
                    while (resultSet.next()) {
                        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                            Object columnValue = resultSet.getObject(i);
                            if (columnValue != null && columnValue.toString().contains(string)) {
                                LOGGER.info("Table : " + tableName + " : " + resultSet.getObject(1));
                            }
                        }
                    }
                } catch (final Exception e) {
                    LOGGER.error(null, e);
                }
            }
        } catch (final Exception e) {
            LOGGER.error(null, e);
        } finally {
            close(connection);
        }
    }

    /**
     * Executes an arbitrary sql statement against the database.
     *
     * @param dataSource the data source to get the connection from
     * @param sql        the sql to execute
     */
    public static void executeStatement(final DataSource dataSource, final String sql) {
        LOGGER.debug("Executing statement : " + sql + ", on data source : " + dataSource);
        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            boolean result = statement.execute(sql);
            LOGGER.debug("Result from statement : " + result);
        } catch (final Exception e) {
            LOGGER.error("Exception executing statement : " + sql + ", on data source : " + dataSource);
            LOGGER.debug(null, e);
        } finally {
            close(statement);
            close(connection);
        }
    }

    /**
     * This method will close all related resources to the category set object in the parameter list. First getting the statement from the category set, then
     * the connection from the statement and closing them, category set, statement then connection.
     *
     * @param resultSet the category set and related database resources to close
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
        } catch (final Exception e) {
            LOGGER.error("Exception getting the statement and connection from the category set : ", e);
        }
        close(resultSet);
        close(statement);
        close(connection);
    }

    /**
     * This method just closes the statement.
     *
     * @param statement the statement to close
     */
    public static void close(final Statement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (final Exception e) {
            LOGGER.error("Exception closing the statement : ", e);
        }
    }

    /**
     * This method just closes the connection.
     *
     * @param connection the connection to close
     */
    public static void close(final Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (final Exception e) {
            LOGGER.error("Exception closing the connection : ", e);
        }
    }

    /**
     * This method closes the category set.
     *
     * @param resultSet the category set to close
     */
    public static void close(final ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }
        try {
            resultSet.close();
        } catch (final Exception e) {
            LOGGER.error("Exception closing the category set : ", e);
        }
    }

    /**
     * Commits the connection, only if the auto commit has been set to false, i.e. the user will manually commit the connection.
     *
     * @param connection the connection to commit
     */
    public static void commit(Connection connection) {
        if (connection == null) {
            LOGGER.warn("Connection null : ");
            return;
        }
        try {
            if (connection.isClosed()) {
                LOGGER.info("Connection already closed : " + connection);
                return;
            }
            if (!connection.getAutoCommit()) {
                connection.commit();
            } else {
                LOGGER.warn("Can't commit the connection as it is not user comitted : " + connection);
            }
        } catch (final Exception e) {
            LOGGER.error("Exception comitting the connection : " + connection, e);
        }
    }

    /**
     * This method just returns all the column names for a particular table.
     *
     * @param connection the connection to the database
     * @param table      the name of the table to get the columns for
     * @return the list of all columns for the table
     */
    @SuppressWarnings("UnusedDeclaration")
    public static List<String> getAllColumns(final Connection connection, final String table) {
        List<String> columnNames = new ArrayList<>();
        ResultSet columnsResultSet = null;
        try {
            String tableName;
            String databaseName = connection.getMetaData().getDatabaseProductName();
            if (databaseName.toLowerCase().contains("postgre") || databaseName.toLowerCase().contains("mysql")) {
                // No upper case for Postgres or MySQL!
                tableName = table;
            } else {
                // Oracle and Db2 are fine with upper case
                tableName = table.toUpperCase();
            }
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            columnsResultSet = databaseMetaData.getColumns(null, null, tableName, null);
            while (columnsResultSet.next()) {
                Object columnValue = columnsResultSet.getObject("COLUMN_NAME");
                columnNames.add(columnValue.toString());
            }
        } catch (final SQLException e) {
            LOGGER.error("Exception getting the column names for table : " + table, e);
        } finally {
            close(columnsResultSet);
        }
        return columnNames;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static List<String> getPrimaryKeys(final Connection connection, final String table) {
        DatabaseMetaData databaseMetaData;
        ResultSet primaryKeyResultSet = null;
        List<String> primaryKeyColumns = new ArrayList<>();
        try {
            databaseMetaData = connection.getMetaData();
            primaryKeyResultSet = databaseMetaData.getPrimaryKeys(null, null, table);
            while (primaryKeyResultSet.next()) {
                Object columnName = primaryKeyResultSet.getObject("COLUMN_NAME");
                primaryKeyColumns.add(columnName.toString());
            }
        } catch (final SQLException e) {
            LOGGER.error("Exception getting the primary keys for table : " + table, e);
        } finally {
            close(primaryKeyResultSet);
        }
        return primaryKeyColumns;
    }

    public static void printResultSet(final ResultSet resultSet) {
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 1; i < resultSetMetaData.getColumnCount(); i++) {
                    String columnName = resultSetMetaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    stringBuilder.append(columnName);
                    stringBuilder.append("=");
                    stringBuilder.append(columnValue);
                    stringBuilder.append("\n");
                }
                LOGGER.warn(stringBuilder.toString());
            }
        } catch (final SQLException e) {
            LOGGER.error("Exception printing the category set : ", e);
        } finally {
            close(resultSet);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static List<String[]> getForeignKeys(final Connection connection, final String table) {
        List<String[]> foreignKeys = new ArrayList<>();
        DatabaseMetaData databaseMetaData;
        ResultSet importedKeys = null;
        try {
            databaseMetaData = connection.getMetaData();
            importedKeys = databaseMetaData.getImportedKeys(connection.getCatalog(), null, table);
            while (importedKeys.next()) {
                printResultSet(importedKeys);
                // String fkTableName = importedKeys.getString("FKTABLE_NAME");
                // String fkColumnName = importedKeys.getString("FKCOLUMN_NAME");
                // String[] key = { fkTableName, fkColumnName };
                // foreignKeys.add(key);
            }
        } catch (final SQLException e) {
            LOGGER.error("Exception getting the foreign keys : " + table, e);
        } finally {
            close(importedKeys);
        }
        return foreignKeys;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static Object getFieldValue(final Field field, final Object object) {
        try {
            return field.get(object);
        } catch (final Exception e) {
            LOGGER.error("Exception accessing field : " + field, e);
        }
        return null;
    }

    /**
     * Singularity.
     */
    private DatabaseUtilities() {
        // Documented
    }

}