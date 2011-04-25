package ikube.action;

import java.util.List;

import ikube.IndexEngine;
import ikube.action.rule.IRule;

/**
 * This is the interface to be implemented for actions see {@link Action}.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IAction<E, F> {
	
	String getRuleExpression();
	
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
