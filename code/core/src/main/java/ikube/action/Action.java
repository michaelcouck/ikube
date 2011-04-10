package ikube.action;

import ikube.action.rule.AreDirectoriesEqual;
import ikube.action.rule.AreIndexesCreated;
import ikube.action.rule.AreSearchablesInitialised;
import ikube.action.rule.AreUnopenedIndexes;
import ikube.action.rule.DirectoryExistsAndIsLocked;
import ikube.action.rule.DirectoryExistsAndNotLocked;
import ikube.action.rule.IRule;
import ikube.action.rule.IsIndexCurrent;
import ikube.action.rule.IsMultiSearcherInitialised;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This is the base class for actions. Actions execute logic on index contexts. Actions may include opening the searcher on a new index,
 * indexing or deleting the old indexes. This class is intended to be sub-classed. Common methods in this base class is checking that the
 * index is current, i.e. has not expired and whether the searcher should be re-opened on the new index.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class Action<E, F> implements IAction<E, F> {

	protected transient Logger logger = Logger.getLogger(Action.class);
	/** The cluster synchronization class. */
	private transient IClusterManager clusterManager;

	private String predicate;
	private List<IRule<E>> rules;

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(final String predicate) {
		this.predicate = predicate;
	}

	public List<IRule<E>> getRules() {
		return rules;
	}

	public void setRules(final List<IRule<E>> rules) {
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
	 * @deprecated this will be removed with the next major release
	 */
	protected boolean isIndexCurrent(final IndexContext indexContext) {
		return new IsIndexCurrent().evaluate(indexContext);
	}

	/**
	 * This method returns whether the searcher should be re-opened on a new index. If there is a new index, or if there is an index added
	 * by another server then the searcher should be opened again.
	 * 
	 * 
	 * @param indexContext
	 *            the index context for the index
	 * @return whether the index should be re-opened
	 * @deprecated this method is replaced by the rules
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
	 * @deprecated this will be removed with the next major release
	 */
	protected boolean directoryExistsAndNotLocked(final File indexDirectory) {
		return new DirectoryExistsAndNotLocked().evaluate(indexDirectory);
	}

	/**
	 * Checks to see if the directory exists on the file system and is locked by Lucene, i.e. that this directory is an index being created.
	 * 
	 * @param indexDirectory
	 *            the directory to check for existence and being locked
	 * @return whether the directory exists as a Lucene index and is locked
	 * @deprecated this will be removed with the next major release
	 */
	protected boolean directoryExistsAndIsLocked(final File indexDirectory) {
		return new DirectoryExistsAndIsLocked().evaluate(indexDirectory);
	}

	/**
	 * @param directoryOne
	 * @param directoryTwo
	 * @return
	 * @deprecated this will be removed with the next major release
	 */
	@SuppressWarnings("unused")
	private boolean directoriesEqual(final File directoryOne, final File directoryTwo) {
		return new AreDirectoriesEqual().evaluate(new File[] { directoryOne, directoryTwo });
	}

}