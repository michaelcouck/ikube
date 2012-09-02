package ikube.gui.data;

import ikube.gui.IConstant;
import ikube.gui.toolkit.GuiTools;
import ikube.service.IMonitorService;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

@Configurable
public class IndexOptionsContainer extends HierarchicalContainer implements IContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexOptionsContainer.class);

	@Autowired
	private transient IMonitorService monitorService;

	public void init() {
	}

	public void setData(final Panel panel, final Object... parameters) {
		ComboBox optionGroup = (ComboBox) GuiTools.findComponent(panel, IConstant.INDEXES_OPTION_GROUP, new ArrayList<Component>());
		String[] indexNames = monitorService.getIndexNames();
		LOGGER.info("Index names : " + Arrays.deepToString(indexNames));
		for (String indexName : indexNames) {
			optionGroup.addItem(indexName);
		}
		optionGroup.requestRepaint();
	}

}