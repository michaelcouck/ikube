package ikube.action.index.handler.database;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.content.ByteOutputStream;
import ikube.action.index.content.ColumnContentProvider;
import ikube.action.index.content.IContentProvider;
import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.database.DatabaseUtilities;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.FILE;
import ikube.toolkit.HASH;
import ikube.toolkit.THREAD;
import org.apache.lucene.document.Document;
import org.omg.SendingContext.RunTime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * This class performs the indexing of tables. It is the primary focus of Ikube. This class is essentially a database crawler,
 * and is multi threaded.
 * <p/>
 * Tables are hierarchical, as such the configuration is also. The operation is as follows:
 * <p/>
 * 1) Sql will be generated to select tables and sub tables with an inner join(to be changed to left outer perhaps)<br>
 * 2) Move to the first row of the joined result set<br>
 * 3) Set all the data from the sql query in the columns in the table objects for all tables including the sub tables<br>
 * 4) Add all the column data to the Lucene document for the current row in the result set<br>
 * 5) Repeat until all records are exhausted<br>
 * <p/>
 * This allows arbitrarily complex data structures in databases to be indexed.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-11-2010
 */
public class IndexableTableHandler extends IndexableHandler<IndexableTable> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ForkJoinTask<?> handleIndexableForked(final IndexContext indexContext, final IndexableTable indexableTable) throws Exception {
        IResourceProvider<ResultSet> resourceProvider = new TableResourceProvider(indexContext, indexableTable);
        return getRecursiveAction(indexContext, indexableTable, resourceProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<?> handleResource(final IndexContext indexContext, final IndexableTable indexableTable, final Object resource) {
        ResultSet resultSet = (ResultSet) resource;
        IContentProvider<IndexableColumn> contentProvider = new ColumnContentProvider();
        try {
            do {
                Document document = new Document();
                // The result set is already moved to the first row, i.e. next()
                try {
                    handleRow(indexableTable, resultSet, document, contentProvider);
                    // Add the document to the index
                    resourceHandler.handleResource(indexContext, indexableTable, document, null);
                    THREAD.sleep(indexContext.getThrottle());
                } catch (final Exception e) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new RuntimeException(e);
                    }
                    handleException(indexableTable, e, "Exception indexing table : " + indexableTable.getName());
                }
            } while (resultSet.next());
        } catch (final SQLException e) {
            handleException(indexableTable, e, "Exception indexing table : " + indexableTable.getName());
        }
        DatabaseUtilities.closeAll(resultSet);
        return null;
    }

    protected void handleRow(final IndexableTable indexableTable, final ResultSet resultSet, final Document currentDocument,
                             final IContentProvider<IndexableColumn> contentProvider) throws Exception {
        StringBuilder builder = new StringBuilder();
        indexableTable.setContent(builder);
        // We have results from the table and we are already on the first result
        List<Indexable> children = indexableTable.getChildren();
        // Set the column types and the data from the table in the column objects
        setColumnTypesAndData(children, resultSet);
        // Set the id field
        setIdField(indexableTable, currentDocument);
        for (final Indexable indexable : children) {
            // Handle all the columns, if any column refers to another column then they
            // must be configured in the correct order so that the name column is before the
            // binary data for the document for example
            if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
                IndexableColumn indexableColumn = (IndexableColumn) indexable;
                handleColumn(contentProvider, indexableColumn, currentDocument);
            }
        }
        for (final Indexable indexable : children) {
            if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
                IndexableTable childTable = (IndexableTable) indexable;
                handleRow(childTable, resultSet, currentDocument, contentProvider);
            }
        }
    }

    /**
     * This method handles a column. Essentially what this means is that the data from the table is extracted and added to the document, in the field specified.
     *
     * @param indexableColumn the column to extract the data from and add to the document
     * @param document        the document to add the data to using the field name specified in the column definition
     * @throws Exception
     */
    protected void handleColumn(final IContentProvider<IndexableColumn> contentProvider, final IndexableColumn indexableColumn, final Document document)
            throws Exception {
        InputStream inputStream = null;
        OutputStream parsedOutputStream = null;
        ByteOutputStream byteOutputStream = null;
        try {
            String mimeType = null;
            if (indexableColumn.getNameColumn() != null) {
                if (indexableColumn.getNameColumn().getContent() != null) {
                    mimeType = indexableColumn.getNameColumn().getContent().toString();
                }
            }

            byteOutputStream = new ByteOutputStream();
            contentProvider.getContent(indexableColumn, byteOutputStream);
            if (byteOutputStream.size() == 0) {
                return;
            }

            byte[] buffer = byteOutputStream.getBytes();
            int length = Math.min(buffer.length, 1024);
            byte[] bytes = new byte[length];
            indexableColumn.setRawContent(buffer);

            System.arraycopy(buffer, 0, bytes, 0, bytes.length);

            inputStream = new ByteArrayInputStream(buffer, 0, byteOutputStream.getCount());
            IParser parser = ParserProvider.getParser(mimeType, bytes);
            parsedOutputStream = parser.parse(inputStream, new ByteOutputStream());

            String fieldName = indexableColumn.getFieldName() != null ? indexableColumn.getFieldName() : indexableColumn.getName();
            String fieldContent = parsedOutputStream.toString();
            if (indexableColumn.isNumeric()) {
                if (indexableColumn.isHashed()) {
                    fieldContent = HASH.hash(fieldContent).toString();
                }
                IndexManager.addNumericField(fieldName, fieldContent, document, indexableColumn.isStored(), indexableColumn.getBoost());
            } else {
                IndexManager.addStringField(fieldName, fieldContent, indexableColumn, document);
            }
            // Concatenate the column data to the table indexable content
            IndexableTable indexableTable = (IndexableTable) indexableColumn.getParent();
            @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
            StringBuilder builder = (StringBuilder) indexableTable.getContent();
            builder.append(" ");
            builder.append(fieldContent);
        } finally {
            FILE.close(inputStream);
            FILE.close(parsedOutputStream);
            FILE.close(byteOutputStream);
        }
    }

    /**
     * Sets the id field for this document. Typically the id for the document in the index is unique in the index but it may not be. It is always a good idea to
     * have a unique field, but a table may be indexed twice of course, in which case there will be duplicates in the id fields.
     *
     * @param indexableTable the table to get the id for
     * @param document       the document to set the id field in
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    protected void setIdField(final IndexableTable indexableTable, final Document document) {
        List<Indexable> children = indexableTable.getChildren();
        IndexableColumn idColumn = QueryBuilder.getIdColumn(children);
        if (idColumn == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(indexableTable.getName());
        builder.append(' ');
        builder.append(idColumn.getName());
        builder.append(' ');
        builder.append(idColumn.getContent());

        String id = builder.toString();
        IndexManager.addStringField(IConstants.ID, id, idColumn, document);
    }

    /**
     * This method sets the data from the table columns in the column objects as well as the type which is gotten from the result set meta data.
     *
     * @param children  the children indexables of the table object
     * @param resultSet the result set for the table
     */
    protected void setColumnTypesAndData(final List<Indexable> children, final ResultSet resultSet) {
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            for (int i = 0, j = 1; i < children.size(); i++) {
                Indexable indexable = children.get(i);
                if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
                    IndexableColumn indexableColumn = (IndexableColumn) indexable;
                    String columnName = indexableColumn.getName();
                    if (columnName.equalsIgnoreCase(resultSetMetaData.getColumnName(j))) {
                        Object object = resultSet.getObject(j);
                        int columnType = resultSetMetaData.getColumnType(j);
                        indexableColumn.setContent(object);
                        indexableColumn.setColumnType(columnType);
                        j++;
                    }
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

}