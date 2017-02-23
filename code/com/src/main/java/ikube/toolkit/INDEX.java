package ikube.toolkit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-12-2015
 */
public class INDEX {

    public static IndexWriter getRamIndexWriter() throws IOException {
        Directory directory = new RAMDirectory();
        return new IndexWriter(directory, getIndexWriterConfig());
    }

    public static IndexWriter getDiskIndexWriter() throws IOException {
        Directory directory = FSDirectory.open(new File("./target/index"));
        return new IndexWriter(directory, getIndexWriterConfig());
    }

    private static IndexWriterConfig getIndexWriterConfig() {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        indexWriterConfig.setRAMBufferSizeMB(512);
        indexWriterConfig.setMaxBufferedDocs(10000);
        return indexWriterConfig;
    }

    public static void commitMerge(final IndexWriter indexWriter) {
        if (indexWriter.hasUncommittedChanges()) {
            try {
                indexWriter.commit();
                indexWriter.forceMerge(5);
                indexWriter.waitForMerges();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Document addStringField(final String fieldName, final String fieldContent, final boolean analyzed,
                                          final boolean stored, final boolean tokenized, final boolean omitNorms, final boolean vectored,
                                          final float boost, final Document document) {
        FieldType fieldType = new FieldType();
        fieldType.setIndexed(analyzed);
        fieldType.setStored(stored);
        // NOTE: Must be tokenized to search correctly, not tokenized? no results!!!
        fieldType.setTokenized(tokenized);
        // For normalization of the length, i.e. longer strings are scored higher
        // normally, but consider that a book with the word 3 times but 10 000 words, will
        // get a lower score than a sentence of two exact words. Omitting the norms will force the book
        // to get a higher score because it has the word three times, although the 'natural' relevance
        // would be the sentence with two words because of the length and matches
        fieldType.setOmitNorms(omitNorms);
        // NOTE: If the term vectors are enabled the field cannot be searched, i.e. no results!!!
        fieldType.setStoreTermVectors(vectored);

        Field field;
        Field oldField = (Field) document.getField(fieldName);
        if (oldField == null) {
            field = new Field(fieldName, fieldContent, fieldType);
        } else {
            document.removeField(fieldName);
            field = new Field(fieldName, oldField.stringValue() + " " + fieldContent, fieldType);
        }
        if (boost > 0) {
            field.setBoost(boost);
        }
        document.add(field);
        return document;
    }

    public static Document addNumericField(final String fieldName, final String fieldContent, final boolean store, final float
            boost, final Document document) {
        FieldType floatFieldType = new FieldType();
        floatFieldType.setStored(store);
        floatFieldType.setIndexed(Boolean.TRUE);
        floatFieldType.setNumericType(FieldType.NumericType.FLOAT);
        // To sort on these fields they must not be tokenized for some reason
        floatFieldType.setTokenized(Boolean.FALSE);
        floatFieldType.setOmitNorms(Boolean.FALSE);

        Field floatField = new FloatField(fieldName, Float.parseFloat(fieldContent), floatFieldType);
        if (boost > 0) {
            floatField.setBoost(boost);
        }
        document.add(floatField);
        return document;
    }

}
