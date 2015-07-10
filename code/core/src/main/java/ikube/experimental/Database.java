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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static ikube.database.DatabaseUtilities.close;

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
@SuppressWarnings("FieldCanBeLocal")
class Database {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // Ssh components
    // To be used for the ssh tunnel
    private JSch jsch;
    private Properties config;
    private Session session;

    // SSH credentials and properties
    // Can be any port, the start port for the ssh tunnel
    @Value("${local-port}")
    private int localPort = 10000;
    // The port that ssh is listening on the target machine
    @Value("${target-ssh-port}")
    private int targetSshPort = 443;
    @Value("${ssh-userid}")
    private String sshUserid = "michael";
    @Value("${ssh-password}")
    private String sshPassword = "bla...";

    // The remote host for the tunnel and the database
    @Value("${remote-host-for-ssh-and-database}")
    private String remoteHostForSshAndDatabase = "ikube.be";

    // Database credentials and items
    @Value("${database-password}")
    String password = "password";
    @Value("${database-userid}")
    private String userid = "BPHADMIN";
    // Must be the target port for the database
    @Value("${database-port}")
    private int databasePort = 1521;
    @Value("${database-url}")
    private String url = "jdbc:oracle:thin:@localhost:" + localPort + "/BPH";
    private Connection connection;

    // The last timestamp for data that was indexed
    private Timestamp modification;

    private boolean working = Boolean.FALSE;

    Database() {
        jsch = new JSch();
        config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        modification = new Timestamp(0);
    }

    List<List<Object>> readChangedRecords() throws SQLException, JSchException {
        if (working) {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
        working = Boolean.TRUE;
        try {
            createSshTunnel();
            createDatabaseConnection();
            List<List<Object>> results = new ArrayList<>();

            String sql = "SELECT * FROM worklistitem WHERE whenmodified > ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, modification);

            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            // Reset the last modification timestamp
            modification = new Timestamp(System.currentTimeMillis());

            while (resultSet.next()) {
                List<Object> row = new ArrayList<>();
                for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
                    Object columnValue = resultSet.getObject(columnIndex);
                    row.add(columnValue);
                }
                results.add(row);
                if (results.size() % 1000 == 0) {
                    logger.info("Adding row : " + results.size() + ", " + row);
                }
            }

            return results;
        } finally {
            // close(connection);
            // session.disconnect();
            working = Boolean.FALSE;
        }
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