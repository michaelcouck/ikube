package ikube.toolkit;

import ikube.BaseTest;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.Test;

public class AdHocTest extends BaseTest {

	@Test
	@Ignore
	public void print() throws Exception {
		IndexReader indexReader = IndexReader.open(FSDirectory.open(new File("/home/indexes/geospatial/1384869103623/192.168.1.9-8010")));
		printIndex(indexReader, 10);
		indexReader.close();
	}

}
