package ikube.experimental;

import com.jcraft.jsch.JSchException;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.toolkit.STRING;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class writes documents to the indexes, some in memory, and some on the disk.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
public class Writer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private long lastCommitTime;
    private IndexWriter indexWriter;

    public Writer() throws IOException {
        lastCommitTime = System.currentTimeMillis();
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        indexWriterConfig.setRAMBufferSizeMB(512);
        indexWriterConfig.setMaxBufferedDocs(10000);
        indexWriter = new IndexWriter(new RAMDirectory(), indexWriterConfig);
    }

    void writeToIndex(final Document document) throws IOException {
        logger.debug("Writing document : " + document.get(IConstants.ID));
        indexWriter.addDocument(document);
        if (indexWriter.hasUncommittedChanges() && System.currentTimeMillis() - lastCommitTime > 5000) {
            lastCommitTime = System.currentTimeMillis();
            indexWriter.commit();
            indexWriter.forceMerge(5);
            logger.info("Num docs writer : " + indexWriter.numDocs());
        }
    }

    List<Document> createDocuments(final List<Map<Object, Object>> records) throws SQLException, JSchException {
        List<Document> documents = new ArrayList<>();
        for (final Map<Object, Object> row : records) {
            int counter = 0;
            Document document = new Document();
            for (final Map.Entry<Object, Object> mapEntry : row.entrySet()) {
                String fieldName = mapEntry.getKey().toString();
                String fieldValue = mapEntry.getValue() != null ? mapEntry.getValue().toString() : "";
                if (STRING.isNumeric(fieldValue)) {
                    IndexManager.addNumericField(fieldName, fieldValue, document, true, 0);
                } else {
                    IndexManager.addStringField(document, fieldName, fieldValue, true, true, true, false, true, 0);
                }
                counter++;
            }
            documents.add(document);
            if (counter % 1000 == 0) {
                logger.info("Documents created : " + documents.size() + " : " + counter);
            }
        }
        return documents;
    }

    Directory[] getDirectories() {
        return new Directory[]{indexWriter.getDirectory()};
    }

}