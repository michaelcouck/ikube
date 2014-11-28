package ikube.database;

import ikube.toolkit.LOGGING;
import org.apache.log4j.Logger;

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
        LOGGING.configure();
    }

    private static final Logger LOGGER = Logger.getLogger(DatabaseUtilities.class);

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

    /**
     * Singularity.
     */
    private DatabaseUtilities() {
        // Documented
    }

}