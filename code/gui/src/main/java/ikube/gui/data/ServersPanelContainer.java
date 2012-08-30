package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.Application;
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

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

@Configurable
public class ServersPanelContainer extends HierarchicalContainer implements IContainer {

	private static final String SERVER_COLUMN = "Server";
	private static final String SIZE_COLUMN = "Size";
	private static final String DOCS_COLUMN = "Docs";
	private static final String TIMESTAMP_COLUMN = "Timestamp";
	private static final String PER_MINUTE_COLUMN = "Per min";
	private static final String ACTION_COLUMN = "Action";

	private static final Logger LOGGER = LoggerFactory.getLogger(ServersPanelContainer.class);

	private Resource serverIcon;
	private Resource sizeIcon;
	private Resource docsIcon;
	private Resource timestampIcon;
	private Resource perMinIcon;
	private Resource actionIcon;

	@Autowired
	private transient IClusterManager clusterManager;
	@Autowired
	private transient IMonitorService monitorService;

	public void init() {
	}

	public void init(final Panel panel) {
		TreeTable treeTable = GuiTools.findComponent(panel, TreeTable.class);
		if (serverIcon == null) {
			serverIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			sizeIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			docsIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			timestampIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			perMinIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			actionIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());

			treeTable.addContainerProperty(SERVER_COLUMN, String.class, null, SERVER_COLUMN, serverIcon, null);
			treeTable.addContainerProperty(SIZE_COLUMN, String.class, null, SIZE_COLUMN, sizeIcon, null);
			treeTable.addContainerProperty(DOCS_COLUMN, Long.class, null, DOCS_COLUMN, docsIcon, null);
			treeTable.addContainerProperty(TIMESTAMP_COLUMN, Date.class, null, TIMESTAMP_COLUMN, timestampIcon, null);
			treeTable.addContainerProperty(PER_MINUTE_COLUMN, Long.class, null, PER_MINUTE_COLUMN, perMinIcon, null);
			treeTable.addContainerProperty(ACTION_COLUMN, Component.class, null, ACTION_COLUMN, actionIcon, null);
		}
		setData(treeTable);
	}

	private void setData(final TreeTable treeTable) {
		treeTable.removeAllItems();
		Map<String, Server> servers = clusterManager.getServers();
		List<Server> sortedServers = new ArrayList<Server>(servers.values());
		Collections.sort(sortedServers, new Comparator<Server>() {
			@Override
			public int compare(Server serverOne, Server serverTwo) {
				return serverOne.getIp().compareTo(serverTwo.getIp());
			}
		});
		for (Server server : sortedServers) {
			String ip = server.getIp();
			treeTable.addItem(new Object[] { ip, null, null, null, null, null }, ip);
			treeTable.setCollapsed(ip, Boolean.FALSE);
			addSnapshotData(server, treeTable);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addSnapshotData(final Server server, final TreeTable treeTable) {
		for (final IndexContext indexContext : server.getIndexContexts()) {
			List<Snapshot> snapshots = indexContext.getSnapshots();
			if (snapshots == null || snapshots.size() == 0) {
				continue;
			}
			if (!isWorking(server, indexContext)) {
				continue;
			}
			// Verify that this index context has an action working on it
			final String indexName = indexContext.getIndexName();
			Snapshot snapshot = snapshots.get(snapshots.size() - 1);
			String indexSize = Double.toString(((double) snapshot.getIndexSize()) / 1000000);
			Long numDocs = snapshot.getNumDocs();
			Date timestamp = snapshot.getLatestIndexTimestamp();
			Long docsPerMinute = snapshot.getDocsPerMinute();

			String actionName = null;
			if (server.getActions() != null) {
				for (Action action : server.getActions()) {
					actionName = action.getActionName();
				}
			}
			
			Resource resource = new ClassResource(this.getClass(), "/images/icons/red_square.gif", Application.getApplication());
			final Embedded embedded = new Embedded(actionName, resource);
			HorizontalLayout horizontalLayout = new HorizontalLayout();
			horizontalLayout.addComponent(embedded);
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
			Object[] columnData = new Object[] { indexName, indexSize, numDocs, timestamp, docsPerMinute, horizontalLayout };
			treeTable.addItem(columnData, indexName);
			treeTable.setParent(indexName, server.getIp());
		}
	}

	private boolean isWorking(final Server server, final IndexContext<?> indexContext) {
		for (Action action : server.getActions()) {
			if (indexContext.getIndexName().equals(action.getIndexName())) {
				return true;
			}
		}
		return false;
	}

}