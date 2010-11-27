package ikube.index.handler.database;

import ikube.ATest;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.net.InetAddress;

import org.junit.Test;

public class IndexableTableHandlerTest extends ATest {

	@Test
	public void handle() throws Exception {
		FileUtilities.deleteFiles(new File("."), new String[] { ".transaction", ".odb" });

		String host = InetAddress.getLocalHost().getHostAddress();
		long time = System.currentTimeMillis();
		ApplicationContextManager.getApplicationContext("/handler/spring.xml");
		IndexContext indexContext = ApplicationContextManager.getBean(IndexContext.class);

		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);

		IndexManager.openIndexWriter(host, indexContext, time);
		IndexableTable indexableTable = ApplicationContextManager.getBean("faqTable");
		IndexableTableHandler handler = ApplicationContextManager.getBean(IndexableTableHandler.class);
		handler.handle(indexContext, indexableTable);
		IndexManager.closeIndexWriter(indexContext);
	}

	public static void main(String[] args) throws Exception {
		IndexableTableHandlerTest indexableTableHandlerTest = new IndexableTableHandlerTest();
		indexableTableHandlerTest.handle();
	}

}
