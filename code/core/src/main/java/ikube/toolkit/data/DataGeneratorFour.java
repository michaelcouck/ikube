package ikube.toolkit.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Id;

import org.springframework.util.ReflectionUtils;

/**
 * This class will generate an object graph based on the entity and the type. Note that the many to many type of reference is not
 * implemented, it will cause infinite recursion. This can be implemented however it is 17:05 on a Saturday and I just don't feel like it at
 * the moment, too much heavy lifting.
 * 
 * Also bi-directional is not implemented, also infinite recursion.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DataGeneratorFour extends ADataGenerator {

	private int iterations;
	private Class<?>[] classes;
	private EntityManager entityManager;
	private Map<Class<?>, Object> entities;

	public DataGeneratorFour(EntityManager entityManager, int iterations, Class<?>... classes) {
		this.entityManager = entityManager;
		this.iterations = iterations;
		this.classes = classes;
		entities = new HashMap<Class<?>, Object>();
	}

	@Override
	public void generate() throws Exception {
		persist(entityManager);
	}

	protected void persist(EntityManager entityManager) throws Exception {
		entityManager.getTransaction().begin();
		// Persist all the classes that are specified
		for (int i = 0; i < iterations; i++) {
			for (Class<?> klass : classes) {
				Object entity = createInstance(klass);
				entityManager.persist(entity);
			}
		}
		logger.info("Comitting : ");
		entityManager.getTransaction().commit();
	}

	@SuppressWarnings("unchecked")
	protected <T> T createInstance(Class<T> klass) throws Exception {
		T entity = (T) entities.remove(klass);
		if (entity == null) {
			entity = klass.newInstance();
			entities.put(klass, entity);
			// Set the fields
			createFields(klass, entity);
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	protected <C extends Collection<T>, T> C createCollection(Class<C> collectionClass, Class<T> klass) throws Exception {
		Collection<T> collection = collectionClass.newInstance();
		T t = createInstance(klass);
		collection.add(t);
		return (C) collection;
	}

	@SuppressWarnings("unchecked")
	protected <T> T createFields(Class<?> klass, T entity) throws Exception {
		Field[] fields = klass.getDeclaredFields();
		for (Field field : fields) {
			Id id = field.getAnnotation(Id.class);
			if (id != null) {
				continue;
			}
			field.setAccessible(Boolean.TRUE);
			Class<?> fieldClass = field.getType();
			logger.debug("Field class : " + fieldClass);
			Object fieldValue = null;
			// If this is a collection then create the collection
			if (Collection.class.isAssignableFrom(fieldClass)) {
				Type genericType = field.getGenericType();
				Type theActualTypeArgument = null;
				if (ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
					ParameterizedType parameterizedType = (ParameterizedType) genericType;
					Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
					for (Type actualTypeArgument : actualTypeArguments) {
						logger.debug("Actual type argument : " + actualTypeArgument);
						theActualTypeArgument = actualTypeArgument;
					}
				}
				fieldValue = createCollection(ArrayList.class, (Class<?>) theActualTypeArgument);
			} else if (fieldClass.getPackage() == null || fieldClass.getPackage().getName().startsWith("java")) {
				// Java lang class
				Column column = null;
				Annotation[] annotations = field.getAnnotations();
				if (annotations != null) {
					for (Annotation annotation : annotations) {
						if (Column.class.isAssignableFrom(annotation.getClass())) {
							column = (Column) annotation;
							break;
						}
					}
				}
				int length = column != null && column.length() > 0 ? column.length() : 48;
				fieldValue = createInstance(fieldClass, length);
			} else {
				// This is a non Java lang class
				fieldValue = createInstance(fieldClass);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Field : " + field + ", " + fieldClass + ", " + fieldValue);
			}
			ReflectionUtils.setField(field, entity, fieldValue);
		}
		Class<?> superClass = klass.getSuperclass();
		if (!Object.class.equals(superClass)) {
			createFields(superClass, entity);
		}
		return entity;
	}

	public void delete(EntityManager entityManager, Class<?>... classes) {
		for (Class<?> klass : classes) {
			try {
				entityManager.getTransaction().begin();
				logger.info("Deleting : " + klass.getSimpleName());
				List<?> results = entityManager.createQuery("select e from " + klass.getSimpleName() + " as e").getResultList();
				for (Object object : results) {
					entityManager.remove(object);
				}
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				try {
					entityManager.getTransaction().commit();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}

}