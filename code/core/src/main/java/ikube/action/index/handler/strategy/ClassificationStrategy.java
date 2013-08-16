package ikube.action.index.handler.strategy;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.IConstants.CLASSIFICATION_CONFLICT;
import static ikube.action.index.IndexManager.addStringField;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.FeatureExtractor;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.Timer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

import libsvm.LibSVM;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.tools.weka.WekaClassifier;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLogistic;

/**
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
public class ClassificationStrategy extends AStrategy {

	private int maxTraining = 1000;
	private String language = "en";

	private Dataset dataset;
	private Classifier[] classifiers;
	private FeatureExtractor featureExtractor;

	private boolean trained = false;

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
		classifiers = new Classifier[1];
		featureExtractor = new FeatureExtractor();
		try {
			addClassifiers();
		} catch (IOException e) {
			logger.error(null, e);
		}
	}

	private void addClassifiers() throws IOException {
		String[] text = { "", "" };
		String dictionary = Arrays.deepToString(text);

		double[] positiveVector = featureExtractor.extractFeatures(text[0], dictionary);
		Instance positiveInstance = new SparseInstance(positiveVector, IConstants.POSITIVE);

		double[] negativeVector = featureExtractor.extractFeatures(text[1], dictionary);
		Instance negativeInstance = new SparseInstance(negativeVector, IConstants.NEGATIVE);

		dataset = new DefaultDataset(Arrays.asList(positiveInstance, negativeInstance));

		addClassifiers(dataset);
	}

	private void addClassifiers(final Dataset dataset) {
		LibSVM libSvmClassifier = new LibSVM();
		libSvmClassifier.buildClassifier(dataset);
		classifiers[0] = libSvmClassifier;

		Classifier wekaClassifier = new WekaClassifier(new Logistic());
		wekaClassifier.buildClassifier(dataset);
		wekaClassifier = new WekaClassifier(new SMO());
		// wekaClassifier.buildClassifier(dataset);
		wekaClassifier = new WekaClassifier(new SimpleLogistic());
		// wekaClassifier.buildClassifier(dataset);
		wekaClassifier = new WekaClassifier(new NaiveBayes());
		// wekaClassifier.buildClassifier(dataset);
		// classifiers[1] = wekaLogisticClassifier;
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
				String currentClassification = classify(content);
				if (StringUtils.isEmpty(previousClassification)) {
					// Not analyzed so add the sentiment that we get
					addStringField(CLASSIFICATION, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
				} else {
					// We only train if we have had this tweet classified already
					train(previousClassification, content);
					if (!previousClassification.contains(currentClassification)) {
						// We don't change the original analysis, do we?
						addStringField(CLASSIFICATION_CONFLICT, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
					}
				}
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	public String classify(final String content) {
		double[] features;
		try {
			features = featureExtractor.extractFeatures(content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Instance instance = new SparseInstance(features);
		// Check each of the classifiers, if they don't match the log it
		TreeSet<String> classifications = new TreeSet<String>();
		for (final Classifier classifier : classifiers) {
			classifications.add(classifier.classify(instance).toString());
		}
		if (classifications.size() == 1) {
			return classifications.first();
		}
		boolean first = true;
		StringBuilder stringBuilder = new StringBuilder();
		for (final String classification : classifications) {
			if (!first) {
				stringBuilder.append(" ");
			}
			first = false;
			stringBuilder.append(classification);
		}
		return stringBuilder.toString();
	}

	synchronized void train(final String category, final String content) {
		if (trained) {
			return;
		}
		Iterator<Instance> iterator = dataset.iterator();
		int trainedCategory = 0;
		int nextTrainedCategory = 0;
		while (iterator.hasNext()) {
			Instance instance = iterator.next();
			if (instance.classValue().equals(category)) {
				trainedCategory++;
			} else {
				nextTrainedCategory++;
			}
		}
		if (trainedCategory >= maxTraining) {
			if (nextTrainedCategory >= maxTraining) {
				trained = true;
			}
			return;
		}
		double[] features;
		try {
			features = featureExtractor.extractFeatures(content, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Instance instance = new SparseInstance(features, category);
		dataset.add(instance);
		if (dataset.size() % 100 == 0) {
			logger.info("Building classifier : " + category + ", " + language + ", " + dataset.size());
			long duration = Timer.execute(new Timer.Timed() {
				@Override
				public void execute() {
					Dataset newDataset = null;
					newDataset = dataset.copy();
					addClassifiers(newDataset);
				}
			});
			logger.info("Built classifier in : " + duration);
		}
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setMaxTraining(final int maxTraining) {
		this.maxTraining = maxTraining;
	}

}