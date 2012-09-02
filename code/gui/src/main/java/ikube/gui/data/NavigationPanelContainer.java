package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.IConstant;
import ikube.gui.toolkit.GuiTools;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorService;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Component;
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
	public void setData(final Panel target, final Object... parameters) {
		Tree tree = (Tree) GuiTools.findComponent(target, IConstant.NAVIGATION_TREE, new ArrayList<Component>());

		addItem(tree, IConstant.DASH, null);
		addItem(tree, IConstant.SEARCH, null);
		addItem(tree, IConstant.INDEXES, null);

		for (final Map.Entry<String, IndexContext> mapEntry : monitorService.getIndexContexts().entrySet()) {
			final IndexContext indexContext = mapEntry.getValue();
			String id = indexContext.getIndexName();
			addItem(tree, id, IConstant.INDEXES);
		}
		addItem(tree, IConstant.SERVERS, null);
		for (Map.Entry<String, Server> mapEntry : clusterManager.getServers().entrySet()) {
			Server server = mapEntry.getValue();
			String id = server.getIp();
			addItem(tree, id, IConstant.SERVERS);
		}

		// We'll repaint the tree just in case
		tree.setContainerDataSource(this);
		tree.requestRepaint();
	}

	private void addItem(final Tree tree, final Object itemId, final Object parentId) {
		if (getItem(itemId) == null) {
			addItem(itemId);
			if (parentId != null) {
				setParent(itemId, parentId);
			} else {
				tree.setChildrenAllowed(itemId, Boolean.FALSE);
			}
		}
	}

}