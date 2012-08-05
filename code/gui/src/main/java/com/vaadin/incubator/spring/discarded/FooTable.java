package com.vaadin.incubator.spring.discarded;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.incubator.spring.model.Foo;
import com.vaadin.incubator.spring.util.ApplicationObjectSupport;
import com.vaadin.ui.Table;

@Configurable(preConstruction = true)
public class FooTable extends Table implements ItemClickListener {

	private static final long serialVersionUID = 1L;
	private final static Log logger = LogFactory.getLog(FooTable.class);

	@Autowired
	private transient FooService fooService;
	@Autowired
	private transient ApplicationObjectSupport app;

	private List<FooClickListener> listeners = new ArrayList<FooClickListener>();

	public FooTable() {
		super();
		setCaption(app.getMessage("footable.caption"));
		List<Foo> foos = fooService.findFoos();
		if(foos == null || foos.size() < 1) {
			foos = new ArrayList<Foo>();
			foos.add(new Foo());
		}
		setContainerDataSource(createContainer(foos));
		setColumnHeader("id", app.getMessage("footable.id"));
		setColumnHeader("version", app.getMessage("footable.version"));
		setColumnHeader("name", app.getMessage("footable.name"));
		setColumnHeader("number", app.getMessage("footable.number"));
		setColumnHeader("date", app.getMessage("footable.date"));
		setColumnWidth("date", 100);
		setPageLength(5);
		setSelectable(true);
		addListener((ItemClickListener) this);
	}

	public void addFooClickListener(FooClickListener listener) {
		listeners.add(listener);
	}

	private Container createContainer(List<Foo> foos) {
		IndexedContainer con = new IndexedContainer();
		con.addContainerProperty("id", Long.class, null);
		con.addContainerProperty("version", Integer.class, null);
		con.addContainerProperty("name", String.class, null);
		con.addContainerProperty("number", Integer.class, null);
		con.addContainerProperty("date", String.class, null);
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, ApplicationHelper.getCurrentLocale());
		for(Foo foo : foos) {
			Item item = con.addItem(foo);
			item.getItemProperty("id").setValue(foo.getId());
			item.getItemProperty("version").setValue(foo.getVersion());
			item.getItemProperty("name").setValue(foo.getName());
			item.getItemProperty("number").setValue(foo.getNumber());
			if(foo.getDate() != null) {
				item.getItemProperty("date").setValue(df.format(foo.getDate()));
			}
		}
		return con;
	}

	public void refresh() {
		if(logger.isDebugEnabled()) {
			logger.debug("refreshing table");
		}
		setContainerDataSource(createContainer(fooService.findFoos()));
	}

	public void itemClick(ItemClickEvent event) {
		if(logger.isDebugEnabled()) {
			logger.debug("itemClick()");
		}
		Foo foo = (Foo) event.getItemId();
		if(logger.isDebugEnabled()) {
			logger.debug(foo);
		}
		for(FooClickListener listener : listeners) {
			listener.fooClicked(foo);
		}
	}

}
