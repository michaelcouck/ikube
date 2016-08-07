package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.STRING;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

import java.util.List;

/**
 * This handler is for iterating over a row in a csv file and adding the columns as fields to the index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 26-01-2013
 */
public class RowResourceHandler extends ResourceHandler<IndexableFileSystemCsv> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Document handleResource(final IndexContext indexContext, final IndexableFileSystemCsv indexableFileSystemCsv,
                                   final Document document, final Object resource) throws Exception {
        @SuppressWarnings("unchecked")
        List<IndexableColumn> indexableColumns = (List<IndexableColumn>) resource;

        String fileName = FilenameUtils.getName(indexableFileSystemCsv.getPath());
        int lineNumber = indexableFileSystemCsv.getLineNumber();
        String lineNumberFieldName = indexableFileSystemCsv.getLineNumberFieldName();
        String identifier = StringUtils.join(new Object[]{fileName, Integer.toString(lineNumber)}, IConstants.SPACE);
        // Add the line number field
        IndexManager.addStringField(lineNumberFieldName, identifier, indexableFileSystemCsv, document);
        for (final Indexable indexable : indexableColumns) {
            IndexableColumn indexableColumn = (IndexableColumn) indexable;
            String fieldName = indexableColumn.getFieldName();
            String fieldValue = (String) indexableColumn.getContent();

            if (fieldValue == null) {
                continue;
            }
            if (STRING.isNumeric(fieldValue)) {
                IndexManager.addNumericField(fieldName, fieldValue, document, indexableColumn.isStored(), indexableColumn.getBoost());
            } else {
                fieldValue = STRING.strip(fieldValue, "\"");
                IndexManager.addStringField(fieldName, fieldValue, indexableFileSystemCsv, document);
            }
            indexableColumn.setContent(null);
        }
        addDocument(indexContext, document);
        return document;
    }

}
