package ikube.action;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseActionTest extends BaseTest {

	/**
	 * Creates a real Lucene index in the directory specified.
	 * 
	 * @param latestIndexDirectory
	 * @param serverName
	 * @return
	 * @throws Exception
	 */
	protected File createIndex(File latestIndexDirectory, String ip, String contextName) throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append(latestIndexDirectory.getAbsolutePath());
		builder.append(File.separator);
		builder.append(contextName);
		builder.append(File.separator);
		builder.append(System.currentTimeMillis());
		builder.append(File.separator);
		builder.append(ip);
		return createIndex(FileUtilities.getFile(builder.toString(), Boolean.TRUE));
	}

	protected File createIndex(File indexDirectory) throws Exception {
		logger.info("Creating Lucene index in : " + indexDirectory);
		Directory directory = null;
		IndexWriter indexWriter = null;
		try {
			directory = FSDirectory.open(indexDirectory);
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
				try {
					indexWriter.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		return indexDirectory;
	}

}