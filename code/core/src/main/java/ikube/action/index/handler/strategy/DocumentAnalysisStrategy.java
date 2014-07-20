package ikube.action.index.handler.strategy;

import com.google.common.collect.Lists;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.IConstants.CLASSIFICATION_CONFLICT;
import static ikube.action.index.IndexManager.addStringField;
import static java.text.BreakIterator.getCharacterInstance;

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
        String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
        if (!StringUtils.isEmpty(StringUtils.stripToEmpty(content))) {
            // Split the document text into sentences
            String language = document.get(IConstants.LANGUAGE);
            if (language == null) {
                language = Locale.ENGLISH.getLanguage();
            }
            List<String> sentences = breakDocumentIntoSentences(content, language);
            String majorityClassification = highestVotedClassification(sentences);
            // Add the highest voted result to the index
            if (!StringUtils.isEmpty(majorityClassification)) {
                String previousClassification = document.get(IConstants.CLASSIFICATION);
                if (previousClassification == null) {
                    addStringField(CLASSIFICATION, majorityClassification, indexable, document);
                } else //noinspection ConstantConditions
                    if (!majorityClassification.equals(previousClassification)) {
                        addStringField(CLASSIFICATION_CONFLICT, majorityClassification, indexable, document);
                    }
            }
            if (System.currentTimeMillis() % 15000 == 0) {
                logger.warn("Classification : " + majorityClassification + ", " + context.getName() + ", " + content);
            }
        }
        return super.aroundProcess(indexContext, indexable, document, resource);
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

    List<String> breakDocumentIntoSentences(final String text, final String language) {
        List<String> sentences = Lists.newArrayList();
        BreakIterator breakIterator = getCharacterInstance(Locale.forLanguageTag(language));
        breakIterator.setText(text);

        int start = 0;
        int offset;
        while ((offset = breakIterator.next()) != BreakIterator.DONE) {
            String sentence = text.substring(start, offset);
            sentences.add(sentence);
            start = offset + 1;
        }
        return sentences;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}