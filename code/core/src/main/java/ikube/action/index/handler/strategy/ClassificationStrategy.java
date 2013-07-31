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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

/**
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
public class ClassificationStrategy extends AStrategy {

	private int maxTraining = 10000;

	private Lock lock;
	private Dataset dataset;
	private Classifier[] classifiers;
	private FeatureExtractor featureExtractor;
	private Map<String, AtomicInteger> trainedCategories;

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
		lock = new ReentrantLock();

		classifiers = new Classifier[2];
		featureExtractor = new FeatureExtractor();
		trainedCategories = new HashMap<String, AtomicInteger>();

		try {
			addClassifiers();
		} catch (IOException e) {
			logger.error(null, e);
		}
	}

	private void addClassifiers() throws IOException {
		String[] text = { "The news is broardcast every day", "What a beautiful child", "Life sucks, and then you die" };
		String dictionary = Arrays.deepToString(text);

		double[] positiveVector = featureExtractor.extractFeatures(text[1], dictionary);
		Instance positiveInstance = new SparseInstance(positiveVector, IConstants.POSITIVE);

		double[] negativeVector = featureExtractor.extractFeatures(text[2], dictionary);
		Instance negativeInstance = new SparseInstance(negativeVector, IConstants.NEGATIVE);

		dataset = new DefaultDataset(Arrays.asList(positiveInstance, negativeInstance));

		addClassifiers(dataset);
	}

	private void addClassifiers(final Dataset dataset) {
		LibSVM libSvmClassifier = new LibSVM();
		libSvmClassifier.buildClassifier(dataset);
		classifiers[0] = libSvmClassifier;

		// Classifier wekaLogisticClassifier = new WekaClassifier(new Logistic());
		// Classifier wekaLogisticClassifier = new WekaClassifier(new SMO());
		// Classifier wekaLogisticClassifier = new WekaClassifier(new SimpleLogistic());
		Classifier wekaLogisticClassifier = new WekaClassifier(new NaiveBayes());
		wekaLogisticClassifier.buildClassifier(dataset);
		classifiers[1] = wekaLogisticClassifier;
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

	void train(final String category, final String content) {
		if (maxTraining == 0) {
			maxTraining--;
			// TODO Persist the data sets in the classifier
		} else if (maxTraining > 0) {
			if (!canTrain(category)) {
				return;
			}
			maxTraining--;
			// Check that the training for the categories are equal
			double[] features;
			try {
				features = featureExtractor.extractFeatures(content, content);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			try {
				if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
					Instance instance = new SparseInstance(features, category);
					dataset.add(instance);
				}
			} catch (InterruptedException e) {
				logger.error(null, e);
			} finally {
				lock.unlock();
			}
			if (dataset.size() % 1000 == 0) {
				logger.info("Building classifier : " + dataset.size());
				long duration = Timer.execute(new Timer.Timed() {
					@Override
					public void execute() {
						Dataset newDataset = null;
						try {
							if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
								newDataset = dataset.copy();
							}
						} catch (InterruptedException e) {
							logger.error(null, e);
						} finally {
							lock.unlock();
						}
						if (newDataset != null) {
							addClassifiers(newDataset);
						}
					}
				});
				logger.info("Built classifier in : " + duration);
			}
		}
	}

	private boolean canTrain(final String category) {
		AtomicInteger atomicInteger = trainedCategories.get(category);
		if (atomicInteger == null) {
			atomicInteger = new AtomicInteger(0);
			trainedCategories.put(category, atomicInteger);
		}
		for (Map.Entry<String, AtomicInteger> mapEntry : trainedCategories.entrySet()) {
			if (category.equals(mapEntry.getKey())) {
				continue;
			}
			if (atomicInteger.get() - mapEntry.getValue().get() > 100) {
				return Boolean.FALSE;
			}
		}
		atomicInteger.incrementAndGet();
		return Boolean.TRUE;
	}

	public void setMaxTraining(int maxTraining) {
		this.maxTraining = maxTraining;
	}

}