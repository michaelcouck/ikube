package ikube.monitoring;

import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.Logging;
import ikube.toolkit.SerializationUtilities;

import java.net.InetAddress;
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

	private Logger logger;
	private transient String protocol;
	private transient Integer port;
	private transient String path;
	private transient String indexName;
	private transient String searchString;
	private transient String fieldName;
	private transient boolean fragment;
	private transient int start;
	private transient int end;
	private transient int resultsSizeMinimum;

	public SearcherWebServiceExecuter() {
		this.logger = Logger.getLogger(this.getClass());
		ListenerManager.addListener(new IListener() {
			@Override
			public void handleNotification(final Event event) {
				if (event.getType().equals(Event.SERVICE)) {
					try {
						execute();
					} catch (Exception e) {
						logger.error("Exception accessing the results", e);
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
		String host = InetAddress.getLocalHost().getHostAddress();
		ISearcherWebService searchRemote = ServiceLocator.getService(ISearcherWebService.class, protocol, host, port, path,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
		String xml = searchRemote.searchSingle(indexName, searchString, fieldName, fragment, start, end);
		List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
		if (results.size() < resultsSizeMinimum) {
			logger.warn(Logging.getString("Results not expected : " + results, indexName, searchString, fieldName, fragment, start, end, resultsSizeMinimum));
			ListenerManager.fireEvent(Event.NO_RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
		} else {
			// logger.info("Results expected : " + (results != null ? results.size() : 0));
			ListenerManager.fireEvent(Event.RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProtocol(final String protocol) {
		this.protocol = protocol;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPort(final Integer port) {
		this.port = port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPath(final String path) {
		this.path = path;
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
	public void setFieldName(final String fieldName) {
		this.fieldName = fieldName;
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
	public void setResultsSizeMinimum(final int resultsSizeMinimum) {
		this.resultsSizeMinimum = resultsSizeMinimum;
	}

}