package ikube;

import ikube.action.index.parse.mime.MimeMapper;
import ikube.action.index.parse.mime.MimeTypes;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.scheduling.Scheduler;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.List;

/**
 * This base class for the integration tests will load some snapshots
 * into the database as well as initialize the application context.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-10-2010
 */
@Ignore
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:ikube/spring.xml"})
@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class IntegrationTest extends BaseTest {

    private static final Logger LOGGER = Logger.getLogger(IntegrationTest.class);
    private static final File DOT_DIRECTORY = new File(".");

    static {
        try {
            new MimeTypes(IConstants.MIME_TYPES);
            new MimeMapper(IConstants.MIME_MAPPING);
            FileUtilities.deleteFiles(DOT_DIRECTORY, "ikube.h2.db", "ikube.lobs.db", "ikube.log", "openjpa.log");
            new WebServiceAuthentication().authenticate(HTTP_CLIENT, LOCALHOST, SERVER_PORT, REST_USER_NAME, REST_PASSWORD);
        } catch (final Exception e) {
            LOGGER.error(null, e);
        }
    }

    @BeforeClass
    public static void beforeClass() {
        ThreadUtilities.initialize();
    }

    @AfterClass
    public static void afterClass() {
        ThreadUtilities.destroy();
        ApplicationContextManager.getBean(Scheduler.class).shutdown();
    }

    @Autowired
    protected IMonitorService monitorService;

    /**
     * This method will delete all the specified classes from the database.
     *
     * @param dataBase the database to use for deleting the data
     * @param klasses  the classes to delete from the database
     */
    protected void delete(final IDataBase dataBase, final Class<?>... klasses) {
        int batchSize = 1000;
        for (final Class<?> klass : klasses) {
            try {
                List<?> list = dataBase.find(klass, 0, batchSize);
                do {
                    dataBase.removeBatch(list);
                    list = dataBase.find(klass, 0, batchSize);
                } while (list.size() > 0);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}