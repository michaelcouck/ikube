package ikube.service;

/**
 * This interface is for implementing the search on the auto completion index. This index is typically an n-gram index of the searches that
 * have been performed on the collections in the configuration.?
 * 
 * @author Michael Couck
 * @since 29.10.12
 * @version 01.00
 */
public interface IAutoCompleteService {

	/**
	 * This method will return a string array of the suggested searches that have been performed on the collections, based on an n-gram
	 * matching.
	 * 
	 * @param searchString the search string to patch against the searches that have already been performed on the indexes
	 * @return the suggestions for the auto complete
	 */
	String[] suggestions(final String searchString);

}
