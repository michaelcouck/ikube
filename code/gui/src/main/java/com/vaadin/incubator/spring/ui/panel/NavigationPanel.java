package com.vaadin.incubator.spring.ui.panel;

import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorService;

import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window.Notification;

public class NavigationPanel extends Panel {

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;

	public NavigationPanel() {
		setHeight("100%");
		setWidth(null);
		getContent().setSizeUndefined();

		addNavigationTree();
	}

	private void addNavigationTree() {
		Tree tree = new Tree();

		// Navigation for the indexes
		Object indexes = "Indexes";
		tree.addItem(indexes);
		addIndexes(indexes, tree);
		// Navigation for the servers
		Object servers = "Servers";
		tree.addItem(servers);
		addServers(servers, tree);
		// Navigation for searching
		tree.addItem("Search");
		// Navigation for the statistics
		tree.addItem("Statistics");

		for (Iterator<?> it = tree.rootItemIds().iterator(); it.hasNext();) {
			tree.expandItemsRecursively(it.next());
		}

		tree.setImmediate(true);
		addTreeListener(tree);
		addComponent(tree);
	}

	private void addTreeListener(final Tree tree) {
		tree.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Property property = event.getProperty();
				getWindow().showNotification(property.toString(), Notification.TYPE_HUMANIZED_MESSAGE);
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private void addIndexes(final Object parent, final Tree tree) {
		Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
		for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			IndexContext indexContext = mapEntry.getValue();
			tree.addItem(indexContext.getIndexName());
			tree.setParent(indexContext.getIndexName(), parent);
		}
	}

	private void addServers(final Object parent, final Tree tree) {
		Map<String, Server> servers = clusterManager.getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			tree.addItem(server.getAddress());
			tree.setParent(server.getAddress(), parent);
		}
	}

}