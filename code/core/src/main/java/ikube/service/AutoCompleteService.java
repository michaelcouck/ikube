package ikube.service;

import ikube.IConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see IAutoCompleteService
 * @author Michael Couck
 * @since 29.10.12
 * @version 01.00
 */
public class AutoCompleteService implements IAutoCompleteService {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AutoCompleteService.class);

	@Autowired
	private ISearcherService searcherService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] suggestions(String searchString) {
		boolean first = Boolean.TRUE;
		StringTokenizer stringTokenizer = new StringTokenizer(searchString, " ,;|.");
		StringBuilder stringBuilder = new StringBuilder();
		while (stringTokenizer.hasMoreTokens()) {
			String token = stringTokenizer.nextToken();
			if (!first) {
				stringBuilder.append(" AND ");
			}
			stringBuilder.append(token);
			first = Boolean.FALSE;
		}
		ArrayList<HashMap<String, String>> results = searcherService.searchSingle(IConstants.AUTOCOMPLETE, stringBuilder.toString(),
				IConstants.CONTENT, Boolean.TRUE, 0, 100);
		if (results.size() > 0) {
			results.remove(results.size() - 1);
		}
		Set<String> suggestions = new TreeSet<String>();
		for (final HashMap<String, String> result : results) {
			suggestions.add(result.get(IConstants.CONTENT));
			if (suggestions.size() >= 10) {
				break;
			}
		}
		return suggestions.toArray(new String[suggestions.size()]);
	}

}