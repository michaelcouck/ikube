package ikube.toolkit;

import ikube.Base;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class AdHocTest extends Base {

	@Test
	public void print() throws Exception {
		IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(
				"/media/nas/xfs-one/indexes/desktop/1359048705139/192.168.122.1")));
		printIndex(indexReader);
	}

}
