package ikube.monitoring;

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
	List<Map<String, String>> execute() throws Exception;

	/** Setters for the properties for accessing the web service. Defined in the Spring configuration. */

	void setProtocol(String protocol);

	void setPort(Integer port);

	void setPath(String path);
	
	void setIndexName(String indexName);

	void setSearchString(String searchString);

	void setFieldName(String fieldName);

	void setFragment(boolean fragment);

	void setStart(int start);

	void setEnd(int end);

	void setResultsSizeMinimum(int resultsSizeMinimum);

}
