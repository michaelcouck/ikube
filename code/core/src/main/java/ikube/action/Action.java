package ikube.action;

import ikube.action.rule.IRule;
import ikube.action.rule.RuleInterceptor;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.notify.IMailer;
import ikube.toolkit.UriUtilities;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.nfunk.jep.JEP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the base class for actions. Actions execute logic on index contexts. Actions may include opening the searcher on a new index,
 * indexing or deleting the old indexes. This class is intended to be sub-classed. Common methods in this base class is checking that the
 * index is current, i.e. has not expired and whether the searcher should be re-opened on the new index.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public abstract class Action<E, F> implements IAction<IndexContext<?>, Boolean> {

	protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

	/** This class sends mails to the configured recipient from the configured sender. */
	@Autowired
	private IMailer mailer;
	/** Access to the database, like a generic dao. */
	@Autowired
	protected IDataBase dataBase;
	/** The cluster manager for locking the cluster during rule evaluation. */
	@Autowired
	protected IClusterManager clusterManager;
	/**
	 * This is an optional action that the action depends on. For example the index action requires that the reset action is run completely
	 * first for this index context
	 */
	private IAction<IndexContext<?>, Boolean> dependent;

	/**
	 * These are the rules defined for this action. They will be evaluated collectively by the {@link RuleInterceptor} and the action will
	 * be executed depending on the result of the rules.
	 */
	private List<IRule<IndexContext<?>>> rules;

	/**
	 * This is the predicate that will be evaluated. The predicate consists of a boolean expression that contains the individual results of
	 * the rules. For example '!IsThisServerWorking && !AnyServersWorking'. The rules' results will be inserted into the parameter place
	 * holders and the expression evaluated by {@link JEP}.
	 */
	private String predicate;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(IndexContext<?> context) throws Exception {
		if (dependent != null) {
			logger.info("Executing dependent action : " + dependent);
			dependent.execute(context);
		}
		return executeInternal(context);
	}

	/**
	 * This method is called by the super class, i.e. this class on the implementations, which allows the super class to execute any actions
	 * that need to be executed, that the implementing classes rely on, like the reset action which may not have executed between indexes.
	 * 
	 * @param indexContext the index context to execute the action on
	 * @return whether the execution was successful
	 * @throws Exception
	 */
	abstract boolean executeInternal(final IndexContext<?> indexContext) throws Exception;

	/**
	 * This is a convenience method for the implementing classes to call to announce to the cluster that the action is started.
	 * 
	 * @param indexName the name of the index that the actions is starting on
	 * @param indexableName the name of the indexable that the action is performing on
	 * @return the action that is returned by the cluster manager
	 */
	protected ikube.model.Action start(String indexName, String indexableName) {
		return clusterManager.startWorking(getClass().getSimpleName(), indexName, indexableName);
	}

	/**
	 * This is a convenience method for the implementing classes to call to announce to the cluster that the action has ended.
	 * 
	 * @param action the action to announce to the cluster that is ended
	 */
	protected void stop(ikube.model.Action action) {
		clusterManager.stopWorking(action);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRuleExpression() {
		return predicate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRuleExpression(final String predicate) {
		this.predicate = predicate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IRule<IndexContext<?>>> getRules() {
		return rules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRules(final List<IRule<IndexContext<?>>> rules) {
		this.rules = rules;
	}

	/**
	 * This method will close the searchables. All the sub searchables are closed one by one first then the composite searchable to ensure
	 * that they are all closed completely.
	 * 
	 * @param indexContext the index context to close the searchables for
	 */
	protected void closeSearchables(IndexContext<?> indexContext) {
		MultiSearcher multiSearcher = indexContext.getMultiSearcher();
		if (multiSearcher != null) {
			// Get all the searchables from the searcher and close them one by one
			Searchable[] searchables = multiSearcher.getSearchables();
			if (searchables != null) {
				for (Searchable searchable : searchables) {
					try {
						IndexSearcher indexSearcher = (IndexSearcher) searchable;
						IndexReader reader = indexSearcher.getIndexReader();
						Directory directory = reader.directory();
						if (IndexWriter.isLocked(directory)) {
							IndexWriter.unlock(directory);
						}
						close(directory, reader, searchable);
					} catch (NullPointerException e) {
						logger.error("Reader closed perhaps?");
						logger.debug(null, e);
					} catch (Exception e) {
						logger.error("Exception trying to close the searcher", e);
					}
				}
			}
			indexContext.setMultiSearcher(null);
		}
	}

	/**
	 * This method sill close the directory, the reader and or the searchable depending on whether they are still open.
	 * 
	 * @param directory the Lucene directory to close
	 * @param reader the Lucene reader to close
	 * @param searcher the Lucene searchable to close
	 */
	protected void close(Directory directory, IndexReader reader, Searchable searcher) {
		try {
			if (directory != null) {
				directory.close();
			}
		} catch (Exception e) {
			logger.error("Exception closing the directory : ", e);
		}
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (Exception e) {
			logger.error("Exception closing the reader : ", e);
		}
		try {
			if (searcher != null) {
				searcher.close();
			}
		} catch (Exception e) {
			logger.error("Exception closing the searcher : ", e);
		}
	}

	/**
	 * This method will send a mail to the address that is configured for the {@link IMailer}.
	 * 
	 * @param subject the subject of the message
	 * @param body and the main text for the message
	 */
	protected void sendNotification(final String subject, final String body) {
		try {
			String ip = UriUtilities.getIp();
			mailer.sendMail(subject + ":" + ip, body);
		} catch (Exception e) {
			logger.error("Exception sending mail : " + subject, e);
			logger.error("Mailer details : " + ToStringBuilder.reflectionToString(mailer), e);
		}
	}

	/**
	 * Sets the action that this action is dependent on. For example the the index action requires that the reset action is executed first.
	 * In this way the actions can be chained and the results from the previous action used to determine whether the action is then
	 * executed.
	 * 
	 * @param dependent the action that this action is dependent on
	 */
	public void setDependent(IAction<IndexContext<?>, Boolean> dependent) {
		this.dependent = dependent;
	}

}