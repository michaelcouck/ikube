package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.StringUtilities;
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
		String encoding = indexableFileSystemCsv.getEncoding() != null ? indexableFileSystemCsv.getEncoding() : IConstants.ENCODING;
		logger.info("Using encoding for file : " + encoding + ", " + file);
		LineIterator lineIterator = FileUtils.lineIterator(file, encoding);
		try {
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
					String[] values = StringUtils.split(line, separator);
					if (columns.length != values.length) {
						logger.warn("Columns and values different on line : " + lineNumber + ", columns : " + columns.length
								+ ", values : " + values.length + ", data : " + Arrays.deepToString(values));
					}
					Document document = handleLine(columns, values, lineNumberFieldName, separator, lineNumber, store, analyzed,
							termVector, file);
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
		} finally {
			LineIterator.closeQuietly(lineIterator);
		}
	}

	public Document handleLine(final String[] columns, final String[] values, final String lineNumberFieldName, final char separator,
			final int lineNumber, final Store store, final Index index, final TermVector termVector, final File file) {
		Document document = new Document();
		String identifier = StringUtils.join(new Object[] { file.getName(), Integer.toString(lineNumber) }, IConstants.SPACE);
		// Add the line number field
		IndexManager.addStringField(lineNumberFieldName, identifier, document, Store.YES, Index.ANALYZED, TermVector.NO);
		for (int i = 0; i < columns.length && i < values.length; i++) {
			String value = values[i];
			if (StringUtilities.isNumeric(value)) {
				IndexManager.addNumericField(columns[i], value, document, store);
			} else {
				value = StringUtilities.strip(value, "\"");
				IndexManager.addStringField(columns[i], value, document, store, index, termVector);
			}
		}
		return document;
	}

	protected synchronized boolean isExcluded(final File file, final Pattern pattern) {
		return !file.getName().endsWith(".csv") || super.isExcluded(file, pattern);
	}

}