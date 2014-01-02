package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.analytics.IAnalyzer;
import ikube.model.IndexableTweets;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.PerformanceTester;

import java.util.Locale;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.social.twitter.api.Tweet;

/**
 * @author Michael Couck
 * @since 05.12.13
 * @version 01.00
 */
public class ClassifierTrainingStrategyTest extends AbstractTest {

	@SuppressWarnings("rawtypes")
	private IAnalyzer analyzer;
	/** Class under test. */
	private ClassifierTrainingStrategy classifierTrainingStrategy;

	@Before
	@SuppressWarnings("unchecked")
	public void before() throws Exception {
		// This is for an ad hoc test with a real analyzer, but we'll mock it rather
		// ApplicationContextManager.closeApplicationContext();
		// ApplicationContextManager.getApplicationContextFilesystem("src/test/resources/spring/spring-analytics.xml");
		// IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> analyzer = ApplicationContextManager.getBean("analyzer-smo");

		analyzer = Mockito.mock(IAnalyzer.class);

		classifierTrainingStrategy = new ClassifierTrainingStrategy();
		classifierTrainingStrategy.setClassifier(analyzer);
		classifierTrainingStrategy.setLanguage(Locale.ENGLISH.getLanguage());
		classifierTrainingStrategy.setNegative(1000);
		classifierTrainingStrategy.setPositive(1000);
	}

	@After
	public void after() {
		// To be uncommented with the 'real' analyzer test
		// ApplicationContextManager.closeApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void aroundProcess() throws Exception {
		Document document = new Document();
		final IndexableTweets indexableTweets = Mockito.mock(IndexableTweets.class);
		Mockito.when(indexableTweets.isStored()).thenReturn(Boolean.TRUE);
		Mockito.when(indexableTweets.isAnalyzed()).thenReturn(Boolean.TRUE);

		IndexManager.addStringField(IConstants.LANGUAGE, Locale.ENGLISH.getLanguage(), indexableTweets, document);
		IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.POSITIVE, indexableTweets, document);
		final Tweet tweet = ObjectToolkit.populateFields(new Tweet(0, null, null, null, null, null, 0, null, null), Boolean.TRUE, 10);
		classifierTrainingStrategy.aroundProcess(indexContext, indexableTweets, document, tweet);
		Mockito.verify(analyzer).train(Mockito.any(Object[].class));

		int iterations = ClassifierTrainingStrategy.REBUILD_COUNT + 1;
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() {
				try {
					classifierTrainingStrategy.aroundProcess(indexContext, indexableTweets, new Document(), tweet);
				} catch (Exception e) {
					logger.error(null, e);
				}
			}
		}, "Classifier training performance : ", iterations, Boolean.TRUE);
	}

}