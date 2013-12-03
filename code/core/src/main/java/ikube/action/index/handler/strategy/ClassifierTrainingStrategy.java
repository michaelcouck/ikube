package ikube.action.index.handler.strategy;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.action.index.IndexManager.addStringField;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyzer;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

/**
 * @author Michael Couck
 * @since 02.12.13
 * @version 01.00
 */
public class ClassifierTrainingStrategy extends AStrategy {

	/** The maximum number of positive training folds */
	private int positive;
	/** The maximum number of negative training folds */
	private int negative;
	/** The wrapper for the 'real' classifier, probably Weka */
	private IAnalyzer<String, String> classifier;

	public ClassifierTrainingStrategy() {
		this(null);
	}

	public ClassifierTrainingStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
		// TODO Perhaps detect the subject and the object. Separate the constructs of the sentence for further processing
		// If this data is already classified by another strategy then maxTraining the language
		// classifiers on the data. We can then also classify the data and correlate the results
		if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
			String previousClassification = document.get(CLASSIFICATION);
			String currentClassification = classifier.analyze(content);
			if (!StringUtils.isEmpty(currentClassification)) {
				// We'll assume that the previous strategy 'knows' that this is
				// correctly classified, perhaps from the emoticon Twitter data
				if (!StringUtils.isEmpty(previousClassification)) {
					train(previousClassification, content);
				}
				// We concatenate the current classification to the existing one if it already exists,
				// in this way we can differentiate between the classifiers in the stack, perhaps first
				// a Bays then a SVM then regression, etc. The result could be something like: positive
				// negative neutral, for the field, but of course if they are well trained then they should all
				// agree. This also gives an indication of how the classifiers are doing against each other
				addStringField(CLASSIFICATION, currentClassification, indexable, document);
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

	public void setPositive(int positive) {
		this.positive = positive;
	}

	public void setNegative(int negative) {
		this.negative = negative;
	}

	public void setClassifier(IAnalyzer<String, String> classifier) {
		this.classifier = classifier;
	}

}