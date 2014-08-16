package ikube.action;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 05-01-2012
 */
@Ignore
@SuppressWarnings("SpringJavaAutowiringInspection")
public class IndexDeltaIntegration extends IntegrationTest {

    /**
     * Class under test.
     */
    @Autowired
    private Index index;
    @Autowired
    @Qualifier("desktop")
    private IndexContext indexContext;
    @Autowired
    @Qualifier("desktopFolder")
    private IndexableFileSystem indexableFileSystem;

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
    }

    @Test
    public void execute() throws Exception {
        indexContext.setDelta(Boolean.TRUE);
        File secondDeltaFile = null;
        try {
            String inserted = "Delta file data";
            // Create the index, delta or otherwise
            File deltaFile = FileUtilities.findFileRecursively(new File("."), "delta.txt");
            OutputStream outputStream = new FileOutputStream(deltaFile);
            IOUtils.write(inserted.getBytes(), outputStream);
            IOUtils.closeQuietly(outputStream);
            indexableFileSystem.setPath(deltaFile.getParentFile().getAbsolutePath());

            // Indexes the file and results in one hit
            executeDelta();
            verifyNumDocs(1);
            verifyIndex(inserted);

            // Touch the file and it should be re-indexed
            FileUtils.touch(deltaFile);
            executeDelta();
            verifyNumDocs(1);
            verifyIndex(inserted);

            // Modify a file on the file system
            String random = appendRandomString(deltaFile);
            // Re-indexes the file, deletes the old entry and results in still one hit
            executeDelta();
            verifyIndex(random);
            verifyNumDocs(1);

            // Now add a file with the same name in a different folder
            @SuppressWarnings("StringBufferReplaceableByString")
            StringBuilder stringBuilder = new StringBuilder(deltaFile.getParentFile().getAbsolutePath());
            stringBuilder.append(IConstants.SEP);
            stringBuilder.append("delta-folder");
            stringBuilder.append(IConstants.SEP);
            stringBuilder.append(deltaFile.getName());

            secondDeltaFile = FileUtilities.getFile(stringBuilder.toString(), Boolean.FALSE);
            outputStream = new FileOutputStream(secondDeltaFile);
            IOUtils.write("Second delta file".getBytes(), outputStream);
            IOUtils.closeQuietly(outputStream);

            random = appendRandomString(secondDeltaFile);
            // Only a hit from the second delta file
            executeDelta();
            logger.info("Searching for random : " + random);
            verifyIndex(random);
            verifyNumDocs(2);

            // No documents should be added after here
            logger.warn("******************** NO DOCUMENTS *************************************");
            executeDelta();
            verifyIndex(random);

            // Verify that the length and the time stamps are the same and the number in the index
        } finally {
            if (secondDeltaFile != null) {
                FileUtilities.deleteFile(secondDeltaFile.getParentFile(), 1);
            }
        }
    }

    private void verifyIndex(final String random) throws Exception {
        IndexReader indexReader = getIndexReader();
        for (int i = 0; i < indexReader.numDocs(); i++) {
            Document document = indexReader.document(i);
            for (final IndexableField fieldable : document.getFields()) {
                String fieldableValue = fieldable.stringValue();
                logger.info("Field value : " + fieldableValue);
                if (fieldableValue.contains(random)) {
                    return;
                }
            }
        }
    }

    private void verifyNumDocs(final int numDocs) throws Exception {
        IndexReader indexReader = getIndexReader();
        assertEquals("There should be this number of documents in the index : ", numDocs, indexReader.numDocs());
    }

    private IndexReader getIndexReader() throws Exception {
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        File indexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
        File indexDirectoryServer = new File(indexDirectory, UriUtilities.getIp());

        Directory directory = FSDirectory.open(indexDirectoryServer);
        return DirectoryReader.open(directory);
    }

    private void executeDelta() throws Exception {
        // Execute the delta index
        index.preExecute(indexContext);
        index.execute(indexContext);
        index.postExecute(indexContext);
    }

    private String appendRandomString(final File deltaFile) throws Exception {
        String random = RandomStringUtils.randomAlphabetic(10);
        FileOutputStream fileOutputStream = new FileOutputStream(deltaFile, Boolean.TRUE);
        fileOutputStream.write("\n".getBytes());
        fileOutputStream.write(random.getBytes());
        IOUtils.closeQuietly(fileOutputStream);
        return random;
    }

}