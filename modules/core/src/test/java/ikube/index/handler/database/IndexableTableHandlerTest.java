package ikube.index.handler.database;

import java.net.InetAddress;

import ikube.ATest;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

public class IndexableTableHandlerTest extends ATest {

	@Test
	public void handle() throws Exception {
		ApplicationContextManager.getApplicationContext("/handler/spring.xml");
		IndexContext indexContext = ApplicationContextManager.getBean(IndexContext.class);
		IndexManager.openIndexWriter(InetAddress.getLocalHost().getHostAddress(), indexContext, System.currentTimeMillis());
		IndexableTable indexableTable = ApplicationContextManager.getBean("faqTable");
		IndexableTableHandler handler = new IndexableTableHandler(null);
		handler.handle(indexContext, indexableTable);
		IndexManager.closeIndexWriter(indexContext);
	}

}
