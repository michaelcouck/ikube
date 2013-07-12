package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 19.06.13
 * @version 01.00
 */
public final class EmoticonSentimentAnalysisStrategy extends AStrategy {

	private Set<Long> emoticonHashesPos;
	private Set<Long> emoticonHashesNeg;
	private AtomicInteger atomicInteger;

	public EmoticonSentimentAnalysisStrategy() {
		this(null);
	}

	public EmoticonSentimentAnalysisStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
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
				boolean pos = Boolean.FALSE;
				boolean neg = Boolean.FALSE;
				StringTokenizer stringTokenizer = new StringTokenizer(content, " ");
				while (stringTokenizer.hasMoreTokens()) {
					String token = stringTokenizer.nextToken();
					long hash = HashUtilities.hash(token);
					if (emoticonHashesPos.contains(hash)) {
						pos = Boolean.TRUE;
						// Replace emoticons with the equivalent words, either positive or negative?
					}
					if (emoticonHashesNeg.contains(hash)) {
						neg = Boolean.TRUE;
						// Replace emoticons with the equivalent words, either positive or negative?
					}
				}
				if (pos && neg) {
					// Do nothing because there are positive and negative
				} else if (pos) {
					// Positive sentiment
					IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.POSITIVE, document, Store.YES, Index.ANALYZED, TermVector.NO);
				} else {
					// Negative sentiment
					IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.NEGATIVE, document, Store.YES, Index.ANALYZED, TermVector.NO);
				}
				if ((pos || neg) && (atomicInteger.getAndIncrement() % 1000 == 0)) {
					// Found at least one emoticon!
					logger.info("Emoticon : " + pos + ", " + neg);
				}
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		try {
			atomicInteger = new AtomicInteger(0);
			emoticonHashesPos = new HashSet<Long>();
			emoticonHashesNeg = new HashSet<Long>();
			loadEmoticonHashes("emoticons-pos\\.txt", emoticonHashesPos);
			loadEmoticonHashes("emoticons-neg\\.txt", emoticonHashesNeg);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadEmoticonHashes(final String file, final Set<Long> emoticonHashes) throws IOException {
		File emoticonPosFile = FileUtilities.findFileRecursively(new File("."), file);
		List<String> linesPos = Files.readAllLines(emoticonPosFile.toPath(), Charset.defaultCharset());
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