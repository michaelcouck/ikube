package com.vaadin.incubator.spring.discarded;

import java.util.Collection;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;

public class BeanValidationForm<T> extends Form {

	private static final long serialVersionUID = 1L;
	
	private BeanItem<T> beanItem;
	
	public BeanValidationForm(T bean) {
		if(bean == null) {
			throw new IllegalArgumentException("Bean can't be null");
		}
		beanItem = new BeanItem<T>(bean);
	}
	
	public BeanValidationForm(T bean, Collection<?> propertyIds) {
		if(bean == null) {
			throw new IllegalArgumentException("Bean can't be null");
		}
		if(propertyIds == null) {
			throw new IllegalArgumentException("PropertyIds can't be null");
		}
		beanItem = new BeanItem<T>(bean, propertyIds);
	}

	@Override
	public void addField(Object propertyId, Field field) {
		BeanValidationValidator validator = new BeanValidationValidator(beanItem.getBean().getClass(), String.valueOf(propertyId));
		field.addValidator(validator);
		field.setPropertyDataSource(beanItem.getItemProperty(propertyId));
		if(validator.isRequired()) {
			field.setRequired(true);
			field.setRequiredError(validator.getRequiredMessage());
		}
		super.addField(propertyId, field);
	}
	
	public void rebind(T bean) {
		if(bean == null) {
			throw new IllegalArgumentException("Bean can't be null");
		}
		beanItem = new BeanItem<T>(bean, getItemPropertyIds());
		for(Object propertyId : getItemPropertyIds()) {
			Field field = getField(propertyId);
			field.setPropertyDataSource(beanItem.getItemProperty(propertyId));
		}
	}

	public BeanItem<T> getBeanItem() {
		return beanItem;
	}
}
