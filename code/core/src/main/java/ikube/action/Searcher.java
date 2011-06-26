package ikube.action;

import ikube.listener.Event;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.Mailer;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class will do a search on the indexes on all the servers defined in this cluster. If there are not as many results as expected then
 * a mail will be sent to the administrator.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Searcher extends Action<IndexContext<?>, Boolean> {

	private boolean fragment = Boolean.TRUE;
	private int start = 0;
	private int end = 10;
	private String searchString;
	private int resultsSizeMinimum;
	private int iterations;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Boolean execute(final IndexContext<?> indexContext) {
		try {
			List<Server> servers = getClusterManager().getServers();
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
						xml = searchRemote.searchSingle(indexName, searchString, searchFields[0], Boolean.TRUE, 0, 10);
						xml = searchRemote.searchMulti(indexName, searchStrings, searchFields, fragment, start, end);
						// xml = searchRemote.searchMultiSorted(indexName, searchStrings, searchFields, sortFields, fragment, start, end);
						xml = searchRemote.searchSpacialMulti(indexName, searchStrings, searchFields, fragment, start, end, 10, latitude,
								longitude);
						xml = searchRemote.searchMultiAll(indexName, searchStrings, fragment, start, end);
					}
				}
			}
			List<Map<String, String>> results = new ArrayList<Map<String, String>>();
			if (xml != null) {
				results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
			}
			if (results.size() < resultsSizeMinimum) {
				String message = Logging.getString("Results not expected : ", results.size(), indexContext.getIndexName(), searchString,
						start, end, resultsSizeMinimum);
				logger.info(message);
				ListenerManager.fireEvent(Event.NO_RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
				sendNotification(indexContext, "Integration results : " + indexContext.getIndexName(), results.toString());
			} else {
				ListenerManager.fireEvent(Event.RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
			}
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getSimpleName(), "", Boolean.FALSE);
		}
		return Boolean.TRUE;
	}

	protected void sendNotification(final IndexContext<?> indexContext, final String subject, final String body) {
		try {
			Mailer mailer = ApplicationContextManager.getBean(Mailer.class);
			mailer.sendMail(subject, body);
		} catch (Exception e) {
			logger.error("Exception sending mail : ", e);
		}
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