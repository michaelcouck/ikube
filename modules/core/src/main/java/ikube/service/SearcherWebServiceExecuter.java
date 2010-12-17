package ikube.service;

import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.toolkit.SerializationUtilities;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SuppressWarnings("unchecked")
public class SearcherWebServiceExecuter implements ISearcherWebServiceExecuter {

	private Logger logger;
	private String endpointUri;
	private String configurationName;
	private String searchString;
	private String fieldName;
	private boolean fragment;
	private int start;
	private int end;
	private int resultsSizeMinimum;

	public SearcherWebServiceExecuter() {
		this.logger = Logger.getLogger(this.getClass());
		ListenerManager.addListener(new IListener() {
			@Override
			public void handleNotification(Event event) {
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

	@Override
	public List<Map<String, String>> execute() throws Exception {
		QName serviceName = new QName(ISearcherWebService.TARGET_NAMESPACE, ISearcherWebService.SERVICE_NAME);
		URL wsdlURL = new URL(endpointUri);
		Service service = Service.create(wsdlURL, serviceName);
		ISearcherWebService searchRemote = service.getPort(ISearcherWebService.class);
		String xml = searchRemote.searchSingle(configurationName, searchString, fieldName, fragment, start, end);
		List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
		if (results.size() < resultsSizeMinimum) {
			logger.warn("Results not expected : " + results);
			ListenerManager.fireEvent(Event.RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
		} else {
			logger.info("Results expected : " + (results != null ? results.size() : 0));
			ListenerManager.fireEvent(Event.NO_RESULTS, System.currentTimeMillis(), null, Boolean.TRUE);
		}
		return results;
	}

	public void setEndpointUri(String endpointUri) {
		this.endpointUri = endpointUri;
	}

	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
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

	public void setResultsSizeMinimum(int resultsSizeMinimum) {
		this.resultsSizeMinimum = resultsSizeMinimum;
	}

}