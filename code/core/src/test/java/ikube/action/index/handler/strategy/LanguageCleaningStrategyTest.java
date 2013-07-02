package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.model.Indexable;
import ikube.search.spelling.SpellingChecker;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LanguageCleaningStrategyTest extends AbstractTest {

	private LanguageCleaningStrategy languageCleaningStrategy;

	@Before
	public void before() throws Exception {
		new SpellingChecker().initialize();
		languageCleaningStrategy = new LanguageCleaningStrategy();
		languageCleaningStrategy.initialize();
	}

	@Test
	public void aroundProcess() throws Exception {
		Indexable<?> indexable = Mockito.mock(Indexable.class);
		Mockito.when(indexable.getContent()).thenReturn(
				"What a looovely dai where theere are moneyy mistaces aaaahhhhhhh thereererereee yooooouuuuu aaarrrrreeee");
		Document document = new Document();
		Object resource = new Object();
		languageCleaningStrategy.aroundProcess(indexContext, indexable, document, resource);
		Mockito.verify(indexable, Mockito.atLeastOnce()).setContent("what a loovely dai where theere are moneyy mistaces aahh thereerereree yoouu aarree");
	}

}
