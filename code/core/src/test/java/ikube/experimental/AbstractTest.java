package ikube.experimental;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class AbstractTest {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    Directory[] getDirectories(final int numberOfDirectories) throws IOException {
        Directory[] directories = new Directory[numberOfDirectories];
        for (int i = 0; i < numberOfDirectories; i++) {
            Directory directory = new RAMDirectory();
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
            for (int j = 0; j < numberOfDirectories; j++) {
                Document document = new Document();
                String fieldName = Integer.toString(j);
                String fieldValue = Integer.toString(j);
                StringField stringField = new StringField("string-".concat(fieldName), fieldValue, Field.Store.YES);

                FieldType floatFieldType = new FieldType();
                floatFieldType.setStored(Boolean.TRUE);
                floatFieldType.setIndexed(Boolean.TRUE);
                floatFieldType.setNumericType(FieldType.NumericType.FLOAT);
                // To sort on these fields they must not be tokenized for some reason
                floatFieldType.setTokenized(Boolean.FALSE);
                floatFieldType.setOmitNorms(Boolean.FALSE);
                Field floatField = new FloatField("float-".concat(fieldName), i, floatFieldType);

                document.add(stringField);
                document.add(floatField);

                indexWriter.addDocument(document);
            }
            indexWriter.commit();
            indexWriter.close();
            directories[i] = indexWriter.getDirectory();
        }
        return directories;
    }

    /**
     * This method will just print the data in the index reader.L
     *
     * @param indexReader the reader to print the documents for
     * @throws Exception
     */
    protected void printIndex(final IndexReader indexReader, final int numDocs) throws Exception {
        logger.error("Num docs : " + indexReader.numDocs());
        for (int i = 0; i < numDocs && i < indexReader.numDocs(); i++) {
            Document document = indexReader.document(i);
            logger.error("Document : " + i + ", " + document.toString().length());
            printDocument(document);
        }
    }

    protected void printDocument(final Document document) {
        List<IndexableField> fields = document.getFields();
        for (IndexableField indexableField : fields) {
            logger.error("        : {}", indexableField);
        }
    }

}
