package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This handler is a custom handler for the CSV files. Rather than inserting the data into the database, meaning creating tables and the
 * like, and the painful process of importing the data using some tool, and then the file is updated and automation is required, this
 * handler will just read the 'structured' files line by line and index the data as if it were
 * 
 * @author Michael Couck
 * @since 08.02.2011
 * @version 01.00
 */
public class IndexableFilesystemCsvHandler extends IndexableHandler<IndexableFileSystemCsv> {

	@Autowired
	private ResourceRowHandler resourceRowHandler;

	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableFileSystemCsv indexableFileSystem)
			throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		Runnable runnable = new Runnable() {
			public void run() {
				File[] files = new File(indexableFileSystem.getPath()).listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return !isExcluded(pathname);
					}
				});
				if (files == null || files.length == 0) {
					logger.warn("No files in directory : " + indexableFileSystem.getPath());
					return;
				}
				for (final File file : files) {
					try {
						handleFile(indexContext, indexableFileSystem, file);
					} catch (Exception e) {
						handleException(indexableFileSystem, e);
					}
				}
			}
		};
		Future<?> future = ThreadUtilities.submit(indexContext.getIndexName(), runnable);
		futures.add(future);
		return futures;
	}
	
	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final Indexable<?> indexable, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
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
				String column = columns[i];
				columns[i] = column.trim();
			}

			List<Indexable<?>> indexableColumns = getIndexableColumns(indexableFileSystemCsv, columns);
			indexableFileSystemCsv.setChildren(indexableColumns);

			int lineNumber = 0;
			indexableFileSystemCsv.setFile(file);
			while (lineIterator.hasNext() && ThreadUtilities.isInitialized()) {
				indexableFileSystemCsv.setLineNumber(lineNumber);
				try {
					String line = lineIterator.nextLine();
					String[] values = StringUtils.split(line, separator);
					if (indexableColumns.size() != values.length) {
						logger.warn("Columns and values different on line : " + lineNumber + ", columns : " + columns.length
								+ ", values : " + values.length + ", data : " + Arrays.deepToString(values));
					}
					for (int i = 0; i < values.length && i < indexableColumns.size(); i++) {
						IndexableColumn indexableColumn = (IndexableColumn) indexableColumns.get(i);
						indexableColumn.setContent(values[i]);
					}
					resourceRowHandler.handleResource(indexContext, indexableFileSystemCsv, new Document(), file);
					ThreadUtilities.sleep(indexContext.getThrottle());
				} catch (Exception e) {
					logger.error("Exception processing file : " + file, e);
					handleException(indexableFileSystemCsv, e);
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

	protected synchronized boolean isExcluded(final File file) {
		return !file.getName().endsWith(".csv");
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