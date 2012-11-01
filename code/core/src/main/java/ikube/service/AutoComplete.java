package ikube.service;

import ikube.IConstants;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class will get all the existing searches from the database and index them. Then it will execute n-gram queries on the index (in
 * memory) returning the best set of matches.
 * 
 * @author Michael Couck
 * @since 29.10.12
 * @version 01.00
 */
public class AutoComplete implements IAutoComplete {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AutoComplete.class);

	@Autowired
	private ISearcherService searcherService;

	@Override
	public String[] suggestions(String searchString) {
		ArrayList<HashMap<String, String>> results = searcherService.searchSingle(IConstants.AUTOCOMPLETE, searchString,
				IConstants.CONTENT, Boolean.TRUE, 0, 10);
		if (results.size() > 0) {
			results.remove(results.size() - 1);
		}
		int index = 0;
		String[] suggestions = new String[results.size()];
		for (final HashMap<String, String> result : results) {
			suggestions[index++] = result.get(IConstants.FRAGMENT);
		}
		// TODO Highlight the characters that are a hit orare these already highlighted
		return suggestions;
	}

}