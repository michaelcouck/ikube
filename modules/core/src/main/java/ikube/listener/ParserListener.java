package ikube.listener;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Event;
import ikube.model.Url;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ParserListener implements IListener {

	protected Logger logger;
	private IDataBase dataBase;

	public ParserListener() {
		this.logger = Logger.getLogger(this.getClass());
		ListenerManager.addListener(this);
	}

	@Override
	public/* synchronized */void handleNotification(Event event) {
		try {
			if (event.getType().equals(Event.LINK)) {
				Url url = (Url) event.getObject();
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put(IConstants.URL, url.getUrl());
				parameters.put(IConstants.NAME, url.getName());
				Url dbUrl = dataBase.find(Url.class, parameters, Boolean.FALSE);
				// logger.debug("Event : " + event + ", " + dbUrl + ", " + url);
				if (dbUrl == null) {
					// logger.debug("Persisting : " + url);
					dataBase.persist(url);
				}
			}
		} catch (Exception e) {
			logger.error("Exception persisting the link : " + event.getObject(), e);
		} finally {
			// notifyAll();
		}
	}

	public void setDataBase(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

}
