package ikube.action;

import ikube.IndexEngine;
import ikube.action.rule.IRule;

import java.util.List;

import org.nfunk.jep.JEP;

/**
 * This is the interface to be implemented for actions see {@link Action}.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IAction<E, F> {

	/**
	 * Accesses the predicate or rule expression. This is a boolean expression that can be interpreted by {@link JEP}. Typically this
	 * expression will be something like '!ServerIsWorking && !IndexesAreCreated'. At runtime the variables will be substituted with the
	 * values that are returned from these rules, and the expression will then be evaluated.
	 * 
	 * @return the string representation of the rules expression for JEP
	 */
	String getRuleExpression();

	/**
	 * Access to the rules for this action. Actions will get executed if the rules evaluate to true for the action. The rules are
	 * interpreted one by one, the values are substituted into the rules expression and fed to {@link JEP} to evaluate in a rule graph.
	 * 
	 * @return the rules that are defined for this action
	 */
	List<IRule<E>> getRules();

	/**
	 * Executes the action on the index context. The generic parameter E is the index context and the return value is typically a boolean
	 * indicating that the action executed completely or not.
	 * 
	 * @param context
	 *            the index context
	 * @return whether the action completed the logic successfully
	 * @throws Exception
	 *             any exception during the action. This should be propagated up the stack to the caller, which is generally the
	 *             {@link IndexEngine}
	 */
	F execute(E context) throws Exception;

}
