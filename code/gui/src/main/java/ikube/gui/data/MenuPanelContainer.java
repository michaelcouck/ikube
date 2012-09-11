package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.service.IMonitorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Panel;

@Configurable
public class MenuPanelContainer extends AContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MenuPanelContainer.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private transient IClusterManager clusterManager;
	@Autowired
	private IMonitorService monitorService;

	@Override
	public void setData(final Panel panel, final Object... parameters) {
	}

}