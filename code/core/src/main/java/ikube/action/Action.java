package ikube.action;

import ikube.action.rule.AreDirectoriesEqual;
import ikube.action.rule.AreIndexesCreated;
import ikube.action.rule.AreSearchablesInitialised;
import ikube.action.rule.AreUnopenedIndexes;
import ikube.action.rule.DirectoryExistsAndIsLocked;
import ikube.action.rule.IRule;
import ikube.action.rule.IsIndexCurrent;
import ikube.action.rule.IsMultiSearcherInitialised;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This is the base class for actions. Actions execute logic on index contexts. Actions may include opening the searcher on a new index,
 * indexing or deleting the old indexes. This class is intended to be sub-classed. Common methods in this base class is checking that the
 * index is current, i.e. has not expired and whether the searcher should be re-opened on the new index.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class Action implements IAction<IndexContext, Boolean> {

	protected transient Logger logger = Logger.getLogger(Action.class);
	/** The cluster synchronization class. */
	private transient IClusterManager clusterManager;

	private String predicate;
	private List<IRule<?>> rules;

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(final String predicate) {
		this.predicate = predicate;
	}

	public List<IRule<?>> getRules() {
		return rules;
	}

	public void setRules(final List<IRule<?>> rules) {
		this.rules = rules;
	}

	protected IClusterManager getClusterManager() {
		if (clusterManager == null) {
			clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		}
		return clusterManager;
	}

	/**
	 * Checks to see if the current index is not passed it's expiration period. Each index had a parent directory that is a long of the
	 * system time that the index was started. This time signifies the age of the index.
	 * 
	 * @param indexContext
	 *            the index context to check if the index is expired
	 * @return whether the index for this index context is passed it's expiration date
	 */
	protected boolean isIndexCurrent(final IndexContext indexContext) {
		return new IsIndexCurrent().evaluate(indexContext);
	}

	/**
	 * This method returns whether the searcher should be re-opened on a new index. If there is a new index, or if there is an index added
	 * by another server then the searcher should be opened again.
	 * 
	 * @param indexContext
	 *            the index context for the index
	 * @return whether the index should be re-opened
	 */
	protected boolean shouldReopen(final IndexContext indexContext) {
		// If there is no searcher open then try to open one
		if (!new IsMultiSearcherInitialised().evaluate(indexContext)) {
			logger.debug("Multi searcher null, should try to reopen : ");
			return Boolean.TRUE;
		}
		if (!new AreSearchablesInitialised().evaluate(indexContext)) {
			logger.debug("No searchables open, should try to reopen : ");
			return Boolean.TRUE;
		}
		if (!new IsIndexCurrent().evaluate(indexContext)) {
			logger.debug("Index not current, no need to reopen : ");
			return Boolean.FALSE;
		}
		if (new AreIndexesCreated().evaluate(indexContext) && new AreUnopenedIndexes().evaluate(indexContext)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * Checks to see if the directory exists on the file system and is not locked by Lucene, i.e. that a searcher can be opened on it.
	 * 
	 * @param indexDirectory
	 *            the directory to check for existence and availability
	 * @return whether the directory exists as a Lucene index and is not locked by Lucene
	 */
	protected boolean directoryExistsAndNotLocked(final File indexDirectory) {
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexDirectory);
			boolean exists = IndexReader.indexExists(directory);
			boolean locked = IndexWriter.isLocked(directory);
			// logger.info(Logging.getString("Server index directory : ", indexDirectory, ", exists : ", exists, ", locked : ", locked));
			if (exists && !locked) {
				return Boolean.TRUE;
			} else {
				logger.info("Non existant or locked directory found, will not open on this one yet : " + directory);
			}
		} catch (Exception e) {
			logger.error("Exception checking the directories : ", e);
		} finally {
			try {
				directory.close();
			} catch (Exception e) {
				logger.error("Exception closing the directory : " + directory, e);
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Checks to see if the directory exists on the file system and is locked by Lucene, i.e. that this directory is an index being created.
	 * 
	 * @param indexDirectory
	 *            the directory to check for existence and being locked
	 * @return whether the directory exists as a Lucene index and is locked
	 */
	protected boolean directoryExistsAndIsLocked(final File indexDirectory) {
		return new DirectoryExistsAndIsLocked().evaluate(indexDirectory);
	}

	@SuppressWarnings("unused")
	private boolean directoriesEqual(final File directoryOne, final File directoryTwo) {
		return new AreDirectoriesEqual().evaluate(new File[] { directoryOne, directoryTwo });
	}

}