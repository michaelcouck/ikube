package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.handler.strategy.GeospatialEnrichmentStrategy;
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

	/** TODO : Remove after testing. */
	private IStrategy strategy;
	@Autowired
	private RowResourceHandler rowResourceHandler;

	@Override
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final IndexableFileSystemCsv indexableFileSystem) throws Exception {
		IResourceProvider<File> resourceProvider = new FileSystemCsvResourceProvider(indexableFileSystem.getPath());
		return getRecursiveAction(indexContext, indexableFileSystem, resourceProvider);
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
	void handleFile(final IndexContext<?> indexContext, final IndexableFileSystemCsv indexableFileSystemCsv, final File file) throws Exception {
		String encoding = indexableFileSystemCsv.getEncoding() != null ? indexableFileSystemCsv.getEncoding() : IConstants.ENCODING;
		logger.info("Using encoding for file : " + encoding + ", " + file);
		LineIterator lineIterator = FileUtils.lineIterator(file, encoding);
		try {
			// The first line is the header, i.e. the columns of the file
			String separator = indexableFileSystemCsv.getSeparator();
			String headerLine = lineIterator.nextLine();
			String[] columns = StringUtils.splitPreserveAllTokens(headerLine, separator);
			// Trim any space on the column headers
			for (int i = 0; i < columns.length; i++) {
				columns[i] = columns[i].trim();
			}

			List<Indexable<?>> indexableColumns = getIndexableColumns(indexableFileSystemCsv, columns);
			indexableFileSystemCsv.setChildren(indexableColumns);
			logger.info("Doing columns : " + indexableColumns.size());

			int lineNumber = 0;
			indexableFileSystemCsv.setFile(file);
			Map<Integer, String> differentLines = new HashMap<>();
			while (lineIterator.hasNext() && ThreadUtilities.isInitialized() && lineNumber < indexableFileSystemCsv.getMaxLines()) {
				indexableFileSystemCsv.setLineNumber(lineNumber);
				try {
					String line = lineIterator.nextLine();
					String[] values = StringUtils.splitPreserveAllTokens(line, separator);
					if (indexableColumns.size() != values.length) {
						differentLines.put(lineNumber, Arrays.deepToString(values));
					}
					for (int i = 0; i < values.length && i < indexableColumns.size(); i++) {
						IndexableColumn indexableColumn = (IndexableColumn) indexableColumns.get(i);
						indexableColumn.setContent(values[i]);
					}
					Document document = new Document();
					// TODO : Remove this, this is done in the configuration, just for test here
					// strategy.aroundProcess(indexContext, indexableFileSystemCsv, document, file);
					rowResourceHandler.handleResource(indexContext, indexableFileSystemCsv, document, file);
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

	protected List<Indexable<?>> getIndexableColumns(final IndexableFileSystemCsv indexable, final String[] columns) {
		List<Indexable<?>> indexableColumns = indexable.getChildren();
		if (indexableColumns == null) {
			indexableColumns = new ArrayList<>();
			indexable.setChildren(indexableColumns);
		}
		if (!indexable.isAllColumns()) {
			return indexable.getChildren();
		}
		List<Indexable<?>> sortedIndexableColumns = new ArrayList<>();
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