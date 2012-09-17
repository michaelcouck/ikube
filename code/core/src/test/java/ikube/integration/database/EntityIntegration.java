package ikube.integration.database;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.database.IDataBase;
import ikube.model.DataSource;
import ikube.model.File;
import ikube.model.IndexContext;
import ikube.model.IndexableColumn;
import ikube.model.IndexableDictionary;
import ikube.model.IndexableEmail;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableFileSystemLog;
import ikube.model.IndexableFileSystemWiki;
import ikube.model.IndexableInternet;
import ikube.model.IndexableTable;
import ikube.model.Search;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ObjectToolkit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

/**
 * This class will scan the class path looking for entities. Then build an entity graph, persist, update and delete the entity, verifying
 * that the operations were successful as a sanity test for the mappings between the entities and the database.
 * 
 * @author U365981
 * @since 16-may-12 14:22:16
 * 
 * @revision 01.00
 * @lastChangedBy Michael Couck
 * @lastChangedDate 16-may-12 14:22:16
 */
@Ignore
public class EntityIntegration {

	private interface EntityTester {
		void doWithEntity(Object entity, Class<?> entityClass);
	}

	private static final Logger LOGGER = Logger.getLogger(EntityIntegration.class);
	/** The maximum depth to build the object graph for an entity, generally one level is fine, could be more of course. */
	private static final int MAX_DEPTH = 1;

	private IDataBase dataBase;
	/** The names of the classes that we will test in the package. */
	private Class<?>[] entityClasses = new Class<?>[] { /* Action.class, */DataSource.class, File.class, IndexableColumn.class,
			IndexableDictionary.class, IndexableEmail.class, IndexableFileSystem.class, IndexableFileSystemLog.class,
			IndexableFileSystemWiki.class, IndexableInternet.class, IndexableTable.class, IndexContext.class, Search.class, Server.class,
			Snapshot.class, Url.class };

	@Before
	public void before() {
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
	}

	@After
	public void after() {
		for (Class<?> klass : entityClasses) {
			List<?> entities = dataBase.find(klass, 0, Integer.MAX_VALUE);
			dataBase.removeBatch(entities);
		}
	}

	/**
	 * Test for one entity(and associated graph) is persisted.
	 */
	@Test
	public void persist() throws Exception {
		doTest(new EntityTester() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void doWithEntity(Object entity, Class<?> entityClass) {
				// Build a graph for all the entities, penetrating one level deep
				populateFields(entityClass, entity, true, 0);
				LOGGER.info("Entity : " + entity);
				// Insert the object graph
				dataBase.persist(entity);
				// Verify that the object is inserted
				List<?> entities = getEntities(entity.getClass());
				assertTrue("There should be at least one " + entity + " in the database : " + entities.size(), entities.size() > 0);
			}
		});
	}

	/**
	 * Test for one entity updated correctly.
	 */
	@Test
	@SuppressWarnings("synthetic-access")
	public void update() throws Exception {
		doTest(new EntityTester() {
			@Override
			public void doWithEntity(final Object entity, final Class<?> entityClass) {
				// Build a graph for all the entities, penetrating one level deep
				populateFields(entityClass, entity, false, 0);
				LOGGER.info("Entity : " + entity);
				// Insert the object graph
				dataBase.persist(entity);
				// Update each field independently
				ReflectionUtils.doWithFields(entityClass, new ReflectionUtils.FieldCallback() {
					@Override
					public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
						Object value = ObjectToolkit.getObject(field.getType());
						field.setAccessible(Boolean.TRUE);
						ReflectionUtils.setField(field, entity, value);
						LOGGER.info("Entity class : " + entityClass + ", " + field);
						dataBase.merge(entity);
						// Check the database that the entity is updated
						String sql = buildSql(entityClass, field);
						Object result;
						try {
							result = dataBase.find(entityClass, sql, new String[] { field.getName() }, new Object[] { field.get(entity) });
						} catch (RuntimeException e) {
							throw e;
						}
						assertNotNull(result);
					}
				}, new ReflectionUtils.FieldFilter() {
					@Override
					public boolean matches(Field field) {
						return containsJpaAnnotations(field)
								&& field.getType().getPackage().getName().contains(Object.class.getPackage().getName());
					}
				});
			}
		});
	}

	/**
	 * Test to remove one entity correctly.
	 */
	@Test
	public void remove() throws Exception {
		doTest(new EntityTester() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void doWithEntity(Object entity, Class<?> entityClass) {
				int existingRecords = dataBase.count(entityClass).intValue();
				// Build a graph for all the entities, penetrating one level deep
				populateFields(entityClass, entity, true, 0);
				LOGGER.info("Entity : " + entity);
				// Insert the object graph
				dataBase.persist(entity);
				// Remove the entity
				dataBase.remove(entity);
				List<?> entities = getEntities(entity.getClass());
				assertTrue(existingRecords <= entities.size());
			}
		});
	}

	private String buildSql(final Class<?> entityClass, final Field field) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select e from ");
		stringBuilder.append(entityClass.getSimpleName());
		stringBuilder.append(" as e where e.");
		stringBuilder.append(field.getName());
		stringBuilder.append(" = :");
		stringBuilder.append(field.getName());
		// Should be something like 'select e from CommercialOffer as e where e.id = :id'
		return stringBuilder.toString();
	}

	/**
	 * This method will build an object graph for the entity to e certain depth. In the case where the entity contains a collection of
	 * classes then the collection will also be instantiated, and one member will be added to the collection.
	 * 
	 * @param klass the class to build a graph for
	 * @param target the target object to populate
	 * @param depth the current depth in the object graph
	 * @return the target object populated to a certain depth
	 */
	@SuppressWarnings("synthetic-access")
	private <T> T populateFields(final Class<?> klass, final T target, final boolean collections, final int depth) {
		ReflectionUtils.doWithFields(klass, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				try {
					Object fieldValue = null;
					boolean isCollection = Collection.class.isAssignableFrom(field.getType());
					if (field.getType().isEnum()) {
						// Enum type, just get any one
						fieldValue = ObjectToolkit.getEnum(field.getType());
					} else if (!isCollection && field.getType().getName().startsWith("java")) {
						// Java type, get the most appropriate
						fieldValue = ObjectToolkit.getObject(field.getType());
					} else if (depth < MAX_DEPTH) {
						if (!isCollection) {
							// If this is not a primitive then populate all the fields
							fieldValue = ObjectToolkit.getObject(field.getType());
							populateFields(fieldValue.getClass(), fieldValue, collections, depth + 1);
						} else if (collections && isCascadeTypeAll(field)) {
							// Check that the mapping is CascadeType.ALL or the persist will throw an exception
							// Init the collection and add one member
							ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
							Class<?> collectionKlass = (Class<?>) stringListType.getActualTypeArguments()[0];
							LOGGER.info("Collection class type : " + collectionKlass);
							Object collectionEntity = collectionKlass.newInstance();
							populateFields(collectionKlass, collectionEntity, collections, 0);
							fieldValue = Arrays.asList(collectionEntity);
						}
					}
					field.setAccessible(Boolean.TRUE);
					field.set(target, fieldValue);
				} catch (Exception e) {
					LOGGER.error(null, e);
				}
			}
		}, new ReflectionUtils.FieldFilter() {
			@Override
			public boolean matches(Field field) {
				// We don't set the fields that are static or id fields, only the column fields
				return containsJpaAnnotations(field);
			}
		});
		return target;
	}

	/**
	 * This method will check that the Jpa mapping is cascade type all so that adding one to the collection and persisting the parent will
	 * not throw an exception that the child is not persisted or transient.
	 * 
	 * @param field the field to check for the cascade type
	 * @return whether the cascade type includes the all type
	 */
	private boolean isCascadeTypeAll(final Field field) {
		OneToMany oneToMany = field.getAnnotation(OneToMany.class);
		CascadeType[] cascadeTypes = oneToMany.cascade();
		for (CascadeType cascadeType : cascadeTypes) {
			if (CascadeType.ALL.equals(cascadeType)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * This method checks to see if the field is part of the Jpa model, which is decided if it has any Jpa annotations on the field.
	 * 
	 * @param field the field to check for Jpa annotations
	 * @return whether this field is a persisted type
	 */
	private boolean containsJpaAnnotations(final Field field) {
		boolean isJpa = Boolean.FALSE;
		Annotation[] annotations = field.getAnnotations();
		for (Annotation annotation : annotations) {
			Class<?> annotationType = annotation.annotationType();
			isJpa |= annotationType.getPackage().getName().equals(Id.class.getPackage().getName());
		}
		return isJpa && field.getAnnotation(Id.class) == null;
	}

	/**
	 * This method just returns all the entities of a certain class type.
	 * 
	 * @param entityClass the class of entity to get from the database
	 * @return all the entities of a certain type
	 */
	private List<?> getEntities(final Class<?> entityClass) {
		return dataBase.find(entityClass, 0, Integer.MAX_VALUE);
	}

	/**
	 * Perform the validation with an instance of each entity class identified in the test.
	 * 
	 * @param entityTester the tester that will execute the test on the entity
	 */
	private void doTest(EntityTester entityTester) {
		for (Class<?> entityClass : entityClasses) {
			try {
				if (Modifier.isAbstract(entityClass.getModifiers())) {
					continue;
				}
				Object entity = entityClass.newInstance();
				try {
					entityTester.doWithEntity(entity, entityClass);
				} finally {
					removeQuietly(entity);
				}
			} catch (Exception e) {
				throw new RuntimeException("An error occurred for class " + entityClass.getName(), e);
			}
		}
	}

	/**
	 * Remove the given entity. Any exceptions thrown while attempting to remove it are ignored.
	 * 
	 * @param entity the entity to remove
	 */
	private void removeQuietly(Object entity) {
		if (entity == null) {
			return;
		}
		// Remove the entity quietly
		try {
			dataBase.remove(entity);
		} catch (Exception e) {
			// Ignore
		}
	}
}