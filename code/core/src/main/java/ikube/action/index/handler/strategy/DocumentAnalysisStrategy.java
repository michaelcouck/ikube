package ikube.action.index.handler.strategy;

import com.google.common.collect.Lists;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.StringUtilities;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.IConstants.CLASSIFICATION_CONFLICT;
import static ikube.action.index.IndexManager.addStringField;
import static java.text.BreakIterator.getSentenceInstance;

/**
 * This strategy, like it's predecessor, will analyze text, using the underlying analyzer. The
 * document or text in this case will be broken into sentences and the aggregate of the sentences
 * taken together to result in the sentiment.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20-07-2014
 */
@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "SpringJavaAutowiringInspection"})
public class DocumentAnalysisStrategy extends AStrategy {

    /**
     * The context that contains among other things the actual {@link ikube.analytics.IAnalyzer} object, which is in fact
     * the wrapper interface for the underlying analyzer algorithm. In the default implementation Weka was used as the machine
     * learning library.
     */
    private Context context;

    /**
     * This is the service that will process the {@link ikube.model.Analysis} object, optionally distributing it into the
     * cluster. In the case the property {@link ikube.model.Analysis#isDistributed()} is true, then a random server will be chosen
     * to execute the analytics on this analysis object.
     */
    @Autowired
    private IAnalyticsService analyticsService;

    private SentenceDetectorME sentenceDetector;

    public DocumentAnalysisStrategy() {
        this(null);
    }

    public DocumentAnalysisStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
        File modelFile = FileUtilities.findFileRecursively(new File(IConstants.IKUBE_DIRECTORY), "en-sent.bin");
        try {
            SentenceModel sentenceModel = new SentenceModel(modelFile);
            sentenceDetector = new SentenceDetectorME(sentenceModel);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource) throws Exception {
        if (indexable.getContent() != null) {
            String content = indexable.getContent().toString();
            if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
                // Split the document text into sentences
                List<String> sentences = breakDocumentIntoSentences(content);
                String highestVotedClassification = aggregateClassificationForSentences(sentences);
                // Add the highest voted result to the index
                if (!StringUtils.isEmpty(highestVotedClassification)) {
                    String previousClassification = document.get(IConstants.CLASSIFICATION);
                    if (previousClassification == null) {
                        addStringField(CLASSIFICATION, highestVotedClassification, indexable, document);
                    } else //noinspection ConstantConditions
                        if (!highestVotedClassification.equals(previousClassification)) {
                            addStringField(CLASSIFICATION_CONFLICT, highestVotedClassification, indexable, document);
                        }
                }
                if (System.currentTimeMillis() % 15000 == 0) {
                    logger.warn("Classification : " + highestVotedClassification + ", " + context.getName() + ", " + content);
                }
            }
        }
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    @SuppressWarnings("unchecked")
    String aggregateClassificationForSentences(final List<String> sentences) {
        // Analyze each sentence separately
        String[] classes = new String[sentences.size()];
        double[][] distributionForInstances = new double[sentences.size()][];
        for (int i = 0; i < distributionForInstances.length; i++) {
            String sentence = sentences.get(i);
            sentence = StringUtilities.stripToAlphaNumeric(sentence);
            Analysis<Object, Object> analysis = new Analysis<>();
            analysis.setInput(new String[]{sentence});
            analysis.setContext(context.getName());
            analysis = analyticsService.analyze(analysis);

            classes[i] = analysis.getClazz();
            distributionForInstances[i] = (double[]) analysis.getOutput();

            if (logger.isInfoEnabled()) {
                logger.info("Class : " + classes[i]);
                logger.info("Distribution : " + distributionForInstances[i]);
                logger.info("Sentence : " + sentence);
            }
        }
        if (distributionForInstances[0] == null) {
            return null;
        }
        // Aggregate the results, i.e. the greatest average probability wins
        double[] aggregateDistributionForSentences = new double[distributionForInstances[0].length];
        for (double[] distributionForInstance : distributionForInstances) {
            for (int j = 0; j < distributionForInstance.length; j++) {
                aggregateDistributionForSentences[j] += distributionForInstance[j];
            }
        }
        for (int i = 0; i < aggregateDistributionForSentences.length; i++) {
            aggregateDistributionForSentences[i] = aggregateDistributionForSentences[i] / sentences.size();
        }
        // Find the highest probability in the distribution list
        int index = 0;
        double highestProbability = 0.00;
        for (int i = 0; i < aggregateDistributionForSentences.length; i++) {
            if (aggregateDistributionForSentences[i] > highestProbability) {
                highestProbability = aggregateDistributionForSentences[i];
                index = i;
            }
        }
        String mostProbableClass = null;
        // Find the closest match in the sentences to this average
        double smallestDifference = Long.MAX_VALUE;
        for (int i = 0; i < distributionForInstances.length; i++) {
            double difference = Math.abs(highestProbability - distributionForInstances[i][index]);
            if (difference < smallestDifference) {
                smallestDifference = difference;
                mostProbableClass = classes[i];
            }
        }
        logger.error("Most probable class : " + mostProbableClass + ", sentence size : " + sentences.size());
        return mostProbableClass;
    }

    /**
     * NOTE: This method only works with correct spaces and capitals, i.e. lowercase does not work!
     * <p/>
     * This method will break the document into sentences. This is a very naive approach, the
     * {@link java.text.BreakIterator} will just tokenize the string, and look for sentence boundaries
     * using the punctuation in the text, apparently. But fine for a first implementation.
     *
     * @param text     the input text to break into sentences
     * @param language the language of the text
     * @return the sentences in the text
     */
    List<String> breakDocumentIntoSentences(final String text, final String language) {
        List<String> sentences = Lists.newArrayList();
        BreakIterator breakIterator = getSentenceInstance(Locale.forLanguageTag(language));
        breakIterator.setText(text);

        int from = 0;
        int to;

        to = breakIterator.first();
        while (to != BreakIterator.DONE) {
            String sentence = StringUtils.stripToEmpty(text.substring(from, to));
            if (StringUtils.isNotEmpty(sentence)) {
                sentences.add(sentence);
            }
            from = to;
            to = breakIterator.next();
        }
        return sentences;
    }

    /**
     * This method uses OpenNlp as the sentence boundary detector. Seemingly, this is also
     * just a tokenizer, looking for punctuation that indicates the end of sentences. Also not
     * very cleaver, what a disappointment.
     * <p/>
     * But at least it works.
     */
    @SuppressWarnings("UnusedDeclaration")
    List<String> breakDocumentIntoSentences(final String text) throws IOException {
        String[] sentences = sentenceDetector.sentDetect(text);
        return Arrays.asList(sentences);
    }

    public void setContext(Context context) {
        this.context = context;
    }
}