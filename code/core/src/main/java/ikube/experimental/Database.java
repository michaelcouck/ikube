package ikube.experimental;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

/**
 * Reads the database finds all changed records, inserts, updates and deletes.
 * <p/>
 * The ssh tunnel works as follows:
 * <p/>
 * <pre>
 *     1) Open a tunnel from local port(let say 10000) => ikube.be remote port(and say 443)
 *     2) Open a database connection, which will go to the local port, i.e. in this case 10 000
 *     3) On the target ssh machine we get to port 443, but the port forwarding forwards to 1521
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Component
@Configuration
public class Database {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Ssh components, to be used for the ssh tunnel **
     */

    private JSch jsch;
    private Properties config;
    private Session session;

    /**
     * SSH credentials and properties **
     */

    // Can be any port, the start port for the ssh tunnel
    @Value("${local-port:10000}")
    private int localPort = 10000;

    // The port that ssh is listening on the target machine
    @Value("${target-ssh-port:22}")
    private int targetSshPort = 22;

    // The userid for the ssh tunnel, i.e. the operating system userid
    @Value("${ssh-userid:laptop}")
    private String sshUserid = "laptop";

    // The password for the ssh tunnel, i.e. the operating system password
    @Value("${ssh-password:caherline}")
    private String sshPassword = "caherline";

    /**
     * Database credentials and items **
     */

    // The remote host for the tunnel and the database
    @Value("${remote-host-for-ssh-and-database:localhost}")
    private String remoteHostForSshAndDatabase = "localhost";

    // The database userid
    @Value("${database-userid:sa}")
    private String userid = "sa";

    // The database password
    @Value("${database-password:}")
    String password = "";

    // Must be the target port for the database
    @Value("${database-port:8082}")
    private int databasePort = 8082;

    // The url for the database
    @Value("${database-url:jdbc:h2:tcp://localhost:8043/mem:ikube;DB_CLOSE_ON_EXIT=TRUE}")
    private String url = "jdbc:h2:tcp://localhost:8043/mem:ikube;DB_CLOSE_ON_EXIT=TRUE";
    // And this we get from the driver
    private Connection connection;

    // The last timestamp for data that was indexed
    private Timestamp modification;

    public Database() {
        jsch = new JSch();
        config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        modification = new Timestamp(0);

        logger.info("Database : " + this);
    }

    List<Map<Object, Object>> readChangedRecords() throws SQLException, JSchException {
        createSshTunnel();
        createDatabaseConnection();

        List<Map<Object, Object>> results = new ArrayList<>();

        long time = System.currentTimeMillis();
        Timestamp previousTimestamp = modification;
        // Reset the last modification timestamp
        modification = new Timestamp(time);

        String sql = "SELECT * FROM rule WHERE timestamp >= ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setTimestamp(1, previousTimestamp);

        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        while (resultSet.next()) {
            Map<Object, Object> row = new HashMap<>();
            for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
                Object columnName = resultSetMetaData.getColumnName(columnIndex);
                Object columnValue = resultSet.getObject(columnIndex);
                row.put(columnName, columnValue);
            }
            results.add(row);
            if (results.size() % 10000 == 0) {
                logger.info("Adding row : " + results.size() + ", " + row);
            }
        }

        return results;
    }

    void createDatabaseConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, userid, password);
            logger.info("Connected JDBC through ssh tunnel : ");
        }
    }

    void createSshTunnel() throws JSchException {
        if (session == null || !session.isConnected()) {
            session = jsch.getSession(sshUserid, remoteHostForSshAndDatabase, targetSshPort);
            session.setPassword(sshPassword);
            session.setConfig(config);
            session.connect();
            session.setPortForwardingL(localPort, remoteHostForSshAndDatabase, databasePort);
        }
    }

}