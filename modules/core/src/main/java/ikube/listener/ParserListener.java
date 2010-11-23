package ikube.listener;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Event;
import ikube.model.Url;

import java.util.HashMap;
import java.util.Map;

public class ParserListener implements IListener {

	private IDataBase dataBase;

	public ParserListener() {
		ListenerManager.addListener(this);
	}

	@Override
	public void handleNotification(Event event) {
		if (event.getType().equals(Event.LINK)) {
			// Found a new link
			Url url = (Url) event.getObject();

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.URL, url.getUrl());
			parameters.put(IConstants.NAME, url.getName());

			Url dbUrl = dataBase.find(Url.class, parameters, Boolean.TRUE);
			if (dbUrl != null) {
				dataBase.persist(url);
			}
		}
	}

	public void setDataBase(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

}
