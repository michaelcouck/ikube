package ikube.toolkit;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IMetadataHandler;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.operation.DatabaseOperation;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This utility class has methods to insert data into the database.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03.07.2011
 */
@SuppressWarnings("UnusedDeclaration")
public final class DataUtilities {

    static {
        Logging.configure();
    }

    private static Logger LOGGER = Logger.getLogger(DataUtilities.class);
    private static IDataTypeFactory DATA_TYPE_FACTORY;
    private static IMetadataHandler METADATA_HANDLER;

    public static void setDataTypeFactory(final IDataTypeFactory dataTypeFactory) {
        DATA_TYPE_FACTORY = dataTypeFactory;
    }

    public static void setMetadataHandler(IMetadataHandler metadataHandler) {
        METADATA_HANDLER = metadataHandler;
    }

    /**
     * This method inserts data from a DBUnit file into the database defined by the url in the parameter list.
     *
     * @param url      the url for the database
     * @param userid   the userid for the database
     * @param password the password for the database
     * @param filePath the relative or absolute file path for the DBUnit data file. If the path is relative, for example './folder/file.xml' then note that this
     *                 is relative to where the Jvm starts
     */
    public static void insertData(final String url, final String userid, final String password, final String filePath) {
        try {
            IDataSet dataSet = getDataSet(filePath);
            insertData(url, userid, password, dataSet);
        } catch (final Exception e) {
            LOGGER.error("Exception : " + filePath, e);
        }
    }

    public static void insertData(final String url, final String userid, final String password, final InputStream inputStream) {
        try {
            IDataSet dataSet = getDataSet(inputStream);
            insertData(url, userid, password, dataSet);
        } catch (final Exception e) {
            LOGGER.error("Exception : " + inputStream, e);
        }
    }

    private static void insertData(final String url, final String userid, final String password, IDataSet dataSet) {
        IDatabaseConnection databaseConnection = null;
        try {
            databaseConnection = getDatabaseConnection(url, userid, password);
            DatabaseOperation.INSERT.execute(databaseConnection, dataSet);
        } catch (final SQLException | DatabaseUnitException e) {
            LOGGER.error("Exception : " + dataSet, e);
        } finally {
            close(databaseConnection);
        }
    }

    /**
     * This method inserts data from a DBUnit file into the database defined by the url in the parameter list.
     *
     * @param connection the connection to the database
     * @param filePath   the relative or absolute file path for the DBUnit data file. If the path is relative, for example './folder/file.xml' then note that this
     *                   is relative to where the Jvm starts
     */
    public static void insertData(final Connection connection, final String filePath) {
        IDatabaseConnection databaseConnection = null;
        try {
            databaseConnection = new DatabaseConnection(connection);
            IDataSet dataSet = getDataSet(filePath);
            DatabaseOperation.INSERT.execute(databaseConnection, dataSet);
        } catch (final SQLException | DatabaseUnitException e) {
            LOGGER.error("Exception : " + filePath, e);
        } finally {
            close(databaseConnection);
        }
    }

    public static void insertData(final Connection connection, final InputStream inputStream) {
        IDatabaseConnection databaseConnection;
        try {
            IDataSet dataSet = getDataSet(inputStream);
            databaseConnection = new DatabaseConnection(connection);
            DatabaseOperation.INSERT.execute(databaseConnection, dataSet);
        } catch (final DatabaseUnitException | SQLException e) {
            LOGGER.error("Exception : " + inputStream, e);
        }
    }

    /**
     * This method deletes the data in the database that corresponds to the data in the DBUnit data file.
     *
     * @param url      the url for the database
     * @param userid   the userid for the database
     * @param password the password for the database
     * @param filePath the relative or absolute file path for the DBUnit data file. If the path is relative, for example './folder/file.xml' then note that this
     *                 is relative to where the Jvm starts
     */
    public static void deleteData(final String url, final String userid, final String password, final String filePath) {
        IDatabaseConnection databaseConnection = null;
        try {
            databaseConnection = getDatabaseConnection(url, userid, password);
            IDataSet dataSet = getDataSet(filePath);
            DatabaseOperation.DELETE.execute(databaseConnection, dataSet);
        } catch (final Exception e) {
            LOGGER.error("Exception : " + filePath, e);
        } finally {
            close(databaseConnection);
        }
    }

    /**
     * This method will first delete the data in the database that corresponds to the data in the DBUnit data file, then insert the data again, essentially
     * acting like a refresh of the data in the data file.
     *
     * @param url      the url for the database
     * @param userid   the userid for the database
     * @param password the password for the database
     * @param filePath the relative or absolute file path for the DBUnit data file. If the path is relative, for example './folder/file.xml' then note that this
     *                 is relative to where the Jvm starts
     */
    public static void deleteInsertData(final String url, final String userid, final String password, final String filePath) {
        deleteData(url, userid, password, filePath);
        insertData(url, userid, password, filePath);
    }

    /**
     * Closes the DBUnit database connection.
     *
     * @param connection the DBUnit connection to close
     */
    public static void close(final IDatabaseConnection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (final Exception e) {
            LOGGER.error("Exception : " + connection, e);
        }
    }

    /**
     * Closes an input stream.
     *
     * @param inputStream the input stream to close
     */
    public static void close(final InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (final Exception e) {
            LOGGER.error("Exception closing stream : " + inputStream, e);
        }
    }

    /**
     * This method first gets a connection to the database, then uses this connection object to create a DBUnit connection.
     *
     * @param url      the url for the database
     * @param userid   the userid for the database
     * @param password the password for the database
     * @return the DBUnit connection
     */
    private static IDatabaseConnection getDatabaseConnection(final String url, final String userid, final String password) {
        boolean exception = Boolean.FALSE;
        IDatabaseConnection databaseConnection = null;
        try {
            Connection connection = getConnection(url, userid, password);
            // connection.createStatement().execute("SET REFERENTIAL_INTEGRITY FALSE;");
            databaseConnection = new DatabaseConnection(connection);
            if (DATA_TYPE_FACTORY != null) {
                databaseConnection.getConfig().setProperty("http://www.dbunit.org/properties/datatypeFactory", DATA_TYPE_FACTORY);
            }
            if (METADATA_HANDLER != null) {
                databaseConnection.getConfig().setProperty("http://www.dbunit.org/properties/metadataHandler", METADATA_HANDLER);
            }
            return databaseConnection;
        } catch (final Exception e) {
            exception = Boolean.TRUE;
            LOGGER.error("Exception : " + url, e);
        } finally {
            if (exception) {
                close(databaseConnection);
            }
        }
        return null;
    }

    /**
     * This method will get a straight connection to the database using the standard driver manager mechanism. Note that this connection will not be
     * transactional.
     *
     * @param url      the url for the database
     * @param userid   the userid for the database
     * @param password the password for the database
     * @return the database connection or null if there is an exception
     */
    public static Connection getConnection(final String url, final String userid, final String password) {
        try {
            return DriverManager.getConnection(url, userid, password);
        } catch (final Exception e) {
            LOGGER.error("Exception : " + url, e);
        }
        return null;
    }

    /**
     * This method will get a DBUnit data set based on the file path passed as a parameter. Note that the entire file is read into memory, so it is not a good
     * idea to try this method with very large data sets, like 100 meg for instance.
     *
     * @param filePath the relative or absolute file path for the DBUnit data file. If the path is relative, for example './folder/file.xml' then note that this
     *                 is relative to where the Jvm starts
     * @return the DBUnit data set that can then be inserted into the database
     */
    private static IDataSet getDataSet(String filePath) {
        InputStream inputStream = null;
        try {
            File file = new File(filePath);
            inputStream = new FileInputStream(file);
            return getDataSet(inputStream);
        } catch (final Exception e) {
            LOGGER.error("Exception : " + filePath, e);
        } finally {
            close(inputStream);
        }
        return null;
    }

    private static IDataSet getDataSet(InputStream inputStream) {
        try {
            InputSource inputSource = new InputSource(inputStream);
            FlatXmlProducer flatXmlProducer = new FlatXmlProducer(inputSource, Boolean.FALSE, Boolean.TRUE);
            IDataSet dataSet = new FlatXmlDataSet(flatXmlProducer);
            return new FilteredDataSet(dataSet.getTableNames(), dataSet);
        } catch (final Exception e) {
            LOGGER.error("Exception : " + inputStream, e);
        } finally {
            close(inputStream);
        }
        return null;
    }

    public static void execute(String url, String userid, String password, String filePath) {
        Connection connection = getConnection(url, userid, password);
        File file = FileUtilities.getFile(filePath, Boolean.FALSE);
        String sql = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
        try {
            connection.createStatement().executeUpdate(sql);
        } catch (final Exception e) {
            LOGGER.error("Exception : " + filePath, e);
        } finally {
            DatabaseUtilities.close(connection);
        }
    }

    public static void executeSql(String url, String userid, String password, String sql) {
        Connection connection = getConnection(url, userid, password);
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
        } catch (final Exception e) {
            LOGGER.error("Exception : " + sql, e);
        } finally {
            DatabaseUtilities.close(statement);
            DatabaseUtilities.close(connection);
        }
    }

    public static void export(String url, String userid, String password, String filePath, String... tableNames) {
        try {
            IDatabaseConnection databaseConnection = getDatabaseConnection(url, userid, password);
            QueryDataSet queryDataSet = new QueryDataSet(databaseConnection);
            for (String tableName : tableNames) {
                queryDataSet.addTable(tableName);
            }
            File file = FileUtilities.getFile(filePath, Boolean.FALSE);
            FlatXmlDataSet.write(queryDataSet, new FileOutputStream(file));
        } catch (final Exception e) {
            LOGGER.error("Exception : " + filePath, e);
        }
    }

    public static void exportAll(String url, String userid, String password, String filePath) {
        try {
            IDatabaseConnection databaseConnection = getDatabaseConnection(url, userid, password);
            IDataSet dataSet = databaseConnection.createDataSet();
            String[] tableNames = dataSet.getTableNames();
            for (String tableName : tableNames) {
                LOGGER.info(tableName);
            }
            File file = FileUtilities.getFile(filePath, Boolean.FALSE);
            FlatXmlDataSet.write(dataSet, new FileOutputStream(file));
        } catch (final Exception e) {
            LOGGER.error("Exception : " + filePath, e);
        }
    }

}