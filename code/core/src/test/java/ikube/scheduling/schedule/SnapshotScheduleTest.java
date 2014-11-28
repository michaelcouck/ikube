package ikube.scheduling.schedule;

import static ikube.toolkit.FILE.deleteFile;
import static ikube.toolkit.FILE.getOrCreateFile;
import static ikube.toolkit.FILE.setContents;
import static ikube.toolkit.OBJECT.populateFields;
import static java.util.Arrays.asList;
import static mockit.Deencapsulation.setField;
import static mockit.Mockit.setUpMock;
import static mockit.Mockit.tearDownMocks;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.toolkit.LOGGING;
import ikube.toolkit.SERIALIZATION;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Michael Couck
 * @since 22-07-2012
 * @version 01.00
 */
public class SnapshotScheduleTest extends AbstractTest {

	private SnapshotSchedule snapshotSchedule;

    @Before
    @SuppressWarnings("UnnecessaryBoxing")
	public void before() throws Exception {
		snapshotSchedule = new SnapshotSchedule();

		when(fsDirectory.fileLength(anyString())).thenReturn(Long.MAX_VALUE);
		when(fsDirectory.listAll()).thenReturn(new String[] { "file" });

		setUpMock(ApplicationContextManagerMock.class);
		when(dataBase.execute(anyString(), any(String[].class), any(Object[].class))).thenReturn(new Long(0));

		setField(snapshotSchedule, dataBase);
		setField(snapshotSchedule, monitorService);
		setField(snapshotSchedule, clusterManager);
	}

	@After
	public void after() {
		tearDownMocks(ApplicationContextManagerMock.class);
		deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void handleNotification() {
		IndexContext indexContext = new IndexContext();
		indexContext.setName(action.getIndexName());
		indexContext.setIndexDirectoryPath("./indexes");
		Map<String, IndexContext> indexContexts = new HashMap<>();
		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);

		Mockito.doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Snapshot snapshot = (Snapshot) args[0];
				snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));
				return null;
			}
		}).when(dataBase).persist(any(Snapshot.class));

		double maxSnapshots = (IConstants.MAX_SNAPSHOTS / 10) + 10d;
		for (int i = 0; i < maxSnapshots; i++) {
			snapshotSchedule.run();
		}
		verify(action, atLeastOnce()).setSnapshot(any(Snapshot.class));
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
		
		Snapshot one = (Snapshot) SERIALIZATION.clone(previous);
		Snapshot two = (Snapshot) SERIALIZATION.clone(previous);
		Snapshot three = (Snapshot) SERIALIZATION.clone(previous);
		Snapshot four = (Snapshot) SERIALIZATION.clone(previous);
		Snapshot five = (Snapshot) SERIALIZATION.clone(previous);
		Snapshot six = (Snapshot) SERIALIZATION.clone(previous);
		Snapshot seven = (Snapshot) SERIALIZATION.clone(previous);
		Snapshot eight = (Snapshot) SERIALIZATION.clone(previous);
		Snapshot nine = (Snapshot) SERIALIZATION.clone(previous);
		Snapshot ten = (Snapshot) SERIALIZATION.clone(previous);
		
		one.setNumDocsForIndexWriters(0);
		five.setNumDocsForIndexWriters(Integer.MAX_VALUE);
		seven.setNumDocsForIndexWriters(Integer.MAX_VALUE);
		
		when(indexContext.getSnapshots()).thenReturn(asList(one, two, three, four, five, six, seven, eight, nine, ten));
		snapshotSchedule.getDocsPerMinute(indexContext, snapshot);
		logger.info(ToStringBuilder.reflectionToString(five));
		assertTrue(five.getNumDocsForIndexWriters() > 100 && five.getNumDocsForIndexWriters() < 250);
		assertTrue(six.getNumDocsForIndexWriters() > 100 && six.getNumDocsForIndexWriters() < 250);
		assertTrue(seven.getNumDocsForIndexWriters() > 100 && seven.getNumDocsForIndexWriters() < 250);
		verify(dataBase, atLeastOnce()).merge(any());
	}

	@Test
	public void getSearchesPerMinute() {
		List<Snapshot> snapshots = new ArrayList<>();
		when(indexContext.getSnapshots()).thenReturn(snapshots);
		Snapshot snapshot = new Snapshot();

		long searchesPerMinute = snapshotSchedule.getSearchesPerMinute(indexContext, snapshot);
        assertEquals(0, searchesPerMinute);

        Snapshot previous = new Snapshot();
        previous.setTimestamp(new Timestamp(System.currentTimeMillis() - 65000));
        previous.setTotalSearches(100);

        snapshot.setTotalSearches(200);
        snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));

        when(indexContext.getSnapshots()).thenReturn(asList(previous));

        searchesPerMinute = snapshotSchedule.getSearchesPerMinute(indexContext, snapshot);
		assertEquals(100, searchesPerMinute);
	}

	@Test
	public void setLogTail() {
		String string = "Log tail";
		File outputFile = getOrCreateFile("./" + IConstants.IKUBE + IConstants.SEP + IConstants.IKUBE_LOG);
		setContents(outputFile, string.getBytes());
		setField(LOGGING.class, "LOG_FILE", outputFile);
		Server server = new Server();
		snapshotSchedule.setLogTail(server);
		assertEquals(string, server.getLogTail());

		byte[] bytes = new byte[IConstants.MILLION + 10];
		Arrays.fill(bytes, (byte) 'a');
		setContents(outputFile, bytes);
		snapshotSchedule.setLogTail(server);
		assertTrue(server.getLogTail().length() > 0);
	}

	@Test
	public void sortSnapshots() throws Exception {
		String string = "Log tail";
		File outputFile = getOrCreateFile("./" + IConstants.IKUBE + IConstants.SEP + IConstants.IKUBE_LOG);
		setContents(outputFile, string.getBytes());
		setField(LOGGING.class, "LOG_FILE", outputFile);
		
		List<Snapshot> snapshots = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Snapshot snapshot = populateFields(Snapshot.class, new Snapshot(), Boolean.TRUE, 5);
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