package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.StringUtilities;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

public class RowResourceHandler extends ResourceHandler<IndexableFileSystemCsv> {

	/**
	 * 
	 * @param indexContext
	 * @param indexableFileSystemCsv
	 * @param document
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	@Override
	public Document handleResource(IndexContext<?> indexContext, IndexableFileSystemCsv indexableFileSystemCsv, Document document, Object resource)
			throws Exception {
		// fileName, lineNumber, lineNumberFieldName
		String fileName = indexableFileSystemCsv.getFile().getName();
		int lineNumber = indexableFileSystemCsv.getLineNumber();
		String lineNumberFieldName = indexableFileSystemCsv.getLineNumberFieldName();
		List<Indexable<?>> indexableColumns = indexableFileSystemCsv.getChildren();

		String identifier = StringUtils.join(new Object[] { fileName, Integer.toString(lineNumber) }, IConstants.SPACE);
		// Add the line number field
		IndexManager.addStringField(lineNumberFieldName, identifier, indexableFileSystemCsv, document);
		for (Indexable<?> indexable : indexableColumns) {
			if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				continue;
			}
			IndexableColumn indexableColumn = (IndexableColumn) indexable;
			String fieldName = indexableColumn.getFieldName();
			String fieldValue = (String) indexableColumn.getContent();

			if (fieldValue == null) {
				continue;
			}
			if (StringUtilities.isNumeric(fieldValue)) {
				IndexManager.addNumericField(fieldName, fieldValue, document, indexableColumn.isStored());
			} else {
				fieldValue = StringUtilities.strip(fieldValue, "\"");
				IndexManager.addStringField(fieldName, fieldValue, indexableFileSystemCsv, document);
			}
			indexableColumn.setContent(null);
		}

		addDocument(indexContext, indexableFileSystemCsv, document);

		return document;
	}

}
