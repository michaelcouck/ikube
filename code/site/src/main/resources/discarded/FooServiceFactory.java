package com.vaadin.incubator.spring.discarded;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.incubator.spring.util.ApplicationObjectSupport;
/*
 * This class is not used at all but it could be used instead of autowiring these
 * to Vaadin UI objects. It makes serialization easier at least. 
 */
@Configurable(preConstruction = true)
public class FooServiceFactory {

	@Autowired
	private FooService service;
	@Autowired
	private ApplicationObjectSupport app;

	
	private static FooServiceFactory instance = new FooServiceFactory();
	
	private FooServiceFactory() {
	}
	
	public FooService getFooService() {
		return service; 
	}
	
	public ApplicationObjectSupport getApp() {
		return app;
	}
	
	public static FooServiceFactory getInstance() {
		return instance;
	}
}
