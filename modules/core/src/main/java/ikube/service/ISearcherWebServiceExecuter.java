package ikube.service;

import java.util.List;
import java.util.Map;

/**
 * This interface is for testing the web service in production. Typically implementations will be added to the timer and fired periodically. The
 * results from executing the web service can then be collected in an event and verified against the expected results so there can be continuous
 * monitoring of the web service in production.
 *
 * @author Michael Couck
 * @since 11.08.10
 * @version 01.00
 */
public interface ISearcherWebServiceExecuter {

	/**
	 * Executes the call on the web service.
	 *
	 * @throws Exception
	 *             any exception, should be caught and an event registered
	 */
	public List<Map<String, String>> execute() throws Exception;

	/** Setters for the properties for accessing the web service. Defined in the Spring configuration. */

	public void setEndpointUri(String endpointUri);

	public void setConfigurationName(String configurationName);

	public void setSearchString(String searchString);

	public void setFieldName(String fieldName);

	public void setFragment(boolean fragment);

	public void setStart(int start);

	public void setEnd(int end);

	public void setResultsSizeMinimum(int resultsSizeMinimum);

}
