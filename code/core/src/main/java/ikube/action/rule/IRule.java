package ikube.action.rule;

/**
 * This is the base interface for the rules. Rules are executed one by one, then the results are evaluated in Jep. Arbitrarily complex
 * expressions can be compiled by the rules, for example "IsIndexCurrent && (IndexIsBackedUp || IndexIsCurrent)".
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public interface IRule<T> {

	/**
	 * This method evaluates the condition for the rule. The object passed to the method as a parameter can be the index context, a file or
	 * an indexable etc. The rule then needs to differentiate the type of object and perform the validation.
	 * 
	 * @param object
	 *            the object to perform the evaluation of, either a file or the index etc.
	 * @return whether the evaluation of the logic resulted in a true or false. For example if the rule is to check whether the index has
	 *         been created and the index is created then the logic would verify that the index exists and is not corrupt and return true
	 */
	boolean evaluate(T object);

}
