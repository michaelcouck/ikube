package ikube.action.index.handler.strategy;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.IConstants.CLASSIFICATION_CONFLICT;
import static ikube.action.index.IndexManager.addStringField;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IClassifier;
import ikube.analytics.WekaClassifier;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * TODO This strategy ...
 * 
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
public class DynamicallyTrainedLanguageSpecificClassificationStrategy extends AStrategy {

	private int maxTraining = 10000;
	private String language = "en";

	private IClassifier<String, String, String, Boolean> classifier;

	public DynamicallyTrainedLanguageSpecificClassificationStrategy() {
		this(null);
	}

	public DynamicallyTrainedLanguageSpecificClassificationStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@Override
	public void initialize() {
		classifier = new WekaClassifier();
		try {
			classifier.initialize();
			classifier.train(IConstants.POSITIVE, IConstants.POSITIVE);
			classifier.train(IConstants.NEGATIVE, IConstants.NEGATIVE);
			((WekaClassifier) classifier).build();
		} catch (final Exception e) {
			logger.error(null, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		String language = document.get(IConstants.LANGUAGE);
		if (language != null && language.equals(this.language)) {
			// TODO Perhaps detect the subject and the object. Separate the constructs of the sentence for further processing
			String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
			if (content != null) {
				// If this data is already classified by another strategy then maxTraining the language
				// classifiers on the data. We can then also classify the data and correlate the results
				String previousClassification = document.get(CLASSIFICATION);
				String currentClassification = classifier.classify(content);
				if (StringUtils.isEmpty(previousClassification)) {
					// Not analyzed so add the sentiment that we get
					addStringField(CLASSIFICATION, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
				} else {
					// We only train if we have had this tweet classified already
					if (previousClassification.equals(IConstants.POSITIVE) || previousClassification.equals(IConstants.NEGATIVE)) {
						train(previousClassification, content);
					}
					if (!previousClassification.contains(currentClassification)) {
						// We don't change the original analysis, do we?
						addStringField(CLASSIFICATION_CONFLICT, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
					}
				}
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	void train(final String clazz, final String content) {
		if (maxTraining > 0) {
			maxTraining--;
			try {
				classifier.train(clazz, content);
			} catch (Exception e) {
				logger.error(null, e);
			}
		}
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}

	public void setMaxTraining(final int maxTraining) {
		this.maxTraining = maxTraining;
	}

}