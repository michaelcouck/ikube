package ikube.gui.data;

import ikube.gui.Application;
import ikube.gui.toolkit.GuiTools;
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

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

@Configurable
public class IndexesPanelContainer extends HierarchicalContainer implements IContainer {

	private static final String NAME_COLUMN = "Name";
	private static final String DOCUMENTS_COLUMN = "Docs";
	private static final String SIZE_COLUMN = "Size";
	private static final String OPEN_COLUMN = "Open";
	private static final String MAX_AGE_COLUMN = "Max age";
	private static final String TIMESTAMP_COLUMN = "Timestamp";
	private static final String PATH_COLUMN = "Path";
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(IndexesPanelContainer.class);

	private Resource nameIcon;
	private Resource documentsIcon;
	private Resource sizeIcon;
	private Resource openIcon;
	private Resource maxAgeIcon;
	private Resource timestampIcon;
	private Resource pathIcon;

	@Autowired
	private transient IMonitorService monitorService;

	public void init() {
	}

	public void init(final Panel panel) {
		// LOGGER.info("Init indexes panel : " + panel);
		if (nameIcon == null) {
			nameIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
			documentsIcon = new ClassResource(this.getClass(), "/images/icons/web.gif", Application.getApplication());
			sizeIcon = new ClassResource(this.getClass(), "/images/icons/index_performance.gif", Application.getApplication());
			openIcon = new ClassResource(this.getClass(), "/images/icons/open.gif", Application.getApplication());
			maxAgeIcon = new ClassResource(this.getClass(), "/images/icons/progress_task.gif", Application.getApplication());
			timestampIcon = new ClassResource(this.getClass(), "/images/icons/register_view.gif", Application.getApplication());
			pathIcon = new ClassResource(this.getClass(), "/images/icons/memory_view.gif", Application.getApplication());
		}
		TreeTable treeTable = GuiTools.findComponent(panel, TreeTable.class);
		setData(treeTable);
	}

	@SuppressWarnings("rawtypes")
	private void setData(final TreeTable treeTable) {
		treeTable.removeAllItems();

		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();

		treeTable.addContainerProperty(NAME_COLUMN, String.class, NAME_COLUMN, NAME_COLUMN, nameIcon, null);
		treeTable.addContainerProperty(DOCUMENTS_COLUMN, String.class, DOCUMENTS_COLUMN, DOCUMENTS_COLUMN, documentsIcon, null);
		treeTable.addContainerProperty(SIZE_COLUMN, String.class, SIZE_COLUMN, SIZE_COLUMN, sizeIcon, null);
		treeTable.addContainerProperty(OPEN_COLUMN, String.class, OPEN_COLUMN, OPEN_COLUMN, openIcon, null);
		treeTable.addContainerProperty(MAX_AGE_COLUMN, String.class, MAX_AGE_COLUMN, MAX_AGE_COLUMN, maxAgeIcon, null);
		treeTable.addContainerProperty(TIMESTAMP_COLUMN, String.class, TIMESTAMP_COLUMN, TIMESTAMP_COLUMN, timestampIcon, null);
		treeTable.addContainerProperty(PATH_COLUMN, String.class, PATH_COLUMN, PATH_COLUMN, pathIcon, null);

		for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			IndexContext indexContext = mapEntry.getValue();
			Snapshot snapshot = indexContext.getLastSnapshot();
			if (snapshot == null) {
				snapshot = new Snapshot();
				snapshot.setLatestIndexTimestamp(new Date(0));
			}

			String indexName = indexContext.getIndexName();
			String documents = Long.toString(snapshot.getNumDocs());
			String size = Long.toString(snapshot.getIndexSize());
			String open = indexContext.getMultiSearcher() != null ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
			String maxAge = Long.toString(indexContext.getMaxAge());
			String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(snapshot.getLatestIndexTimestamp());
			String path = indexContext.getIndexDirectoryPath();

			treeTable.addItem(new Object[] { indexName, documents, size, open, maxAge, timestamp, path }, indexName);

			addIndexables(treeTable, indexContext, indexName);
		}
	}

	private void addIndexables(final TreeTable treeTable, final Indexable<?> indexable, final Object parentId) {
		if (indexable.getChildren() != null) {
			for (Indexable<?> childIndexable : indexable.getChildren()) {
				String childId = Integer.toString(childIndexable.hashCode());
				String childName = childIndexable.getName();
				treeTable.addItem(new Object[] { childName, "", "", "", "", "", "" }, childId);
				treeTable.setParent(childId, parentId);
				addIndexables(treeTable, childIndexable, childId);
			}
		}
	}

}