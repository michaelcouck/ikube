package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.xml.sax.SAXParseException;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;

/**
 * This ersource handler will do files, getting the correct parser for the type and extracting the data, eventually adding the data to the specified fields int
 * he Lucene index.
 * 
 * @author Michael Couck
 * @since 25.03.13
 * @version 01.00
 */
public class ResourceFileHandler extends ResourceHandler<IndexableFileSystem> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document handleResource(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final Document document,
			final Object resource) throws Exception {
		File file = (File) resource;
		try {
			handleResource(indexContext, indexableFileSystem, document, file, file.getName());
		} catch (Exception e) {
			if (SAXParseException.class.isAssignableFrom(e.getClass())) {
				// If this is an xml exception then try the html parser it is more lenient
				handleResource(indexContext, indexableFileSystem, document, file, "text/html");
			}
		}
		return document;
	}

	private Document handleResource(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem, final Document document,
			final File file, final String mimeType) throws Exception {
		InputStream inputStream = null;
		ByteArrayInputStream byteInputStream = null;
		ByteArrayOutputStream byteOutputStream = null;

		try {
			int length = file.length() > 0 && file.length() < indexableFileSystem.getMaxReadLength() ? (int) file.length() : (int) indexableFileSystem
					.getMaxReadLength();
			byte[] byteBuffer = new byte[length];

			if (TFile.class.isAssignableFrom(file.getClass())) {
				inputStream = new TFileInputStream(file);
			} else {
				inputStream = new FileInputStream(file);
			}

			int read = inputStream.read(byteBuffer, 0, byteBuffer.length);

			byteInputStream = new ByteArrayInputStream(byteBuffer, 0, read);
			byteOutputStream = new ByteArrayOutputStream();

			IParser parser = ParserProvider.getParser(file.getName(), byteBuffer);
			String parsedContent = parser.parse(byteInputStream, byteOutputStream).toString();

			Store store = indexableFileSystem.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexableFileSystem.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexableFileSystem.isVectored() ? TermVector.YES : TermVector.NO;

			// This is the unique id of the resource to be able to delete it
			String fileId = HashUtilities.hash(file.getAbsolutePath()).toString();
			String pathFieldName = indexableFileSystem.getPathFieldName();
			String nameFieldName = indexableFileSystem.getNameFieldName();
			String modifiedFieldName = indexableFileSystem.getLastModifiedFieldName();
			String lengthFieldName = indexableFileSystem.getLengthFieldName();
			String contentFieldName = indexableFileSystem.getContentFieldName();

			// NOTE to self: To be able to delete using the index writer the identifier field must be non analyzed and non
			// tokenized/vectored!
			IndexManager.addStringField(IConstants.FILE_ID, fileId, document, Store.YES, Index.NOT_ANALYZED, TermVector.NO);
			IndexManager.addStringField(pathFieldName, file.getAbsolutePath(), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(nameFieldName, file.getName(), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(modifiedFieldName, Long.toString(file.lastModified()), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(lengthFieldName, Long.toString(file.length()), document, Store.YES, analyzed, termVector);
			IndexManager.addStringField(contentFieldName, parsedContent, document, store, analyzed, termVector);
			addDocument(indexContext, indexableFileSystem, document);
			
			indexableFileSystem.setContent(parsedContent);
		} finally {
			FileUtilities.close(inputStream);
			FileUtilities.close(byteInputStream);
			FileUtilities.close(byteOutputStream);
		}
		return document;
	}

}
