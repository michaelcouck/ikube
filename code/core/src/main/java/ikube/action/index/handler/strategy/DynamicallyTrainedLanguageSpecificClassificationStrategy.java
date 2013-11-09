package ikube.action.index.handler.strategy;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.IConstants.CLASSIFICATION_CONFLICT;
import static ikube.action.index.IndexManager.addStringField;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyzer;
import ikube.analytics.WekaClassifier;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

/**
 * This strategy will train a classifier, typically for sentiment, i.e. positive and negative. It will expect some data that already has been classified, like
 * from the emoticon strategy, and use this data to train the classifier. It will also classify the data and add it's own classification result, based on the
 * classifier that it has trained.
 * 
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
public class DynamicallyTrainedLanguageSpecificClassificationStrategy extends AStrategy {

	private int maxTraining = 10000;
	private int positive;
	private int negative;
	private String language = "en";

	private IAnalyzer<String, String, String, Boolean> classifier;

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
		try {
			classifier.train(IConstants.POSITIVE, IConstants.POSITIVE);
			classifier.train(IConstants.NEGATIVE, IConstants.NEGATIVE);
			((WekaClassifier) classifier).build();
			positive = maxTraining / 2;
			negative = maxTraining / 2;
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
				String currentClassification = null;
				if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
					currentClassification = classifier.analyze(content);
					if (StringUtils.isEmpty(previousClassification)) {
						if (!StringUtils.isEmpty(currentClassification)) {
							// Not analyzed so add the sentiment that we get
							addStringField(CLASSIFICATION, currentClassification, indexable, document);
						}
					} else {
						// We only train if we have had this tweet classified already
						if (previousClassification.equals(IConstants.POSITIVE) || previousClassification.equals(IConstants.NEGATIVE)) {
							train(previousClassification, content);
						}
						if (!StringUtils.isEmpty(currentClassification) && !previousClassification.contains(currentClassification)) {
							// We don't change the original analysis, do we?
							addStringField(CLASSIFICATION_CONFLICT, currentClassification, indexable, document);
						}
					}
				}
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	void train(final String clazz, final String content) {
		if (IConstants.POSITIVE.equals(clazz)) {
			if (positive < 0) {
				return;
			}
			positive--;
		} else if (IConstants.NEGATIVE.equals(clazz)) {
			if (negative < 0) {
				return;
			}
			negative--;
		} else {
			logger.info("Can't train with class : " + clazz);
			return;
		}
		try {
			classifier.train(clazz, content);
		} catch (Exception e) {
			logger.error(null, e);
		}
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setMaxTraining(final int maxTraining) {
		this.maxTraining = maxTraining;
	}

	public void setClassifier(final IAnalyzer<String, String, String, Boolean> classifier) {
		this.classifier = classifier;
	}

}