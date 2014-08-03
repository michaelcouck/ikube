package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 19-06-2013
 */
public final class EmoticonClassificationStrategy extends AStrategy {

    private Set<Long> emoticonHashesPos;
    private Set<Long> emoticonHashesNeg;

    public EmoticonClassificationStrategy() {
        this(null);
    }

    public EmoticonClassificationStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean preProcess(
        final IndexContext indexContext,
        final Indexable indexable,
        final Document document,
        final Object resource)
            throws Exception {
        // Remove duplicate or re-tweets?
        // Remove the @username strings?
        // Remove data with bi-polar sentiment, i.e. + and - emoticons
        String sentiment = document.get(IConstants.CLASSIFICATION);
        if (StringUtils.isEmpty(sentiment)) {
            // We only add the sentiment if it is not filled in for this strategy
            String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
            if (content != null) {
                // Break the text up into tokens and match them against the emoticons
                int positive = 0;
                int negative = 0;
                StringTokenizer stringTokenizer = new StringTokenizer(content, " @");
                while (stringTokenizer.hasMoreTokens()) {
                    String token = stringTokenizer.nextToken();
                    long hash = HashUtilities.hash(token);
                    if (emoticonHashesPos.contains(hash)) {
                        positive++;
                    }
                    if (emoticonHashesNeg.contains(hash)) {
                        negative++;
                    }
                }

                if (positive > 0 && negative == 0) {
                    // Positive sentiment
                    IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.POSITIVE, indexable, document);
                } else if (negative > 0 && positive == 0) {
                    // Negative sentiment
                    IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.NEGATIVE, indexable, document);
                }
            }
        }
        return super.preProcess(indexContext, indexable, document, resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        try {
            emoticonHashesPos = new HashSet<>();
            emoticonHashesNeg = new HashSet<>();
            loadEmoticonHashes("emoticons-pos\\.txt", emoticonHashesPos);
            loadEmoticonHashes("emoticons-neg\\.txt", emoticonHashesNeg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadEmoticonHashes(final String file, final Set<Long> emoticonHashes) throws IOException {
        File emoticonPosFile = FileUtilities.findFileRecursively(new File("."), file);
        List<String> linesPos = Files.readAllLines(emoticonPosFile.toPath(), Charset.forName(IConstants.ENCODING));
        for (final String linePos : linesPos) {
            StringTokenizer stringTokenizer = new StringTokenizer(linePos, "\n\r ", false);
            while (stringTokenizer.hasMoreTokens()) {
                String emoticon = stringTokenizer.nextToken();
                Long emoticonHash = HashUtilities.hash(emoticon);
                emoticonHashes.add(emoticonHash);
            }
        }
    }

}