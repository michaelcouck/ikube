package ikube.action.index.handler.strategy;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.action.index.IndexManager.addStringField;
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
public class AnalysisStrategy extends AStrategy {

	/** In the event this should analyzer is language specific. */
	private String language;
	/** The wrapper for the 'real' analyzer, probably Weka */
	private IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> classifier;

	public AnalysisStrategy() {
		this(null);
	}

	public AnalysisStrategy(final IStrategy nextStrategy) {
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
		boolean process = Boolean.TRUE;
		if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
			if (!StringUtils.isEmpty(this.language)) {
				// Configuration specified a language
				String language = document.get(IConstants.LANGUAGE);
				if (StringUtils.isEmpty(language) || !this.language.equals(language)) {
					// Couldn't find the language or not the correct one so don't train
					process = Boolean.FALSE;
				}
			}
			if (process) {
				Analysis<String, double[]> analysis = new Analysis<>();
				analysis.setInput(content);
				Object currentClassification = classifier.analyze(analysis).getClazz();
				if (currentClassification != null && !StringUtils.isEmpty(currentClassification.toString())) {
					// We concatenate the current classification to the existing one if it already exists,
					// in this way we can differentiate between the classifiers in the stack, perhaps first
					// a Bays then a SVM then regression, etc. The result could be something like: positive
					// negative neutral, for the field, but of course if they are well trained then they should all
					// agree. This also gives an indication of how the classifiers are doing against each other
					addStringField(CLASSIFICATION, currentClassification.toString(), indexable, document);
				}
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	public void setLanguage(final String language) {
		this.language = language;
	}

	public void setClassifier(final IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> classifier) {
		this.classifier = classifier;
	}

}