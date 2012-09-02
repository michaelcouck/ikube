package ikube.gui.data;

import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;

import com.vaadin.data.util.HierarchicalContainer;

public abstract class AContainer extends HierarchicalContainer implements IContainer {

	protected boolean isWorking(final Server server, final IndexContext<?> indexContext) {
		for (Action action : server.getActions()) {
			if (indexContext.getIndexName().equals(action.getIndexName())) {
				return true;
			}
		}
		return false;
	}

}
