package ikube.action;

import ikube.IConstants;
import ikube.listener.Event;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.SerializationUtilities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class will do a search on the indexes on the index defined in this server. If there are not as many results as
 * expected then a mail will be sent to the administrator.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Searcher extends Action<IndexContext<?>, Boolean> {

	private int		start				= 0;
	private int		end					= 10;
	private int		iterations			= 1;
	private String	searchString		= "Hello";
	private int		resultsSizeMinimum	= 0;
	private boolean	fragment			= Boolean.TRUE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Boolean execute(final IndexContext<?> indexContext) {
		try {
			String indexName = indexContext.getIndexName();

			if (indexName.contains("dictionary")) {
				return Boolean.FALSE;
			}
			String xml = null;

			Server server = getClusterManager().getServer();

			String ip = InetAddress.getLocalHost().getHostAddress();
			int searchWebServicePort = server.getSearchWebServicePort();
			ISearcherWebService searcherWebService = ServiceLocator.getService(ISearcherWebService.class, "http", ip, searchWebServicePort,
					ISearcherWebService.PUBLISHED_PATH, ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);

			int monitoringPort = server.getMonitoringWebServicePort();

			IMonitorWebService monitorWebService = ServiceLocator.getService(IMonitorWebService.class, "http", ip, monitoringPort,
					IMonitorWebService.PUBLISHED_PATH, IMonitorWebService.NAMESPACE, IMonitorWebService.SERVICE);

			String[] searchFields = monitorWebService.getIndexFieldNames(indexName);
			String[] searchStrings = new String[searchFields.length];
			String[] sortFields = new String[searchFields.length];
			Arrays.fill(searchStrings, 0, searchStrings.length, searchString);
			System.arraycopy(searchFields, 0, sortFields, 0, sortFields.length);
			for (int i = 0; i < iterations; i++) {
				if (searchFields == null || searchFields.length == 0) {
					searchFields = new String[] { IConstants.CONTENT };
				}
				xml = searcherWebService.searchSingle(indexName, searchString, searchFields[0], Boolean.TRUE, 0, 10);
				xml = searcherWebService.searchMulti(indexName, searchStrings, searchFields, fragment, start, end);
				double latitude = 50.7930727874172;
				double longitude = 4.36242219751376;
				xml = searcherWebService.searchSpacialMulti(indexName, searchStrings, searchFields, fragment, start, end, 10, latitude, longitude);
				xml = searcherWebService.searchMultiAll(indexName, searchStrings, fragment, start, end);
			}

			List<Map<String, String>> results = new ArrayList<Map<String, String>>();
			if (xml != null) {
				results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
			}
			ListenerManager listenerManager = ApplicationContextManager.getBean(ListenerManager.class);
			if (results.size() < resultsSizeMinimum) {
				String message = Logging.getString("Results not expected : ", results.size(), indexContext.getIndexName(), searchString, start, end,
						resultsSizeMinimum);
				logger.info(message);
				listenerManager.fireEvent(Event.NO_RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
				String subject = "Search results for index : " + indexContext.getIndexName() + ", server : " + server.getAddress();
				String body = xml;
				sendNotification(subject, body);
			} else {
				listenerManager.fireEvent(Event.RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
			}
		} catch (UnknownHostException e) {
			logger.error("Exception searching index : ", e);
		} finally {
			getClusterManager().stopWorking(getClass().getSimpleName(), indexContext.getIndexName(), "");
		}
		return Boolean.TRUE;
	}

	public void setFragment(boolean fragment) {
		this.fragment = fragment;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public void setResultsSizeMinimum(int resultsSizeMinimum) {
		this.resultsSizeMinimum = resultsSizeMinimum;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

}