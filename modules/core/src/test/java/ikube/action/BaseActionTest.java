package ikube.action;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.IndexContext;
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
		builder.append(ip);
		builder.append(File.separator);
		builder.append(contextName);
		return createIndex(FileUtilities.getFile(builder.toString(), Boolean.TRUE));
	}

	protected File createIndex(File contextIndexDirectory) throws Exception {
		logger.info("Creating Lucene index in : " + contextIndexDirectory);
		Directory directory = null;
		IndexWriter indexWriter = null;
		try {
			directory = FSDirectory.open(contextIndexDirectory);
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
		return contextIndexDirectory;
	}

	/**
	 * Returns the path to the latest index directory for this server and this context. The result will be something like
	 * './faq/1234567890/127.0.0.1/faqContext'.
	 *
	 * @param indexContext
	 *            the index context to get the directory path for
	 * @return the directory path to the latest index directory for this servers and context
	 */
	protected String getContextIndexDirectoryPath(IndexContext indexContext) {
		StringBuilder builder = new StringBuilder();
		builder.append(indexContext.getIndexDirectoryPath());
		builder.append(File.separator);
		builder.append(System.currentTimeMillis());
		builder.append(File.separator);
		builder.append(ip);
		builder.append(File.separator);
		builder.append(indexContext.getName());
		return builder.toString();
	}

}