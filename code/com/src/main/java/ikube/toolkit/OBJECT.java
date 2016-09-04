package ikube.toolkit;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Id;
import java.lang.reflect.*;
import java.util.*;

import static org.springframework.util.ReflectionUtils.*;

/**
 * This class has utility methods to generate object graphs for testing.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-jun-12 15:44:33
 */
public final class OBJECT {

    /**
     * This is the interface that will determine whether an object can be built by the object toolkit. In some cases this is not required
     * for example when the object is transient then the predicate would return false from a transient check.
     *
     * @author Michael Couck
     */
    public interface Predicate {
        boolean perform(final Object target);
    }

    private static final Logger LOGGER = Logger.getLogger(OBJECT.class);
    private static final Map<Class<?>, Field> ID_FIELDS = new HashMap<>();
    private static final List<Predicate> PREDICATES = new ArrayList<>();

    public static void registerPredicates(final Predicate... predicates) {
        Collections.addAll(PREDICATES, predicates);
    }

    /**
     * This method will populate and object and the related graph of the object, i.e. building an object graph from a prototype.
     *
     * @param target         the original target object, could be the sub class
     * @param collections    whether collections should also be populated
     * @param maxDepth       maximum depth to populate the fields
     * @param excludedFields the fields that will not be populated
     * @return the target with populated fields
     */
    public static <T> T populateFields(final T target, final boolean collections, final int maxDepth, final String... excludedFields) {
        return populateFields(target.getClass(), target, collections, 0, maxDepth, excludedFields);
    }

    /**
     * This method will populate and object and the related graph of the object, i.e. building an object graph from a prototype.
     *
     * @param klass          the class of object to build the graph for
     * @param target         the original target object, could be the sub class
     * @param collections    whether collections should also be populated
     * @param maxDepth       maximum depth to populate the fields
     * @param excludedFields the fields that will not be populated
     * @return the target with populated fields
     */
    public static <T> T populateFields(final Class<?> klass, final T target, final boolean collections, final int maxDepth, final String... excludedFields) {
        return populateFields(klass, target, collections, 0, maxDepth, excludedFields);
    }

    /**
     * This method will populate and object and the related graph of the object, i.e. building an object graph from a prototype.
     *
     * @param klass          the class of object to build the graph for
     * @param target         the original target object, could be the sub class
     * @param collections    whether collections should also be populated
     * @param depth          the depth into the graph to descend to build it
     * @param maxDepth       maximum depth to populate the fields
     * @param excludedFields the fields that will not be populated
     * @return the target with populated fields
     */
    public static <T> T populateFields(final Class<?> klass, final T target, final boolean collections, final int depth,
                                       final int maxDepth, final String... excludedFields) {
        if (maxDepth > 100) {
            LOGGER.warn("Warning, the graph depth is very large : ");
        }
        if (depth > maxDepth) {
            return null;
        }
        FieldFilter fieldFilter = getFieldFilter();
        FieldCallback fieldCallback = getFieldCallback(target, collections, depth, maxDepth, excludedFields);
        doWithFields(klass, fieldCallback, fieldFilter);
        if (!Object.class.equals(klass.getSuperclass())) {
            populateFields(klass.getSuperclass(), target, collections, depth, maxDepth, excludedFields);
        }
        return target;
    }

    private static FieldFilter getFieldFilter() {
        return new FieldFilter() {
            @Override
            public boolean matches(final Field field) {
                // We don't set the fields that are static, final
                return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())
                        && !Modifier.isTransient(field.getModifiers());
            }
        };
    }

    private static FieldCallback getFieldCallback(final Object target, final Boolean collections, final int depth, final int maxDepth, final String... excludedFields) {
        class ObjectCreatorFieldCallback implements FieldCallback {
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
                Object fieldValue = null;
                try {
                    if (isIgnored(field, excludedFields)) {
                        // LOGGER.info("Excluding field : " + field + ", " + target);
                        return;
                    }
                    for (Predicate predicate : PREDICATES) {
                        if (!predicate.perform(field)) {
                            return;
                        }
                    }
                    boolean isCollection = Collection.class.isAssignableFrom(field.getType());
                    if (isCollection && collections) {
                        // Init the collection and add one member
                        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                        Type type = stringListType.getActualTypeArguments()[0];
                        if (Class.class.isAssignableFrom(type.getClass())) {
                            Class<?> collectionKlass = (Class<?>) type;
                            if (!collectionKlass.isInterface()) {
                                Object collectionEntity = getObject(collectionKlass); // collectionKlass.newInstance();
                                populateFields(collectionKlass, collectionEntity, collections, depth + 1, maxDepth, excludedFields);
                                if (List.class.isAssignableFrom(field.getType())) {
                                    fieldValue = new ArrayList();
                                } else if (Set.class.isAssignableFrom(field.getType())) {
                                    fieldValue = new TreeSet();
                                } else {
                                    if (!field.getType().isInterface()) {
                                        field.getType().newInstance();
                                    }
                                }
                                // fieldValue = Arrays.asList(collectionEntity);
                                if (fieldValue != null) {
                                    ((Collection) fieldValue).add(collectionEntity);
                                }
                            }
                        }
                    } else if (field.getType().isEnum()) {
                        // Enum type, just get any one
                        fieldValue = getEnum(field.getType());
                    } else if (field.getType().isPrimitive()) {
                        fieldValue = getPrimitive(field.getType());
                    } else {
                        fieldValue = getObject(field.getType());
                    }
                    // If this is not a primitive or a collection and if maxDepth not reached -> populate all the fields
                    if (!isCollection && !field.getType().isPrimitive() && fieldValue != null && depth < maxDepth
                            && !field.getType().getName().startsWith("java")) {
                        populateFields(fieldValue.getClass(), fieldValue, collections, depth + 1, maxDepth, excludedFields);
                    }
                    // Nulls are not interesting to set
                    if (fieldValue == null) {
                        return;
                    }
                    // Don't set a field that is 0, not useful
                    //noinspection ConstantConditions
                    if (Number.class.isAssignableFrom(fieldValue.getClass()) && ((Number) fieldValue).doubleValue() == 0) {
                        // LOGGER.info("Not setting field : " + fieldValue + ", " + field + ", " + target);
                        return;
                    }
                    // LOGGER.info("Setting field : " + field + ", " + fieldValue + ", " + target);
                    BeanUtils.setProperty(target, field.getName(), fieldValue);
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage() + ", " + field + ", " + fieldValue, e);
                }
            }
        }
        return new ObjectCreatorFieldCallback();
    }

    private static boolean isIgnored(final Field field, final String[] excludedFields) {
        if (excludedFields == null) {
            return Boolean.FALSE;
        }
        for (final String excludedField : excludedFields) {
            if (field.getName().equals(excludedField)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public static Object getPrimitive(final Class<?> klass) {
        Random random = new Random();
        if (int.class.getName().equals(klass.getSimpleName())) {
            return Math.abs(random.nextInt());
        } else if (long.class.getName().equals(klass.getSimpleName())) {
            return Math.abs(random.nextLong());
        } else if (boolean.class.getName().equals(klass.getSimpleName())) {
            return random.nextBoolean();
        } else if (double.class.getName().equals(klass.getSimpleName())) {
            return Math.abs(random.nextDouble());
        }
        return null;
    }

    /**
     * This method will just instantiate a class based on the most appropriate constructor. It will try several constructors for
     * {@link Integer} for example, with a string '1234567890', until one works.
     *
     * @param klass the class to instantiate
     * @return the instantiated class or null if there were not constructors that had no parameters or with one string
     */
    public static Object getObject(final Class<?> klass) {
        Constructor<?>[] constructors = klass.getConstructors();
        // Sort the constructors from the least parameters to the most
        Arrays.sort(constructors, new Comparator<Constructor<?>>() {
            @Override
            public int compare(final Constructor<?> o1, final Constructor<?> o2) {
                int paramsOne = o1.getTypeParameters().length;
                int paramsTwo = o2.getTypeParameters().length;
                return paramsOne < paramsTwo ? -1 : paramsOne == paramsTwo ? 0 : 1;
            }
        });
        // First try with zero parameter constructors
        for (final Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes == null || parameterTypes.length == 0 && !Modifier.isAbstract(constructor.getModifiers())
                    && !Modifier.isAbstract(klass.getModifiers())) {
                try {
                    // Excellent no parameters just create one
                    return constructor.newInstance();
                } catch (final Exception e) {
                    LOGGER.error("Error while instantiating " + constructor, e);
                }
            }
        }
        // Now try with parameters in the constructor
        for (final Constructor<?> constructor : constructors) {
            constructor.setAccessible(Boolean.TRUE);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].isPrimitive()) {
                    parameters[i] = getPrimitive(parameterTypes[i]);
                } else if (String.class.isAssignableFrom(parameterTypes[i])) {
                    parameters[i] = RandomStringUtils.randomNumeric(2);
                } else if (Boolean.class.isAssignableFrom(parameterTypes[i])) {
                    parameters[i] = Boolean.TRUE;
                } else {
                    parameters[i] = getObject(parameterTypes[i]);
                }
            }
            try {
                return constructor.newInstance(parameters);
            } catch (final Exception e) {
                LOGGER.debug("Error, oh oh... :(");
                LOGGER.trace(null, e);
            }
        }

        return null;
    }

    /**
     * This method will just look through the enumeration class, find a field and if that field is an enumeration then return it, i.e. any
     * one of the enumerations in the class.
     *
     * @param enumerationClass the class to get one of the enumerations for
     * @return the first enumeration in the class that is found
     * @throws IllegalArgumentException bla...
     * @throws IllegalAccessException bla...
     */
    private static Enum<?> getEnum(final Class<?> enumerationClass) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = enumerationClass.getDeclaredFields();
        for (final Field field : fields) {
            if (field.isEnumConstant()) {
                return (Enum<?>) field.get(enumerationClass);
            }
        }
        return null;
    }

    public static Object getFieldValue(final Object target, final String fieldName) {
        Field field = getField(target, fieldName);
        return ReflectionUtils.getField(field, target);
    }

    public static Field getField(final Object target, final String fieldName) {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(Boolean.TRUE);
        return field;
    }

    /**
     * This method will look into an object and try to find the field that is the id field in the object, then set it with the id specified
     * in the parameter list.
     *
     * @param <T>    the type of object to set the id field for
     * @param object the object to set the id field for
     * @param id     the id to set in the object
     */
    public static <T> void setIdField(final T object, final long id) {
        if (object == null) {
            return;
        }
        Field idField = getIdField(object.getClass());
        if (idField != null) {
            try {
                idField.set(object, id);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Can't set the id : " + id + ", in the field : " + idField + ", of object : " + object, e);
            } catch (IllegalAccessException e) {
                LOGGER.error("Field not accessible : " + idField, e);
            }
        } else {
            LOGGER.warn("No id field defined for object : " + object);
        }
    }

    public static void setField(final Object target, final String fieldName, final Object fieldValue) {
        Field field = findField(target.getClass(), fieldName);
        boolean accessible = field.isAccessible();
        field.setAccessible(Boolean.TRUE);
        if (Modifier.isFinal(field.getModifiers())) {
            try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(Boolean.TRUE);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            } catch (final IllegalAccessException | NoSuchFieldException e) {
                LOGGER.error("Can't set the field non final : " + field, e);
            }
        }
        ReflectionUtils.setField(field, target, fieldValue);
        field.setAccessible(accessible);
    }

    /**
     * Gets the id field in an object. The id field is defined by the {@link Id} annotation.
     *
     * @param klass the class of the object
     * @return the id field for the object or null if there is no field designated as the id
     */
    private static Field getIdField(final Class<?> klass) {
        Field idField = ID_FIELDS.get(klass);
        if (idField != null) {
            return idField;
        }
        Class<?> targetClass = klass;
        do {
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields) {
                Id idAnnotation = field.getAnnotation(Id.class);
                if (idAnnotation != null) {
                    ID_FIELDS.put(klass, field);
                    field.setAccessible(Boolean.TRUE);
                    return field;
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null);
        return null;
    }

    /**
     * Gets the value of the id field of an object.
     *
     * @param <T>    the type of object
     * @param object the object to find the id field value in
     * @return the id field value for the object or null if there is no id field or if the id field is null
     */
    public static <T> Object getIdFieldValue(final T object) {
        if (object == null) {
            return null;
        }
        Field idField = getIdField(object.getClass());
        if (idField != null) {
            try {
                return idField.get(object);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Can't get the id in the field : " + idField + ", of object : " + object, e);
            } catch (IllegalAccessException e) {
                LOGGER.error("Field not accessible : " + idField, e);
            }
        } else {
            LOGGER.info("Id field not found for object : " + object.getClass().getName());
        }
        return null;
    }

    /**
     * Gets the name of the id field in the object.
     *
     * @param klass the class of the object
     * @return the name of the id field or null if there is no id field defined
     */
    public static String getIdFieldName(final Class<?> klass) {
        Field field = getIdField(klass);
        return field != null ? field.getName() : null;
    }

    /**
     * Private constructor to avoid instantiation
     */
    private OBJECT() {
    }

}