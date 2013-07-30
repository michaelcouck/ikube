package ikube.action.index.handler.strategy;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.IConstants.CLASSIFICATION_CONFLICT;
import static ikube.action.index.IndexManager.addStringField;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.FeatureExtractor;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import libsvm.LibSVM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
public class ClassificationStrategy extends AStrategy {

	private int maxTraining = 1000000;

	private LibSVM libSvm;
	private Dataset dataset;
	private FeatureExtractor featureExtractor;

	public ClassificationStrategy() {
		this(null);
	}

	public ClassificationStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@Override
	public void initialize() {
		libSvm = new LibSVM();
		featureExtractor = new FeatureExtractor();
		try {
			String content = "shit what a lousy day";
			double[] featureVector = featureExtractor.extractFeatures(content, content);
			Instance instance = new SparseInstance(featureVector, IConstants.NEGATIVE);
			dataset = new DefaultDataset(Arrays.asList(instance));
			libSvm.buildClassifier(dataset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		// TODO Perhaps detect the subject and the object. Separate the constructs of the sentence for further processing
		String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
		if (content != null) {
			// If this data is already classified by another strategy then maxTraining the language
			// classifiers on the data. We can then also classify the data and correlate the results
			String previousClassification = document.get(CLASSIFICATION);
			String currentClassification = detectSentiment(content);
			if (StringUtils.isEmpty(previousClassification)) {
				// Not analyzed so add the sentiment that we get
				addStringField(CLASSIFICATION, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
			} else {
				if (maxTraining > 0) {
					maxTraining--;
					// Retrain on the previous strategy sentiment
					train(previousClassification, content);
				} else {
					if (maxTraining == 0) {
						maxTraining--;
						// TODO Persist the data sets in the classifier
					}
				}
				if (!previousClassification.contains(currentClassification)) {
					// We don't change the original analysis, do we?
					addStringField(CLASSIFICATION_CONFLICT, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
				}
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	public String detectSentiment(final String content) throws IOException {
		double[] features = featureExtractor.extractFeatures(content);
		Instance instance = new SparseInstance(features);
		return libSvm.classify(instance).toString();
	}

	void train(final String category, final String content) throws IOException {
		Iterator<Instance> iterator = dataset.iterator();
		double[] features = featureExtractor.extractFeatures(content, content);
		Instance instance = new SparseInstance(features, category);
		dataset = new DefaultDataset(Arrays.asList(instance));
		while (iterator.hasNext()) {
			instance = iterator.next();
			dataset.add(instance);
			if (dataset.size() % 1000 == 0) {
				libSvm.buildClassifier(dataset);
			}
		}
	}

}