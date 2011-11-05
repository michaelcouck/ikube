package ikube.action;

import ikube.action.rule.IRule;
import ikube.action.rule.RuleInterceptor;
import ikube.cluster.IClusterManager;
import ikube.notify.IMailer;
import ikube.notify.Mailer;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;

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

	/**
	 * These are the rules defined for this action. They will be evaluated collectively by the {@link RuleInterceptor} and the action will
	 * be executed depending on the result of the rules.
	 */
	@Transient
	private transient List<IRule<E>> rules;
	/** The cluster manager for locking the cluster during rule evaluation. */
	@Transient
	private transient IClusterManager clusterManager;
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
	public List<IRule<E>> getRules() {
		return rules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRules(final List<IRule<E>> rules) {
		this.rules = rules;
	}

	protected void sendNotification(final String subject, final String body) {
		IMailer mailer = null;
		try {
			mailer = ApplicationContextManager.getBean(Mailer.class);
			mailer.sendMail(subject, body);
		} catch (Exception e) {
			logger.error("Exception sending mail : " + subject, e);
			logger.error("Mailer details : " + ToStringBuilder.reflectionToString(mailer), e);
		}
	}

	public void setClusterManager(IClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	protected IClusterManager getClusterManager() {
		return clusterManager;
	}

}