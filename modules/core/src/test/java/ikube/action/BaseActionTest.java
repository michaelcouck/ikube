package ikube.action;

import static org.mockito.Mockito.mock;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;

public abstract class BaseActionTest extends BaseTest {

	protected MultiSearcher multiSearcher = mock(MultiSearcher.class);
	protected IndexSearcher indexSearcher = mock(IndexSearcher.class);
	protected IndexReader indexReader = mock(IndexReader.class);
	protected FSDirectory fsDirectory = mock(FSDirectory.class);
	protected Searchable[] searchables = new Searchable[] { indexSearcher };
	protected Lock lock = mock(Lock.class);

	/**
	 * Creates a real Lucene index in the directory specified.
	 *
	 * @param latestIndexDirectory
	 * @param serverName
	 * @return
	 * @throws Exception
	 */
	protected File createIndex(File latestIndexDirectory, String serverName) throws Exception {
		String filePath = latestIndexDirectory.getAbsolutePath() + File.separatorChar + serverName;
		File serverIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		logger.info("Creating Lucene index in : " + serverIndexDirectory);
		Directory directory = null;
		IndexWriter indexWriter = null;
		try {
			directory = FSDirectory.open(serverIndexDirectory);
			indexWriter = new IndexWriter(directory, IConstants.ANALYZER, MaxFieldLength.UNLIMITED);
			Document document = new Document();
			document.add(new Field(IConstants.CONTENTS, "Michael Couck", Store.YES, Index.ANALYZED));
			indexWriter.addDocument(document);
			indexWriter.commit();
			indexWriter.optimize(Boolean.TRUE);
		} finally {
			try {
				directory.close();
			} finally {
				indexWriter.close();
			}
		}
		return serverIndexDirectory;
	}

}
