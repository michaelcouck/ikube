package ikube;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.LOGGING;
import ikube.toolkit.STRING;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2013
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractTest {

    static {
        LOGGING.configure();
    }

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected File createIndexFileSystem(
            final IndexContext indexContext,
            final long time,
            final String ip,
            final String... strings) {
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, time, ip);
        addDocuments(indexWriter, IConstants.CONTENTS, strings);
        File indexDirectory = ((FSDirectory) indexWriter.getDirectory()).getDirectory();
        IndexManager.closeIndexWriter(indexWriter);
        return indexDirectory;
    }

    protected void addDocuments(
            final IndexWriter indexWriter,
            final String field,
            final String... strings) {
        try {
            for (final String string : strings) {
                String id = Long.toString(System.currentTimeMillis());
                Document document = getDocument(id, string, field);
                indexWriter.addDocument(document);
            }
            indexWriter.commit();
            indexWriter.maybeMerge();
        } catch (final NullPointerException e) {
            logger.error("Null pointer, mock? : " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Document getDocument(final String id, final String string, final String field) {
        Document document = new Document();
        Indexable indexable = getIndexable();
        IndexManager.addStringField(IConstants.ID, id, indexable, document);
        IndexManager.addStringField(IConstants.NAME, string, indexable, document);
        if (STRING.isNumeric(string.trim())) {
            IndexManager.addNumericField(field, string.trim(), document, Boolean.TRUE, indexable.getBoost());
        } else {
            IndexManager.addStringField(field, string, indexable, document);
        }
        return document;
    }

    protected Indexable getIndexable() {
        Indexable indexable = new Indexable() {
        };
        indexable.setAnalyzed(Boolean.TRUE);
        indexable.setOmitNorms(Boolean.TRUE);
        indexable.setStored(Boolean.TRUE);
        indexable.setVectored(Boolean.FALSE);
        indexable.setTokenized(Boolean.TRUE);
        return indexable;
    }

}