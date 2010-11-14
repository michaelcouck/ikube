package ikube.cluster;

import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Event;

import org.jgroups.Message;

public class ServicePublisher extends Service implements IListener {

	public ServicePublisher() throws Exception {
		ListenerManager.addListener(this);
	}

	@Override
	public void handleNotification(Event event) {
		if (event.getType().equals(Event.CLUSTERING)) {
			try {
				Message message = new Message();
				message.setObject(Event.CLUSTERING);
				CHANNEL.send(message);
			} catch (Exception e) {
				logger.error("Exception broardcasting in the cluster : ", e);
			}
		}
	}

}
