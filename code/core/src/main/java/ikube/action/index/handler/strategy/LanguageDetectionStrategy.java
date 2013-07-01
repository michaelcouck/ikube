package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

/**
 * This strategy will detect the language of the resource and add the language field to the index document.
 * 
 * @author Michael Couck
 * @since 04.04.2013
 * @version 01.00
 */
public final class LanguageDetectionStrategy extends AStrategy {

	public LanguageDetectionStrategy() {
		this(null);
	}

	public LanguageDetectionStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
		initialize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		// Concatenate the data in the indexable
		String content = getContent(indexable, new StringBuilder()).toString();
		if (!StringUtils.isEmpty(content)) {
			Detector detector = DetectorFactory.create();
			detector.append(content.toString());
			try {
				String language = detector.detect();
				logger.info("Language : " + language);
				IndexManager.addStringField(IConstants.LANGUAGE, language, document, Store.YES, Index.ANALYZED, TermVector.NO);
			} catch (LangDetectException e) {
				logger.debug("Language processing error : " + e.getMessage());
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	final StringBuilder getContent(final Indexable<?> indexable, final StringBuilder builder) {
		if (indexable.getContent() != null) {
			builder.append(indexable.getContent());
		}
		if (indexable.getChildren() != null) {
			for (final Indexable<?> child : indexable.getChildren()) {
				getContent(child, builder);
			}
		}
		return builder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		if (DetectorFactory.getLangList() != null && DetectorFactory.getLangList().size() > 0) {
			return;
		}
		File profileDirectory = FileUtilities.findDirectoryRecursively(new File("."), IConstants.LANGUAGE_DETECT_PROFILES_DIRECTORY);
		try {
			if (profileDirectory != null) {
				String profileDirectoryPath = FileUtilities.cleanFilePath(profileDirectory.getAbsolutePath());
				DetectorFactory.loadProfile(profileDirectoryPath);
				logger.debug("Loading language profiles from : " + profileDirectoryPath + ", " + DetectorFactory.getLangList());
			} else {
				logger.warn("Couldn't find the language detection profile's directory : ");
			}
		} catch (Exception e) {
			logger.error("Exception starting the language detector, configuration issues : profile directory : " + profileDirectory, e);
		}
	}

}