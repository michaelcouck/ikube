package ikube.gui.panel;

import ikube.gui.Application;

import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.Reindeer;

public class MenuPanel extends Panel {

	private static final String IKUBE_CAPTION = "Ikube";
	private static final String ICON_PATH = "/images/Fotolia_11470344_XS_80px.jpg";

	public MenuPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(true);

		AbsoluteLayout horizontalLayout = new AbsoluteLayout();

		Resource icon = new ClassResource(this.getClass(), ICON_PATH, Application.getApplication());
		Embedded image = new Embedded(null, icon);
		horizontalLayout.addComponent(image, "left: 20px;");

		Label caption = new Label(IKUBE_CAPTION, Label.CONTENT_XHTML);
		horizontalLayout.addComponent(caption, "left: 120px; top: 30px;");

		TabSheet tabSheet = new TabSheet();
		tabSheet.setStyleName(Reindeer.TABSHEET_MINIMAL);
		tabSheet.addStyleName(Reindeer.TABSHEET_MINIMAL);

		tabSheet.addTab(new Label((String) null, Label.CONTENT_XHTML), "Menu one", icon);
		tabSheet.addTab(new Label((String) null, Label.CONTENT_XHTML), "Menu two", icon);
		tabSheet.addTab(new Label((String) null, Label.CONTENT_XHTML), "Menu three", icon);
		tabSheet.addTab(new Label((String) null, Label.CONTENT_XHTML), "Menu four", icon);

		horizontalLayout.addComponent(tabSheet, "left: 230px; top: 50px;");

		setContent(horizontalLayout);
	}

}
