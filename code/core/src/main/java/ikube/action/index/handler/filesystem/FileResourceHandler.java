package ikube.action.index.handler.filesystem;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FILE;
import ikube.toolkit.HASH;
import org.apache.lucene.document.Document;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXParseException;

import java.io.*;

/**
 * This resource handler will handle files from the file system handler. Getting the correct parser for the type and
 * extracting the data, eventually adding the data to the specified fields in the Lucene index.
 *
 * Switched parsers to Tika from Apache.
 *
 * @author Michael Couck
 * @version 01.10
 * @since 25-03-2013
 */
public class FileResourceHandler extends ResourceHandler<IndexableFileSystem> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Document handleResource(
            final IndexContext indexContext,
            final IndexableFileSystem indexableFileSystem,
            final Document document,
            final Object resource)
            throws Exception {
        File file = (File) resource;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Processing file : " + file);
            }
            handleResource(indexContext, indexableFileSystem, document, file);
        } catch (Exception e) {
            if (SAXParseException.class.isAssignableFrom(e.getClass())) {
                // If this is an xml exception then try the html parser it is more lenient
                handleResource(indexContext, indexableFileSystem, document, file);
            }
        }
        return document;
    }

    private Document handleResource(
            final IndexContext indexContext,
            final IndexableFileSystem indexableFileSystem,
            final Document document,
            final File file)
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

            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler((int) indexableFileSystem.getMaxReadLength());
            Metadata metadata = new Metadata();

            parser.parse(byteInputStream, handler, metadata);
            String parsedContent = handler.toString();

            // This is the unique id of the resource to be able to delete it
            String fileId = HASH.hash(file.getAbsolutePath()).toString();
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
            addDocument(indexContext, document);

            logger.info("Read length : " + length + ", file : " + file + ", " + (parsedContent != null ? parsedContent.length() : 0));
            if (logger.isDebugEnabled()) {
            }

            indexableFileSystem.setContent(parsedContent);
        } finally {
            FILE.close(inputStream);
            FILE.close(byteInputStream);
            FILE.close(byteOutputStream);
        }
        return document;
    }

}
