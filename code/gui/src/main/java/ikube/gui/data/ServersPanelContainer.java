package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.Application;
import ikube.gui.toolkit.GuiTools;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Label;
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
		// LOGGER.info("Init indexes panel : " + panel);
		TreeTable treeTable = GuiTools.findComponent(panel, TreeTable.class);
		if (serverIcon == null) {
			serverIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			sizeIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			docsIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			timestampIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			perMinIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			actionIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());

			treeTable.addContainerProperty(SERVER_COLUMN, String.class, null, SERVER_COLUMN, serverIcon, null);
			treeTable.addContainerProperty(SIZE_COLUMN, Long.class, null, SIZE_COLUMN, sizeIcon, null);
			treeTable.addContainerProperty(DOCS_COLUMN, Long.class, null, DOCS_COLUMN, docsIcon, null);
			treeTable.addContainerProperty(TIMESTAMP_COLUMN, Date.class, null, TIMESTAMP_COLUMN, timestampIcon, null);
			treeTable.addContainerProperty(PER_MINUTE_COLUMN, Long.class, null, PER_MINUTE_COLUMN, perMinIcon, null);
			treeTable.addContainerProperty(ACTION_COLUMN, Label.class, null, ACTION_COLUMN, actionIcon, null);

			treeTable.addListener(new ItemClickEvent.ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					LOGGER.info("Event : " + event);
				}
			});
		}
		setData(treeTable);
	}

	private void setData(final TreeTable treeTable) {
		treeTable.removeAllItems();
		Map<String, Server> servers = clusterManager.getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			String ip = server.getIp();
			treeTable.addItem(new Object[] { ip, null, null, null, null, null }, ip);
			treeTable.setCollapsed(ip, Boolean.FALSE);
			addSnapshotData(server, treeTable);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addSnapshotData(final Server server, final TreeTable treeTable) {
		for (IndexContext indexContext : server.getIndexContexts()) {
			List<Snapshot> snapshots = indexContext.getSnapshots();
			if (snapshots == null || snapshots.size() == 0) {
				continue;
			}
			String indexName = indexContext.getIndexName();
			Snapshot snapshot = snapshots.get(snapshots.size() - 1);
			Long indexSize = snapshot.getIndexSize();
			Long numDocs = snapshot.getNumDocs();
			Date timestamp = snapshot.getLatestIndexTimestamp();
			Long docsPerMinute = snapshot.getDocsPerMinute();
			Resource stopButton = new ClassResource(this.getClass(), "/images/icons/red_square.gif", Application.getApplication());
			Label label = new Label("Stop");
			label.setIcon(stopButton);
			Object[] columnData = new Object[] { indexName, indexSize, numDocs, timestamp, docsPerMinute, label };
			treeTable.addItem(columnData, indexName);
			treeTable.setParent(indexName, server.getIp());
		}
	}

}