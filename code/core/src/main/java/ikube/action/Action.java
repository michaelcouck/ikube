package ikube.action;

import ikube.action.rule.IRule;
import ikube.action.rule.RuleInterceptor;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.toolkit.IMailer;
import ikube.toolkit.UriUtilities;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Closeable;
import java.util.List;

/**
 * This is the base class for actions. Actions execute logic on index contexts. Actions may include opening the searcher on a new index, indexing or deleting
 * the old indexes. This class is intended to be sub-classed. Common methods in this base class is checking that the index is current, i.e. has not expired and
 * whether the searcher should be re-opened on the new index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21.11.10
 */
@SuppressWarnings({"SpringJavaAutowiringInspection", "UnusedDeclaration"})
public abstract class Action<E, F> implements IAction<IndexContext<?>, Boolean> {

    protected transient Logger logger = Logger.getLogger(this.getClass());

    /**
     * This class sends mails to the configured recipient from the configured sender.
     */
    @Autowired
    private IMailer mailer;

    /**
     * Access to the database, like a generic dao.
     */
    @Autowired
    protected IDataBase dataBase;

    /**
     * The cluster manager for locking the cluster during rule evaluation.
     */
    @Autowired
    protected IClusterManager clusterManager;

    /**
     * This is an optional action that the action depends on. For example the index action requires that the reset action is run completely first for this index
     * context
     */
    private IAction<IndexContext<?>, Boolean> dependent;

    /**
     * These are the rules defined for this action. They will be evaluated collectively by the {@link RuleInterceptor} and the action will be executed depending
     * on the category of the rules.
     */
    private List<IRule<IndexContext<?>>> rules;

    /**
     * This is the predicate that will be evaluated. The predicate consists of a boolean expression that contains the individual results of the rules. For
     * example '!IsThisServerWorking && !AnyServersWorking'. The rules' results will be inserted into the parameter place holders and the expression evaluated
     * by {@link JexlEngine}.
     */
    private String predicate;

    /**
     * Flag to exclude actions that don't need a cluster lock to perform the execution of the rules.
     */
    private boolean requiresClusterLock;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean preExecute(final IndexContext<?> indexContext) throws Exception {
        // To be over ridden by sub classes
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean execute(final IndexContext<?> indexContext) throws Exception {
        try {
            preExecute(indexContext);
            boolean result = internalExecute(indexContext);
            if (result && dependent != null) {
                result = dependent.execute(indexContext);
            }
            return result;
        } finally {
            postExecute(indexContext);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresClusterLock() {
        return requiresClusterLock;
    }

    /**
     * This method will return whether the action requires a cluster lock. In many cases, like the {@link Reset} action, that only works on this server, it is
     * not necessary to have the cluster lock, and because getting the lock in a cluster of 100 machines is very network expensive, we try to avoid this if we
     * can.
     *
     * @param requiresClusterLock whether the action rewuires cluster atomicity and a lock before the logic is executed
     */
    public void setRequiresClusterLock(final boolean requiresClusterLock) {
        this.requiresClusterLock = requiresClusterLock;
    }

    /**
     * This method is called by the super class, i.e. this class on the implementations, which allows the super class to execute any actions that need to be
     * executed, that the implementing classes rely on, like the reset action which may not have executed between indexes.
     *
     * @param indexContext the index context to execute the action on
     * @return whether the execution was successful
     * @throws Exception
     */
    abstract boolean internalExecute(final IndexContext<?> indexContext) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postExecute(final IndexContext<?> indexContext) throws Exception {
        // To be over ridden by sub classes
        return Boolean.TRUE;
    }

    /**
     * This is a convenience method for the implementing classes to call to announce to the cluster that the action is started.
     *
     * @param indexName     the name of the index that the actions is starting on
     * @param indexableName the name of the indexable that the action is performing on
     * @return the action that is returned by the cluster manager
     */
    protected ikube.model.Action start(final String indexName, final String indexableName) {
        return clusterManager.startWorking(getClass().getSimpleName(), indexName, indexableName);
    }

    /**
     * This is a convenience method for the implementing classes to call to announce to the cluster that the action has ended.
     *
     * @param action the action to announce to the cluster that is ended
     */
    protected void stop(final ikube.model.Action action) {
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
     * This method sill close the directory, the reader and or the searchable depending on whether they are still open.
     *
     * @param closeables the items to close
     */
    protected void close(final Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (final Closeable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (final NullPointerException e) {
                logger.error("Already closed perhaps?");
                logger.debug(null, e);
            } catch (final Exception e) {
                logger.error("Exception closing the directory : ", e);
            }
        }
    }

    /**
     * This method will send a mail to the address that is configured for the {@link IMailer}.
     *
     * @param subject the subject of the message
     * @param body    and the main text for the message
     */
    protected void sendNotification(final String subject, final String body) {
        try {
            String ip = UriUtilities.getIp();
            mailer.sendMail(subject + ":" + ip, body);
        } catch (final Exception e) {
            logger.error("Exception sending mail : " + subject + ", " + e.getMessage(), e);
        }
    }

    /**
     * Sets the action that this action is dependent on. For example the the index action requires that the reset action is executed first. In this way the
     * actions can be chained and the results from the previous action used to determine whether the action is then executed.
     *
     * @param dependent the action that this action is dependent on
     */
    public void setDependent(final IAction<IndexContext<?>, Boolean> dependent) {
        this.dependent = dependent;
    }

}