package ikube.experimental;

import com.jcraft.jsch.JSchException;
import ikube.toolkit.THREAD;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
@Ignore
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"spring.xml"})
public class Integration {

    @Autowired
    @Qualifier("ikube.experimental.Manager")
    public Manager manager;

    @Test
    public void process() throws SQLException, JSchException {
        for (int i = 0; i < 1000; i++) {
            manager.indexRecords();
            THREAD.sleep(15000);
        }
    }

}
