package ikube.cluster;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

public class ServiceSubscriber extends Service {

	public ServiceSubscriber() throws Exception {
		CHANNEL.setReceiver(new Receiver() {

			@Override
			public void viewAccepted(View view) {
				logger.info("View : " + view);
			}

			@Override
			public void suspect(Address address) {
				logger.info("Address : " + address);
			}

			@Override
			public void block() {
			}

			@Override
			public void setState(byte[] bytes) {
				logger.info("State : " + new String(bytes));
			}

			@Override
			public void receive(Message message) {
				logger.info("Message : " + message);
			}

			@Override
			public byte[] getState() {
				return null;
			}
		});

	}

}