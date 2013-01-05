package ikube.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.model.Action;
import ikube.model.File;
import ikube.model.IndexContext;
import ikube.model.IndexableColumn;
import ikube.model.IndexableDataSource;
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
import ikube.model.security.Group_;
import ikube.model.security.Role;
import ikube.model.security.User;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ObjectToolkit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
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
public class EntityIntegration {

	private interface EntityTester {
		void doWithEntity(Object entity, Class<?> entityClass);
	}

	private static final Logger LOGGER = Logger.getLogger(EntityIntegration.class);

	private IDataBase dataBase;
	/** The names of the classes that we will test in the package. */
	private Class<?>[] entityClasses = new Class<?>[] { Action.class, IndexableDataSource.class, File.class, IndexableColumn.class,
			IndexableDictionary.class, IndexableEmail.class, IndexableFileSystem.class, IndexableFileSystemLog.class,
			IndexableFileSystemWiki.class, IndexableInternet.class, IndexableTable.class, IndexContext.class, Search.class, Server.class,
			Snapshot.class, Url.class, User.class, Role.class, Group_.class };

	@BeforeClass
	public static void beforeClass() {
		ObjectToolkit.registerPredicates(new ObjectToolkit.Predicate() {

			@Override
			public boolean perform(Object target) {
				Field field = (Field) target;
				return isCascadeTypeAll(field) && containsJpaAnnotations(field);
			}
		});
	}

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
				ObjectToolkit.populateFields(entityClass, entity, true, 0, 3, IConstants.ID);
				// populateFields(entityClass, entity, true, 0);
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
				ObjectToolkit.populateFields(entityClass, entity, true, 0, 3, IConstants.ID);
				// populateFields(entityClass, entity, false, 0);
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
						Object result = null;
						try {
							Object id = ObjectToolkit.getIdFieldValue(entity);
							if (id != null) {
								result = dataBase.find(entity.getClass(), (Long) id);
							}
						} catch (RuntimeException e) {
							throw e;
						}
						assertNotNull(result);
						if (value != null) {
							Object fieldValue = ReflectionUtils.getField(field, entity);
							assertEquals(value, fieldValue);
						}
					}
				}, new ReflectionUtils.FieldFilter() {
					@Override
					public boolean matches(Field field) {
						if (field == null || field.getType() == null || field.getType().getPackage() == null
								|| field.getType().getPackage().getName() == null) {
							return false;
						}
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
			@Override
			@SuppressWarnings("synthetic-access")
			public void doWithEntity(Object entity, Class<?> entityClass) {
				int existingRecords = dataBase.count(entityClass).intValue();
				// Build a graph for all the entities, penetrating one level deep
				ObjectToolkit.populateFields(entityClass, entity, true, 0, 3, IConstants.ID);
				// populateFields(entityClass, entity, true, 0);
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

	/**
	 * This method will check that the Jpa mapping is cascade type all so that adding one to the collection and persisting the parent will
	 * not throw an exception that the child is not persisted or transient.
	 * 
	 * @param field the field to check for the cascade type
	 * @return whether the cascade type includes the all type
	 */
	private static boolean isCascadeTypeAll(final Field field) {
		OneToMany oneToMany = field.getAnnotation(OneToMany.class);
		if (oneToMany == null) {
			return Boolean.FALSE;
		}
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
	private static boolean containsJpaAnnotations(final Field field) {
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