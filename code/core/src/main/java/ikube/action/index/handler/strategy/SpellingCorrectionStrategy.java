package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.search.spelling.SpellingChecker;
import ikube.toolkit.STRING;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.StringTokenizer;

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
        StringTokenizer stringTokenizer = new StringTokenizer(content, ".,;: \n\r\t(){}[]\"@&|#!/*+_$", true);
        while (stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            String strippedWord = STRING.stripToAlphaNumeric(word);
            if (StringUtils.isEmpty(strippedWord)) {
                stringBuilder.append(word);
                continue;
            }
            String correctedWord = spellingChecker.checkWord(strippedWord);
            if (StringUtils.isEmpty(correctedWord)) {
                // No match at all, i.e. not even close to any word, something like 'zzzzzeeeee'?
                stringBuilder.append(word);
                continue;
            }
            if (correctedWord.equals(strippedWord)) {
                stringBuilder.append(correctedWord);
            } else {
                double cleanedLength = strippedWord.length();
                double correctedLength = correctedWord.length();
                double distance = StringUtils.getLevenshteinDistance(strippedWord, correctedWord);
                // Normalize the maximum distance as a percentage of the average length of the two
                // words, and the distance between them as a value of the number of changes required
                // to convert one to the other
                double maxDistance = (distance / (cleanedLength + correctedLength / 2));
                if (maxDistance <= maxSpellingDistanceAllowed) {
                    stringBuilder.append(correctedWord);
                } else {
                    // If we can't find a correction then just put the word back
                    stringBuilder.append(word);
                }
            }
        }
        return stringBuilder.toString().trim();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setMaxSpellingDistanceAllowed(final double maxSpellingDistanceAllowed) {
        this.maxSpellingDistanceAllowed = maxSpellingDistanceAllowed;
    }

}