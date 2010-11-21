package ikube.index.visitor.internet;

import ikube.BaseTest;
import ikube.model.IndexableInternet;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetVisitorTest extends BaseTest {

	@Test
	@SuppressWarnings("unchecked")
	public void visit() {
		indexContext.setIndexWriter(indexWriter);
		IndexableInternet indexableInternet = ApplicationContextManager.getBean(IndexableInternet.class);
		IndexableInternetVisitor<IndexableInternet> indexableInternetVisitor = ApplicationContextManager
				.getBean(IndexableInternetVisitor.class);
		indexableInternetVisitor.visit(indexableInternet);

		// TODO - still have to implement the class
	}

}
