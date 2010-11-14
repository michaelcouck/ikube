package ikube.cluster;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class ServiceSubscriber extends Service {

	public ServiceSubscriber() throws Exception {
		CHANNEL.setReceiver(new ReceiverAdapter() {

			@Override
			public void viewAccepted(View view) {
				// Check the cluster manager that we have all the members
				// in the server list
			}

			@Override
			public void suspect(Address address) {
				// Check that we remove the server from the cluster list
			}

			@Override
			public void receive(Message message) {
				// Process any messages that there are. Could be that there is
				// a database available from one of the servers
			}

		});

	}

}