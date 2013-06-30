package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;

import com.aliasi.classify.BaseClassifierEvaluator;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassifier;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;

/**
 * @author Michael Couck
 * @since 19.06.13
 * @version 01.00
 */
public final class SentimentAnalysisStrategy extends AStrategy {

	static int MIN_SENTS = 5;
	static int MAX_SENTS = 25;

	File mPolarityDir;
	String[] mCategories;
	DynamicLMClassifier<NGramProcessLM> mClassifier;
	JointClassifier<CharSequence> mSubjectivityClassifier;

	public SentimentAnalysisStrategy() {
		this(null);
	}

	public SentimentAnalysisStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		// First detect the language, should already be done in the language detection strategy
		// Detect the subject and the object. Separate the constructs of the sentence for further processing
		// Detect the sentiment based on multiple variables, including possibly the emoticons, weighted of course
		// Feed the data back into the classifiers to re-train them
		// Add all the detected data to the index
		Object content = indexable.getContent();
		if (content != null) {
			evaluate();
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	void evaluate() throws IOException {
		boolean storeInstances = false;
		BaseClassifierEvaluator<CharSequence> evaluator = new BaseClassifierEvaluator<CharSequence>(null, mCategories, storeInstances);
		for (int i = 0; i < mCategories.length; ++i) {
			String category = mCategories[i];
			File file = new File(mPolarityDir, mCategories[i]);
			File[] trainFiles = file.listFiles();
			for (int j = 0; j < trainFiles.length; ++j) {
				File trainFile = trainFiles[j];
				if (!isTrainingFile(trainFile)) {
					String review = Files.readFromFile(trainFile, "ISO-8859-1");
					String subjReview = subjectiveSentences(review);
					Classification classification = mClassifier.classify(subjReview);
					evaluator.addClassification(category, classification, null);
				}
			}
		}
		System.out.println();
		System.out.println(evaluator.toString());
	}

	String subjectiveSentences(String review) {
		String[] sentences = review.split("\n");
		BoundedPriorityQueue<ScoredObject<String>> pQueue = new BoundedPriorityQueue<ScoredObject<String>>(ScoredObject.comparator(), MAX_SENTS);
		for (int i = 0; i < sentences.length; ++i) {
			String sentence = sentences[i];
			ConditionalClassification subjClassification = (ConditionalClassification) mSubjectivityClassifier.classify(sentences[i]);
			double subjProb;
			if (subjClassification.category(0).equals("quote"))
				subjProb = subjClassification.conditionalProbability(0);
			else
				subjProb = subjClassification.conditionalProbability(1);
			pQueue.offer(new ScoredObject<String>(sentence, subjProb));
		}
		StringBuilder reviewBuf = new StringBuilder();
		Iterator<ScoredObject<String>> it = pQueue.iterator();
		for (int i = 0; it.hasNext(); ++i) {
			ScoredObject<String> so = it.next();
			if (so.score() < .5 && i >= MIN_SENTS)
				break;
			reviewBuf.append(so.getObject() + "\n");
		}
		String result = reviewBuf.toString().trim();
		return result;
	}

	void train() throws Exception {
		int numTrainingCases = 0;
		int numTrainingChars = 0;

		System.out.println("\nTraining.");
		for (int i = 0; i < mCategories.length; ++i) {
			String category = mCategories[i];
			Classification classification = new Classification(category);
			File file = new File(mPolarityDir, mCategories[i]);
			if (!file.isFile()) {
				continue;
			}
			String data = Files.readFromFile(file, "ISO-8859-1");
			String[] sentences = data.split("\n");
			System.out.println("# Sentences " + category + "=" + sentences.length);
			int numTraining = (sentences.length * 9) / 10;
			for (int j = 0; j < numTraining; ++j) {
				String sentence = sentences[j];
				numTrainingChars += sentence.length();
				Classified<CharSequence> classified = new Classified<CharSequence>(sentence, classification);
				mClassifier.handle(classified);
			}
		}

		System.out.println("\nCompiling.\n  Model file=subjectivity.model");
		OutputStream fileOut = new FileOutputStream("subjectivity.model");
		ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
		mClassifier.compileTo(objOut);
		objOut.close();

		System.out.println("  # Training Cases=" + 9000);
		System.out.println("  # Training Chars=" + numTrainingChars);

		mCategories = new String[] { "neg", "pos" };
		System.out.println("\nTraining.");
		for (int i = 0; i < mCategories.length; ++i) {
			String category = mCategories[i];
			Classification classification = new Classification(category);
			File file = new File(mPolarityDir, mCategories[i]);
			File[] trainFiles = file.listFiles();
			for (int j = 0; j < trainFiles.length; ++j) {
				File trainFile = trainFiles[j];
				if (isTrainingFile(trainFile)) {
					++numTrainingCases;
					String review = Files.readFromFile(trainFile, "ISO-8859-1");
					numTrainingChars += review.length();
					Classified<CharSequence> classified = new Classified<CharSequence>(review, classification);
					mClassifier.handle(classified);
				}
			}
		}
		System.out.println("  # Training Cases=" + numTrainingCases);
		System.out.println("  # Training Chars=" + numTrainingChars);
		// if you want to write the polarity model out for future use, uncomment the following line
		// com.aliasi.util.AbstractExternalizable.compileTo(mClassifier,new File("polarity.model"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void initialize() {
		int nGram = 8;
		mPolarityDir = FileUtilities.findDirectoryRecursively(new File("."), "txt_sentoken");

		mCategories = mPolarityDir.list();
		mClassifier = DynamicLMClassifier.createNGramProcess(mCategories, nGram);

		try {
			train();
		} catch (Exception e) {
			e.printStackTrace();
		}

		File modelFile = new File("subjectivity.model");

		InputStream fileIn = null;
		ObjectInputStream objIn = null;
		try {
			fileIn = new FileInputStream(modelFile);
			objIn = new ObjectInputStream(fileIn);
			mSubjectivityClassifier = (JointClassifier<CharSequence>) objIn.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (fileIn != null) {
				IOUtils.closeQuietly(fileIn);
			}
			if (objIn != null) {
				IOUtils.closeQuietly(objIn);
			}
		}
	}

	boolean isTrainingFile(File file) {
		return file.getName().charAt(2) != '9'; // test on fold 9
	}

}