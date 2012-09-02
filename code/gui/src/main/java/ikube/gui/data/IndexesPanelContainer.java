package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.Application;
import ikube.gui.Window;
import ikube.gui.toolkit.GuiTools;
import ikube.listener.Event;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Snapshot;
import ikube.service.IMonitorService;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

@Configurable
public class IndexesPanelContainer extends AContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexesPanelContainer.class);

	@Autowired
	private transient IClusterManager clusterManager;
	@Autowired
	private transient IMonitorService monitorService;

	public void init() {
	}

	public void setData(final Panel panel, final Object... parameters) {
		// LOGGER.info("Init indexes panel : " + panel);
		TreeTable treeTable = GuiTools.findComponent(panel, TreeTable.class);
		populateTable(treeTable);
	}

	@SuppressWarnings("rawtypes")
	private void populateTable(final TreeTable treeTable) {
		Resource resource = new ClassResource(this.getClass(), "/images/icons/relaunch.gif", Application.getApplication());
		// treeTable.removeAllItems();
		boolean mustRepaint = false;
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			IndexContext indexContext = mapEntry.getValue();
			Snapshot snapshot = indexContext.getLastSnapshot();
			if (snapshot == null) {
				snapshot = new Snapshot();
				snapshot.setLatestIndexTimestamp(new Date(0));
			}

			String indexName = indexContext.getIndexName();
			String documents = Long.toString(snapshot.getNumDocs());
			String size = Double.toString(((double) snapshot.getIndexSize()) / 1000000);
			String open = indexContext.getMultiSearcher() != null ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
			String maxAge = Long.toString(indexContext.getMaxAge());
			String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(snapshot.getLatestIndexTimestamp());
			String path = indexContext.getIndexDirectoryPath();
			Object[] rowData;

			Item item = treeTable.getItem(indexName);
			// LOGGER.info("Item : " + item);
			if (item == null) {
				mustRepaint = true;
				Embedded startButton = new Embedded(null, resource);
				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.addComponent(startButton);
				rowData = new Object[] { indexName, documents, size, open, maxAge, timestamp, path, horizontalLayout };
				treeTable.addItem(rowData, indexName);
				addIndexables(treeTable, indexContext, indexName);
				addStartButtonListeners(indexName, startButton);
			} else {
				rowData = new Object[] { indexName, documents, size, open, maxAge, timestamp, path };
				Object[] propertyIds = item.getItemPropertyIds().toArray();
				for (int i = 0; i < rowData.length; i++) {
					Object rowDatum = rowData[i];
					Object propertyId = propertyIds[i];
					Property property = item.getItemProperty(propertyId);
					Object propertyValue = property.getValue();
					if (!rowDatum.equals(propertyValue)) {
						// LOGGER.info("Property : " + propertyId + ", " + propertyValue);
						mustRepaint = true;
						property.setValue(rowDatum);
					}
				}
			}
		}
		if (mustRepaint) {
			treeTable.requestRepaint();
		}
	}

	private void addIndexables(final TreeTable treeTable, final Indexable<?> indexable, final Object parentId) {
		if (indexable.getChildren() != null) {
			for (Indexable<?> childIndexable : indexable.getChildren()) {
				String childId = Integer.toString(childIndexable.hashCode());
				String childName = childIndexable.getName();
				treeTable.addItem(new Object[] { childName, "", "", "", "", "", "", null }, childId);
				treeTable.setParent(childId, parentId);
				addIndexables(treeTable, childIndexable, childId);
			}
		}
	}

	private void addStartButtonListeners(final String indexName, final Embedded startButton) {
		// The dialog for the confirmation of terrmination
		class DialogListener implements ConfirmDialog.Listener {
			@Override
			public void onClose(ConfirmDialog confirmDialog) {
				if (confirmDialog.isConfirmed()) {
					long time = System.currentTimeMillis();
					Event startEvent = ListenerManager.getEvent(Event.STARTUP, time, indexName, Boolean.FALSE);
					LOGGER.info("Sending start event : " + startEvent);
					clusterManager.sendMessage(startEvent);
				}
			}
		}
		// The click listener for the user terminate click
		class ClickListener implements MouseEvents.ClickListener {
			@Override
			public void click(ClickEvent event) {
				LOGGER.info("Event : " + event + ",  " + event.getSource());
				if (event.getSource() != null) {
					String caption = "Start indexing - " + indexName;
					String message = "Are you sure you want to start indexing this index [" + indexName + "]";
					ConfirmDialog.show(Window.INSTANCE, caption, message, "Yes", "No", new DialogListener());
				}
			}
		}
		startButton.addListener(new ClickListener());
	}

}