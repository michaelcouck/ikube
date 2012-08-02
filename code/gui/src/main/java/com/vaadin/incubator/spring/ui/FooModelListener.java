package com.vaadin.incubator.spring.ui;

import com.vaadin.incubator.spring.model.Foo;

public interface FooModelListener {
	
	public void fooAdded(Foo model);
	
	public void fooUpdated(Foo model);

}
