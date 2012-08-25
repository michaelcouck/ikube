package ikube.miscellaneous;

import ikube.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;

import org.apache.lucene.index.IndexWriter;

public class MergeTool {

	public static void main(String[] args) throws Exception {
		IndexContext<Object> indexContext = new IndexContext<Object>();
		indexContext.setCompoundFile(true);
		indexContext.setBufferedDocs(10000);
		indexContext.setMaxFieldLength(10000);
		indexContext.setMergeFactor(10000);
		indexContext.setBufferSize(1024);
		File indexDirectory = new File("/media/nas/xfs-two/history/index/wikiHistoryTwo/1345583761125/192.168.1.4.8000.bck");
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, false);
		IndexManager.closeIndexWriter(indexWriter);
	}

}
