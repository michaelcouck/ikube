package ikube.database;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.model.Persistable;
import ikube.toolkit.ObjectToolkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This test will generate each entity and perform crud operations on each one to verify
 * the constraints and entity to database mapping.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 16-may-12
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class EntityIntegration extends IntegrationTest {

    private interface EntityTester {
        void doWithEntity(final Object entity, final Class<?> entityClass);
    }

    // "parent", "children", "foreignKey", "nameColumn"
    private static final String[] SKIPPED_FIELDS = {IConstants.ID};

    @Autowired
    private IDataBase dataBase;
    /**
     * The names of the classes that we will test in the package.
     */
    private Set<Class<? extends Persistable>> entityClasses;

    @Before
    public void before() {
        entityClasses = new Reflections("ikube.model").getSubTypesOf(Persistable.class);
        ObjectToolkit.registerPredicates(new ObjectToolkit.Predicate() {
            @Override
            public boolean perform(final Object target) {
                Field field = (Field) target;
                return isCascadeTypeAll(field) && containsJpaAnnotations(field);
            }
        });
    }

    @After
    public void after() {
        for (final Class clazz : entityClasses) {
            delete(dataBase, clazz);
        }
    }

    /**
     * Test for one entity(and associated graph) is persisted.
     */
    @Test
    public void persist() throws Exception {
        doTest(new EntityTester() {
            @Override
            public void doWithEntity(final Object entity, final Class<?> entityClass) {
                // Build a graph for all the entities, penetrating one level deep
                ObjectToolkit.populateFields(entityClass, entity, true, 0, 3, SKIPPED_FIELDS);
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
    public void update() throws Exception {
        EntityTester entityTester = new EntityTester() {
            @Override
            public void doWithEntity(final Object entity, final Class<?> entityClass) {
                // Build a graph for all the entities, penetrating one level deep
                ObjectToolkit.populateFields(entityClass, entity, true, 0, 3, SKIPPED_FIELDS);
                // Insert the object graph
                dataBase.persist(entity);
                // Update each field independently
                class FieldCallback implements ReflectionUtils.FieldCallback {
                    @Override
                    public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
                        Object value = ObjectToolkit.getObject(field.getType());
                        field.setAccessible(Boolean.TRUE);
                        ReflectionUtils.setField(field, entity, value);
                        dataBase.merge(entity);
                        // Check the database that the entity is updated
                        Object id = ObjectToolkit.getIdFieldValue(entity);
                        if (id != null) {
                            Object result = dataBase.find(entity.getClass(), (Long) id);
                            assertNotNull(result);
                        }
                        if (value != null) {
                            Object fieldValue = ReflectionUtils.getField(field, entity);
                            assertEquals(value, fieldValue);
                        }
                    }
                }

                class FieldFilter implements ReflectionUtils.FieldFilter {
                    @Override
                    @SuppressWarnings("SimplifiableIfStatement")
                    public boolean matches(final Field field) {
                        if (field == null ||
                                field.getType() == null ||
                                field.getType().getPackage() == null ||
                                field.getType().getPackage().getName() == null) {
                            return Boolean.FALSE;
                        }
                        boolean partOfJavaPackage = field.getType().getPackage().getName().contains(Object.class.getPackage().getName());
                        return containsJpaAnnotations(field) && partOfJavaPackage;
                    }
                }

                ReflectionUtils.doWithFields(entityClass, new FieldCallback(), new FieldFilter());
            }
        };
        doTest(entityTester);
    }

    /**
     * Test to remove one entity correctly.
     */
    @Test
    public void remove() throws Exception {
        doTest(new EntityTester() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void doWithEntity(final Object entity, final Class<?> entityClass) {
                int existingRecords = dataBase.count(entityClass).intValue();
                // Build a graph for all the entities, penetrating one level deep
                ObjectToolkit.populateFields(entityClass, entity, true, 0, 3, SKIPPED_FIELDS);
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
     * This method will check that the Jpa mapping is cascade type all so that adding one
     * to the collection and persisting the parent will not throw an exception that the child
     * is not persisted or transient.
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
     * This method checks to see if the field is part of the Jpa model, which is
     * decided if it has any Jpa annotations on the field.
     *
     * @param field the field to check for Jpa annotations
     * @return whether this field is a persisted type
     */
    private static boolean containsJpaAnnotations(final Field field) {
        boolean isJpa = Boolean.FALSE;
        boolean notIdField = field.getAnnotation(Id.class) == null;
        Annotation[] annotations = field.getAnnotations();
        for (final Annotation annotation : annotations) {
            Class<?> annotationType = annotation.annotationType();
            isJpa |= annotationType.getPackage().getName().equals(Id.class.getPackage().getName());
        }
        return isJpa && notIdField;
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
    private void doTest(final EntityTester entityTester) {
        boolean exceptions = Boolean.FALSE;
        for (final Class<?> entityClass : entityClasses) {
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
            } catch (final Exception e) {
                logger.error("Exception thrown on class : " + entityClass, e);
                exceptions = Boolean.TRUE;
            }
        }
        if (exceptions) {
            throw new RuntimeException("An error occurred for classes : ");
        }
    }

    /**
     * Remove the given entity. Any exceptions thrown while attempting to remove it are ignored.
     *
     * @param entity the entity to remove
     */
    private void removeQuietly(final Object entity) {
        if (entity == null) {
            return;
        }
        try {
            // Remove the entity quietly
            dataBase.remove(entity);
        } catch (Exception e) {
            // Ignore
        }
    }
}