package ikube.data;

import ikube.database.DatabaseUtilities;
import ikube.toolkit.FILE;
import ikube.toolkit.PERFORMANCE;
import ikube.toolkit.THREAD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * General database operations like closing result sets etc.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-06-2015
 */
public class DatabasePerformance {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePerformance.class);

    private static final String[] USER_GROUP_IDS = {
            "KBC",
            "Raffizen",
            "Credit Agricole",
            "Pireus",
            "Aareal Bank",
            "Aargauische Kanton",
            "AB SEB bankas, V",
            "Abanka Vipa, Lj",
            "ABC Banking Co",
            "ABC Internati",
            "ABC Islamic",
            "ABI Bank",
            "ABLV Bank",
            "ABN AMRO",
            "ABN AMRO Group",
            "Absolut Bank",
            "Absolutbank",
            "Abu Dhabi Comm",
            "Abu Dhabi India",
            "Abu Dhabi Islamic",
            "Abu Dhabi Egypt",
            "ACBA Leasing",
            "ACBA-Credit",
            "Accent Bank",
            "Access Bank Group",
            "AccessBank",
            "Access Bank",
            "Access Bank Ghana",
            "Access Bank Rwanda",
            "AccessBank Azer",
            "Access Bank Zam",
            "Achmea Bank",
            "Achmea Hypo",
            "Acleda Bank",
            "Acleda Bank Laos",
            "Acleda Bank Myan"};

    public static void main(final String[] args) {
        THREAD.initialize();
        Connection connection = null;
        List<Future<Object>> clients = new ArrayList<>();
        try {
            final String csSearchQuery = FILE.findFileRecursivelyAndGetContents(new File("."), "cs-search-query.sql");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.1.27:1521/BPH", "BPHADMIN", "password");
            final Connection clientConnection = connection;
            for (int i = 0; i < 10; i++) {
                Future future = THREAD.submit("cs-search-query", new Runnable() {
                    public void run() {
                        try {
                            double queriesPerSecond = performQuery(clientConnection, csSearchQuery);
                            LOGGER.warn("Queries per second : " + queriesPerSecond);
                        } catch (final SQLException e) {
                            LOGGER.error(null, e);
                        }
                    }
                });
                //noinspection unchecked
                clients.add(future);
            }
        } catch (final SQLException e) {
            LOGGER.error(null, e);
        }
        THREAD.waitForFutures(clients, 60l);
        DatabaseUtilities.close(connection);
    }

    private static double performQuery(final Connection connection, final String csSearchQuery) throws SQLException {

        final PreparedStatement preparedStatement = connection.prepareStatement(
                csSearchQuery,
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT);
        preparedStatement.setFetchSize(10);
        preparedStatement.setString(1, USER_GROUP_IDS[new Random().nextInt(USER_GROUP_IDS.length - 1)]);

        return PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() throws SQLException {
                ResultSet resultSet = null;
                try {
                    // LOGGER.warn("Executing query : ");
                    resultSet = preparedStatement.executeQuery();
                    //noinspection StatementWithEmptyBody
                    // while (resultSet.next()) ;
                } finally {
                    DatabaseUtilities.close(resultSet);
                }
            }
        }, "Query performance : ", 100, Boolean.TRUE);
    }

}