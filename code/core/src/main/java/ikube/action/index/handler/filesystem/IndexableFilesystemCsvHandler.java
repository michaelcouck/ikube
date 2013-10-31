package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This handler is a custom handler for the CSV files. Rather than inserting the data into the database, meaning creating tables and the like, and the painful
 * process of importing the data using some tool, and then the file is updated and automation is required, this handler will just read the 'structured' files
 * line by line and index the data as if it were a database table.
 * 
 * @author Michael Couck
 * @since 08.02.2011
 * @version 01.00
 */
public class IndexableFilesystemCsvHandler extends IndexableHandler<IndexableFileSystemCsv> {

	@Autowired
	private RowResourceHandler rowResourceHandler;

	@Override
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final IndexableFileSystemCsv indexableFileSystem) throws Exception {
		return getRecursiveAction(indexContext, indexableFileSystem, new FileSystemCsvResourceProvider(indexableFileSystem.getPath()));
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableFileSystemCsv indexableFileSystemCsv, final Object resource) {
		try {
			logger.info("Handling resource : " + resource);
			handleFile(indexContext, indexableFileSystemCsv, (File) resource);
		} catch (Exception e) {
			handleException(indexableFileSystemCsv, e, "Exception handling csv file : " + resource);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	void handleFile(final IndexContext<?> indexContext, final IndexableFileSystemCsv indexableFileSystem, final File file) throws Exception {
		IndexableFileSystemCsv indexableFileSystemCsv = (IndexableFileSystemCsv) indexableFileSystem;
		String encoding = indexableFileSystemCsv.getEncoding() != null ? indexableFileSystemCsv.getEncoding() : IConstants.ENCODING;
		logger.info("Using encoding for file : " + encoding + ", " + file);
		LineIterator lineIterator = FileUtils.lineIterator(file, encoding);
		try {
			// The first line is the header, i.e. the columns of the file
			String separator = indexableFileSystemCsv.getSeparator();
			String headerLine = lineIterator.nextLine();
			String[] columns = StringUtils.split(headerLine, separator);
			// Trim any space on the column headers
			for (int i = 0; i < columns.length; i++) {
				columns[i] = columns[i].trim();
			}

			List<Indexable<?>> indexableColumns = getIndexableColumns(indexableFileSystemCsv, columns);
			indexableFileSystemCsv.setChildren(indexableColumns);

			int lineNumber = 0;
			indexableFileSystemCsv.setFile(file);
			Map<Integer, String> differentLines = new HashMap<Integer, String>();
			while (lineIterator.hasNext() && ThreadUtilities.isInitialized()) {
				indexableFileSystemCsv.setLineNumber(lineNumber);
				try {
					String line = lineIterator.nextLine();
					String[] values = StringUtils.split(line, separator);
					if (indexableColumns.size() != values.length) {
						differentLines.put(lineNumber, Arrays.deepToString(values));
					}
					for (int i = 0; i < values.length && i < indexableColumns.size(); i++) {
						IndexableColumn indexableColumn = (IndexableColumn) indexableColumns.get(i);
						indexableColumn.setContent(values[i]);
					}
					rowResourceHandler.handleResource(indexContext, indexableFileSystemCsv, new Document(), file);
					ThreadUtilities.sleep(indexContext.getThrottle());
				} catch (Exception e) {
					logger.error("Exception processing file : " + file, e);
					handleException(indexableFileSystemCsv, e);
				}
				++lineNumber;
				if (lineNumber % 10000 == 0) {
					logger.info("Lines done : " + lineNumber);
					for (final Map.Entry<Integer, String> mapEntry : differentLines.entrySet()) {
						logger.warn("Columns and values different on line : " + mapEntry.getKey() + ", columns : " + columns.length + ", values : "
								+ mapEntry.getValue());
						if (!logger.isDebugEnabled()) {
							// Only print one line if it is not in debug
							break;
						}
					}
					differentLines.clear();
				}
			}
		} finally {
			LineIterator.closeQuietly(lineIterator);
		}
	}

	protected List<Indexable<?>> getIndexableColumns(final Indexable<?> indexable, final String[] columns) {
		List<Indexable<?>> indexableColumns = indexable.getChildren();
		if (indexableColumns == null) {
			indexableColumns = new ArrayList<Indexable<?>>();
			indexable.setChildren(indexableColumns);
		}
		List<Indexable<?>> sortedIndexableColumns = new ArrayList<Indexable<?>>();
		// Add all the columns that are not present in the configuration
		for (final String columnName : columns) {
			IndexableColumn indexableColumn = null;
			for (final Indexable<?> child : indexableColumns) {
				if (((IndexableColumn) child).getFieldName().equals(columnName)) {
					indexableColumn = (IndexableColumn) child;
					break;
				}
			}
			if (indexableColumn == null) {
				// Add the column to the list
				indexableColumn = new IndexableColumn();
				indexableColumn.setName(columnName);
				indexableColumn.setFieldName(columnName);
				indexableColumn.setAddress(Boolean.FALSE);
				indexableColumn.setAnalyzed(Boolean.TRUE);
				indexableColumn.setFieldName(columnName);
				indexableColumn.setIdColumn(Boolean.FALSE);
				indexableColumn.setNumeric(Boolean.FALSE);
				indexableColumn.setParent(indexable);
				indexableColumn.setStored(Boolean.TRUE);
				indexableColumn.setStrategies(indexable.getStrategies());
				indexableColumn.setVectored(Boolean.TRUE);
			}
			sortedIndexableColumns.add(indexableColumn);
		}
		return sortedIndexableColumns;
	}

}