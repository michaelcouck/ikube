package ikube.monitoring;

import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Server;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.Mailer;
import ikube.toolkit.SerializationUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @see ISearcherWebServiceExecuter
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SuppressWarnings("unchecked")
public class SearcherWebServiceExecuter implements ISearcherWebServiceExecuter {

	private static final Logger LOGGER = Logger.getLogger(SearcherWebServiceExecuter.class);

	private transient int end;
	private transient int start;
	private transient int iterations = 1;
	private transient String indexName;
	private transient String searchString;
	private transient boolean fragment;
	private transient int resultsSizeMinimum;

	public SearcherWebServiceExecuter() {
		LOGGER.info("SearcherWebServiceExecuter()");
		ListenerManager.addListener(new IListener() {
			@Override
			public void handleNotification(final Event event) {
				LOGGER.info("Event : " + event);
				if (event.getType().equals(Event.PERFORMANCE)) {
					try {
						execute();
					} catch (Exception e) {
						LOGGER.error("Exception accessing the results", e);
					}
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Map<String, String>> execute() throws Exception {
		List<Server> servers = ApplicationContextManager.getBean(IClusterManager.class).getServers();
		String xml = null;
		for (Server server : servers) {
			String webServiceUrl = server.getSearchWebServiceUrl();
			ISearcherWebService searchRemote = ServiceLocator.getService(ISearcherWebService.class, webServiceUrl,
					ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
			String monitoringWebServiceUrl = server.getMonitoringWebServiceUrl();
			IMonitorWebService monitorWebService = ServiceLocator.getService(IMonitorWebService.class, monitoringWebServiceUrl,
					IMonitorWebService.NAMESPACE, IMonitorWebService.SERVICE);
			String[] indexNames = monitorWebService.getIndexNames();
			for (String indexName : indexNames) {
				String[] searchFields = monitorWebService.getIndexFieldNames(indexName);
				String[] searchStrings = new String[searchFields.length];
				String[] sortFields = new String[searchFields.length];
				Arrays.fill(searchStrings, 0, searchStrings.length, searchString);
				System.arraycopy(searchFields, 0, sortFields, 0, sortFields.length);
				double latitude = new Double(50.7930727874172);
				double longitude = new Double(4.36242219751376);
				for (int i = 0; i < iterations; i++) {
					xml = searchRemote.searchSingle(indexName, searchString, searchFields[0], fragment, start, end);
					xml = searchRemote.searchMulti(indexName, searchStrings, searchFields, fragment, start, end);
					// xml = searchRemote.searchMultiSorted(indexName, searchStrings, searchFields, sortFields, fragment, start, end);
					xml = searchRemote.searchSpacialMulti(indexName, searchStrings, searchFields, fragment, start, end, 10, latitude,
							longitude);
					xml = searchRemote.searchMultiAll(indexName, searchStrings, fragment, start, end);
				}
			}
		}
		List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
		if (results.size() < resultsSizeMinimum) {
			String message = Logging.getString("Results not expected : ", results.size(), indexName, searchString, start, end,
					resultsSizeMinimum);
			LOGGER.info(message);
			ListenerManager.fireEvent(Event.NO_RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
			Mailer mailer = ApplicationContextManager.getBean(Mailer.class);
			mailer.sendMail("Integration results : " + indexName, results.toString());
		} else {
			ListenerManager.fireEvent(Event.RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIndexName(final String indexName) {
		this.indexName = indexName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSearchString(final String searchString) {
		this.searchString = searchString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFragment(final boolean fragment) {
		this.fragment = fragment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStart(final int start) {
		this.start = start;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnd(final int end) {
		this.end = end;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResultsSizeMinimum(final int resultsSizeMinimum) {
		this.resultsSizeMinimum = resultsSizeMinimum;
	}

}