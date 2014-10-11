package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.search.spelling.SpellingChecker;
import ikube.toolkit.StringUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * This strategy will correct every word in the input content, and set the content back in the
 * indexable. Note that this correction mechanism is dependant on the languages and the lists of
 * words that are made available to the autocomplete index, as it is this index that will be searched
 * for the spelling check, and indeed for the suggestions for the best or closest match to the target
 * word.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-09-2014
 */
public class SpellingCorrectionStrategy extends AStrategy {

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private SpellingChecker spellingChecker;
    @Value("${max-spelling-distance-allowed}")
    private double maxSpellingDistanceAllowed = 1;

    public SpellingCorrectionStrategy() {
        this(null);
    }

    public SpellingCorrectionStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource)
            throws Exception {
        String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
        String cleanedContent = cleanContent(content);
        indexable.setContent(cleanedContent);
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    String cleanContent(final String content) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] words = StringUtils.splitPreserveAllTokens(content, ".,;: \n\r\t(){}[]\"@&|#!/*+_$");
        for (final String word : words) {
            String cleanedWord = StringUtilities.stripToAlphaNumeric(word);
            if (StringUtils.isEmpty(cleanedWord) || !StringUtils.isAlpha(word)) {
                continue;
            }
            String correctedWord = spellingChecker.checkWord(cleanedWord);
            if (StringUtils.isEmpty(correctedWord)) {
                // No match at all, i.e. not even close to any word, something like 'zzzzzeeeee'?
                continue;
            }
            if (correctedWord.equals(cleanedWord)) {
                stringBuilder.append(correctedWord);
                stringBuilder.append(IConstants.SPACE);
            } else {
                double cleanedLength = cleanedWord.length();
                double correctedLength = correctedWord.length();
                double distance = StringUtils.getLevenshteinDistance(cleanedWord, correctedWord);
                // Normalize the maximum distance as a percentage of the average length of the two
                // words, and the distance between them as a value of the number of changes required
                // to convert one to the other
                double maxDistance = (distance / (cleanedLength + correctedLength / 2));
                if (maxDistance <= maxSpellingDistanceAllowed) {
                    stringBuilder.append(correctedWord);
                    stringBuilder.append(IConstants.SPACE);
                }
            }
        }
        return stringBuilder.toString().trim();
    }

}