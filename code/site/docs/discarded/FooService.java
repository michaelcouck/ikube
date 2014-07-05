package com.vaadin.incubator.spring.discarded;

import java.util.List;



public interface FooService {

	public void saveFoo(Foo foo);

	public Foo updateFoo(Foo foo);

	public List<Foo> findFoos();

}