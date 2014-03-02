package ikube.action;

import ikube.action.rule.IRule;
import ikube.scheduling.schedule.IndexSchedule;

import java.util.List;

/**
 * This is the interface to be implemented for actions see {@link Action}.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public interface IAction<E, F> {

    /**
     * Sets the predicate that is a rule in a format that the rules engine can process.
     *
     * @param predicate the predicate for the rules engine
     */
    void setRuleExpression(final String predicate);

    /**
     * Accesses the predicate or rule expression. This is a boolean expression that can be interpreted by
     * {@link org.apache.commons.jexl2.JexlEngine}. Typically this expression will be something like '!ServerIsWorking &&
     * !IndexesAreCreated'. At runtime the variables will be substituted with the values that are returned from these
     * rules, and the expression will then be evaluated.
     *
     * @return the string representation of the rules expression for Jexl
     */
    String getRuleExpression();

    /**
     * Sets the rules that are
     *
     * @param rules the rules to execute before running this action
     */
    void setRules(final List<IRule<E>> rules);

    /**
     * Access to the rules for this action. Actions will get executed if the rules evaluate to true for the
     * action. The rules are interpreted one by one, the values are substituted into the rules expression and fed
     * to {@link org.apache.commons.jexl2.JexlEngine} to evaluate in a rule graph.
     *
     * @return the rules that are defined for this action
     */
    List<IRule<E>> getRules();

    /**
     * TODO Move this logic to the strategies
     */
    boolean preExecute(final E indexContext) throws Exception;

    /**
     * Executes the action on the index context. The generic parameter E is the index context and the return
     * value is typically a boolean indicating that the action executed completely or not.
     *
     * @param indexContext the index context
     * @return whether the action completed the logic successfully
     * @throws Exception any exception during the action. This should be propagated up the stack to the caller,
     *                   which is generally the {@link IndexSchedule}
     */
    F execute(final E indexContext) throws Exception;

    /**
     * TODO Move this logic to the strategies
     */
    boolean postExecute(final E indexContext) throws Exception;

    boolean requiresClusterLock();

}