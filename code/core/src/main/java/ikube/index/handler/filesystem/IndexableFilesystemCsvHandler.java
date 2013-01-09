package ikube.index.handler.filesystem;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * This handler is a custom handler for the CSV files. Rather than inserting the data into the database, meaning creating tables and the
 * like, and the painful process of importing the data using some tool, and then the file is updated and automation is required, this
 * handler will just read the 'structured' files line by line and index the data as if it were
 * 
 * @author Michael Couck
 * @since 08.02.2011
 * @version 01.00
 */
public class IndexableFilesystemCsvHandler extends IndexableFilesystemHandler {

	@Override
	public void handleFile(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final File file)
			throws Exception {
		IndexableFileSystemCsv indexableFileSystemCsv = (IndexableFileSystemCsv) indexableFileSystem;
		LineIterator lineIterator = FileUtils.lineIterator(file);
		// The first line is the header, i.e. the columns of the file
		String headerLine = lineIterator.nextLine();
		String[] columns = StringUtils.split(headerLine, indexableFileSystemCsv.getSeparator());
		Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
		Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
		TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;
		while (lineIterator.hasNext()) {
			try {
				String line = lineIterator.nextLine();
				Document document = new Document();
				String[] values = StringUtils.split(line, indexableFileSystemCsv.getSeparator());
				for (int i = 0; i < values.length; i++) {
					String strippedValue = StringUtils.strip(values[i], "\"");
					IndexManager.addStringField(columns[i], strippedValue, document, store, analyzed, termVector);
				}
				addDocument(indexContext, indexableFileSystemCsv, document);
				ThreadUtilities.sleep(indexContext.getThrottle());
			} catch (Exception e) {
				logger.error("Exception processing file : " + file, e);
				handleMaxExceptions(indexableFileSystemCsv, e);
			}
		}
	}

	protected synchronized boolean isExcluded(final File file, final Pattern pattern) {
		return !file.getName().endsWith(".csv") || super.isExcluded(file, pattern);
	}

}