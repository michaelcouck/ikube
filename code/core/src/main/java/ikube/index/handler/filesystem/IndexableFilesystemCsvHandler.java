package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.Arrays;
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
		char separator = indexableFileSystemCsv.getSeparator();
		int lineNumber = 0;
		String lineNumberFieldName = indexableFileSystemCsv.getLineNumberFieldName() != null ? indexableFileSystemCsv
				.getLineNumberFieldName() : IConstants.ID;
		while (lineIterator.hasNext()) {
			try {
				String line = lineIterator.nextLine();
				Document document = new Document();
				String[] values = StringUtils.split(line, separator);
				if (columns.length != values.length) {
					logger.warn("Columns and values different on line : " + lineNumber + ", columns : " + columns.length + ", values : "
							+ values.length + ", of file : " + file + ", data : " + Arrays.deepToString(values));
				}
				String identifier = StringUtils.join(new Object[] { file.getName(), Integer.toString(lineNumber) }, IConstants.SPACE);
				// Add the line number field
				IndexManager.addStringField(lineNumberFieldName, identifier, document, Store.YES, Index.ANALYZED, TermVector.NO);
				for (int i = 0; i < columns.length && i < values.length; i++) {
					if (StringUtils.isNumeric(values[i])) {
						IndexManager.addNumericField(columns[i], values[i], document, store);
					} else {
						IndexManager.addStringField(columns[i], values[i], document, store, analyzed, termVector);
					}
				}
				// logger.info("Adding document : " + document);
				addDocument(indexContext, indexableFileSystemCsv, document);
				ThreadUtilities.sleep(indexContext.getThrottle());
			} catch (Exception e) {
				logger.error("Exception processing file : " + file, e);
				handleMaxExceptions(indexableFileSystemCsv, e);
			}
			++lineNumber;
			if (lineNumber % 10000 == 0) {
				logger.info("Lines done : " + lineNumber);
			}
		}
	}

	protected synchronized boolean isExcluded(final File file, final Pattern pattern) {
		return !file.getName().endsWith(".csv") || super.isExcluded(file, pattern);
	}

}