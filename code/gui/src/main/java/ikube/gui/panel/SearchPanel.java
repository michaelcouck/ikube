package ikube.gui.panel;

import ikube.gui.Application;
import ikube.gui.IConstant;
import ikube.gui.data.IContainer;

import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

public class SearchPanel extends Panel {

	private static final String INDEX_COLUMN = "Index";
	private static final String ID_COLUMN = "Id";
	private static final String SCORE_COLUMN = "Score";
	private static final String FRAGMENT_COLUMN = "Fragment";

	private Resource indexIcon;
	private Resource idIcon;
	private Resource scoreIcon;
	private Resource fragmentIcon;

	public SearchPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(Boolean.TRUE);

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setWidth(100f, Sizeable.UNITS_PERCENTAGE);

		createTable(verticalLayout);

		HorizontalLayout searchInputLayout = new HorizontalLayout();
		searchInputLayout.setMargin(Boolean.TRUE);
		searchInputLayout.setSpacing(Boolean.TRUE);
		createIndexOptions(searchInputLayout);
		createSearchFieldAndButton(searchInputLayout);
		verticalLayout.addComponent(searchInputLayout);

		HorizontalLayout searchStatisticsLayout = new HorizontalLayout();
		searchStatisticsLayout.setMargin(Boolean.TRUE);
		searchStatisticsLayout.setSpacing(Boolean.TRUE);
		createSearchStatisticsFields(searchStatisticsLayout);
		verticalLayout.addComponent(searchStatisticsLayout);

		addComponent(verticalLayout);
	}

	public void setData(Object data) {
		((IContainer) data).setData(this);
	}

	private void createIndexOptions(final ComponentContainer componentContainer) {
		ComboBox indexOptions = new ComboBox();
		indexOptions.setImmediate(true);
		indexOptions.setDescription(IConstant.INDEXES_OPTION_GROUP);
		componentContainer.addComponent(indexOptions);
	}

	private void createSearchFieldAndButton(final ComponentContainer componentContainer) {
		Label searchLabel = new Label("Search terms : ");
		TextField searchField = new TextField();
		searchField.setDescription(IConstant.SEARCH_FIELD);
		Button searchButton = new Button("Go!");
		searchButton.setDescription(IConstant.SEARCH_BUTTON);

		componentContainer.addComponent(searchLabel);
		componentContainer.addComponent(searchField);
		componentContainer.addComponent(searchButton);
	}

	private void createSearchStatisticsFields(final ComponentContainer componentContainer) {
		Label timeTaken = new Label("Time taken : ");
		componentContainer.addComponent(timeTaken);
		Label totalResults = new Label("Total results : ");
		componentContainer.addComponent(totalResults);
	}

	private TreeTable createTable(final ComponentContainer componentContainer) {
		TreeTable treeTable = new TreeTable();
		treeTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		treeTable.setSelectable(Boolean.TRUE);
		treeTable.setImmediate(Boolean.TRUE);
		treeTable.setSortDisabled(Boolean.FALSE);
		treeTable.setDescription(IConstant.SEARCH_PANEL_TREE_TABLE);

		indexIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		idIcon = new ClassResource(this.getClass(), "/images/icons/alphab_sort_co.gif", Application.getApplication());
		scoreIcon = new ClassResource(this.getClass(), "/images/icons/correction_cast.gif", Application.getApplication());
		fragmentIcon = new ClassResource(this.getClass(), "/images/icons/drop_to_frame.gif", Application.getApplication());

		treeTable.addContainerProperty(INDEX_COLUMN, String.class, null, INDEX_COLUMN, indexIcon, null);
		treeTable.addContainerProperty(ID_COLUMN, String.class, null, ID_COLUMN, idIcon, null);
		treeTable.addContainerProperty(SCORE_COLUMN, String.class, null, SCORE_COLUMN, scoreIcon, null);
		treeTable.addContainerProperty(FRAGMENT_COLUMN, String.class, null, FRAGMENT_COLUMN, fragmentIcon, null);

		treeTable.setColumnWidth(INDEX_COLUMN, 80);
		treeTable.setColumnWidth(ID_COLUMN, 150);
		treeTable.setColumnWidth(SCORE_COLUMN, 80);
		treeTable.setColumnWidth(FRAGMENT_COLUMN, 450);
		treeTable.setColumnExpandRatio(FRAGMENT_COLUMN, 100);

		componentContainer.addComponent(treeTable);

		return treeTable;
	}

}