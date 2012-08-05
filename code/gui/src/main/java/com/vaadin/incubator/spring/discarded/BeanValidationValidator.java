package com.vaadin.incubator.spring.discarded;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.MessageInterpolator.Context;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import com.vaadin.data.Validator;
import com.vaadin.data.util.MethodProperty;

public class BeanValidationValidator implements Validator {

	private static final long serialVersionUID = 1L;
	private static Log logger = LogFactory.getLog(BeanValidationValidator.class);
	private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	
	private transient javax.validation.Validator validator;
	private String propertyName;
	private Class<?> clazz;
	private MethodProperty method;
	
	public BeanValidationValidator(Class<?> clazz, String propertyName) {
		this.clazz = clazz;		
		this.propertyName = propertyName;		
		this.validator = factory.getValidator();
		try {
			this.method = new MethodProperty(clazz.newInstance(), propertyName);
		} catch(Exception e) {
			throw new IllegalArgumentException("Class '" + clazz + "' must contain default constructor");
		}
	}
	
	@Override
	public boolean isValid(Object value) {
		try {
			validate(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean isRequired() {
		PropertyDescriptor desc = validator.getConstraintsForClass(clazz).getConstraintsForProperty(propertyName);
		if(desc != null) {
			Iterator<ConstraintDescriptor<?>> it = desc.getConstraintDescriptors().iterator();
			while(it.hasNext()) {
				final ConstraintDescriptor<?> d = it.next();
				Annotation a = d.getAnnotation();
				if(a instanceof NotNull) {
					return true;
				} 
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public String getRequiredMessage() {
		return getErrorMessage(null, NotNull.class);
	}

	@Override
	public void validate(final Object value) throws InvalidValueException {
		try {
			method.setValue(value);
		} catch (Exception e) {
			if(logger.isDebugEnabled()) {
				logger.debug("conversion exception, value '" + value + "' -> " + method.getType());
			}
			String msg = getErrorMessage(value);
			if(msg != null) {
				throw new InvalidValueException(msg);
			} else {
				//if not any constraints
				//there should be always some constraints if conversion is needed
				//for example if String -> Integer then Digits annotation
				throw new InvalidValueException("Conversion exception");
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("validating " + method.getValue());
		}
		Set<?> violations = validator.validateValue(clazz, propertyName, method.getValue());
		if(violations.size() > 0) {
			List<String> exceptions = new ArrayList<String>();
			for(Object v : violations) {
				final ConstraintViolation<?> violation = (ConstraintViolation<?>)v;
				String msg = factory.getMessageInterpolator().interpolate(violation.getMessageTemplate(), new Context() {

					@Override
					public ConstraintDescriptor<?> getConstraintDescriptor() {
						return violation.getConstraintDescriptor();
					}

					@Override
					public Object getValidatedValue() {
						return method.getValue();
					}
					
				}, LocaleContextHolder.getLocale());
				exceptions.add(msg);
			}
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < exceptions.size(); i++) {
				if(i != 0) {
					b.append("<br/>");
				}
				b.append(exceptions.get(i));
			}
			throw new InvalidValueException(b.toString());
		}
	}
	
	private String getErrorMessage(final Object value, Class<? extends Annotation>... an) {
		PropertyDescriptor desc = validator.getConstraintsForClass(clazz).getConstraintsForProperty(propertyName);
		Iterator<ConstraintDescriptor<?>> it = desc.getConstraintDescriptors().iterator();
		List<String> exceptions = new ArrayList<String>();
		while(it.hasNext()) {
			final ConstraintDescriptor<?> d = it.next();
			Annotation a = d.getAnnotation();
			boolean skip = false;
			if(an != null && an.length > 0) {
				skip = true;
				for(Class<? extends Annotation> t : an) {
					if(t == a.annotationType()) {
						skip = false;
						break;
					}
				}
			}
			if(!skip) {
				String messageTemplate = null;
				try {
					Method m = a.getClass().getMethod("message"); 
					messageTemplate = (String)m.invoke(a);
				} catch(Exception ex) {
					throw new InvalidValueException("Annotation must have message attribute");
				}
				String msg = factory.getMessageInterpolator().interpolate(messageTemplate, new Context() {
					
					@Override
					public Object getValidatedValue() {
						return value;
					}
					
					@Override
					public ConstraintDescriptor<?> getConstraintDescriptor() {
						return d;
					}
				}, LocaleContextHolder.getLocale());
				exceptions.add(msg);
			}
		}
		if(exceptions.size() > 0){
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < exceptions.size(); i++) {
				if(i != 0) {
					b.append("<br/>");
				}
				b.append(exceptions.get(i));
			}
			return b.toString();
		}
		return null;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.validator = factory.getValidator();
	}
}
