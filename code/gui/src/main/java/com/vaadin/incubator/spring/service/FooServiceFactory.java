package com.vaadin.incubator.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.incubator.spring.util.VaadinApplicationObjectSupport;
/*
 * This class is not used at all but it could be used instead of autowiring these
 * to Vaadin UI objects. It makes serialization easier at least. 
 */
@Configurable(preConstruction = true)
public class FooServiceFactory {

	@Autowired
	private FooService service;
	@Autowired
	private VaadinApplicationObjectSupport app;

	
	private static FooServiceFactory instance = new FooServiceFactory();
	
	private FooServiceFactory() {
	}
	
	public FooService getFooService() {
		return service; 
	}
	
	public VaadinApplicationObjectSupport getApp() {
		return app;
	}
	
	public static FooServiceFactory getInstance() {
		return instance;
	}
}
