package ikube.experimental;

import com.jcraft.jsch.JSchException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class writes documents to the indexes, some in memory, and some on the disk.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
class Writer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private long lastCommitTime;
    private IndexWriter indexWriter;

    Writer() throws IOException {
        lastCommitTime = System.currentTimeMillis();
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        indexWriter = new IndexWriter(new RAMDirectory(), indexWriterConfig);
    }

    void writeToIndex(final Document document) throws IOException {
        indexWriter.addDocument(document);
        if (System.currentTimeMillis() - lastCommitTime > 5000) {
            indexWriter.commit();
            logger.info("Num docs writer : " + indexWriter.numDocs());
            lastCommitTime = System.currentTimeMillis();
        }
    }

    List<Document> createDocuments(final List<List<Object>> records) throws SQLException, JSchException {
        List<Document> documents = new ArrayList<>();
        for (final List<Object> row : records) {
            int counter = 0;
            Document document = new Document();
            for (final Object value : row) {
                String fieldName = Integer.toString(counter);
                String fieldValue = value != null ? value.toString() : "";
                IndexableField indexableField = new StringField(fieldName, fieldValue, Field.Store.YES);
                document.add(indexableField);
                counter++;
            }
            documents.add(document);
        }
        return documents;
    }

    Directory[] getDirectories() {
        return new Directory[]{indexWriter.getDirectory()};
    }

}