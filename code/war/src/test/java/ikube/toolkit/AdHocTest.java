package ikube.toolkit;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.action.Open;
import ikube.action.index.IndexManager;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.handler.filesystem.IndexableFilesystemCsvHandler;
import ikube.action.index.handler.filesystem.RowResourceHandler;
import ikube.action.index.handler.strategy.GeospatialEnrichmentStrategy;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystemCsv;
import ikube.search.SearchSpatial;
import junit.framework.Assert;
import mockit.Deencapsulation;
import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;

@Ignore
public class AdHocTest extends BaseTest {

	private IndexContext<?> indexContext;

	@Before
	public void before() throws Exception {
		ThreadUtilities.initialize();

	}

	@Test
	public void doSearch() throws Exception {
		initialize(false);

		SearchSpatial searchSpatial = new SearchSpatial(indexContext.getMultiSearcher());

		searchSpatial.setFirstResult(0);
		searchSpatial.setMaxResults(10);
		searchSpatial.setFragment(Boolean.TRUE);

		searchSpatial.setDistance(10);
		searchSpatial.setSearchStrings("Costa de Xurius");
		searchSpatial.setSearchFields(IConstants.NAME);
		searchSpatial.setOccurrenceFields(IConstants.SHOULD);
		searchSpatial.setTypeFields(IConstants.STRING);

		Coordinate coordinate = new Coordinate(42.5, 1.48333);
		searchSpatial.setCoordinate(coordinate);

		ArrayList<HashMap<String, String>> results = searchSpatial.execute();
		printResults(results);
		Assert.assertTrue(results.size() > 1);
	}

	private void initialize(final boolean createIndex) throws Exception {
		indexContext = new IndexContext<>();
		indexContext.setIndexName(IConstants.NAME);
		indexContext.setIndexDirectoryPath("/mnt/sdb/indexes/");
		indexContext.setAnalyzer(new StemmingAnalyzer());
		indexContext.setBufferedDocs(10000);
		indexContext.setBufferSize(1024);
		indexContext.setBatchSize(10000);
		indexContext.setCompoundFile(Boolean.TRUE);
		indexContext.setMaxFieldLength(10000);
		indexContext.setMaxReadLength(Integer.MAX_VALUE);
		indexContext.setMergeFactor(10000);
		indexContext.setMaxExceptions(1000);

		Indexable indexableFileSystemCsv = new IndexableFileSystemCsv();
		indexableFileSystemCsv.setName(IConstants.NAME);
		((IndexableFileSystemCsv) indexableFileSystemCsv).setSeparator(";");
		((IndexableFileSystemCsv) indexableFileSystemCsv).setAllColumns(Boolean.TRUE);
		((IndexableFileSystemCsv) indexableFileSystemCsv).setEncoding(IConstants.ENCODING);
		((IndexableFileSystemCsv) indexableFileSystemCsv).setPath("/mnt/sdb/data/geoname");
		((IndexableFileSystemCsv) indexableFileSystemCsv).setContentFieldName(IConstants.CONTENTS);
		((IndexableFileSystemCsv) indexableFileSystemCsv).setUnpackZips(Boolean.TRUE);
		((IndexableFileSystemCsv) indexableFileSystemCsv).setMaxLines(IConstants.MILLION);

		indexableFileSystemCsv.setStored(Boolean.TRUE);
		indexableFileSystemCsv.setAnalyzed(Boolean.TRUE);
		indexableFileSystemCsv.setVectored(Boolean.FALSE);

		indexableFileSystemCsv.setAddress(Boolean.TRUE);
		indexableFileSystemCsv.setOmitNorms(Boolean.TRUE);
		indexableFileSystemCsv.setTokenized(Boolean.TRUE);
		indexableFileSystemCsv.setThreads(1);

		IndexableHandler indexableFileSystemHandler = new IndexableFilesystemCsvHandler();
		Deencapsulation.setField(indexableFileSystemHandler, "strategy", new GeospatialEnrichmentStrategy());
		Deencapsulation.setField(indexableFileSystemHandler, "rowResourceHandler", new RowResourceHandler());

		if (createIndex) {
			IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(),
				"127.0.0.1");
			indexContext.setIndexWriters(indexWriter);
			ForkJoinTask<?> forkJoinTask = indexableFileSystemHandler.handleIndexableForked(indexContext,
				indexableFileSystemCsv);
			ThreadUtilities.executeForkJoinTasks(IConstants.GEOSPATIAL, indexableFileSystemCsv.getThreads(),
				forkJoinTask);
			ThreadUtilities.waitForFuture(forkJoinTask, Integer.MAX_VALUE);
			IndexManager.closeIndexWriters(indexContext);
		}

		new Open().execute(indexContext);
		printIndex(indexContext.getMultiSearcher());
	}

	private void printResults(final ArrayList<HashMap<String, String>> results) {
		for (final HashMap<String, String> result : results) {
			logger.info("Result : ");
			for (final Map.Entry<String, String> mapEntry : result.entrySet()) {
				logger.info("       : " + mapEntry.getKey() + "-" + mapEntry.getValue());
			}
		}
	}

}