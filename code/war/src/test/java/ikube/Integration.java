package ikube;

import ikube.database.IDataBase;
import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeTypes;
import ikube.listener.ListenerManager;
import ikube.listener.Scheduler;
import ikube.model.Snapshot;
import ikube.security.WebServiceAuthentication;
import ikube.service.IMonitorService;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * This base class for the integration tests will load some snapshots into the database as well as initialize the application context. IT
 * also provides
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

		new MimeTypes(IConstants.MIME_TYPES);
		new MimeMapper(IConstants.MIME_MAPPING);

		startContext();
		Thread.sleep(3000);
		insertData();
		Thread.sleep(3000);
		FileUtilities.deleteFiles(DOT_DIRECTORY, "btm1.tlog", "btm2.tlog", "ikube.h2.db", "ikube.lobs.db", "ikube.log", "openjpa.log");
		WebServiceAuthentication.authenticate(HTTP_CLIENT, LOCALHOST, SERVER_PORT, REST_USER_NAME, REST_PASSWORD);
	}

	private static void startContext() {
		ApplicationContextManager.getBean(ListenerManager.class).removeListeners();
		ApplicationContextManager.getBean(ListenerManager.class).removeListeners();
		ApplicationContextManager.getBean(Scheduler.class).shutdown();
		ThreadUtilities.destroy();
	}

	private static void insertData() throws SQLException, FileNotFoundException {
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		List<Snapshot> snapshots = new ArrayList<Snapshot>();
		for (int i = 0; i < 11000; i++) {
			Snapshot snapshot = ObjectToolkit.populateFields(Snapshot.class, new Snapshot(), true, 0, 1, "id", "indexContext");
			snapshot.setId(0);
			snapshots.add(snapshot);
		}
		dataBase.persistBatch(snapshots);
	}

	/**
	 * This method will delete all the specified classes from the database.
	 * 
	 * @param dataBase the database to use for deleting the data
	 * @param klasses the classes to delete from the database
	 */
	public static void delete(final IDataBase dataBase, final Class<?>... klasses) {
		int batchSize = 1000;
		for (Class<?> klass : klasses) {
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