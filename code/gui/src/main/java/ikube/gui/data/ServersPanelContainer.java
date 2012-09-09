package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.Application;
import ikube.gui.IConstant;
import ikube.gui.Window;
import ikube.gui.toolkit.GuiTools;
import ikube.listener.Event;
import ikube.listener.ListenerManager;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;

@Configurable
public class ServersPanelContainer extends AContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServersPanelContainer.class);

	@Autowired
	private transient IMonitorService monitorService;
	@Autowired
	private transient IClusterManager clusterManager;

	public void setData(final Panel panel, final Object... parameters) {
		Table treeTable = (Table) GuiTools.findComponent(panel, IConstant.SERVERS_PANEL_TABLE, new ArrayList<Component>());
		setData(treeTable);
	}

	private void setData(final Table treeTable) {
		Map<String, Server> servers = clusterManager.getServers();
		List<Server> sortedServers = new ArrayList<Server>(servers.values());
		Collections.sort(sortedServers, new Comparator<Server>() {
			@Override
			public int compare(Server serverOne, Server serverTwo) {
				return serverOne.getIp().compareTo(serverTwo.getIp());
			}
		});
		for (Server server : sortedServers) {
			addSnapshotData(server, treeTable);
		}
		treeTable.requestRepaint();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addSnapshotData(final Server server, final Table treeTable) {
		for (final IndexContext indexContext : server.getIndexContexts()) {
			List<Snapshot> snapshots = indexContext.getSnapshots();
			if (snapshots == null || snapshots.size() == 0) {
				continue;
			}
			final String indexName = indexContext.getIndexName();
			String itemId = server.getIp() + "-" + indexName;
			boolean isWorking = isWorking(server, indexContext);
			// LOGGER.info("Server : " + server.getIp() + ", " + isWorking + ", " + indexName);
			if (!isWorking) {
				if (treeTable.getItem(itemId) != null) {
					boolean removed = treeTable.removeItem(itemId);
					LOGGER.info("Removing : " + removed + ", " + itemId);
				}
				continue;
			}

			Snapshot snapshot = indexContext.getLastSnapshot();
			String indexSize = Double.toString(((double) snapshot.getIndexSize()) / 1000000);
			Long numDocs = snapshot.getNumDocs();
			Date timestamp = snapshot.getLatestIndexTimestamp();
			Long docsPerMinute = snapshot.getDocsPerMinute();
			String actionName = getActionName(server, indexContext);
			HorizontalLayout horizontalLayout = new HorizontalLayout();

			Object[] columnData = new Object[] { server.getIp(), indexName, indexSize, numDocs, timestamp, docsPerMinute, actionName,
					horizontalLayout };

			Item item = treeTable.getItem(itemId);
			if (item == null) {
				LOGGER.info("Adding item : " + itemId);
				Resource resource = new ClassResource(this.getClass(), "/images/icons/red_square.gif", Application.getApplication());
				Embedded embedded = new Embedded(null, resource);
				horizontalLayout.addComponent(embedded);
				addListeners(indexName, embedded);
				treeTable.addItem(columnData, itemId);
			} else {
				Object[] itemPropertyIds = item.getItemPropertyIds().toArray();
				for (int i = 0; i < columnData.length - 1; i++) {
					Property property = item.getItemProperty(itemPropertyIds[i]);
					Object propertyValue = property.getValue();
					if (!propertyValue.equals(columnData[i])) {
						// LOGGER.info("Changing value : " + columnData[i]);
						property.setValue(columnData[i]);
					}
				}
			}
		}
	}

	/**
	 * This method will find the action that is currently being performed on the index context specified.
	 * 
	 * @param server the server that is performing actions on the index contexts
	 * @param indexContext the index context that is being examined for actions being performed
	 * @return the action name of the action that is currently performing tasks on the index context in question
	 */
	private String getActionName(final Server server, final IndexContext<?> indexContext) {
		if (server.getActions() != null) {
			for (Action action : server.getActions()) {
				if (indexContext.getIndexName().equals(action.getIndexName())) {
					return action.getActionName();
				}
			}
		}
		return null;
	}

	private void addListeners(final String indexName, final Embedded embedded) {
		// The dialog for the confirmation of terrmination
		class DialogListener implements ConfirmDialog.Listener {
			@Override
			public void onClose(ConfirmDialog confirmDialog) {
				if (confirmDialog.isConfirmed()) {
					long time = System.currentTimeMillis();
					Event terminateEvent = ListenerManager.getEvent(Event.TERMINATE, time, indexName, Boolean.FALSE);
					LOGGER.info("Sending terminate event : " + terminateEvent);
					clusterManager.sendMessage(terminateEvent);
				}
			}
		}
		// The click listener for the user terminate click
		class ClickListener implements MouseEvents.ClickListener {
			@Override
			public void click(ClickEvent event) {
				LOGGER.info("Event : " + event + ",  " + event.getSource());
				if (event.getSource() != null) {
					String caption = "Terminate index - " + indexName;
					String message = "Are you sure you want to terminate index [" + indexName + "]";
					ConfirmDialog.show(Window.INSTANCE, caption, message, "Yes", "No", new DialogListener());
				}
			}
		}
		embedded.addListener(new ClickListener());
	}

}