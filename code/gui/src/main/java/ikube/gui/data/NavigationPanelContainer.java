package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.IConstant;
import ikube.gui.toolkit.GuiTools;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;

@Configurable
public class NavigationPanelContainer extends HierarchicalContainer implements IContainer {

	@Autowired
	private transient IMonitorService monitorService;
	@Autowired
	private transient IClusterManager clusterManager;

	public void init() {
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public void init(final Panel target) {
		Tree tree = GuiTools.findComponent(target, Tree.class);

		addItemIfNotPresent(IConstant.DASH, null);
		addItemIfNotPresent(IConstant.INDEXES, null);

		for (final Map.Entry<String, IndexContext> mapEntry : monitorService.getIndexContexts().entrySet()) {
			final IndexContext indexContext = mapEntry.getValue();
			String id = indexContext.getIndexName();
			addItemIfNotPresent(id, IConstant.INDEXES);
		}
		addItemIfNotPresent(IConstant.SERVERS, null);
		for (Map.Entry<String, Server> mapEntry : clusterManager.getServers().entrySet()) {
			Server server = mapEntry.getValue();
			String id = server.getIp();
			addItemIfNotPresent(id, IConstant.SERVERS);
		}

		// We'll repaint the tree just in case
		tree.setContainerDataSource(this);
	}

	private void addItemIfNotPresent(final Object itemId, final Object parentId) {
		if (getItem(itemId) == null) {
			addItem(itemId);
			if (parentId != null) {
				setParent(itemId, parentId);
			}
		}
	}

}