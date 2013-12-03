package ikube.action.index.handler.strategy;

import static ikube.IConstants.CLASSIFICATION;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
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
	/** In the event this should only train on a specific language. */
	private String language;
	/** The wrapper for the 'real' classifier, probably Weka */
	private IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> classifier;

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
		if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
			boolean train = Boolean.TRUE;
			if (!StringUtils.isEmpty(this.language)) {
				// Configuration specified a language
				String language = document.get(IConstants.LANGUAGE);
				if (StringUtils.isEmpty(language) || !this.language.equals(language)) {
					// Couldn't find the language or not the correct one so don't train
					train = Boolean.FALSE;
				}
			}
			if (train) {
				String classification = document.get(CLASSIFICATION);
				if (!StringUtils.isEmpty(classification)) {
					train(classification, content);
				}
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	@SuppressWarnings("unchecked")
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
			Analysis<String, double[]> analysis = new Analysis<>();
			analysis.setInput(content);
			classifier.train(analysis);
		} catch (Exception e) {
			logger.error(null, e);
		}
	}

	public void setPositive(final int positive) {
		this.positive = positive;
	}

	public void setNegative(final int negative) {
		this.negative = negative;
	}

	public void setLanguage(final String language) {
		this.language = language;
	}

	public void setClassifier(final IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> classifier) {
		this.classifier = classifier;
	}

}