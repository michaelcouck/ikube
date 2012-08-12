package ikube.gui.data;

import ikube.service.IMonitorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.HierarchicalContainer;

@Configurable
public class IndexesPanelContainer extends HierarchicalContainer {

	@Autowired
	private transient IMonitorService monitorService;

	public void init() {
	}

}
