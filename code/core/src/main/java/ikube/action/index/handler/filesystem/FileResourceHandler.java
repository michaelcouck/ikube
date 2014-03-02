package ikube.action.index.handler.filesystem;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
import org.apache.lucene.document.Document;
import org.xml.sax.SAXParseException;

import java.io.*;

/**
 * This resource handler will handle files from the file system handler. Getting the correct parser for the type and
 * extracting the data, eventually adding the data to the specified fields in the Lucene index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25-03-2013
 */
public class FileResourceHandler extends ResourceHandler<IndexableFileSystem> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Document handleResource(
            final IndexContext<?> indexContext,
            final IndexableFileSystem indexableFileSystem,
            final Document document,
            final Object resource)
            throws Exception {
        File file = (File) resource;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Processing file : " + file);
            }
            handleResource(indexContext, indexableFileSystem, document, file, file.getName());
        } catch (Exception e) {
            if (SAXParseException.class.isAssignableFrom(e.getClass())) {
                // If this is an xml exception then try the html parser it is more lenient
                handleResource(indexContext, indexableFileSystem, document, file, "text/html");
            }
        }
        return document;
    }

    private Document handleResource(
            final IndexContext<?> indexContext,
            final IndexableFileSystem indexableFileSystem,
            final Document document,
            final File file,
            final String mimeType)
            throws Exception {
        InputStream inputStream = null;
        ByteArrayInputStream byteInputStream = null;
        ByteArrayOutputStream byteOutputStream = null;

        try {
            int length = (int) Math.min(file.length(), indexableFileSystem.getMaxReadLength());
            byte[] byteBuffer = new byte[length];

            if (TFile.class.isAssignableFrom(file.getClass())) {
                inputStream = new TFileInputStream(file);
            } else {
                inputStream = new FileInputStream(file);
            }

            int read = inputStream.read(byteBuffer, 0, byteBuffer.length);
            indexableFileSystem.setRawContent(byteBuffer);

            byteInputStream = new ByteArrayInputStream(byteBuffer, 0, read);
            byteOutputStream = new ByteArrayOutputStream();

            IParser parser = ParserProvider.getParser(file.getName(), byteBuffer);
            String parsedContent = parser.parse(byteInputStream, byteOutputStream).toString();

            // This is the unique id of the resource to be able to delete it
            String fileId = HashUtilities.hash(file.getAbsolutePath()).toString();
            String pathFieldName = indexableFileSystem.getPathFieldName();
            String nameFieldName = indexableFileSystem.getNameFieldName();
            String modifiedFieldName = indexableFileSystem.getLastModifiedFieldName();
            String lengthFieldName = indexableFileSystem.getLengthFieldName();
            String contentFieldName = indexableFileSystem.getContentFieldName();

            // NOTE to self: To be able to delete using the index writer the identifier field must be non analyzed and non tokenized/vectored!
            IndexManager.addStringField(IConstants.FILE_ID, fileId, indexableFileSystem, document);
            IndexManager.addStringField(pathFieldName, file.getAbsolutePath(), indexableFileSystem, document);
            IndexManager.addStringField(nameFieldName, file.getName(), indexableFileSystem, document);
            IndexManager.addNumericField(modifiedFieldName, Long.toString(file.lastModified()), document, Boolean.TRUE, indexableFileSystem.getBoost());
            IndexManager.addStringField(lengthFieldName, Long.toString(file.length()), indexableFileSystem, document);
            IndexManager.addStringField(contentFieldName, parsedContent, indexableFileSystem, document);
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
