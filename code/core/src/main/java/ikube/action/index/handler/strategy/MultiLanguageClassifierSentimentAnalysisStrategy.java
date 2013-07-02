package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.springframework.beans.factory.annotation.Value;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramProcessLM;

/**
 * @author Michael Couck
 * @since 19.06.13
 * @version 01.00
 */
public final class MultiLanguageClassifierSentimentAnalysisStrategy extends AStrategy {

	@Value("${multi.language.ngram}")
	private int nGram = 8;
	private AtomicInteger atomicInteger;
	private Map<String, DynamicLMClassifier<NGramProcessLM>> languageClassifiers;

	public MultiLanguageClassifierSentimentAnalysisStrategy() {
		this(null);
	}

	public MultiLanguageClassifierSentimentAnalysisStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		// TODO Perhaps detect the subject and the object. Separate the constructs of the sentence for further processing
		String language = document.get(IConstants.LANGUAGE);
		String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
		if (language != null && content != null) {
			// If this data is already classified by another strategy then train the language
			// classifiers on the data. We can then also classify the data and correlate the results
			String sentiment = document.get(IConstants.SENTIMENT);
			DynamicLMClassifier<NGramProcessLM> dynamicLMClassifier = getDynamicLMClassifier(language);
			if (sentiment != null) {
				train(content, sentiment, dynamicLMClassifier);
			}
			Classification classification = dynamicLMClassifier.classify(content);
			String languageSentiment = classification.bestCategory();
			if (StringUtils.isEmpty(sentiment)) {
				// Not analyzed so add the sentiment that we get
				IndexManager.addStringField(IConstants.SENTIMENT, languageSentiment, document, Store.YES, Index.ANALYZED, TermVector.NO);
			} else {
				// Retrain on the previous strategy sentiment
				train(content, sentiment, dynamicLMClassifier);
				if (!sentiment.contains(languageSentiment)) {
					// We don't change the original analysis do we?
					IndexManager.addStringField(IConstants.SENTIMENT_CONFLICT, languageSentiment, document, Store.YES, Index.ANALYZED, TermVector.NO);
				}
			}
		}
		logger.info("Document : " + document + ", " + document.hashCode());
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	private void train(final String content, final String sentiment, DynamicLMClassifier<NGramProcessLM> dynamicLMClassifier) throws Exception {
		Classification classification = new Classification(sentiment);
		Classified<CharSequence> classified = new Classified<CharSequence>(content, classification);
		dynamicLMClassifier.handle(classified);
	}

	private DynamicLMClassifier<NGramProcessLM> getDynamicLMClassifier(final String language) {
		DynamicLMClassifier<NGramProcessLM> dynamicLMClassifier = languageClassifiers.get(language);
		if (dynamicLMClassifier == null) {
			dynamicLMClassifier = DynamicLMClassifier.createNGramProcess(IConstants.SENTIMENT_CATEGORIES, nGram);
			languageClassifiers.put(language, dynamicLMClassifier);
		}
		return dynamicLMClassifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		atomicInteger = new AtomicInteger(0);
		languageClassifiers = new HashMap<String, DynamicLMClassifier<NGramProcessLM>>();
	}

}