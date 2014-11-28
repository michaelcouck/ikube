package ikube.action.index.handler.filesystem;

import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemLog;
import ikube.toolkit.FILE;
import org.apache.lucene.document.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

/**
 * This handler is for indexing log files one line at a time.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-08-2014
 */
public class LogFileResourceHandler extends ResourceHandler<IndexableFileSystemLog> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Document handleResource(final IndexContext indexContext, final IndexableFileSystemLog indexable, final Document document, final Object resource)
            throws Exception {
        File logFile = (File) resource;

        Reader reader = null;
        BufferedReader bufferedReader = null;

        int lineNumber = 1;
        try {
            reader = new FileReader(logFile);
            bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            for (lineNumber = 0; line != null; lineNumber++) {
                Document lineDocument = new Document();
                String fileFieldName = indexable.getFileFieldName();
                String pathFieldName = indexable.getPathFieldName();
                String lineFieldName = indexable.getLineFieldName();
                String stringLineNumber = Integer.toString(lineNumber);
                String contentFieldName = indexable.getContentFieldName();

                IndexManager.addStringField(fileFieldName, logFile.getName(), indexable, lineDocument);
                IndexManager.addStringField(pathFieldName, logFile.getAbsolutePath(), indexable, lineDocument);
                IndexManager.addStringField(lineFieldName, stringLineNumber, indexable, lineDocument);
                IndexManager.addStringField(contentFieldName, line, indexable, lineDocument);

                addDocument(indexContext, lineDocument);

                line = bufferedReader.readLine();
                Thread.sleep(indexContext.getThrottle());
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            FILE.close(bufferedReader);
            FILE.close(reader);
        }
        logger.info("Indexed lines : " + lineNumber);

        return document;
    }

}
