package ikube.index.handler.filesystem;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.StringUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

	/**
	 * {@inheritDoc}
	 */
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

			List<IndexableColumn> indexableColumns = getIndexableColumns(indexableFileSystemCsv, columns);

			int lineNumber = 0;
			String fileName = file.getName();
			char separator = indexableFileSystemCsv.getSeparator();
			String lineNumberFieldName = indexableFileSystemCsv.getLineNumberFieldName();
			while (lineIterator.hasNext()) {
				try {
					String line = lineIterator.nextLine();
					String[] values = StringUtils.split(line, separator);
					if (indexableColumns.size() != values.length) {
						logger.warn("Columns and values different on line : " + lineNumber + ", columns : " + columns.length
								+ ", values : " + values.length + ", data : " + Arrays.deepToString(values));
					}
					for (int i = 0; i < values.length && i < indexableColumns.size(); i++) {
						IndexableColumn indexableColumn = indexableColumns.get(i);
						indexableColumn.setContent(values[i]);
					}
					// columns, values, separator, store, index, termVector, indexableColumns
					handleResource(indexContext, indexableFileSystemCsv, new Document(), fileName, lineNumber, lineNumberFieldName,
							indexableColumns);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void handleResource(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final Document document,
			final Object... resources) {
		// fileName, lineNumber, lineNumberFieldName
		String fileName = (String) resources[0];
		int lineNumber = (Integer) resources[1];
		String lineNumberFieldName = (String) resources[2];
		List<IndexableColumn> indexableColumns = (List<IndexableColumn>) resources[3];

		String identifier = StringUtils.join(new Object[] { fileName, Integer.toString(lineNumber) }, IConstants.SPACE);
		// Add the line number field
		IndexManager.addStringField(lineNumberFieldName, identifier, document, Store.YES, Index.ANALYZED, TermVector.NO);

		for (IndexableColumn indexableColumn : indexableColumns) {
			String fieldName = indexableColumn.getFieldName();
			String fieldValue = (String) indexableColumn.getContent();

			Store store = indexableColumn.isStored() ? Store.YES : Store.NO;
			Index index = indexableColumn.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableColumn.isVectored() ? TermVector.YES : TermVector.NO;

			if (StringUtilities.isNumeric(fieldValue)) {
				IndexManager.addNumericField(fieldName, fieldValue, document, store);
			} else {
				fieldValue = StringUtilities.strip(fieldValue, "\"");
				IndexManager.addStringField(fieldName, fieldValue, document, store, index, termVector);
			}
			indexableColumn.setContent(null);
		}

		try {
			addDocument(indexContext, indexableFileSystem, document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected synchronized boolean isExcluded(final File file, final Pattern pattern) {
		return !file.getName().endsWith(".csv") || super.isExcluded(file, pattern);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<IndexableColumn> getIndexableColumns(final Indexable<?> indexable, final String[] columns) {
		class IndexableColumnComparator<T extends IndexableColumn> implements Comparator<IndexableColumn> {
			@Override
			public int compare(IndexableColumn o1, IndexableColumn o2) {
				return o1.getFieldName().compareTo(o2.getFieldName());
			}
		}
		List<Indexable<?>> indexableColumns = indexable.getChildren();
		Comparator indexableColumnComparator = new IndexableColumnComparator();
		Collections.sort(indexableColumns, indexableColumnComparator);
		// Add all the columns that are not present in the configuration
		for (final String columnName : columns) {
			IndexableColumn indexableColumn = new IndexableColumn();
			indexableColumn.setFieldName(columnName);
			if (Collections.binarySearch(indexableColumns, columnName, indexableColumnComparator) < 0) {
				// Add the column to the list
				indexableColumn.setName(columnName);
				indexableColumn.setAddress(Boolean.FALSE);
				indexableColumn.setAnalyzed(Boolean.TRUE);
				indexableColumn.setFieldName(columnName);
				indexableColumn.setIdColumn(Boolean.FALSE);
				indexableColumn.setNumeric(Boolean.FALSE);
				indexableColumn.setParent(indexable);
				indexableColumn.setStored(Boolean.TRUE);
				indexableColumn.setStrategies(indexable.getStrategies());
				indexableColumn.setVectored(Boolean.TRUE);
				indexableColumns.add(indexableColumn);
				// We need to re-sort each time there is an addition column
				Collections.sort(indexableColumns, indexableColumnComparator);
			}
		}
		// Now sort the columns in the list according to the columns in the header of the file
		List<IndexableColumn> sortedIndexableColumns = new ArrayList<IndexableColumn>();
		for (final String columnName : columns) {
			for (final Indexable<?> indexableColumn : indexableColumns) {
				if (indexableColumn.getName().equals(columnName)) {
					sortedIndexableColumns.add((IndexableColumn) indexableColumn);
					break;
				}
			}
		}
		return sortedIndexableColumns;
	}

}