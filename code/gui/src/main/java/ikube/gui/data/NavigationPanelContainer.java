package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.IConstant;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

@Configurable
public class NavigationPanelContainer extends HierarchicalContainer {

	@Autowired
	private transient IMonitorService monitorService;
	@Autowired
	private transient IClusterManager clusterManager;

	@SuppressWarnings({ "unused", "rawtypes" })
	public void init() {
		Item dashboard = addItem(IConstant.DASHBOARD);
		Item indexes = addItem(IConstant.INDEXES);
		for (final Map.Entry<String, IndexContext> mapEntry : monitorService.getIndexContexts().entrySet()) {
			final IndexContext indexContext = mapEntry.getValue();
			String id = indexContext.getIndexName();
			addItem(id);
			setParent(id, IConstant.INDEXES);
		}
		addItem(IConstant.SERVERS);
		for (Map.Entry<String, Server> mapEntry : clusterManager.getServers().entrySet()) {
			Server server = mapEntry.getValue();
			String id = server.getAddress();
			addItem(server.getAddress());
			setParent(id, IConstant.SERVERS);
		}
	}

}
