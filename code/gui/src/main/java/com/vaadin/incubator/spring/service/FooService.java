package com.vaadin.incubator.spring.service;

import java.util.List;

import com.vaadin.incubator.spring.model.Foo;


public interface FooService {

	public void saveFoo(Foo foo);

	public Foo updateFoo(Foo foo);

	public List<Foo> findFoos();

}