package ikube.search;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static org.apache.commons.lang.StringUtils.replace;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the inter/instr/trans search query using straight JDBC, first in a multi client way,
 * clients being simulated by threads, each issuing multiple queries, and a query resulting in high volumes
 * of results, and testing the database io and final memory/performance of the result set.
 * <p/>
 * This test typically will need to be executed manually as the Jenkins test database will typically not
 * have the volumes that are needed to test the query performance and the fetch of the results. There
 * should be at least 10m(one day) records in the database, the database should typically be under load at the time i.e.
 * several thousand records being inserted in the intch/instr/trans tables in parallel to simulate a more realistic
 * production environment.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 06-07-2015
 */
@Ignore
public class GppQueryPerformanceTest {

    /**
     * This is the interface to implement by clients that want to test the performance on a method.
     */
    interface IPerform {

        boolean log();

        void execute() throws Exception;

    }

    public abstract class APerform implements IPerform {

        public boolean log() {
            return Boolean.TRUE;
        }

    }

    private static final String DOMAIN = "gpp/client";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Database connection, either from the driver manager or from Hibernate,
     * which ever is more convenient.
     */
    // private Connection connection;
    @Before
    public void before() throws SQLException {
        // GPPTestUtil.transactionBegin();
        // connection = GPPTestUtil.getHibernateSession().connection();
    }

    @After
    public void after() throws SQLException {
        // connection.close();
        // GPPTestUtil.transactionRollback();
        // connection.close();
    }

    /**
     * Executes multiple queries on the database in several threads to simulate clients.
     */
    @Test
    public void multipleClients() throws InterruptedException {
        logger.error(GppQuery.INTER_INSTRU_TRANS_OPIC_EXCHAN_QUERY);

        final int fetchSize = 10;
        final int incrementClients = 30;
        final int queriesPerClient = 100;

        Thread client = null;
        final Map<Integer, List<Double>> results = Collections.synchronizedMap(new HashMap<Integer, List<Double>>());
        for (int i = 3; i < incrementClients; i += 3) {
            int clients = i;
            results.put(clients, Collections.synchronizedList(new ArrayList<Double>()));
            final int index = clients;
            while (clients-- >= 0) {
                client = new Thread(new Runnable() {
                    public void run() {
                        Connection connection = null;
                        try {
                            connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.1.27:1521:BPH", "BPHADMIN", "password");

                            Object userGroupId = GppQuery.USER_GROUP_IDS[new Random().nextInt(GppQuery.USER_GROUP_IDS.length - 1)];
                            List<Object> parameters = new ArrayList<Object>(Arrays.asList(userGroupId));
                            double queriesPerSecond = performQueries(connection, GppQuery.INTER_INSTRU_TRANS_OPIC_EXCHAN_QUERY, fetchSize, queriesPerClient, parameters);
                            results.get(index).add(queriesPerSecond);
                            logger.debug("Queries per second : " + queriesPerSecond);
                            assertTrue("Must get more than one query per second, in the worst case : ",
                                    queriesPerSecond > 1);
                        } catch (final SQLException e) {
                            throw new RuntimeException(e);
                        } finally {
                            try {
                                if (connection != null) {
                                    connection.close();
                                }
                            } catch (final SQLException e) {
                                logger.error(null, e);
                            }
                        }
                    }
                });
                client.start();
            }
            // We join the last client
            //noinspection ConstantConditions
            client.join();
            Thread.sleep(10000);
        }

        // Build the strings for the graph, the number of clients, the average
        // number of queries per client, and the total number of queries per second aggregating
        // all the clients i.e. number-of-clients * queries-per-second
        Object[][] clientsAverageQueriesPerSecond = new Object[results.size()][];
        Object[][] clientsTotalQueriesPerSecond = new Object[results.size()][];

        int index = 0;
        Set<Integer> numberOfClientsSet = new TreeSet<Integer>(results.keySet());
        for (final Integer numberOfClients : numberOfClientsSet) {
            Object[] clientAverageQueriesPerSecond = new Object[2];
            Object[] clientTotalQueriesPerSecond = new Object[2];

            List<Double> result = results.get(numberOfClients);
            double totalQueriesPerSecond = 0;
            for (final Double value : result)
                totalQueriesPerSecond += value;
            double average = totalQueriesPerSecond / result.size();

            clientAverageQueriesPerSecond[0] = numberOfClients;
            clientAverageQueriesPerSecond[1] = average;

            clientTotalQueriesPerSecond[0] = numberOfClients;
            clientTotalQueriesPerSecond[1] = totalQueriesPerSecond;

            clientsAverageQueriesPerSecond[index] = clientAverageQueriesPerSecond;
            clientsTotalQueriesPerSecond[index] = clientTotalQueriesPerSecond;

            index++;
            logger.debug(replace(replace(result.toString(), "[", ""), "]", ""));
        }

        logger.info(Arrays.deepToString(clientsAverageQueriesPerSecond));
        logger.info(Arrays.deepToString(clientsTotalQueriesPerSecond));
    }

    /**
     * This test fetches 100 000 records from the database and populates a map with them.
     */
    @Test
    public void largeResultSetVolume() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.1.27:1521:BPH", "BPHADMIN", "password");
        Object userGroupId = GppQuery.USER_GROUP_IDS[new Random().nextInt(GppQuery.USER_GROUP_IDS.length - 1)];
        List<Object> parameters = new ArrayList<Object>(Arrays.asList(userGroupId));
        double queriesPerSecond = performQueries(connection, GppQuery.INTER_INSTRU_TRANS_OPIC_EXCHAN_QUERY, 100000, 1, parameters);
        double timeTakenInSeconds = 1d / queriesPerSecond;
        logger.info("Time taken : " + timeTakenInSeconds);
        logger.info("Queries per second : " + queriesPerSecond);
        assertTrue("This must return in a reasonable time : ", timeTakenInSeconds < 600);
    }

    /**
     * Executes a certain number of queries using the connection and query defined.
     *
     * @param connection the connection to the database
     * @param query      the query to execute one or several times
     * @return the number of queries per second
     */
    private double performQueries(final Connection connection, final String query, final int fetchSize,
                                  final int iterations, final List<Object> parameters) {
        final PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement(query,
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.CLOSE_CURSORS_AT_COMMIT);
            preparedStatement.setMaxRows(fetchSize);
            preparedStatement.setFetchSize(fetchSize);
            int index = 1;
            for (final Object parameter : parameters) {
                preparedStatement.setObject(index++, parameter);
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        return execute(new APerform() {
            public void execute() throws SQLException, IOException {
                // Pop everything in a map to check the memory allocation
                Map<String, Object> results = new HashMap<String, Object>();
                ResultSet resultSet = null;
                try {
                    resultSet = preparedStatement.executeQuery();
                    //noinspection StatementWithEmptyBody
                    // ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    //if (resultSet.next()) {
                    //    do {
                    //        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    //            String columnName = resultSetMetaData.getColumnName(i);
                    //            results.put(columnName, resultSet.getObject(columnName));
                    //        }
                    //    } while (resultSet.next());
                    //}
                } finally {
                    //noinspection ConstantConditions
                    resultSet.close();
                }
                //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                //ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                //objectOutputStream.writeObject(results);
                //logger.debug("Memory allocation, more or less : " + byteArrayOutputStream.size() + ", result size : "
                //        + results.size());
            }
        }, "Query performance : ", iterations, Boolean.TRUE);
    }

    /**
     * Executes the perform object a set number of times, prints the duration and iterations per second
     * to the log and returns the number of iterations per second.
     *
     * @param perform    the interface that will call the object to be executed
     * @param type       the type of object to be executed, typically a string that will be printed to the output
     * @param iterations the number of executions to perform
     * @return the number of executions per second
     */
    double execute(final IPerform perform, final String type, final double iterations, final boolean memory) {
        long freeMemory = Runtime.getRuntime().freeMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        double start = System.currentTimeMillis();
        try {
            for (int i = 0; i < iterations; i++) {
                perform.execute();
            }
        } catch (final Exception e) {
            logger.error(null, e);
        }
        double end = System.currentTimeMillis();
        double duration = (end - start) / 1000d;
        double executionsPerSecond = (iterations / duration);
        if (perform.log()) {
            logger.info("Duration : " + duration + ", " + type + " per second : " + executionsPerSecond);
            if (memory) {
                logger.info("Free memory before : " + freeMemory + ", after : "
                        + Runtime.getRuntime().freeMemory());
                logger.info("Max memory before : " + maxMemory + ", after : "
                        + Runtime.getRuntime().maxMemory());
                logger.info("Total memory before : " + totalMemory + ", after : "
                        + Runtime.getRuntime().totalMemory());
            }
        }
        return executionsPerSecond;
    }

}