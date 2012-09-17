package ikube.toolkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
	 * Private constructor to avoid instantiation
	 */
	private ObjectToolkit() {
	}

	/**
	 * This method will populate and object and the related graph of the object, i.e. building an object graph from a prototype.
	 * 
	 * @param klass the class of object to build the graph for
	 * @param target the original target object, could be the sub class
	 * @param collections whether collections should also be populated
	 * @param depth the depth into the graph to descend to build it
	 * @param maxDepth maximum depth to populate the fields
	 * @param valueGenerators determine generators for specific types
	 * @return the target with populated fields
	 */
	public static <T> T populateFields(final Class<?> klass, final T target, final boolean collections, final int depth,
			final int maxDepth, final String... excludedFields) {
		if (depth > maxDepth) {
			return null;
		}
		ReflectionUtils.doWithFields(klass, new ReflectionUtils.FieldCallback() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				try {
					if (excludedFields != null) {
						for (final String excludedField : excludedFields) {
							if (field.getName().equals(excludedField)) {
								LOGGER.info("Ignoring field : " + field);
								return;
							}
						}
					}
					Object fieldValue = null;
					boolean isCollection = Collection.class.isAssignableFrom(field.getType());
					if (fieldValue == null) {
						if (isCollection) {
							if (collections) {
								// Init the collection and add one member
								ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
								Class<?> collectionKlass = (Class<?>) stringListType.getActualTypeArguments()[0];
								Object collectionEntity = collectionKlass.newInstance();
								populateFields(collectionKlass, collectionEntity, collections, depth + 1, maxDepth);
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
						} else {
							fieldValue = getObject(field.getType());
						}
					}
					// If this is not a primitive or a collection and if maxDepth not reached -> populate all the fields
					if (!isCollection && !field.getType().isPrimitive() && fieldValue != null && depth < maxDepth
							&& !field.getType().getName().startsWith("java")) {
						populateFields(fieldValue.getClass(), fieldValue, collections, depth + 1, maxDepth);
					}
					field.setAccessible(Boolean.TRUE);
					field.set(target, fieldValue);
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
				}
			}
		}, new ReflectionUtils.FieldFilter() {
			@Override
			public boolean matches(Field field) {
				// We don't set the fields that are static, final
				return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
			}
		});
		if (!Object.class.equals(klass.getSuperclass())) {
			populateFields(klass.getSuperclass(), target, collections, depth, maxDepth);
		}
		return target;
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

}
