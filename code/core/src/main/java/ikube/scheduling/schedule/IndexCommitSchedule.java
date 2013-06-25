package ikube.scheduling.schedule;

import ikube.cluster.IMonitorService;
import ikube.model.IndexContext;
import ikube.scheduling.Schedule;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michael Couck
 * @since 21.06.13
 * @version 01.00
 */
public class IndexCommitSchedule extends Schedule {

	@Autowired
	private IMonitorService monitorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	public void run() {
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			IndexContext<?> indexContext = mapEntry.getValue();
			if (!indexContext.isDelta()) {
				continue;
			}
			IndexWriter[] indexWriters = indexContext.getIndexWriters();
			if (indexWriters == null || indexWriters.length == 0) {
				continue;
			}
			for (final IndexWriter indexWriter : indexWriters) {
				try {
					indexWriter.maybeMerge();
					indexWriter.forceMerge(100, Boolean.TRUE);
					indexWriter.commit();
					indexWriter.deleteUnusedFiles();
				} catch (IOException e) {
					logger.error("Exception comitting the index writer : ", e);
				}
			}
		}
	}

}