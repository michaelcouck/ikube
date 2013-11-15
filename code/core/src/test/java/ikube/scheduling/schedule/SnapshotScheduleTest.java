package ikube.scheduling.schedule;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.SerializationUtilities;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Michael Couck
 * @since 22.07.12
 * @version 01.00
 */
public class SnapshotScheduleTest extends AbstractTest {

	private SnapshotSchedule snapshotSchedule;

	@Before
	public void before() throws Exception {
		snapshotSchedule = new SnapshotSchedule();

		when(fsDirectory.fileLength(anyString())).thenReturn(Long.MAX_VALUE);
		when(fsDirectory.listAll()).thenReturn(new String[] { "file" });

		Mockit.setUpMock(ApplicationContextManagerMock.class);
		when(dataBase.execute(anyString(), any(String[].class), any(Object[].class))).thenReturn(new Long(0));

		Deencapsulation.setField(snapshotSchedule, dataBase);
		Deencapsulation.setField(snapshotSchedule, monitorService);
		Deencapsulation.setField(snapshotSchedule, clusterManager);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void handleNotification() {
		IndexContext indexContext = new IndexContext<Object>();
		indexContext.setName(action.getIndexName());
		indexContext.setIndexDirectoryPath("./indexes");
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);

		Mockito.doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Snapshot snapshot = (Snapshot) args[0];
				snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));
				return null;
			}
		}).when(dataBase).persist(Mockito.any(Snapshot.class));

		double maxSnapshots = (IConstants.MAX_SNAPSHOTS / 10) + 10d;
		for (int i = 0; i < maxSnapshots; i++) {
			snapshotSchedule.run();
		}
		Mockito.verify(action, Mockito.atLeastOnce()).setSnapshot(Mockito.any(Snapshot.class));
	}

	@Test
	public void getDocsPerMinute() {
		when(indexContext.isDelta()).thenReturn(Boolean.TRUE);
		Snapshot snapshot = new Snapshot();
		long docsPerMinute = snapshotSchedule.getDocsPerMinute(indexContext, snapshot);
		assertEquals(0, docsPerMinute);
		snapshot.setNumDocsForIndexWriters(250);
		snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));

		Snapshot previous = new Snapshot();
		previous.setTimestamp(new Timestamp(System.currentTimeMillis() - 65000));
		previous.setNumDocsForIndexWriters(125);
		
		Snapshot one = (Snapshot) SerializationUtilities.clone(previous);
		Snapshot two = (Snapshot) SerializationUtilities.clone(previous);
		Snapshot three = (Snapshot) SerializationUtilities.clone(previous);
		Snapshot four = (Snapshot) SerializationUtilities.clone(previous);
		Snapshot five = (Snapshot) SerializationUtilities.clone(previous);
		Snapshot six = (Snapshot) SerializationUtilities.clone(previous);
		
		one.setNumDocsForIndexWriters(0);
		six.setNumDocsForIndexWriters(Integer.MAX_VALUE);
		
		when(indexContext.getSnapshots()).thenReturn(Arrays.asList(one, two, three, four, five, six));
		snapshotSchedule.getDocsPerMinute(indexContext, snapshot);
		logger.info(ToStringBuilder.reflectionToString(six));
		assertTrue(six.getNumDocsForIndexWriters() > 100 && six.getNumDocsForIndexWriters() < 250);
		Mockito.verify(dataBase, Mockito.atLeastOnce()).merge(any());
	}

	@Test
	public void getSearchesPerMinute() {
		List<Snapshot> snapshots = new ArrayList<Snapshot>();
		when(indexContext.getSnapshots()).thenReturn(snapshots);
		Snapshot snapshot = new Snapshot();

		long searchesPerMinute = snapshotSchedule.getSearchesPerMinute(indexContext, snapshot);
		assertEquals(0, searchesPerMinute);

		Snapshot previous = new Snapshot();
		previous.setTimestamp(new Timestamp(System.currentTimeMillis() - 65000));
		previous.setTotalSearches(100);

		snapshot.setTotalSearches(200);
		snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));

		when(indexContext.getSnapshots()).thenReturn(Arrays.asList(previous));

		searchesPerMinute = snapshotSchedule.getSearchesPerMinute(indexContext, snapshot);
		assertTrue(searchesPerMinute > 50 && searchesPerMinute < 100);
	}

	@Test
	public void setLogTail() {
		String string = "Log tail";
		File outputFile = FileUtilities.getOrCreateFile("./" + IConstants.IKUBE + IConstants.SEP + IConstants.IKUBE_LOG);
		FileUtilities.setContents(outputFile, string.getBytes());
		Deencapsulation.setField(Logging.class, "LOG_FILE", outputFile);
		Server server = new Server();
		snapshotSchedule.setLogTail(server);
		assertEquals(string, server.getLogTail());

		byte[] bytes = new byte[IConstants.MILLION + 10];
		Arrays.fill(bytes, (byte) 'a');
		FileUtilities.setContents(outputFile, bytes);
		snapshotSchedule.setLogTail(server);
		assertTrue(server.getLogTail().length() > 0);
	}

	@Test
	public void sortSnapshots() throws Exception {
		String string = "Log tail";
		File outputFile = FileUtilities.getOrCreateFile("./" + IConstants.IKUBE + IConstants.SEP + IConstants.IKUBE_LOG);
		FileUtilities.setContents(outputFile, string.getBytes());
		Deencapsulation.setField(Logging.class, "LOG_FILE", outputFile);
		
		List<Snapshot> snapshots = new ArrayList<Snapshot>();
		for (int i = 0; i < 10; i++) {
			Snapshot snapshot = ObjectToolkit.populateFields(Snapshot.class, new Snapshot(), Boolean.TRUE, 5);
			snapshot.setTimestamp(new Timestamp(System.currentTimeMillis() - i * 1000));
			snapshots.add(snapshot);
		}
		snapshots = snapshotSchedule.sortSnapshots(snapshots);
		Timestamp previousTimestamp = null;
		for (final Snapshot snapshot : snapshots) {
			if (previousTimestamp == null) {
				previousTimestamp = snapshot.getTimestamp();
				continue;
			}
			assertTrue(previousTimestamp.before(snapshot.getTimestamp()));
		}
	}

}