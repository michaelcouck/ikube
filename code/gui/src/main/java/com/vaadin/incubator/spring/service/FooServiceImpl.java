package com.vaadin.incubator.spring.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.vaadin.incubator.spring.model.Foo;


@Service
public class FooServiceImpl implements FooService {
	
	private final static Log logger = LogFactory.getLog(FooServiceImpl.class);
	@PersistenceContext
	private EntityManager em;

	@Secured("ROLE_ADMIN")
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveFoo(Foo foo) {
		if(logger.isDebugEnabled()) {
			logger.debug("saveFoo(" + foo + ")");
		}
		em.persist(foo);
	}
	
	@Secured("ROLE_ADMIN")
	@Transactional(propagation = Propagation.REQUIRED)
	public Foo updateFoo(Foo foo) {
		if(logger.isDebugEnabled()) {
			logger.debug("updateFoo(" + foo + ")");
		}
		return em.merge(foo);
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public List<Foo> findFoos() {
		if(logger.isDebugEnabled()) {
			logger.debug("findFoos()");
		}
		return (List<Foo>)em.createQuery("select f from Foo f").getResultList();
	}
	
}
