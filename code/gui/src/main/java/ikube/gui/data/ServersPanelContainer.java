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

	private static final Logger LOGGER = LoggerFactory.getLogger(ServersPanelContainer.class);

	@Autowired
	private transient IClusterManager clusterManager;
	@Autowired
	private transient IMonitorService monitorService;

	public void init() {
	}

	public void setData(final Panel panel, final Object... parameters) {
		TreeTable treeTable = (TreeTable) GuiTools.findComponent(panel, IConstant.SERVERS_PANEL_TABLE, new ArrayList<Component>());
		setData(treeTable);
	}

	private void setData(final TreeTable treeTable) {
		// treeTable.removeAllItems();
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
			if (treeTable.getItem(ip) == null) {
				LOGGER.info("Adding server : " + ip);
				treeTable.addItem(new Object[] { ip, null, null, null, null, null, null }, ip);
				treeTable.setCollapsed(ip, Boolean.FALSE);
			}
			addSnapshotData(server, treeTable);
		}
		treeTable.requestRepaint();
		treeTable.requestRepaintAll();
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
					break;
				}
			}

			boolean removed = treeTable.removeItem(indexName);
			LOGGER.info("Adding item : " + removed + ", " + indexName);

			Resource resource = new ClassResource(this.getClass(), "/images/icons/red_square.gif", Application.getApplication());
			final Embedded embedded = new Embedded(null, resource);
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
			Object[] columnData = new Object[] { indexName, indexSize, numDocs, timestamp, docsPerMinute, actionName, horizontalLayout };
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