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
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    public DocumentAnalysisStrategy() {
        this(null);
    }

    public DocumentAnalysisStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource)
        throws Exception {
        String content = getContentForProcessing(indexable, resource);
        if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
            String language = document.get(IConstants.LANGUAGE);
            if (language == null) {
                language = Locale.ENGLISH.getLanguage();
            }
            // Split the document text into sentences
            List<String> sentences = breakDocumentIntoSentences(content, language);
            String highestVotedClassification = highestVotedClassification(sentences);
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
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    private String getContentForProcessing(final Indexable indexable, final Object resource) {
        if (indexable.getContent() != null) {
            return indexable.getContent().toString();
        }
        if (resource != null) {
            return resource.toString();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    String highestVotedClassification(final List<String> sentences) {
        String highestVotedClassification = null;
        Map<String, AtomicInteger> classificationCounts = new HashMap<>();

        // Analyze each sentence separately
        for (final String sentence : sentences) {
            Analysis<Object, Object> analysis = new Analysis<>();
            analysis.setInput(sentence);
            analysis.setContext(context.getName());
            analysis = analyticsService.analyze(analysis);
            String classification = analysis.getClazz();

            // Aggregate the results, i.e. the greatest analysis wins
            AtomicInteger classificationCount = classificationCounts.get(classification);
            if (classificationCount == null) {
                classificationCount = new AtomicInteger();
                classificationCounts.put(classification, classificationCount);
            }
            classificationCount.incrementAndGet();
            if (highestVotedClassification == null) {
                highestVotedClassification = classification;
            }
            if (classificationCount.get() > classificationCounts.get(highestVotedClassification).get()) {
                highestVotedClassification = classification;
            }
        }

        return highestVotedClassification;
    }

    /**
     * This method will break the document into sentences. This is a very naieve approach, the
     * {@link java.text.BreakIterator} will just tokenize the string, and look for sentence bounradies
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
            String sentence = text.substring(from, to);
            if (StringUtils.isNotEmpty(sentence)) {
                sentences.add(StringUtils.stripToEmpty(sentence));
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
     */
    @SuppressWarnings("UnusedDeclaration")
    List<String> breakDocumentIntoSentences(final String text) throws IOException {
        File modelFile = FileUtilities.findFileRecursively(new File("."), "en-sent.bin");
        SentenceModel sentenceModel = new SentenceModel(modelFile);
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);
        String[] sentences = sentenceDetector.sentDetect(text);
        return Arrays.asList(sentences);
    }

    public void setContext(Context context) {
        this.context = context;
    }
}