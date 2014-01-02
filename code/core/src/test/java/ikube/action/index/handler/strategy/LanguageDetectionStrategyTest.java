package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Indexable;
import ikube.toolkit.PerformanceTester;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 05.04.13
 */
public class LanguageDetectionStrategyTest extends AbstractTest {

	private LanguageDetectionStrategy languageDetectionStrategy;

	@Before
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void before() {
		languageDetectionStrategy = new LanguageDetectionStrategy();
		languageDetectionStrategy.initialize();
		List<Indexable<?>> children = new ArrayList(Arrays.asList(indexableColumn));
		when(indexableTable.getChildren()).thenReturn(children);
		when(indexableTable.isStored()).thenReturn(Boolean.TRUE);
		when(indexableTable.isAnalyzed()).thenReturn(Boolean.TRUE);
	}

//	@After
//	public void after() {
//		Mockit.tearDownMocks();
//	}

	@Test
	public void aroundProcess() throws Exception {
		Document document = new Document();
		// English
		when(indexableColumn.getContent()).thenReturn("some english text that can not be confused with swedish for " +
			"God's sake");
		languageDetectionStrategy.aroundProcess(indexContext, indexableTable, document, null);
		String english = Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH);
		String language = document.get(IConstants.LANGUAGE);
		assertEquals("We expect English for this one : ", english, language);

		// Russian, and that is enough I think
		document = new Document();
		when(indexableColumn.getContent()).thenReturn("господи");
		languageDetectionStrategy.aroundProcess(indexContext, indexableTable, document, null);
		language = document.get(IConstants.LANGUAGE);

		String russian = new Locale("ru").getDisplayLanguage(Locale.ENGLISH);
		assertEquals("We expect " + russian + " for this one : ", russian, language);
	}

	@Test
	public void aroundProcessPerformance() {
		when(indexableColumn.getContent()).thenReturn("господи");
		int iterations = 1000;
		final Document document = new Document();
		double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Exception {
				languageDetectionStrategy.aroundProcess(indexContext, indexableColumn, document, null);
			}
		}, "Language detection strategy : ", iterations, Boolean.TRUE);
		assertTrue(perSecond > 1000);
	}

}