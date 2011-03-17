package ikube.toolkit.datageneration;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Id;

import org.springframework.util.ReflectionUtils;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DataGeneratorFour extends ADataGenerator {

	private int iterations;
	private Class<?>[] classes;
	private EntityManager entityManager;

	public DataGeneratorFour(EntityManager entityManager, int iterations, Class<?>... classes) {
		this.entityManager = entityManager;
		this.iterations = iterations;
		this.classes = classes;
	}

	@Override
	public void generate() throws Exception {
		persist(entityManager);
		references(entityManager);
	}

	protected void persist(EntityManager entityManager) throws Exception {
		entityManager.getTransaction().begin();
		// Persist all the classes that are specified
		for (int i = 0; i < iterations; i++) {
			for (Class<?> klass : classes) {
				Object entity = klass.newInstance();
				generateFieldData(klass, entity);
				entityManager.persist(entity);
			}
		}
		logger.info("Comitting : ");
		entityManager.getTransaction().commit();
	}

	protected void references(EntityManager entityManager) {
		// Link all the object in the database with all the other objects
		// in the database where appropriate
		for (Class<?> resultClass : classes) {
			// Address
			String query = "select e from " + resultClass.getSimpleName() + " e";
			List<?> resultObjects = entityManager.createQuery(query, resultClass).getResultList();
			if (resultObjects.isEmpty()) {
				continue;
			}
			entityManager.getTransaction().begin();
			for (Class<?> targetClass : classes) {
				// logger.info("Class : " + resultClass + ", " + targetClass);
				// TODO Get the parameterized type of the collection if it is one
				// and set it in the target object as a collection. Else get all the fields
				// in the target and set the first object found from the selection
				if (targetClass.equals(resultClass)) {
					continue;
				}
				// Patient
				query = "select e from " + targetClass.getSimpleName() + " e";
				List<?> targetObjects = entityManager.createQuery(query, targetClass).getResultList();
				setTargets(targetClass, resultClass, targetObjects, resultObjects);
				for (Object targetObject : targetObjects) {
					entityManager.merge(targetObject);
				}
			}
			entityManager.getTransaction().commit();
		}
	}

	protected void setTargets(Class<?> targetClass, Class<?> resultClass, List<?> targetObjects, List<?> resultObjects) {
		for (Object targetObject : targetObjects) {
			logger.debug("Target : " + targetObject);
			Field[] targetFields = targetClass.getDeclaredFields();
			for (Field targetField : targetFields) {
				logger.debug("Target field : " + targetField);
				Type type = targetField.getGenericType();
				logger.debug("Target field type : " + type);
				if (targetField.getType().equals(resultClass)) {
					Object resultValue = resultObjects.iterator().next();
					logger.debug("Setting target value : " + resultValue);
					targetField.setAccessible(Boolean.TRUE);
					ReflectionUtils.setField(targetField, targetObject, resultValue);
					continue;
				}
				if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
					Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
					if (Collection.class.isAssignableFrom(targetField.getType())) {
						if (typeArguments == null || typeArguments.length == 0) {
							continue;
						}
						for (Type typeArgument : typeArguments) {
							logger.debug("Type argument : " + typeArgument);
							if (typeArgument.equals(resultClass)) {
								targetField.setAccessible(Boolean.TRUE);
								logger.debug("Setting target value : " + resultObjects);
								try {
									Collection<Object> resultObjectsCollection = new ArrayList<Object>();
									for (Object object : resultObjects) {
										resultObjectsCollection.add(object);
									}
									ReflectionUtils.setField(targetField, targetObject, resultObjectsCollection);
								} catch (Exception e) {
									logger.error("", e);
								}
								continue;
							}
						}
						continue;
					}
					continue;
				}
			}
		}
		// Do the super class
		Class<?> targetSuperClass = targetClass.getSuperclass();
		if (!targetSuperClass.equals(Object.class)) {
			setTargets(targetSuperClass, resultClass, targetObjects, resultObjects);
		}
	}

	protected <T> T generateFieldData(Class<?> klass, T entity) {
		Field[] fields = klass.getDeclaredFields();
		for (Field field : fields) {
			Id id = field.getAnnotation(Id.class);
			if (id != null) {
				continue;
			}
			field.setAccessible(Boolean.TRUE);
			Column column = field.getAnnotation(Column.class);
			int length = 0;
			if (column != null) {
				length = column.length();
			}
			Class<?> fieldClass = field.getType();
			Object fieldValue = instanciateObject(fieldClass, length);
			if (logger.isDebugEnabled()) {
				logger.debug("Field : " + field + ", " + fieldClass + ", " + fieldValue);
			}
			ReflectionUtils.setField(field, entity, fieldValue);
		}
		Class<?> superClass = klass.getSuperclass();
		if (!Object.class.equals(superClass)) {
			generateFieldData(superClass, entity);
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