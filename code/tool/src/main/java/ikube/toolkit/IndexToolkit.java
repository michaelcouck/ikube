package ikube.toolkit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IndexToolkit {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexToolkit.class);

	public static void main(String[] args) {
		File indexDirectory = new File(args[0]);
		int numDocs = Integer.parseInt(args[1]);

		FSDirectory directory = null;
		IndexReader indexReader = null;
		try {
			// directory = FSDirectory.open(indexDirectory);
			directory = NIOFSDirectory.open(indexDirectory);
			indexReader = IndexReader.open(directory);
			printIndex(indexReader, numDocs);
		} catch (Exception e) {
			LOGGER.error("Exception printing index : " + Arrays.deepToString(args), e);
		} finally {
			if (directory != null) {
				directory.close();
			}
			if (indexReader != null) {
				try {
					indexReader.close();
				} catch (IOException e) {
					LOGGER.error("Exception closing the index reader : ", e);
				}
			}
		}
	}

	public static void printIndex(final IndexReader indexReader, final int numDocs) throws Exception {
		LOGGER.info("Num docs : " + indexReader.numDocs());
		for (int i = 0; i < numDocs && i < indexReader.numDocs(); i++) {
			Document document = indexReader.document(i);
			LOGGER.info("Document : " + i);
			printDocument(document);
		}
	}

	public static void printDocument(final Document document) {
		List<IndexableField> fields = document.getFields();
		for (IndexableField fieldable : fields) {
			String fieldName = fieldable.name();
			String fieldValue = fieldable.stringValue();
			int fieldLength = fieldValue != null ? fieldValue.length() : 0;
			LOGGER.info("        : " + fieldName + ", " + fieldLength + ", " + fieldValue);
		}
	}

}
