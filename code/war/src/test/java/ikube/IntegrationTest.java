package ikube;

import ikube.action.index.parse.mime.MimeMapper;
import ikube.action.index.parse.mime.MimeTypes;
import ikube.database.IDataBase;
import ikube.scheduling.Scheduler;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static ikube.toolkit.OBJECT.populateFields;

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
public abstract class IntegrationTest extends AbstractTest {

    private static final Logger LOGGER = Logger.getLogger(IntegrationTest.class);
    private static final File DOT_DIRECTORY = new File(".");

    static {
        try {
            new MimeTypes(IConstants.MIME_TYPES);
            new MimeMapper(IConstants.MIME_MAPPING);
            FILE.deleteFiles(DOT_DIRECTORY, "ikube.h2.db", "ikube.lobs.db", "ikube.log", "openjpa.log");
            new WebServiceAuthentication().authenticate(HTTP_CLIENT, LOCALHOST, SERVER_PORT, REST_USER_NAME, REST_PASSWORD);
        } catch (final Exception e) {
            LOGGER.error(null, e);
        }
    }

    @BeforeClass
    public static void beforeClass() {
        THREAD.initialize();
    }

    @AfterClass
    public static void afterClass() {
        try {
            THREAD.destroy();
            ApplicationContextManager.getBean(Scheduler.class).shutdown();
        } catch (final Exception e) {
            LOGGER.error("Exception closing down the thread pools : ", e);
        }
    }

    /**
     * This method will delete all the specified classes from the database.
     *
     * @param dataBase the database to use for deleting the data
     * @param classes  the classes to delete from the database
     */
    protected static void delete(final IDataBase dataBase, final Class<?>... classes) {
        int batchSize = 1000;
        for (final Class<?> klass : classes) {
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

    /**
     * This method will persist entities in the database, the specified number, in batch mode. First
     * populating the entities with random data.
     *
     * @param dataBase the database access object to persist the data with
     * @param klass    the type of class to persist/populate
     * @param entities the number of entities to persist
     * @param <T>      the parameter type of entity
     */
    protected static <T> void insert(final IDataBase dataBase, final Class<T> klass, final int entities) {
        insert(dataBase, klass, entities, "id", "indexContext");
    }

    /**
     * Same as the above but with some excluded fields specified.
     */
    protected static <T> void insert(final IDataBase dataBase, final Class<T> klass, final int entities, final String... excludedFields) {
        List<T> tees = new ArrayList<>();
        for (int i = 0; i < entities; i++) {
            T tee;
            try {
                tee = populateFields(klass, klass.newInstance(), Boolean.TRUE, 1, excludedFields);
                tees.add(tee);
                if (tees.size() >= 1000) {
                    dataBase.persistBatch(tees);
                    tees.clear();
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        dataBase.persistBatch(tees);
    }

}