package ikube.data;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexPrinter {

	private static final Logger LOGGER = Logger.getLogger(IndexPrinter.class);

	public static void main(String[] args) {
		IndexReader indexReader = null;
		try {
			args = new String[] { "/tmp/index/ikube/1362304143294/192.168.122.1-8002" };
			File file = new File(args[0]);
			Directory directory = FSDirectory.open(file);
			indexReader = IndexReader.open(directory);
			LOGGER.info("Num docs : " + indexReader.numDocs());
			for (int i = 0; i < indexReader.numDocs(); i++) {
				Document document = indexReader.document(i);
				LOGGER.info("Document : " + i);
				List<Fieldable> fields = document.getFields();
				for (Fieldable fieldable : fields) {
					String fieldName = fieldable.name();
					String fieldValue = fieldable.stringValue();
					int fieldLength = fieldValue != null ? fieldValue.length() : 0;
					LOGGER.info("        : " + fieldName + ", " + fieldLength + ", " + fieldValue);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			if (indexReader != null) {
				try {
					indexReader.close();
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}
		}
	}

}