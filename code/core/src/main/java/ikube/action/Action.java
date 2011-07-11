package ikube.action;

import ikube.action.rule.IRule;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Mailer;

import java.util.List;

import javax.persistence.Transient;

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

	@Transient
	private transient List<IRule<E>> rules;
	@Transient
	private transient IClusterManager clusterManager;

	private String predicate;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRuleExpression() {
		return predicate;
	}

	public void setRuleExpression(final String predicate) {
		this.predicate = predicate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IRule<E>> getRules() {
		return rules;
	}

	public void setRules(final List<IRule<E>> rules) {
		this.rules = rules;
	}
	
	protected void sendNotification(final IndexContext<?> indexContext, final String subject, final String body) {
		try {
			Mailer mailer = ApplicationContextManager.getBean(Mailer.class);
			mailer.sendMail(subject, body);
		} catch (Exception e) {
			logger.error("Exception sending mail : ", e);
		}
	}

	protected IClusterManager getClusterManager() {
		if (clusterManager == null) {
			clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		}
		return clusterManager;
	}

}