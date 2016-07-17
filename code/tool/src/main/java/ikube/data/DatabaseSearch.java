package ikube.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseSearch {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("SqlDialectInspection")
    public void search(final String url, final String userid, final String password, final String table) {
        try (Connection connection = DriverManager.getConnection(url, userid, password)) {
            ResultSet resultSet = connection.createStatement().executeQuery("select count(*) from " + table);
            resultSet.next();
            int count = resultSet.getInt(1);
            System.out.println("Url : " + url + " : " + count);
        } catch (final SQLException e) {
            logger.error(e.getMessage());
        }
    }

}
