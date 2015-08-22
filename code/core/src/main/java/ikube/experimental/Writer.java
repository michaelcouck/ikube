package ikube.experimental;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.experimental.listener.*;
import ikube.toolkit.STRING;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
@Component
public class Writer implements IConsumer<IndexWriterEvent>, IProducer<IndexWriterEvent> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Qualifier("ikube.experimental.listener.ListenerManager")
    private ListenerManager listenerManager;

    private IndexWriter indexWriter;

    public Writer() throws IOException {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        indexWriterConfig.setRAMBufferSizeMB(512);
        indexWriterConfig.setMaxBufferedDocs(10000);
        indexWriter = new IndexWriter(new RAMDirectory(), indexWriterConfig);
    }

    @Override
    public void notify(final IndexWriterEvent writerEvent) {
        List<Map<Object, Object>> data = writerEvent.getData();
        if (data != null) {
            process(writerEvent.getContext(), data);
        }
        List<Document> documents = writerEvent.getDocuments();
        if (documents != null) {
            writeToIndex(documents);
        }
        synchronized (this) {
            if (indexWriter.hasUncommittedChanges()) {
                try {
                    indexWriter.commit();
                    indexWriter.forceMerge(5);
                    indexWriter.waitForMerges();
                    logger.debug("Num docs writer : {}", indexWriter.numDocs());
                    // Fire JVM internal event for searcher to open on new directories
                    Context context = writerEvent.getContext();
                    Directory[] directories = new Directory[]{indexWriter.getDirectory()};
                    IEvent<?, ?> searcherEvent = new OpenSearcherEvent(context, directories);
                    listenerManager.fire(searcherEvent, true);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
            notifyAll();
        }
    }

    @Override
    public void fire(final IndexWriterEvent event) {
        listenerManager.fire(event, false);
    }

    public List<Map<Object, Object>> process(final Context context, final List<Map<Object, Object>> data) {
        // Create the Lucene documents from the changed records
        List<Document> documents = createDocuments(data);
        if (documents.size() > 0) {
            logger.debug("Popping documents in grid : {}", documents.size());
            // Pop the documents in the grid to be indexed by all nodes
            IndexWriterEvent indexWriterEvent = new IndexWriterEvent(context, documents, null);
            fire(indexWriterEvent);
        }
        return data;
    }

    public void writeToIndex(final List<Document> documents) {
        for (final Document document : documents) {
            try {
                // TODO: Delete the document by the id first! Only if it exists I guess.
                logger.debug("Writing document : {}", document.get(IConstants.ID));
                indexWriter.addDocument(document);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<Document> createDocuments(final List<Map<Object, Object>> records) {
        // TODO: Parse the data before creating documents for Lucene
        List<Document> documents = new ArrayList<>();
        for (final Map<Object, Object> row : records) {
            Document document = new Document();
            for (final Map.Entry<Object, Object> mapEntry : row.entrySet()) {
                String fieldName = mapEntry.getKey().toString();
                String fieldValue = mapEntry.getValue() != null ? mapEntry.getValue().toString() : "";
                if (STRING.isNumeric(fieldValue)) {
                    IndexManager.addNumericField(fieldName, fieldValue, document, true, 0);
                } else {
                    IndexManager.addStringField(document, fieldName, fieldValue, true, true, true, false, true, 0);
                }
            }
            documents.add(document);
        }
        return documents;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void printIndex(final int numDocs) {
        IndexReader indexReader;
        try {
            indexReader = DirectoryReader.open(indexWriter.getDirectory());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        logger.error("Num docs : " + indexReader.numDocs());
        for (int i = 0; i < numDocs && i < indexReader.numDocs(); i++) {
            try {
                Document document = indexReader.document(i);
                logger.error("Document : " + i + ", " + document.toString().length());
                printDocument(document);
            } catch (final IOException e) {
                logger.error(null, e);
            }
        }
    }

    protected void printDocument(final Document document) {
        List<IndexableField> fields = document.getFields();
        for (IndexableField indexableField : fields) {
            logger.error("        : {}", indexableField);
        }
    }

}