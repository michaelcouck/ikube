package ikube;

import ikube.action.index.parse.mime.MimeMapper;
import ikube.action.index.parse.mime.MimeTypes;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.Snapshot;
import ikube.scheduling.Scheduler;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ObjectToolkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * This base class for the integration tests will load some snapshots into the database as well as initialize the application context.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@Ignore
public abstract class Integration extends Base {

	private static final Logger LOGGER = Logger.getLogger(Integration.class);

	private static boolean INITIALIZED = Boolean.FALSE;
	private static final File DOT_DIRECTORY = new File(".");

	@BeforeClass
	public static void beforeClass() throws Exception {
		if (INITIALIZED) {
			return;
		}
		INITIALIZED = Boolean.TRUE;
		FileUtilities.deleteFiles(DOT_DIRECTORY, "btm1.tlog", "btm2.tlog", "ikube.h2.db", "ikube.lobs.db", "ikube.log", "openjpa.log");

		new MimeTypes(IConstants.MIME_TYPES);
		new MimeMapper(IConstants.MIME_MAPPING);

		ApplicationContextManager.getBean(Scheduler.class).shutdown();
		Thread.sleep(3000);
		insertData(Snapshot.class, 11000);
		Thread.sleep(3000);
		WebServiceAuthentication.authenticate(HTTP_CLIENT, LOCALHOST, SERVER_PORT, REST_USER_NAME, REST_PASSWORD);
	}

	public static <T> void insertData(final Class<T> klass, final int entities) throws SQLException, FileNotFoundException {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		List<T> tees = new ArrayList<T>();
		for (int i = 0; i < entities; i++) {
			try {
				T tee = ObjectToolkit.populateFields(klass, klass.newInstance(), true, 0, 1, "id", "indexContext");
				tees.add(tee);
				if (i % 10000 == 0) {
					dataBase.persistBatch(tees);
					tees.clear();
				}
			} catch (Exception e) {
				LOGGER.error(null, e);
			}
		}
		dataBase.persistBatch(tees);
	}

	/**
	 * This method will delete all the specified classes from the database.
	 * 
	 * @param dataBase the database to use for deleting the data
	 * @param klasses the classes to delete from the database
	 */
	public static void delete(final IDataBase dataBase, final Class<?>... klasses) {
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

	protected IMonitorService monitorService = ApplicationContextManager.getBean(IMonitorService.class);

}