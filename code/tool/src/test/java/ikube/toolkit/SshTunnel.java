package ikube.toolkit;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import ikube.database.DatabaseUtilities;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.*;
import java.util.Properties;

/**
 * This is a simple test class to tunnel a JDBC connection through an ssh tunnel. This will allow
 * secure transfer of data over the network, and in the case of the internet this can be particularly
 * useful if you need to read a database exposed to the wild, wild, web.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 07-07-2015
 */
@Ignore
public class SshTunnel {

    @Test
    public void tunnelJdbcThroughSsh() throws SQLException, JSchException {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        JSch jsch = new JSch();
        Session session = null;
        Connection connection = null;

        // Can be any port
        int localPort = 1521;
        String remoteHost = "ikube.be";
        // Must be the target port for the database
        int remotePort = 1521;
        try {
            session = jsch.getSession("michael", "ikube.be", 443);
            session.setPassword("change_me_when_you_run_this_test");
            session.setConfig(config);
            session.connect();
            session.setPortForwardingL(localPort, remoteHost, remotePort);
            System.out.println("Connected ssh tunnel : ");

            String url = "jdbc:oracle:thin:@localhost:" + localPort + "/BPH";
            String userid = "BPHADMIN";
            String password = "password";
            connection = DriverManager.getConnection(url, userid, password);

            System.out.println("Connected JDBC through ssh tunnel : ");

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT count(*) FROM paymenttransaction");
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            Object paymentTransactionsTotal = resultSet.getObject(1);
            System.out.println(paymentTransactionsTotal);
        } finally {
            // First close the database connection
            DatabaseUtilities.close(connection);

            // Then close the ssh tunnel
            //noinspection ConstantConditions
            session.disconnect();
        }
    }

}