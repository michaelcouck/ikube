package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * TODO: Document me...
 *
 * @author Michael Couck
 * @version 02.00
 * @since 08-02-2011
 */
class FileSystemCsvResourceProvider implements IResourceProvider<List<IndexableColumn>> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private int lineNumber = 0;
    private boolean terminated;
    private LineIterator lineIterator;
    private IndexableFileSystemCsv indexableFileSystemCsv;
    private Stack<List<IndexableColumn>> resources = new Stack<>();
    private Stack<List<IndexableColumn>> used_resources = new Stack<>();

    FileSystemCsvResourceProvider(final IndexableFileSystemCsv indexableFileSystemCsv) throws IOException {
        this.indexableFileSystemCsv = indexableFileSystemCsv;

        File file = new File(indexableFileSystemCsv.getPath());
        indexableFileSystemCsv.setFile(file);
        String encoding = indexableFileSystemCsv.getEncoding() != null ? indexableFileSystemCsv.getEncoding() : IConstants.ENCODING;
        logger.info("Using encoding for file : " + encoding + ", " + file);
        lineIterator = FileUtils.lineIterator(file, encoding);

        // The first line is the header, i.e. the columns of the file
        String separator = indexableFileSystemCsv.getSeparator();
        String headerLine = lineIterator.nextLine();
        String[] columns = StringUtils.splitPreserveAllTokens(headerLine, separator);
        List<Indexable> indexableColumns = getIndexableColumns(indexableFileSystemCsv, columns);
        // Trim any space on the column headers
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
            for (final Indexable indexable : indexableColumns) {
                IndexableColumn indexableColumn = (IndexableColumn) indexable;
                if (indexableColumn.getName().equals(columns[i])) {
                    indexableColumn.setIndex(i);
                }
            }
        }


        indexableFileSystemCsv.setChildren(indexableColumns);
        logger.info("Doing columns : " + indexableColumns.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<IndexableColumn> getResource() {
        if (resources.size() == 0) {
            replenishResources();
        }
        notifyAll();
        return this.resources.size() > 0 ? this.resources.pop() : null;
    }

    private synchronized void replenishResources() {
        logger.warn("Replenish resources : line number : " + lineNumber +
                ", max lines : " + indexableFileSystemCsv.getMaxLines() +
                ", terminated : " + isTerminated() +
                ", has next : " + lineIterator.hasNext() +
                ", resources size : " + resources.size());
        if (lineNumber < indexableFileSystemCsv.getMaxLines() && !isTerminated()) {
            while (lineIterator.hasNext() && resources.size() < 1000) {
                String line = lineIterator.nextLine();
                String[] values = StringUtils.splitPreserveAllTokens(line, indexableFileSystemCsv.getSeparator());
                List<IndexableColumn> indexableColumns;
                if (this.used_resources.size() > 0) {
                    indexableColumns = used_resources.pop();
                } else {
                    indexableColumns = new ArrayList<>();
                    for (final Indexable indexable : indexableFileSystemCsv.getChildren()) {
                        IndexableColumn indexableColumn = new IndexableColumn();
                        try {
                            BeanUtilsBean.getInstance().copyProperties(indexableColumn, indexable);
                        } catch (final IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                        indexableColumns.add(indexableColumn);
                    }
                }

                for (final IndexableColumn indexableColumn : indexableColumns) {
                    indexableColumn.setContent(values[indexableColumn.getIndex()]);
                    indexableColumn.setRawContent(values[indexableColumn.getIndex()]);
                }

                ++lineNumber;
                if (lineNumber % 10000 == 0) {
                    logger.info("Lines done : " + lineNumber);
                }

                indexableFileSystemCsv.setLineNumber(lineNumber);
                resources.push(indexableColumns);
            }
        }
    }

    @Override
    public void setResources(final List<List<IndexableColumn>> resources) {
        if (resources != null) {
            for (final List<IndexableColumn> indexableColumns : resources) {
                this.used_resources.push(indexableColumns);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTerminated(final boolean terminated) {
        this.terminated = terminated;
    }

    List<Indexable> getIndexableColumns(final IndexableFileSystemCsv indexable, final String[] columns) {
        List<Indexable> indexableColumns = indexable.getChildren();
        if (indexableColumns == null) {
            indexableColumns = new ArrayList<>();
            indexable.setChildren(indexableColumns);
        }
        if (!indexable.isAllColumns()) {
            return indexable.getChildren();
        }
        List<Indexable> sortedIndexableColumns = new ArrayList<>();
        // Add all the columns that are not present in the configuration
        for (final String columnName : columns) {
            IndexableColumn indexableColumn = null;
            for (final Indexable child : indexableColumns) {
                if (((IndexableColumn) child).getFieldName().equals(columnName)) {
                    indexableColumn = (IndexableColumn) child;
                    break;
                }
            }
            if (indexableColumn == null) {
                // Add the column to the list
                indexableColumn = new IndexableColumn();
                indexableColumn.setParent(indexable);
                indexableColumn.setName(columnName);
                indexableColumn.setFieldName(columnName);

                indexableColumn.setStored(Boolean.TRUE);
                indexableColumn.setAnalyzed(Boolean.TRUE);

                indexableColumn.setAddress(Boolean.FALSE);
                indexableColumn.setNumeric(Boolean.FALSE);
                indexableColumn.setIdColumn(Boolean.FALSE);
                indexableColumn.setVectored(Boolean.FALSE);
                indexableColumn.setTokenized(Boolean.FALSE);

                indexableColumn.setStrategies(indexable.getStrategies());
            }
            sortedIndexableColumns.add(indexableColumn);
        }
        return sortedIndexableColumns;
    }

}
