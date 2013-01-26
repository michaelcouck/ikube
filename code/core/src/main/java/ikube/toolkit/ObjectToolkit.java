package ikube.toolkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Id;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.ReflectionUtils;

/**
 * This class has utility methods to generate object graphs for testing.
 * 
 * @author U365981
 * @since 05-jun-12 15:44:33
 * 
 * @revision 01.00
 * @lastChangedBy Michael Couck
 * @lastChangedDate 05-jun-12 15:44:33
 */
public final class ObjectToolkit {

	private static final Logger LOGGER = Logger.getLogger(ObjectToolkit.class);

	/**
	 * This is the interface that will determine whether an object can be built by the object toolkit. In some cases this is not required
	 * for example when the object is transient then the predicate would return false from a transient check.
	 * 
	 * @author U365981
	 */
	public interface Predicate {
		public boolean perform(final Object target);
	}

	private static final Map<Class<?>, Field> ID_FIELDS = new HashMap<Class<?>, Field>();
	private static final List<Predicate> PREDICATES = new ArrayList<ObjectToolkit.Predicate>();

	/**
	 * Private constructor to avoid instantiation
	 */
	private ObjectToolkit() {
	}

	public static final void registerPredicates(final Predicate... predicates) {
		for (final Predicate predicate : predicates) {
			PREDICATES.add(predicate);
		}
	}

	/**
	 * This method will populate and object and the related graph of the object, i.e. building an object graph from a prototype.
	 * 
	 * @param klass the class of object to build the graph for
	 * @param target the original target object, could be the sub class
	 * @param collections whether collections should also be populated
	 * @param maxDepth maximum depth to populate the fields
	 * @param excludedFields the fields that will not be populated
	 * @return the target with populated fields
	 */
	public static final <T> T populateFields(final Class<?> klass, final T target, final boolean collections, final int maxDepth,
			final String... excludedFields) {
		return populateFields(klass, target, collections, 0, maxDepth, excludedFields);
	}

	/**
	 * This method will populate and object and the related graph of the object, i.e. building an object graph from a prototype.
	 * 
	 * @param klass the class of object to build the graph for
	 * @param target the original target object, could be the sub class
	 * @param collections whether collections should also be populated
	 * @param depth the depth into the graph to descend to build it
	 * @param maxDepth maximum depth to populate the fields
	 * @param excludedFields the fields that will not be populated
	 * @return the target with populated fields
	 */
	public static final <T> T populateFields(final Class<?> klass, final T target, final boolean collections, final int depth,
			final int maxDepth, final String... excludedFields) {
		if (depth > maxDepth) {
			return null;
		}
		ReflectionUtils.FieldFilter fieldFilter = getFieldFilter();
		ReflectionUtils.FieldCallback fieldCallback = getFieldCallback(target, collections, depth, maxDepth, excludedFields);
		ReflectionUtils.doWithFields(klass, fieldCallback, fieldFilter);
		if (!Object.class.equals(klass.getSuperclass())) {
			populateFields(klass.getSuperclass(), target, collections, depth, maxDepth, excludedFields);
		}
		return target;
	}

	private static ReflectionUtils.FieldFilter getFieldFilter() {
		return new ReflectionUtils.FieldFilter() {
			@Override
			public boolean matches(Field field) {
				// We don't set the fields that are static, final
				return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())
						&& !Modifier.isTransient(field.getModifiers());
			}
		};
	}

	private static ReflectionUtils.FieldCallback getFieldCallback(final Object target, final Boolean collections, final int depth,
			final int maxDepth, final String... excludedFields) {
		return new ReflectionUtils.FieldCallback() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
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
							Object collectionEntity = collectionKlass.newInstance();
							populateFields(collectionKlass, collectionEntity, collections, depth + 1, maxDepth, excludedFields);
							if (List.class.isAssignableFrom(field.getType())) {
								fieldValue = new ArrayList();
							} else if (Set.class.isAssignableFrom(field.getType())) {
								fieldValue = new TreeSet();
							} else {
								field.getType().newInstance();
							}
							// fieldValue = Arrays.asList(collectionEntity);
							((Collection) fieldValue).add(collectionEntity);
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
					if (Number.class.isAssignableFrom(fieldValue.getClass()) && ((Number) fieldValue).doubleValue() == 0) {
						// LOGGER.info("Not setting field : " + fieldValue + ", " + field + ", " + target);
						return;
					}
					// LOGGER.info("Setting field : " + field + ", " + fieldValue + ", " + target);
					BeanUtils.setProperty(target, field.getName(), fieldValue);
				} catch (Exception e) {
					LOGGER.error(e.getMessage() + ", " + field + ", " + fieldValue, e);
				}
			}
		};
	}

	private static final boolean isIgnored(final Field field, final String[] excludedFields) {
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

	private static final Object getPrimitive(final Class<?> klass) {
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
	public static final Object getObject(final Class<?> klass) {
		Constructor<?>[] constructors = klass.getConstructors();
		// First try with parameters in the constructor
		for (Constructor<?> constructor : constructors) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length == 1) {
				for (Class<?> parameterType : parameterTypes) {
					try {
						if (Boolean.class.equals(parameterType)) {
							return constructor.newInstance(Boolean.TRUE);
						}
						return constructor.newInstance(RandomStringUtils.randomNumeric(5));
					} catch (Exception e) {
						// LOGGER.debug("Constructor error : " + klass + ", " + parameterType + ", " + defaultConstructorArgument);
					}
				}
			}
		}
		// Finally try with zero parameter constructors
		for (Constructor<?> constructor : constructors) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes == null || parameterTypes.length == 0 && !Modifier.isAbstract(constructor.getModifiers())
					&& !Modifier.isAbstract(klass.getModifiers())) {
				try {
					// Excellent no parameters just create one
					return constructor.newInstance();
				} catch (Exception e) {
					LOGGER.error("Error while instantiating " + constructor, e);
				}
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
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Enum<?> getEnum(final Class<?> enumerationClass) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = enumerationClass.getDeclaredFields();
		for (Field field : fields) {
			if (field.isEnumConstant()) {
				return (Enum<?>) field.get(enumerationClass);
			}
		}
		return null;
	}

	public static final Object getFieldValue(final Object target, final String fieldName) {
		Field field = getField(target, fieldName);
		return ReflectionUtils.getField(field, target);
	}

	public static final void setFieldValue(final Object target, final String fieldName, final Object value) {
		Field field = getField(target, fieldName);
		ReflectionUtils.setField(field, target, value);
	}

	public static final Field getField(final Object target, final String fieldName) {
		Field field = ReflectionUtils.findField(target.getClass(), fieldName);
		field.setAccessible(Boolean.TRUE);
		return field;
	}

	/**
	 * This method will look into an object and try to find the field that is the id field in the object, then set it with the id specified
	 * in the parameter list.
	 * 
	 * @param <T> the type of object to set the id field for
	 * @param object the object to set the id field for
	 * @param id the id to set in the object
	 */
	public static <T> void setIdField(final T object, final long id) {
		if (object == null) {
			return;
		}
		Field idField = getIdField(object.getClass(), null);
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

	/**
	 * Gets the id field in an object. The id field is defined by the {@link Id} annotation.
	 * 
	 * @param klass the class of the object
	 * @param superKlass the super class of the object
	 * @return the id field for the object or null if there is no field designated as the id
	 */
	public static Field getIdField(final Class<?> klass, final Class<?> superKlass) {
		Field idField = ID_FIELDS.get(klass);
		if (idField != null) {
			return idField;
		}
		Field[] fields = superKlass != null ? superKlass.getDeclaredFields() : klass.getDeclaredFields();
		for (Field field : fields) {
			Id idAnnotation = field.getAnnotation(Id.class);
			if (idAnnotation != null) {
				ID_FIELDS.put(klass, field);
				field.setAccessible(Boolean.TRUE);
				return field;
			}
		}
		// Try the super classes
		Class<?> superClass = superKlass != null ? superKlass.getSuperclass() : klass.getSuperclass();
		if (superClass != null && !Object.class.getName().equals(superClass.getName())) {
			return getIdField(klass, superClass);
		}
		return null;
	}

	/**
	 * Gets the value of the id foeld of an object.
	 * 
	 * @param <T> the type of object
	 * @param object the object to find the id field value in
	 * @return the id field value for the object or null if there is no id field or if the id field is null
	 */
	public static <T> Object getIdFieldValue(final T object) {
		if (object == null) {
			return null;
		}
		Field idField = getIdField(object.getClass(), null);
		if (idField != null) {
			try {
				return idField.get(object);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Can't get the id in the field : " + idField + ", of object : " + object, e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Field not accessible : " + idField, e);
			}
		} else {
			LOGGER.info(Logging.getString("Id field not found for object : ", object.getClass().getName()));
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
		Field field = getIdField(klass, null);
		return field != null ? field.getName() : null;
	}

}