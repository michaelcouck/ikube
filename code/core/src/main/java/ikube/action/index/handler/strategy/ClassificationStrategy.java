package ikube.action.index.handler.strategy;

import static ikube.IConstants.CATEGORIES;
import static ikube.IConstants.CLASSIFICATION;
import static ikube.IConstants.CLASSIFICATION_CONFLICT;
import static ikube.IConstants.CLASSIFIERS;
import static ikube.IConstants.IKUBE_DIRECTORY;
import static ikube.action.index.IndexManager.addStringField;
import static ikube.toolkit.FileUtilities.findDirectoryRecursively;
import static ikube.toolkit.FileUtilities.getContent;
import static ikube.toolkit.FileUtilities.getOrCreateDirectory;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.io.Reporters;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.FeatureExtractor;

/**
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
public class ClassificationStrategy extends AStrategy {

	class ClassifierOpener implements Runnable {
		public void run() {
			TokenizerFactory tokenizerFactory = new NGramTokenizerFactory(minGram, maxGram);
			FeatureExtractor<CharSequence> featureExtractor = new TokenFeatureExtractor(tokenizerFactory);
			RegressionPrior regressionPrior = RegressionPrior.gaussian(priorVariance, noninformativeIntercept);
			AnnealingSchedule annealingSchedule = AnnealingSchedule.exponential(initialLearningRate, base);
			try {
				LogisticRegressionClassifier<CharSequence> newLogisticRegressionClassifier = LogisticRegressionClassifier.//
						<CharSequence> train(//
								xValidatingObjectCorpus, //
								featureExtractor, //
								minFeatureCount, //
								addInterceptFeature, //
								regressionPrior, //
								blockSize, //
								logisticRegressionClassifier, //
								annealingSchedule, //
								minImprovement, //
								rollingAvgSize, //
								minEpochs, //
								maxEpochs, //
								classifierHandler, //
								Reporters.stdOut());
				logisticRegressionClassifier = newLogisticRegressionClassifier;
			} catch (Exception e) {
				logger.error("Exception opening classifier : ", e);
			}
		}
	}

	private AtomicInteger positive;
	private AtomicInteger neutral;
	private AtomicInteger negative;

	private Lock lock;

	private int trained;
	/** This is the maximum training iterations/documents/tweets etc. before the classifier will not be trained any more. */
	private int maxTrained = 10000;

	/** This is the minimum token size for the n-gram tokenizer. */
	private int minGram = 2;
	/** This is the maximum token size for the n-gram tokenizer. */
	private int maxGram = 18;
	/** These are the categories for the classifier, the default are the sentiment categories. */
	private String[] categories = CATEGORIES;

	private int blockSize;

	private int numFolds = 10;
	private int minEpochs = 100;
	private int rollingAvgSize = 10;
	private int maxEpochs = 20000;
	private int minFeatureCount = 2;

	private boolean addInterceptFeature = true;
	private boolean noninformativeIntercept = true;

	private double base = 0.999;
	private double priorVariance = 1.0;
	private double initialLearningRate = 0.00025;
	private double minImprovement = 0.000000001;

	private LogisticRegressionClassifier<CharSequence> logisticRegressionClassifier;
	private ObjectHandler<LogisticRegressionClassifier<CharSequence>> classifierHandler;
	private XValidatingObjectCorpus<Classified<CharSequence>> xValidatingObjectCorpus;

	public ClassificationStrategy() {
		this(null);
	}

	public ClassificationStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		lock = new ReentrantLock();
		positive = new AtomicInteger();
		neutral = new AtomicInteger();
		negative = new AtomicInteger();
		// See it there is a classifier serialized to disk to get a hot start from
		logisticRegressionClassifier = getPersistedClassifier();
		xValidatingObjectCorpus = new XValidatingObjectCorpus<Classified<CharSequence>>(numFolds);
		trainClassifier(xValidatingObjectCorpus);
		blockSize = xValidatingObjectCorpus.size();
		reopenClassifier();
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
			// If this data is already classified by another strategy then train the language
			// classifiers on the data. We can then also classify the data and correlate the results
			String previousClassification = document.get(CLASSIFICATION);
			String currentClassification = detectSentiment(content);
			if (StringUtils.isEmpty(previousClassification)) {
				// Not analyzed so add the sentiment that we get
				addStringField(CLASSIFICATION, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
			} else {
				// Retrain on the previous strategy sentiment
				train(previousClassification, content);
				if (!previousClassification.contains(currentClassification)) {
					// We don't change the original analysis do we?
					addStringField(CLASSIFICATION_CONFLICT, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
				}
			}
			if (IConstants.POSITIVE.equals(document.get(CLASSIFICATION))) {
				positive.incrementAndGet();
			} else if (IConstants.NEUTRAL.equals(document.get(CLASSIFICATION))) {
				neutral.incrementAndGet();
			} else {
				negative.incrementAndGet();
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	public String detectSentiment(final String content) {
		ConditionalClassification conditionalClassification = logisticRegressionClassifier.classify(content);
		return conditionalClassification.bestCategory();
	}

	protected void train(final String sentiment, final String content) throws Exception {
		if (trained > maxTrained || StringUtils.isEmpty(content)) {
			// Stop training
			return;
		}
		Classification classification = new Classification(sentiment);
		Classified<CharSequence> classified = new Classified<CharSequence>(content, classification);
		xValidatingObjectCorpus.handle(classified);
		if (trained++ % 10000 == 0) {
			double ratio = (((double) positive.get()) / ((double) negative.get())) * 100d;
			logger.info("Positive : " + positive + ", neutral : " + neutral + ", negative : " + negative + ", ratio : " + ratio);
			reopenClassifier();
			setPersistedClassifier();
		}
	}

	protected void reopenClassifier() {
		final String name = ClassificationStrategy.class.getSimpleName();
		try {
			lock.lock();
			logger.info("Opening classifier : ");
			double duration = Timer.execute(new Timer.Timed() {
				@Override
				public void execute() {
					Future<?> future = ThreadUtilities.submit(name, new ClassifierOpener());
					ThreadUtilities.waitForFuture(future, Integer.MAX_VALUE);
				}
			});
			logger.info("Open classifier duration : " + (duration / 1000));
		} finally {
			ThreadUtilities.destroy(name);
			lock.unlock();
		}
	}

	private void trainClassifier(final XValidatingObjectCorpus<Classified<CharSequence>> corpus) {
		File classifiersDirectory = getClassifiersDirectory();
		for (final String category : categories) {
			Classification classification = new Classification(category);
			// We look for files that have content similar to the category
			File corpusContentDirectory = findDirectoryRecursively(classifiersDirectory, category);
			if (corpusContentDirectory != null) {
				File[] corpusContentFiles = corpusContentDirectory.listFiles();
				if (corpusContentFiles != null && corpusContentFiles.length > 0) {
					for (final File corpusContentFile : corpusContentFiles) {
						String content = getContent(corpusContentFile);
						Classified<CharSequence> classified = new Classified<CharSequence>(content, classification);
						corpus.handle(classified);
					}
				}
			}
			Classified<CharSequence> classified = new Classified<CharSequence>(category, classification);
			corpus.handle(classified);
		}
	}

	@SuppressWarnings("unchecked")
	protected LogisticRegressionClassifier<CharSequence> getPersistedClassifier() {
		File logisticRegressionClassifierFile = getClassifierFile();
		if (logisticRegressionClassifierFile.length() > 100) {
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(logisticRegressionClassifierFile);
				byte[] bytes = IOUtils.toByteArray(inputStream);
				return (LogisticRegressionClassifier<CharSequence>) SerializationUtils.deserialize(bytes);
			} catch (Exception e) {
				logger.error("Exception reading the serialized/trained classifier : ", e);
			} finally {
				if (inputStream != null) {
					IOUtils.closeQuietly(inputStream);
				}
			}
		}
		return null;
	}

	private void setPersistedClassifier() {
		// Persist the classifiers from time to time
		File logisticRegressionClassifierFile = getClassifierFile();
		byte[] bytes = SerializationUtils.serialize(logisticRegressionClassifier);
		logger.info("Bytes : " + bytes.length);
		FileUtilities.setContents(logisticRegressionClassifierFile, bytes);
	}

	protected File getClassifierFile() {
		File classifiersDirectory = getClassifiersDirectory();
		String categoriesString = Arrays.deepToString(categories);
		String classifierName = LogisticRegressionClassifier.class.getSimpleName() + " - " + categoriesString;
		return FileUtilities.getOrCreateFile(new File(classifiersDirectory, classifierName));
	}

	protected File getClassifiersDirectory() {
		File classifiersDirectory = findDirectoryRecursively(new File("."), CLASSIFIERS);
		if (classifiersDirectory == null) {
			classifiersDirectory = new File(IKUBE_DIRECTORY, CLASSIFIERS);
		}
		return getOrCreateDirectory(classifiersDirectory);
	}

	public void setMinGram(int minGram) {
		this.minGram = minGram;
	}

	public void setMaxGram(int maxGram) {
		this.maxGram = maxGram;
	}

	public void setCategories(String[] categories) {
		this.categories = categories;
	}

	public void setNumFolds(int numFolds) {
		this.numFolds = numFolds;
	}

	public void setMinEpochs(int minEpochs) {
		this.minEpochs = minEpochs;
	}

	public void setRollingAvgSize(int rollingAvgSize) {
		this.rollingAvgSize = rollingAvgSize;
	}

	public void setMaxEpochs(int maxEpochs) {
		this.maxEpochs = maxEpochs;
	}

	public void setMinFeatureCount(int minFeatureCount) {
		this.minFeatureCount = minFeatureCount;
	}

	public void setNoninformativeIntercept(boolean noninformativeIntercept) {
		this.noninformativeIntercept = noninformativeIntercept;
	}

	public void setPriorVariance(double priorVariance) {
		this.priorVariance = priorVariance;
	}

	public void setInitialLearningRate(double initialLearningRate) {
		this.initialLearningRate = initialLearningRate;
	}

	public void setMinImprovement(double minImprovement) {
		this.minImprovement = minImprovement;
	}

	public void setMaxTrained(int maxTrained) {
		this.maxTrained = maxTrained;
	}

}