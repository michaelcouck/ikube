package ikube.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * This class is a convenience to print the documents in an index from the command line.
 *
 * @author Michael Couck
 * @version 01.00
 * @since at least 23-11-2013
 */
public class IndexPrinter {

    static Logger LOGGER = LoggerFactory.getLogger(IndexPrinter.class);

    /**
     * Prints the Lucene index.
     *
     * @param args the first argument is the absolute path to the index directory
     *             and the second the number of documents in the index to print, for
     *             example => java -jar ikube-tool.jar ikube.data.IndexPrinter /tmp/index 100
     */
    public static void main(final String[] args) {
        IndexReader indexReader = null;
        try {
            File file = new File(args[0]);
            int numDocsToPrint = Integer.parseInt(args[1]);
            Directory directory = NIOFSDirectory.open(file);
            indexReader = DirectoryReader.open(directory);
            LOGGER.error("Num docs : " + indexReader.numDocs());
            for (int i = 0; i < numDocsToPrint && i < indexReader.numDocs(); i++) {
                Document document = indexReader.document(i);
                LOGGER.error("Document : " + i);
                List<IndexableField> fields = document.getFields();
                for (final IndexableField fieldable : fields) {
                    String fieldName = fieldable.name();
                    String fieldValue = fieldable.stringValue();
                    int fieldLength = fieldValue != null ? fieldValue.length() : 0;
                    LOGGER.error("        : " + fieldable);
                    LOGGER.error("        : " + fieldName + ", " + fieldLength + ", " + fieldValue);
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Exception printing index : " + Arrays.toString(args), e);
        } finally {
            if (indexReader != null) {
                try {
                    indexReader.close();
                } catch (final Exception e) {
                    LOGGER.error("Exception closing index : " + Arrays.toString(args), e);
                }
            }
        }
    }

}